package com.example.myapplication;

/**
 * Package com.hc.opengl
 * Created by HuaChao on 2016/7/28.
 */
public class Point {
    public float x;
    public float y;
    public float z;
    public float mark;
    //x 俯仰角，y横向角,z 航向角
    public float pitch;//俯仰角 绕x轴
    public float roll;//横滚角 绕y轴
    public float yaw;//航向角 绕z轴
    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Point(float x, float y, float z, float p,float r,float ya) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.roll=r;
        this.pitch =p;
        this.yaw=ya;
    }
    public Point(){};
    public double distanceBetwwenPoints(Point point)
    {
        return Math.sqrt((double)((point.z-this.z)*(point.z-this.z)
                +(point.y-this.y)*(point.y-this.y)
                +(point.x-this.x)*(point.x-this.x)));
    }
}
