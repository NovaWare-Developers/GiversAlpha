package pt.unl.fct.di.example.apdc2021.data;

import com.google.gson.annotations.SerializedName;

public class EditUserProfile {
    @SerializedName("address")
    String address;
    @SerializedName("at")
    AuthToken at;
    @SerializedName("dateOfBirth")
    long dateOfBirth;
    @SerializedName("description")
    String description;
    @SerializedName("gender")
    String gender;
    @SerializedName("interests")
    String[] interests = new String[] {};
    @SerializedName("nationality")
    String nationality;
    @SerializedName("phoneNr")
    String phoneNr;
    @SerializedName("photo")
    byte[] photo;

    public EditUserProfile(String address, AuthToken at, long dateOfBirth, String description, String gender, String nationality, String phoneNr) {
        this.address = address;
        this.at = at;
        this.dateOfBirth = dateOfBirth;
        this.description = description;
        this.gender = gender;
        this.nationality = nationality;
        this.phoneNr = phoneNr;
    }

    public EditUserProfile(String address, AuthToken at, long dateOfBirth, String description, String gender, String nationality, String phoneNr, byte[] photo) {
        this.address = address;
        this.at = at;
        this.dateOfBirth = dateOfBirth;
        this.description = description;
        this.gender = gender;
        this.nationality = nationality;
        this.phoneNr = phoneNr;
        this.photo = photo;
    }

    public String getAddress() {
        return address;
    }

    public AuthToken getAt() {
        return at;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDescription() {
        return description;
    }

    public String getGender() {
        return gender;
    }

    public String[] getInterests() {
        return interests;
    }

    public String getNationality() {
        return nationality;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public byte[] getPhoto() {
        return photo;
    }
}
