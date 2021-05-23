package giversapdc.resources;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.Consumes;
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
import com.google.cloud.datastore.Entity.Builder;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import giversapdc.util.AuthToken;
import giversapdc.util.EventData;
import giversapdc.util.GroupData;
import giversapdc.util.InstitutionData;
import giversapdc.util.UserData;

@Path("/edit")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EditResource {

	private static final String SU = "SU";
	private static final String BO = "BO";
	
	private static final String PROFILE = "profile";
	private static final String INSTITUTION = "institution";
	private static final String EVENT = "event";
	private static final String GROUP = "group";
	
	private static final String PNG = "image/png";
	private static final String JPEG = "image/jpeg";
	
	//private static final String PROJECT_ID = "giversapdc";
	//private static final String BUCKET_ID = "giversapdc.appspot.com";
	//private static final String IMAGE_URL = "https://storage.googleapis.com/giversapdc.appspot.com/";
	private static final String PROJECT_ID = "givers-volunteering";
	private static final String BUCKET_ID = "givers-volunteering.appspot.com";
	private static final String IMAGE_URL = "https://storage.googleapis.com/givers-volunteering.appspot.com/";
	
	private static final Logger LOG = Logger.getLogger(EditResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private KeyFactory profileKeyFactory = datastore.newKeyFactory().setKind("Profile");
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	private KeyFactory institutionKeyFactory = datastore.newKeyFactory().setKind("Institution");
	private KeyFactory eventKeyFactory = datastore.newKeyFactory().setKind("Event");
	private KeyFactory groupKeyFactory = datastore.newKeyFactory().setKind("Group");
	private KeyFactory rbacKeyFactory = datastore.newKeyFactory().setKind("AccessControl");
	

	public EditResource() { }

	/*
	 * Method that calls the rest service to edit a user profile.
	 * Used to edit the profile of a logged in user. A profile can only by edited by the owner.
	 */
	@POST
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editProfile(UserData data) {
		LOG.info("Attempt to edit profile.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editProfile");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}

			String username = authToken.getString("username");
			Key profileKey = profileKeyFactory.addAncestors(PathElement.of("User", username)).newKey(username);
			Entity profile = txn.get(profileKey);
			
			//Validade given data
			r = data.validDataEdit();
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			//If user uploaded a new photo, upload to cloud storage
			String fileName = PROFILE + authToken.getString("username");
			String fileLink = IMAGE_URL + fileName;
			try {
				if( data.photo != null ) {
					//Get file type
					InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.photo));
					String mimeType = URLConnection.guessContentTypeFromStream(is);

					//Check if not null mime type, and if not, check if png or jpeg
					if( mimeType == null || (!mimeType.equals(PNG) && !mimeType.equals(JPEG)) ) {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Tipo de ficheiro não suportado. Tente png ou jpeg.").build();
					}
					
					Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
		
					BlobId blobId = BlobId.of(BUCKET_ID, fileName);
					BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
										
					//storage.create(blobInfo, Files.readAllBytes(Paths.get(data.photo)));
					storage.create(blobInfo, data.photo);
				}
			} catch(Exception e) {
				txn.rollback();
				LOG.severe(e.getMessage());
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Erro na leitura do ficheiro.").build();
			}
			
			// Rebuild the profile, with old data + new data
			profile = Entity.newBuilder(profileKey)
					.set("interests", converToValueList(data.interests))
					.set("phoneNr", data.phoneNr)
					.set("dateOfBirth", data.dateOfBirth)
					.set("gender", data.gender)
					.set("nationality", data.nationality)
					.set("address", data.address)
					.set("description", data.description)
					.set("photo", (data.photo != null) ? fileLink : profile.getString("photo"))
					.build();
			
			txn.put(profile);
			txn.commit();
			return Response.ok().entity("Perfil editado com sucesso.").build();
			
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	
	@POST
	@Path("/profile/password")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editPassword(UserData data) {
		LOG.info("Attempt to edit password.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editPassword");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);
			
			//Check if provided old password is correct
			if( data.oldPassword != null && !user.getString("password").equals(DigestUtils.sha512Hex(data.oldPassword))) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Password antiga incorreta.").build();
			}
			
			//Validate new password
			if( !data.validPassword() ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nova password tem de ter mais de 3 caracteres.").build();
			} 
			
			//Check if both new password and confirmation are the equal
			if( data.passwordConfirm == null || !DigestUtils.sha512Hex(data.password).equals(DigestUtils.sha512Hex(data.passwordConfirm)) ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Confirmação da password incorreta.").build();
			}

			user = Entity.newBuilder(user)
					.set("password", DigestUtils.sha512Hex(data.password))
					.build(); 

			txn.put(user);
			txn.commit();
			return Response.ok().entity("Password editada com sucesso.").build();
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
	 * Method that calls the rest service to edit a user's role. Users can't change
	 * their own role, and only higher roles can edit lower ones.
	 */
	@POST
	@Path("/profile/role")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editRole(UserData data) {
		LOG.info("Attempt to edit user role.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editRole");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);
			
			// Check if user exists in the database.
			if( user == null ) {
				LOG.warning("User doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("User não existe.").build();
			}
			
			String roleNew = data.role.replaceAll("\\s+", "").toUpperCase();
			String roleOp = authToken.getString("role");
			String roleOld = user.getString("role");
			
			/*
			 * Fail if: 
			 * 		BO attempts to edit something to BO or SU
			 * 		BO attempts to edit BO or SU to something else
			 */
			if( ((roleNew.equals(BO) || roleNew.equals(SU) ) && roleOp.equals(BO)) 
					|| ((roleOld.equals(SU) || roleOld.equals(BO)) && roleOp.equals(BO) )) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não tem permissões para tal.").build();
			} 

			user = Entity.newBuilder(user)
					.set("role", roleNew)
					.build(); 
			
			//Get user's auth tokens
			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("AuthToken")
					.setFilter(PropertyFilter.eq("username", user.getString("username")))
					.build();
			QueryResults<Entity> tasks = txn.run(query);
			
			//Change role on all of user's active tokens
			tasks.forEachRemaining(autTok -> { 	
				autTok = Entity.newBuilder(autTok.getKey())
						.set("role", roleNew)
						.build();
				txn.put(autTok);
			});

			txn.put(user);
			txn.commit();
			return Response.ok().entity("Role editado com sucesso.").build();
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
	 * Method that calls the rest service to edit a user's role. Users can't change
	 * their own role, and only higher roles can edit lower ones.
	 */
	@POST
	@Path("/profile/state")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editState(UserData data) {
		LOG.info("Attempt to edit user state.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editState");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			// Check if user exists in the database.
			if( user == null ) {
				LOG.warning("User doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("User não existe.").build();
			}

			String roleOp = authToken.getString("role");
			String roleUser = user.getString("role");
			//BO users can't change state on other BO or SU
			if( roleOp.equals(BO) && (roleUser.equals(BO) || roleUser.equals(SU)) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não tem permissões para tal.").build();
			}
			
			boolean newState = !user.getBoolean("state");
			
			user = Entity.newBuilder(user)
					.set("state", newState)
					.build(); 
			
			//If newState is false, means account became inactive, so we delete user's active auth tokens
			if( newState == false ) {
				Query<Entity> query = Query.newEntityQueryBuilder()
						.setKind("AuthToken")
						.setFilter(PropertyFilter.eq("username", user.getString("username")))
						.build();
				QueryResults<Entity> tasks = txn.run(query);
				
				tasks.forEachRemaining(autTok -> { 
					txn.delete(autTok.getKey());
				});
			}
			
			txn.put(user);
			txn.commit();
			return Response.ok().entity("State editado com sucesso.").build();

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
	 * Method that calls the rest service to edit an institution Used to edit an
	 * Institution. Currently only the institution owner can edit it.
	 */
	@POST
	@Path("/institution")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editInstitution(InstitutionData data) {
		LOG.info("Attempt to edit institution info.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editInstitution");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			String instName = data.name.replaceAll("\\s+", "");
			Key instKey = institutionKeyFactory.newKey(instName.toLowerCase());
			Entity inst = txn.get(instKey);

			// Check if institution exists in the database
			if( inst == null ) {
				LOG.warning("Institution doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Instituição não existe.").build();
			}
			
			//Verify new data
			Response re = data.validDataEdit();
			if( re.getStatus() != 200 ) {
				txn.rollback();
				return re;
			}
						
			//If user uploaded a new photo for the institution, upload to cloud storage
			String fileName = INSTITUTION + instName.toLowerCase();
			String fileLink = IMAGE_URL + fileName;
			try {
				if( data.photo != null ) {
					//Get file type
					InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.photo));
					String mimeType = URLConnection.guessContentTypeFromStream(is);

					//Check if not null mime type, and if not, check if png or jpeg
					if( mimeType == null || (!mimeType.equals(PNG) && !mimeType.equals(JPEG)) ) {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Tipo de ficheiro não suportado. Tente png ou jpeg.").build();
					}
					
					Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
		
					BlobId blobId = BlobId.of(BUCKET_ID, fileName);
					BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
										
					//storage.create(blobInfo, Files.readAllBytes(Paths.get(data.photo)));
					storage.create(blobInfo, data.photo);
				}
			} catch(Exception e) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Ficheiro não existe.").build(); 
			}
						
			//Edit institution and upload to database
			Builder instBuilder = Entity.newBuilder(inst)
					.set("email", data.email)
					.set("phoneNr", data.phoneNr) 
					.set("address", data.address)
					.set("lat", data.lat)
					.set("lon", data.lon)
					.set("photo", (data.photo != null) ? fileLink : inst.getString("photo"));
			
			inst = instBuilder.build();
			txn.put(inst);
			txn.commit();
			return Response.ok().entity("Instituição editada com sucesso.").build();
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
	 * Method that calls the rest service to edit an event.
	 * Used to edit an existent event. Currently only the owner of the Institution that is hosting the event can
	 * edit it.
	 */
	@POST
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editEvent(EventData data) {
		LOG.info("Attempt to edit event.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editEvent");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			if( data.name == null || data.name.equals("") ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "");
			Key eventKey = eventKeyFactory.newKey(eventName.toLowerCase());
			Entity event = txn.get(eventKey);
			
			// Check if event with given name exists in the database
			if( event == null ) {
				LOG.warning("Event doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			String instName = event.getString("institution").toLowerCase();
			Key instKey = institutionKeyFactory.newKey(instName.toLowerCase());
			Entity inst = txn.get(instKey);

			// Check if institution exists in the database
			if( inst == null ) {
				LOG.warning("Institution doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Instituição não existe.").build();
			}

			// Check if user editing is owner of institution that's hosting the event.
			if( !inst.getString("owner").toLowerCase().equals(authToken.getString("username")) ) {
				LOG.warning("User is not owner.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Não tem permissão para gerir esta instituição.").build();
			}

			//Check if event is ongoing or already ended == can't edit
			if( event.getLong("date_start") < System.currentTimeMillis()) {
				LOG.warning("Attempt to edit ongoing or ended event.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento está a decorrer ou já terminou, não pode ser editado.").build();
			}
			
			//Check validity of given data
			r = data.validDataEdit();
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			// Check if the new capacity is bigger than the number of participants
			if( data.capacity != 0 && data.capacity < event.getList("participants").size() ) {
				LOG.warning("New capacity is smaller than the number of participants.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nova capacidade é menor do que o número de participantes já inscritos.").build();
			}

			//If user uploaded a new photo for the event, upload to cloud storage
			String fileName = EVENT + eventName.toLowerCase();
			String fileLink = IMAGE_URL + fileName;
			try {
				if( data.photo != null ) {
					//Get file type
					InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.photo));
					String mimeType = URLConnection.guessContentTypeFromStream(is);

					//Check if not null mime type, and if not, check if png or jpeg
					if( mimeType == null || (!mimeType.equals(PNG) && !mimeType.equals(JPEG)) ) {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Tipo de ficheiro não suportado. Tente png ou jpeg.").build();
					}
					
					Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
		
					BlobId blobId = BlobId.of(BUCKET_ID, fileName);
					BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
										
					//storage.create(blobInfo, Files.readAllBytes(Paths.get(data.photo)));
					storage.create(blobInfo, data.photo);
				}
			} catch(Exception e) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Ficheiro não existe.").build();
			}
			
			// Rebuild the event and put on the database.
			event = Entity.newBuilder(event)
					.set("interests", data.interests)
					.set("address", data.address)
					.set("date_start", data.dateStart)
					.set("date_end", (data.duration != 0) ? event.getLong("date_start") + data.duration : event.getLong("date_end"))
					.set("capacity", data.capacity)
					.set("description", data.description)
					.set("photo", (data.photo != null) ? fileLink : event.getString("photo"))
					.build();

			txn.put(event);
			txn.commit();
			return Response.ok().entity("Evento editado com sucesso.").build();
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
	 * Method that calls the rest service to edit an event.
	 * Used to edit an existent event. Currently only the owner of the Institution that is hosting the event can
	 * edit it.
	 */
	@POST
	@Path("/group")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editGroup(GroupData data) {
		LOG.info("Attempt to edit group.");

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
			r = checkRBAC(txn, authToken.getString("role"), "editGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do grupo inválido.").build();
			}
			
			String groupName = data.name.replaceAll("\\s+", "");
			Key groupKey = groupKeyFactory.newKey(groupName.toLowerCase());
			Entity group = txn.get(groupKey);
			
			// Check if group with given name exists in the database
			if( group == null ) {
				LOG.warning("Group doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Grupo não existe.").build();
			}
			
			// Check if user editing is owner of group
			if( !group.getString("owner").equals(authToken.getString("username").toLowerCase()) ) {
				LOG.warning("User is not owner.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não tem permissão para gerenciar este grupo.").build();
			}
			
			r = data.validDataEdit();
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			// Check if the new capacity is bigger than the number of participants
			if( data.capacity != 0 && data.capacity < group.getList("participants").size() ) {
				LOG.warning("New capacity is smaller than the number of participants.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nova capacidade é menor que o número de membros do grupo.").build();
			}

			//If user uploaded a new photo for the group, upload to cloud storage
			String fileName = GROUP + groupName.toLowerCase();
			String fileLink = IMAGE_URL + fileName;
			try {
				if( data.photo != null ) {
					//Get file type
					InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.photo));
					String mimeType = URLConnection.guessContentTypeFromStream(is);

					//Check if not null mime type, and if not, check if png or jpeg
					if( mimeType == null || (!mimeType.equals(PNG) && !mimeType.equals(JPEG)) ) {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("Tipo de ficheiro não suportado. Tente png ou jpeg.").build();
					}
					
					Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
		
					BlobId blobId = BlobId.of(BUCKET_ID, fileName);
					BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
										
					//storage.create(blobInfo, Files.readAllBytes(Paths.get(data.photo)));
					storage.create(blobInfo, data.photo);
				}
			} catch(Exception e) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Ficheiro não existe.").build(); 
			}
			
			// Build the new event and put on the database.
			group = Entity.newBuilder(group)
					.set("capacity", data.capacity)
					.set("description", data.description)
					.set("photo", (data.photo != null) ? fileLink : group.getString("photo"))
					.build();

			txn.put(group);
			txn.commit();
			return Response.ok().entity("Grupo editado com sucesso.").build();
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
	 * Used to check if a user is in a session, by doing some checks to authToken
	 */
	private Response checkLogin(Transaction txn, AuthToken at, Entity authToken) {
		//Check both given token and database token
		if( at == null || authToken == null ) {
			LOG.warning("Attempt to operate with no login.");
			txn.rollback();
			return Response.status(Status.NOT_FOUND).entity("Login não encontrado.").build();
		}
		
		//If token is found, check for validity
		if( authToken.getLong("expirationDate") < System.currentTimeMillis() ) {
			LOG.warning("Auth Token expired.");
			txn.delete(authToken.getKey());
			txn.commit();
			return Response.status(Status.BAD_REQUEST).entity("Auth Token expirado. Faça login antes de tentar novamente.").build();
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
			return Response.status(Status.FORBIDDEN).entity("Você não tem permissão para realizar tal ação.").build();
		} 
		else
			return Response.ok().build();
	}
	
	
	/*
	 * Method used to convert a list of strings into a list of Values of Strings
	 */
	private List<Value<String>> converToValueList(List<String> list){
		List<Value<String>> newList = new ArrayList<Value<String>>();
		
		for( String s : list )
			newList.add(StringValue.of(s));
			
		return newList;
	}

}
