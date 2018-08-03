package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthorisationToken {
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("expiresIn")
    @Expose
    private long expiresIn;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
