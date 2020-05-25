package com.example.adictic.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;

public class InformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);

        Button bt = (Button) findViewById(R.id.BT_title);
        WebView wv = (WebView) findViewById(R.id.WV_content);

        wv.setBackgroundColor(Color.TRANSPARENT);

        String title = getIntent().getStringExtra("title");
        String file = "file:///android_asset/"+getIntent().getStringExtra("file");

        bt.setText(title);
        wv.loadUrl(file);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
