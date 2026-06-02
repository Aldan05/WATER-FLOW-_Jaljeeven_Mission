package com.example.water_flow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        Button buttonCallGov = findViewById(R.id.buttonCallGov);
        Button buttonEmailSupport = findViewById(R.id.buttonEmailSupport);
        Button buttonBackSupport = findViewById(R.id.buttonBackSupport);

        // Call Government Authority (Example: 1916)
        buttonCallGov.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:1916"));
            startActivity(callIntent);
        });

        // Email Technical Support
        buttonEmailSupport.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@pureflow.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Technical Support - Pure Flow");
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        });

        buttonBackSupport.setOnClickListener(v -> finish());
    }
}