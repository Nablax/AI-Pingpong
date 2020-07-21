package com.example.myapplication;

import android.os.Environment;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by 叶明林 on 2017/9/17.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final String errorLogFile= Environment.getExternalStorageDirectory()
            + File.separator +"ballGame"+File.separator+"SystemErrorLog";
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        File rootFile=new File(errorLogFile);
        if(!rootFile.exists())
            rootFile.mkdir();
        WriteDataToFile writeDataToFile=new WriteDataToFile(rootFile+File.separator+DateTool.getCurrentTime()+".txt");
        e.printStackTrace();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        writeDataToFile.write(result.toString());
        writeDataToFile.close();
    }
}
