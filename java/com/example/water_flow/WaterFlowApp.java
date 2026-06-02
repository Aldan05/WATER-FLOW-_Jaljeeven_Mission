package com.example.water_flow;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class WaterFlowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable offline persistence for the entire app
        FirebaseDatabase.getInstance("https://watersafetyapp-default-rtdb.firebaseio.com")
                .setPersistenceEnabled(true);
    }
}