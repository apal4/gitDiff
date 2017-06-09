package com.capozio.flightbag.feature.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.capozio.flightbag.R;
import com.capozio.flightbag.data.model.PilotDataResponse;
import com.capozio.flightbag.feature.main.MainActivity;
import com.capozio.flightbag.rest.RestClient;
import com.capozio.flightbag.rest.RestInterface;
import com.capozio.flightbag.util.ToastUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.capozio.flightbag.R.id.backdoor;
import static com.capozio.flightbag.util.Configs.DATADBID;
import static com.capozio.flightbag.util.KeyBoardUtil.hideKeyBoard;

/**
 * Created by PTC on 2/17/2017.
 */
/** User login. First thing the user sees. Uses Firebase. */
public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogIn, btnResetPwd, btnToRegister, btnToLogin, btnBackToLogin;
    private ProgressBar mProgressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

//        if(auth.getCurrentUser() != null) {
//            goToMainActivity();
//        }

        setContentView(R.layout.activity_login);

        // TODO: this is a backdoor for skipping the login, delete in production
        findViewById(backdoor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainActivity("admin@admin.com");
            }
        });



        btnLogIn = (Button) findViewById(R.id.btn_login_action);
        editEmail = (EditText)findViewById(R.id.edit_login_email);
        editPassword = (EditText)findViewById(R.id.edit_login_password);
        mProgressBar = (ProgressBar)findViewById(R.id.progressbar_login);
        btnResetPwd = (Button) findViewById(R.id.btn_pwdreset);
        btnToLogin = (Button) findViewById(R.id.btn_to_login);
        btnToRegister = (Button) findViewById(R.id.btn_to_register);
        btnBackToLogin = (Button) findViewById(R.id.btn_back_to_login);

        btnToLogin.setVisibility(View.GONE);


        /*--------- Login Button ---------*/
        final View.OnClickListener loginButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Email Address is Empty!");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Password is Empty!");
                    return;
                }

                if(password.length() < 6) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Password Too Short! Enter Minimum 6 Characters! ");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);

                // authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(!task.isSuccessful()) {
                                    mProgressBar.setVisibility(View.GONE);
                                    ToastUtil.makeLongToast(getApplicationContext(), "Login Failed! Check your email and password.");
                                } else {
                                    goToMainActivity(email);
                                }
                             }
                        });
            }
        };

         /*--------- Register Button ---------*/
        final View.OnClickListener registerButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Email Address is Empty!");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Password is Empty!");
                    return;
                }

                if(password.length() < 6) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Password Too Short! Enter Minimum 6 Characters! ");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if(!task.isSuccessful()) {
                                    mProgressBar.setVisibility(View.GONE);
                                    String errMsg = task.getException().toString();
                                    String[] msgs = errMsg.split("Exception:");
                                    Log.d("TAG", errMsg);
                                    ToastUtil.makeShortToast(LoginActivity.this, msgs[msgs.length-1].trim());
                                } else {
                                    ToastUtil.makeLongToast(getApplicationContext(), "A new account has been created!");
                                    goToMainActivity(email);
                                }
                            }
                        });
            }
        };

         /*--------- Reset Password Button ---------*/
        final View.OnClickListener resetPasswordButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                if(TextUtils.isEmpty(email)) {
                    ToastUtil.makeLongToast(getApplicationContext(), "Enter your email address to proceed.");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    ToastUtil.makeLongToast(getApplicationContext(), "Make sure the email address is correct!");
                                } else {
                                    ToastUtil.makeLongToast(getApplicationContext(), "Check your email for instructions to reset your password.");
                                    btnBackToLogin.callOnClick();
                                }


                            }
                        });
            }
        };

        btnLogIn.setOnClickListener(loginButtonClickListener);

        btnToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPassword.setText("");
                btnResetPwd.setVisibility(View.GONE);
                btnToLogin.setVisibility(View.VISIBLE);
                btnToRegister.setVisibility(View.GONE);
                btnLogIn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.accent));
                btnLogIn.setText(getString(R.string.action_register_short));
                btnLogIn.setOnClickListener(registerButtonClickListener);
            }
        });

        btnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPassword.setText("");
                btnResetPwd.setVisibility(View.VISIBLE);
                btnToLogin.setVisibility(View.GONE);
                btnToRegister.setVisibility(View.VISIBLE);
                btnLogIn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
                btnLogIn.setText(getString(R.string.action_login_short));
                btnLogIn.setOnClickListener(loginButtonClickListener);
            }
        });

        final TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.textinputlayout_password);

        // this button is only visible in the "reset password" screen
        // go back to the "login" screen from the "reset password" screen
        btnBackToLogin.setVisibility(View.GONE);
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPassword.setText("");
                btnResetPwd.setVisibility(View.VISIBLE);
                btnToLogin.setVisibility(View.GONE);
                btnToRegister.setVisibility(View.VISIBLE);
                btnLogIn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
                btnLogIn.setText(getString(R.string.action_login_short));
                btnLogIn.setOnClickListener(loginButtonClickListener);
                btnBackToLogin.setVisibility(View.GONE);
                passwordLayout.setVisibility(View.VISIBLE);
            }
        });



        btnResetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBackToLogin.setVisibility(View.VISIBLE);
                btnResetPwd.setVisibility(View.GONE);
                btnLogIn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.accent));
                btnLogIn.setText(getString(R.string.action_reset_password_short));
                btnLogIn.setOnClickListener(resetPasswordButtonClickListener);
                passwordLayout.setVisibility(View.GONE);
                btnToRegister.setVisibility(View.GONE);
            }
        });
    }

    // hiding keyboard when the editText is out of focus
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                hideKeyBoard(v);
                v.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }

    private void goToMainActivity(String email) {
        // get pilot info
        final RestInterface restService = RestClient.getInstance().create(RestInterface.class);
        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("EmailAddress", email);
        innerObject.addProperty("type", "Pilot");
        JsonObject emailQuery = new JsonObject();
        emailQuery.add("selector", innerObject);

        restService.getPilotInfo(DATADBID, emailQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PilotDataResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", e.toString());
                    }

                    @Override
                    public void onNext(PilotDataResponse pilotDataResponse) {
                        // send pilot info to main activity so that other fragments will be able to access this data
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.putExtra("pilot", new Gson().toJson(pilotDataResponse));
                        mProgressBar.setVisibility(View.GONE);

                        startActivity(mainIntent);
                        finish();
                    }
                });

    }
}
