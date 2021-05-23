package pt.unl.fct.di.example.apdc2021.ui.editProfile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;

import pt.unl.fct.di.example.apdc2021.DatePickerFragment;
import pt.unl.fct.di.example.apdc2021.ExecutorApp;
import pt.unl.fct.di.example.apdc2021.R;
import pt.unl.fct.di.example.apdc2021.data.Result;
import pt.unl.fct.di.example.apdc2021.data.UserDataSource;
import pt.unl.fct.di.example.apdc2021.data.model.UserProfile;
import pt.unl.fct.di.example.apdc2021.ui.GetProfileResult;
import pt.unl.fct.di.example.apdc2021.ui.login.SessionManagement;

public class EditProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditProfileActivity mActivity;
    private EditProfileViewModel editProfileViewModel;
    private TextView bdayTextView;
    private String tokenID;
    private MutableLiveData<GetProfileResult> getProfileResult = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tokenID = getIntent().getStringExtra("tokenId");
        Button btnBday = (Button) findViewById(R.id.bttnBday);
        btnBday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        mActivity = this;
        // The Application is necessary to extract the executor service.
        editProfileViewModel = new ViewModelProvider(this, new EditProfileViewModelFactory(((ExecutorApp) getApplication()).getExecutorService()))
                .get(EditProfileViewModel.class);

        final EditText addressEditText = findViewById(R.id.editTextAddress);
        final EditText phoneNumEditText = findViewById(R.id.editTextPhone);
        bdayTextView = (TextView) findViewById(R.id.textBday);
        final Spinner genderSpinner = findViewById(R.id.editGenders);
        final EditText nationalityEditText = findViewById(R.id.editTextNationality);
        final EditText bioEditText = findViewById(R.id.editTextBio);
        final Button submitButton = findViewById(R.id.submitBtn);
        final ProgressBar loadingProgressBar = findViewById(R.id.loadingEdit);

        SessionManagement sessionManagement = new SessionManagement(EditProfileActivity.this);
        UserDataSource dataSource = new UserDataSource();
        Executor executor = ((ExecutorApp) getApplication()).getExecutorService();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Result<UserProfile> result = dataSource.getProfile(tokenID, sessionManagement.getUsername());
                if (result instanceof Result.Success) {
                    UserProfile data = ((Result.Success<UserProfile>) result).getData();
                    getProfileResult.postValue(new GetProfileResult(data));
                } else {
                    getProfileResult.postValue(new GetProfileResult(R.string.get_profile_failed));
                }
            }
        });

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
                    UserProfile up = getProfileResult.getSuccess();
                    if(!up.getAddress().getValue().equals(""))
                        addressEditText.setText(up.getAddress().getValue());
                    if(up.getDateOfBirth().getValue() != 0) {
                        DateFormat simple = new SimpleDateFormat("dd/MM/yyyy");
                        Date result = new Date(up.getDateOfBirth().getValue());
                        bdayTextView.setText(simple.format(result));
                    }
                    if(up.getGender().getValue().equals("Male"))
                        genderSpinner.setSelection(1);
                    if(up.getGender().getValue().equals("Female"))
                        genderSpinner.setSelection(2);
                    if(up.getGender().getValue().equals("Other"))
                        genderSpinner.setSelection(3);
                    if(!up.getPhoneNr().getValue().equals(""))
                        phoneNumEditText.setText(up.getPhoneNr().getValue());
                    if(!up.getNationality().getValue().equals(""))
                        nationalityEditText.setText(up.getNationality().getValue());
                    if(!up.getDescription().getValue().equals(""))
                        bioEditText.setText(up.getDescription().getValue());
                }
            }
        });

        editProfileViewModel.getEditProfileFormState().observe(this, new Observer<EditProfileFormState>() {
            @Override
            public void onChanged(@Nullable EditProfileFormState editProfileFormState) {
                if (editProfileFormState == null) {
                    return;
                }
                submitButton.setEnabled(editProfileFormState.isDataValid());

                if (editProfileFormState.getBdayError() != null) {
                    bdayTextView.setError(getString(editProfileFormState.getBdayError()));
                }

                if (editProfileFormState.getAddressError() != null) {
                    addressEditText.setError(getString(editProfileFormState.getAddressError()));
                }

                if (editProfileFormState.getPhoneNumError() != null) {
                    phoneNumEditText.setError(getString(editProfileFormState.getPhoneNumError()));
                }

                if (editProfileFormState.getNationalityError() != null) {
                    nationalityEditText.setError(getString(editProfileFormState.getNationalityError()));
                }

                if (editProfileFormState.getBioError() != null) {
                    bioEditText.setError(getString(editProfileFormState.getBioError()));
                }
            }
        });

        editProfileViewModel.getEditResult().observe(this, new Observer<EditResult>() {
            @Override
            public void onChanged(@Nullable EditResult editResult) {
                if (editResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (editResult.getError() != null) {
                    showEditFailed(editResult.getError());
                }
                if (editResult.getSuccess() != null) {
                    updateUiWithUser(editResult.getSuccess());
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addressEditText.getText().toString().isEmpty()){
                    showEditFailed(R.string.invalid_address);
                    return;
                }
                if(bdayTextView.getText().toString().equals("No date selected.")){
                    showEditFailed(R.string.invalid_bday);
                    return;
                }
                final String genderText= genderSpinner.getSelectedItem().toString();
                if(genderText.equals("Choose gender")){
                    showEditFailed(R.string.invalid_gender);
                    return;
                }
                if(phoneNumEditText.getText().toString().isEmpty()){
                    showEditFailed(R.string.invalid_phonenum);
                    return;
                }
                if(nationalityEditText.getText().toString().isEmpty()){
                    showEditFailed(R.string.invalid_nationality);
                    return;
                }
                if(bioEditText.getText().toString().isEmpty()){
                    showEditFailed(R.string.invalid_bio);
                    return;
                }
                loadingProgressBar.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date date = sdf.parse( bdayTextView.getText().toString());
                    long millis = date.getTime();
                    //System.out.println("time: " + millis);
                    editProfileViewModel.edit(addressEditText.getText().toString(),
                           millis, genderText, phoneNumEditText.getText().toString(),
                            nationalityEditText.getText().toString(), bioEditText.getText().toString(), tokenID);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private void updateUiWithUser(EditedProfileView model) {
        String edited = getString(R.string.edited);
        Toast.makeText(getApplicationContext(), edited, Toast.LENGTH_LONG).show();
    }

    private void showEditFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = SimpleDateFormat.getDateInstance().format(c.getTime());
        bdayTextView.setText(currentDateString);
    }
}