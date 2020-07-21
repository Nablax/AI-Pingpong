package com.example.myapplication;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by timothy on 17-8-19.
 */

public class BasicAnalysis {
    //  击球时刻不确定，故掉球时间目前无法准确获得
    public int countTraceNum = 0;
    private int pointNum;
    private int whenBit;
    private int cordirect = 1;
    private double yawCos;
    private double yawSin;
    private double pitchCos;
    private double pitchSin;
    private double rollCos;
    private double rollSin;
    private int direct;
    private double vel;
    private double sumVel = 0;
    private double aveVel;
    private double maxVel = 0;
    private double acc;
    private double gravityAcc = -9.886;
    private double batAveMass = 0.175;
    private double force;
    private double sumForce = 0;
    private double aveForce = 0;

    public List<Integer> noUse(List<Double[]> dataArray) {
        //  没有任何卵用的函数，强行设定是否击球，强行认为在三分之二动作时为击球时刻
        List<Integer> list = new ArrayList<>();
        if (dataArray.get(0).length == 15) {
            pointNum = dataArray.size();
            whenBit = (int) Math.floor(pointNum * 2 / 3);
            list.add(whenBit);
            list.add(pointNum);
        } else {
            list.add(-1);
            list.add(1);
        }

        return list;
    }

    /*private int correctionDirect(List<Double[]> dataArray) {
        if (dataArray.get(0)[13] < 0) {
            cordirect = 1;
        } else {
            cordirect = -1;
        }

        return cordirect;
    }*/

    private double[][] countMatrix(double yaw, double pitch, double roll) {
        //  输入航向角、俯仰角和横滚角，输出姿态矩阵
        double attitudeArray[][] = new double[3][3];
        yawCos = Math.cos(yaw);
        yawSin = Math.sin(yaw);
        pitchCos = Math.cos(pitch);
        pitchSin = Math.sin(pitch);
        rollCos = Math.cos(roll);
        rollSin = Math.sin(roll);
        attitudeArray[0][0] = yawCos * rollCos - yawSin * pitchSin * rollSin;
        attitudeArray[0][1] = -yawSin * pitchCos;
        attitudeArray[0][2] = yawCos * rollSin + yawSin * pitchSin * rollCos;
        attitudeArray[1][0] = yawCos * pitchSin * rollSin + yawSin * rollCos;
        attitudeArray[1][1] = yawCos * pitchCos;
        attitudeArray[1][2] = yawSin * rollSin - yawCos * pitchSin * rollCos;
        attitudeArray[2][0] = -pitchCos * rollSin;
        attitudeArray[2][1] = pitchSin;
        attitudeArray[2][2] = pitchCos * rollCos;
        return attitudeArray;
    }


    public int juggleDirection(List<Double[]> dataArray) {
        //  判断颠球方向，0为未击球，1为正手，-1为反手
        List<Integer> list = noUse(dataArray);
        whenBit = list.get(0);
        pointNum = list.get(1);
        //cordirect = correctionDirect(dataArray);

        for (int i = 0; i < pointNum; i++) {
            if (i == whenBit) {
                if (dataArray.get(i)[13] > 0) {
                    direct = -cordirect;
                    break;
                } else {
                    direct = cordirect;
                    break;
                }
            } else {
                direct = 0;
            }
        }

        return direct;
    }

    public List<Integer> dropTime() {
        return null;
    }

    public double bitVel(List<Double[]> dataArray) {
        //  返回每个轨迹的平均速度
        List<Integer> list = noUse(dataArray);
        pointNum = list.get(1);

        for (int i = 0; i < pointNum; i++) {
            vel = Math.sqrt(Math.pow(dataArray.get(i)[3] / 1000, 2) + Math.pow(dataArray.get(i)[4] / 1000 - gravityAcc * cordirect, 2) + Math.pow(dataArray.get(i)[5] / 1000, 2));
            sumVel += vel;
        }

        aveVel = sumVel / pointNum;
        return aveVel;
    }

    public double bitForce(List<Double[]> dataArray) {
        //  返回每个轨迹的平均力量
        List<Integer> list = noUse(dataArray);
        pointNum = list.get(1);
        //cordirect = correctionDirect(dataArray);
        for (int i = 0; i < pointNum; i++) {
            acc = Math.sqrt(Math.pow(dataArray.get(i)[12] / 1000, 2) + Math.pow(dataArray.get(i)[13] / 1000 - gravityAcc * cordirect, 2) + Math.pow(dataArray.get(i)[14] / 1000, 2));
            force = acc * batAveMass;  //  力的单位是N
            sumForce += force;
        }

        aveForce = sumForce / pointNum;
        return aveForce;
    }
}