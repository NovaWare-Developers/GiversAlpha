package pt.unl.fct.di.example.apdc2021.ui.editProfile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executor;

import pt.unl.fct.di.example.apdc2021.data.EditProfileRepository;
import pt.unl.fct.di.example.apdc2021.data.UserDataSource;

/**
 * ViewModel provider factory to instantiate RegisterViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class EditProfileViewModelFactory implements ViewModelProvider.Factory {


    // We need this to access the Executor Service created at the LoginApp
    private Executor executor;

    public EditProfileViewModelFactory(Executor executor) {
        this.executor = executor;
    }
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EditProfileViewModel.class)) {
            return (T) new EditProfileViewModel(EditProfileRepository.getInstance(new UserDataSource()),executor);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}