package com.example.android.bluetoothlegatt.libs;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by New User on 3/22/2015.
 */
public class fileReader {
    public boolean parsingComplete = false;
    String path=null;
    File file;
    String tmpText="";
    public fileReader(){

    }
    public fileReader(String _path){
        path=_path;
        Log.d("fileReader", path);
    }

    private XmlPullParserFactory xmlFactoryObject;
    private XmlPullParser mParser;
    public boolean ready=false;
    public void initParser(){
        try {
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            mParser = xmlFactoryObject.newPullParser();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void openFile() {
        try {
            InputStream stream = new FileInputStream(new File(path));
            readStream(stream);
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void openXMLfile(String _path) {
        try {
            InputStream stream = new FileInputStream(new File(_path));
            readStream(stream);
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void openXMLUrl(String _path) {
        try {
            URL url = new URL(_path);
            InputStream stream=url.openStream();
            readStream(stream);
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void openXMLtxt(String str){
        try {
            tmpText=str;
            InputStream stream = new ByteArrayInputStream(str.getBytes("UTF-8"));
            readStream(stream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void readStream(InputStream stream){
        try {

            mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES
                    , false);
            mParser.setInput(stream, null);
            Log.d("fileReader", "start Reading");
            parseXML(mParser);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public XMLNode rootNode=null;

    public void parseXML(XmlPullParser myParser) {
        Stack<XMLNode> nodes;
        nodes = new Stack<XMLNode>();
        int event;
        try {
            XMLNode curNode=null;
            XMLNode parentNode=null;
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        //parentNode=curNode;
                        if(name!=null) {
                            if (!nodes.isEmpty()) {
                                parentNode = nodes.peek();
                            }

                            curNode=new XMLNode(myParser);
                            if(curNode!=null) {
                                nodes.push(curNode);
                            }

                            curNode.parent=parentNode;
                            if (parentNode != null) {
                                Log.d("xmlNode", "parent:" + parentNode.name);
                                if (parentNode.childNames == null) {
                                    parentNode.childNames = new ArrayList<String>();
                                    parentNode.childNodes = new ArrayList<ArrayList<XMLNode>>();
                                    Log.d("xmlNode", "first child");
                                }
                                //parentNode.childNames.add(curNode.name);
                                parentNode.addChild(curNode);
                                Log.d("xmlNode", "parent has " + parentNode.childNodes.size() + " childNodes");
                            }else{
                                rootNode=curNode;
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        //parentNode=curNode.parent;
                        if(name!=null) {
                            if (nodes.isEmpty()) {
                                //rootNode = curNode;
                                parentNode=null;
                            }else{
                                nodes.pop();
                                if (!nodes.isEmpty()) {
                                    parentNode = nodes.peek();
                                }else{
                                    parentNode=null;
                                }
                            }
                        }


                }
                event = myParser.next();

            }
            parsingComplete = true;
        }catch(Exception e){
                e.printStackTrace();
        }
        Log.d("fileReader", "show Nodes, root:" + rootNode.name);
        //rootNode.showNodes();
        //Log.d("fileReader",rootNode.getXML());
    }
    public static void saveTxt(String url,String data){
        FileHelper.saveTxt(url,data);
    }
}
