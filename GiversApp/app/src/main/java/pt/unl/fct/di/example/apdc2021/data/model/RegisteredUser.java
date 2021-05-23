package pt.unl.fct.di.example.apdc2021.data.model;

public class RegisteredUser {

    private String username;
    private String email;
    private String name;

    public RegisteredUser(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public String getName() { return name; }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
