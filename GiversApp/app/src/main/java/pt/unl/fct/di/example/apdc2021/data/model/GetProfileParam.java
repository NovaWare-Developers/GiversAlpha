package pt.unl.fct.di.example.apdc2021.data.model;

import pt.unl.fct.di.example.apdc2021.data.AuthToken;

public class GetProfileParam {
    AuthToken at;
    String name;

    public GetProfileParam(AuthToken at, String name) {
        this.at = at;
        this.name = name;
    }

    public AuthToken getAt() {
        return at;
    }

    public String getName() {
        return name;
    }
}
