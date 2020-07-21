package com.example.myapplication;

import android.graphics.Rect;

import static com.example.myapplication.Main2Activity.iconScaling;

/**
 * Created by SKY on 2017/9/19.
 */

public class IconCompress {
    public static void IconDealing(Rect rectTmp){
        rectTmp.left = (int)((rectTmp.left)/iconScaling);
        rectTmp.right = (int)((rectTmp.right)/iconScaling);
        rectTmp.top = (int)((rectTmp.top)/iconScaling);
        rectTmp.bottom = (int)((rectTmp.bottom)/iconScaling);
    }
}
