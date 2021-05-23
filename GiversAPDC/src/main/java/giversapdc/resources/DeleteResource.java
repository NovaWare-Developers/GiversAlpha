package giversapdc.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Value;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import giversapdc.util.AuthToken;
import giversapdc.util.EventData;
import giversapdc.util.GroupData;
import giversapdc.util.InstitutionData;
import giversapdc.util.UserData;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteResource {

	private static final String USER = "USER";
	private static final String INST_OWNER = "INST_OWNER";
	private static final String GROUP_OWNER = "GROUP_OWNER";
	private static final String SU = "SU";
	
	private static final String PROFILE = "profile";
	private static final String INSTITUTION = "institution";
	private static final String EVENT = "event";
	private static final String GROUP = "group";
	
	//private static final String PROJECT_ID = "giversapdc";
	//private static final String BUCKET_ID = "giversapdc.appspot.com";
	private static final String PROJECT_ID = "givers-volunteering";
	private static final String BUCKET_ID = "givers-volunteering.appspot.com";
	
	private static final Logger LOG = Logger.getLogger(DeleteResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private KeyFactory profileKeyFactory = datastore.newKeyFactory().setKind("Profile");
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	private KeyFactory institutionKeyFactory =  datastore.newKeyFactory().setKind("Institution");
	private KeyFactory eventKeyFactory = datastore.newKeyFactory().setKind("Event");
	private KeyFactory groupKeyFactory = datastore.newKeyFactory().setKind("Group");
	private KeyFactory rbacKeyFactory = datastore.newKeyFactory().setKind("AccessControl");
	
	
	public DeleteResource() { }
	
	/*
	 * Method that calls the rest /delete/user.
	 * Allows a user to delete his own account, given a password and confirmation.
	 * If user is an owner of an institution, he must first give ownership to someone else or delete the institution.
	 * Removes user from all future events that he was partaking in.
	 */
	@DELETE
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUser(UserData data) {
		LOG.info("Attempt to delete account.");
		
		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			//Check login
			Response r = checkLogin(txn, data.at, authToken);
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			//Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deleteUser");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			String username = authToken.getString("username");
			Key userKey = userKeyFactory.newKey(username);
			Key profileKey = profileKeyFactory.addAncestors(PathElement.of("User", username)).newKey(username);
			Entity user = txn.get(userKey);
			
			//Check if given password and confirmation are correct
			if( !DigestUtils.sha512Hex(data.password).equals(user.getString("password")) 
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password")) ) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Password ou confirmação incorretas.").build();
			}
			
			String role = authToken.getString("role");
			if( role.equals(INST_OWNER) || role.equals(GROUP_OWNER) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você é dono de um/a grupo/instituição, logo não pode remover a sua conta.").build();
			}
			
			//Get user's ongoing and future events
			List<Value<String>> eventsJoined = user.getList("eventsJoined");
			List<Entity> eventsEntity = new ArrayList<Entity>();
			for( Value<String> v : eventsJoined ) {
				Key eventKey = eventKeyFactory.newKey(v.get().replaceAll("\\s+", "").toLowerCase());
				Entity event = txn.get(eventKey);
				if( event.getLong("date_end") > System.currentTimeMillis()) {
					eventsEntity.add(event);
				}
			}
			
			//Check if user is in any ongoing event
			for( Entity e : eventsEntity ) {
				if( e.getLong("date_start") < System.currentTimeMillis() ) {
					LOG.warning(authToken.getString("username") + " attempt to delete account while joined ongoing events.");
					txn.rollback();
					return Response.status(Status.FORBIDDEN)
							.entity("Você está a participar num evento decorrente. Tente novamente quando o evento terminar.").build();
				}
			}
			
			//Remove user from joined future events
			for( Entity e : eventsEntity ) {
				List<Value<String>> newParticipants = removeStringFromListValuesString(e.getList("participants"), authToken.getString("username"));
				e = Entity.newBuilder(e)
						.set("participants", newParticipants)
						.build();
				txn.put(e);
			}
			
			//Get joined groups and remove user from said groups
			List<Value<String>> groupsJoined = user.getList("groupsJoined");
			for( Value<String> v : groupsJoined ) {
				Key groupKey = groupKeyFactory.newKey(v.get().replaceAll("\\s+", "").toLowerCase());
				Entity group = txn.get(groupKey);
				List<Value<String>> newParticipants = removeStringFromListValuesString(group.getList("participants"), authToken.getString("username"));
				group = Entity.newBuilder(group)
						.set("participants", newParticipants)
						.build();
				txn.put(group);
			}
			
			//Get and Delete all active tokens of the user, and delete the user account afterwards
			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("AuthToken")
					.setFilter(PropertyFilter.eq("username", username))
					.build();
			QueryResults<Entity> tasks = txn.run(query);
			
			tasks.forEachRemaining(autTok -> { 
				txn.delete(autTok.getKey());
			});

			txn.delete(userKey, profileKey);
			txn.commit();
			return Response.ok().entity("Conta removida com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if( txn.isActive() )
				txn.rollback();
		}
	}
	
	
	/*
	 * Method that calls the rest /delete/institution.
	 * Allows a user to delete an institution, as long as he is the owner. Cascade deletes all future events, 
	 * but keeps completed and ongoing ones.
	 */
	@DELETE
	@Path("/institution")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteInstitution(InstitutionData data) {
		LOG.info("Attempt to delete institution.");
		
		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			//Check login
			Response r = checkLogin(txn, data.at, authToken);
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			//Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deleteInstitution");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			String username = authToken.getString("username");
			Key userKey = userKeyFactory.newKey(username.toLowerCase());
			Entity user = txn.get(userKey);
			
			//Check if institution name is valid
			if( data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome da instituição inválido.").build();
			}
			
			String instName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key instKey = institutionKeyFactory.newKey(instName);
			Entity inst = txn.get(instKey);
			
			//Check if institution exists in database
			if( inst == null ) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Instituição inexistente.").build();
			}
			
			//Check if given password and confirmation are correct
			if( !DigestUtils.sha512Hex(data.password).equals(user.getString("password")) 
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password")) ) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Password ou confirmação incorretas.").build();
			}
			
			//Check if user attempting delete is the owner of the institution
			if( !inst.getString("owner").equals(username) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Apenas o dono da instituição a consegue remover.").build();
			}

			//Check if the institution has any ongoing or future events. If so, can't delete
			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("Event")
					.setFilter(CompositeFilter.and(
							PropertyFilter.eq("institution".toLowerCase(), instName), 
							PropertyFilter.ge("date_end", System.currentTimeMillis())))
					.build();
			QueryResults<Entity> tasks = txn.run(query);
			
			if( tasks.hasNext() ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Instituição tem eventos decorrentes/agendados, não pode ser removida.").build();
			}
			
			//If role is SU, stays SU
			if( !user.getString("role").equals(SU) ) {
				//Get user's list of owned institutions
				query = Query.newEntityQueryBuilder()
						.setKind("Institution")
						.setFilter(PropertyFilter.eq("owner", username))
						.build();
				tasks = txn.run(query);
				
				int instCount = 0;
				while( tasks.hasNext() ) {
					instCount++;
					tasks.next();
				}
				
				//If user has only this institution left (which is getting deleted), he loses the role of inst owner
				if( instCount == 1 ) {
					user = Entity.newBuilder(user)
							.set("role", USER)
							.build();
				
					//Get user's auth tokens
					query = Query.newEntityQueryBuilder()
							.setKind("AuthToken")
							.setFilter(PropertyFilter.eq("username", username))
							.build();
					tasks = txn.run(query);
					
					//Change role on all of user's active tokens
					tasks.forEachRemaining(autTok -> { 	
						autTok = Entity.newBuilder(autTok)
								.set("role", USER)
								.build();
						txn.put(autTok);
					});
				}
			}
			
			txn.delete(instKey);
			txn.put(user);
			txn.commit();
			return Response.ok().entity("Instituição removida com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method that calls the rest /delete/event.
	 * Allows a user to delete an event as long as it's not ongoing or in the past.
	 * User must be the owner of the institution to which the event belongs.
	 */
	@DELETE
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteEvent(EventData data) {
		LOG.info("Attempt to delete event.");
		
		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			//Check login
			Response r = checkLogin(txn, data.at, authToken);
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			//Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deleteEvent");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			String username = authToken.getString("username").toLowerCase();
			Key userKey = userKeyFactory.newKey(username);
			Entity user = txn.get(userKey);
			
			//Check if event name is valid
			if( data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			//Check if given password and confirmation are correct
			if( !DigestUtils.sha512Hex(data.password).equals(user.getString("password")) 
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password")) ) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Password ou confirmação incorretas.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);
			
			//Check if event exists in database
			if( event == null ) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			String instName = event.getString("institution").replaceAll("\\s+", "").toLowerCase();
			Key instKey = institutionKeyFactory.newKey(instName);
			Entity inst = txn.get(instKey);

			//Check if user attempting delete is the owner of the institution hosting the event
			if( !inst.getString("owner").toLowerCase().equals(username) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Apenas o dono da instituição e do evento o pode remover.").build();
			}

			//Check if event is already finished. If so, can't delete
			if( event.getLong("date_end") < System.currentTimeMillis() ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento já terminou, não pode ser removido.").build();
			}
			
			//Check if event is ongoing. If so, can't delete.
			if( event.getLong("date_start") < System.currentTimeMillis() && event.getLong("date_end") > System.currentTimeMillis()) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento está a decorrer, não pode ser removido.").build();
			}

			//Check if the event has any participants. If so, can't delete
			if( !event.getList("participants").isEmpty() ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento já tem participantes inscritos, não pode ser removido.").build();
			}

			//If event is not ongoing, is future, and has no participants -> can delete
			txn.delete(eventKey);
			txn.commit();
			return Response.ok().entity("Evento removido com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method that calls the rest /delete/group.
	 * Allows a user to delete a group as long as he's the owner and group is empty.
	 */
	@DELETE
	@Path("/group")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteGroup(GroupData data) {
		LOG.info("Attempt to delete group.");
		
		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			//Check login
			Response r = checkLogin(txn, data.at, authToken);
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			//Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deleteGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username"));
			Entity user = txn.get(userKey);
			String username = authToken.getString("username");
						
			//Check if group name is valid
			if( data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do grupo inválido.").build();
			}
			
			//Check if given password and confirmation are correct
			if( !DigestUtils.sha512Hex(data.password).equals(user.getString("password")) 
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password")) ) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Password ou confirmação incorretas.").build();
			}
			
			String groupName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = eventKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);
			
			//Check if group exists in database
			if( group == null ) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Grupo não existe.").build();
			}

			//Check if user attempting delete is the owner of the group
			if( !group.getString("owner").toLowerCase().equals(username) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Apenas o dono do grupo o pode remover.").build();
			}

			//Check if the group has any participants. If so, can't delete
			if( !group.getList("participants").isEmpty() ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Grupo com membros, não pode ser removido.").build();
			}
			
			if( group.getList("eventsJoined").size() > 0 ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Grupo está inscrito em eventos, não pode ser removido.").build();
			}
			
			
			//If role is SU, stays SU
			if( !user.getString("role").equals(SU) ) {
				//Get user's list of owned groups
				Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("Group")
					.setFilter(PropertyFilter.eq("owner", username))
					.build();
				QueryResults<Entity> tasks = txn.run(query);

				int groupCount = 0;
				while( tasks.hasNext() ) {
					groupCount++;
					tasks.next();
				}
				
				//If user has only this group left (which is getting deleted), he loses the role of group owner
				if( groupCount == 1 ) {
				
					//If user has no groups left, he loses the role of group owner
					if( !tasks.hasNext() ) {
						user = Entity.newBuilder(user)
								.set("role", USER)
								.build();
					
						//Get user's auth tokens
						query = Query.newEntityQueryBuilder()
								.setKind("AuthToken")
								.setFilter(PropertyFilter.eq("username", username))
								.build();
						tasks = txn.run(query);
						
						//Change role on all of user's active tokens
						tasks.forEachRemaining(autTok -> { 	
							autTok = Entity.newBuilder(autTok.getKey())
									.set("role", USER)
									.build();
							txn.put(autTok);
						});
					}
				}
			}
			
			txn.put(user);
			txn.delete(groupKey);
			txn.commit();
			return Response.ok().entity("Grupo removido com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method that calls the rest /delete/profilePhoto. Allows a user to delete his
	 * profile photo.
	 */
	@DELETE
	@Path("/photoProfile")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deletePhotoProfile(UserData data) {
		LOG.info("Attempt to delete profile photo.");

		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			// Check login
			Response r = checkLogin(txn, data.at, authToken);
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deletePhotoProfile");
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			String username = authToken.getString("username");
			Key profileKey = profileKeyFactory.addAncestors(PathElement.of("User", username)).newKey(username);
			Entity profile = txn.get(profileKey);

			// Rebuild the profile, with old data + new data
			profile = Entity.newBuilder(profile)
					.set("photo", "")
					.build();

			Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
			storage.delete(BUCKET_ID, PROFILE + authToken.getString("username"));

			txn.put(profile);
			txn.commit();

			return Response.ok().entity("Foto de perfil removida com sucesso.").build();

		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method that calls the rest /delete/eventPhoto. Allows the owner of an event to delete the event photo
	 */
	@DELETE
	@Path("/photoEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deletePhotoEvent(EventData data) {
		LOG.info("Attempt to delete event photo.");

		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			// Check login
			Response r = checkLogin(txn, data.at, authToken);
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deletePhotoEvent");
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check if event name is valid
			if (data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}

			/*
			// Check if given password and confirmation are correct
			if (!DigestUtils.sha512Hex(data.password).equals(user.getString("password"))
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password"))) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Incorrect password or confirmation.").build();
			}
			*/
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);

			// Check if event exists in database
			if (event == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			String username = authToken.getString("username").toLowerCase();
			String instName = event.getString("institution").replaceAll("\\s+", "").toLowerCase();
			Key instKey = institutionKeyFactory.newKey(instName);
			Entity inst = txn.get(instKey);

			// Check if user attempting delete is the owner of the institution hosting the event
			if (!inst.getString("owner").toLowerCase().equals(username)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Apenas o dono da instituição e do evento pode remover a foto do evento.").build();
			}

			// Check if event is already finished. If so, can't delete photo
			if (event.getLong("date_end") < System.currentTimeMillis()) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento já terminou, não pode remover a foto deste.").build();
			}

			// Check if event is ongoing. If so, can't delete.
			if (event.getLong("date_start") < System.currentTimeMillis() && event.getLong("date_end") > System.currentTimeMillis()) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento está a decorrer, não pode remover a foto deste.").build();
			}

			// Build the new event and put on the database.
			event = Entity.newBuilder(event)
					.set("photo", "")
					.build();

			Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
			storage.delete(BUCKET_ID, EVENT + eventName);

			txn.put(event);
			txn.commit();
			return Response.ok().entity("Foto do evento removida com sucesso.").build();

		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}


	/*
	 * Method that calls the rest /delete/instPhoto. Allows an owner to delete the photo of one of his institutions
	 */
	@DELETE
	@Path("/photoInstitution")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deletePhotoInstitution(InstitutionData data) {
		LOG.info("Attempt to delete institution photo.");

		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			// Check login
			Response r = checkLogin(txn, data.at, authToken);
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deletePhotoInstitution");
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check if institution name is valid
			if( data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome da instituição inválido.").build();
			}

			String instName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key instKey = institutionKeyFactory.newKey(instName);
			Entity inst = txn.get(instKey);

			// Check if institution exists in database
			if (inst == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Instituição não existe.").build();
			}

			/*
			// Check if given password and confirmation are correct
			if (!DigestUtils.sha512Hex(data.password).equals(user.getString("password"))
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password"))) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Incorrect password or confirmation.").build();
			}
			*/

			String username = authToken.getString("username");
			
			// Check if user attempting delete the photo is the owner of the institution
			if (!inst.getString("owner").equals(username)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Apenas o dono da instituição pode remover a foto desta.").build();
			}

			inst = Entity.newBuilder(inst)
					.set("photo", "")
					.build();

			Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
			storage.delete(BUCKET_ID, INSTITUTION + instName);

			txn.put(inst);
			txn.commit();
			return Response.ok().entity("Foto da instituição removida com sucesso.").build();

		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method that calls the rest /delete/instPhoto. Allows an owner to delete the photo of one of his institutions
	 */
	@DELETE
	@Path("/photoGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deletePhotoGroup(GroupData data) {
		LOG.info("Attempt to delete group photo.");

		Transaction txn = datastore.newTransaction();
		try {
			Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
			Entity authToken = txn.get(authTokenKey);

			// Check login
			Response r = checkLogin(txn, data.at, authToken);
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check RBAC
			r = checkRBAC(txn, authToken.getString("role"), "deletePhotoGroup");
			if (r.getStatus() != 200) {
				txn.rollback();
				return r;
			}

			// Check if group name is valid
			if (data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome de grupo inválido.").build();
			}

			String groupName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = groupKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);

			// Check if group exists in database
			if (group == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Grupo não existe.").build();
			}

			/*
			// Check if given password and confirmation are correct
			if (!DigestUtils.sha512Hex(data.password).equals(user.getString("password"))
					|| !DigestUtils.sha512Hex(data.passwordConfirm).equals(user.getString("password"))) {
				LOG.warning("Incorrect confirmation.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Incorrect password or confirmation.").build();
			}
			*/

			String username = authToken.getString("username");
			
			// Check if user attempting delete the photo is the owner of the institution
			if (!group.getString("owner").equals(username)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Apenas o dono do grupo pode remover a foto deste.").build();
			}

			group = Entity.newBuilder(group)
					.set("photo", "")
					.build();

			Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
			storage.delete(BUCKET_ID, GROUP + groupName);

			txn.put(group);
			txn.commit();
			return Response.ok().entity("Foto de grupo removida com sucesso.").build();

		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method used to clean expired tokens. 
	 * This is done automatically every x hours by a scheduled cloud operation.
	 */
	@POST
	@Path("/tokens")
	public void deleteExpiredTokens() {
		
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("AuthToken")
				.setFilter(PropertyFilter.le("expirationDate", System.currentTimeMillis()))
				.build();
		QueryResults<Entity> tasks = datastore.run(query);
		
		tasks.forEachRemaining(at -> {
			datastore.delete(at.getKey());
		});
	}
	
	
	/*
	 * Used to check if a user is in a session, by doing some checks to authToken
	 */
	private Response checkLogin(Transaction txn, AuthToken at, Entity authToken) {
		//Check both given token and database token
		if( at == null || authToken == null ) {
			LOG.warning("Attempt to operate with no login.");
			txn.rollback();
			return Response.status(Status.NOT_FOUND).entity("Login inexistente.").build();
		}
		
		//If token is found, check for validity
		if( authToken.getLong("expirationDate") < System.currentTimeMillis() ) {
			LOG.warning("Auth Token expired.");
			txn.delete(authToken.getKey());
			txn.commit();
			return Response.status(Status.FORBIDDEN).entity("Auth Token expirado. Faça login antes de tentar novamente.").build();
		}
		
		return Response.ok().build();
	}
	
	
	/*
	 * Used to check if the user can perform a certain method
	 */
	private Response checkRBAC(Transaction txn, String role, String methodName) {
		Key methodKey = rbacKeyFactory.newKey(methodName.toLowerCase());
		Entity method = txn.get(methodKey);
		
		//Check if method exists
		if( method == null ) {
			txn.rollback();
			return Response.status(Status.NOT_FOUND).entity("Método especificado não existe.").build();
		}
		
		//Check RBAC
		if( method.getBoolean(role) == false ) {
			LOG.warning("User doesn't have permission for this action.");
			txn.rollback();
			return Response.status(Status.FORBIDDEN).entity("Você não tem permissão para realizar esta ação.").build();
		} 
		else
			return Response.ok().build();
	}
	
	
	private List<Value<String>> removeStringFromListValuesString(List<Value<String>> list, String toRem) {
		List<Value<String>> listNew = new ArrayList<Value<String>>();
		
		for( Value<String> v : list ) 
			listNew.add(v);

		listNew.remove(StringValue.of(toRem));
		
		return listNew;
	}
	
}
