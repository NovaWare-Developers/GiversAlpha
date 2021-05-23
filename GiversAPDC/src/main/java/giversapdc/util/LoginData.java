package giversapdc.util;

public class LoginData {

	public String username;		
	public String password;
	
	public AuthToken at;
	
	public LoginData() { }
	
	public boolean validUsername() {
		return this.username.replaceAll("\\s+", "").length() >= 3;
	}
}
