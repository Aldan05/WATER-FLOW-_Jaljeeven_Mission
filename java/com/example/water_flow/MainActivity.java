package com.example.water_flow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText chlorineEditText, phEditText, silverEditText, tdsEditText;
    private TextView textViewTdsDisplay, textViewModeLabel, textViewOfflineHint;
    private SwitchMaterial switchAutoFetch;
    private Button submitButton, logoutButton, visualizeButton, historyButton, helpSupportButton, aiChatButton;

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private OkHttpClient client = new OkHttpClient();
    
    private Handler autoFetchHandler = new Handler(Looper.getMainLooper());
    private Runnable autoFetchRunnable;
    private static final int FETCH_INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Elements
        chlorineEditText = findViewById(R.id.editTextChlorine);
        phEditText = findViewById(R.id.editTextPH);
        silverEditText = findViewById(R.id.editTextSilver);
        tdsEditText = findViewById(R.id.editTextTDS);
        textViewTdsDisplay = findViewById(R.id.textViewTdsDisplay);
        textViewModeLabel = findViewById(R.id.textViewModeLabel);
        textViewOfflineHint = findViewById(R.id.textViewOfflineHint);
        switchAutoFetch = findViewById(R.id.switchAutoFetch);
        
        submitButton = findViewById(R.id.buttonCheck);
        logoutButton = findViewById(R.id.buttonLogout);
        visualizeButton = findViewById(R.id.buttonVisualize);
        historyButton = findViewById(R.id.buttonHistory);
        helpSupportButton = findViewById(R.id.buttonHelpSupport);
        aiChatButton = findViewById(R.id.buttonAIChat);

        database = FirebaseDatabase.getInstance("https://watersafetyapp-default-rtdb.firebaseio.com").getReference("water_data");

        // Setup Live Mode Auto-Fetch Logic
        autoFetchRunnable = new Runnable() {
            @Override
            public void run() {
                if (switchAutoFetch.isChecked()) {
                    fetchTdsFromESP32();
                    autoFetchHandler.postDelayed(this, FETCH_INTERVAL);
                }
            }
        };

        // Switch Mode: Online (Live Sensor) vs Offline (Manual Entry)
        switchAutoFetch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Online Mode
                textViewModeLabel.setText("Mode: Online (Live Sensor)");
                textViewModeLabel.setTextColor(Color.parseColor("#00796B"));
                textViewTdsDisplay.setVisibility(View.VISIBLE);
                textViewOfflineHint.setVisibility(View.GONE);
                tdsEditText.setEnabled(false); // TDS data comes from the sensor
                autoFetchHandler.post(autoFetchRunnable);
                Toast.makeText(this, "Live Data Mode Active", Toast.LENGTH_SHORT).show();
            } else {
                // Offline Mode
                textViewModeLabel.setText("Mode: Offline (Manual Entry)");
                textViewModeLabel.setTextColor(Color.parseColor("#546E7A"));
                textViewTdsDisplay.setVisibility(View.GONE);
                textViewOfflineHint.setVisibility(View.VISIBLE);
                tdsEditText.setEnabled(true); // TDS must be entered manually
                autoFetchHandler.removeCallbacks(autoFetchRunnable);
                Toast.makeText(this, "Manual Entry Mode Active", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> saveWaterData());
        visualizeButton.setOnClickListener(v -> startActivity(new Intent(this, VisualizeActivity.class)));
        historyButton.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        helpSupportButton.setOnClickListener(v -> startActivity(new Intent(this, HelpSupportActivity.class)));
        aiChatButton.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void fetchTdsFromESP32() {
        String url = "http://192.168.4.1/tds";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    textViewTdsDisplay.setText("TDS: Sensor Offline");
                    textViewTdsDisplay.setTextColor(Color.RED);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        double tds = json.getDouble("tds");
                        runOnUiThread(() -> {
                            tdsEditText.setText(String.valueOf(tds));
                            
                            if (tds <= 0.0) {
                                // No water touching or absolute pure
                                textViewTdsDisplay.setText("TDS: 0.0 ppm (Idle)");
                                textViewTdsDisplay.setTextColor(Color.parseColor("#212121"));
                            } else if (tds <= 500.0) {
                                // Pure/Safe water
                                textViewTdsDisplay.setText("TDS: " + tds + " ppm (Safe)");
                                textViewTdsDisplay.setTextColor(Color.parseColor("#2E7D32"));
                            } else {
                                // Unsafe water
                                textViewTdsDisplay.setText("TDS: " + tds + " ppm (Unsafe)");
                                textViewTdsDisplay.setTextColor(Color.RED);
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> textViewTdsDisplay.setText("TDS: Data Error"));
                    }
                }
            }
        });
    }

    private void saveWaterData() {
        String chl = chlorineEditText.getText().toString();
        String ph = phEditText.getText().toString();
        String sil = silverEditText.getText().toString();
        String tds = tdsEditText.getText().toString();

        if (chl.isEmpty() || ph.isEmpty() || sil.isEmpty() || tds.isEmpty()) {
            Toast.makeText(this, "Please ensure all values (Chlorine, pH, Silver, TDS) are filled.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double chlorine = Double.parseDouble(chl);
            double phVal = Double.parseDouble(ph);
            double silver = Double.parseDouble(sil);
            double tdsVal = Double.parseDouble(tds);

            WaterData data = new WaterData(chlorine, phVal, silver, tdsVal, System.currentTimeMillis());

            // Check safety for user feedback
            boolean isSafe = (chlorine >= 1.0 && chlorine <= 12.0) &&
                             (phVal >= 1.0 && phVal <= 14.0) &&
                             (silver >= 1.0 && silver <= 10.0) &&
                             (tdsVal <= 500.0);

            // Push to Firebase Backend (Works in both Online and Offline manual modes)
            String id = database.push().getKey();
            if (id != null) {
                database.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, isSafe ? "✅ Record Saved: Safe" : "🚨 Record Saved: Unsafe", Toast.LENGTH_LONG).show();
                        
                        // Clear fields only if we are in Manual mode
                        if (!switchAutoFetch.isChecked()) {
                            chlorineEditText.setText("");
                            phEditText.setText("");
                            silverEditText.setText("");
                            tdsEditText.setText("");
                        }
                        
                        startActivity(new Intent(this, VisualizeActivity.class));
                    } else {
                        Toast.makeText(this, "Backend Sync Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid Data Format", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            })
            .setNegativeButton("Stay", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoFetchHandler.removeCallbacks(autoFetchRunnable);
    }
}