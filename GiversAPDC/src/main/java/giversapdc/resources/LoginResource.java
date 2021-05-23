package giversapdc.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import giversapdc.util.AuthToken;
import giversapdc.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private KeyFactory authTokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
	
	public LoginResource() { }
	
	/*
	 * Method that calls the rest /login.
	 * Allows a user to login to the app with attributes username and password. If an auth token is valid,
	 * login is aborted/failed.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginUser(LoginData data) {
		LOG.info("Login attempt by user.");
		
		Transaction txn = datastore.newTransaction();
		try {
			//Initial check for session login
			if( data.at != null ) {
				LOG.warning("Attempt to login while already in a logged session.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Deve fazer logout antes de tentar fazer login novamente.").build();
			}
			
			if( data.username == null || data.password == null ) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Dados inválidos.").build();
			}
			
			Key userKey = userKeyFactory.newKey(data.username.toLowerCase());
			Entity user = datastore.get(userKey);
			
			if( user == null ) {
				LOG.warning("User does not exist.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Utilizador não existe.").build();
			} 
			else if( user.getBoolean("state") == false ) {
				LOG.warning("Attempt to login disabled account.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Conta inativa.").build();
			}
			
			//Get DB hashed pass and check if it matches the given pass
			String hashedPass = (String) user.getString("password");
			if( !hashedPass.equals(DigestUtils.sha512Hex(data.password)) ) {
				LOG.warning("Incorrect username or password.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Username ou password incorretos.").build();
			}
			else {		//Return auth token to user and store in DB
				AuthToken token = new AuthToken(data.username.toLowerCase(), user.getString("role"));
				
				//Using tokenID as key, because if it were the username, could not have 2 different tokens for same user
				Key authTokenKey = authTokenKeyFactory.newKey(token.tokenID); 
				Entity authToken = txn.get(authTokenKey);
				authToken = Entity.newBuilder(authTokenKey)
						.set("username", token.username)
						.set("role", token.role)
						.set("creationDate", token.creationDate)
						.set("expirationDate", token.expirationDate)
						.build();
				
				txn.add(authToken);
				txn.commit();
				LOG.info("User " + data.username + " logged in successfully.");
				return Response.ok(g.toJson(token)).build();
			}
		} catch( Exception e ) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something broke.").build();
		} finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
	
	
}
