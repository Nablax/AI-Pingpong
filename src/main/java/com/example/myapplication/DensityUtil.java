package com.example.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.myapplication.Main2Activity.iconScaling;

public class DensityUtil {
    public static final int PX=0;
    public static final int SP=1;
    public static final int DP=2;
    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density; 
        return  (dpValue * scale + 0.5f);
    }
    public static float px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (pxValue / scale + 0.5f);
    }
    public static float px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (pxValue / fontScale + 0.5f);
    }
    public static float sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return  (spValue * fontScale + 0.5f);
    }
    public static Drawable scaleImage(Context context,int id,float height,float width,int mode)
    {
        Resources resources=context.getResources();
        float imageWidth=(float)(width/iconScaling);
        float imageHeight=(float)(height/iconScaling);
        switch (mode)
        {
            case SP:
                imageWidth=sp2px(context,imageWidth);
                imageHeight=sp2px(context,imageHeight);
                break;
            case DP:
                imageHeight=dip2px(context,imageHeight);
                imageWidth=dip2px(context,imageWidth);
                break;
        }
        BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
        bfoOptions.inScaled=false;
        Bitmap bitmap= BitmapFactory.decodeResource(resources,id);
        int bitmapHeight=bitmap.getHeight();
        int bitmapWidth=bitmap.getWidth();
        float numx = imageWidth / (float)bitmapWidth;
        float numy = imageHeight / (float)bitmapHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(numx, numy);
        // 缩放后的Bitmap对象
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
                bitmapHeight, matrix, true);
        //System.out.println(px2dip(context,resizeBitmap.getHeight())+" "+px2dip(context,resizeBitmap.getWidth()));
        Drawable drawable = new BitmapDrawable(resizeBitmap);
        return drawable;
    }
    public static String floatPrecision(int precision,float value)
    {
        String parse=".";
        for(int i=0;i<precision;i++)
            parse+="0";
        DecimalFormat decimalFormat=new DecimalFormat(parse);
        String ans=(precision==0?(int)value+"":decimalFormat.format(value));
        if(ans.length()!=0&&ans.charAt(0)=='.')
            ans="0"+ans;
        return ans;
    }
    public static int[] colorToRGBFromInt(int color)
    {
        if(color<0x01000000)
        {
            int ans[]=new int[3];
            ans[0]=(color&0x00ff0000)>>16;
            ans[1]=(color&0x0000ff00)>>8;
            ans[2]=(color&0x000000ff);
            return ans;
        }
        int ans[]=new int[4];
        ans[0]=(color&0xff000000)>>24;
        ans[1]=(color&0x00ff0000)>>16;
        ans[2]=(color&0x0000ff00)>>8;
        ans[3]=(color&0x000000ff);
        return ans;
    }
    //列表转换,nums为一维长度
    public static List<Double[]> convertDoublesToDoubleArray(List<Double> doubles,int nums)
    {
        List<Double[]>ans=new ArrayList<Double[]>();
        for(int i=0,len=doubles.size();i<len;i+=nums)
        {
            Double[] keep=new Double[nums];
            for(int j=i,index=0;j<i+nums;j++)
                keep[index++]=doubles.get(j);
            ans.add(keep);
        }
        return ans;
    }
    //返回文件后缀
    public static String getFilePostfix(@NonNull String fileName)
    {
        String []strings=fileName.split("\\.");
        if(strings.length!=2)
            return null;
        return strings[1];
    }
    //返回文件名（不带后缀）
    public static String getFileNameWithoutPostfix(@NonNull String fileName)
    {
        String []strings=fileName.split("\\.");
        if(strings.length!=2)
            return null;
        return strings[0];
    }

    public static int byte4ToInt(byte[] bytes, int offset) {
        int b3 = bytes[offset + 3] & 0xFF;
        int b2 = bytes[offset + 2] & 0xFF;
        int b1 = bytes[offset + 1] & 0xFF;
        int b0 = bytes[offset + 0] & 0xFF;
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public static short byte2ToShort(byte[] bytes, int offset) {
        int b1 = bytes[offset + 1] & 0xFF;
        int b0 = bytes[offset + 0] & 0xFF;
        return (short) ((b1 << 8) | b0);
    }

    public static float byte4ToFloat(byte[] bytes, int offset) {

        return Float.intBitsToFloat(byte4ToInt(bytes, offset));
    }
    //读取assets文件夹下txt文件的内容
    public static String readAssetsTxt(Context context,String fileName){
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName+".txt");
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            // Finally stick the string into the text view.
            return text;
        } catch (IOException e) {
            // Should never happen!
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }
    //求Z=0平面上 sourcePoint 绕 centerPoint旋转后的点
    public static Point pointRote(Point sourcePoint,Point centerPoint,float degrees)
    {
        double radians=Math.PI*degrees/180;
        double x=(sourcePoint.x-centerPoint.x)*Math.cos(radians)
                    +(sourcePoint.y-centerPoint.y)*Math.sin(radians)+centerPoint.x;
        double y=(sourcePoint.y-centerPoint.y)*Math.cos(radians)
                    -(sourcePoint.x-centerPoint.x)*Math.sin(radians)+centerPoint.y;
        return new Point((float)x,(float)y,0);
    }
    //bitmap放缩
    public static Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
        if(bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = w;
        int newHeight = h;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }

    public static float getRandomFloat(float max,float min)
    {
        Random random = new Random();
        return min+(max-min)*random.nextFloat();
    }

    public static int getRandomInt(int max,int min)
    {
        Random random  = new Random();
        return random.nextInt(max)%(max-min+1)+min;
    }
}