package com.example.adictic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.UserRegister;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Crypt;
import com.example.adictic.util.Funcions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class Register extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        Funcions.closeKeyboard(findViewById(R.id.main_parent),this);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Button b_reg = Register.this.findViewById(R.id.register_button);
        // This is the listener that will be used when the user presses the "Register" button
        b_reg.setOnClickListener(v -> {
            EditText u = Register.this.findViewById(R.id.register_username);
            EditText p1 = Register.this.findViewById(R.id.register_password1);
            EditText p2 = Register.this.findViewById(R.id.register_password2);
            EditText e = Register.this.findViewById(R.id.register_email);

            TextView noValidEmail = Register.this.findViewById(R.id.TV_noValidEmail);
            TextView noPwMatch = Register.this.findViewById(R.id.TV_NoPwMatch);

            noValidEmail.setVisibility(GONE);
            noPwMatch.setVisibility(GONE);

            if(p1.getText().toString().equals(p2.getText().toString())) {
                if(isValidEmail(e.getText())){
                    Register.this.checkCredentials(u.getText().toString(), p1.getText().toString(), e.getText().toString());
                }
                else{
                    noValidEmail.setVisibility(View.VISIBLE);
                }
            }
            else{
                if(!isValidEmail(e.getText())){
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

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful()) {
                    Register.this.startActivity(new Intent(Register.this, Login.class));
                    Login.getInstance().finish();
                    Toast toast = Toast.makeText(Register.this, getString(R.string.good_register), Toast.LENGTH_SHORT);
                    toast.show();
                    Register.this.finish();
                } else {
                    Toast toast = Toast.makeText(Register.this, getString(R.string.error_noRegister), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast toast = Toast.makeText(Register.this, getString(R.string.error_noRegister), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}
