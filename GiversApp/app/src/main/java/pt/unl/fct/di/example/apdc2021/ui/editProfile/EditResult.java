package pt.unl.fct.di.example.apdc2021.ui.editProfile;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
public class EditResult {
    @Nullable
    private EditedProfileView success;
    @Nullable
    private Integer error;

    public EditResult(@Nullable Integer error) {
        this.error = error;
    }

    public EditResult(@Nullable EditedProfileView success) {
        this.success = success;
    }

    @Nullable
    public EditedProfileView getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}