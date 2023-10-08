package com.example.mapstest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //button to go back to maps activity
        Button backButton = findViewById((R.id.backButton));
        backButton.setOnClickListener(v -> finish());
        //button to go to user guide
        Button webButton = findViewById((R.id.webButton));
        webButton.setOnClickListener(new View.OnClickListener() {
            final Intent intent = new Intent(SettingsActivity.this, WebGuideActivity.class);
            public void onClick(View v) {
                startActivity(intent);
            }
        });
        //button to go to police api website
        Button chromeButton = findViewById((R.id.chromeButton));
        //intent to go outside app here
        chromeButton.setOnClickListener(v -> {
            String url = "https://data.police.uk/docs/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        //button to refresh page
        Button refreshButton = findViewById((R.id.refreshButton));
        //alarm and broadcast here
        refreshButton.setOnClickListener(v -> alarmStart());
    }
    public void alarmStart(){
        //function to refresh page using alarm and broadcast
        Intent intent = new Intent(this, broadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 3, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pendingIntent);
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_LONG).show();
    }
}