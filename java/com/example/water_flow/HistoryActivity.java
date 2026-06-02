package com.example.water_flow;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private HistoryAdapter historyAdapter;
    private List<WaterData> historyList;
    private TextView textViewTotalRecords;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        textViewTotalRecords = findViewById(R.id.textViewTotalRecords);
        RecyclerView recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        Button buttonBackToMain = findViewById(R.id.buttonBackToMain);

        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList);
        
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);

        // Path: water_data
        database = FirebaseDatabase.getInstance("https://watersafetyapp-default-rtdb.firebaseio.com").getReference("water_data");

        fetchAllHistory();

        buttonBackToMain.setOnClickListener(v -> finish());
    }

    private void fetchAllHistory() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    WaterData data = dataSnapshot.getValue(WaterData.class);
                    if (data != null) {
                        historyList.add(data);
                    }
                }
                
                // Sort by timestamp descending (newest first)
                Collections.sort(historyList, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));
                
                if (textViewTotalRecords != null) {
                    textViewTotalRecords.setText(getString(R.string.label_total_records, historyList.size()));
                }
                historyAdapter.notifyDataSetChanged();
                
                if (historyList.isEmpty()) {
                    Toast.makeText(HistoryActivity.this, "No history records found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HistoryActivity", "Database error: " + error.getMessage());
                Toast.makeText(HistoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}