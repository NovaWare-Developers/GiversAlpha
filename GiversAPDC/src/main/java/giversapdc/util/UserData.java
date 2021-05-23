package giversapdc.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Value;

public class UserData {

	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,3}$", Pattern.CASE_INSENSITIVE);
	
	private static final String USER = "USER";
	
	public String username;		
	public String password;
	public String email;
	public String role;
	
	public String passwordConfirm;
	public String oldPassword;
	
	public String phoneNr;
	public String name;
	public long dateOfBirth;
	public String gender;
	public String nationality;
	public String address;
	public List<String> interests;
	public String description;
	//public String photo;
	public byte[] photo;
	
	
	public AuthToken at;
	
	public UserData() { }
		
	public Response validDataEdit() {
		if( !this.phoneNr.equals("") && !validPhoneNr() )
			return Response.status(Status.BAD_REQUEST).entity("Número de telefone deverá ter 9 algarismos.").build();
		else if( !validDateOfBirth() )
			return Response.status(Status.BAD_REQUEST).entity("Data de nascimento inválida").build();
		else if( !this.gender.equals("") && !validGender() )
			return Response.status(Status.BAD_REQUEST).entity("Sexo inválido.").build();
		else if( !this.nationality.equals("") && !validNationality() )
			return Response.status(Status.BAD_REQUEST).entity("Nacionalidade inválida").build();
		else if( !this.address.equals("") && !validAddress() )
			return Response.status(Status.BAD_REQUEST).entity("Morada inválida, deverá conter pelo menos 3 caracteres.").build();
		else if( !this.description.equals("") && !validDescription() )
			return Response.status(Status.BAD_REQUEST).entity("Descrição inválida, deverá conter pelo menos 3 caracteres.").build();
	
		return Response.ok().build();
	}
	
	public boolean validUsername() {
		return this.username != null && this.username.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validPassword() {
		return this.password != null && this.password.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validEmail() {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(this.email); 
		return this.email != null && !this.email.equals("") && matcher.find();
	}
	
	public boolean validName() {
		return this.name != null && this.name.replaceAll("\\s+", "").length() >= 1;
	}
	
	public boolean validPhoneNr() {
		return this.phoneNr != null && this.phoneNr.replaceAll("\\s+", "").length() == 9;
	}
	
	public boolean validDateOfBirth() {
		return this.dateOfBirth > 0 && this.dateOfBirth < System.currentTimeMillis();
	}
	
	public boolean validGender() {
		return this.gender != null;
	}
	
	public boolean validNationality() {
		return this.nationality != null;
	}
	
	public boolean validAddress() {
		return this.address != null && this.address.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validDescription() {
		return this.description != null && this.description.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validRole() {
		return  this.role != null && this.role.equalsIgnoreCase(USER);
	}
	
}
