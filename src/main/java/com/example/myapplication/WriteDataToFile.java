package com.example.myapplication;

import java.io.File;
import java.io.FileWriter;

//写入txt
public class WriteDataToFile
{
    private String filePath;
    private FileWriter fileWriter;
    public WriteDataToFile(String path)
    {
        this.filePath=path;
        try
        {
            File file =new File(path);
            if(!file.exists())
                file.createNewFile();
            fileWriter=new FileWriter(this.filePath,true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void write(String value)
    {
        try
        {
            fileWriter.write(value);
            fileWriter.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void close()
    {
        try
        {
            if(fileWriter!=null)
                fileWriter.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    //文件重命名
    public void renameFile(String path,String oldname,String newname)
    {
        if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名
            File oldfile=new File(path+"/"+oldname);
            File newfile=new File(path+"/"+newname);
            if(!oldfile.exists()){
                return;//重命名文件不存在
            }
            if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                System.out.println(newname+"已经存在！");
            else{
                oldfile.renameTo(newfile);
            }
        }else{
            System.out.println("新文件名和旧文件名相同...");
        }
    }
}
