package com.kvw.oscdroid.connection;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

public class OscDroidWriter extends Writer {

	private static final String TAG="oscdroid.connection.oscdroidwriter";
	private static final int TIMEOUT = 10;
	
	
	private final UsbDeviceConnection usbConnection;
	private final UsbEndpoint usbEndOut;
	
	public OscDroidWriter(UsbDeviceConnection conn, UsbEndpoint ep){
		usbConnection=conn;
		usbEndOut=ep;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(char[] buf, int offset, int count) throws IOException {
		byte[] buffer = new byte[count]; //new String(buf, offset, count).getBytes(Charset.forName("US-ASCII"));
		
		for(int i=0;i<count;i++){
			buffer[i]=(byte)buf[i];
		}
		
//		Log.d(TAG,"Sending: " + String.valueOf(buffer[2]));
		
		int retries=3;
		int tmp=-1;
		for(int i=0;i<retries;retries--){
			tmp=usbConnection.bulkTransfer(usbEndOut, buffer, buffer.length, TIMEOUT);
			if(tmp<0)
				Log.e(TAG,"Sending failed, retry");
			else
				break;
		}
		Log.d(TAG,"Sent: " + tmp);
	}

}
