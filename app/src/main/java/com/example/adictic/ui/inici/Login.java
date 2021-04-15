package com.example.adictic.ui.inici;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.entity.User;
import com.example.adictic.entity.UserLogin;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Crypt;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// This is the Login fragment where the user enters the username and password and
// then a RESTResponder_RF is called to check the authentication
public class Login extends AppCompatActivity {

    TodoApi mTodoService;
    static Login login;

    public static final String TAG = "Login";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        login = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Funcions.closeKeyboard(findViewById(R.id.main_parent),this);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Button b_log = findViewById(R.id.login_button);
        TextView b_reg = findViewById(R.id.TV_register);
        // This is the listener that will be used when the user presses the "Login" button
        b_log.setOnClickListener((View.OnClickListener) v -> {
            final EditText u = Login.this.findViewById(R.id.login_username);
            final EditText p = Login.this.findViewById(R.id.login_password);
            final RadioButton tutor = findViewById(R.id.RB_tutor);
            final RadioButton tutelat = findViewById(R.id.RB_tutelat);
            final TextView noTypeDevice = (TextView)findViewById(R.id.TV_noTypeDevice);

            if(u.getText().length() == 0){
                u.setHint(getString(R.string.error_noUsername));
                u.setHintTextColor(Color.parseColor("#fc8279"));
                p.setHint(getString(R.string.hint_password));
                p.setHintTextColor(Color.parseColor("#808080"));
            }
            else{
                u.setHint(getString(R.string.hint_username));
                u.setHintTextColor(Color.parseColor("#808080"));
                if(p.getText().length()==0){
                    p.setHint(getString(R.string.error_password));
                    p.setHintTextColor(Color.parseColor("#fc8279"));
                } else {
                    p.setHint(getString(R.string.hint_password));
                    p.setHintTextColor(Color.parseColor("#808080"));
                }
            }

            noTypeDevice.setVisibility(View.GONE);

            // Firebase token
            final String TAG = "Firebase Token: ";
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

                        if(tutor.isChecked()) {
                            Log.d(TAG, "TOKEN PASSAR: " + token);
                            Login.this.checkCredentials(u.getText().toString(), p.getText().toString(),1, token);
                        }
                        else if(tutelat.isChecked()){
                            Log.d(TAG, "TOKEN PASSAR: " + token);
                            Login.this.checkCredentials(u.getText().toString(), p.getText().toString(),0, token);
                        }
                        else{
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
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {

                    User usuari = response.body();
                    TodoApp.setTutor(usuari.tutor);
                    TodoApp.setIDTutor(usuari.id);
                    if(usuari.tutor == 0){
                        Bundle extras = new Bundle();

                        extras.putSerializable("user", usuari);
                        extras.putString("token", token);
                        extras.putLong("id", usuari.id);

                        Intent i = new Intent(Login.this, NomFill.class);
                        i.putExtras(extras);

                        Login.this.startActivity(i);
                        Login.this.finish();
                    }
                    else{
                        Login.this.startActivity(new Intent(Login.this, NavActivity.class));
                        Login.this.finish();
                    }
                } else {
                    Toast toast = Toast.makeText(Login.this, getString(R.string.error_noLogin), Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast toast = Toast.makeText(Login.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static Login getInstance(){
        return login;
    }

}