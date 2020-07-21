package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * Created by 叶明林 on 2017/8/26.
 */
interface ClientResultCallback
{
    public void onClientResult(String message);
}
public class AIPingPongClientUsage {
    private Handler uiHandler;
    private String clientUrl="http://192.168.1.147:8080";
    private final String loginStr="/Login";
    private final String registerStr="/Register";
    private ClientResultCallback clientResultCallback;
    public AIPingPongClientUsage(Handler handler)
    {
        this.uiHandler=handler;
    }
    public void setClientResultCallback(ClientResultCallback clientResultCallback)
    {
        this.clientResultCallback=clientResultCallback;
    }
    private void sendMessageToUiThread(String state)
    {
        if(this.uiHandler==null)
            return ;
        Message message=new Message();
        Bundle bundle=message.getData();
        bundle.putString("CLIENTRESULT",state);
        uiHandler.sendMessage(message);
    }
    public void upLoadFile(@NonNull String url,@NonNull String filePath)
    {
        try
        {
            File file=new File(filePath);
            RequestParams requestParams =new RequestParams();
            requestParams.put("file",file);
            AIPingPongClient.post(url, requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    sendMessageToUiThread("UPLOADSUCCESS");
                    if(AIPingPongClientUsage.this.clientResultCallback!=null)
                        AIPingPongClientUsage.this.clientResultCallback.onClientResult("UPLOADSUCCESS");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    sendMessageToUiThread("UPLOADFAIL");
                    if(AIPingPongClientUsage.this.clientResultCallback!=null)
                        AIPingPongClientUsage.this.clientResultCallback.onClientResult("UPLOADFAIL");
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void login(@NonNull String user,@NonNull String password)
    {
        try
        {
            RequestParams requestParams =new RequestParams();
            requestParams.put("user",user);
            requestParams.put("password",password);
            AIPingPongClient.get(this.clientUrl+this.loginStr, requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String loginState=new String (responseBody);
                    if(loginState.equals("succeed"))
                    {
                        sendMessageToUiThread("LOGINSUCCESS");
                        if(AIPingPongClientUsage.this.clientResultCallback!=null)
                            AIPingPongClientUsage.this.clientResultCallback.onClientResult("登陆成功");
                    }
                    else
                    {
                        sendMessageToUiThread("LOGINFAIL");
                        if(AIPingPongClientUsage.this.clientResultCallback!=null)
                            AIPingPongClientUsage.this.clientResultCallback.onClientResult("登录失败");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    sendMessageToUiThread("CONNECTFAIL");
                    if(AIPingPongClientUsage.this.clientResultCallback!=null)
                        AIPingPongClientUsage.this.clientResultCallback.onClientResult("连接服务器失败");
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
