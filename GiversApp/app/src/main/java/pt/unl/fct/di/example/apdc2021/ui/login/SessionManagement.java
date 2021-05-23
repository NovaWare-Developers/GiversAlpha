package pt.unl.fct.di.example.apdc2021.ui.login;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    String SHARED_PREF_NAME = "session";
    String SESSION_KEY = "session_user";
    String USERNAME_KEY = "username";

    public SessionManagement(Context context){
        sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void saveSession(LoggedInUserView user){
        editor.putString(SESSION_KEY, user.getTokenId()).commit();
        editor.putString(USERNAME_KEY, user.getDisplayName()).commit();
    }

    public String getSession(){
        return sharedPref.getString(SESSION_KEY, "null");
    }

    public String getUsername(){
        return sharedPref.getString(USERNAME_KEY, "null");
    }

    public void removeSession(){
        editor.putString(SESSION_KEY, "null").commit();
    }

}
