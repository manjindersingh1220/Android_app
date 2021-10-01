package com.example.caipsgcms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity2 extends AppCompatActivity {
FloatingActionButton next,back;
TextView textview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        next = findViewById(R.id.next2);
        back = findViewById(R.id.back2);
        textview = findViewById(R.id.textviewsecond);
        //String text = "These notes can be ordered by a Canadian Citizen or Permanent resident only, under Access to Information Act.<font color=#DD0000>Read more</font>";
        //textview.setText(Html.fromHtml(text));
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                startActivity(intent);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this,MainActivity.class);
                //startActivity(intent);
                onBackPressed();
            }
        });
    }
}