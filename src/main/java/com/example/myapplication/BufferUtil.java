package com.example.myapplication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by 叶明林 on 2017/8/24.
 */

public class BufferUtil {
    public static FloatBuffer floatToBuffer(float[] a) {
        // 先初始化buffer，数组的长度*4，因为一个float占4个字节
        ByteBuffer mbb = ByteBuffer.allocateDirect(a.length * 4);
        // 数组排序用nativeOrder
        mbb.order(ByteOrder.nativeOrder());
        FloatBuffer mBuffer = mbb.asFloatBuffer();
        mBuffer.put(a);
        mBuffer.position(0);
        return mBuffer;
    }

    public static ShortBuffer shortToBuffer(short[] a) {
        ByteBuffer mbb = ByteBuffer.allocateDirect(a.length * 2);
        // 数组排序用nativeOrder
        mbb.order(ByteOrder.nativeOrder());
        ShortBuffer mBuffer = mbb.asShortBuffer();
        mBuffer.put(a);
        mBuffer.position(0);
        return mBuffer;
    }
}
