package com.example.adictic.ui.inici;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.entity.User;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Crypt;
import com.example.adictic.util.LocaleHelper;
import com.example.adictic.util.TodoApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

    }

    @Override
    protected void onResume() {
        super.onResume();

        final TodoApi todoApi = ((TodoApp) this.getApplication()).getAPI();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    token = task.getResult();

                    Call<User> call = todoApi.checkWithToken(Crypt.getAES(token));
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                            if (response.isSuccessful()) {
                                if (TodoApp.getTutor() == 1)
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                else if (TodoApp.getTutor() == 0 && TodoApp.getIDChild() >= 0)
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                else {
                                    User usuari = response.body();
                                    assert usuari != null;

                                    TodoApp.setTutor(usuari.tutor);
                                    TodoApp.setIDTutor(usuari.id);
                                    if (usuari.tutor == 0 && !usuari.llista.isEmpty()) {
                                        TodoApp.setIDChild(usuari.llista.get(0).idChild);
                                        SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                        SplashScreen.this.finish();
                                    } else if (usuari.tutor == 0) {
                                        Bundle extras = new Bundle();

                                        extras.putSerializable("user", usuari);
                                        extras.putString("token", token);
                                        extras.putLong("id", usuari.id);

                                        Intent i = new Intent(SplashScreen.this, NomFill.class);
                                        i.putExtras(extras);

                                        SplashScreen.this.startActivity(i);
                                        SplashScreen.this.finish();
                                    } else {
                                        SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                        SplashScreen.this.finish();
                                    }
                                }
                            } else {
                                SplashScreen.this.startActivity(new Intent(SplashScreen.this, Login.class));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                            Toast toast = Toast.makeText(SplashScreen.this, "Error checking login status", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = sharedPreferences.getString("language", "none");
        if (lang.equals("none")) super.attachBaseContext(newBase);
        else super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }
}
