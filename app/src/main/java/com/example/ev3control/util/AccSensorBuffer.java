package com.example.ev3control.util;

/**
 * Created by Teilnehmer on 13.12.2017.
 */

public class AccSensorBuffer {

    private static AccSensorBuffer instance = new AccSensorBuffer();

    private LimitedQueue xAcc = new LimitedQueue(25);
    private LimitedQueue yAcc = new LimitedQueue(25);
    private LimitedQueue zAcc = new LimitedQueue(25);

    private AccSensorBuffer(){

    }

    public static AccSensorBuffer getInstance(){
        return instance;
    }

    public void addValue(float[] val){
        xAcc.add(val[0]);
        yAcc.add(val[1]);
        zAcc.add(val[2]);
    }

    public float[] getAVG(){
        float[] result = new float[3];
        result[0] = xAcc.getAVG();
        result[1] = yAcc.getAVG();
        result[2] = zAcc.getAVG();

        return result;
    }




}