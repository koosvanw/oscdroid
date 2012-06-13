package com.kvw.oscdroid.connection;

import java.io.IOException;
import java.io.Reader;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

public class OscDroidReader extends Reader {

	private static final String TAG="oscdroid.connection.oscdroidreader";
	private static final int TIMEOUT=100;
	
	private final UsbDeviceConnection usbConnection;
	private final UsbEndpoint usbEndIn;
	private byte[] buffer;
	
	public OscDroidReader(UsbDeviceConnection conn, UsbEndpoint ep)
	{
		usbConnection = conn;
		usbEndIn = ep;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public int read(char[] buf, int offset, int count) throws IOException {

		buffer=new byte[8192];
		int retries = 3;
		int tmp=-1;
		
		for(int i=0;i<retries;retries--){

			tmp = usbConnection.bulkTransfer(usbEndIn, buffer, count, TIMEOUT);
			
			if(tmp<0){
				Log.e(TAG,"Error receiving data: " + tmp + " bytes read");
				try{wait(1);}
				catch(InterruptedException ex){}
			}
			else				
				break;
		}
		if(tmp>0){
			for(int i=0; i<tmp;i++){
				buf[i]=(char)buffer[i];
			}
//			int errCheck = usbConnection.bulkTransfer(usbEndIn, new byte[1], 1, TIMEOUT);
//			while(errCheck>0) //Try to completely read the endpoint
//				errCheck = usbConnection.bulkTransfer(usbEndIn, new byte[1], 1, TIMEOUT);
		}
		buffer=null;
		System.gc();
		return tmp;
	}


}
