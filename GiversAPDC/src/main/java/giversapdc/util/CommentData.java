package giversapdc.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class CommentData {

	public String name;		//Event name
	public String comment;
	
	public AuthToken at;
	
	public CommentData() {}
	
	public Response validData() {
		if( !validName() )
			return Response.status(Status.BAD_REQUEST).entity("Nome deve conter pelo menos 1 caractere.").build();
		else if( !validComment() )
			return Response.status(Status.BAD_REQUEST).entity("Comentário deve conter pelo menos 3 caracteres.").build();
		else 
			return Response.ok().build();
	}
	
	public boolean validName() {
		return this.name != null && this.name.replaceAll("\\s+", "").length() >= 1;
	}
	
	public boolean validComment() {
		return this.comment != null && this.comment.replaceAll("\\s+", "").length() >= 3;
	}
}
