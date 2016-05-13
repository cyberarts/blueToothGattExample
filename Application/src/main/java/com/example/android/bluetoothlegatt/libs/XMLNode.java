package com.example.android.bluetoothlegatt.libs;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

/**
 * Created by New User on 3/22/2015.
 */
public class XMLNode {
    public ArrayList<ArrayList<XMLNode>> childNodes;
    public XMLNode parent=null;
    public String insideTxt=null;
    public ArrayList<String> childNames,attrNames,attrStr;
    public String name;
    public XMLNode(String _name){
        name=_name;
    }
    public void addChild(XMLNode node){
        if(childNames==null){
            childNames=new ArrayList<String>();
        }
        if(!childNames.isEmpty()){
            if(childNames.contains(node.name)){
               int idx= childNames.indexOf(node.name);
               childNodes.get(idx).add(node);
            }else{
                addNewNode(node);
            }
        }else{
            addNewNode(node);
        }
    }
    public void addNewNode(XMLNode node){
        if(childNodes==null){
            childNodes=new ArrayList<ArrayList<XMLNode>>();
        }
        int idx=childNames.size();
        childNames.add(node.name);
        childNodes.add(new ArrayList<XMLNode>());
        childNodes.get(idx).add(node);
    }
    public XMLNode(XmlPullParser mParser) {
        name = mParser.getName();
        Log.d("xmlNode", name);

        int attrCount = mParser.getAttributeCount();
        if(attrCount>0) {
            attrNames=new ArrayList<String>();
            attrStr=new ArrayList<String>();
            for (int i = 0; i < attrCount; i++) {
                attrNames.add(mParser.getAttributeName(i));
                attrStr.add(mParser.getAttributeValue(i));
            }
        }
    }
    public void showNodes(){
        Log.d("xmlNode", name);
        if(childNames!=null){
            for(int i=0;i<childNodes.size();i++){
                for(int j=0;j<childNodes.get(i).size();j++) {
                    childNodes.get(i).get(j).showNodes(j);
                }
            }
        }
    }
    public XMLNode findNode(String nodeName,int idx){
        int i=childNames.indexOf(nodeName);
        if(i<0){
            return null;
        }
        if(childNodes.get(i).size()>idx){
           return childNodes.get(i).get(idx);
        }
        return null;
    }
    public int getCount(String nodeName){
        int i=childNames.indexOf(nodeName);
        if(i<0){
            return -1;
        }
        return childNodes.get(i).size();
    }
    public String getAttr(String attr){
        int i=-1;
        if(!attrNames.contains(attr)){
            Log.d("xmlNode", "no attrName");
            return null;
        }else{
            Log.d("xmlNode", "attr:" + attr);
        }
        try{i=attrNames.indexOf(attr);}catch (Exception e){e.printStackTrace();}

        Log.d("xmlNode", attrStr.get(i));
        return attrStr.get(i);

        //    return null;


    }
    public String getXML(){
        String str;
        str = ("<"+name) ;
        if(attrNames!=null){
            for(int i=0;i<attrNames.size();i++){
                str += (" "+attrNames.get(i)+"=\""+attrStr.get(i)+"\"");
            }
        }
        if(childNames==null){
            str += ("></"+name+">\r\n");
        }else if(childNames.isEmpty()){
            str += ("></"+name+">\r\n");
        }else {
            str += (">\r\n");
            for(int i=0;i<childNodes.size();i++){
                for(int j=0;j<childNodes.get(i).size();j++) {
                    str+=childNodes.get(i).get(j).getXML();
                }
            }
            str += ("</"+name+">");
        }
        return str;
    }
    public void setAttr(String attrName,String val){
        if(attrNames==null){
            attrNames=new ArrayList<String>();
            attrStr= new ArrayList<String>();
        }
        if(!attrNames.contains(attrName)) {
            attrNames.add(attrName);
            attrStr.add(val);
        }else {
            int idx = attrNames.indexOf(attrName);
            attrStr.set(idx, val);
        }

    }
    public void showNodes(int idx){
        Log.d("xmlNode", name + "_" + idx);
        if(childNames!=null){
            for(int i=0;i<childNodes.size();i++){
                for(int j=0;j<childNodes.get(i).size();j++) {
                    childNodes.get(i).get(j).showNodes(j);
                }
            }
        }
    }

}
