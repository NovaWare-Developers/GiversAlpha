package pt.unl.fct.di.example.apdc2021.ui.editProfile;

import androidx.annotation.Nullable;

public class EditProfileFormState {

    @Nullable
    private Integer addressError;
    @Nullable
    private Integer phoneNumError;
    @Nullable
    private Integer bdayError;
    @Nullable
    private Integer genderError;
    @Nullable
    private Integer nationalityError;
    @Nullable
    private Integer bioError;

    private boolean isDataValid;

    EditProfileFormState(@Nullable Integer addressError, @Nullable Integer phoneNumError, @Nullable Integer bdayError, @Nullable Integer genderError, @Nullable Integer nationalityError, @Nullable Integer bioError, boolean isDataValid) {
        this.addressError = addressError;
        this.phoneNumError = phoneNumError;
        this.bdayError = bdayError;
        this.genderError = genderError;
        this.nationalityError = nationalityError;
        this.bioError = bioError;
        this.isDataValid = isDataValid;
    }

    EditProfileFormState(boolean isDataValid) {
        this.bdayError = null;
        this.addressError = null;
        this.phoneNumError = null;
        this.genderError = null;
        this.nationalityError = null;
        this.bioError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getGenderError() { return genderError; }

    @Nullable
    public Integer getBdayError() { return bdayError; }

    @Nullable
    Integer getAddressError() { return addressError; }

    @Nullable
    Integer getPhoneNumError() {
        return phoneNumError;
    }

    @Nullable
    public Integer getNationalityError() {
        return nationalityError;
    }

    @Nullable
    public Integer getBioError() {
        return bioError;
    }

    boolean isDataValid() {
        return isDataValid;
    }

}
