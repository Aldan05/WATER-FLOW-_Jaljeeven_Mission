package com.example.water_flow;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VisualizeActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;
    private ScatterChart scatterChart;
    private RadarChart radarChart;
    private TextView textViewAdvice, textViewStatusTitle;
    private Button buttonBack;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize);

        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        scatterChart = findViewById(R.id.scatterChart);
        radarChart = findViewById(R.id.radarChart);
        
        textViewAdvice = findViewById(R.id.textViewAdvice);
        textViewStatusTitle = findViewById(R.id.textViewStatusTitle);
        buttonBack = findViewById(R.id.buttonBack);

        database = FirebaseDatabase.getInstance("https://watersafetyapp-default-rtdb.firebaseio.com").getReference("water_data");

        fetchDataAndPopulateCharts();

        buttonBack.setOnClickListener(v -> finish());
    }

    private void fetchDataAndPopulateCharts() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Entry> chlorineEntries = new ArrayList<>();
                List<Entry> phEntries = new ArrayList<>();
                List<Entry> silverEntries = new ArrayList<>();
                List<Entry> tdsEntries = new ArrayList<>();

                int safeCount = 0;
                int unsafeCount = 0;
                int recordIndex = 0;
                WaterData lastData = null;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    WaterData data = dataSnapshot.getValue(WaterData.class);
                    if (data != null) {
                        chlorineEntries.add(new Entry(recordIndex, (float) data.chlorine));
                        phEntries.add(new Entry(recordIndex, (float) data.ph));
                        silverEntries.add(new Entry(recordIndex, (float) data.silver));
                        tdsEntries.add(new Entry(recordIndex, (float) data.tds));
                        
                        boolean isSafe = (data.ph >= 1.0 && data.ph <= 14.0) &&
                                         (data.chlorine >= 1.0 && data.chlorine <= 12.0) &&
                                         (data.silver >= 1.0 && data.silver <= 10.0) &&
                                         (data.tds <= 500.0);
                        
                        if (isSafe) safeCount++;
                        else unsafeCount++;

                        lastData = data;
                        recordIndex++;
                    }
                }

                if (recordIndex > 0) {
                    setupLineChart(chlorineEntries, phEntries, silverEntries, tdsEntries);
                    setupPieChart(safeCount, unsafeCount);
                    
                    List<BarEntry> latestBarEntries = new ArrayList<>();
                    latestBarEntries.add(new BarEntry(0, (float) lastData.chlorine));
                    latestBarEntries.add(new BarEntry(1, (float) lastData.ph));
                    latestBarEntries.add(new BarEntry(2, (float) lastData.silver));
                    latestBarEntries.add(new BarEntry(3, (float) lastData.tds));
                    setupBarChart(latestBarEntries);

                    setupScatterChart(phEntries);
                    
                    List<RadarEntry> radarEntries = new ArrayList<>();
                    radarEntries.add(new RadarEntry((float) lastData.chlorine));
                    radarEntries.add(new RadarEntry((float) lastData.ph));
                    radarEntries.add(new RadarEntry((float) lastData.silver));
                    radarEntries.add(new RadarEntry((float) (lastData.tds / 10.0))); // Scale TDS for radar visibility
                    setupRadarChart(radarEntries);
                    
                    generateHealthAdvice(lastData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupScatterChart(List<Entry> phEntries) {
        ScatterDataSet set = new ScatterDataSet(phEntries, "pH Consistency");
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set.setScatterShapeSize(10f);

        ScatterData data = new ScatterData(set);
        scatterChart.setData(data);
        scatterChart.getDescription().setEnabled(false);
        scatterChart.animateX(1000);
        scatterChart.invalidate();
    }

    private void setupRadarChart(List<RadarEntry> entries) {
        RadarDataSet set = new RadarDataSet(entries, "Latest Parameter Spread (TDS scaled /10)");
        set.setColor(Color.MAGENTA);
        set.setFillColor(Color.MAGENTA);
        set.setDrawFilled(true);
        set.setFillAlpha(100);
        set.setLineWidth(2f);

        RadarData data = new RadarData(set);
        radarChart.setData(data);
        radarChart.getDescription().setEnabled(false);
        radarChart.animateXY(1000, 1000);
        radarChart.invalidate();
    }

    private void generateHealthAdvice(WaterData data) {
        StringBuilder advice = new StringBuilder();
        boolean isSafe = true;

        // Detailed pH Advice
        if (data.ph < 1.0) {
            advice.append("🚨 CRITICAL pH ALERT: Level is too low (Acidic).\n")
                  .append("• Risk: Severe corrosion of plumbing and potential for heavy metal leaching.\n")
                  .append("• Prevention: Install a neutralizing filter with calcite or magnesium oxide.\n\n");
            isSafe = false;
        } else if (data.ph > 14.0) {
            advice.append("🚨 CRITICAL pH ALERT: Level is too high (Alkaline).\n")
                  .append("• Risk: Scaling of pipes and potential skin/eye irritation.\n")
                  .append("• Prevention: Use a water softener or add a food-grade pH reducer.\n\n");
            isSafe = false;
        }

        // Detailed Chlorine Advice
        if (data.chlorine < 1.0) {
            advice.append("⚠️ LOW CHLORINE WARNING:\n")
                  .append("• Risk: Insufficient disinfection allows bacteria like E. coli to thrive.\n")
                  .append("• How to drink safely: Boil water for at least 1 minute or use UV sterilization.\n\n");
            isSafe = false;
        } else if (data.chlorine > 12.0) {
            advice.append("⚠️ HIGH CHLORINE WARNING:\n")
                  .append("• Risk: Strong odor and potential respiratory or stomach irritation.\n")
                  .append("• Prevention: Use an activated carbon filter or let water stand in an open container for 24 hours.\n\n");
            isSafe = false;
        }

        // Detailed Silver Advice
        if (data.silver < 1.0) {
            advice.append("ℹ️ LOW SILVER NOTE: Secondary disinfection is minimal.\n")
                  .append("• Safe to drink? Yes, if Chlorine and pH are okay.\n\n");
        } else if (data.silver > 10.0) {
            advice.append("🚨 HIGH SILVER ALERT:\n")
                  .append("• Risk: Long-term exposure leads to Argyria (irreversible blue skin tint).\n")
                  .append("• Prevention: Switch to non-silver based filtration systems.\n\n");
            isSafe = false;
        }

        // Detailed TDS Advice
        if (data.tds > 500.0) {
            advice.append("⚠️ HIGH TDS WARNING (").append(data.tds).append(" ppm):\n")
                  .append("• Risk: Poor taste, scale buildup, and high mineral content.\n")
                  .append("• Prevention: Use Reverse Osmosis (RO) or distillation treatment.\n\n");
            isSafe = false;
        }

        // General Safe Drinking Tips
        advice.append("💡 SAFE DRINKING TIPS:\n")
              .append("1. Always clean your storage tanks every 6 months.\n")
              .append("2. If in doubt, boil water before consumption.\n")
              .append("3. Monitor these 4 parameters regularly to ensure consistent safety.");

        if (isSafe) {
            textViewStatusTitle.setText("✅ Safe to Drink");
            textViewStatusTitle.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            textViewStatusTitle.setText("🚨 Health Disclaimer");
            textViewStatusTitle.setTextColor(Color.RED);
        }

        textViewAdvice.setText(advice.toString());
    }

    private void setupLineChart(List<Entry> chlorine, List<Entry> ph, List<Entry> silver, List<Entry> tds) {
        LineDataSet set1 = new LineDataSet(chlorine, "Chlorine");
        set1.setColor(Color.BLUE);
        LineDataSet set2 = new LineDataSet(ph, "pH");
        set2.setColor(Color.parseColor("#4CAF50"));
        LineDataSet set3 = new LineDataSet(silver, "Silver");
        set3.setColor(Color.RED);
        LineDataSet set4 = new LineDataSet(tds, "TDS");
        set4.setColor(Color.BLACK);
        
        lineChart.setData(new LineData(set1, set2, set3, set4));
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void setupPieChart(int safe, int unsafe) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(safe, "Safe"));
        entries.add(new PieEntry(unsafe, "Unsafe"));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.GREEN, Color.RED});
        pieChart.setData(new PieData(dataSet));
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }

    private void setupBarChart(List<BarEntry> entries) {
        BarDataSet set = new BarDataSet(entries, "Latest Levels (Chl, pH, Sil, TDS)");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        barChart.setData(new BarData(set));
        barChart.animateY(1000);
        barChart.invalidate();
    }
}