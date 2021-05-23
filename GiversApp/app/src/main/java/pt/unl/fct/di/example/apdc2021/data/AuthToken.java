package pt.unl.fct.di.example.apdc2021.data;

import com.google.gson.annotations.SerializedName;

public class AuthToken {
    @SerializedName("tokenID")
    private String tokenID;
    @SerializedName("username")
    private String username;

    public AuthToken(String tokenID, String username) {
        this.tokenID = tokenID;
        this.username = username;
    }

    public String getTokenId() {
        return tokenID;
    }

    public String getUsername() {
        return username;
    }
}
