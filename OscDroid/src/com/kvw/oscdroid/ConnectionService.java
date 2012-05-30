/** This file is part of OscDroid for Android.
 *
 * Copyright (C) 2012 K. van Wijk, Enschede, The Netherlands
 *
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. * 
 * 
 */

package com.kvw.oscdroid;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class ConnectionService {

	/** public statics, result codes */
	public static final int RESULT_OK 		= 0x10;
	public static final int RESULT_ERROR		= 0x11;
	public static final int RESULT_CANCEL 	= 0x12;
	public static final int CH1_DATA_START	= 0x41;
	public static final int CH2_DATA_START	= 0x42;
	
	/** private statics, commands and values */ 
	private static final int ENABLED			= 0x01;
	private static final int DISABLED			= 0xFF;
	private static final int TRIG_OFF_LEFT	= 0x0A;
	private static final int TRIG_OFF_CENT	= 0x0B;
	private static final int TRIG_OFF_RIGHT	= 0x0C;
	private static final int RUN_MODE_AUTO	= 0x1A;
	private static final int RUN_MODE_SINGLE	= 0x1B;
	private static final int RUN_MODE_CONT	= 0x1C;
	
	private static final int SET_CH1_ENABLED	= 0xAA;
	private static final int SET_CH2_ENABLED 	= 0xBB;
	private static final int SET_TRIG_SOURCE_CH1 = 0x0A;
	private static final int SET_TRIG_SOURCE_CH2 = 0x0B;
	private static final int SET_TRIG_LEVEL	= 0x1A;
	private static final int SET_TRIG_OFF		= 0x1B;
	private static final int SET_VDIFF_CH1	= 0x0C;
	private static final int SET_VDIFF_CH2	= 0x1C;
	private static final int SET_TIME_DIFF	= 0x20;
	private static final int SET_RUN_MODE		= 0x30;	
	
	private static final int CONNTYPE_WIFI=1;
	private static final int CONNTYPE_USB=2;
	
	private static final int STATUS_NC=3;
	private static final int STATUS_CONNECTING=4;
	private static final int STATUS_CONNECTED=5;
	private static final int STATUS_DISCONNECTED=6;
	
	private static final String TAG = "ConnectionService";
	private static final String ACTION_USB_PERMISSION = "com.kvw.oscdroid.connectionservice.usb";
	
	private final Handler mHandler;
	
	private int connectionStatus = STATUS_NC;
	private int connectionType = CONNTYPE_USB;
	
	private UsbManager usbManager;
	private UsbAccessory usbDevice;
	private PendingIntent mPermissionIntent;
	private usbAccessoryConnection connectionThread;
	
	private final Context parentContext;
	
	/**
	 * Constructor for ConnectionService class
	 * 
	 * @param context
	 * @param handler
	 */
	public ConnectionService(Context context, Handler handler)
	{
		mHandler=handler;
		usbManager= (UsbManager) context.getSystemService(Context.USB_SERVICE);
		parentContext=context;
		
		context.registerReceiver(mUsbReceiver,new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_ATTACHED));
		context.registerReceiver(mUsbReceiver,new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
		context.registerReceiver(mUsbReceiver,new IntentFilter(ACTION_USB_PERMISSION));
		
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),0);
	}
	
	public synchronized void setConnectionType(int type)
	{
		connectionStatus=STATUS_NC;
		connectionType=type;
		//TODO
	}
	
	public synchronized void setupConnection()
	{
		setState(STATUS_CONNECTING);
		//TODO
		if(connectionThread !=null)
			connectionThread.mRun=false;
		
		if(usbDevice!=null){
			connectionThread=new usbAccessoryConnection();
			connectionThread.start();
		}
		setState(STATUS_CONNECTED);		
	}
	
	public synchronized void closeConnection()
	{
		//TODO
		if(connectionThread!=null)
			connectionThread.mRun=false;
		
		setState(STATUS_DISCONNECTED);	
	}
	
	public synchronized void cleanup()
	{
		closeConnection();
		parentContext.unregisterReceiver(mUsbReceiver);
	}
	
	private void handleData(String data)
	{
		Log.d(TAG,"Received: " + data);
		byte[] tmpBuf = data.getBytes();
		
		switch (tmpBuf[0]){
		case RESULT_OK:
			break;
		case RESULT_ERROR:
			//TODO handle error
			break;
		case RESULT_CANCEL:
			//TODO handle canceled
			break;
		case CH1_DATA_START:
			//TODO handle data
			break;
		case CH2_DATA_START:
			//TODO handle data
			break;
		}
	}
	
	private void setState(int state)
	{
		connectionStatus=state;
		
	}
	 
	public synchronized int getState(){
		return connectionStatus;
	}
	
	/**
	 * BroadcastReceiver to handle Usb Accessory events	 * 
	 * 
	 */
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction(); 

	        if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
	            UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	            if (accessory != null | usbDevice != null) {
	            	closeConnection();
	            	Log.v(TAG,"accessory detached");
	                // call your method that cleans up and closes communication with the accessory
	            }
	            
	        } else if(UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)){
	        	usbDevice = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	        	usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	        	usbManager.requestPermission(usbDevice, mPermissionIntent);
	        	Log.v(TAG,"accessory attached");
	        	
	        	
	        } else if(ACTION_USB_PERMISSION.equals(action)){
	        	
	        	Log.d(TAG,"Permission request result received");
	        	usbDevice = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	        	if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
	        		if (usbDevice!=null)
	        			setupConnection();
	        	}else Log.d(TAG,"Permission denied for: " + usbDevice);
	        }
	    }
	};
	
	/**
	 * 
	 * DataThread, read/send data to Usb Accessory
	 * 
	 */
	class usbAccessoryConnection extends Thread{
		private final FileInputStream inStream;
		private final FileOutputStream outStream;
		private FileDescriptor fd;
		private ParcelFileDescriptor fileDescriptor;
		
		public boolean mRun = true;
		
		public usbAccessoryConnection(){
			fileDescriptor = usbManager.openAccessory(usbDevice);
			fd = fileDescriptor.getFileDescriptor();
						
			inStream = new FileInputStream(fd);
			outStream = new FileOutputStream(fd);
		}
		
		/**
		 * 
		 * @param data byte array to be written, 2 bytes expected on receiving end
		 */
		public void writeCmd(byte[] data)
		{
			//TODO write data to usb endpoint
			if(data.length>2)
				return;
			try{
				outStream.write(data);
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		
		public void run(){
			byte[] data = new byte[16384];			
			while(mRun){
				//TODO read here
				try{
					int read = inStream.read(data);
					if(read!=-1)
						handleData(new String(data,0,read));
					
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			
			// run==false, so close the streams and let thread die nicelye
			try {
				inStream.close();
				outStream.close();
				fileDescriptor.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}	
}


