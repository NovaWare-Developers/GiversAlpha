package pt.unl.fct.di.example.apdc2021.ui.register;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import pt.unl.fct.di.example.apdc2021.R;
import pt.unl.fct.di.example.apdc2021.data.RegisterRepository;
import pt.unl.fct.di.example.apdc2021.data.Result;
import pt.unl.fct.di.example.apdc2021.data.model.RegisteredUser;

public class RegisterViewModel extends ViewModel {

    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    private RegisterRepository registerRepository;

    private final Executor executor;

    RegisterViewModel(RegisterRepository registerRepository, Executor executor) {
        this.registerRepository = registerRepository;
        this.executor = executor;
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    public void register(String username, String password, String confirmation, String email, String name) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Result<RegisteredUser> result = registerRepository.registerUser(username, password, confirmation, email, name);
                if (result instanceof Result.Success) {
                    RegisteredUser data = ((Result.Success<RegisteredUser>) result).getData();
                    registerResult.postValue(new RegisterResult(new RegisteredUserView(data.getUsername(), data.getEmail(), data.getName())));
                } else {
                    registerResult.postValue(new RegisterResult(R.string.register_failed));
                }
            }
        });
    }

    public void registerDataChanged(String username, String password, String email) {
        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null, null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password,null, null));
        } else if(!isEmailValid(email)){
            registerFormState.setValue(new RegisterFormState(null, null, null, R.string.invalid_email));
        }else {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    private boolean isEmailValid(String email) {
        if (email == null ){
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

}
