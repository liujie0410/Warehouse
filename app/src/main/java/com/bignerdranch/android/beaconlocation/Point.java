package com.bignerdranch.android.beaconlocation;

import android.support.annotation.NonNull;

public class Point{
    private int x;
    private int y;
    private int rssi = 0;
    Point(int x,int y){
        this.x = x;
        this.y = y;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

  //  public void setX(int x) { this.x = x; }

  //  public void setY(int y) {this.y = y;}

}
