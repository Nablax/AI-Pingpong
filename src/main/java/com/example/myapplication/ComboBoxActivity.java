package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 叶明林 on 2017/8/12.
 */

public class ComboBoxActivity extends Activity {
    //List<Button> buttonList=new ArrayList<Button>();
    private String defaultString="No Data";
    public void myListener(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        ScrollView scrollView =new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#80000000"));
        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        List<String> stringList=new ArrayList<String>();
        //传入数据
        Intent intent=getIntent();
        String []keep=intent.getStringArrayExtra("paths");
        if(keep.length==0)
        {
            TextView defaultTextView = new TextView(this);
            defaultTextView.setGravity(Gravity.CENTER);
            defaultTextView.setTextSize(20);
            defaultTextView.setTextColor(Color.parseColor("#ffffff"));
            defaultTextView.setText(this.defaultString);
            linearLayout.setBackgroundColor(Color.parseColor("#80000000"));
            linearLayout.addView(defaultTextView);
            linearLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }
            );
            setContentView(linearLayout);
            return ;
        }
        String rootPath=keep[0];
        for(int i=1;i<keep.length;i++)
            stringList.add(keep[0]);
        for(int i=0;i<stringList.size();i++)
        {
            final Button button=new Button(this);
            setAtrributeForButton(button);
            final String message=stringList.get(i);
            button.setText(message);
            button.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent();
                            intent.putExtra("String",message);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
            );
            //this.buttonList.add(button);
            linearLayout.addView(button);
        }
        scrollView.addView(linearLayout);
        scrollView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
        setContentView(scrollView);
    }

    private void setAtrributeForButton(Button button)
    {
        Resources resources =this.getResources();
        Drawable drawable=resources.getDrawable(R.drawable.btn_selector_mid);
        button.setBackground(drawable);
        button.setHeight((int)dip2px(50));
        button.setWidth((int)dip2px(288));
        button.setTextColor(Color.parseColor("#EEEEEE"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, (int)dip2px(41));
        button.setLayoutParams(params);

        //style="@style/Widget.AppCompat.Button.Colored"
    }
    private float dip2px(float dipValue)
    {
        return (dipValue * this.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }  
}
