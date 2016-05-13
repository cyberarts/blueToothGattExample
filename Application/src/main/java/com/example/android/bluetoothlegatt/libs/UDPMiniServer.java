package com.example.android.bluetoothlegatt.libs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class UDPMiniServer extends Thread {
	int MAX_UDP_DATAGRAM_LEN = 1500;
	int UDP_SERVER_PORT = 9000;
	Queue message=new LinkedList<String>();
	byte[] lmessage = new byte[MAX_UDP_DATAGRAM_LEN];
	boolean running = true;
	DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);
	
	DatagramSocket socket = null;
	//Handler mHandler;
	//public UDPMiniServer(Handler _handler){
	//	mHandler=_handler;
	//}
	public UDPMiniServer(){

	}
	public UDPMiniServer(int _port){
		UDP_SERVER_PORT=_port;
	}
	public interface onPacketListener{
		public void receivedMessage(String msg, InetAddress addr, int port);
	}
	
	public static onPacketListener mListener;

    public int sendPacket(String addr,int port,String msg){
        try{
            return sendPacket(InetAddress.getByName(addr),port,msg);
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }
    public int sendPacket(InetAddress addr,int port,String msg){
        if(socket!=null){
            DatagramPacket packetOut = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
            try {
                packetOut.setAddress(addr);
                packetOut.setPort(port);
            } catch (Exception e1) {
                // TODO Auto-generated catch block

                e1.printStackTrace();
                return -1;
            }
            try {

                socket.send(packetOut);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return -2;
            }
        }else{
            return -3;
        }
        return 0;
    }
	public void run() {
		try {
			socket = new DatagramSocket(UDP_SERVER_PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (running) {
			try {
				socket.receive(packet);
				//message.add(new String(lmessage, 0, packet.getLength()));
				mListener.receivedMessage(new String(lmessage, 0, packet.getLength()),packet.getAddress(),packet.getPort());
				// textMessage.setText(message);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					socket.close();
					try {
						socket = new DatagramSocket(UDP_SERVER_PORT);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
	}
}
