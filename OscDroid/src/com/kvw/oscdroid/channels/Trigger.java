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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;

import com.kvw.oscdroid.R;

/**
 * 
 * @author K. van Wijk
 *
 */
public class Trigger {

	private final Handler mHandler;
	private final Context mParentContext;
	public final static int TRIG_LVL_CHANGED = 0xD3;
	public final static int TRIG_POS_CHANGED = 0xD4;
	
	
	private int trigPosition = 1; //0=left, 1=center, 2=right
	private int trigLevel=128;
	private int trigSource=1;
	private boolean risingEdge=true;
	
	private Rect rect;
	private Bitmap trigLvlRis;
	private Bitmap trigLvlFall;
	private Rect rect2;
	private Bitmap trigPosCh1;
	private Bitmap trigPosCh2;
	
	float horOffset;
	float vertOffset;
	
	private final Paint trigPaint;
	
	/**
	 * Constructor
	 * @param handler Handler to return messages to main activity
	 * @param context Parent context, necessary to retrieve resources
	 */
	public Trigger(Handler handler, Context context){
		mHandler=handler;
		mParentContext = context;
		trigPaint=new Paint();
		trigPaint.setColor(Color.BLUE);
		trigPaint.setStrokeWidth(1);
		
		trigLvlRis = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.trig_rising);		
		trigLvlFall = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.trig_falling);
		trigPosCh1 = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.trig_ch1);
		trigPosCh2 = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.trig_ch2);
	}
	
	/**
	 * Draw the trigger settings
	 * 
	 * @param canvas Canvas on which to draw the trigger
	 */
	public void drawTrigger(Canvas canvas){
		int width=canvas.getWidth();
		int height=canvas.getHeight();
		
		// Calculate drawing positions
		horOffset=width/2;
		vertOffset=(height*(255-trigLevel))/255;
		
		switch(trigPosition){
		case 0: //left
			horOffset=width/5-2;
			break;
		case 1: //center
			horOffset=width/2-4;
			break;
		case 2: //right
			horOffset=width/5*4-4;
			break;
		}
		
		// Draw the lines and bitmaps
		canvas.drawLine(horOffset, 0, horOffset, height, trigPaint);
		rect = new Rect((int)horOffset-12, 0, (int)horOffset+12, 35);
		if(trigSource==1)
			canvas.drawBitmap(trigPosCh1, null, rect,trigPaint);
		else if(trigSource==2)
			canvas.drawBitmap(trigPosCh2, null, rect,trigPaint);
		
		canvas.drawLine(0, vertOffset, width, vertOffset, trigPaint);
		rect2 = new Rect(width-35,(int)vertOffset-12,width,(int)vertOffset+12);
		
		if(risingEdge)
			canvas.drawBitmap(trigLvlRis,null,rect2, trigPaint);
		else if(!risingEdge)
			canvas.drawBitmap(trigLvlFall,null,rect2, trigPaint);

		// TODO calculate correct triggerLevel with voltage conversion
		if(vertOffset>30)
			canvas.drawText("Lvl: " + trigLevel, width-70, vertOffset-15, trigPaint);
		else canvas.drawText("Lvl: " + trigLevel, width-70, vertOffset+25, trigPaint);
	}
	
	/**
	 * Get the position of the triggerOffset
	 * @return position of triggerOffset
	 */
	public float getHorOffset()
	{
		return horOffset;
	}
	
	/**
	 * Get the position of the triggerLevel
	 * @return position of triggerLevel
	 */
	public float getVertOffset()
	{
		return vertOffset;
	}
	
	/**
	 * Get the triggerLevel
	 * @return triggerLevel, normalized to 8-bits value
	 */
	public int getLevel()
	{
		return trigLevel;
	}

	/**
	 * Get triggerPosition, left, center or right
	 * @return 0/1/2
	 */
	public int getPos()
	{
		return trigPosition;
	}
	
	/**
	 * Get rising/falling edge setting
	 * @return
	 */
	public boolean isRising()
	{
		return risingEdge;
	}
	
	/**
	 * Set rising/falling edge
	 * @param rising
	 */
	public synchronized void setRising(boolean rising)
	{
		risingEdge=rising;
	}
	
	/**
	 * Set trigger level
	 * @param lvl
	 */
	public synchronized void setLevel(int lvl)
	{
		if(lvl>=0 && lvl<=255)
			trigLevel=lvl;
		else if(lvl>255)
			trigLevel=255;
		else if(lvl<0)
			trigLevel=0;
	}
	
	/**
	 * Set trigger source; Used to determine trigger graphics
	 * @param source 1=ch1, 2=ch2
	 */
	public synchronized void setSource(int source)
	{
		trigSource=source;
	}
	
	/**
	 * Set trigger position
	 * @param pos
	 */
	public synchronized void setPos(int pos)
	{
		trigPosition=pos;
	}
}
