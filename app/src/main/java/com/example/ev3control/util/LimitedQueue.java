package com.example.ev3control.util;

import java.util.ArrayList;

/**
 * Created by Teilnehmer on 13.12.2017.
 */

public class LimitedQueue extends ArrayList<Float> {
    private int capacity;

    public LimitedQueue(int capacity){
        this.capacity = capacity;
    }

    @Override
    public boolean add(Float val) {
        if(this.size() >= capacity){
            this.remove(0);
        }
        return super.add(val);
    }

    public float getAVG(){
        if(this.isEmpty()){
            return 0f;
        }
        float sum = 0;
        for(float f : this){
            sum += f;
        }
        return sum / this.size();
    }
}
