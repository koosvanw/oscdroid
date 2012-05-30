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

	private static final int CONNTYPE_WIFI=1;
	private static final int CONNTYPE_USB=2;
	
	private static final int STATUS_NC=3;
	private static final int STATUS_CONNECTING=4;
	private static final int STATUS_CONNECTED=5;
	private static final int STATUS_DISCONNECTED=6;
	
	private static final String TAG = "ConnectionService";
	private static final String ACTION_USB_PERMISSION = "com.kvw.oscdroid.connectionservice.usb";
	
	private final Handler mHandler;
	
	private int connectionStatus=STATUS_NC;
	private int connectionType=CONNTYPE_USB;
	
	private UsbManager usbManager;
	private UsbAccessory usbDevice;
	private PendingIntent mPermissionIntent;
	private usbAccessoryConnection connectionThread;
	
	private final Context parentContext;
	
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
		
		
		
	}
	
	public void destroy()
	{
		closeConnection();
		parentContext.unregisterReceiver(mUsbReceiver);
	}
	
	private void handleData(String data)
	{
		Log.v(TAG,"Received: " + data);
	}
	
	private synchronized void setState(int state)
	{
		connectionStatus=state;
		//TODO
	}
	 
	public synchronized int getState(){
		return connectionStatus;
	}
	
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction(); 

	        if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
	            UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	            if (accessory != null) {
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
	        	usbDevice = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	        	if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
	        		if (usbDevice!=null)
	        			setupConnection();
	        	}else Log.d(TAG,"Permission denied for: " + usbDevice);
	        }
	    }
	};
	
	/** DataThread  */
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
		
		public void writeString(String data)
		{
			//TODO write data to usb endpoint
		}
		
		public void run(){
			//TODO implement continuous reading of data, and sending to handler method
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


