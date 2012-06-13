/** This file is part of OscDroid for Android.
 *
 * Copyright (C) 2012 E. Hoogma, Enschede, The Netherlands
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

import com.kvw.oscdroid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 
 * @author E. Hoogma
 *
 */
public class Cursor {

	private final boolean isVertical;
	private final boolean isMinimum;
	private final Context mParentContext;
	
	private int posmin = 100;
	private int posmax = 156;
	private int currentPos;
	
	private Rect rect;
	private Bitmap cursfig;
	
	private boolean enabled=false;
	private final Paint curPaint;
	
	/**
	 * Constructor
	 * @param vertical Indicate if the cursor is vertical or not
	 * @param minimum Indicate if this is the minimum cursor (cursor 1 or 2)
	 * @param context Parent Context, necessary to retrieve resources
	 */
	public Cursor(boolean vertical, boolean minimum, Context context)
	{
		isVertical=vertical;
		isMinimum=minimum;
		mParentContext = context;
		
		curPaint=new Paint();
		curPaint.setColor(Color.MAGENTA);
		curPaint.setStrokeWidth(1);
		
		if(isVertical && isMinimum)
			cursfig = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.curst1);
		else if(isVertical && !isMinimum)
			cursfig = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.curst2);
		else if(!isVertical && isMinimum)
			cursfig = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.cursv1);
		else if(!isVertical && !isMinimum)
			cursfig = BitmapFactory.decodeResource(mParentContext.getResources(), R.drawable.cursv2);
		
		if(isMinimum)
			currentPos=posmin;
		else
			currentPos=posmax;
		
	}
	
	/**
	 * Draw the cursor
	 * 
	 * @param canvas Canvas on which to draw the cursor
	 */
	public void drawCursor(Canvas canvas)
	{
		if(!enabled)
			return;
		
		int width=canvas.getWidth();
		int height=canvas.getHeight();
		
		if(isVertical)
		{
			canvas.drawLine(currentPos, 0, currentPos, height, curPaint);
			rect = new Rect((int)currentPos-12, 0, (int)currentPos+12, 35);
			canvas.drawBitmap(cursfig,null,rect, curPaint);
		}
		else
		{
			canvas.drawLine(0, currentPos, width, currentPos, curPaint);
			rect = new Rect(width-35, (int)currentPos-12, width, (int)currentPos+12);
			canvas.drawBitmap(cursfig,null,rect, curPaint);
		}
	}
	
	/**
	 * Set the position of the cursor
	 * 
	 * @param pos Position of the cursor on the surface. Position check should be done before calling
	 */
	public synchronized void setPos(int pos)
	{
		currentPos=pos;
	}
	
	/**
	 * Get the position of the cursor
	 * 
	 * @return float with position on the surface
	 */
	public float getPos()
	{
		return currentPos;
	}

	/**
	 * Switch cursor on/off
	 * 
	 * @param enable 
	 */
	public synchronized void setEnabled(boolean enable)
	{
		enabled=enable;
	}
	
}
