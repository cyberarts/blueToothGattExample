package com.example.android.bluetoothlegatt.libs;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by New User on 3/24/2015.
 */
public class ReadHttpTxt extends AsyncTask<String, Void ,String> {
    public boolean ready=false;
    public String result="";
    @Override
    protected String doInBackground(String... urls) {
        try {
            // Create a URL for the desired page
            URL url = new URL(urls[0]);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            String retStr="";
            while ((str = in.readLine()) != null) {
                // str is one line of text; readLine() strips the newline character(s)
                retStr+=(str+"\r\n");
            }
            in.close();
            return retStr;
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    return null;
    }
    @Override
    protected void onPostExecute(String str){
        result=str;
        ready=true;
    }
}
