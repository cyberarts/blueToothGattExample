package com.example.android.bluetoothlegatt.libs;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by New User on 3/24/2015.
 */
public class FileHelper {
    public static void saveTxt(String url,String data){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(url)));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public static boolean checkExist(String url){
        File f=new File(url);
        return f.exists();
    }
    public static void saveXML(String url,XMLNode node){
        saveTxt(url,node.getXML());
    }
    public static String readText(String url){
        StringBuilder text = new StringBuilder();
        File file=new File(url);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        }

        catch (Exception e) {
            return null;
            //You'll need to add proper error handling here
        }


    }

}
