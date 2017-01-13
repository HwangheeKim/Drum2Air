package com.example.q.drum2air;

import java.util.ArrayList;

public class PreDataSet {
    public static int PRESET_SIZE = 20;
    public static int OFFSET_SIZE = 0;
    ArrayList<AccelData> accelDatas;
    ArrayList<OrientData> orientDatas;
    int type;

    public PreDataSet(ArrayList<AccelData> accelHistory, ArrayList<OrientData> orientHistory, int type) {
        accelDatas = new ArrayList<>(accelHistory.subList(OFFSET_SIZE * 2, (OFFSET_SIZE + PRESET_SIZE) * 2));
        orientDatas = new ArrayList<>(orientHistory.subList(OFFSET_SIZE, OFFSET_SIZE + PRESET_SIZE));
        this.type = type;
    }

    public double distance(ArrayList<AccelData> accelHistory, ArrayList<OrientData> orientHistory) {
        double distance = 0;
        for (int i = 0; i < PRESET_SIZE * 2; i++) {
            if (i < PRESET_SIZE) {
                distance += Math.pow(angularDiff(orientHistory.get(i + OFFSET_SIZE).getAzimuth(), orientDatas.get(i).getAzimuth()), 2);
                distance += Math.pow(angularDiff(orientHistory.get(i + OFFSET_SIZE).getPitch(), orientDatas.get(i).getPitch()), 2);
                distance += Math.pow((orientHistory.get(i + OFFSET_SIZE).getRoll() - orientDatas.get(i).getRoll()), 2);
            }

            distance += Math.pow((accelHistory.get(i + OFFSET_SIZE * 2).getX() - accelDatas.get(i).getX()), 2);
            distance += Math.pow((accelHistory.get(i + OFFSET_SIZE * 2).getY() - accelDatas.get(i).getY()), 2);
            distance += Math.pow((accelHistory.get(i + OFFSET_SIZE * 2).getZ() - accelDatas.get(i).getZ()), 2);
        }
        return distance;
    }

    private double angularDiff(double a, double b) {
        double diff = (a > b) ? (a - b) : (b - a);
        if(diff > 180) return 360 - diff;
        return diff;
    }
}
