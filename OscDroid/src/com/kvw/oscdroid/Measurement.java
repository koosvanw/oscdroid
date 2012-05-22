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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
/**
 * 
 * @author K. van Wijk
 *
 */
public class Measurement extends Thread{

	final Handler mHandler;
	private static final int MAX_MEASUREMENTS=4;
	public static final int MSG_MEASUREMENTS=10;
	public static final String DELTAT="Dt";
	public static final String DELTAV="Dv";
	public static final String MAX="maximum";
	public static final String MIN="minimum";
	public static final String PKPK="pk-pk";
	public static final String FREQ="frequency";
	
	private measurement[] measurementArray;
	
	private int numMeasurements=0;
	
	private boolean mRun=false;
	
	/**
	 * Constructor for Measurement class. Implements doing the measurements
	 * @param handler Handler for callback to main activity
	 */
	public Measurement(Handler handler)
	{
		mHandler = handler;
		measurementArray = new measurement[MAX_MEASUREMENTS];
	}
	
	/**
	 * Add measurement to be calculated and displayed
	 * 
	 * @param channel Measurement source
	 * @param type Type of measurement
	 */
	public void addMeasurement(AnalogChannel channel, int type)
	{
		if(numMeasurements>=MAX_MEASUREMENTS)
			return; //TODO return flag to indicate maximum amount measurements reached
		
		measurementArray[numMeasurements] = new measurement(channel,type);
		numMeasurements++;
	}
	
	/**
	 * Set thread to running
	 * 
	 * @param run True to enable, false to disable
	 */
	public void setRunning(boolean run)
	{
		mRun=run;
	}
	
	/**
	 * Main loop of the thread
	 */
	@Override
	public void run(){
		Log.v("measure","Thread started");
		
		while(mRun){
            try {
            	//TODO implement doing the measurements here
            	
            	Log.v("measure","running");
            	
            	synchronized(this){wait(500);}
            	
            	Log.v("Measurement","Measuring");
            	Message msg = new Message();
            	msg.what=MSG_MEASUREMENTS;
            	Bundle msgData = new Bundle();
            	
    			for(int i=0;i<numMeasurements;i++){
    				switch(measurementArray[i].mType){
    				case 0: 	//delta-T measurement
    					//TODO implement delta-T measurement, check for cursors
    					//TODO add measurement to msgData
    					
    					
    					break;
    				case 1: 	//delta-V measurement
    					//TODO implement delta-V measurement, check for cursors
    					//TODO add measurement to msgData
    					break;
    				case 2: 	//maximum
    					//TODO implement maximum measurement
    					//TODO add measurement to msgData
    					synchronized(measurementArray[i].mSource){
    					float max = measurementArray[i].mSource.getMaximum();
    					msgData.putFloat(MAX, max);
    					Log.v("measure","reached case");}
    					break;
    				case 3: 	//minimum
    					break;
    				case 4:		//Pk-Pk
    					break;
    				case 5:		//Frequency
    					break;
    				default:
    					//TODO add empty or null, to indicate no measurement
    					break;
    				}
    			}
    			msg.setData(msgData);
    			mHandler.sendMessage(msg);
            	
            	
            }catch(Exception e){Log.v("measure",e.toString());} 
            finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
            	
                //TODO release resources in case of exception
            }
		}
	}
	
}


/**
 * Types, enumerated 0-5:
 * 		delta-T, delta-V, max, min, Pk-Pk, frequency
 * 
 * @author K. van Wijk
 *
 */
class measurement {
	
	public final AnalogChannel mSource;
	public final int mType;
	
	/**
	 * Constructor of measurement object
	 * 
	 * @param source measurement source
	 * @param type measurement type: delta-T, delta-V, max, min, Pk-Pk, frequency
	 */
	public measurement(AnalogChannel source, int type)
	{
		mSource=source;
		mType=type;
	}
}
