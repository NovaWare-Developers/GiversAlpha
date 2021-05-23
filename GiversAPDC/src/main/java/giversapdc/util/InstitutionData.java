package giversapdc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class InstitutionData {

	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,3}$", Pattern.CASE_INSENSITIVE);
	
	public String name;
	public String email;
	public String phoneNr;
	public String address;
	public long lat;
	public long lon;
	
	public String password;
	public String passwordConfirm;
	
	//public String photo;
	public byte[] photo;
	
	public AuthToken at;
	
	public String startCursorString;
	//This var is used for the query of events to select which time to show: -1 ended, 0 all, 1 future, 2 ongoing
	public int queryTime;
	
	public InstitutionData() { }
	
	public Response validDataRegister() {
		if( !validName() ) 
			return Response.status(Status.BAD_REQUEST).entity("Nome da instituição deverá ter pelo menos comprimento 3.").build();
		else if( !validEmail() ) 
			return Response.status(Status.BAD_REQUEST).entity("Email deverá ter formato ***@***.*** .").build();
		else if( !validPhoneNr() ) 
			return Response.status(Status.BAD_REQUEST).entity("Número de telefone deverá ter 9 algoritmos.").build();
		else if( !validAddress() ) 
			return Response.status(Status.BAD_REQUEST).entity("Morada deverá ter pelo menos comprimento 3.").build();
		else if( !validCoords() )
			return Response.status(Status.BAD_REQUEST).entity("Coordenadas incorretas.").build();
		else
			return Response.ok().build();
	}
	
	public Response validDataEdit() {
		if( !this.email.equals("") && !validEmail() ) 
			return Response.status(Status.BAD_REQUEST).entity("Email deverá ter formato ***@***.*** .").build();
		else if( !this.phoneNr.equals("") && !validPhoneNr() ) 
			return Response.status(Status.BAD_REQUEST).entity("Número de telefone deverá ter 9 algoritmos.").build();
		else if( !this.address.equals("") && !validAddress() ) 
			return Response.status(Status.BAD_REQUEST).entity("Morada deverá ter pelo menos comprimento 3.").build();
		else if( !validCoords() )
			return Response.status(Status.BAD_REQUEST).entity("Coordenadas incorretas.").build();
		else
			return Response.ok().build();
	}
		
	public boolean validName() {
		return this.name != null && this.name.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validEmail() {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(this.email.replaceAll("\\s+", "")); 
		return this.email != null && matcher.find();
	}
	
	public boolean validPhoneNr() {
		return this.phoneNr != null && phoneNr.replaceAll("\\s+", "").length() == 9;
	}
	
	public boolean validAddress() {
		return this.address != null && address.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validCoords() {
		return validLat() && validLon();
	}
	
	public boolean validLat() {
		return this.lat >= -90 && this.lat <= 90;
	}
	
	public boolean validLon() {
		return this.lon >= -180 && this.lon <= 180;
	}
	
}
