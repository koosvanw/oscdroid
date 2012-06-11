package com.kvw.oscdroid.channels;

import android.graphics.Canvas;

public class Cursor {

	private final boolean isVertical;
	private final int number;
	
	
	
	private boolean enabled=false;
	
	public Cursor(boolean vertical, int num)
	{
		isVertical=vertical;
		number=num;
	}
	
	public void drawCursor(Canvas canvas)
	{
		if(!enabled)
			return;
		
		int width=canvas.getWidth();
		int height=canvas.getHeight();
		
	}
	
	public synchronized void setEnabled(boolean enable)
	{
		enabled=enable;
	}
	
}
