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
import java.io.Writer;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

/**
 * 
 * @author K. van Wijk
 *
 */
public class OscDroidWriter extends Writer {

	private static final String TAG="oscdroid.connection.oscdroidwriter";
	private static final int TIMEOUT = 10;
	
	
	private final UsbDeviceConnection usbConnection;
	private final UsbEndpoint usbEndOut;
	
	/**
	 * Constructor
	 * @param conn Connection to use
	 * @param ep Endpoint to use
	 */
	public OscDroidWriter(UsbDeviceConnection conn, UsbEndpoint ep){
		usbConnection=conn;
		usbEndOut=ep;
	}
	
	@Override
	public void close() throws IOException {
		
	}

	@Override
	public void flush() throws IOException {
		
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
//		Log.d(TAG,"Sent: " + tmp);
	}

}
