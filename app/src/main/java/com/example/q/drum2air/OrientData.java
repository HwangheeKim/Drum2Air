package com.example.q.drum2air;

public class OrientData {
    private long timestamp;
    private double azimuth;
    private double pitch;
    private double roll;

    public OrientData(long timestamp, double azimuth, double pitch, double roll) {
        this.timestamp = timestamp;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }
}
