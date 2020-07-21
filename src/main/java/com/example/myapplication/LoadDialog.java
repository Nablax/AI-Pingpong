package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by 叶明林 on 2017/9/7.
 */

public class LoadDialog extends Dialog {
    private Context context;
    private static LoadDialog dialog;
    private ImageView ivProgress;
    public LoadDialog(@NonNull Context context) {
        super(context);
        this.context=context;
    }

    public LoadDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context=context;
    }

    protected LoadDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context=context;
    }
    public static LoadDialog showDialog(Context context){
        dialog = new LoadDialog(context, R.style.MyDialog);//dialog样式
        dialog.setContentView(R.layout.layout_dialog);//dialog布局文件
        dialog.setCanceledOnTouchOutside(false);//点击外部不允许关闭dialog
        return dialog;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && dialog != null){
            ivProgress = (ImageView) dialog.findViewById(R.id.ivProgress);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.dialog_progress_anim);
            ivProgress.startAnimation(animation);
        }
    }
}
