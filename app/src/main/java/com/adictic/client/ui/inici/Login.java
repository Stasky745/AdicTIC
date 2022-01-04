package com.adictic.client.ui.inici;

import static com.adictic.client.ui.inici.Register.isValidEmail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.main.NavActivity;
import com.adictic.client.ui.setting.TemporalPasswordChangeActivity;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.RecoverPassword;
import com.adictic.common.entity.User;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;

// This is the Login fragment where the user enters the username and password and
// then a RESTResponder_RF is called to check the authentication
public class Login extends AppCompatActivity {
    private static Login login;
    private AdicticApi mTodoService;
    private SharedPreferences sharedPreferences;
    private final String TAG = "Login";

    public static Login getInstance() {
        return login;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        login = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        sharedPreferences = Funcions.getEncryptedSharedPreferences(Login.this);

        Funcions.closeKeyboard(findViewById(R.id.main_parent), this);

        mTodoService = ((AdicticApp) this.getApplication()).getAPI();

        final EditText u = Login.this.findViewById(R.id.login_username);
        final EditText p = Login.this.findViewById(R.id.login_password);
        final RadioButton tutor = findViewById(R.id.RB_tutor);
        final RadioButton tutelat = findViewById(R.id.RB_tutelat);
        final TextView noTypeDevice = findViewById(R.id.TV_noTypeDevice);
        final Button b_log = findViewById(R.id.login_button);
        final TextView b_reg = findViewById(R.id.TV_register);
        final TextView t_reg = findViewById(R.id.TV_login_register);

        if(getIntent().getBooleanExtra("fromBiometric", false)){
            u.setText(Crypt.decryptAES(sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME,"")));
            tutor.setChecked(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false));
            tutelat.setChecked(!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, true));
            tutor.setEnabled(false);
            tutelat.setEnabled(false);
            if(p.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            t_reg.setVisibility(View.GONE);
            b_reg.setVisibility(View.GONE);
        }

        // This is the listener that will be used when the user presses the "Login" button
        b_log.setOnClickListener(v -> {
            if (u.getText().length() == 0) {
                u.setHint(getString(R.string.error_noUsername));
                u.setHintTextColor(Color.parseColor("#fc8279"));
                p.setHint(getString(R.string.hint_password));
                p.setHintTextColor(Color.parseColor("#808080"));
            } else {
                u.setHint(getString(R.string.hint_username));
                u.setHintTextColor(Color.parseColor("#808080"));
                if (p.getText().length() == 0) {
                    p.setHint(getString(R.string.error_password));
                    p.setHintTextColor(Color.parseColor("#fc8279"));
                } else {
                    p.setHint(getString(R.string.hint_password));
                    p.setHintTextColor(Color.parseColor("#808080"));
                }
            }

            noTypeDevice.setVisibility(View.GONE);

            // Firebase token
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult();

                        // Log and toast
                        Log.d(TAG, token);

                        if (tutor.isChecked()) {
                            Log.d(TAG, "TOKEN PASSAR: " + token);
                            Login.this.checkCredentials(u.getText().toString().trim(), p.getText().toString().trim(), 1, token);
                        } else if (tutelat.isChecked()) {
                            Log.d(TAG, "TOKEN PASSAR: " + token);
                            Login.this.checkCredentials(u.getText().toString().trim(), p.getText().toString().trim(), 0, token);
                        } else {
                            noTypeDevice.setVisibility(View.VISIBLE);
                        }
                    });
        });

        // This is the listener that will be used when the user presses the "Register" button
        b_reg.setOnClickListener(v -> Login.this.startActivity(new Intent(Login.this, Register.class)));

        setRecoverPasswordButton();
    }

    private void setRecoverPasswordButton() {
        findViewById(R.id.TV_login_forgot_pass).setOnClickListener(v -> {
            final AlertDialog.Builder passwordRecoverDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = Login.this.getLayoutInflater();

            passwordRecoverDialog.setTitle(getString(R.string.recover_password));
            passwordRecoverDialog.setMessage(getString(R.string.enter_email_account));

            View dialogView= inflater.inflate(R.layout.recover_dialog, null);
            passwordRecoverDialog.setView(dialogView);

            AlertDialog alertDialog = passwordRecoverDialog.create();

            EditText recover_email = dialogView.findViewById(R.id.ET_recover_email);
            TextView format_invalid = dialogView.findViewById(R.id.TV_recover_format_invalid);
            Button accept = dialogView.findViewById(R.id.BT_recover_accept);
            accept.setOnClickListener(view -> {
                String mail = recover_email.getText().toString().trim();
                if (isValidEmail(mail)){
                    RecoverPassword recoverPassword = new RecoverPassword();
                    recoverPassword.email = mail;
                    mTodoService.sendPetitionToRecoverPassword(recoverPassword).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            super.onResponse(call, response);
                            if(response.isSuccessful()){
                                alertDialog.dismiss();
                                new AlertDialog.Builder(Login.this)
                                        .setTitle(getString(R.string.recover_password))
                                        .setMessage(getString(R.string.recover_password_success))
                                        .setNeutralButton(getString(R.string.accept), (dialog, which) -> dialog.dismiss())
                                        .show();
                            } else if(response.errorBody()!=null) {
                                try {
                                    JSONObject obj = new JSONObject(response.errorBody().string());
                                    if(obj.getString("message").equals("This email doesn't exist in the database")) {
                                        format_invalid.setText(getString(R.string.error_emailNotFound));
                                        format_invalid.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(Login.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(Login.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            super.onFailure(call, t);
                        }
                    });
                } else {
                    format_invalid.setText(getString(R.string.error_noValidEmail));
                    format_invalid.setVisibility(View.VISIBLE);
                }

            });

            Button cancel = (Button) dialogView.findViewById(R.id.BT_recover_cancel);
            cancel.setOnClickListener(view -> {
                alertDialog.dismiss();
            });

            alertDialog.show();
        });
    }


    // This method is called when the "Login" button is pressed in the Login fragment
    public void checkCredentials(String username, String password, final Integer tutor, final String token) {
        UserLogin ul = new UserLogin();
        ul.username = Crypt.getAES(username);
        ul.password = Crypt.getSHA256(password);
        ul.tutor = tutor;
        ul.token = Crypt.getAES(token);
        Call<User> call = mTodoService.login(ul);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User usuari = response.body();
                    assert usuari != null;

                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_USERNAME,ul.username).apply();
                    if(!usuari.temporalPass) //Only save password if is not temporal. Avoid overwriting old good password.
                        sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD, ul.password).apply();
                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN,ul.token).apply();

                    if (usuari.tutor == 1) {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ISTUTOR, true).apply();
                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER,usuari.id).apply();
                    }
                    else {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ISTUTOR, false).apply();
                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDTUTOR, usuari.id).apply();
                    }
                    Intent i;
                    Bundle extras = new Bundle();
                    if (usuari.tutor == 0) {

                        extras.putParcelable("user", usuari);
                        extras.putString("token", token);
                        extras.putLong("id", usuari.id);
                        extras.putInt("tutor", 0);

                        i = new Intent(Login.this, NomFill.class);
                        i.putExtras(extras);
                    } else {
                        extras.putInt("tutor", 1);
                        i = new Intent(Login.this, NavActivity.class);
                    }
                    if(usuari.temporalPass){
                        i = new Intent(Login.this, TemporalPasswordChangeActivity.class);
                        extras.putString("temporalAccess", password);
                        i.putExtras(extras);
                    }
                    Login.this.startActivity(i);
                    Login.this.finish();
                } else {
                    TextView usernameInvalid = Login.this.findViewById(R.id.TV_login_usernameInvalid);
                    TextView passwordInvalid = Login.this.findViewById(R.id.TV_login_passwordError);
                    usernameInvalid.setVisibility(View.INVISIBLE);
                    passwordInvalid.setVisibility(View.INVISIBLE);
                    try {
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        switch (obj.getString("message").trim()) {
                            case "User does not exists":
                                usernameInvalid.setVisibility(View.VISIBLE);
                                break;
                            case "Password does not match":
                                passwordInvalid.setVisibility(View.VISIBLE);
                                break;
                            default:
                                Toast toast = Toast.makeText(Login.this, getString(R.string.error_noLogin), Toast.LENGTH_LONG);
                                toast.show();
                                System.err.println("Error desconegut HTTP en Login: "+obj.getString("message"));
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast toast = Toast.makeText(Login.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}