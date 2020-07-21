package com.example.myapplication;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 叶明林 on 2017/8/24.
 */

public class OpenGLView extends GLSurfaceView
{
    private OpenGLRenderer openGLRenderer;
    public OpenGLView(Context context) {
        super(context);
    }
    public OpenGLView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        this.openGLRenderer=new OpenGLRenderer(context, new RendererCallBack() {
            private Timer timer ;
            @Override
            public void onDataReset() {
            }

            @Override
            public void onDataInitComplete() {
                if(this.timer==null)
                {
                    timer =new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            requestRender();
                        }
                    },1000/30,1000/30);//,1000,1000
                }
            }

            @Override
            public void onDrawComplete() {
            }
        });
        this.setRenderer(openGLRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        this.setClickable(true);
        this.setOnTouchListener(
                new OnTouchListener() {
                    private int mode=0;
                    private float oldDist=0;
                    private float xRecord=0;
                    private float yRecord=0;
                    private final float incremental=(float) 1;      //距离衰减值
                    private float spacing(MotionEvent event)
                    {
                        float x = event.getX(0) - event.getX(1);
                        float y = event.getY(0) - event.getY(1);
                        return (float)Math.sqrt(x * x + y * y);
                    }
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                mode = 1;
                                xRecord=event.getX();
                                yRecord=event.getY();
                                break;
                            case MotionEvent.ACTION_UP:
                                mode = 0;
                                float distanceX=event.getX()-xRecord;
                                float distanceY=event.getY()-yRecord;
                                if(Math.abs(distanceX)>Math.abs(distanceY))
                                {
                                    OpenGLView.this.openGLRenderer.moveCameraRotation(distanceX);
                                    OpenGLView.this.requestRender();
                                    float flag=distanceX<0?-1:1;
                                    float sum=0;
                                    while(sum*sum<distanceX*distanceX)
                                    {
                                        OpenGLView.this.openGLRenderer.moveCameraRotation(incremental*flag);
                                        sum+=incremental*flag;
                                        OpenGLView.this.requestRender();
                                    }
                                }
                                else
                                {
                                    OpenGLView.this.openGLRenderer.moveCameraUpDown(distanceY);
                                    OpenGLView.this.requestRender();
                                }
                                break;
                            case MotionEvent.ACTION_POINTER_UP:
                                mode -= 1;
                                break;
                            case MotionEvent.ACTION_POINTER_DOWN:
                                oldDist = spacing(event);
                                mode += 1;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (mode >= 2)
                                {
                                    float newDist = spacing(event);
                                    if(newDist!=oldDist)
                                    {
                                        OpenGLView.this.openGLRenderer.scaling(oldDist/newDist);
                                        OpenGLView.this.requestRender();
                                    }
                                    oldDist=newDist;
                                    break;
                                }
                        }
                        return false;
                    }
                }
        );
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public void setData(List<Double> doubles)
    {
        this.openGLRenderer.setDoubles(doubles);
    }
//    public OpenGLView(Context context,AttributeSet attrs,int defStyle)
//    {
//        super(context,attrs,defStyle);
//
//    }
}
