package giversapdc.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
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
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import giversapdc.util.AuthToken;
import giversapdc.util.EventData;
import giversapdc.util.GroupData;
import giversapdc.util.InstitutionData;
import giversapdc.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	private static final String USER = "USER";
	private static final String INST_OWNER = "INST_OWNER";
	private static final String GROUP_OWNER = "GROUP_OWNER";
	private static final String SU = "SU";
	
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private KeyFactory profileKeyFactory = datastore.newKeyFactory().setKind("Profile");
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	private KeyFactory institutionKeyFactory =  datastore.newKeyFactory().setKind("Institution");
	private KeyFactory eventKeyFactory = datastore.newKeyFactory().setKind("Event");
	private KeyFactory groupKeyFactory = datastore.newKeyFactory().setKind("Group");
	private KeyFactory rbacKeyFactory = datastore.newKeyFactory().setKind("AccessControl");
	private KeyFactory markerKeyFactory = datastore.newKeyFactory().setKind("Marker");
	
	public RegisterResource() { }
	
	/*
	 * Method used to register a new user in the system given a username, email, password and password confirmation.
	 * Default role is set to USER
	 */
	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUser(RegisterData data) { 
		LOG.info("Attempt to register user.");
		
		Transaction txn = datastore.newTransaction();

		try {
			//Check for user login
			if( data.at != null ) {
				LOG.warning("Attempt to register new account while user logged in.");
				return Response.status(Status.FORBIDDEN).entity("Deve realizar o logout antes de registar outra conta.").build();
			}
			
			//Check input data
			Response r = data.validRegisterData();
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			String username = data.username.replaceAll("\\+s", "").toLowerCase();
			Key userKey = userKeyFactory.newKey(username);
			Key profileKey = profileKeyFactory.addAncestors(PathElement.of("User", username)).newKey(username);
			Entity user = txn.get(userKey);
			Entity profile = txn.get(profileKey);
			
			//Check for user in the database.
			if( user != null ) {
				LOG.warning("User already exists.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User já existe.").build();
			}

			//Creates a user with main attributes, and blank profile, which can be edited later		
			//Default role is USER, account state is ACTIVE (state = true), lists are empty and points == 0
			user = Entity.newBuilder(userKey)
					.set("username", data.username)
					.set("password", DigestUtils.sha512Hex(data.password))
					.set("email", data.email)
					.set("name", data.name)
					.set("role", USER)
					.set("state", true)
					.set("creation_time", Timestamp.now())
					.set("shopPoints", 0)
					.set("eventsJoined", new ArrayList<Value<String>>())
					.set("groupsJoined", new ArrayList<Value<String>>())
					.build();
			
			//Blank profile
			profile = Entity.newBuilder(profileKey)
					.set("interests", new ArrayList<Value<String>>())
					.set("phoneNr", "")
					.set("dateOfBirth", 0)
					.set("gender", "")
					.set("nationality", "")
					.set("address", "")
					.set("description", "")
					.set("photo", "")
					.build();
			
			txn.add(user, profile);
			LOG.info("User registered successfully with username " + data.username);
			txn.commit();
			return Response.ok().entity("User registado com sucesso com o username: " + data.username).build();
			
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
	 * Method that calls the rest /register/institution. 
	 * Used to register a new Institution in the system given a Name, Email, Phone Number and Address.
	 * Can be called by any user.
	 */
	@POST
	@Path("/institution")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerInsitution(InstitutionData data) {
		LOG.info("Attempt to register new institution.");
		
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
			r = checkRBAC(txn, authToken.getString("role"), "registerInstitution");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);
			
			//Check given data validity
			Response re = data.validDataRegister();
			if( re.getStatus() != 200 ) {
				txn.rollback();
				return re;
			}
			
			String instName = data.name.replaceAll("\\s+", "");
			Key institutionKey = institutionKeyFactory.newKey(instName.toLowerCase());
			Entity institution = txn.get(institutionKey);
			
			//Check if institution exists in the database
			if( institution != null ) {
				LOG.warning("Institution already exists with given name.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Já existe uma instituição com o mesmo nome.").build();
			}
			
			//Create a new institution and add to database. For now, default owner is the creator
			institution = Entity.newBuilder(institutionKey)
					.set("owner", authToken.getString("username"))
					.set("name", data.name)
					.set("email", data.email)
					.set("phoneNr", data.phoneNr)
					.set("address", data.address)
					.set("lat", data.lat)
					.set("lon", data.lon)
					.set("photo", "")
					.build();

			//If role is SU, stays SU. Otherwise, if not already INST_OWNER, change role to that
			if( !user.getString("role").equals(SU) && !user.getString("role").equals(INST_OWNER) ) {
				//Update user role to INST_OWNER
				user = Entity.newBuilder(user)
						.set("role", INST_OWNER)
						.build();
				
				//Get user's auth tokens
				Query<Entity> query = Query.newEntityQueryBuilder()
						.setKind("AuthToken")
						.setFilter(PropertyFilter.eq("username", authToken.getString("username")))
						.build();
				QueryResults<Entity> tasks = txn.run(query);
				
				//Change role on all of user's active tokens
				tasks.forEachRemaining(autTok -> { 	
					autTok = Entity.newBuilder(autTok)
							.set("role", INST_OWNER)
							.build();
					txn.put(autTok);
				});
			}
			
			txn.add(institution);
			txn.put(user);
			txn.commit();
			return Response.ok().entity("Instituição registada com sucesso com o id: " + instName).build();
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
	 * Method that calls the rest /register/event. 
	 * Used to register a new Event in the system.
	 * Can be called by any user, given an institution name. If the user is the owner of the institution, event will be created.
	 */
	@POST
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerEvent(EventData data) {
		LOG.info("Attempt to register new event.");
		
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
			r = checkRBAC(txn, authToken.getString("role"), "registerEvent");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			//Check data validity
			Response re = data.validDataRegister();
			if( re.getStatus() != 200 ) {
				txn.rollback();
				return re;
			}
			
			String eventName = data.name.replaceAll("\\s+", "");
			String instName = data.institutionName.replaceAll("\\s+", "");
			
			Key instKey = institutionKeyFactory.newKey(instName.toLowerCase()); 
			Key eventKey = eventKeyFactory.newKey(eventName.toLowerCase());
			
			Entity inst = txn.get(instKey);
			
			//Check if institution exists in the database
			if( inst == null ) {
				LOG.warning("Institution doesn't exist");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Instituição não existe.").build();
			}
			
			//If institution exists, check if owner corresponds to operating user by checking the auth token username
			if( !inst.getString("owner").toLowerCase().equals(authToken.getString("username")) ) {
				LOG.warning("User " + authToken.getString("username") + " is not owner of " + instName);
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Você não é o dono da instituição.").build();
			}
			
			Entity event = txn.get(eventKey);
			
			//Check if event with given name already exists in the database
			if( event != null ) {
				LOG.warning("Event with given name already exists, name: " + instName);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Já existe um evento com o mesmo nome.").build();
			}
			
			//Create event with given data and an empty list of participants
			event = Entity.newBuilder(eventKey)
					.set("institution", instName)
					.set("name", data.name)
					.set("interests", data.interests)
					.set("address", data.address)
					.set("date_start", data.dateStart)
					.set("date_end", data.dateStart + data.duration)
					.set("capacity", data.capacity)
					.set("description", data.description)
					.set("participants", new ArrayList<Value<String>>())
					.set("photo", "")
					.set("points_rewarded", false)
					.build();
			
			//Add all map markers
			KeyFactory mkKeyFactory = markerKeyFactory.addAncestor(PathElement.of("Event", eventName));
			for( int i = 0, j = 0; i < data.markers.length; i=i+2, j++ ) {
				Key markerKey = mkKeyFactory.newKey(eventName + j);
				Entity marker = Entity.newBuilder(markerKey)
						.set("lat", data.markers[i])
						.set("lon", data.markers[i+1])
						.set("name", data.name)
						.set("id", eventName)
						.build();
				txn.add(marker);
			}
			
			txn.add(event);
			txn.commit();
			LOG.fine("Event registered successfully with id " + eventName);
			return Response.ok().entity("Evento criado com sucesso.").build();
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
	 * Method that calls the rest /register/group. 
	 * Used to register a new Group in the system, given a Name, Capacity and Description.
	 * Currently a group can be created by any user.
	 */
	@POST
	@Path("/group")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerGroup(GroupData data) {
		LOG.info("Attempt to register group.");
		
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
			r = checkRBAC(txn, authToken.getString("role"), "registerGroup");
			if( r.getStatus() != 200 ) {
				txn.rollback();
				return r;
			}
			
			Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
			Entity user = txn.get(userKey);
			
			//Check data validity
			Response re = data.validDataRegister();
			if( re.getStatus() != 200 ) {
				txn.rollback();
				return re;
			}
			
			String groupName = data.name.replaceAll("\\s+", "").toLowerCase();
			Key groupKey = groupKeyFactory.newKey(groupName);
			Entity group = txn.get(groupKey);

			//Check if group with given name already exists in the database
			if( group != null ) {
				LOG.warning("Event with given name already exists, name: " + groupName);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Já existe um grupo com o mesmo nome.").build();
			}
			
			//Create group with given data and an empty list of participants and events
			group = Entity.newBuilder(groupKey)
					.set("owner", authToken.getString("username"))
					.set("name", data.name)
					.set("capacity", data.capacity)
					.set("description", data.description)
					.set("eventsJoined", new ArrayList<Value<String>>())
					.set("participants", new ArrayList<Value<String>>())
					.set("photo", "")
					.build();
			
			//If role is SU, stays SU. Otherwise, if not already GROUP_OWNER, change role to that
			if( !user.getString("role").equals(SU) ) {
				//Update user role to GROUP_OWNER
				user = Entity.newBuilder(user)
						.set("role", GROUP_OWNER)
						.build();
				
				//Get user's auth tokens
				String username = authToken.getString("username");
				Query<Entity> query = Query.newEntityQueryBuilder()
						.setKind("AuthToken")
						.setFilter(PropertyFilter.eq("username", username))
						.build();
				QueryResults<Entity> tasks = txn.run(query);
				
				//Change role on all of user's active tokens
				tasks.forEachRemaining(autTok -> { 	
					autTok = Entity.newBuilder(autTok)
							.set("role", GROUP_OWNER)
							.build();
					txn.put(autTok);
				});
			}
			
			txn.add(group);
			txn.commit();
			LOG.fine("Group registered successfully with id " + groupName);
			return Response.ok().entity("Grupo criado com sucesso.").build();
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
	 * Method used to convert a list of Strings into a list of values to send to the database
	 */
	private List<Value<String>> convertToValueList(List<String> list) {
		List<Value<String>> result = new ArrayList<Value<String>>();
		
		for( String s : list )
			result.add(StringValue.of(s));
		
		return result;
	}
	
	/*
	 * Doing list.put or list.delete with StringValue.of("string") doesn't work
	 * These 2 methods are a workaround to add/remove Strings to Lists of Values of Strings
	 */
	private List<Value<String>> addStringToListValuesString(List<Value<String>> list, String toAdd) {
		List<Value<String>> listNew = new ArrayList<Value<String>>();
		
		for(Value<String> v : list) {
			listNew.add(v);
		}

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
	
}
