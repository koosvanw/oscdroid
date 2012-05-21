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


