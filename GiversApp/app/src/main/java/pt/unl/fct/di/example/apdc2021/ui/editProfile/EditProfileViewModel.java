package pt.unl.fct.di.example.apdc2021.ui.editProfile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;

import pt.unl.fct.di.example.apdc2021.R;
import pt.unl.fct.di.example.apdc2021.data.EditProfileRepository;
import pt.unl.fct.di.example.apdc2021.data.Result;

public class EditProfileViewModel extends ViewModel {

    private MutableLiveData<EditProfileFormState> editProfileFormState = new MutableLiveData<>();
    private MutableLiveData<EditResult> editResult = new MutableLiveData<>();
    private EditProfileRepository editProfileRepository;

    private final Executor executor;

    EditProfileViewModel(EditProfileRepository editProfileRepository, Executor executor) {
        this.editProfileRepository = editProfileRepository;
        this.executor = executor;
    }

    LiveData<EditProfileFormState> getEditProfileFormState() {
        return editProfileFormState;
    }

    LiveData<EditResult> getEditResult() {
        return editResult;
    }

    public void edit(String address, long bday, String gender, String phoneNum, String nationality, String bio, String tokenID) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Result<Void> result = editProfileRepository.editUser(address, bday, gender, phoneNum, nationality, bio, tokenID);
                if (result instanceof Result.Success) {
                    editResult.postValue(new EditResult(new EditedProfileView(address, bday, gender)));
                } else {
                    editResult.postValue(new EditResult(R.string.edit_failed));
                }
            }
        });
    }

}
