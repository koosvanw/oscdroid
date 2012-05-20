package com.kvw.oscdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OscDroidSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	
	/** STATIC VALUES */
	private final static String TAG="OscDroidSurfaceView";
	
	private final static int CHANNEL1 = 0;
	private final static int CHANNEL2 = 1;
	private final static int LOGICPROBE = 2;
	
	private final static int SINGLETOUCH=0;
	private final static int MULTITOUCH=1;

	private final static int TRUE_OFFSET=20;
	private final static int FALSE_OFFSET=30;
	
	private Handler mHandler;
	
	private AnalogChannel channel1;
	private AnalogChannel channel2;
	
	private int surfaceWidth=0;
	private int surfaceHeight=0;
	private int touchMode=SINGLETOUCH;
	
	private int backgroundColor=Color.BLACK;
	
	public int SELECTED_CHANNEL=-1;
	
	
	private float mPreviousX;
	private float mPreviousY;
	private float mOffsetX;
	private float mOffsetY;
	
	private float tmpX=100;
	private float tmpY=100;
	
    private float oldDist;
    private float newDist;
	
	public int time=0;
	
	private Grid mGrid;
	
	private DrawThread drawThread;
	Paint tmpPaint = new Paint();
	
	public OscDroidSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		drawThread = new DrawThread(getHolder(),this);
		setFocusable(true);
	}
	
	public void setHandler(Handler handler){
		mHandler = handler;
	}
	
	public void setColors(int ch1Color, int ch2Color, int logColor, int backColor)
	{
		
		backgroundColor=backColor;
		
		
		
		//TODO set channel colors

	}
	
	public void addChannel(AnalogChannel chan,float width, float height)
	{
		if(channel1==null)
			channel1=chan;
		else if(channel1!=null)
			channel2=chan;
		else return;
	}
	
	private float spacing(MotionEvent event) {
 	   float x = event.getX(0) - event.getX(1);
 	   float y = event.getY(0) - event.getY(1);
 	   return FloatMath.sqrt(x * x + y * y);
 	}
	
	private int isVertical(MotionEvent event)
	{
		int isVert = 0;
		float touchOffMin = 90f;
		float touchOffMax = 20f;
		
		float x0=event.getX(0);
		float x1=event.getX(1);
		float y0=event.getY(0);
		float y1=event.getY(1);
		float spaceX;
		float spaceY;
		
		if (x0 > x1) spaceX=x0-x1;
		else spaceX=x1-x0;
		
		if (y0 > y1) spaceY=y0-y1;
		else spaceY=y1-y0;
		
		if(spaceY < touchOffMin && spaceX > touchOffMax) isVert=2; //horizontal pinch/zoom
		
		if(spaceX < touchOffMin && spaceY > touchOffMax) isVert=1; //vertical pinch/zoom

		
		return isVert;
	}

	private void changeVoltZoom(MotionEvent event)
	{
		//TODO set zoom, not volt div!!!!

		float zoomFactor = newDist-oldDist;		
		
		switch(SELECTED_CHANNEL){
			case CHANNEL1:
				if(channel1.isEnabled())
				channel1.setZoom(0, zoomFactor);
				break;
			case CHANNEL2:
				if(channel2.isEnabled())
				channel2.setZoom(0, zoomFactor);
				break;
			case LOGICPROBE:
				
				break;
			default:
					
				break;
		}				
	}
	
	private void changeTimeZoom(MotionEvent event)
	{
		//TODO should zoom, not set time div!!!
//		time = "Horizontal " + String.valueOf(newDist/oldDist);
		
		float zoomFactor=newDist-oldDist;
		
		switch(SELECTED_CHANNEL){
		case CHANNEL1:
			if(channel1.isEnabled())
			channel1.setZoom(zoomFactor, 0);
			break;
		case CHANNEL2:
			if(channel2.isEnabled())
			channel2.setZoom(zoomFactor, 0);
			break;
		case LOGICPROBE:
			
			break;
		default:
				
			break;
	}
		
	}
	
	@Override
	public void onDraw(Canvas canvas){
		canvas.drawColor(backgroundColor);
		
		if (mGrid != null)
			mGrid.drawGrid(canvas);
		
		if(channel1!=null)
			if(channel1.isEnabled())
				channel1.drawChannel(canvas);
		if(channel2!=null)
			if(channel2.isEnabled())
				channel2.drawChannel(canvas);
		
		tmpPaint.setColor(Color.RED);
		tmpPaint.setStrokeWidth(1f);
		canvas.drawCircle(tmpX, tmpY, 10f, tmpPaint);
		canvas.drawText(String.valueOf(time), 20, 100, tmpPaint);
		time++;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		
		switch(event.getActionMasked()){
		case MotionEvent.ACTION_DOWN:
			mOffsetX=x;
			mOffsetY=y;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist=spacing(event);
			
			if(oldDist > 10f)
				touchMode=MULTITOUCH;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			touchMode=SINGLETOUCH;
			if(SELECTED_CHANNEL==CHANNEL1)
				channel1.releaseZoom();
			if(SELECTED_CHANNEL==CHANNEL2)
				channel2.releaseZoom();
			break;
		case MotionEvent.ACTION_MOVE:
			if(touchMode==SINGLETOUCH){
				tmpX=x;
				tmpY=y;
				
				float spacingX = (x-mOffsetX);
				spacingX = FloatMath.sqrt(spacingX*spacingX);
				float spacingY = (y-mOffsetY);
				spacingY = FloatMath.sqrt(spacingY*spacingY);
				
				switch(SELECTED_CHANNEL){
				case CHANNEL1:
					if(channel1.isEnabled())
					if(spacingX < FALSE_OFFSET && spacingY > TRUE_OFFSET)
						channel1.setOffset(0, y-mPreviousY);
					
					if(channel1.isEnabled())
					if(spacingY<FALSE_OFFSET && spacingX>TRUE_OFFSET)
						channel1.setOffset(x-mPreviousX, 0);
					break;
				case CHANNEL2:
					if(channel2.isEnabled())
					if(spacingX < FALSE_OFFSET && spacingY > TRUE_OFFSET)
						channel2.setOffset(0, y-mPreviousY);
					
					if(channel2.isEnabled())
					if(spacingY<FALSE_OFFSET && spacingX>TRUE_OFFSET)
						channel2.setOffset(x-mPreviousX, 0);
					break;
				case LOGICPROBE:
					break;
				}
				
				//TODO implement moving/offset of channels
				//TODO implement moving of cursors (hor+vert)
				//TODO implement changing trigger level
			} 
			else if (touchMode==MULTITOUCH){
				newDist=spacing(event);
				//TODO implement zooming, not volt divs changing!!!!
				if (isVertical(event)==1) changeVoltZoom(event);
				else if (isVertical(event)==2) changeTimeZoom(event);
//				else time="Not valid!";
			}			
			break;
		}
		mPreviousX=x;
		mPreviousY=y;
		
		return true;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		surfaceWidth=width;
		surfaceHeight=height;
		
		if(channel1!=null)
			channel1.setDimensions(width,height);
		if(channel2!=null)
			channel2.setDimensions(width,height);
		
		mGrid = new Grid(width,height);
		if(backgroundColor==Color.WHITE)
			mGrid.blackBack=false;
		else if (backgroundColor==Color.BLACK)
			mGrid.blackBack=true;
		
		Log.v(TAG,"SurfaceChanged; black= "+mGrid.blackBack 
				+ " ch1: "+channel1.isEnabled() + " w: "+String.valueOf(width)
				+" h: " + String.valueOf(height));
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(drawThread.getState().equals(Thread.State.TERMINATED))
			drawThread = new DrawThread(getHolder(),this);
		
		drawThread.setRunning(true);
		drawThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
	    drawThread.setRunning(false);
	    
	    while (retry) {
	        try {
	            drawThread.join();
	            retry = false;
	        } catch (InterruptedException e) {
	            // we will try it again and again...
	        }
	    }		
	}
}