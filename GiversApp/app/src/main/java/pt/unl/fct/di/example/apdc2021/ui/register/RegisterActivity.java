package pt.unl.fct.di.example.apdc2021.ui.register;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import pt.unl.fct.di.example.apdc2021.ExecutorApp;
import pt.unl.fct.di.example.apdc2021.MainActivity;
import pt.unl.fct.di.example.apdc2021.R;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private RegisterActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mActivity = this;
        // The Application is necessary to extract the executor service.
        registerViewModel = new ViewModelProvider(this, new RegisterViewModelFactory(((ExecutorApp) getApplication()).getExecutorService()))
                .get(RegisterViewModel.class);

        final EditText pNameEditText = findViewById(R.id.personalNameRegist);
        final EditText emailEditText = findViewById(R.id.emailRegister);
        final EditText usernameEditText = findViewById(R.id.usernameRegister);
        final EditText passwordEditText = findViewById(R.id.passwordRegister);
        final EditText confirmEditText = findViewById(R.id.confirmPass);
        final Button registerButton = findViewById(R.id.registerBtn);
        final ProgressBar loadingProgressBar = findViewById(R.id.loadingRegister);

        registerViewModel.getRegisterFormState().observe(this, new Observer<RegisterFormState>() {
            @Override
            public void onChanged(@Nullable RegisterFormState registerFormState) {
                if (registerFormState == null) {
                    return;
                }
                registerButton.setEnabled(registerFormState.isDataValid());

                if (registerFormState.getEmailError() != null) {
                    emailEditText.setError(getString(registerFormState.getEmailError()));
                }

                if (registerFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(registerFormState.getUsernameError()));
                }

                if (registerFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(registerFormState.getPasswordError()));
                }

                if (registerFormState.getConfirmationError() != null) {
                    confirmEditText.setError(getString(registerFormState.getConfirmationError()));
                }
            }
        });

        registerViewModel.getRegisterResult().observe(this, new Observer<RegisterResult>() {
            @Override
            public void onChanged(@Nullable RegisterResult registerResult) {
                if (registerResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (registerResult.getError() != null) {
                    showRegisterFailed(registerResult.getError());
                }
                if (registerResult.getSuccess() != null) {
                    updateUiWithUser(registerResult.getSuccess());
                    setResult(Activity.RESULT_OK);
                    Intent i = new Intent(mActivity, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), emailEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    registerViewModel.register(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), confirmEditText.getText().toString(), emailEditText.getText().toString(), pNameEditText.getText().toString());
                }
                return false;
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!passwordEditText.getText().toString().equals(confirmEditText.getText().toString())) {
                    showRegisterFailed(R.string.invalid_confirmation);
                    return;
                }
                if(pNameEditText.getText().toString().isEmpty()){
                    showRegisterFailed(R.string.invalid_name);
                    return;
                }
                loadingProgressBar.setVisibility(View.VISIBLE);
                registerViewModel.register(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), confirmEditText.getText().toString(), emailEditText.getText().toString(), pNameEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(RegisteredUserView model) {
        String register = getString(R.string.registered) + " " + model.getUsername();
        Toast.makeText(getApplicationContext(), register, Toast.LENGTH_LONG).show();
    }

    private void showRegisterFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}