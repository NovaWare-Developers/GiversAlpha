package pt.unl.fct.di.example.apdc2021.ui.register;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class RegisterFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer confirmationError;

    private boolean isDataValid;

    RegisterFormState(@Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer confirmationError, @Nullable Integer emailError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.emailError = emailError;
        this.confirmationError = confirmationError;
        this.isDataValid = false;
    }

    RegisterFormState(boolean isDataValid) {
        this.emailError = null;
        this.usernameError = null;
        this.passwordError = null;
        this.confirmationError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getConfirmationError() { return confirmationError; }

    @Nullable
    public Integer getEmailError() { return emailError; }

    @Nullable
    Integer getUsernameError() { return usernameError; }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}