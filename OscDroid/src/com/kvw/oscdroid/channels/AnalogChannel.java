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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;

import com.badlogic.gdx.audio.analysis.FFT;

/**
 * 
 * @author K. van Wijk
 *
 */
public class AnalogChannel {
	
	private final String chName;
	private final String TAG="oscdroid.channel.AnalogChannel";
	
	private int NUM_SAMPLES=1024;
	
	private int chColor;
	private int chVoltDiv;
	private int chTimeDiv;
	private float chMaximum;
	private float chMinimum;
	private float chPeakpeak;
	private float chAverage;
	
	private float screenWidth;
	private float screenHeight;
	
	private float chVoltOffset=0;
	private float chTimeOffset=0;
	private float chVoltZoom=0;
	private float chTimeZoom=0;
	
	private float chVoltZoomOld=0;
	private float chTimeZoomOld=0;
	
	private float chFrequency;
	
	private boolean chEnabled=false;
	private boolean chNewDataAvailable;
	
	private Paint chPaint;
	
	final Handler mHandler;
	
	private static final int[] mTimeDivSwitchTable = new int[]{50,100,250,500,1000,1250,
		1667,2000,1923,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,
		5000,10000,20000,50000};   
	private static final int[] mSampleRates = new int[]{100000000,100000000,100000000,100000000,100000000,50000000,
		33333333,20000000,7692307,4000000,2000000,800000,400000,200000,40000,40000,20000,8000,4000,2000,2000,2000,2000,2000};
			
//			{2000,2000,2000,2000,2000,4000,8000,20000,
//		40000,80000,200000,400000,800000,2000000,4000000,7692307,20000000,33333333,
//		50000000,100000000,100000000,100000000,100000000,100000000};
	
	private volatile int[] mDataSet;
	private int triggerAddress = NUM_SAMPLES/2-3;
	private int triggerPos=1;
	
	static {System.loadLibrary("analog");}
	
	private native float calcDisplayX(int num, int numSamples, float scrnWidth, float zoomX, float offsetX);
	private native float calcDisplayY(int dataPoint, float scrnHeight, float zoomY, float offsetY);
	private native float getMax(int[] mDataSet, int numSamples);
	private native int[] calcDispSamples(int[] mDataSet, int trigAddress, int numSamples);
	
	
	/**
	 * 
	 * @param handler Handler to send messages back to the main activity
	 * @param name Channel name
	 */
	public AnalogChannel(Handler handler, String name)
	{
		mHandler = handler;
		chName=name;
		
		
		//Sine, 16 periods
//		mDataSet = new int[]{128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115,
//				128,140,152,165,176,188,198,208,218,226,234,240,245,250,253,254,255,254,253,
//				250,245,240,234,226,218,208,198,188,176,165,152,140,128,115,103,90,79,67,57,
//				47,37,29,21,15,10,5,2,1,0,1,2,5,10,15,21,29,37,47,57,67,79,90,103,115};
		
		//Complex
		mDataSet=new int[]{128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177,173,169,165,162,159,156,153,151,149,147,145,144,143,141,
				140,139,138,137,136,135,134,132,131,130,129,128,126,125,124,123,122,122,121,121,120,120,121,
				121,121,122,123,123,124,125,125,126,126,126,126,125,124,123,121,118,115,112,108,104,99,94,88,
				82,76,69,63,57,51,45,39,34,29,25,22,19,18,17,17,18,20,23,28,33,39,45,53,61,70,79,89,99,109,119,
				128,139,149,159,169,178,187,195,203,210,215,220,224,228,230,231,231,231,230,228,225,222,219,
				215,210,206,201,196,191,187,182,177};
		
		triggerAddress=NUM_SAMPLES/2;
		
//		Random random = new Random();
//		for (int i=0;i<mDataSet.length;i++)
//		{
//			mDataSet[i]=random.nextInt(255);
//		}
		
		chColor=Color.BLUE;
		chPaint=new Paint();
		chPaint.setStrokeWidth(1f);
		chPaint.setStyle(Style.STROKE);
		chPaint.setColor(chColor);
		chPaint.setDither(false);
	}
	
	/**
	 * Function that implements drawing of the channel.
	 * 
	 * Loop through datasamples in 2 steps, taking triggerAddress and 
	 * trigger position into account.
	 * 
	 * @param canvas Canvas on which the channel is to be drawn
	 */
	public void drawChannel(Canvas canvas)
	{
		if(!chEnabled)
			return;
		
		Path chPath=new Path();
		float max = 0;
		float min = 255;
		int NUM_DISPLAY_SAMPLES=mTimeDivSwitchTable[chTimeDiv];
		
		if(NUM_DISPLAY_SAMPLES>NUM_SAMPLES)
			NUM_DISPLAY_SAMPLES = NUM_SAMPLES;
		int start=NUM_SAMPLES/2;
		int stop=NUM_SAMPLES;
		
		//TODO fix offset, currently not fully correct
		
		// Check trigger position, correctly redraw the samples
		switch(triggerPos){
		case 0:
			start = triggerAddress-NUM_DISPLAY_SAMPLES/5 -(int)chTimeOffset < 0 ? 
					NUM_SAMPLES-(NUM_DISPLAY_SAMPLES/5-triggerAddress)-(int)chTimeOffset : triggerAddress-NUM_DISPLAY_SAMPLES/5-(int)chTimeOffset;
					
			stop = start+NUM_DISPLAY_SAMPLES > NUM_SAMPLES -(int)chTimeOffset ? 
					NUM_DISPLAY_SAMPLES-(NUM_SAMPLES-start) -(int)chTimeOffset : start+NUM_DISPLAY_SAMPLES-(int)chTimeOffset;
			
			break;
		case 1:
			start = triggerAddress-NUM_DISPLAY_SAMPLES/2-(int)chTimeOffset < 0 ? 
					NUM_SAMPLES-(NUM_DISPLAY_SAMPLES/2-triggerAddress)-(int)chTimeOffset : triggerAddress-NUM_DISPLAY_SAMPLES/2-(int)chTimeOffset;
					
			stop = start+NUM_DISPLAY_SAMPLES > NUM_SAMPLES-(int)chTimeOffset ? 
					NUM_DISPLAY_SAMPLES-(NUM_SAMPLES-start)-(int)chTimeOffset : start+NUM_DISPLAY_SAMPLES-(int)chTimeOffset;
			
			break;
		case 2:
			start = triggerAddress-(NUM_DISPLAY_SAMPLES*4/5)-(int)chTimeOffset < 0 ? 
					NUM_SAMPLES-(NUM_DISPLAY_SAMPLES*4/5-triggerAddress)-(int)chTimeOffset : triggerAddress-NUM_DISPLAY_SAMPLES*4/5-(int)chTimeOffset;
					
			stop = start+NUM_DISPLAY_SAMPLES > NUM_SAMPLES-(int)chTimeOffset ? 
					NUM_DISPLAY_SAMPLES-(NUM_SAMPLES-start)-(int)chTimeOffset : start+NUM_DISPLAY_SAMPLES-(int)chTimeOffset;
			break;		
		}
		
		if(start<0){
			start=0;
			stop=NUM_DISPLAY_SAMPLES;
		}
		if(start>=NUM_SAMPLES) start=NUM_SAMPLES-1;
		if(stop>NUM_SAMPLES){
			stop = NUM_SAMPLES;
			start = NUM_SAMPLES-NUM_DISPLAY_SAMPLES;
		}
			
		
		
		//Log.d(TAG,"Start: " + start + " stop: " + stop);
		
		int dataNumber=0;
		
		if(start>stop){
			for(int i=start; i<NUM_SAMPLES;i++){
				
				float x = calcDisplayX(dataNumber,NUM_DISPLAY_SAMPLES,screenWidth,chTimeZoom,chTimeOffset);
				float y = calcDisplayY(mDataSet[i],screenHeight,chVoltZoom,chVoltOffset);
				if (mDataSet[i] > max) max = mDataSet[i];
				if (mDataSet[i]<min) min = mDataSet[i];
				
				if(i==start)
					chPath.moveTo(x,y);
				
				chPath.lineTo(x,y);
				dataNumber++;
			}
			
			for(int i=0;i<stop;i++){
				float x = calcDisplayX(dataNumber,NUM_DISPLAY_SAMPLES,screenWidth,chTimeZoom,chTimeOffset);
				float y = calcDisplayY(mDataSet[i],screenHeight,chVoltZoom,chVoltOffset);
				if (mDataSet[i] > max) max = mDataSet[i];
				if (mDataSet[i]<min) min = mDataSet[i];
				
				chPath.lineTo(x,y);
				dataNumber++;
			}	
		} else if (start<stop){
			for(int i=start; i<stop;i++){
				
				float x = calcDisplayX(dataNumber,NUM_DISPLAY_SAMPLES,screenWidth,chTimeZoom,chTimeOffset);
				float y = calcDisplayY(mDataSet[i],screenHeight,chVoltZoom,chVoltOffset);
				if (mDataSet[i] > max) max = mDataSet[i];
				if (mDataSet[i]<min) min = mDataSet[i];
				
				if(i==start)
					chPath.moveTo(x,y);
				
				chPath.lineTo(x,y);
				dataNumber++;
			}
		}		
		
		chMaximum=max;
		chMinimum=min;
		chPeakpeak=max-min;
		
		canvas.drawPath(chPath, chPaint);
		chPath=null;		
	}
	
	/**
	 * 
	 * @param color Color to use when drawing the channel
	 */
	public void setColor(int color)
	{
		chColor=color;
		chPaint.setColor(color);
	}
	
	/**
	 * 
	 * @param divs Integer representing the volt divs
	 */
	public synchronized void setVoltDivs(int divs)
	{
		chVoltDiv = divs;
	}
	
	/**
	 * 
	 * @param divs Integer representing the time divs
	 */
	public synchronized void setTimeDivs(int divs)
	{
		chTimeDiv=divs;
	}
	
	/**
	 * 
	 * @param enabled True when enabled, false when disabled
	 */
	public void setEnabled(boolean enabled)
	{
		chEnabled=enabled;
	}
	
	/**
	 * 
	 * @param xOffset float representing offset on X-axis
	 * @param yOffset float representing offset on Y-axis
	 */
	public void setOffset(float xOffset,float yOffset)
	{
		chTimeOffset+=xOffset/2;
		chVoltOffset+=yOffset/2;
	}
	
	/** Reset zoom to 0 zoom, 0 offset */
	public void resetZoom()
	{
		chTimeZoom=0;
		chVoltZoom=0;
		chTimeOffset=0;
		chVoltOffset=0;
		chTimeZoomOld=0;
		chVoltZoomOld=0;
	}
	
	/**
	 * 
	 * @param xZoom float representing zoomfactor for the X-axis
	 * @param yZoom float representing zoomfactor for the Y-axis
	 */
	public void setZoom(float xZoom, float yZoom)
	{
		chTimeZoom=xZoom + chTimeZoomOld;
		chVoltZoom=yZoom + chVoltZoomOld;
		
		if(chTimeZoom < 0-screenWidth)
			chTimeZoom=0-screenWidth;
		if(chVoltZoom < 0-screenHeight)
			chVoltZoom=0-screenHeight;
	}
	
	/**
	 * Save old zoomfactors to use when further zooming
	 */
	public void releaseZoom()
	{
		chTimeZoomOld=chTimeZoom;
		chVoltZoomOld=chVoltZoom;
	}
	
	/**
	 * Set dimensions of the canvas on which the channel is drawn
	 * 
	 * @param width Width of the canvas
	 * @param height Height of the canvas
	 */
	public void setDimensions(float width,float height)
	{
		screenWidth=width;
		screenHeight=height;
	}
	
	/**
	 * Get dimensions of canvas
	 * @return width of canvas
	 */
	public float getWidth()
	{
		return screenWidth;
	}
	
	/**
	 * Get dimensions of canvas
	 * @return height of canvas
	 */
	public float getHeight()
	{
		return screenHeight;
	}
	
	/**
	 * 
	 * @return Boolean, true when enabled, false when disabled
	 */
	public boolean isEnabled()
	{
		return chEnabled;
	}
	
	/**
	 * Set new dataset
	 * @param data byte[] containing samples
	 * @param numSamples number of samples
	 * @param trigger triggerAddress
	 */
	public synchronized void setNewData(int[] data, int numSamples, int trigger)
	{		
		NUM_SAMPLES=numSamples;	
		
		synchronized(mDataSet){
			mDataSet=new int[numSamples];		
			mDataSet=data;
		}
		triggerAddress=trigger;
	}

	public synchronized void appendNewData(int[] data)
	{
		//TODO test functionality
		NUM_SAMPLES=1024; //ensure 1024 samples to display
		
		int[] tmpArray = new int[NUM_SAMPLES+data.length];
		
		Log.d(TAG,"Appending new data to: " + chName);
		
		System.arraycopy(mDataSet, 0, tmpArray, 0, NUM_SAMPLES);
		System.arraycopy(data, 0, tmpArray, NUM_SAMPLES, data.length);
		System.arraycopy(tmpArray, tmpArray.length-NUM_SAMPLES, mDataSet, 0, NUM_SAMPLES);
		
	}
	
	
	/**
	 * Set position of the trigger, left, center, right
	 * @param pos 0/1/2
	 */
	public synchronized void setTriggerPos(int pos)
	{
		triggerPos=pos;
	}
	
	/**
	 * Get volt/div setting
	 * @return
	 */
	public synchronized int getVoltDiv()
	{
		return chVoltDiv;
	}
	
	/**
	 * get time/div setting
	 * @return
	 */
	public synchronized int getTimeDiv()
	{		
		return chTimeDiv;
	}
	
	/**
	 * 
	 * @return Minimum value of the current samples in Volts
	 */
	public float getMinimum()
	{
		return chMinimum-128;		
	}
	
	/**
	 * 
	 * @return Maximum value of the current samples in Volts
	 */
	public float getMaximum()
	{
		return chMaximum-128;	
	}
	
	/**
	 * 
	 * @return Peak-Peak value of the current samples in Volts
	 */
	public float getPkPk()
	{
		return chPeakpeak;
	}
	
	/**
	 * 
	 * @return Frequency of the signal in the current samples
	 */
	public float getFreq()
	{
		//float freq=calcFreq();
		calcFreq();
		return chFrequency;
	}
	
	public float getAverage()
	{
		return chAverage;
	}
	
	private void  calcFreq()
	{
		float fIndex=-1;
		float maxMag=-1;
		float[] mags;
		
		float[] fft_array=new float[NUM_SAMPLES];
		
		FFT fft = new FFT(NUM_SAMPLES,mSampleRates[chTimeDiv]);
		
		for(int i=0; i<NUM_SAMPLES;i++)
			fft_array[i] = (float)mDataSet[i]-127;
		
		fft.forward(fft_array);
		mags=fft.getSpectrum();		
		
		for(int i=0;i<mags.length;i++){	
			if(mags[i] > maxMag){
				maxMag=mags[i];
			fIndex=i;
			}
		}
		
		//Somehow the calculated frequency differs a factor 2. Compensate by dividing by 2
		chFrequency=(float)mSampleRates[chTimeDiv]*fIndex/mags.length/2;
		
//		Log.d(TAG,"Mag: " + maxMag + " Index: " + fIndex + " Freq: " + chFrequency + " SR: " + mSampleRates[chTimeDiv]);
	}

	private void calcAverage()
	{
		int total=0;
		
		for(int i=0;i<mDataSet.length;i++)
			total+=mDataSet[i]-127;
		
		chAverage=total/mDataSet.length;
	}
	
}
