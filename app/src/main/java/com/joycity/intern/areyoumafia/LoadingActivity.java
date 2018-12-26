package com.joycity.intern.areyoumafia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity {
    private TextView loadTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        loadTitle = findViewById(R.id.loadTitle);

        //loadTitle.setText();
    }
}
