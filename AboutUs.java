package com.example.caipsgcms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.os.Bundle;
import android.widget.TextView;

public class AboutUs extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        textView = findViewById(R.id.textViewAboutUs);
        textView.setText(R.string.aboutUs);
    }
}