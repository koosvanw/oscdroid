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
import android.view.SurfaceHolder;

public class DrawThread extends Thread{
	
	private SurfaceHolder mSurface;
	private OscDroidSurfaceView mSurfaceView;
	private boolean mRun = false;
	
	/**
	 * Constructor of the DrawThread class
	 * 
	 * @param surfaceHolder surfaceHolder of the class, for reference
	 * @param surface SurfaceView to draw on
	 */
	public DrawThread(SurfaceHolder surfaceHolder, OscDroidSurfaceView surface)
	{
		mSurface=surfaceHolder;
		mSurfaceView=surface;
	}
	
	/**
	 * Set thread to running
	 * 
	 * @param run True to enable, false to disable
	 */
	public void setRunning(boolean run)
	{
		mRun=run;
	}
	
	
	/**
	 * Main loop of the thread
	 */
	@Override
	public void run(){
		Canvas canvas;
		while(mRun){
			canvas = null;
			//mSurfaceView.time++;
            try {
                canvas = mSurface.lockCanvas(null);
                synchronized (mSurface) {
                    mSurfaceView.onDraw(canvas);
                }
            }catch(Exception e){} 
            finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (canvas != null) {
                    mSurface.unlockCanvasAndPost(canvas);
                }
            }
		}
	}
	
}
