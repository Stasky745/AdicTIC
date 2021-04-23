package com.example.adictic.ui.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.adictic.R;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.inici.Register;
import com.example.adictic.util.TodoApp;

public class ChangePasswordActivity extends Activity {

    TodoApi todoApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.popup_change_password);
        todoApi = ((TodoApp) this.getApplication()).getAPI();

        Button b_accept = findViewById(R.id.BT_popCP_accept);
        Button b_cancel = findViewById(R.id.BT_popCP_cancel);

        b_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pOld = ChangePasswordActivity.this.findViewById(R.id.ET_popCP_actualPass);
                EditText p1 = ChangePasswordActivity.this.findViewById(R.id.ET_popCP_newPass);
                EditText p2 = ChangePasswordActivity.this.findViewById(R.id.ET_popCP_newPass2);

                //todoApi.changePassword();
            }
        });

        b_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
