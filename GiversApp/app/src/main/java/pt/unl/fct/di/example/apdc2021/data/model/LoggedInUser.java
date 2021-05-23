package pt.unl.fct.di.example.apdc2021.data.model;

/**
 * Data class that captures user information for logged in users retrieved from
 */
public class LoggedInUser {

    private String tokenId;
    private String displayName;

    public LoggedInUser(String tokenId, String displayName) {
        this.tokenId = tokenId;
        this.displayName = displayName;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getDisplayName() {
        return displayName;
    }
}