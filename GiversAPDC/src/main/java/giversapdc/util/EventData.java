package giversapdc.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class EventData {

	public String institutionName;
	
	public String name;
	//public List<String> interests;
	public String interests;
	public String address;
	public long dateStart;
	public long duration;
	public int capacity;
	public String description;
	public long[] markers;
	
	public String password;
	public String passwordConfirm;
	
	//public String photo;
	public byte[] photo;
	
	public AuthToken at;
	
	public String startCursorString;
	public int queryTime; 	//This var is used for the query of events to select which time to show: -1 ended, 0 all, 1 future, 2 ongoing
	
	
	public boolean group;		//Used to indicate if joining as group or user
	public String groupName;	
	
	
	public EventData() { }
	
	public Response validDataRegister() {
		if( !validName() )
			return Response.status(Status.BAD_REQUEST).entity("Nome deve conter pelo menos 3 caracteres.").build();
		else if( !validInstitution() )
			return Response.status(Status.BAD_REQUEST).entity("Nome da instituição inválido.").build();
		else if( !validAddress() )
			return Response.status(Status.BAD_REQUEST).entity("Morada deve conter pelo menos 3 caracteres.").build();
		else if( !validDateStart() )
			return Response.status(Status.BAD_REQUEST).entity("Data de início deve ser maior que agora.").build();
		else if( !validDuration() )
			return Response.status(Status.BAD_REQUEST).entity("Duração deve ser maior que zero.").build();
		else if( !validCapacity() )
			return Response.status(Status.BAD_REQUEST).entity("Capacidade deve ser maior que 0.").build();
		else if( !validDescription() )
			return Response.status(Status.BAD_REQUEST).entity("Descrição deve conter pelo menos 3 caracteres.").build();
		else
			return Response.ok().build();
	}
	
	public Response validDataEdit() {
		if( !this.name.equals("") && !validName() )
			return Response.status(Status.BAD_REQUEST).entity("Nome deve conter pelo menos 3 caracteres.").build();
		else if( !this.institutionName.equals("") && !validInstitution() )
			return Response.status(Status.BAD_REQUEST).entity("Nome da instituição inválido.").build();
		else if( !this.address.equals("") && !validAddress() )
			return Response.status(Status.BAD_REQUEST).entity("Morada deve conter pelo menos 3 caracteres.").build();
		else if( !validDateStart() )
			return Response.status(Status.BAD_REQUEST).entity("Data de início deve ser maior que agora.").build();
		else if( !validDuration() )
			return Response.status(Status.BAD_REQUEST).entity("Duração deve ser maior que zero.").build();
		else if( !validCapacity() )
			return Response.status(Status.BAD_REQUEST).entity("Capacidade deve ser maior que 0.").build();
		else if( !this.description.equals("") && !validDescription() )
			return Response.status(Status.BAD_REQUEST).entity("Descrição deve conter pelo menos 3 caracteres.").build();
		else if( !this.interests.equals("") && !validInterests() )
			return Response.status(Status.BAD_REQUEST).entity("Interesse deve conter pelo menos 2 caracteres.").build();
		else
			return Response.ok().build();
	}
	
	
	public boolean validName() {
		return this.name != null && this.name.replaceAll("\\s+", "").length() >= 3;
	}
	
	/*
	public boolean validInterests() {
		return this.interests != null && this.interests.size() > 0;
	}
	*/
	
	public boolean validInterests() {
		return this.interests != null && this.interests.replaceAll("\\s+", "").length() >= 2;
	}
	
	public boolean validAddress() {
		return this.address != null && this.address.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validDateStart() {
		return this.dateStart >= System.currentTimeMillis();
	}
	
	public boolean validDuration() {
		return this.duration > 0;
	}
	
	public boolean validCapacity() {
		return capacity > 0;
	}
	
	public boolean validDescription() {
		return this.description != null && this.description.replaceAll("\\s+", "").length() >= 3;
	}
	
	public boolean validInstitution() {
		return this.institutionName != null && this.institutionName.replaceAll("\\s+", "").length() >= 1;
	}
	
}
