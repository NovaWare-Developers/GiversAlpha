package pt.unl.fct.di.example.apdc2021.data.model;

// Class with the parameters for the input REST service
// They are translated to JSON by RETROFIT2+gson-converter
public class UserCredentials {
    String username;
    String password;
    String passwordConfirm;
    String email;
    String name;

    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserCredentials(String email, String password, String passwordConfirm, String username, String name) {
        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.email = email;
        this.name = name;
    }

    public String getEmail() { return email; }

    public String getPasswordConfirm() { return passwordConfirm; }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() { return  name; }

}
