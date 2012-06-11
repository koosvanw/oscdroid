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
import android.os.Handler;

public class Trigger {

	private final Handler mHandler;
	public final static int TRIG_LVL_CHANGED = 0xD3;
	public final static int TRIG_POS_CHANGED = 0xD4;
	
	
	private int trigPosition = 1; //0=left, 1=center, 2=right
	private int trigLevel=128;
	
	float horOffset;
	float vertOffset;
	
	private final Paint trigPaint;
	
	public Trigger(Handler handler){
		mHandler=handler;
		
		trigPaint=new Paint();
		trigPaint.setColor(Color.BLUE);
		trigPaint.setStrokeWidth(1);		
	}
	
	public void drawTrigger(Canvas canvas){
		int width=canvas.getWidth();
		int height=canvas.getHeight();
		
		horOffset=width/2;
		vertOffset=(height*(255-trigLevel))/255;
		
		switch(trigPosition){
		case 0: //left
			horOffset=width/5;
			break;
		case 1: //center
			horOffset=width/2;
			break;
		case 2: //right
			horOffset=width/5*4;
			break;
		}
				
		canvas.drawLine(horOffset, 0, horOffset, height, trigPaint);
		canvas.drawRect(horOffset-10, 0, horOffset+10, 30, trigPaint);
		canvas.drawLine(0, vertOffset, width, vertOffset, trigPaint);
		canvas.drawRect(width-30, vertOffset-10, width, vertOffset+10, trigPaint);
	}
	
	public float getHorOffset()
	{
		return horOffset;
	}
	
	public float getVertOffset()
	{
		return vertOffset;
	}
	
	public int getLevel()
	{
		return trigLevel;
	}

	public int getPos()
	{
		return trigPosition;
	}
	
	public synchronized void setLevel(int lvl)
	{
		trigLevel=lvl;
	}
	
	public synchronized void setPos(int pos)
	{
		trigPosition=pos;
	}
}
