package com.example.android.bluetoothlegatt.libs;

import android.os.AsyncTask;


/**
 * Created by New User on 3/24/2015.
 */
public class ReadHttpXML extends AsyncTask<String, Void ,XMLNode> {
    public boolean mReady=false;
    public XMLNode result=null;
    public fileReader f;
    String xmltext=null;
    @Override
    protected XMLNode doInBackground(String... urls) {
        f=new fileReader();
        f.initParser();
        f.openXMLUrl(urls[0]);
        xmltext=f.tmpText;
        return f.rootNode;
    }
    @Override
    protected void onPostExecute( XMLNode node){
        result=node;
        mReady=true;
        postExec(node);
    }
    public void postExec( XMLNode node){
        //do whatever u want, or override it;
    }
}
