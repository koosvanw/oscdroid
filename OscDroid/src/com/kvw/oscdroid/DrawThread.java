package com.kvw.oscdroid;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class DrawThread extends Thread{
	
	private SurfaceHolder mSurface;
	private OscDroidSurfaceView mSurfaceView;
	private boolean mRun = false;
	
	public DrawThread(SurfaceHolder surfaceHolder, OscDroidSurfaceView surface)
	{
		mSurface=surfaceHolder;
		mSurfaceView=surface;
	}
	
	public void setRunning(boolean run)
	{
		mRun=run;
	}
	
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
