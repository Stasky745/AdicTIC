package com.adictic.client.ui.support;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.client.R;

import java.util.Objects;

public class InformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button bt = findViewById(R.id.BT_title);
        WebView wv = findViewById(R.id.WV_content);

        wv.setBackgroundColor(Color.TRANSPARENT);

        String title = getIntent().getStringExtra("title");
        String file = "file:///android_asset/" + getIntent().getStringExtra("file");

        bt.setText(title);
        wv.loadUrl(file);

        bt.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
