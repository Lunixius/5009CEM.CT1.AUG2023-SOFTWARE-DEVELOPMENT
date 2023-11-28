package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Button addEntityButton = findViewById(R.id.addentity);
        Button updateEntityButton = findViewById(R.id.updateentity);
        Button deleteEntityButton = findViewById(R.id.deleteentity);

        addEntityButton.setOnClickListener(view -> {
            // Go to activity_add.xml
            Intent intent = new Intent(homepage.this, add.class);
            startActivity(intent);
        });

        updateEntityButton.setOnClickListener(view -> {
            // Go to activity_update.xml
            Intent intent = new Intent(homepage.this, update.class);
            startActivity(intent);
        });

        deleteEntityButton.setOnClickListener(view -> {
            // Go to activity_delete.xml
            Intent intent = new Intent(homepage.this, delete.class);
            startActivity(intent);
        });
    }
}

