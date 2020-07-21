package com.example.myapplication;

import android.graphics.drawable.Drawable;

/**
 * Created by 叶明林 on 2017/9/30.
 */

public class UserDataStruct {

    public UserDataStruct(String userName, String position, String signature, int praiseNumber, int hitNumber, double sportTime) {
        this.userName = userName;
        this.position = position;
        this.signature = signature;
        this.hitNumber = hitNumber;
        this.praiseNumber = praiseNumber;
        this.sportTime = sportTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Drawable getHeadPicture() {
        return headPicture;
    }

    public void setHeadPicture(Drawable headPicture) {
        this.headPicture = headPicture;
    }

    public int getPraiseNumber() {
        return praiseNumber;
    }

    public void setPraiseNumber(int praiseNumber) {
        this.praiseNumber = praiseNumber;
    }

    public double getSportTime() {
        return sportTime;
    }

    public void setSportTime(double sportTime) {
        this.sportTime = sportTime;
    }

    public int getHitNumber() {
        return hitNumber;
    }

    public void setHitNumber(int hitNumber) {
        this.hitNumber = hitNumber;
    }
    private String userName;
    private String position;
    private String signature;
    private Drawable headPicture;
    private int hitNumber;
    private int praiseNumber;
    private double sportTime;
}
