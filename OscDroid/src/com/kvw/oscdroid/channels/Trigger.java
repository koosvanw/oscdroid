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

public class Trigger {

	private final Handler mHandler;
	private final Context mParentContext;
	public final static int TRIG_LVL_CHANGED = 0xD3;
	public final static int TRIG_POS_CHANGED = 0xD4;
	
	
	private int trigPosition = 1; //0=left, 1=center, 2=right
	private int trigLevel=128;
	private boolean risingEdge=true;
	
	private Rect rect;
	private Bitmap tr;
	private Rect rect2;
	private Bitmap tr2;
	
	float horOffset;
	float vertOffset;
	
	private final Paint trigPaint;
	
	public Trigger(Handler handler, Context context){
		mHandler=handler;
		mParentContext = context;
		trigPaint=new Paint();
		trigPaint.setColor(Color.BLUE);
		trigPaint.setStrokeWidth(1);
		
		
		tr = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.trigvert);
		
		
		tr2 = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.trighor);
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
			horOffset=width/2-4;
			break;
		case 2: //right
			horOffset=width/5*4;
			break;
		}
				
		canvas.drawLine(horOffset, 0, horOffset, height, trigPaint);
		rect = new Rect((int)horOffset-12, 0, (int)horOffset+12, 35);
		canvas.drawBitmap(tr,null,rect, trigPaint);
		
		canvas.drawLine(0, vertOffset, width, vertOffset, trigPaint);
		rect2 = new Rect(width-35,(int)vertOffset-12,width,(int)vertOffset+12);
		canvas.drawBitmap(tr2, null, rect2,trigPaint);

		if(vertOffset>30)
			canvas.drawText("Lvl: " + trigLevel, width-70, vertOffset-15, trigPaint);
		else canvas.drawText("Lvl: " + trigLevel, width-70, vertOffset+25, trigPaint);
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
	
	public boolean isRising()
	{
		return risingEdge;
	}
	
	public synchronized void setRising(boolean rising)
	{
		risingEdge=rising;
	}
	
	public synchronized void setLevel(int lvl)
	{
		if(lvl>=0 && lvl<=255)
			trigLevel=lvl;
		else if(lvl>255)
			trigLevel=255;
		else if(lvl<0)
			trigLevel=0;
	}
	
	public synchronized void setPos(int pos)
	{
		trigPosition=pos;
	}
}
