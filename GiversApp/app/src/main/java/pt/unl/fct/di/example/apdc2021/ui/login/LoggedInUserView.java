package pt.unl.fct.di.example.apdc2021.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String displayName;
    private String tokenId;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName, String tokenId) {
        this.displayName = displayName;
        this.tokenId = tokenId;
    }

    String getDisplayName() {
        return displayName;
    }

    String getTokenId(){ return tokenId; }
}