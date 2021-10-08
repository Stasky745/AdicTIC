package com.adictic.common.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.common.R;
import com.adictic.common.entity.Report;
import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    private Boolean isTypeBug;

    private EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        TextView textView = findViewById(R.id.Report_text);
        editText = findViewById(R.id.Report_editText);
        Button acceptButton = findViewById(R.id.Report_send);
        Button cancelButton = findViewById(R.id.Report_cancel);

        isTypeBug = getIntent().getBooleanExtra("isTypeBug", false);
        Objects.requireNonNull(getSupportActionBar()).setTitle(isTypeBug ? getString(R.string.report_bug) : getString(R.string.report_suggestion));

        if (isTypeBug) {
            textView.setText(getString(R.string.report_message_bug));
        } else {
            textView.setText(getString(R.string.report_message_suggestion));
        }

        acceptButton.setOnClickListener(view -> sendReport());
        cancelButton.setOnClickListener(view -> finish());
    }

    private void sendReport() {
        Api api = ((App)getApplication()).getAPI();
        String text = editText.getText().toString().trim();
        if(text.length()>0) {
            Report report = new Report();
            report.message = text;
            report.type = isTypeBug ? "Bug" : "Suggestion";

            api.sendReport(report).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                    Toast.makeText(getApplicationContext(), getString(R.string.sended_correctly), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
        }
        finish();
    }
}
