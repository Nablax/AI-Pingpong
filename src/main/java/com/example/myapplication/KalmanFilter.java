package com.example.myapplication;

/**
 * Created by timothy on 17-8-30.
 */

import java.util.ArrayList;
import java.util.List;

public class KalmanFilter {

    private final double Q = 0.000001;//  预测值协方差（参数可调）
    private final double R = 0.001; //  测量值协方差（参数可调）
    private int length;
    private double z[]; //  data
    private double xhat[];
    private double xhatminus[];
    private double P[];
    private double Pminus[];
    private double K[];
    private int pointNum;

    private List<Double> kalmanFilter(List<Double> dataList) {
        //  输入一组其中一个变量的测量数据
        //  卡尔曼滤波五个公式
        //  目前参数需要调整，调整完成后请在滤波之后再调用BasicAnalysis下的其他函数
        length = dataList.size();
        z = new double[length];
        xhat = new double[length];
        xhatminus = new double[length];
        P = new double[length];
        Pminus = new double[length];
        K = new double[length];
        xhat[0] = 0;
        P[0] = 1.0; //  初始化最优角度估计协方差（参数可调）

        for (int i = 0; i < length; i++) {
            z[i] = dataList.get(i);
        }

        if (dataList.size() < 2) {
            return dataList;
        }

        for (int k = 1; k < length; k++) {
            //  X(k|k - 1) = AX(k - 1|k - 1) + BU(k) + W(k),A = 1,BU(k) = 0
            xhatminus[k] = xhat[k - 1];

            //  P(k|k - 1) = AP(k -1|k - 1)A' + Q(k) ,A = 1
            Pminus[k] = P[k - 1] + Q;

            //  Kg(k)=P(k|k - 1)H'/[HP(k|k - 1)H' + R],H = 1
            K[k] = Pminus[k] / (Pminus[k] + R);

            //  X(k|k) = X(k|k - 1) + Kg(k)[Z(k) - HX(k|k - 1)], H = 1
            xhat[k] = xhatminus[k] + K[k] * (z[k] - xhatminus[k]);

            //  P(k|k) = (1 - Kg(k)H)P(k|k - 1), H = 1
            P[k] = (1 - K[k]) * Pminus[k];
        }

        for (int i = 0; i < length; i++) {
            dataList.set(i, xhat[i]);
        }

        dataList.remove(0);
        return dataList;
    }

    public List<Double[]> kalc(List<Double[]> dataArray) {
        //  循环对整个轨迹滤波
        BasicAnalysis basic = new BasicAnalysis();
        List<Integer> list = basic.noUse(dataArray);
        List<Double> dataList = new ArrayList<>();
        pointNum = list.get(1);

        for (int i = 9; i < 15; i++) {
            for (int j = 0; j < pointNum; j++) {
                dataList.add(dataArray.get(j)[i]);
            }

            kalmanFilter(dataList);

            for (int j = 0; j < pointNum; j++) {
                dataArray.get(j)[i] = dataList.get(j);
            }

            dataList.clear();
        }

        return dataArray;
    }
}

