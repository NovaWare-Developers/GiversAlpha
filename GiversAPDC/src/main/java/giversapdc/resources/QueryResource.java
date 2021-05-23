package giversapdc.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Entity.Builder;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.google.gson.Gson;

import giversapdc.util.AuthToken;
import giversapdc.util.MapMarker;
import giversapdc.util.Pair;
import giversapdc.util.QueryData;

@Path("/query")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class QueryResource {

	private static final Integer PAGE_SIZE = 5;
	
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private KeyFactory profileKeyFactory = datastore.newKeyFactory().setKind("Profile");
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	private KeyFactory institutionKeyFactory = datastore.newKeyFactory().setKind("Institution");
	private KeyFactory eventKeyFactory = datastore.newKeyFactory().setKind("Event");
	private KeyFactory groupKeyFactory = datastore.newKeyFactory().setKind("Group");
	private KeyFactory rbacKeyFactory = datastore.newKeyFactory().setKind("AccessControl");
	private KeyFactory markerKeyFactory = datastore.newKeyFactory().setKind("Marker");

	public QueryResource() { }

	/*
	 * Used to retrieve the profile of a user with given username.
	 */
	@POST
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProfile(QueryData data) {		

		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getProfile");
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Get user
		Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
		Entity user = datastore.get(userKey);

		// Check if user exists.
		if( user == null )
			return Response.status(Status.NOT_FOUND).entity("User não existe.").build();

		//Get user's profile 
		Key profileKey = profileKeyFactory
				.addAncestor(PathElement.of("User", authToken.getString("username").toLowerCase()))
				.newKey(authToken.getString("username"));
		Entity profile = datastore.get(profileKey);

		return Response.ok(g.toJson(profile.getProperties())).build();
	} 

	
	/*
	 * Used to retrieve the information about an event, given its name.
	 */
	@POST
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvent(QueryData data) {

		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getEvent");
		if( r.getStatus() != 200 ) {
			return r;
		}

		Key eventKey = eventKeyFactory.newKey(data.name.replaceAll("\\s+", "").toLowerCase());
		Entity event = datastore.get(eventKey);

		if( event == null )
			return Response.status(Status.NOT_FOUND).entity("Evento não existe.").build();
		
		return Response.ok(g.toJson(event.getProperties())).build();
	} 
	
	
	/*
	 * Used to retrieve a list of all events - ended, future, or all. 
	 * Use "query cursor" to send a set number of events per each call.
	 */
	@POST
	@Path("/eventsFiltered")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEventsFiltered(QueryData data) {
	
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getEventsFiltered");
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		Cursor startCursor = null;
		
		if( data.startCursorString != null && !data.startCursorString.equals("") ) {
			//Where query stopped last time
			startCursor = Cursor.fromUrlSafe(data.startCursorString);
		}
		
		List<Map<String, Value<?>>> events = new ArrayList<Map<String, Value<?>> >();
		
		//Prepare query that will get events
		EntityQuery.Builder query = Query.newEntityQueryBuilder()
				.setKind("Event")
				.setLimit(PAGE_SIZE)
				.setStartCursor(startCursor);
		
		/*	-1 past, 0 all, 1 future (missing a "2 ongoing" here, but it's quite problematic, since we can't do inequality filters on different property)
			Per documentation: "(...) a single query may not use inequality comparisons on more than one property across all of its filters."
			So, given this we can't compare start and end date of an event simultaneously.
		*/		
		PropertyFilter filter = null;
		if( data.queryTime == -1 )
			filter = PropertyFilter.lt("date_end", System.currentTimeMillis());
		else if ( data.queryTime == 0 )
			filter = null;
		else if ( data.queryTime == 1 )
			filter = PropertyFilter.gt("date_start", System.currentTimeMillis());	
			
		//If user gave interest, filter by that as well
		if( data.interests != "" ) {
			String interest = data.interests.replaceAll("\\s+", "").toLowerCase();
			if( filter != null )
				query.setFilter(CompositeFilter.and(PropertyFilter.eq("interests", interest), filter));
			else
				query.setFilter(PropertyFilter.eq("interests", interest));
		} 
		else
			query.setFilter(filter);
			
		QueryResults<Entity> tasks = datastore.run(query.build());
		
		while( tasks.hasNext() ) {
			Entity task = tasks.next();
			events.add(task.getProperties());
		}
		
		//Where to start next time
		Cursor cursor = tasks.getCursorAfter();
		if( cursor != null && events.size() == PAGE_SIZE ) {
			String cursorString = cursor.toUrlSafe();
			Pair p = new Pair(events, cursorString);
			return Response.ok(g.toJson(p)).build();
		}
		else {
			Pair p = new Pair(events, "end");
			return Response.ok(g.toJson(p)).build();
		}		
	} 

	
	public Response getEventsFutureMap(QueryData data) {
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getEventsFutureMap");
		if( r.getStatus() != 200 ) {
			return r;
		}
				
		//Get all future events
		Query<Entity> query = Query.newEntityQueryBuilder()
				.setKind("Event")
				.setFilter(PropertyFilter.ge("date_start", System.currentTimeMillis()))
				.build();
		
		QueryResults<Entity> tasks = datastore.run(query);
		
		//Get markers of all future events and add to list of events
		List<MapMarker> eventsMarkers = new ArrayList<MapMarker>();
		tasks.forEachRemaining(ev -> {
			String eventId = ev.getString("name").replaceAll("\\s+", "").toLowerCase();
			KeyFactory mkKeyFactory = markerKeyFactory.addAncestor(PathElement.of("Event", eventId));
			Key markerKey = mkKeyFactory.newKey(eventId + 0);
			Entity eventMarker = datastore.get(markerKey);
			//Build the marker
			MapMarker mrkr = new MapMarker(eventMarker.getLong("lat"), eventMarker.getLong("lon"), eventId, eventMarker.getString("name"));
			eventsMarkers.add(mrkr);
		});
		
		return Response.ok().entity(g.toJson(eventsMarkers)).build();
	}
	
	/*
	 * Used to retrieve a list of user's events: -1 past, 0 all, 1 ongoing, 2 future
	 * We don't use a cursor here because of the problem of ongoing events, which we consider to be important to be seen as a user
	 */
	@POST
	@Path("/eventsUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserEvents(QueryData data) {

		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getUserEvents");
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		Key userKey = userKeyFactory.newKey(authToken.getString("username").toLowerCase());
		Entity user = datastore.get(userKey);
		
		List<Value<String>> eventsValue = user.getList("eventsJoined");
		List<Entity> eventsEntity = new ArrayList<Entity>();
		for( Value<String> v : eventsValue ) {
			Key eventKey = eventKeyFactory.newKey(v.get().replaceAll("\\s+", "").toLowerCase());
			eventsEntity.add(datastore.get(eventKey));
		}

		List<Map<String, Value<?>>> events = new ArrayList<Map<String, Value<?>>>();
		if( data.queryTime == -1 )
			for( Entity e : eventsEntity ) {
				if( e.getLong("date_end") < System.currentTimeMillis() )
					events.add(e.getProperties());
			}
		else if( data.queryTime == 0 )
			for( Entity e : eventsEntity ) {
				events.add(e.getProperties());
			}
		else if( data.queryTime == 1 )
			for( Entity e : eventsEntity ) {
				if( e.getLong("date_start") > System.currentTimeMillis() )
					events.add(e.getProperties());
			}
		else if(data.queryTime == 2)
			for( Entity e : eventsEntity ) {
				if( e.getLong("date_end") > System.currentTimeMillis() && e.getLong("date_start") < System.currentTimeMillis() )
					events.add(e.getProperties());
			}
		
		return Response.ok().entity(g.toJson(events)).build();
	} 
	
	
	/*
	 * Used to retrieve a list of an institution's events: -1 past, 0 all, 1 future
	 */
	@POST
	@Path("/eventsInstitution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInstitutionEvents(QueryData data) {
		
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "eventsInstitution");
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		Cursor startCursor = null;
		
		if( data.startCursorString != null && !data.startCursorString.equals("") ) {
			//Where query stopped last time
			startCursor = Cursor.fromUrlSafe(data.startCursorString);
		}
		
		//Get all events of an institution
		String instName = data.name.replaceAll("\\s+", "").toLowerCase();
		EntityQuery.Builder query = Query.newEntityQueryBuilder()
				.setKind("Event")
				.setLimit(PAGE_SIZE)
				.setStartCursor(startCursor);
				
		//Prepare time filter
		PropertyFilter filter = null;
		if( data.queryTime == -1 )
			filter = PropertyFilter.lt("date_end", System.currentTimeMillis());
		else if ( data.queryTime == 0 )
			filter = null;
		else if ( data.queryTime == 1 )
			filter = PropertyFilter.gt("date_start", System.currentTimeMillis());	
		
		//Add institution name filter
		if( filter != null )
			query.setFilter(CompositeFilter.and(PropertyFilter.eq("institution".toLowerCase(), instName), filter));
		else
			query.setFilter(PropertyFilter.eq("institution".toLowerCase(), instName));
		
		QueryResults<Entity> tasks = datastore.run(query.build());

		List<Map<String, Value<?>>> events = new ArrayList<Map<String, Value<?>>>();

		//Where to start next time
		Cursor cursor = tasks.getCursorAfter();
		if( cursor != null && events.size() == PAGE_SIZE ) {
			String cursorString = cursor.toUrlSafe();
			Pair p = new Pair(events, cursorString);
			return Response.ok(g.toJson(p)).build();
		}
		else {
			Pair p = new Pair(events, "end");
			return Response.ok(g.toJson(p)).build();
		}		
	} 
	

	/*
	 * Used to retrieve the information about an institution, given its name.
	 */
	@POST
	@Path("/institution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInstitution(QueryData data) {
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getInstitution");
		if( r.getStatus() != 200 ) {
			return r;
		}
	
		String instName = data.name.replaceAll("\\s+", "").toLowerCase();
		Key instKey = institutionKeyFactory.newKey(instName);
		Entity inst = datastore.get(instKey);

		if( inst == null )
			return Response.status(Status.NOT_FOUND).entity("Instituição não existe.").build();

		return Response.ok(g.toJson(inst.getProperties())).build();
	} 

	
	/*
	 * Used to retrieve all the institutions from the system. Use "query cursor"
	 * to send a set number of events per each call.
	 */
	@POST
	@Path("/institutionsAll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInstitutionsAll(QueryData data) {
		
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getInstitutionsAll");
		if( r.getStatus() != 200 ) {
			return r;
		}
				
		Cursor startCursor = null;
		
		if( data.startCursorString != null && !data.startCursorString.equals("") ) {
			//Where query stopped last time
			startCursor = Cursor.fromUrlSafe(data.startCursorString);
		}
		
		List<Map<String, Value<?>>> institutions = new ArrayList<Map<String, Value<?>>>();
		
		//Prepare query that will get institutions
		EntityQuery.Builder query = Query.newEntityQueryBuilder()
				.setKind("Institution")
				.setLimit(PAGE_SIZE)
				.setStartCursor(startCursor);
	
		//Get institutions
		QueryResults<Entity> tasks = datastore.run(query.build());
		
		tasks.forEachRemaining( inst -> {
			System.out.println("test");
			institutions.add(inst.getProperties());
		});
		
		//Where to start next time
		Cursor cursor = tasks.getCursorAfter();
		if( cursor != null && institutions.size() == PAGE_SIZE ) {
			String cursorString = cursor.toUrlSafe();
			Pair p = new Pair(institutions, cursorString);
			return Response.ok(g.toJson(p)).build();
		}
		else {
			Pair p = new Pair(institutions, "end");
			return Response.ok(g.toJson(p)).build();
		}
	} 

	
	/*
	 * Used to retrieve information about a group given a name. 
	 * Use "query cursor"
	 */
	@POST
	@Path("/group")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroup(QueryData data) {
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getGroup");
		if( r.getStatus() != 200 ) {
			return r;
		}
	
		String groupName = data.name.replaceAll("\\s+", "").toLowerCase();
		Key groupKey = groupKeyFactory.newKey(groupName);
		Entity group = datastore.get(groupKey);

		if( group == null )
			return Response.status(Status.NOT_FOUND).entity("Grupo não existe.").build();

		return Response.ok(g.toJson(group.getProperties())).build();
	} 

	
	/*
	 * Used to retrieve all groups. User "query cursor"
	 */
	@POST
	@Path("/groupsAll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllGroups(QueryData data) {
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "groupsAll");
		if( r.getStatus() != 200 ) {
			return r;
		}
				
		Cursor startCursor = null;
		
		if( data.startCursorString != null && !data.startCursorString.equals("") ) {
			//Where query stopped last time
			startCursor = Cursor.fromUrlSafe(data.startCursorString);
		}
		
		List<Map<String, Value<?>>> events = new ArrayList<Map<String, Value<?>>>();
		
		//Prepare query that will get groups
		EntityQuery.Builder query = Query.newEntityQueryBuilder()
				.setKind("Group")
				.setLimit(PAGE_SIZE)
				.setStartCursor(startCursor);
	
		//Get groups
		QueryResults<Entity> tasks = datastore.run(query.build());
		
		while( tasks.hasNext() ) {
			Entity task = tasks.next();
			events.add(task.getProperties());
		}
		
		//Where to start next time
		Cursor cursor = tasks.getCursorAfter();
		if( cursor != null && events.size() == PAGE_SIZE ) {
			String cursorString = cursor.toUrlSafe();
			Pair p = new Pair(events, cursorString);
			return Response.ok(g.toJson(p)).build();
		}
		else {
			Pair p = new Pair(events, "end");
			return Response.ok(g.toJson(p)).build();
		}
	} 
	
	
	/*
	 * Used to retrieve all comments from an event
	 */
	@POST
	@Path("/comments")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComments(QueryData data) {
		Key authTokenKey = authTokenKeyFactory.newKey(data.at.tokenID);
		Entity authToken = datastore.get(authTokenKey);

		//Check login
		Response r = checkLogin( data.at, authToken);
		if( r.getStatus() != 200 ) {
			return r;
		}
		
		//Check RBAC
		r = checkRBAC(authToken.getString("role"), "getComments");
		if( r.getStatus() != 200 ) {
			return r;
		}
			
		Cursor startCursor = null;
		
		if( data.startCursorString != null && !data.startCursorString.equals("") ) {
			//Where query stopped last time
			startCursor = Cursor.fromUrlSafe(data.startCursorString);
		}
		
		List<Map<String, Value<?>>> comments = new ArrayList<Map<String, Value<?>>>();
		String eventName = data.name.replaceAll("\\s+", "").toLowerCase();
		
		//Prepare query that will get comments
		EntityQuery.Builder query = Query.newEntityQueryBuilder()
				.setKind("Comment")
				.setFilter(PropertyFilter.hasAncestor(eventKeyFactory.newKey(eventName)))
				.setLimit(PAGE_SIZE)
				.setStartCursor(startCursor);
	
		//Set order asc or desc
		if( data.newFirst == false )
			query.setOrderBy(OrderBy.asc("date"));
		else
			query.setOrderBy(OrderBy.desc("date"));
		
		//Get comments
		QueryResults<Entity> tasks = datastore.run(query.build());
		
		while( tasks.hasNext() ) {
			Entity task = tasks.next();
			comments.add(task.getProperties());
		}
		
		//Where to start next time
		Cursor cursor = tasks.getCursorAfter();
		if( cursor != null && comments.size() == PAGE_SIZE ) {
			String cursorString = cursor.toUrlSafe();
			Pair p = new Pair(comments, cursorString);
			return Response.ok(g.toJson(p)).build();
		}
		else {
			Pair p = new Pair(comments, "end");
			return Response.ok(g.toJson(p)).build();
		}
	} 

	
	
	/*
	 * Used to check if a user is in a session, by doing some checks to authToken
	 */
	private Response checkLogin(AuthToken at, Entity authToken) {
		//Check both given token and database token
		if( at == null || authToken == null ) {
			LOG.warning("Attempt to operate with no login.");
			return Response.status(Status.NOT_FOUND).entity("Login não encontrado.").build();
		}
		
		//If token is found, check for validity
		if( authToken.getLong("expirationDate") < System.currentTimeMillis() ) {
			LOG.warning("Auth Token expired.");
			datastore.delete(authToken.getKey());
			return Response.status(Status.FORBIDDEN).entity("Auth Token expirado. Faça login antes de tentar novamente.").build();
		}
		
		return Response.ok().build();
	}
	
	
	/*
	 * Used to check if the user can perform a certain method
	 */
	private Response checkRBAC(String role, String methodName) {
		Key methodKey = rbacKeyFactory.newKey(methodName.toLowerCase());
		Entity method = datastore.get(methodKey);
		
		//Check if method exists
		if( method == null ) {
			return Response.status(Status.NOT_FOUND).entity("Método especificado não existe.").build();
		}
		
		//Check RBAC
		if( method.getBoolean(role) == false ) {
			LOG.warning("User doesn't have permission for this action.");
			return Response.status(Status.FORBIDDEN).entity("Você não tem permissão para realizar esta ação.").build();
		} 
		else
			return Response.ok().build();
	}
	
}
