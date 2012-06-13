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
	private Cursor curv1;
	private Cursor curv2;
	private Cursor curt1;
	private Cursor curt2;
	
	private float scrnWidth;
	private float scrnHeight;
	
	private int numMeasurements=0;
	
	// First 2 in ns, then 9x in us, then 9x in ms, then 4x in s
	private float[] mTimeConversion=new float[]{500,1000,2.5f,5,10,25,50,100,250,
			500,1000,2.5f,5,10,25,50,100,250,500,1000,2.5f,5,10,25};
	
	// 6 in mV, 5 in V
	private float[] mVoltConversion = new float[]{16,40,80,160,400,800,1.6f,4,8,16,40};
	
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
		scrnWidth=channel.getWidth();
		scrnHeight=channel.getHeight();
		
		if(numMeasurements>=MAX_MEASUREMENTS)
			return; //TODO return flag to indicate maximum amount measurements reached
		
		if(type==0){
			curt1.setEnabled(true);
			curt2.setEnabled(true);
		}else if (type==1){
			curv1.setEnabled(true);
			curv2.setEnabled(true);
		}
		
		measurementArray[numMeasurements] = new AnalogMeasurement(channel, chan, type);
		numMeasurements++;
	}
	
	public void addCursors(Cursor v1, Cursor v2, Cursor t1, Cursor t2)
	{
		curv1=v1;
		curv2=v2;
		curt1=t1;
		curt2=t2;
	}
	
	public synchronized void removeMeasurement(int which)
	{
		synchronized(measurementArray){
			
			if(measurementArray[which].mType==0){
				curt1.setEnabled(false);
				curt2.setEnabled(false);
			}
			if(measurementArray[which].mType==1){
				curv1.setEnabled(false);
				curv2.setEnabled(false);
			}
			
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
            	synchronized(this){wait(500);}
            	
    			for(int i=0;i<numMeasurements;i++){
    				float val=0;
    				float diff=0;
    				int timeDiv = measurementArray[i].mSource.getTimeDiv();
    				int voltDiv = measurementArray[i].mSource.getVoltDiv();
    				String result=null;
    				Message msg = new Message();
                	msg.what=MSG_MEASUREMENTS;
                	msg.arg1=-1;
                	msg.arg2=-1;
                	Bundle msgData = new Bundle();
    				
    				switch(measurementArray[i].mType){
    				case 0: 	//delta-T measurement
    					diff = curt2.getPos()-curt1.getPos();//reversed, because of coordinate system on tablet
    					val = diff/scrnWidth*mTimeConversion[timeDiv];   
    					
    					if(timeDiv<2)
    						result=String.format("%.2f",val) + " ns";
    					if(timeDiv>=2 && timeDiv<=10)
    						result=String.format("%.2f",val) + " us";
    					if(timeDiv>=11 && timeDiv<=19)
    						result=String.format("%.2f",val) + " ms";
    					if(timeDiv>19)
    						result=String.format("%.2f", val) + " s";
    					
    					break;
    				case 1: 	//delta-V measurement
    					diff = curv2.getPos()-curv1.getPos(); //reversed, because of coordinate system on tablet 
    					val = diff/scrnHeight*mVoltConversion[voltDiv];
    					
    					if(voltDiv<6)
    						result = String.format("%.2f", val) + " mV";
    					if(voltDiv>=6)
    						result=String.format("%.2f", val) + " V";
    					
    					break;
    				case 2: 	//maximum	
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMaximum();
	    					}
    					val = val/255*mVoltConversion[voltDiv];
    					if(voltDiv<6)
    						result = String.format("%.2f", val) + " mV";
    					if(voltDiv>=6)
    						result=String.format("%.2f", val) + " V";
    					
    					break;
    				case 3: 	//minimum
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMinimum();
	    					}
    					val = val/255*mVoltConversion[voltDiv];
    					if(voltDiv<6)
    						result = String.format("%.2f", val) + " mV";
    					if(voltDiv>=6)
    						result=String.format("%.2f", val) + " V";
    					break;
    				case 4:		//Pk-Pk
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getPkPk();
	    					}
    					val = val/255*mVoltConversion[voltDiv];
    					if(voltDiv<6)
    						result = String.format("%.2f", val) + " mV";
    					if(voltDiv>=6)
    						result=String.format("%.2f", val) + " V";
    					break;
    				case 5:		//Frequency
    					synchronized(measurementArray[i].mSource){
    						val = measurementArray[i].mSource.getMaximum();
	    					}
    					result="no F";
    					break;
    				default:
    					result="...";
    					break;
    				}
    				
    				synchronized(measurementArray[i].mSource){
						msg.arg1= measurementArray[i].mType;
						msg.arg2=i;
						
    					msgData.putString(MEASUREMENT_RESULT, result);
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
