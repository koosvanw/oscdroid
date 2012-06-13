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

package com.kvw.oscdroid.display;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * 
 * @author K. van Wijk
 *
 */
public class Grid {
  
	private static final int NUM_DIVISIONS_HOR = 10;
	private static final int NUM_DIVISIONS_VERT = 8;
	
	private int surfaceWidth=0;
	private int surfaceHeight=0;
	private int divWidth=0;
	private int divHeight=0;

	private Paint gridPenDark;
	private Paint gridPenLight;
	private DashPathEffect stroked;
	
	public boolean blackBack=true;

	/**
	 * Draw the grid based on Canvas dimensions
	 * 
	 * @param canvas Canvas to draw the grid on
	 */
	public void drawGrid(Canvas canvas){
		if (surfaceWidth==0 || surfaceHeight==0 || divWidth==0 || divHeight==0)
			return;
		// Draw vertical gridlines based on NumDivisions. Used paint based on background color
		for (int i=1; i<NUM_DIVISIONS_HOR; i++)
		{
			if (i==(NUM_DIVISIONS_HOR/2) && blackBack){
				gridPenLight.setStrokeWidth(2f);
				canvas.drawLine(i*divWidth, 0, i*divWidth, surfaceHeight, gridPenLight);
			} else if(i==(NUM_DIVISIONS_HOR/2) &! blackBack){
				gridPenDark.setStrokeWidth(2f);
				canvas.drawLine(i*divWidth, 0, i*divWidth, surfaceHeight, gridPenDark);
			} else if(i!=(NUM_DIVISIONS_HOR/2) && blackBack){
				gridPenDark.setStrokeWidth(1.2f);
				canvas.drawLine(i*divWidth, 0, i*divWidth, surfaceHeight, gridPenDark);
			} else if(i!=(NUM_DIVISIONS_HOR/2) &! blackBack){
				gridPenLight.setStrokeWidth(1.2f);
				canvas.drawLine(i*divWidth, 0, i*divWidth, surfaceHeight, gridPenLight);
			}
		}
		
		// Draw horizontal gridlines, based on NumDivisions and background color
		for (int i=1; i<NUM_DIVISIONS_VERT; i++)
		{
			if (i==(NUM_DIVISIONS_VERT/2) && blackBack){
				gridPenLight.setStrokeWidth(2f);
				canvas.drawLine(0, i*divHeight, surfaceWidth, i*divHeight, gridPenLight);
			} else if(i==(NUM_DIVISIONS_VERT/2) &! blackBack){
				gridPenDark.setStrokeWidth(2f);
				canvas.drawLine(0, i*divHeight, surfaceWidth, i*divHeight, gridPenDark);
			} else if(i!=(NUM_DIVISIONS_VERT/2) && blackBack){
				gridPenDark.setStrokeWidth(1.2f);
				canvas.drawLine(0, i*divHeight, surfaceWidth, i*divHeight, gridPenDark);
			} else if(i!=(NUM_DIVISIONS_VERT/2) &! blackBack){
				gridPenLight.setStrokeWidth(1.2f);
				canvas.drawLine(0, i*divHeight, surfaceWidth, i*divHeight, gridPenLight);
			}
		}
	} 
	
	/**
	 * Constructor for the grid. Set dimensions and create Paints for normal and 
	 * center gridlines
	 * 
	 * @param xlength Width of the canvas
	 * @param ylength Height of the canvas
	 */
	public Grid(int xlength, int ylength) {	
		divWidth = xlength/NUM_DIVISIONS_HOR;
		divHeight = ylength/NUM_DIVISIONS_VERT;
		
		surfaceWidth=xlength;
		surfaceHeight=ylength;
		
		stroked = new DashPathEffect(new float[]{4f,12f},0);
		
		gridPenLight=new Paint();
		gridPenLight.setColor(Color.GRAY);
		gridPenLight.setStrokeWidth(1f);
		gridPenLight.setStyle(Style.STROKE);
		gridPenLight.setPathEffect(stroked);
		
		gridPenDark=new Paint();
		gridPenDark.setColor(Color.DKGRAY);
		gridPenDark.setStrokeWidth(1f);
		gridPenDark.setStyle(Style.STROKE);
		gridPenDark.setPathEffect(stroked);
	} 
	

}



