package com.example.water_flow;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<WaterData> historyList;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<WaterData> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        WaterData data = historyList.get(position);
        
        holder.textViewDate.setText(dateTimeFormat.format(new Date(data.timestamp)));
        holder.historyPH.setText(String.valueOf(data.ph));
        holder.historyChlorine.setText(data.chlorine + " ppm");
        holder.historySilver.setText(data.silver + " ppm");
        holder.historyTDS.setText(data.tds + " ppm");

        // Check safety
        boolean isSafe = (data.ph >= 1.0 && data.ph <= 14.0) &&
                         (data.chlorine >= 1.0 && data.chlorine <= 12.0) &&
                         (data.silver >= 1.0 && data.silver <= 10.0) &&
                         (data.tds <= 500.0);

        if (isSafe) {
            holder.textViewStatus.setText("SAFE");
            holder.textViewStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32"))); // Dark Green
        } else {
            holder.textViewStatus.setText("UNSAFE");
            holder.textViewStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C62828"))); // Dark Red
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewStatus, historyPH, historyChlorine, historySilver, historyTDS;

        HistoryViewHolder(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewHistoryDate);
            textViewStatus = itemView.findViewById(R.id.textViewHistoryStatus);
            historyPH = itemView.findViewById(R.id.historyPH);
            historyChlorine = itemView.findViewById(R.id.historyChlorine);
            historySilver = itemView.findViewById(R.id.historySilver);
            historyTDS = itemView.findViewById(R.id.historyTDS);
        }
    }
}