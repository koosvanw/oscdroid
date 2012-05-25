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

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;

public class AnalogChannel {
	
	private final String chName;
	private final String TAG="CHann";
	
	private int NUM_SAMPLES=1024;
	
	private int chColor;
	private int chVoltDiv;
	private int chTimeDiv;
	private float chMaximum;
	private float chMinimum;
	private float chPeakpeak;
	
	private float screenWidth;
	private float screenHeight;
	
	private float chVoltOffset=0;
	private float chTimeOffset=0;
	private float chVoltZoom=0;
	private float chTimeZoom=0;
	
	private float chVoltZoomOld=0;
	private float chTimeZoomOld=0;
	
	private float chFrequency;
	
	private boolean chEnabled;
	private boolean chNewDataAvailable;
	
	private Paint chPaint;
	
	final Handler mHandler;
	
	private int[] mDataSet;
	
	static {System.loadLibrary("analog");}
	
	private native float calcDisplayX(int num, int numSamples, float scrnWidth, float zoomX, float offsetX);
	private native float calcDisplayY(int dataPoint, float scrnHeight, float zoomY, float offsetY);
	private native float getMax(int[] mDataSet, int numSamples);
	
	/**
	 * 
	 * @param handler Handler to send messages back to the main activity
	 * @param name Channel name
	 */
	public AnalogChannel(Handler handler, String name)
	{
		//TODO initialize channel
		mHandler = handler;
		chName=name;
		
		mDataSet = new int[NUM_SAMPLES];
		
		Random random = new Random();
		for (int i=0;i<mDataSet.length;i++)
		{
			mDataSet[i]=random.nextInt(255);
			//mDataSet[i]=1;
			//mDataSet[i]=255;
		}
		
		chColor=Color.BLUE;
		chPaint=new Paint();
		chPaint.setStrokeWidth(1f);
		chPaint.setStyle(Style.STROKE);
		chPaint.setColor(chColor);
		chPaint.setDither(false);
	}
	
	/**
	 * Function that implements drawing of the channel.
	 * @param canvas Canvas on which the channel is to be drawn
	 */
	public void drawChannel(Canvas canvas)
	{
		Path chPath=new Path();
		float max = 0;
		float min = 255;
		
		for(int i=0; i<mDataSet.length;i++){
//			float x = (screenWidth+chTimeZoom)/NUM_SAMPLES*i+chTimeOffset;
//			float y = (screenHeight+chVoltZoom)/256*(255-mDataSet[i])+chVoltOffset;
			
			float x = calcDisplayX(i,NUM_SAMPLES,screenWidth,chTimeZoom,chTimeOffset);
			float y = calcDisplayY(mDataSet[i],screenHeight,chVoltZoom,chVoltOffset);
			if (mDataSet[i] > max) max = mDataSet[i];
			if (mDataSet[i]<min) min = mDataSet[i];
			
			//Log.v(TAG,"y: " + String.valueOf(y) + " H: "+String.valueOf(screenHeight));
			
			if(i==0)
				chPath.moveTo(x,y);
			
			chPath.lineTo(x,y);
			
			//canvas.drawPoint(x, y, chPaint);
		}
		
		chMaximum=max;
		chMinimum=min;
		chPeakpeak=max-min;
		
		canvas.drawPath(chPath, chPaint);
		
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
	public void setVoltDivs(int divs)
	{
		chVoltDiv = divs;
	}
	
	/**
	 * 
	 * @param divs Integer representing the time divs
	 */
	public void setTimeDivs(int divs)
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
	 * 
	 * @return Boolean, true when enabled, false when disabled
	 */
	public boolean isEnabled()
	{
		return chEnabled;
	}
	
	/**
	 * 
	 * @return Minimum value of the current samples in Volts
	 */
	public float getMinimum()
	{
		float min=calcMin();
		return min;		
	}
	
	/**
	 * 
	 * @return Maximum value of the current samples in Volts
	 */
	public float getMaximum()
	{
		
		return chMaximum;
		
	}
	
	/**
	 * 
	 * @return Peak-Peak value of the current samples in Volts
	 */
	public float getPkPk()
	{
		float pkpk=calcPkPk();
		return pkpk;
	}
	
	/**
	 * 
	 * @return Frequency of the signal in the current samples
	 */
	public float getFreq()
	{
		float freq=calcFreq();
		return freq;
	}
	
	/**
	 * 
	 * @return Calculated minimum of the current samples in Volts
	 */
	private float calcMin()
	{
		//TODO
		return 0;
	}
	
	/**
	 * 
	 * @return Calculated maximum of the current samples in Volts
	 */
	private void calcMax()
	{
		chMaximum=getMax(mDataSet, mDataSet.length);
		
	}
	
	/**
	 * 
	 * @return Calculated Peak-Peak value of the current samples in Volts
	 */
	private float calcPkPk()
	{
		//TODO
		return 0;
	}
	
	/**
	 * 
	 * @return Calculated frequency of the signal in the current samples
	 */
	private float calcFreq()
	{
		//TODO
		return 0;
	}
}
