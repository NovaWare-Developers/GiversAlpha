package pt.unl.fct.di.example.apdc2021.data;

import com.google.gson.annotations.SerializedName;

public class Token {
    @SerializedName("at")
    private AuthToken at;

    public AuthToken getAt() {
        return at;
    }

    public Token(AuthToken token) {
        this.at = token;
    }
}
