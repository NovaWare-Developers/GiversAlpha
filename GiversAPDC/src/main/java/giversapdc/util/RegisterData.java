package giversapdc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class RegisterData {

	//Might want to change this to a stronger verification, for specific domains, for example
	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,3}$", Pattern.CASE_INSENSITIVE);
	
	public String username;
	public String email;
	public String name;
	public String password;
	public String passwordConfirm;
	public AuthToken at;
	
	public RegisterData() { }
	
	public Response validRegisterData() { 
		//Weak passwords, ideally would have at least letters+numbers required
		if( !validPassword() )		
			return Response.status(Status.FORBIDDEN).entity("Password deve conter pelo menos 3 caracteres.").build();
		else if( !validPasswordConf() )
			return Response.status(Status.FORBIDDEN).entity("Confirmação da password incorreta.").build();
		else if( !validEmail())
			return Response.status(Status.FORBIDDEN).entity("Email deve ter o seguinte formato *****@*****.*** .").build();
		else if( !validName() )
			return Response.status(Status.FORBIDDEN).entity("Nome deve conter pelo menos 3 caracteres.").build();
		else
			return Response.ok().build();
	}
		
	public boolean validUsername() {
		return this.username != null && this.username.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validName() {
		return this.name != null && this.username.replaceAll("\\s+", "").length() >= 2;
	}
	
	public boolean validPassword() {
		return this.password != null && this.password.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validPasswordConf() {
		return this.passwordConfirm != null && this.passwordConfirm.equals(this.password);
	}
	
	public boolean validEmail() {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(this.email); 
		return this.email != null && matcher.find();
	}
}
