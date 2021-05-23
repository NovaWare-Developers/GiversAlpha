package giversapdc.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Value;

import giversapdc.util.AccessControlData;
import giversapdc.util.AuthToken;
import giversapdc.util.CommentData;
import giversapdc.util.EventData;
import giversapdc.util.GroupData;

@Path("/op")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class OperationsResource {
	
	private static final String BO = "BO";
	private static final String SU = "SU";
	
	private static final Logger LOG = Logger.getLogger(EditResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	private KeyFactory eventKeyFactory = datastore.newKeyFactory().setKind("Event");
	private KeyFactory groupKeyFactory = datastore.newKeyFactory().setKind("Group");
	private KeyFactory commentKeyFactory = datastore.newKeyFactory().setKind("Comment");
	private KeyFactory rbacKeyFactory = datastore.newKeyFactory().setKind("AccessControl");
	
	public OperationsResource() { }

	/*
	 * Method used to add a new user to an existent event. 
	 * User must give the name of the event to join.
	 * User can't join his own events.
	 */
	@POST
	@Path("/joinEventUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response joinEventUser(EventData data) {
		LOG.info("Attempt to join event.");
		
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
			r = checkRBAC(txn, authToken.getString("role"), "joinEventUser");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);

			//Check if event exists
			if( event == null ) {
				LOG.warning("Event doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			// Check if event is ongoing or finished.
			if( event.getLong("date_start") < System.currentTimeMillis() ) {
				LOG.warning("Event is not available anymore.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Evento já não está disponivel.").build();
			}
						
			//Check if event is full.
			if( event.getList("participants").size() == event.getLong("capacity") ) {
				LOG.warning("Event is full.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento está cheio.").build();
			}

			//Check if user already participating
			List<Value<String>> oldParticipants = event.getList("participants");
			
			if( oldParticipants.contains(StringValue.of(authToken.getString("username"))) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você já está inscrito neste evento.").build();
			}

			List<Value<String>> newParticipants = new ArrayList<Value<String>>();

			// Add the user to the list of participants.
			newParticipants	= addStringToListValuesString(oldParticipants, authToken.getString("username"));

			event = Entity.newBuilder(event)
					.set("participants", newParticipants)
					.build();

			// Add the event to the user's list of events.
			List<Value<String>> newEvents = addStringToListValuesString(user.getList("eventsJoined"), eventName);
			
			user = Entity.newBuilder(user)
					.set("eventsJoined", newEvents)
					.build();
			
			txn.put(event, user);
			txn.commit();
			return Response.ok().entity("Inscrição no evento com sucesso.").build();
	
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method used to add a new group to an existent event. 
	 * User must give the name of the event to join.
	 * User can't join his own events.
	 */
	@POST
	@Path("/joinEventGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response joinEventGroup(EventData data) {
		LOG.info("Attempt to join event.");

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
			r = checkRBAC(txn, authToken.getString("role"), "joinEventGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);

			//Check if event exists
			if( event == null ) {
				LOG.warning("Event doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			// Check if event is ongoing or finished.
			if( event.getLong("date_start") < System.currentTimeMillis() ) {
				LOG.warning("Event is not available anymore.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento já não está disponível.").build();
			}
				
			String groupName = data.groupName.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = groupKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);
			
			//Check if all group would fit.
			int afterJoinSize = event.getList("participants").size() + group.getList("participants").size();
			
			if( afterJoinSize > event.getLong("capacity") ) {
				LOG.warning("Event can't support whole group.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento não tem lugares suficientes para o grupo.").build();
			}

			List<Value<String>> oldParticipants = event.getList("participants");
			List<Value<String>> newParticipants = new ArrayList<Value<String>>();

			List<Value<String>> members = group.getList("participants");
			newParticipants = joinValueLists(oldParticipants, members);

			event = Entity.newBuilder(event)
					.set("participants", newParticipants)
					.build();

			// Add the event to the users' list of events.
			for( Value<String> v : members ) {			
				Key userKeyTemp = userKeyFactory.newKey(v.get().toLowerCase());
				Entity userTemp = txn.get(userKeyTemp);
				List<Value<String>> newEvents = addStringToListValuesString(user.getList("eventsJoined"), eventName);
				userTemp = Entity.newBuilder(userTemp)
						.set("eventsJoined", newEvents)
						.build(); 
				txn.put(userTemp);
			}
			
			txn.put(event);
			txn.commit();
			return Response.ok().entity("Grupo inscrito com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	
	/*
	 * Method used to remove a user from an event that he's registered in. 
	 * Event can't be finished or ongoing.
	 */
	@POST
	@Path("/leaveEventUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response leaveEventUser(EventData data) {
		LOG.info("Attempt to leave event.");

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
			r = checkRBAC(txn, authToken.getString("role"), "leaveEventUser");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);

			//Check if event exists in database
			if( event == null ) {
				LOG.warning("Event doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			//Check if user is not participating
			List<Value<String>> oldParticipants = event.getList("participants");
			
			if( !oldParticipants.contains(StringValue.of(authToken.getString("username"))) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você já está inscrito/a neste evento.").build();
			}
			
			// Check if event is started or finished.
			if( event.getLong("date_start") < System.currentTimeMillis() ) {
				LOG.warning("Event is not available anymore.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento está a decorrer ou já terminou, não pode cancelar a inscrição.").build();
			}
			
			List<Value<String>> newParticipants = new ArrayList<Value<String>>();
			// Remove the user of the list of participants.
			newParticipants = removeStringFromListValuesString(event.getList("participants"), authToken.getString("username"));
		
			event = Entity.newBuilder(event)
					.set("participants", newParticipants)
					.build();

			// Remove the event from the user's list of events.
			List<Value<String>> newEvents = removeStringFromListValuesString(user.getList("eventsJoined"), eventName);

			user = Entity.newBuilder(user)
					.set("eventsJoined", newEvents)
					.build();

			txn.put(event, user);
			txn.commit();
			return Response.ok().entity("Cancelou a inscrição do evento com sucesso.").build();
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method used to remove a user from an event that he's registered in. 
	 * Event can't be finished or ongoing.
	 */
	@POST
	@Path("/leaveEventGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response leaveEventGroup(EventData data) {
		LOG.info("Attempt to leave event.");

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
			r = checkRBAC(txn, authToken.getString("role"), "leaveEventGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);

			//Check if event exists in database
			if( event == null ) {
				LOG.warning("Event doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}

			// Check if event is started or finished.
			if( event.getLong("date_start") < System.currentTimeMillis() ) {
				LOG.warning("Event is not available anymore.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento está a decorrer ou já terminou, não pode cancelar a inscrição.").build();
			}
			
			String groupName = data.groupName.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = groupKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);
			
			List<Value<String>> oldParticipants = event.getList("participants");			
			List<Value<String>> newParticipants = new ArrayList<Value<String>>();
			
			List<Value<String>> members = group.getList("participants");
			
			newParticipants = removeValueLists(oldParticipants, group.getList("participants"));
			
			event = Entity.newBuilder(event)
					.set("participants", newParticipants)
					.build();

			// Remove the event from the users' list of events.
			for( Value<String> v : members ) {			
				Key userKeyTemp = userKeyFactory.newKey(v.get().toLowerCase());
				Entity userTemp = txn.get(userKeyTemp);
				List<Value<String>> newEvents = removeStringFromListValuesString(user.getList("eventsJoined"), eventName);
				userTemp = Entity.newBuilder(userTemp)
						.set("eventsJoined", newEvents)
						.build(); 
				txn.put(userTemp);
			}		
			
			txn.put(event);
			txn.commit();
			return Response.ok().entity("Cancelou a inscrição do evento com sucesso.").build();
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method used to add a user to a group. 
	 * Group can't be full, or user be owner of group
	 */
	@POST
	@Path("/joinGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response joinGroup(GroupData data) {
		LOG.info("Attempt to join group.");

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
			r = checkRBAC(txn, authToken.getString("role"), "joinGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do grupo inválido.").build();
			}
			
			String groupName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = groupKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);

			//Check if group exists
			if( group == null ) {
				LOG.warning("Group doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Grupo não existe.").build();
			}

			//Check if group is full.
			if( group.getList("participants").size() >= group.getLong("capacity") ) {
				LOG.warning("Group is full.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Grupo está cheio.").build();
			}

			//Check if user already in group
			List<Value<String>> oldParticipants = group.getList("participants");
			
			if( oldParticipants.contains(StringValue.of(authToken.getString("username"))) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você já faz parte deste grupo.").build();
			}

			// Add the user to the list of participants.
			List<Value<String>> newParticipants	= addStringToListValuesString(oldParticipants, authToken.getString("username"));

			group = Entity.newBuilder(group)
					.set("participants", newParticipants)
					.build();

			// Add the group to the user's list of groups joined.
			List<Value<String>> newGroups = addStringToListValuesString(user.getList("groupsJoined"), groupName);

			user = Entity.newBuilder(user)
					.set("groupsJoined", newGroups)
					.build();

			txn.put(group, user);
			txn.commit();
			return Response.ok().entity("Juntou-se ao grupo com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method used to remove a user from a group. 
	 */
	@POST
	@Path("/leaveGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response leaveGroup(GroupData data) {
		LOG.info("Attempt to leave group.");

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
			r = checkRBAC(txn, authToken.getString("role"), "leaveGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do grupo inválido.").build();
			}
			
			String groupName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = groupKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);

			//Check if group exists
			if( group == null ) {
				LOG.warning("Group doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Grupo não existe.").build();
			}

			//Check if user already out of group
			List<Value<String>> oldParticipants = group.getList("participants");
			
			if( !oldParticipants.contains(StringValue.of(authToken.getString("username"))) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não faz parte deste grupo.").build();
			}

			//Remove the user from the list of participants.
			List<Value<String>> newParticipants	= removeStringFromListValuesString(oldParticipants, authToken.getString("username"));

			group = Entity.newBuilder(group)
					.set("participants", newParticipants)
					.build();

			// Remove the group from the user's list of groups joined.
			List<Value<String>> newGroups = removeStringFromListValuesString(user.getList("groupsJoined"), groupName);

			user = Entity.newBuilder(user)
					.set("groupsJoined", newGroups)
					.build();

			txn.put(group, user);
			txn.commit();
			return Response.ok().entity("Abandonou o grupo com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method used by a user to leave a comment on an event he is registered in.
	 * Comments can be left only on past events.
	 */
	@POST
	@Path("/commentEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response commentEvent(CommentData data) {
		LOG.info("Attempt to comment on event.");

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
			r = checkRBAC(txn, authToken.getString("role"), "commentEvent");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}

			if( data.name == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Nome do evento inválido.").build();
			}
			
			String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key eventKey = eventKeyFactory.newKey(eventName);
			Entity event = txn.get(eventKey);

			//Check if event exists
			if( event == null ) {
				LOG.warning("Event doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
			}
			
			//Check if event is finished
			if( event.getLong("date_end") > System.currentTimeMillis() ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Evento ainda não terminou, ainda não pode comentar.").build();
			}
			
			//Check if user was part of the event
			if( !event.getList("participants").contains(StringValue.of(authToken.getString("username"))) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não participou no evento, não pode comentar.").build();
			}
				
			//Check if comment is not empty
			String commentStr = data.comment.replaceAll("\\s+", "");
			if( commentStr.length() == 0 ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Comentário não pode ser vazio.").build();
			}
			
			//Generate random id for comment
			String key = UUID.randomUUID().toString();
			Key commentKey = commentKeyFactory.addAncestor(PathElement.of("Event", eventName)).newKey(key);
			
			Entity comment = Entity.newBuilder(commentKey)
					.set("comment", data.comment)
					.set("owner", authToken.getString("username"))
					.set("date", System.currentTimeMillis())
					.build();

			txn.put(comment);
			txn.commit();
			return Response.ok().entity("Comentou com sucesso.").build();
			
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Method called at the end of each day (24h intervals) that gives out all the event points.
	 * Filters events finished with property "points_rewarded" false, and gives all participants 1 point.
	 */
	@POST
	@Path("/distributePoints")
	@Consumes(MediaType.APPLICATION_JSON)
	public void collectPoints() {
		LOG.info("Daily distribution of event points.");
		
		Transaction txn = datastore.newTransaction();
		try {
			//Get all finished events with points_rewarded == false
			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("Event")
					.setFilter(CompositeFilter.and(
							PropertyFilter.lt("date_end", System.currentTimeMillis()), 
							PropertyFilter.eq("points_rewarded", false)))
					.build();
			QueryResults<Entity> tasks = datastore.run(query);
	
			//Get usernames of all participants
			List<String> usernames = new ArrayList<String>();
			
			tasks.forEachRemaining(event -> {
				List<Value<String>> usernamesValues = event.getList("participants");
				usernamesValues.forEach(user -> {
					usernames.add(user.get());
				});
				
				//Set "points_rewarded" to true in the event
				event = Entity.newBuilder(event)
						.set("points_rewarded", true)
						.build();
				txn.put(event);
			});
	
			//Add one point to each user and put to database
			usernames.forEach(u -> {
				Key userKey = userKeyFactory.newKey(u.toLowerCase());
				Entity user = datastore.get(userKey);
				
				//Doing null check, because a user could have finished the event and deleted the account before
				//the end of the day, which is when this function is executed
				if( user != null ) {
					user = Entity.newBuilder(user)
							.set("shopPoints", user.getLong("shopPoints") + 1)
							.build();
					txn.put(user);
				}
			});
			
			txn.commit();
			LOG.info("Event points distributed successfully.");
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	
	/*
	 * Used by a back office admin to add a new method to the RBAC
	 * By default, newly added methods are available for only SU
	 */
	@POST
	@Path("/addAccessControl")
	public Response addAccessControl(AccessControlData data) {
		LOG.info("Attempt to add access control method.");
		
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
			r = checkRBAC(txn, authToken.getString("role"), "addAccessControl");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			String methodName = data.methodName.replaceAll("\\s+", "").toLowerCase();
			Key methodKey = rbacKeyFactory.newKey(methodName);
			Entity method = txn.get(methodKey);
			
			Key thisMethodKey = rbacKeyFactory.newKey("addAccessControl".toLowerCase());
			Entity thisMethod = txn.get(thisMethodKey);	
			
			//Check if method exists
			if( method != null ) {
				LOG.warning("Method already exists.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Método já existe.").build();
			}
			
			//Check RBAC
			if( thisMethod.getBoolean(authToken.getString("role")) == false ) {
				LOG.warning("User doesn't have permission for this action.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não tem permissões para realizar esta tarefa.").build();
			}
			
			//Add new method to RBAC, default usable only to SU and BO			
			method = Entity.newBuilder(methodKey)
					.set("USER", data.user)
					.set("BO", data.bo)
					.set("INST_OWNER", data.instOwner)
					.set("GROUP_OWNER", data.groupOwner)
					.set("SU", true)
					.build();
			
			txn.add(method);
			txn.commit();
			LOG.info("Method added successfully.");
			return Response.ok().entity("Método adicionado com sucesso.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
	/*
	 * Used by a back office admin to edit the RBAC on the database
	 */
	@POST
	@Path("/editAccessControl")
	public Response editAccessControl(AccessControlData data) {
		LOG.info("Attempt to change access control rule.");
		
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
			r = checkRBAC(txn, authToken.getString("role"), "editAccessControl");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}

			Key thisMethodKey = rbacKeyFactory.newKey("editAccessControl".replaceAll("\\s+", "").toLowerCase());
			Entity thisMethod = txn.get(thisMethodKey);
			
			String methodName = data.methodName.replaceAll("\\s+", "").toLowerCase();
			Key methodKey = rbacKeyFactory.newKey(methodName);
			Entity method = txn.get(methodKey);
			
			//Check if method exists
			if( method == null ) {
				LOG.warning("Method doesn't exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Método especificado não existe.").build();
			}

			//Check RBAC
			if( thisMethod.getBoolean(authToken.getString("role")) == false ) {
				LOG.warning("User doesn't have permission for this action.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity(".").build();
			}
			
			//If BO tried to edit permissions of BO or SU, fail
			if( data.role.equals(BO) || data.role.equals(SU) ) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não tem permissões para realizar esta tarefa.").build();
			}
			
			Map<String, Value<?>> props = method.getProperties();
			
			Builder methodBuilder = Entity.newBuilder(methodKey);
					
			props.forEach((k, v) -> {
				String temp = v.get().toString();
				if( temp.equals("false") )
					methodBuilder.set(k, false);
				else
					methodBuilder.set(k, true);
			});
			
			String role = data.role.replaceAll("\\s+", "").toUpperCase();
			boolean newState = !method.getBoolean(role);
			method = methodBuilder
					.set(role, newState)
					.build();
			
			txn.put(method);
			txn.commit();
			LOG.info("Role changed successfully.");
			return Response.ok().entity("Role mudado com sucesso.").build();
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
			return Response.status(Status.FORBIDDEN).entity("Auth Token expirado. Faça login antes de tentar outra vez.").build();
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
			return Response.status(Status.BAD_REQUEST).entity("Método especificado não existe.").build();
		}
		
		//Check RBAC
		if( method.getBoolean(role) == false ) {
			LOG.warning("User doesn't have permission for this action.");
			txn.rollback();
			return Response.status(Status.FORBIDDEN).entity("Você não tem permissões para realizar esta tarefa.").build();
		} 
		else
			return Response.ok().build();
	}
	
	
	/*
	 * Doing list.put or list.delete directly on database lists doesn't work
	 * These methods are workarounds to operate over said lists
	 */
	private List<Value<String>> addStringToListValuesString(List<Value<String>> list, String toAdd) {
		List<Value<String>> listNew = new ArrayList<Value<String>>();
		
		for(Value<String> v : list) {
			listNew.add(v);
		}

		if( !listNew.contains(StringValue.of(toAdd)) )
			listNew.add(StringValue.of(toAdd));
		
		return listNew;
	}
	
	
	private List<Value<String>> removeStringFromListValuesString(List<Value<String>> list, String toRem) {
		List<Value<String>> listNew = new ArrayList<Value<String>>();
		
		for( Value<String> v : list ) 
			listNew.add(v);

		listNew.remove(StringValue.of(toRem));
		
		return listNew;
	}
	
	private List<Value<String>> joinValueLists(List<Value<String>> list, List<Value<String>> toAdd) {
		List<Value<String>> listNew = new ArrayList<Value<String>>();
		
		for( Value<String> v : list ) 
			listNew.add(v);

		for( Value<String> v : toAdd ) 
			if( !listNew.contains(v) )
				listNew.add(v);

		return listNew;
	}
	
	private List<Value<String>> removeValueLists(List<Value<String>> list, List<Value<String>> toAdd) {
		List<Value<String>> listNew = new ArrayList<Value<String>>();
		
		for( Value<String> v : list ) 
			listNew.add(v);

		for( Value<String> v : toAdd ) 
			listNew.remove(v);

		return listNew;
	}
	
	
	
	
}
