package com.adictic.client.ui.inici;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.main.NavActivity;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.User;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.client.R;
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

        Button b_log = findViewById(R.id.login_button);
        TextView b_reg = findViewById(R.id.TV_register);
        // This is the listener that will be used when the user presses the "Login" button
        b_log.setOnClickListener(v -> {
            final EditText u = Login.this.findViewById(R.id.login_username);
            final EditText p = Login.this.findViewById(R.id.login_password);
            final RadioButton tutor = findViewById(R.id.RB_tutor);
            final RadioButton tutelat = findViewById(R.id.RB_tutelat);
            final TextView noTypeDevice = findViewById(R.id.TV_noTypeDevice);

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
                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD,ul.password).apply();
                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN,ul.token).apply();

                    if (usuari.tutor == 1) {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ISTUTOR, true).apply();
                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER,usuari.id).apply();
                    }
                    else {
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ISTUTOR, false).apply();
                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDTUTOR, usuari.id).apply();
                    }

                    if (usuari.tutor == 0) {
                        Bundle extras = new Bundle();

                        extras.putParcelable("user", usuari);
                        extras.putString("token", token);
                        extras.putLong("id", usuari.id);

                        Intent i = new Intent(Login.this, NomFill.class);
                        i.putExtras(extras);

                        Login.this.startActivity(i);
                    } else {
                        Login.this.startActivity(new Intent(Login.this, NavActivity.class));
                    }
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