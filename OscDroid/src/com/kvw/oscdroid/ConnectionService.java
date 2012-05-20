package com.kvw.oscdroid;

import android.content.Context;
import android.os.Handler;

public class ConnectionService {

	private static final int CONNTYPE_WIFI=1;
	private static final int CONNTYPE_USB=2;
	
	private static final int STATUS_NC=3;
	private static final int STATUS_CONNECTING=4;
	private static final int STATUS_CONNECTED=5;
	private static final int STATUS_DISCONNECTED=6;
	
	private final Handler mHandler;
	
	private int connectionStatus=STATUS_NC;
	private int connectionType=CONNTYPE_USB;
	
	public ConnectionService(Context context, Handler handler)
	{
		mHandler=handler;
	}
	
	public synchronized void setConnectionType(int type)
	{
		connectionStatus=STATUS_NC;
		connectionType=type;
		//TODO
	}
	
	public synchronized void setupConnection()
	{
		//TODO
	}
	
	public synchronized void closeConnection()
	{
		//TODO
	}
	
	private synchronized void setState(int state)
	{
		connectionStatus=state;
		//TODO
	}
	
	public synchronized int getState(){
		return connectionStatus;
	}
	
}


