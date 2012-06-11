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

package com.kvw.oscdroid.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ConnectionService {

	/** public statics, result codes */
	public static final int RESULT_OK 		= 0x10;
	public static final int RESULT_ERROR	= 0x11;
	public static final int RESULT_CANCEL 	= 0x12;
	public static final int CH1_DATA_START	= 0x41;
	public static final int CH2_DATA_START	= 0x42;
	public static final int CONN_STATUS_CHANGED = 0xFF;
	public static final int NEW_DATA_ARRIVED = 0xAF;
	public static final int CONNECTION_RESET = 0xFFFF;
	
	public static final String ANALOG_DATA = "analog_data";
	
	/** private statics, commands and values */ 
	private static final byte CH1CON_ADDR			= 0x00;
	private static final byte CH2CON_ADDR			= 0x01;
	private static final byte ANATRIGLVL_ADDR		= 0x02;
	private static final byte ANATIMECON_ADDR		= 0x03;
	private static final byte ANATRIGCON_ADDR		= 0x04;
	private static final byte LOGTRIGLVL_ADDR		= 0x05;
	private static final byte LOGTIMECON_ADDR		= 0x06;
	private static final byte DEVICEREV_ADDR		= 0x07;
	
	private static final int CONNTYPE_WIFI=1;
	private static final int CONNTYPE_USB=2;
	
	private static final int STATUS_NC=3;
	private static final int STATUS_CONNECTING=4;
	private static final int STATUS_CONNECTED=5;
	private static final int STATUS_DISCONNECTED=6;
	
	public static final int LEFT=2;
	public static final int RIGHT=1;
	public static final int CENTRE=3;
	
	public static final boolean RISING=true;
	public static final boolean FALLING=false;
	public static final boolean SINGLE=false;
	public static final boolean CONTINUOUS=true;
	
	
	private static final String TAG = "OscDroid.ConnectionService";
	private static final String ACTION_USB_PERMISSION = "com.kvw.oscdroid.connectionservice.usb";
	
	private final Handler mHandler;
	
	private boolean permissionRequested=false;
	private boolean usbBusy=false;
	private boolean newDataReady=false;
	private boolean newDataReadyRequested=false;
	private boolean requestingAllRegisters=false;
	
	private int connectionStatus = STATUS_NC;
	private int connectionType = CONNTYPE_USB;
	
	private int RUNNING_MODE=2; //1=continuous, 2=single
	
	private int usbReadErrorCnt = 0;
	
	/**	FPGA Register values */
	private int CH1CON=0;
	private int CH2CON=0;
	private int ANATRIGLVL=0;
	private int ANATIMECON=0;
	private int ANATRIGCON=0;
	private int LOGTRIGLVL=0;
	private int LOGTIMECON=0;
	private int DEVICEREV=0;
	
	private UsbManager usbManager=null;
	private UsbDevice usbDevice=null;
	private PendingIntent mPermissionIntent;
	private UsbOscilloscopeConnection connectionThread;
	
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
		
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),0);
	}
	
	/**
	 * @deprecated
	 * @param type
	 */
	public void setConnectionType(int type)
	{
		connectionStatus=STATUS_NC;
		connectionType=type;

	}
	
	public void registerReceiver()
	{
//		Log.d(TAG,"Registering usbReceiver");
		parentContext.registerReceiver(mUsbReceiver,new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
		parentContext.registerReceiver(mUsbReceiver,new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
		parentContext.registerReceiver(mUsbReceiver,new IntentFilter(ACTION_USB_PERMISSION));
	}
	
	public void setDevice(UsbDevice tmpAcc)
	{
		usbDevice = tmpAcc;
	}
	
	private void requestAllSettings()
	{
		requestingAllRegisters=true;

		connectionThread.dataToWrite=new byte[]{'/','?',CH1CON_ADDR};
		connectionThread.numBytesToRead=3;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData); //wait for register to be acquired
		
		connectionThread.dataToWrite=new byte[]{'/','?',CH2CON_ADDR};
		connectionThread.numBytesToRead=3;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData); //wait for register to be acquired
		
		connectionThread.dataToWrite=new byte[]{'/','?',ANATRIGLVL_ADDR};
		connectionThread.numBytesToRead=3;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData); //wait for register to be acquired
		
		connectionThread.dataToWrite=new byte[]{'/','?',ANATIMECON_ADDR};
		connectionThread.numBytesToRead=3;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData); //wait for register to be acquired
		
		connectionThread.dataToWrite=new byte[]{'/','?',ANATRIGCON_ADDR};
		connectionThread.numBytesToRead=3;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData); //wait for register to be acquired
		
		requestingAllRegisters=false;
	}
	
	private void setDefaultSettings()
	{
		setTriggerSource(1);
		setTriggerPos(CENTRE);
		setTriggerEdge(RISING);
		setTriggerLvl(128);
		
		//TODO set default amplifications for ch1 and ch2
		//TODO set default clock divider / timediv
		
		setCh1Enabled(true);
		setRunningMode(false);
		
		
	}

	public void setCh1Enabled(boolean enable)
	{
		if(enable){
			CH1CON = CH1CON | (1 << 0);
			connectionThread.dataToWrite = new byte[] {'/','\\',CH1CON_ADDR,(byte)CH1CON,'\\'};
			connectionThread.numBytesToRead=5;
			connectionThread.newWriteData=true;
			connectionThread.newReadData=true;
		}else if(!enable){
			CH1CON = CH1CON & ~(1 << 0);
			connectionThread.dataToWrite=new byte[] {'/','\\',CH1CON_ADDR,(byte)CH1CON,'\\'};
			connectionThread.numBytesToRead=5;
			connectionThread.newWriteData=true;
			connectionThread.newReadData=true;
		}
		
		while(connectionThread.newReadData);
	}
	
	public void setCh2Enabled(boolean enable)
	{
		if(enable){
			CH2CON = CH2CON | (1 << 0);
			connectionThread.dataToWrite = new byte[] {'/','\\',CH2CON_ADDR,(byte)CH2CON,'\\'};
			connectionThread.numBytesToRead=5;
			connectionThread.newWriteData=true;
			connectionThread.newReadData=true;
		}else if(!enable){
			CH2CON = CH2CON & ~(1 << 0);
			connectionThread.dataToWrite=new byte[] {'/','\\',CH2CON_ADDR,(byte)CH2CON,'\\'};
			connectionThread.numBytesToRead=5;
			connectionThread.newWriteData=true;
			connectionThread.newReadData=true;
		}
		
		while(connectionThread.newReadData);
	}

	public void setCh1Div(int div)
	{

		switch(div){
		//TODO switch div, set CH1CON bits accordingly
		}
		
		//TODO send CH1CON to CH1CON_ADDR
		
		while(connectionThread.newReadData);
	}
	
	public void setCh2Div(int div)
	{
		
		switch(div){
		//TODO
		}
		
		while(connectionThread.newReadData);
	}
	
	/**
	 * 
	 * @param lvl 8-bit integer, set trigger level without zero-biasing
	 */
	public void setTriggerLvl(int lvl)
	{
		ANATRIGLVL=lvl;
		
		connectionThread.dataToWrite=new byte[]{'/','\\',ANATRIGLVL_ADDR,(byte)ANATRIGLVL,'\\'};
		connectionThread.numBytesToRead=5;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData);
		
	}
	
	/**
	 * 
	 * @param div integer indicating selected time div
	 */
	public void setTimeDiv(int div)
	{
		int clkdiv=0;
		
		switch(div){
		//TODO calculate clock divider according to setting
		}
		
		ANATIMECON=clkdiv;
		
		connectionThread.dataToWrite=new byte[]{'/','\\',ANATIMECON_ADDR,(byte)ANATIMECON,'\\'};
		connectionThread.numBytesToRead=5;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData);
	}

	/**
	 * 
	 * @param pos 1=left, 2=center, 3=right
	 */
	public void setTriggerPos(int pos)
	{
		switch(pos){
		case 3:
			ANATRIGCON = ANATRIGCON | (1 << 6);
			ANATRIGCON = ANATRIGCON & ~(1 << 7);
			break;
		case 1:
			ANATRIGCON = ANATRIGCON & ~(1 << 6);
			ANATRIGCON = ANATRIGCON | (1 << 7);
			break;
		case 2:
			ANATRIGCON = ANATRIGCON | (1 << 6);
			ANATRIGCON = ANATRIGCON | (1 << 7);
			break;
		case 0:
			ANATRIGCON = ANATRIGCON & ~(1 << 6);
			ANATRIGCON =ANATRIGCON & ~(1<< 7);
			break;		
		}
		
		Log.d(TAG,"TriggerPos: " + ANATRIGCON);
		
		connectionThread.dataToWrite = new byte[] {'/','\\',ANATRIGCON_ADDR,(byte)ANATRIGCON,'\\'};
		connectionThread.numBytesToRead=5;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData);
		
	}
	
	/**
	 * 
	 * @param continu true for continuous mode, false for singleshot mode
	 */
	public void setRunningMode(boolean continu)
	{
		Log.d(TAG,"Setting running mode: " + continu);
		
		if(!continu){ //single shot
			ANATRIGCON = ANATRIGCON & ~(1 << 5);
			RUNNING_MODE=2;
		}
		else if(continu){ //continuous
			ANATRIGCON = ANATRIGCON | (1 << 5);
			RUNNING_MODE=1;
		}
//		connectionThread.dataToWrite=new byte[] {'/','\\',ANATRIGCON_ADDR,(byte)ANATRIGCON,'\\'};
//		connectionThread.numBytesToRead=5;
//		connectionThread.newWriteData=true;
//		connectionThread.newReadData=true;
//		
//		while(connectionThread.newReadData);
	}

	/**
	 * 
	 * @param rising true for rising edge trigger, false for falling edge trigger
	 */
	public void setTriggerEdge(boolean rising)
	{
		if(rising) //rising edge trigger
			ANATRIGCON = ANATRIGCON & ~(1 << 4);
			
		else if (!rising) //falling edge trigger
			ANATRIGCON = ANATRIGCON | (1 << 4);
		
		connectionThread.dataToWrite=new byte[] {'/','\\',ANATRIGCON_ADDR,(byte)ANATRIGCON,'\\'};
		connectionThread.numBytesToRead=5;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData);
	}
	
	/**
	 * 
	 * @param source 1=channel1, 2=channel2
	 */
	public void setTriggerSource(int source)
	{
		if(source==1) // channel1
			ANATRIGCON = ANATRIGCON & ~(1 << 3);
		
		else if(source==2) // channel2
			ANATRIGCON = ANATRIGCON | (1 << 3);
		
		connectionThread.dataToWrite=new byte[] {'/','\\',ANATRIGCON_ADDR,(byte)ANATRIGCON,'\\'};
		connectionThread.numBytesToRead=5;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
		
		while(connectionThread.newReadData);
	}
	
	/**
	 * 
	 * @param enable true for enabled, false for disabled
	 */
	public void setTriggerEnabled(boolean enable)
	{
		if(enable) //trigger enabled
			ANATRIGCON  = ANATRIGCON | (1 << 1);
			
		else if(!enable) //trigger disabled
			ANATRIGCON = ANATRIGCON & ~(1 << 1);
		
		Log.d(TAG,"ANATRIGCON, trig enabled: " + ANATRIGCON);
		
		connectionThread.dataToWrite=new byte[] {'/','\\',ANATRIGCON_ADDR,(byte)ANATRIGCON,'\\'};
		connectionThread.numBytesToRead=5;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;

		while(connectionThread.newReadData);
	}
	
	/**
	 * Enable trigger, wait for data
	 */
	public void getSingleShot()
	{	
		if(usbBusy)
			return;
		
		Log.d(TAG,"singleSHOT");
		usbBusy=true;
		setTriggerEnabled(true);
		isDataReady();
		while(connectionThread.newReadData);		
	}	
	
	/**
	 * Get data from FPGA
	 */
	private void requestData()
	{
		Log.d(TAG,"Requesting data");
		newDataReady=false;
		
		connectionThread.dataToWrite=new byte[]{'/','&'};
		connectionThread.numBytesToRead=2053;
		connectionThread.newWriteData=true;
		connectionThread.newReadData=true;
	}
	
	
	public void getData()
	{
		if(usbDevice==null || connectionThread.newReadData || 
				newDataReady || newDataReadyRequested)
			return;
		
		Log.w(TAG,"Getting data now!!!!");
		
		Thread t = new Thread(new Runnable(){
			public void run(){
				getSingleShot();
			}
		});
		t.start();
		//getSingleShot();
		
	}
	
	/** check if there was a trigger event. If yes: data ready */
	public void isDataReady()
	{
		Log.d(TAG,"Data ready???");
		connectionThread.dataToWrite=new byte[] {'/','?',ANATRIGCON_ADDR};
		connectionThread.numBytesToRead=3; 
		connectionThread.newWriteData=true;
		
		newDataReadyRequested=true;	
		connectionThread.newReadData=true;
		
	}
	
	public void setupConnection()
	{
		setState(STATUS_CONNECTING);

		if(connectionThread !=null)
			connectionThread.mRun=false;
		
		if(usbDevice!=null){
			if(!permissionRequested || !usbManager.hasPermission(usbDevice)){
				permissionRequested=true;
				usbManager.requestPermission(usbDevice, mPermissionIntent);
				return;
			}
			else{
				connectionThread=new UsbOscilloscopeConnection();
				connectionThread.start();
				while(!connectionThread.isRunning) ;
				Message msg = new Message();
				msg.what=CONN_STATUS_CHANGED;
				msg.arg1=0x0A; //connected
				mHandler.sendMessage(msg);
				requestAllSettings();
				setDefaultSettings();
								
			}
		} 
		else { //usbDevice == null
			HashMap <String,UsbDevice> deviceList = usbManager.getDeviceList();
			if(!deviceList.isEmpty()){
				Collection<UsbDevice> c = deviceList.values();
				Iterator<UsbDevice> itr = c.iterator();
				
//				UsbDevice tmpDev = deviceList.get("");
//				
//				if(tmpDev.getProductId()==4660 && tmpDev.getVendorId()==4660)
//					usbDevice=tmpDev;
				
				// Check all devices, find the right one
				while(itr.hasNext()){
					UsbDevice tmpDev = (UsbDevice) itr.next();
					Log.d(TAG,"Connected device: " + tmpDev);
					if(tmpDev.getProductId()==4659 && tmpDev.getVendorId()==4659)
						usbDevice=tmpDev;
					if(usbDevice!=null)
						break;
				}			
				
				if(usbDevice==null) //No correct devices
					return;
				// Need permission?
				if(!permissionRequested || !usbManager.hasPermission(usbDevice)){
					permissionRequested=true;
					usbManager.requestPermission(usbDevice, mPermissionIntent);
				}
			}
		}
	}
	
	public void closeConnection()
	{

		if(connectionThread!=null){
			//Close connection
			connectionThread.mRun=false;
			
//			Log.d(TAG,"Closing Connection");
			try {connectionThread.join();}
			catch(InterruptedException e){e.printStackTrace();}
			finally{
				connectionThread=null;
			}
		}
		permissionRequested=false;
		usbDevice=null;
		setState(STATUS_DISCONNECTED);
		
		Message msg = new Message();
		msg.what=CONN_STATUS_CHANGED;
		msg.arg1=0x0B; //Disconnected
		mHandler.sendMessage(msg);
	}
	
	public void cleanup()
	{
		closeConnection();
		try{
		parentContext.unregisterReceiver(mUsbReceiver);}
		catch(IllegalArgumentException ex){}
	}
	
	/**
	 * Save the settings from the FPGA  registers in the local registers
	 * @param data byte[] containing the data
	 */
	private void saveSettings(int[] data)
	{
		if(data[0]=='\\'){//correct return packet, check register address
			if(data[1]==CH1CON_ADDR)
				CH1CON=data[2];
			else if(data[1]==CH2CON_ADDR)
				CH2CON=data[2];
			else if(data[1]==ANATRIGLVL_ADDR)
				ANATRIGLVL=data[2];
			else if(data[1]==ANATIMECON_ADDR)
				ANATIMECON=data[2];
			else if(data[1]==ANATRIGCON_ADDR)
				ANATRIGCON=data[2];
			
			Log.v(TAG,"setting received: " + String.valueOf(data[2]));
		}
		usbBusy=false;
	}
	
	/**
	 * Send data to main activity through handler
	 * 
	 * @param data array containing datasamples
	 */
	private void sendAnalogueData(int[] data)
	{
		int trigAddress = data[3] + (data[2] << 8);
		
		int[] mData = new int[data.length-5];
		for(int i=0; i<mData.length; i++){
			mData[i]=data[i+5];
		}
		
		Message msg = new Message();
		msg.what=NEW_DATA_ARRIVED;
		msg.arg1=trigAddress;
		Bundle bundle = new Bundle();
		bundle.putIntArray(ANALOG_DATA, mData);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}
	
	/**
	 * check if dataReady bit was set
	 * 
	 * @param data array containing ANATRIGCON byte
	 */
	private void pollDataReady(int[] data)
	{
//		Log.d(TAG,"newDataReadyRequested.." + data[0] + " " + data[1] + " " + data[2]);
		
		if(data[0]=='\\' && data[1]==ANATRIGCON_ADDR){
			Log.d(TAG,"Checking data ready bit...");
			
			if((data[2] & 1 << 0) == 1){
				Log.d(TAG,"New data Ready!!!!");
				newDataReadyRequested=false;
				newDataReady=true;
				return;
			} 
		}
	}
	
	
	private void handleData(byte[] tmpdata, int numRead)
	{
		//Need to convert unsigned byte to int
		int[] data = new int[numRead];
		for(int i=0;i<numRead;i++)
			data[i] = (int)tmpdata[i] & 0xFF;
			
		
		Log.d(TAG,"Received num: " + data.length);
		
		
		if(requestingAllRegisters)
			saveSettings(data);
			
		else if(newDataReadyRequested){ //poll data ready bit
			pollDataReady(data);

		}else { //All other data
			
//			Log.d(TAG,"Data: " + (int)data[3]);
			
			// Analogue data, 2047 bytes
			if(data[0]=='+' && data[1]=='+')
				sendAnalogueData(data);
			usbBusy=false;
			
		}
	}
	
	/**
	 * 
	 * @param state integer indicating the state to set
	 */
	private void setState(int state)
	{
		connectionStatus=state;		
	}
	
	/**
	 * 
	 * @return integer indicating current connectionStatus
	 */
	public int getState(){
		return connectionStatus;
	}
	
	/**
	 * 
	 * @return true when connected, false when not connected
	 */
	public boolean isConnected()
	{
		if(connectionStatus==STATUS_CONNECTED)
			return true;
		else return false;
	}
	
	
	/** BroadcastReceiver to handle Usb Accessory events */
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction(); 

	        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
	            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	            if (device != null || usbDevice != null) {
	            	closeConnection();
	            	Log.w(TAG,"UsbDevice detached");
	                // call your method that cleans up and closes communication with the accessory
	            }
	            
	        } else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
	        	usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	        	usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	        	if(!permissionRequested || !usbManager.hasPermission(usbDevice)){
	        		permissionRequested=true;
//	        		Log.d(TAG,"request permission, broadcastReceiver");
	        		usbManager.requestPermission(usbDevice, mPermissionIntent);
	        	}
	        	Log.v(TAG,"UsbDevice attached");
	        	
	        	
	        } else if(ACTION_USB_PERMISSION.equals(action)){
	        	permissionRequested=true;
	        	usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	        	if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
	        		if (usbDevice!=null)
	        			setupConnection();
	        	}else {
	        		Log.w(TAG,"Permission denied for: " + usbDevice);
	        		usbDevice=null;
	        	}
	        }
	    }
	};

	
	
	
	/** DataThread, read/send data to Usb Accessory */
	class UsbOscilloscopeConnection extends Thread{
		private boolean connectionOk=false;

		private UsbDeviceConnection usbConnection=null;
		private UsbInterface usbIntf=null;
		private UsbEndpoint usbEndIn=null;
		private UsbEndpoint usbEndOut=null;
		
		private int TIMEOUT_READ=200;
		private int TIMEOUT_WRITE=100;
		
		private OscDroidWriter oscDroidWriter;
		private OscDroidReader oscDroidReader;
		
		private BufferedWriter oscWriter;
		private BufferedReader oscReader;
		
		private boolean reading=false;
		private boolean writing=false;
		private boolean reset=false;
		
		public boolean mRun = true;
		public boolean isRunning=false;
		public boolean newWriteData=false;
		public boolean newReadData=false;
		public byte[] dataToWrite=null;
		public int numBytesToRead=-1;
		
		/**
		 * Constructor for  the connectionThread. Setup USB, detect and connect correct endpoints
		 */
		public UsbOscilloscopeConnection(){
			//Setup connection
			
//			Log.d(TAG,"Connecting to: " + usbDevice.getDeviceName() + usbDevice.getDeviceId());
			
			usbIntf=usbDevice.getInterface(1);
			//Find correct endpoints
			if(usbIntf.getEndpoint(0).getDirection()==UsbConstants.USB_DIR_IN){
				usbEndIn=usbIntf.getEndpoint(0);
				usbEndOut=usbIntf.getEndpoint(1);
			} else if (usbIntf.getEndpoint(0).getDirection()==UsbConstants.USB_DIR_OUT){
				usbEndOut=usbIntf.getEndpoint(0);
				usbEndIn=usbIntf.getEndpoint(1);
			}
			
			usbConnection=usbManager.openDevice(usbDevice);
			usbConnection.claimInterface(usbIntf, true);
			
			oscDroidWriter = new OscDroidWriter(usbConnection,usbEndOut);
			oscDroidReader = new OscDroidReader(usbConnection,usbEndIn);
			
			oscWriter = new BufferedWriter(oscDroidWriter);
			oscReader = new BufferedReader(oscDroidReader);
			
			permissionRequested=true;
			if(usbEndIn!=null && usbEndOut != null)
				connectionOk=true;
		}
		
		/**
		 * @param data byte array to be written, 2 bytes expected on receiving end
		 */
		private synchronized void writeCmd(byte[] data)
		{	
			if(reading || writing)
				return;
			writing=true;
			usbBusy=true;
			usbConnection.claimInterface(usbIntf, true);
			
//			try{sleep(1);}
//			catch(InterruptedException ex) {}
			
			//flush in for clean buffer to hold the response
//			usbConnection.bulkTransfer(usbEndIn, new byte[2048], 2048, 1);
			
			char[] tmpdata = new char[dataToWrite.length];
			for(int i=0; i<dataToWrite.length;i++)
			{
				tmpdata[i]=(char)dataToWrite[i];
			}
			
			try {
				oscWriter.write(tmpdata);
				oscWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			int retries=3;
//			int tmp=-1;
//			for(int i=0;i<retries;retries--){
//				tmp=usbConnection.bulkTransfer(usbEndOut, data, data.length, TIMEOUT_WRITE);
//				if(tmp<0)
//					Log.e(TAG,"Sending failed, retry: " + new String(dataToWrite));
//				else
//					break;
//			}			
//			Log.d(TAG,"Sent: " + tmp);
			
			usbConnection.releaseInterface(usbIntf);
			try{sleep(1);}
			catch(InterruptedException ex){}
			writing=false;
//			return tmp;
		}
		
		/**
		 * Read bytes from usb, if they were correctly read, process them in handleData
		 * 
		 * @param numBytes Number of bytes to read from USB Endpoint IN
		 */
		private synchronized void readNumBytes(int numBytes)
		{		
			if(writing || reading)
				return;
			reading=true;
			usbConnection.claimInterface(usbIntf, true);
			
			
			try{sleep(1);}
			catch(InterruptedException ex) {}
			
			char[] buffer = new char[4096];
			byte[] data = new byte[numBytesToRead];
			
			Log.d(TAG,"Reading " + numBytes + " bytes of data");
			
//			int retries = 3;
			int tmp=-1;
			// read data, retry 3 times
			
			try {
				tmp = oscReader.read(buffer, 0, numBytesToRead);
			} catch (IOException e) {

				e.printStackTrace();
			}
			
			if(tmp<0)
			{			
				usbReadErrorCnt++;
				if(usbReadErrorCnt>3){
						newReadData=false;
						newDataReadyRequested=false;
						newDataReady=false;
						usbReadErrorCnt=0;
						usbConnection.claimInterface(usbIntf, true);
						tmp=usbConnection.bulkTransfer(usbEndIn, new byte[1],1,TIMEOUT_READ);
						while(tmp>0)
							tmp=usbConnection.bulkTransfer(usbEndIn,new byte[1],1,TIMEOUT_READ);
						
						Log.d(TAG,"Error, some info: " + usbEndIn.getAddress() + " " + usbEndIn.getAttributes()
								+ " " + usbEndIn.getInterval() + " " + usbEndIn.getType());
						usbBusy=false;
						usbConnection.releaseInterface(usbIntf);
				}
				reading=false;
				return;
			} else {
				usbReadErrorCnt=0;
				
				for(int i=0;i<numBytesToRead;i++){
					data[i]=(byte)buffer[i];
				}
				handleData(data,tmp);
			}
			
//			usbConnection.bulkTransfer(usbEndIn, new byte[2048], 2048, 10);
			newReadData=false;
			numBytesToRead=-1;
			buffer=null;
			reading=false;
			usbConnection.releaseInterface(usbIntf);
			
			try{sleep(1);}
			catch(InterruptedException ex){}
		}
		
		
		public void run(){
			if(!connectionOk){
				Log.w(TAG,"No connection!");
				setState(STATUS_DISCONNECTED);
				return;
			}			
			
			setState(STATUS_CONNECTED);
//			Log.d(TAG,"Connection OK, thread");
			
			try{sleep(1000);}
			catch(InterruptedException ex){}
			
			
			//byte[] tmp=new byte[4096];
			//if(usbConnection.bulkTransfer(usbEndIn, tmp, 4096, TIMEOUT_READ)>=0)
			//	; //Flush inbound endpoint
						
			//Log.d(TAG,"flushed, starting run");			
			
			
			/** Infinite loop for reading and writing from/to usb */
			isRunning=true;
			while(mRun){
				
				if(usbDevice==null){
					mRun=false;
					break;
				}
				
				if(RUNNING_MODE==2){
					
					if(newDataReadyRequested)
						isDataReady();
					
					else if(newDataReady)
						requestData();
					
					if(newWriteData && dataToWrite!=null && usbDevice!=null){
						writeCmd(dataToWrite);
						dataToWrite=null;
						newWriteData=false;
						
					}
					
//					try{sleep(1);}
//					catch(InterruptedException ex){}
					
					if(newReadData && numBytesToRead>0 && usbDevice!=null){
						readNumBytes(numBytesToRead);
					}
					
					
				}
				if(RUNNING_MODE==1){
					try {
						sleep(80);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(!newDataReadyRequested && !newDataReady)
						getData();
					
					if(newDataReadyRequested)
						isDataReady();
					
					else if(newDataReady)
						requestData();
					
					if(newWriteData && dataToWrite!=null && usbDevice!=null){
						writeCmd(dataToWrite);
						dataToWrite=null;
						newWriteData=false;						
					}
					
					if(newReadData && numBytesToRead>0 && usbDevice!=null){
						readNumBytes(numBytesToRead);
					}
					
				}
				
//				else if(newDataReady)
//					requestData();
//				else{
//				// Send data, 3 retries if failed
//				if(newWriteData && dataToWrite!=null && usbDevice!=null){
//					
//					if(writeCmd(dataToWrite)>0){
//						dataToWrite=null;
//						newWriteData=false;
//					}
//				}
//				
//				if(newReadData && numBytesToRead>0 && usbDevice!=null)
//					readNumBytes(numBytesToRead);
//				}
			}
			
			// close usb, nicely close thread
			isRunning=false;
			try{usbConnection.releaseInterface(usbIntf); 
				usbConnection.close();}
			catch(NullPointerException e){}
			
			usbConnection=null;
			usbEndIn=null;
			usbEndOut=null;
			usbIntf=null;
			usbDevice=null;
			
//			if(reset){
//				Message msg = new Message();
//				msg.what=CONNECTION_RESET;
//				mHandler.sendMessage(msg);
//			}
				
				
		}		
	}	
}