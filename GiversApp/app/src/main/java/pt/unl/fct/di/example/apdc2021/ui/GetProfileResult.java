package pt.unl.fct.di.example.apdc2021.ui;

import androidx.annotation.Nullable;

import pt.unl.fct.di.example.apdc2021.data.model.UserProfile;

public class GetProfileResult {
    @Nullable
    private UserProfile success;
    @Nullable
    private Integer error;

    public GetProfileResult(@Nullable Integer error) {
        this.error = error;
    }

    public GetProfileResult(@Nullable UserProfile success) {
        this.success = success;
    }

    @Nullable
    public UserProfile getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}
