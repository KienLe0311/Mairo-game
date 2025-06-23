package com.example.mariogame;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnStart).setOnClickListener(v ->
                startActivity(new Intent(this, GameActivity.class)));

        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistroryActivity.class)));
    }
}
