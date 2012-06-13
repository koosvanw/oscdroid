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

import java.io.IOException;
import java.io.Reader;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

/**
 * 
 * @author K. van Wijk
 *
 */
public class OscDroidReader extends Reader {

	private static final String TAG="oscdroid.connection.oscdroidreader";
	private static final int TIMEOUT=100;
	
	private final UsbDeviceConnection usbConnection;
	private final UsbEndpoint usbEndIn;
	private byte[] buffer;
	
	/**
	 * Constructor
	 * @param conn Connection to use
	 * @param ep Endpoint to use
	 */
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
