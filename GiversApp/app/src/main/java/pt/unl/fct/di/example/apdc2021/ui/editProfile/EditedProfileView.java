package pt.unl.fct.di.example.apdc2021.ui.editProfile;

/**
 * Class exposing authenticated user details to the UI.
 */
class EditedProfileView {
    private String username;
    private long email;
    private String name;
    //... other data fields that may be accessible to the UI

    EditedProfileView(String username, long email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public String getName() { return name; }

    String getUsername() {
        return username;
    }

    long getEmail(){ return email; }
}