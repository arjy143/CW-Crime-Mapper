package com.example.mapstest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        //gets intent information from MapsActivity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String category = extras.getString("category");
        String streetName = extras.getString("streetName");
        String latitude = extras.getString("latitude");
        String longitude = extras.getString("longitude");
        String pos = latitude + ", "+ longitude;
        //inserts intent information into text fields on the screen
        TextView infoCategory = findViewById(R.id.infoCategory);
        infoCategory.setText(category);
        TextView infoStreet = findViewById(R.id.infoStreet);
        infoStreet.setText(streetName);
        TextView infoPos = findViewById(R.id.infoPos);
        infoPos.setText(pos);

        //button goes back to MapsActivity when pressed
        Button backButton = findViewById((R.id.backButton));
        backButton.setOnClickListener(v -> finish());
    }
}