package com.example.water_flow;

public class WaterData {
    public double chlorine;
    public double ph;
    public double silver;
    public double tds;
    public long timestamp;

    public WaterData() {} // Default constructor for Firebase

    public WaterData(double chlorine, double ph, double silver, double tds, long timestamp) {
        this.chlorine = chlorine;
        this.ph = ph;
        this.silver = silver;
        this.tds = tds;
        this.timestamp = timestamp;
    }
}