package giversapdc.resources;

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
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import giversapdc.util.AuthToken;
import giversapdc.util.UserData;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	
	public LogoutResource() { }
	
	/*
	 * Method that calls the rest /logout.
	 * Used by a user, given a valid auth token, to logout of the app. Auth token is revoked
	 * and deleted from the DB.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logoutUser(UserData data) {
		LOG.info("Attempt to logout user.");
		
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
	
			txn.delete(authTokenKey);
			LOG.fine("User " + data.at.username + " logged out successfully.");
			txn.commit();
			return Response.ok().entity("Logout com sucesso.").build();
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
	
	
	
}
