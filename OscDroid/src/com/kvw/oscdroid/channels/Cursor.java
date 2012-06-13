package com.kvw.oscdroid.channels;

import com.kvw.oscdroid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

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
	
	public Cursor(boolean vertical, boolean minimum, Context context)
	{
		isVertical=vertical;
		isMinimum=minimum;
		mParentContext = context;
		
		curPaint=new Paint();
		curPaint.setColor(Color.BLUE);
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
	
	public synchronized void setPos(int pos)
	{
		currentPos=pos;
	}
	
	public float getPos()
	{
		return currentPos;
	}

	
	public synchronized void setEnabled(boolean enable)
	{
		enabled=enable;
	}
	
}
