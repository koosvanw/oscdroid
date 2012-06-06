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


package com.kvw.oscdroid.channels;

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
	private static final int MAX_MEASUREMENTS=8;
	
	private static final String TAG = "oscdroid.channels.measurement";
	
	public static final int MSG_MEASUREMENTS=10;
	public static final String MEASUREMENT_RESULT = "result";

	public static final String SOURCE="chSource";
	
	private AnalogMeasurement[] measurementArray;
	
	private int numMeasurements=0;
	
	private boolean mRun=false;
	
	/**
	 * Constructor for Measurement class. Implements doing the measurements
	 * @param handler Handler for callback to main activity
	 */
	public Measurement(Handler handler)
	{
		mHandler = handler;
		measurementArray = new AnalogMeasurement[MAX_MEASUREMENTS];
	}
	
	/**
	 * Add measurement to be calculated and displayed
	 * 
	 * @param channel Measurement source
	 * @param type Type of measurement
	 */
	public void addMeasurement(AnalogChannel channel, int chan, int type)
	{
		if(numMeasurements>=MAX_MEASUREMENTS)
			return; //TODO return flag to indicate maximum amount measurements reached
		
		measurementArray[numMeasurements] = new AnalogMeasurement(channel, chan, type);
		numMeasurements++;
	}
	
	public synchronized void removeMeasurement(int which)
	{
		synchronized(measurementArray){
			for(int i=which;i<numMeasurements;i++)
				measurementArray[i]= i<numMeasurements-1 ? measurementArray[i+1] : null;

		}
		
		numMeasurements = numMeasurements>0 ? numMeasurements-1 : 0 ;
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

            	
            	synchronized(this){wait(500);}

            	
            	
    			for(int i=0;i<numMeasurements;i++){
    				float val=0;
    				Message msg = new Message();
                	msg.what=MSG_MEASUREMENTS;
                	msg.arg1=-1;
                	msg.arg2=-1;
                	Bundle msgData = new Bundle();
    				
    				switch(measurementArray[i].mType){
    				case 0: 	//delta-T measurement
    					//TODO implement delta-T measurement, check for cursors
    					//TODO add measurement to msgData
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMaximum();
	    					}
    					
    					break;
    				case 1: 	//delta-V measurement
    					//TODO implement delta-V measurement, check for cursors
    					//TODO add measurement to msgData
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMaximum();
	    					}
    					break;
    				case 2: 	//maximum
    					//TODO implement maximum measurement
    					//TODO add measurement to msgData
    					
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMaximum();
	    					}
    					break;
    				case 3: 	//minimum
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMinimum();
	    					}
    					break;
    				case 4:		//Pk-Pk
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getPkPk();
	    					}
    					break;
    				case 5:		//Frequency
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMaximum();
	    					}
    					break;
    				default:
    					//TODO add empty or null, to indicate no measurement
    					break;
    				}
    				
    				synchronized(measurementArray[i].mSource){
						msg.arg1= measurementArray[i].mType;
						msg.arg2=i;
						
    					msgData.putFloat(MEASUREMENT_RESULT, val);
    					msgData.putInt(SOURCE, measurementArray[i].mChan);
    				}
    				msg.setData(msgData);
        			if(msg.arg1!=-1)
        				mHandler.sendMessage(msg);
    			}
    			
            	
            	
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
class AnalogMeasurement {
	
	public final AnalogChannel mSource;
	public final int mType;
	public final int mChan;
	
	/**
	 * Constructor of measurement object
	 * 
	 * @param source measurement source
	 * @param type measurement type: delta-T, delta-V, max, min, Pk-Pk, frequency
	 */
	public AnalogMeasurement(AnalogChannel source, int chan, int type)
	{
		mSource=source;
		mType=type;
		mChan=chan;
	}
}
