package pt.unl.fct.di.example.apdc2021;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.unl.fct.di.example.apdc2021.data.Result;
import pt.unl.fct.di.example.apdc2021.data.UserDataSource;
import pt.unl.fct.di.example.apdc2021.data.model.UserProfile;
import pt.unl.fct.di.example.apdc2021.ui.GetProfileResult;
import pt.unl.fct.di.example.apdc2021.ui.home.HomeFragment;
import pt.unl.fct.di.example.apdc2021.ui.profile.ProfileFragment;
import pt.unl.fct.di.example.apdc2021.ui.slideshow.SlideshowFragment;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private CircleImageView profilePicture;
    private DrawerLayout drawer;
    private static final int PICK_IMAGE = 1;
    private String tokenId;
    private MutableLiveData<GetProfileResult> getProfileResult = new MutableLiveData<>();
    private UserProfile userProfile;
    Uri imageUri;
    String imageUrl;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        tokenId = i.getStringExtra("tokenId");
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                new HomeFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_home);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        final TextView usernameText = findViewById(R.id.usernameDisplay);
        Intent i = getIntent();
        username = i.getStringExtra("username");
        usernameText.setText(username);
        if(imageUrl == null) {
            UserDataSource ds = new UserDataSource();
            Executor executor = ((ExecutorApp) getApplication()).getExecutorService();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Result<UserProfile> result = ds.getProfile(tokenId, usernameText.getText().toString());
                    if (result instanceof Result.Success) {
                        UserProfile data = ((Result.Success<UserProfile>) result).getData();
                        getProfileResult.postValue(new GetProfileResult(data));
                    } else {
                        getProfileResult.postValue(new GetProfileResult(R.string.get_profile_failed));
                    }
                }
            });
        }
        getProfileResult.observe(this, new Observer<GetProfileResult>() {
            @Override
            public void onChanged(@Nullable GetProfileResult getProfileResult) {
                if (getProfileResult == null) {
                    return;
                }
                if (getProfileResult.getError() != null) {
                    Toast.makeText(getApplicationContext(), getProfileResult.getError(), Toast.LENGTH_SHORT).show();
                }
                if (getProfileResult.getSuccess() != null) {
                    userProfile= getProfileResult.getSuccess();
                    imageUrl = userProfile.getPhoto().getValue();
                    if(!imageUrl.equals(""))
                        Picasso.get().load(imageUrl).into(profilePicture);

                }
            }
        });

        profilePicture =  findViewById(R.id.circleImageView);
        profilePicture.setOnClickListener(v -> {
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtra("tokenId", tokenId);
                startActivity(i);
                return true;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imageUri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profilePicture.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] photo = baos.toByteArray();
                File file = new File("");

                System.out.println("photo null?" + photo == null);
                UserDataSource ds = new UserDataSource();
                Executor executor = ((ExecutorApp) getApplication()).getExecutorService();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Result<Void> result = ds.editPhoto(userProfile.getAddress().getValue(), userProfile.getDateOfBirth().getValue(),
                        userProfile.getGender().getValue(), userProfile.getPhoneNr().getValue(), userProfile.getNationality().getValue(),
                                userProfile.getDescription().getValue(), tokenId, photo);
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                Fragment profileFragment = new ProfileFragment();
                Bundle data = new Bundle();
                data.putString("tokenId", tokenId);
                data.putString("username", username);
                profileFragment.setArguments(data);
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, profileFragment).commit();
                break;
            case R.id.nav_slideshow:
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new SlideshowFragment()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }
}