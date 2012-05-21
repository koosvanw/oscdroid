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
	
	private int chColor;
	private int chVoltDiv;
	private int chTimeDiv;
	private int chMaximum;
	private int chMinimum;
	private int chPeakpeak;
	
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

	public AnalogChannel(Handler handler, String name)
	{
		//TODO initialize channel
		mHandler = handler;
		chName=name;
		
		mDataSet = new int[2048];
		
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
	
	public void drawChannel(Canvas canvas)
	{
		//TODO implement drawing of the channel here
		Path chPath=new Path();
		
		for(int i=0; i<mDataSet.length;i+=2){
			float x = (screenWidth+chTimeZoom)/mDataSet.length * i + chTimeOffset;
			float y = (screenHeight+chVoltZoom)/256*(255-mDataSet[i]+chVoltOffset);
			
			
			//Log.v(TAG,"y: " + String.valueOf(y) + " H: "+String.valueOf(screenHeight));
			
			if(i==0)
				chPath.moveTo(x, y);
			
			chPath.lineTo(x, y);
			
			//canvas.drawPoint(x, y, chPaint);
		}
		
		canvas.drawPath(chPath, chPaint);
		
	}
	
	public void setColor(int color)
	{
		chColor=color;
		chPaint.setColor(color);
	}
	
	
	public void setVoltDivs(int divs)
	{
		chVoltDiv = divs;
	}
	
	
	public void setTimeDivs(int divs)
	{
		chTimeDiv=divs;
	}
	
	
	public void setEnabled(boolean enabled)
	{
		chEnabled=enabled;
	}
	
	
	public void setOffset(float xOffset,float yOffset)
	{
		chTimeOffset+=xOffset/2;
		chVoltOffset+=yOffset/2;
	}
	
	
	public void setZoom(float xZoom, float yZoom)
	{
		chTimeZoom=xZoom + chTimeZoomOld;
		chVoltZoom=yZoom + chVoltZoomOld;
		
		if(chTimeZoom < 0-screenWidth)
			chTimeZoom=0-screenWidth;
		if(chVoltZoom < 0-screenHeight)
			chVoltZoom=0-screenHeight;
	}
	
	public void releaseZoom()
	{
		chTimeZoomOld=chTimeZoom;
		chVoltZoomOld=chVoltZoom;
	}
	
	public void setDimensions(float width,float height)
	{
		screenWidth=width;
		screenHeight=height;
	}
	
	
	public boolean isEnabled()
	{
		return chEnabled;
	}
	
	
	public float getMinimum()
	{
		float min=calcMin();
		return min;		
	}
	
	
	public float getMaximum()
	{
		float max=calcMax();
		return max;
	}
	
	
	public float getPkPk()
	{
		float pkpk=calcPkPk();
		return pkpk;
	}
	
	
	public float getFreq()
	{
		float freq=calcFreq();
		return freq;
	}
	
	
	private float calcMin()
	{
		//TODO
		return 0;
	}
	
	
	private float calcMax()
	{
		//TODO
		return 0;
	}
	
	
	private float calcPkPk()
	{
		//TODO
		return 0;
	}
	
	
	private float calcFreq()
	{
		//TODO
		return 0;
	}
}
