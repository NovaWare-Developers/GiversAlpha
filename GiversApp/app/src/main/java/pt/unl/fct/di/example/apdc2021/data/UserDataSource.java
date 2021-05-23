package pt.unl.fct.di.example.apdc2021.data;

import com.google.gson.Gson;

import java.io.IOException;

import pt.unl.fct.di.example.apdc2021.data.model.GetProfileParam;
import pt.unl.fct.di.example.apdc2021.data.model.LoggedInUser;
import pt.unl.fct.di.example.apdc2021.data.model.RegisteredUser;
import pt.unl.fct.di.example.apdc2021.data.model.UserAuthenticated;
import pt.unl.fct.di.example.apdc2021.data.model.UserCredentials;
import pt.unl.fct.di.example.apdc2021.data.model.UserProfile;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class UserDataSource {

    private UserService service;

    public UserDataSource() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://givers-volunteering.appspot.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.service = retrofit.create(UserService.class);
    }

    public Result<LoggedInUser> login(String username, String password) {
        Call<UserAuthenticated> userAuthenticationCall = service.authenticateUser(new UserCredentials(username, password));
        try {
            Response<UserAuthenticated> response = userAuthenticationCall.execute();
            if (response.isSuccessful()) {
                UserAuthenticated ua = response.body();
                return new Result.Success<>(new LoggedInUser(ua.getTokenID(),ua.getUsername()));
            }
            return new Result.Error(new Exception(response.errorBody().toString()));
        } catch (IOException e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public Result<Void> logout(String tokenID, String username) {
        Call<Void> signOut = service.logoutUser(new Token(new AuthToken(tokenID, username)));
        try {
            Response<Void> response = signOut.execute();
            if (response.isSuccessful()) {
                return new Result.Success<>(null);
            }
            return new Result.Error(new Exception(response.errorBody().toString()));
        } catch (IOException e) {
            return new Result.Error(new IOException("Error logout", e));
        }
    }

    public Result<RegisteredUser> register(String username, String password, String confirmation, String email, String name) {

        Call<Void> userAuthenticationCall = service.registerUser(new UserCredentials(email, password, confirmation, username, name));
        try {
            Response<Void> response = userAuthenticationCall.execute();
            if (response.isSuccessful()) {
                return new Result.Success<>(new RegisteredUser(username, email, name));
            }
            return new Result.Error(new Exception(response.errorBody().toString()));
        } catch (IOException e) {
            return new Result.Error(new IOException("Error registing in", e));
        }
    }

    public Result<UserProfile> getProfile(String tokenID, String username) {
        Call<UserProfile> getProfile = service.getUserProfile(new GetProfileParam(new AuthToken(tokenID, username), username));
        try {
            Response<UserProfile> response = getProfile.execute();
            if (response.isSuccessful()) {
                return new Result.Success<>(response.body());
            }
            return new Result.Error(new Exception(response.errorBody().toString()));
        } catch (IOException e) {
            return new Result.Error(new IOException("Error showing profile", e));
        }
    }


    public Result<Void> editProfile(String address, long bday, String gender, String phoneNum, String nacionality, String description, String tokenID) {
        //System.out.println("-----" + " " + address + " " + bday + " " + gender + " " + phoneNum + " " + nacionality + " " + description + " " + tokenID);
        Call<Void> editProfile = service.editUserProfile(new EditUserProfile(address, new AuthToken(tokenID, ""), bday, description, gender, nacionality, phoneNum));
        System.out.println(new Gson().toJson(new EditUserProfile(address, new AuthToken(tokenID, ""), bday, description, gender, nacionality, phoneNum)));
        try {
            Response<Void> response = editProfile.execute();
            if (response.isSuccessful()) {
                return new Result.Success<>(response.body());
            }
            return new Result.Error(new Exception(response.errorBody().toString()));
        } catch (IOException e) {
            return new Result.Error(new IOException("Error showing profile", e));
        }
    }

    public Result<Void> editPhoto(String address, long bday, String gender, String phoneNum, String nacionality, String description, String tokenID, byte[] photo) {
        //System.out.println("-----" + " " + address + " " + bday + " " + gender + " " + phoneNum + " " + nacionality + " " + description + " " + tokenID);
        Call<Void> editProfile = service.editUserProfile(new EditUserProfile(address, new AuthToken(tokenID, ""), bday, description, gender, nacionality, phoneNum, photo));
        System.out.println(new Gson().toJson(new EditUserProfile(address, new AuthToken(tokenID, ""), bday, description, gender, nacionality, phoneNum, photo)));
        try {
            Response<Void> response = editProfile.execute();
            if (response.isSuccessful()) {
                return new Result.Success<>(response.body());
            }
            return new Result.Error(new Exception(response.errorBody().toString()));
        } catch (IOException e) {
            return new Result.Error(new IOException("Error showing profile", e));
        }
    }
}