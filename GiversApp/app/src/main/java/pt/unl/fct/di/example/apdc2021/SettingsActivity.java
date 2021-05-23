package pt.unl.fct.di.example.apdc2021;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executor;

import pt.unl.fct.di.example.apdc2021.data.LoginRepository;
import pt.unl.fct.di.example.apdc2021.data.Result;
import pt.unl.fct.di.example.apdc2021.data.UserDataSource;
import pt.unl.fct.di.example.apdc2021.ui.editProfile.EditProfileActivity;
import pt.unl.fct.di.example.apdc2021.ui.login.SessionManagement;

public class SettingsActivity extends AppCompatActivity {

    String items[] = new String [] {"Logout", "Profile Settings"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = getIntent();
        String tokenID = intent.getStringExtra("tokenId");
        ListView listView = (ListView) findViewById(R.id.listSettings);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(items[position]){
                    case "Logout":
                        SessionManagement sessionManagement = new SessionManagement(SettingsActivity.this);
                        LoginRepository loginRepository = LoginRepository.getInstance(new UserDataSource());
                        Executor executor = ((ExecutorApp) getApplication()).getExecutorService();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                Result<Void> result = loginRepository.logout(tokenID, sessionManagement.getUsername());
                            }
                        });
                        sessionManagement.removeSession();
                        Intent logout = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(logout);
                        finish();
                        break;
                    case "Profile Settings":
                        Intent profile = new Intent(SettingsActivity.this, EditProfileActivity.class);
                        profile.putExtra("tokenId", tokenID);
                        startActivity(profile);

                }
            }
        });
    }
}