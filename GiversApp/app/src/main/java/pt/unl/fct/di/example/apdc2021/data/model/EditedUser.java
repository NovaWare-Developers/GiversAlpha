package pt.unl.fct.di.example.apdc2021.data.model;

public class EditedUser {
    private String address;
    private String bday;
    private String gender;
    private String phoneNum;
    private String nationality;
    private String bio;

    public EditedUser(String address, String bday, String gender, String phoneNum, String nationality, String bio) {
        this.address = address;
        this.bday = bday;
        this.gender = gender;
        this.phoneNum = phoneNum;
        this.nationality = nationality;
        this.bio = bio;
    }

    public String getAddress() {
        return address;
    }

    public String getBday() {
        return bday;
    }

    public String getGender() {
        return gender;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getNationality() {
        return nationality;
    }

    public String getBio() {
        return bio;
    }
}
