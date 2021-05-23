package pt.unl.fct.di.example.apdc2021.ui.register;

/**
 * Class exposing authenticated user details to the UI.
 */
class RegisteredUserView {
    private String username;
    private String email;
    private String name;
    //... other data fields that may be accessible to the UI

    RegisteredUserView(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public String getName() { return name; }

    String getUsername() {
        return username;
    }

    String getEmail(){ return email; }
}