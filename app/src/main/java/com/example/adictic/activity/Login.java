package com.example.adictic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.User;
import com.example.adictic.entity.UserLogin;
import com.example.adictic.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// This is the Login fragment where the user enters the username and password and
// then a RESTResponder_RF is called to check the authentication
public class Login extends AppCompatActivity {

    TodoApi mTodoService;
    static Login login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        login = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Button b_log = (Button)findViewById(R.id.login_button);
        // This is the listener that will be used when the user presses the "Login" button
        b_log.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText u = (EditText) Login.this.findViewById(R.id.login_username);
                EditText p = (EditText) Login.this.findViewById(R.id.login_password);
                RadioButton tutor = (RadioButton)findViewById(R.id.RB_tutor);
                RadioButton tutelat = (RadioButton)findViewById(R.id.RB_tutelat);
                EditText deviceName = (EditText)findViewById(R.id.PT_nomDisp);

                if(tutor.isChecked()) Login.this.checkCredentials(u.getText().toString(), p.getText().toString(),1,"");
                else if(tutelat.isChecked()){
                    if(!deviceName.getText().toString().equals("")) Login.this.checkCredentials(u.getText().toString(), p.getText().toString(),0,deviceName.getText().toString());
                    else{
                        Toast toast = Toast.makeText(Login.this, getString(R.string.error_noDeviceName), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                else{
                    Toast toast = Toast.makeText(Login.this, getString(R.string.error_noTipus), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        TextView b_reg = (TextView)findViewById(R.id.TV_register);
        // This is the listener that will be used when the user presses the "Register" button
        b_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(Login.this, Register.class);
                //startActivity(i);
                System.out.println("Cap a register anem!");
            }
        });

        RadioGroup RadioGroup = (RadioGroup)findViewById(R.id.RG_Tipus);

        RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int isChecked){
                TextView TV_nomDisp = (TextView)findViewById(R.id.TV_nomDisp);
                EditText PT_nomDisp = (EditText)findViewById(R.id.PT_nomDisp);
                if(isChecked == R.id.RB_tutelat){
                    TV_nomDisp.setVisibility(View.VISIBLE);
                    PT_nomDisp.setVisibility((View.VISIBLE));
                }
                else {
                    TV_nomDisp.setVisibility(View.INVISIBLE);
                    PT_nomDisp.setVisibility((View.INVISIBLE));
                }
            }
        });
    }

    // This method is called when the "Login" button is pressed in the Login fragment
    public void checkCredentials(String username, String password, Integer tutor, String nomDevice) {
        UserLogin ul = new UserLogin();
        ul.username = username;
        ul.password = password;

        Call<User> call = mTodoService.login(ul);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccessful()) {
//                    String TAG = "Firebase Token: ";
//                    FirebaseInstanceId.getInstance().getInstanceId()
//                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                                    if (!task.isSuccessful()) {
//                                        Log.w(TAG, "getInstanceId failed", task.getException());
//                                        return;
//                                    }
//
//                                    // Get new Instance ID token
//                                    String token = task.getResult().getToken();
//
//                                    // Log and toast
//                                    Log.d(TAG, token);
//                                    Call<String> call = ((TodoApp)Login.this.getApplication()).getAPI().sendToken(token);
//                                    call.enqueue(new Callback<String>() {
//                                        @Override
//                                        public void onResponse(Call<String> call, Response<String> response) {
//
//                                        }
//
//                                        @Override
//                                        public void onFailure(Call<String> call, Throwable t) {
//
//                                        }
//                                    });
//                                }
//                            });
                    Login.this.startActivity(new Intent(Login.this, MainActivity.class));
                    Login.this.finish();
                } else {
                    Toast toast = Toast.makeText(Login.this, "Error logging in", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast toast = Toast.makeText(Login.this, "Error logging in", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static Login getInstance(){
        return login;
    }

}