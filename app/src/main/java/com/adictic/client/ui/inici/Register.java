package com.adictic.client.ui.inici;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.UserRegister;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Crypt;
import com.adictic.client.R;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    private AdicticApi mTodoService;

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(com.adictic.common.R.string.register));

        Funcions.closeKeyboard(findViewById(R.id.main_parent), this);

        mTodoService = ((AdicticApp) this.getApplication()).getAPI();

        Button b_reg = Register.this.findViewById(R.id.register_button);
        // This is the listener that will be used when the user presses the "Register" button
        b_reg.setOnClickListener(v -> {
            EditText u = Register.this.findViewById(R.id.register_username);
            EditText p1 = Register.this.findViewById(R.id.register_password1);
            EditText p2 = Register.this.findViewById(R.id.register_password2);
            EditText e = Register.this.findViewById(R.id.register_email);

            TextView noValidEmail = Register.this.findViewById(R.id.TV_noValidEmail);
            TextView noPwMatch = Register.this.findViewById(R.id.TV_NoPwMatch);
            TextView noUsername = Register.this.findViewById(R.id.TV_noValidUsername);

            noValidEmail.setVisibility(GONE);
            noPwMatch.setVisibility(GONE);
            noUsername.setVisibility(GONE);

            if (p1.getText().toString().equals(p2.getText().toString())) {
                if (isValidEmail(e.getText().toString().trim())) {
                    Register.this.checkCredentials(u.getText().toString().trim(), p1.getText().toString().trim(), e.getText().toString().trim());
                } else {
                    noValidEmail.setText(getString(R.string.error_noValidEmail));
                    noValidEmail.setVisibility(View.VISIBLE);
                }
            } else {
                if (!isValidEmail(e.getText().toString().trim())) {
                    noValidEmail.setText(getString(R.string.error_noValidEmail));
                    noValidEmail.setVisibility(View.VISIBLE);
                }
                noPwMatch.setVisibility(View.VISIBLE);
            }
        });

        TextView b_login = findViewById(R.id.TV_login);
        // This is the listener that will be used when the user presses the "Register" button
        b_login.setOnClickListener(v -> {
            Register.this.startActivity(new Intent(Register.this, Login.class));
            Login.getInstance().finish();
            Register.this.finish();
        });
    }

    // This method is called when the "Register" button is pressed in the Register fragment
    public void checkCredentials(String username, String password, String email) {
        UserRegister ul = new UserRegister();
        ul.username = Crypt.getAES(username);
        ul.password = Crypt.getSHA256(password);
        ul.email = Crypt.getAES(email);
        Call<String> call = mTodoService.register(ul);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Register.this.startActivity(new Intent(Register.this, Login.class));
                    Login.getInstance().finish();
                    Toast toast = Toast.makeText(Register.this, getString(R.string.good_register), Toast.LENGTH_SHORT);
                    toast.show();
                    Register.this.finish();
                } else {
                    TextView noValidEmail = Register.this.findViewById(R.id.TV_noValidEmail);
                    TextView noUsername = Register.this.findViewById(R.id.TV_noValidUsername);
                    noValidEmail.setVisibility(GONE);
                    noUsername.setVisibility(GONE);
                    try {
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        String[] errors = obj.getString("message").split("\\|");
                        for(String error : errors) {
                            switch (error.trim()) {
                                case "Username already exists":
                                    noUsername.setVisibility(View.VISIBLE);
                                    break;
                                case "Email already exist":
                                    noValidEmail.setText(getString(R.string.emailAlreadyExists));
                                    noValidEmail.setVisibility(View.VISIBLE);
                                    break;
                                default:
                                    Toast toast = Toast.makeText(Register.this, getString(R.string.error_noRegister), Toast.LENGTH_SHORT);
                                    toast.show();
                                    System.err.println("Error desconegut HTTP en Register: "+error);
                                    break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast toast = Toast.makeText(Register.this, getString(R.string.error_noRegister), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}
