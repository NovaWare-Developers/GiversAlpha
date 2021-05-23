package pt.unl.fct.di.example.apdc2021;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import pt.unl.fct.di.example.apdc2021.ui.login.LoginActivity;
import pt.unl.fct.di.example.apdc2021.ui.login.SessionManagement;
import pt.unl.fct.di.example.apdc2021.ui.register.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private MainActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        SessionManagement sessionManagement = new SessionManagement(mActivity);
        String userToken = sessionManagement.getSession();
        String username = sessionManagement.getUsername();
        if(!userToken.equals("null")){
            Intent i = new Intent(mActivity, DrawerActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("tokenId", userToken);
            i.putExtra("username", username);
            startActivity(i);
            finish();
        }
    }

    public void enterLoginActivity(View view){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void enterRegisterActivity(View view){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}