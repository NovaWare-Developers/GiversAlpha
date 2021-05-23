package pt.unl.fct.di.example.apdc2021.data.model;

import pt.unl.fct.di.example.apdc2021.data.InfoLong;
import pt.unl.fct.di.example.apdc2021.data.InfoString;

public class UserProfile {

        public InfoString phoneNr;
        public InfoString name;
        public InfoLong dateOfBirth;
        public InfoString gender;
        public InfoString nationality;
        public InfoString address;
        public InfoString description;
        public InfoString photo;

        public UserProfile(InfoString phoneNr, InfoString name, InfoLong dateOfBirth, InfoString gender, InfoString nationality, InfoString address, InfoString description, InfoString photo) {
                this.phoneNr = phoneNr;
                this.name = name;
                this.dateOfBirth = dateOfBirth;
                this.gender = gender;
                this.nationality = nationality;
                this.address = address;
                this.description = description;
                this.photo = photo;
        }

        public InfoString getPhoneNr() { return phoneNr; }

        public InfoString getName() {
                return name;
        }

        public InfoLong getDateOfBirth() {
                return dateOfBirth;
        }

        public InfoString getGender() {
                return gender;
        }

        public InfoString getNationality() {
                return nationality;
        }

        public InfoString getAddress() {
                return address;
        }

        public InfoString getDescription() {
                return description;
        }

       public InfoString getPhoto() { return photo; }
}
