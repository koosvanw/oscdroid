package com.kvw.oscdroid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;

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

	public void drawGrid(Canvas canvas){
		if (surfaceWidth==0 || surfaceHeight==0 || divWidth==0 || divHeight==0)
			return;
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



