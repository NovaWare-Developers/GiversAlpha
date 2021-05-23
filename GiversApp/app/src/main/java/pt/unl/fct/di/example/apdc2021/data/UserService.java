package pt.unl.fct.di.example.apdc2021.data;

import pt.unl.fct.di.example.apdc2021.data.model.GetProfileParam;
import pt.unl.fct.di.example.apdc2021.data.model.UserAuthenticated;
import pt.unl.fct.di.example.apdc2021.data.model.UserCredentials;
import pt.unl.fct.di.example.apdc2021.data.model.UserProfile;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {
    @POST("rest/login")
    Call<UserAuthenticated> authenticateUser(@Body UserCredentials user);

    @POST("rest/register/user")
    Call<Void> registerUser(@Body UserCredentials user);

    @POST("rest/logout")
    Call<Void> logoutUser(@Body Token authToken);

    @POST("rest/query/profile")
    Call<UserProfile> getUserProfile(@Body GetProfileParam data);

    @POST("rest/edit/profile")
    Call<Void> editUserProfile(@Body EditUserProfile edit);

}
