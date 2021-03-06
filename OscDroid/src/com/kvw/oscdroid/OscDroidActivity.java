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

package com.kvw.oscdroid;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kvw.oscdroid.channels.AnalogChannel;
import com.kvw.oscdroid.channels.Cursor;
import com.kvw.oscdroid.channels.Measurement;
import com.kvw.oscdroid.channels.Trigger;
import com.kvw.oscdroid.connection.ConnectionService;
import com.kvw.oscdroid.display.OscDroidSurfaceView;
import com.kvw.oscdroid.settings.SettingsActivity;

/**
 * 
 * @author K. van Wijk
 *
 */
public class OscDroidActivity extends Activity{
	
	/** Static values */
	private final static String TAG="OscDroidActivity";
	
	private final static int CHANNEL1 = 0;
	private final static int CHANNEL2 = 1;
	private final static int LOGICPROBE = 2;
	private final static int RISING_EDGE=0;
	private final static int FALLING_EDGE=1;
	
	private final static int GET_SETTINGS=20;
	
	private int CURRENT_MODE=1; // DEFAULT SINGLESHOT
	
	protected PowerManager.WakeLock mWakeLock;
	
	/** Interface objects */
	private OscDroidSurfaceView oscSurface;
	
	private TextView chan1;
    private TextView chan2;
    private TextView logChan;
    
    private TextView ch1Div;
    private TextView ch2Div;
    private TextView timeDiv;
    
    private TextView measure1;
    private TextView measure2;
    private TextView measure3;
    private TextView measure4;
    private TextView measure5;
    private TextView measure6;
    private TextView measure7;
    private TextView measure8;
    
    private Button channels;
    private Button selectMeasurements;
    private Button TriggerBtn;
    private Button runModeBtn;
    private Button getDataBtn;
    
    private RadioGroup chan=null;
    private RadioGroup type=null;
    
    private OnClickListener chan1Selected;
    private OnClickListener chan2Selected;
    private OnClickListener logSelected;
    
    private OnClickListener ch1DivClicked;
    private OnClickListener ch2DivClicked;
    private OnClickListener timeDivClicked;
    
    private OnClickListener selectChannel;
    private OnClickListener selectMeas;
    private OnClickListener selectTrigger;
    private OnClickListener selectRunMode;
    private OnClickListener getDataClicked;
    
    private OnClickListener measurementClicked;
    
    /** Class variables and elements */
    private boolean[] enabledChannels={false,false,false};
    private AlertDialog optionsDialog;
    
    private ConnectionService connectionService=null;
    
    /** User preferences */
    private int connectionType;
    private int ch1Color;
    private int ch2Color;
    private int logColor;
    private int overlayColor;
    private int backColor;
    
    /** Scope units */
    
    private AnalogChannel channel1;
    private AnalogChannel channel2;
    
    private Trigger mTrigger;
    
    private Cursor timeCursor1;
    private Cursor timeCursor2;
    private Cursor voltCursor1;
    private Cursor voltCursor2;
    
    private int TRIG_SOURCE=CHANNEL2;
    private int TRIG_MODE=RISING_EDGE;
    
    private int SELECTED_CHANNEL = -1;
    private int SELECTED_DIV_CH1=7;
    private int SELECTED_DIV_CH2=7;
    
    private int SELECTED_DIV_TIME=7;
    
    private String[] VOLT_DIVS;    
	private String[] TIME_DIVS;
	private String[] MEASUREMENTS;
	
	private Measurement measure;
	

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
        channel1=new AnalogChannel(mHandler,"CH1");
        channel1.setColor(ch1Color);
        channel1.setVoltDivs(SELECTED_DIV_CH1);
        channel1.setTimeDivs(SELECTED_DIV_TIME);
        
        channel2=new AnalogChannel(mHandler,"CH2");
        channel2.setColor(ch2Color);
        channel2.setVoltDivs(SELECTED_DIV_CH1);
        channel2.setTimeDivs(SELECTED_DIV_TIME);
        
        measure=new Measurement(mHandler);
        measure.setRunning(true);
        measure.start();
        
        VOLT_DIVS = getResources().getStringArray(R.array.volt_divs);
        TIME_DIVS  = getResources().getStringArray(R.array.time_divs);
        MEASUREMENTS = getResources().getStringArray(R.array.measurements);
        
        

    }
    
    @Override
    public void onDestroy()
    {
    	
    	super.onDestroy();
    	if(connectionService!=null)
    		connectionService.cleanup();
    	
    	boolean retry=true;
    	measure.setRunning(false);
    	
    	while(retry){
    		  try {
  	            measure.join();
  	            retry = false;
  	        } catch (InterruptedException e) {
  	            // we will try it again and again...
  	        }
    	}
    }
    
    /** Connect to all UI components */
    private void loadUIComponents()
    {
        oscSurface = (OscDroidSurfaceView) findViewById(R.id.mSurfaceView);
        oscSurface.setHandler(mHandler);
        oscSurface.addChannel(channel1,oscSurface.getWidth(),oscSurface.getHeight());
        oscSurface.addChannel(channel2,oscSurface.getWidth(),oscSurface.getHeight());
        oscSurface.setTrigger(mTrigger);
        oscSurface.addCursors(voltCursor1, voltCursor2, timeCursor1, timeCursor2);
        
        measure.addCursors(voltCursor1, voltCursor2, timeCursor1, timeCursor2);
        
        
        channels = (Button) findViewById(R.id.textView1);
        selectMeasurements = (Button) findViewById(R.id.measButton);
        TriggerBtn = (Button) findViewById(R.id.triggerButton);
        runModeBtn = (Button) findViewById(R.id.runmodeButton);
        getDataBtn = (Button) findViewById(R.id.getData);
        
        chan1 = (TextView) findViewById(R.id.mChan1);
        chan2 = (TextView) findViewById(R.id.mChan2);
        logChan = (TextView) findViewById(R.id.mChanLog);
        
        ch1Div = (TextView) findViewById(R.id.ch1Div);
        ch2Div = (TextView) findViewById(R.id.ch2Div);
        timeDiv = (TextView) findViewById(R.id.timediv);
        
        measure1 = (TextView) findViewById(R.id.Measure1);
        measure2 = (TextView) findViewById(R.id.Measure2);
        measure3 = (TextView) findViewById(R.id.Measure3);
        measure4 = (TextView) findViewById(R.id.Measure4);
        measure5 = (TextView) findViewById(R.id.Measure5);
        measure6 = (TextView) findViewById(R.id.Measure6);
        measure7 = (TextView) findViewById(R.id.Measure7);
        measure8 = (TextView) findViewById(R.id.Measure8);
    }
    
    /** Enable interaction for all UI components. Set onClickListeners */
    private void initUIInteraction()
    {
    	chan1Selected = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		chan1Select();
        	}
        };
        
        chan2Selected = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		chan2Select();
        	}
        };
        
        logSelected = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		logChanSelect();
        	}
        };
        
        ch1DivClicked = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		setDivVoltCh1Dialog();
        	}
        };
        
        ch2DivClicked = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		setDivVoltCh2Dialog();
        	}
        };
        
        timeDivClicked = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		setDivTimeDialog();
        	}
        };
        
        selectChannel=new OnClickListener(){
			@Override
			public void onClick(View v) {
				selectChannelDialog();
			}
        };
        
        selectMeas=new OnClickListener(){
			@Override
			public void onClick(View v) {
				selectMeasurementsDialog();
			}
        };
        
        selectTrigger=new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		selectTriggerDialog();
        	}
        };
        
        selectRunMode=new OnClickListener(){

			@Override
			public void onClick(View v) {
				selectRunModeDialog();
			}
        	
        };
        
        measurementClicked = new OnClickListener(){

			@Override
			public void onClick(View v) {
				removeMeasurement(v);
			}
        	
        };
        
        getDataClicked = new OnClickListener(){

			@Override
			public void onClick(View v) {
				connectionService.getData();
			}
        	
        };
        
        
        ch1Div.setText(getString(R.string.ch1Div)+" "+VOLT_DIVS[SELECTED_DIV_CH1]);
        ch2Div.setText(getString(R.string.ch2Div) +" " + VOLT_DIVS[SELECTED_DIV_CH2]);
        timeDiv.setText(getString(R.string.timeDiv) +" " + TIME_DIVS[SELECTED_DIV_TIME]);
        
        chan1.setOnClickListener(chan1Selected);
        chan2.setOnClickListener(chan2Selected);
        logChan.setOnClickListener(logSelected);
        
        ch1Div.setOnClickListener(ch1DivClicked);
        ch2Div.setOnClickListener(ch2DivClicked);
        timeDiv.setOnClickListener(timeDivClicked);
        
        channels.setOnClickListener(selectChannel);
        selectMeasurements.setOnClickListener(selectMeas);
        TriggerBtn.setOnClickListener(selectTrigger);
        runModeBtn.setOnClickListener(selectRunMode);
        getDataBtn.setOnClickListener(getDataClicked);
        
        measure1.setOnClickListener(measurementClicked);
        measure2.setOnClickListener(measurementClicked);
        measure3.setOnClickListener(measurementClicked);
        measure4.setOnClickListener(measurementClicked);
        measure5.setOnClickListener(measurementClicked);
        measure6.setOnClickListener(measurementClicked);
        measure7.setOnClickListener(measurementClicked);
        measure8.setOnClickListener(measurementClicked);
    }
    
    /** Set preferences, overriding the current/default settings */
    private void loadPrefs()
    {
    	// Set surface drawing colors
    	oscSurface.setColors(ch1Color, ch2Color, logColor, backColor);
    	
    	// Set overlay colors
    	chan1.setTextColor(overlayColor);
    	chan1.setBackgroundColor(backColor);
    	chan2.setTextColor(overlayColor);
    	chan2.setBackgroundColor(backColor);
    	logChan.setTextColor(overlayColor);
    	logChan.setBackgroundColor(backColor);
    	ch1Div.setTextColor(overlayColor);
    	ch1Div.setBackgroundColor(backColor);
    	ch2Div.setTextColor(overlayColor);
    	ch2Div.setBackgroundColor(backColor);
    	timeDiv.setTextColor(overlayColor);
    	timeDiv.setBackgroundColor(backColor);
    	
    	switch(SELECTED_CHANNEL){
    	case CHANNEL1:
    		chan1.setBackgroundColor(Color.GREEN);
    		break;
    	case CHANNEL2:
    		chan2.setBackgroundColor(Color.GREEN);
    		break;
    	case LOGICPROBE:
    		logChan.setBackgroundColor(Color.GREEN);
    		break;
    		
    	}
    	
    	// Set channel colors
    	channel1.setColor(ch1Color);
    	channel2.setColor(ch2Color);
    	
    }
    
    /** Read all preferences from file, set the variables */
    private void getPrefs()
    {
    	SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
    	
    	connectionType=mPrefs.getInt("connectionType", 1);
    	
    	ch1Color=mPrefs.getInt("ch1Color", Color.YELLOW);
    	ch2Color=mPrefs.getInt("ch2Color",Color.CYAN);
    	logColor=mPrefs.getInt("logColor",Color.GREEN);
    	overlayColor=mPrefs.getInt("overlayColor",Color.RED);
    	backColor=mPrefs.getInt("backColor", Color.BLACK);
    }

    
    /** Called when activity is paused. */
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    	this.mWakeLock.release();
    	SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editor = mPrefs.edit();
    	
    	editor.putInt("connectionType", connectionType);
    	
    	editor.commit();
    	
    }
     
    @Override
    protected void onStop()
    {
//    	Log.d(TAG,"stopping...");
    	
    	connectionService.cleanup();
    	connectionService=null;
    	
    	super.onStop();    	
    }
    
    @Override
    protected void onStart()
    {
    	super.onStart();    	
    	
//   	Log.d(TAG,"resumed GUI, now restarting connection");
    	
    	if(connectionService==null){
    		
    		connectionService = new ConnectionService(this,mHandler);
            UsbDevice tmpAcc = this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if(tmpAcc!=null)
            	connectionService.setDevice(tmpAcc);
    			
    		if(connectionService!=null){
    			connectionService.registerReceiver();
    			connectionService.setupConnection();
    		}
    	} 
    	else if(connectionService!= null){
    		connectionService.registerReceiver();
    		connectionService.setupConnection();
    	}
    	mTrigger=new Trigger(mHandler,this);
    	
    	timeCursor1=new Cursor(true,true,this);
    	timeCursor2=new Cursor(true,false,this);
    	voltCursor1=new Cursor(false,true,this);
    	voltCursor2=new Cursor(false,false,this);
    	
    	connectionService.setMode(1);
    	mTrigger.setSource(2);
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	setTitle(getString(R.string.app_name) + "   Status: Disconnected");
//    	Log.d(TAG,"onResume");
    	
    	loadUIComponents();
    	initUIInteraction();
    	getPrefs();
    	loadPrefs();

    	
    	final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }
    
    /** Called when options menu button is pressed */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.options_menu, menu);
        return true;
    }
    
    /** Handle options clicked item */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case R.id.help:
    		Toast.makeText(this,"Coming soon!!!",Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.settings:
    		Intent intent = new Intent(this,SettingsActivity.class);

    		intent.putExtra(SettingsActivity.CONNECTION_SETTING, connectionType);
    		
    		intent.putExtra(SettingsActivity.COLOR_CH1, ch1Color);
    		intent.putExtra(SettingsActivity.COLOR_CH2, ch2Color);
    		intent.putExtra(SettingsActivity.COLOR_LOGCH, logColor);
    		intent.putExtra(SettingsActivity.COLOR_OVERLAY, overlayColor);
    		intent.putExtra(SettingsActivity.COLOR_BACK, backColor);

    		
    		startActivityForResult(intent,GET_SETTINGS);
    		break;
    	case R.id.resetZoom:
    		switch(SELECTED_CHANNEL){
    		case CHANNEL1:
    			if(channel1!=null)
    			channel1.resetZoom();
    			break;
    		case CHANNEL2:
    			if(channel2!=null)
    			channel2.resetZoom();
    			break;
    		case LOGICPROBE:
    			break;
    		}
    		break;
    	}    	
    	return true;
    }	    
    
    /** Handle ActivityResult. */
    public void onActivityResult(int requestcode,int resultcode,Intent data)
    {
    	switch(requestcode){
    	// Settings was started and has returned
    	case GET_SETTINGS:
//    		Log.v(TAG,"settings finished");
    		
    		if(resultcode==Activity.RESULT_OK){ //Save settings, load settings
//    			Log.v(TAG,"result ok");
    			connectionType=data.getExtras().getInt(SettingsActivity.CONNECTION_SETTING);
    			ch1Color=data.getExtras().getInt(SettingsActivity.COLOR_CH1);
    			ch2Color=data.getExtras().getInt(SettingsActivity.COLOR_CH2);
    			logColor=data.getExtras().getInt(SettingsActivity.COLOR_LOGCH);
    			overlayColor=data.getExtras().getInt(SettingsActivity.COLOR_OVERLAY);
    			backColor=data.getExtras().getInt(SettingsActivity.COLOR_BACK);
    			
    			
//    			Log.v(TAG,"settings received");
    			
    			SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
    			SharedPreferences.Editor editor = mPrefs.edit();
    			
    			editor.putInt("connectionType",connectionType);
    			
    			editor.putInt("ch1Color", ch1Color);
    			editor.putInt("ch2Color", ch2Color);
    			editor.putInt("logColor", logColor);
    			editor.putInt("overlayColor", overlayColor);
    			editor.putInt("backColor", backColor);
    			
    			editor.commit();
    			loadPrefs();
    		} else //Log.v(TAG,"result: " + resultcode);
    		break;
    	}
    }
      
    /**
     * Set volt/div setting for channel1
     * @param div
     */
    private void setDivVoltCh1(int div)
    {
    	SELECTED_DIV_CH1=div;
    	ch1Div.setText(getString(R.string.ch1Div) + " " + VOLT_DIVS[div]);
    	channel1.setVoltDivs(div);
    	if(connectionService.isConnected()){
    		connectionService.setCh1Div(div);
    		connectionService.getData();
    	}
    }
    
    /**
     * Set volt/div setting for channel2
     * @param div
     */
    private void setDivVoltCh2(int div)
    {
    	SELECTED_DIV_CH2=div;
    	ch2Div.setText(getString(R.string.ch2Div) + " " + VOLT_DIVS[div]);
    	channel2.setVoltDivs(div);
    	
    	if(connectionService.isConnected()){
    		connectionService.setCh2Div(div);
    		connectionService.getData();
    	}
    }
    
    /**
     * Set time/div setting
     * @param div
     */
    private void setDivTime(int div)
    {
    	SELECTED_DIV_TIME=div;
    	timeDiv.setText(getString(R.string.timeDiv) + " " + TIME_DIVS[div]);
    	channel1.setTimeDivs(div);
    	channel2.setTimeDivs(div);
    	
    	if(connectionService.isConnected()){
    		connectionService.setTimeDiv(div);
    		connectionService.getData();
    	}
    }
        
    /** Set Volts/division for channel1 */
    private void setDivVoltCh1Dialog()
    {    	
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select div Ch1")
    		.setCancelable(true)
    		.setSingleChoiceItems(VOLT_DIVS, SELECTED_DIV_CH1, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setDivVoltCh1(which);
					dialog.dismiss();
				}
			});
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Set Volts/division for channel2 */
    private void setDivVoltCh2Dialog()
    {
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select div Ch2")
    		.setCancelable(true)
    		.setSingleChoiceItems(VOLT_DIVS, SELECTED_DIV_CH2, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setDivVoltCh2(which);
					dialog.dismiss();
				}
			});
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Set Seconds/division for all channels */
    private void setDivTimeDialog()
    {
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select time div")
    		.setCancelable(true)
    		.setSingleChoiceItems(TIME_DIVS, SELECTED_DIV_TIME, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setDivTime(which);
					dialog.dismiss();
				}
			});
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Set channel1 as selected */
    private void chan1Select()
    {
    	if(SELECTED_CHANNEL!=CHANNEL1){
	    	chan1.setBackgroundColor(Color.GREEN);
	    	chan2.setBackgroundColor(backColor);
	    	logChan.setBackgroundColor(backColor);
	    	
	    	SELECTED_CHANNEL=CHANNEL1;
	    	oscSurface.SELECTED_CHANNEL=CHANNEL1;
	    } else {
	    	chan1.setBackgroundColor(backColor);
	    	chan2.setBackgroundColor(backColor);
	    	logChan.setBackgroundColor(backColor);
	    	
	    	SELECTED_CHANNEL=-1;
	    	oscSurface.SELECTED_CHANNEL=-1;
	    }
    }
    
    /** Set channel2 as selected */
    private void chan2Select()
    {
    	if (SELECTED_CHANNEL!=CHANNEL2){
	    	chan1.setBackgroundColor(backColor);
	    	chan2.setBackgroundColor(Color.GREEN);
	    	logChan.setBackgroundColor(backColor);
	    	
	    	SELECTED_CHANNEL=CHANNEL2;
	    	oscSurface.SELECTED_CHANNEL=CHANNEL2;
    	} else {
	    	chan1.setBackgroundColor(backColor);
	    	chan2.setBackgroundColor(backColor);
	    	logChan.setBackgroundColor(backColor);
	    	
	    	SELECTED_CHANNEL=-1;
	    	oscSurface.SELECTED_CHANNEL=-1;
	    }
    }
    
    /** Set logic probe as selected */
    private void logChanSelect()
    {
    	if (SELECTED_CHANNEL!=LOGICPROBE){
    		chan1.setBackgroundColor(backColor);
	    	chan2.setBackgroundColor(backColor);
	    	logChan.setBackgroundColor(Color.GREEN);
	    	
	    	SELECTED_CHANNEL=LOGICPROBE;
	    	oscSurface.SELECTED_CHANNEL=LOGICPROBE;
	    }else{
	    	chan1.setBackgroundColor(backColor);
	    	chan2.setBackgroundColor(backColor);
	    	logChan.setBackgroundColor(backColor);
	    	
	    	SELECTED_CHANNEL=-1;
	    	oscSurface.SELECTED_CHANNEL=-1;
    	}
    }
    
    /**
     * Remove measurement from current measurements
     * @param v
     */
    private void removeMeasurement(View v)
    {
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    	final View tmpView=v;
    	
    	dialogBuilder.setTitle("Remove measurement?")
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					measure1.setVisibility(View.INVISIBLE);
					measure2.setVisibility(View.INVISIBLE);
					measure3.setVisibility(View.INVISIBLE);
					measure4.setVisibility(View.INVISIBLE);
					
					switch(tmpView.getId()){
					case R.id.Measure1:
						measure.removeMeasurement(0);
						break;
					case R.id.Measure2:
						measure.removeMeasurement(1);
						break;
					case R.id.Measure3:
						measure.removeMeasurement(2);
						break;
					case R.id.Measure4:
						measure.removeMeasurement(3);
					}
					dialog.dismiss();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setCancelable(false);
    	optionsDialog = dialogBuilder.create();
    	optionsDialog.show();
    }
    
    /** Display dialog to enable channels */
    private void selectChannelDialog(){
    	final CharSequence[] items = {"Channel 1","Channel 2"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select channels")
    		.setCancelable(true)
    		.setMultiChoiceItems(items, enabledChannels, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which,
						boolean isChecked) {
					enabledChannels[which]=isChecked;
					switch(which){
					case 0:
						if(isChecked){ 
							chan1.setText(R.string.ch1on);
							channel1.setEnabled(true);
							if(SELECTED_CHANNEL!=CHANNEL1)
								chan1Select();
							if(connectionService.isConnected())
								connectionService.setCh1Enabled(true);
						}
						else{ 
							chan1.setText(R.string.ch1off);
							channel1.setEnabled(false);
							if(SELECTED_CHANNEL==CHANNEL1)
								chan1Select();
							if(connectionService.isConnected())
								connectionService.setCh1Enabled(false);
						}
						
						break;
					case 1:
						if(isChecked){
							chan2.setText(R.string.ch2on);
							channel2.setEnabled(true);
							if(SELECTED_CHANNEL!=CHANNEL2)
								chan2Select();
							if(connectionService.isConnected())
								connectionService.setCh2Enabled(true);
						}
						else{
							chan2.setText(R.string.ch2off);
							channel2.setEnabled(false);
							if(SELECTED_CHANNEL==CHANNEL2)
								chan2Select();
							if(connectionService.isConnected())
								connectionService.setCh2Enabled(false);
							}
						break;
					}					
				}
			});
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /**
     *  Display dialog to select measurements
     */
    private void selectMeasurementsDialog(){
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.measurement_dialog,
    	                               (ViewGroup) findViewById(R.id.dialog_root));
    	
    	chan = (RadioGroup) layout.findViewById(R.id.chSourceSelect);
    	type = (RadioGroup) layout.findViewById(R.id.typeSelect);
    	
    	optionsBuilder.setTitle("Add new measurement")
    		.setCancelable(false)
    		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	  
    	        	   AnalogChannel mChannel = null;
    	        	   int mChan = -1;
    	        	   int mType = -1;
    	        	   
    	        	   switch (chan.getCheckedRadioButtonId()){
    	        	   case R.id.ch1Source:
    	        		   mChannel = channel1;
    	        		   mChan = 1;
    	        		   break;
    	        	   case R.id.ch2Source:
    	        		   mChannel = channel2;
    	        		   mChan = 2;
    	        		   break;
    	        	   }
    	        	   
    	        	   switch (type.getCheckedRadioButtonId()){
    	        	   case R.id.deltaT:
    	        		   mType=0;
    	        		   break;
    	        	   case R.id.deltaV:
    	        		   mType=1;
    	        		   break;
    	        	   case R.id.max:
    	        		   mType=2;
    	        		   break;
    	        	   case R.id.min:
    	        		   mType=3;
    	        		   break;
    	        	   case R.id.PkPk:
    	        		   mType=4;
    	        		   break;
    	        	   case R.id.freq:
    	        		   mType=5;
    	        		   break;
    	        	   case R.id.avg:
	    	        	   mType=6;
	    	        	   break;
    	        	   }
    	        	   
    	        	   if(mChannel!=null && mType!=-1)
    	        		   measure.addMeasurement(mChannel, mChan, mType);
    	        	   //measure1.setVisibility(View.VISIBLE);
    	        	   chan=null;
    	        	   type=null;
    	        	   dialog.dismiss();
    	           }
    	       })
    	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					chan=null;
					type=null;
					dialog.dismiss();					
				}
			})
			.setView(layout);
 		
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Display dialog to select Trigger options */
    private void selectTriggerDialog(){

    	final CharSequence[] items = {"Trigger source","Trigger mode"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Trigger options")
    		.setCancelable(true)
    		.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which==0){ //Trigger source
						selectTriggerSource();
					}
					if(which==1){ //Trigger mode
						selectTriggerMode();
					}
				}
			} );
    	optionsDialog=optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /**
     * Display dialog to select running mode
     */
    private void selectRunModeDialog(){
    	final CharSequence[] items = {"Auto mode","Single mode"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Running mode")
    		.setCancelable(true)
    		.setSingleChoiceItems(items,CURRENT_MODE,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CURRENT_MODE=which;
					connectionService.setMode(which);
					oscSurface.setRunningMode(which);
					channel1.resetZoom();
					channel2.resetZoom();
					dialog.dismiss();
				}
			});
    	optionsDialog=optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Display dialog to select Trigger Source */
    private void selectTriggerSource()
    {
    	final CharSequence[] items={"Channel 1","Channel 2"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Trigger source")
    		.setCancelable(true)
    		.setSingleChoiceItems(items, TRIG_SOURCE, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which){
					case CHANNEL1:
						TRIG_SOURCE=CHANNEL1;
						if(connectionService.isConnected()){
							connectionService.setTriggerSource(1);
							connectionService.getData();
						}
						mTrigger.setSource(1);
						break;
					case CHANNEL2:
						TRIG_SOURCE=CHANNEL2;
						if(connectionService.isConnected()){
							connectionService.setTriggerSource(2);
							connectionService.getData();
						}
						mTrigger.setSource(2);
						break;
					}
					dialog.dismiss();
				}
			});
    	optionsDialog=optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Display dialog to select Trigger Mode */
    private void selectTriggerMode()
    {
    	final CharSequence[] items={"Rising edge","Falling edge"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Trigger mode")
    		.setCancelable(true)
    		.setSingleChoiceItems(items, TRIG_MODE, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which){
					case RISING_EDGE:
						TRIG_MODE=RISING_EDGE;
						mTrigger.setRising(true);
						if(connectionService.isConnected())
							connectionService.setTriggerEdge(true);
						break;
					case FALLING_EDGE:
						TRIG_MODE=FALLING_EDGE;
						mTrigger.setRising(false);
						if(connectionService.isConnected())
							connectionService.setTriggerEdge(false);
						break;
					}
					dialog.dismiss();
				}
			});
    	optionsDialog=optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /**
     * Handle Message containing measurement result
     * @param msg
     */
    private void handleMeasurementMsg(Message msg)
    {
    	//float val = msg.getData().getFloat(Measurement.MEASUREMENT_RESULT);
		String result = msg.getData().getString(Measurement.MEASUREMENT_RESULT);
    	
    	if(msg.arg2==0){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure1.setText("CH1 ");
				measure1.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure1.setText("CH2 ");
				measure1.setTextColor(ch2Color);
			}
			measure1.setVisibility(View.VISIBLE);
			measure1.append(MEASUREMENTS[msg.arg1] + result);
		}else if (msg.arg2==1){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure2.setText("CH1 ");
				measure2.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure2.setText("CH2 ");
				measure2.setTextColor(ch2Color);
			}
			measure2.setVisibility(View.VISIBLE);
			measure2.append(MEASUREMENTS[msg.arg1] + result);
		}else if (msg.arg2==2){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure3.setText("CH1 ");
				measure3.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure3.setText("CH2 ");
				measure3.setTextColor(ch2Color);
			}
			measure3.setVisibility(View.VISIBLE);
			measure3.append(MEASUREMENTS[msg.arg1] + result);
		}else if (msg.arg2==3){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure4.setText("CH1 ");
				measure4.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure4.setText("CH2 ");
				measure4.setTextColor(ch2Color);
			}
			measure4.setVisibility(View.VISIBLE);
			measure4.append(MEASUREMENTS[msg.arg1] + result);
		}
		else if (msg.arg2==4){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure5.setText("CH1 ");
				measure5.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure5.setText("CH2 ");
				measure5.setTextColor(ch2Color);
			}
			measure5.setVisibility(View.VISIBLE);
			measure5.append(MEASUREMENTS[msg.arg1] + result);
		}
		else if (msg.arg2==5){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure6.setText("CH1 ");
				measure6.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure6.setText("CH2 ");
				measure6.setTextColor(ch2Color);
			}
			measure6.setVisibility(View.VISIBLE);
			measure6.append(MEASUREMENTS[msg.arg1] + result);
		}
		else if (msg.arg2==6){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure7.setText("CH1 ");
				measure7.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure7.setText("CH2 ");
				measure7.setTextColor(ch2Color);
			}
			measure7.setVisibility(View.VISIBLE);
			measure7.append(MEASUREMENTS[msg.arg1] + result);
		}
		else if (msg.arg2==7){
			if(msg.getData().getInt(Measurement.SOURCE)==1){
				measure8.setText("CH1 ");
				measure8.setTextColor(ch1Color);
			}
			else if (msg.getData().getInt(Measurement.SOURCE)==2){
				measure8.setText("CH2 ");
				measure8.setTextColor(ch2Color);
			}
			measure8.setVisibility(View.VISIBLE);
			measure8.append(MEASUREMENTS[msg.arg1] + result);
		}
    }
    
    /**
     * Handle Message containing new dataSamples
     * @param msg
     */
    private void handleNewAnalogueData(Message msg)
    {
    	int trigAddress = msg.arg1;
    	int[] data = msg.getData().getIntArray(ConnectionService.ANALOG_DATA);
    	
//    	Log.d(TAG,"Handling data, numSamples: " + data.length + " trigAddress: " + trigAddress);
    	
    	if(channel1.isEnabled() && channel2.isEnabled() && connectionService.getMode()!=2){ //2 channels, 1024 samples/channel
    		int[] dataCh1 = new int[1024];
    		int[] dataCh2 = new int[data.length-1024];
    		System.arraycopy(data, 0, dataCh1, 0, 1024);
    		System.arraycopy(data, 1024, dataCh2, 0, data.length-1024);
    		
    		channel1.setNewData(dataCh1, dataCh1.length,trigAddress);
    		channel2.setNewData(dataCh2, dataCh2.length,trigAddress);    		
    		
    	} else  if(channel1.isEnabled() && !channel2.isEnabled()){ //Only channel 1 enabled, 2048 samples
    		channel1.setNewData(data, data.length,trigAddress);
    		
    	} else if(!channel1.isEnabled() && channel2.isEnabled()){ //Only channel 2 enabled, 2048 samples
    		channel2.setNewData(data, data.length,trigAddress);
    	}    	
    }
    
    
    /**
     * Handler to carry messages from other classes to this main activity.
     * handleMessage implements the handling of the messages
     */
    private Handler mHandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		
    		//TODO add all possible messages
    		
    		switch(msg.what){
    		case Measurement.MSG_MEASUREMENTS:
    			handleMeasurementMsg(msg);
    			break;
    			
    		case OscDroidSurfaceView.SET_VOLT_CH1:
    			setDivVoltCh1(msg.arg1);
    			break;
    			
    		case OscDroidSurfaceView.SET_VOLT_CH2:
    			setDivVoltCh2(msg.arg1);
    			break;
    			
    		case OscDroidSurfaceView.SET_TIME_DIV:
    			setDivTime(msg.arg1);
    			break;
    			
    		case ConnectionService.CONN_STATUS_CHANGED:
    			if(msg.arg1==0x0A) //Connected
    				setTitle(getString(R.string.app_name) + "   Status: Connected");
    			else if (msg.arg1==0x0B) //Disconnected
    				setTitle(getString(R.string.app_name) + "   Status: Disconnected");
    			break;
    		case ConnectionService.NEW_DATA_ARRIVED:
//    			Log.d(TAG,"Got new data in main");
    			handleNewAnalogueData(msg);
    			break;
    		case ConnectionService.APPEND_NEW_DATA:
    			channel1.appendNewData(msg.getData().getIntArray(ConnectionService.ANALOG_DATA));
//    			Log.d(TAG,"Appending analog data");
    			break;
    		case ConnectionService.CONNECTION_RESET:
    			Log.e(TAG,"Connection was reset!");
    			connectionService.setupConnection();
    			break;
    		case Trigger.TRIG_LVL_CHANGED:
    			connectionService.setTriggerLvl(mTrigger.getLevel());
    			break;
    		case Trigger.TRIG_POS_CHANGED:
    			connectionService.setTriggerPos(mTrigger.getPos());
    			channel1.setTriggerPos(mTrigger.getPos());
    			channel2.setTriggerPos(mTrigger.getPos());
    			break;
    		}
    		
    	}
    };
}