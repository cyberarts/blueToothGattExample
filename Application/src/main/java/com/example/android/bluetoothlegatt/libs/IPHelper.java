package com.example.android.bluetoothlegatt.libs;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class IPHelper {
    public static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        if(ipAddress.contains("192.168.")) {
                            //Log.e("IP address", "" + ipAddress);
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Socket exception", ex.toString());
        }
        return null;
    }
    public String getWifiIP(Context cont) {
        WifiManager wifiMgr = (WifiManager) cont.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        try{
            int myIp = wifiInfo.getIpAddress();
            byte[] bytes = BigInteger.valueOf(myIp).toByteArray();
            byte tmp=bytes[0];
            if(bytes.length<2){
                return getIpAddress();
            }
            bytes[0]=bytes[3];
            bytes[3]=tmp;
            tmp=bytes[1];
            bytes[1]=bytes[2];
            bytes[2]=tmp;

            try {
                InetAddress address = InetAddress.getByAddress(bytes);
                return address.toString();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            int intMyIp3 = myIp/0x1000000;
            int intMyIp3mod = myIp%0x1000000;

            int intMyIp2 = intMyIp3mod/0x10000;
            int intMyIp2mod = intMyIp3mod%0x10000;

            int intMyIp1 = intMyIp2mod/0x100;
            int intMyIp0 = intMyIp2mod%0x100;


            String ipAddress=(String.valueOf(intMyIp0)
                    + "" + String.valueOf(intMyIp1)
                    + "." + String.valueOf(intMyIp2)
                    + "." + String.valueOf(intMyIp3)
            );
            return ipAddress;
        }catch(Exception e){
            e.printStackTrace();
            return getIpAddress();
        }
        //return null;
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() ) {
                        String ipString= inetAddress.getHostAddress().toString();
                        //Log.d("interface addr",intf.getDisplayName()+":"+ipString);
                        if(ipString.startsWith("192.168.") || ipString.startsWith("10.0.")){
                            return ipString;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    public static InetAddress getBroadcast() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    return interfaceAddress.getBroadcast();
                }
            }
        }
        return null;
    }
}

