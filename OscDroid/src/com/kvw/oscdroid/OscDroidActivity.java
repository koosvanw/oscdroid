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


import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class OscDroidActivity extends Activity implements TextToSpeech.OnInitListener{
	
	/** Static values */
	private final static String TAG="OscDroidActivity";
	
	private final static int CHANNEL1 = 0;
	private final static int CHANNEL2 = 1;
	private final static int LOGICPROBE = 2;
	private final static int RISING_EDGE=0;
	private final static int FALLING_EDGE=1;
	
	private final static int GET_SETTINGS=20;
	
	/** Interface objects */
	private OscDroidSurfaceView oscSurface;
	
	private TextView chan1;
    private TextView chan2;
    private TextView logChan;
    
    private TextView ch1Div;
    private TextView ch2Div;
    private TextView timeDiv;
    
    private TextView deltaT;
    private TextView deltaV;
    private TextView minV;
    private TextView maxV;
    private TextView PkPkV;
    private TextView freq;
    
    private Button channels;
    private Button selectMeasurements;
    private Button TriggerBtn;
    
    private OnClickListener chan1Selected;
    private OnClickListener chan2Selected;
    private OnClickListener logSelected;
    
    private OnClickListener ch1DivClicked;
    private OnClickListener ch2DivClicked;
    private OnClickListener timeDivClicked;
    
    private OnClickListener selectChannel;
    private OnClickListener selectMeas;
    private OnClickListener selectTrigger;
    
    
    /** Class variables and elements */
    private boolean[] enabledChannels={false,false,false};
    private boolean[] enabledMeasurements={false,false,false,false,false,false};

    private AlertDialog optionsDialog;
    private TextToSpeech mTts;
    private boolean ttsAvailable=false;
    
    /** User preferences */
    private boolean soundsEnabled;
    private int connectionType;
    private int ch1Color;
    private int ch2Color;
    private int logColor;
    private int overlayColor;
    private int backColor;
    
    /** Scope units */
    
    private AnalogChannel channel1;
    private AnalogChannel channel2;
    
    private int TRIG_SOURCE=CHANNEL1;
    private int TRIG_MODE=RISING_EDGE;
    
    private int SELECTED_CHANNEL = -1;
    private int SELECTED_DIV_CH1=7;
    private int SELECTED_DIV_CH2=7;
    
    private int SELECTED_DIV_TIME=6;
    
    private String[] VOLT_DIVS;    
	private String[] TIME_DIVS;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full screen, no Title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
        getPrefs();
        
        channel1=new AnalogChannel(mHandler,"CH1");
        channel1.setColor(ch1Color);
        
        channel2=new AnalogChannel(mHandler,"CH2");
        channel2.setColor(ch2Color);
        
        VOLT_DIVS = getResources().getStringArray(R.array.volt_divs);
        TIME_DIVS  = getResources().getStringArray(R.array.time_divs);
        
        loadUIComponents();
        
        mTts=new TextToSpeech(this,this);
        
        initUIInteraction();
        
        loadPrefs();
        
    }
    
    /** Connect to all UI components */
    private void loadUIComponents()
    {
        oscSurface = (OscDroidSurfaceView) findViewById(R.id.mSurfaceView);
        oscSurface.setHandler(mHandler);
        oscSurface.addChannel(channel1,oscSurface.getWidth(),oscSurface.getHeight());
        oscSurface.addChannel(channel2,oscSurface.getWidth(),oscSurface.getHeight());
        
        channels = (Button) findViewById(R.id.textView1);
        selectMeasurements = (Button) findViewById(R.id.measButton);
        TriggerBtn = (Button) findViewById(R.id.triggerButton);
        
        chan1 = (TextView) findViewById(R.id.mChan1);
        chan2 = (TextView) findViewById(R.id.mChan2);
        logChan = (TextView) findViewById(R.id.mChanLog);
        
        ch1Div = (TextView) findViewById(R.id.ch1Div);
        ch2Div = (TextView) findViewById(R.id.ch2Div);
        timeDiv = (TextView) findViewById(R.id.timediv);
        
        deltaT = (TextView) findViewById(R.id.deltaT);
        deltaV = (TextView) findViewById(R.id.deltaV);
        minV = (TextView) findViewById(R.id.minV);
        maxV = (TextView) findViewById(R.id.maxV);
        PkPkV = (TextView) findViewById(R.id.PkPkV);
        freq = (TextView) findViewById(R.id.freq);
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
        		setDivVoltCh1();
        	}
        };
        
        ch2DivClicked = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		setDivVoltCh2();
        	}
        };
        
        timeDivClicked = new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		setDivTime();
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
    	
    	//TODO implement setting of connectiontype
    }
    
    /** Read all preferences from file, set the variables */
    private void getPrefs()
    {
    	SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
    	
    	soundsEnabled=mPrefs.getBoolean("soundsEnabled", true);
    	connectionType=mPrefs.getInt("connectionType", 1);
    	
    	ch1Color=mPrefs.getInt("ch1Color", Color.YELLOW);
    	ch2Color=mPrefs.getInt("ch2Color",Color.BLUE);
    	logColor=mPrefs.getInt("logColor",Color.GREEN);
    	overlayColor=mPrefs.getInt("overlayColor",Color.RED);
    	backColor=mPrefs.getInt("backColor", Color.BLACK);
    }
    
    
    /** Implements TextToSpeech.OnInitListener. */
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e("","Language is not available.");
                ttsAvailable=false;
            } else {
                ttsAvailable=true;
            }
        } else {
            // Initialization failed.
            Log.e("", "Could not initialize TextToSpeech.");
            ttsAvailable=false;
        }
    }
    
    /** Called when activity is paused. */
    //TODO check if onPause is relevant, fixing destroying and restarting activity
    @Override
    public void onPause()
    {
    	super.onPause();

    	if(mTts!=null){
    		mTts.stop();
    		mTts.shutdown();
    		mTts=null;
    	}
    	SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editor = mPrefs.edit();
    	
    	editor.putBoolean("soundsEnabled", soundsEnabled);
    	editor.putInt("connectionType", connectionType);
    	
    	editor.commit();
    	
    }
     
    @Override
    protected void onResume()
    {
    	super.onResume();
    	if(mTts==null)
    		mTts=new TextToSpeech(this,this);
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
    		if(ttsAvailable)
    			mTts.speak("Help, please",TextToSpeech.QUEUE_FLUSH,null);
    		break;
    	case R.id.settings:
    		Intent intent = new Intent(this,SettingsActivity.class);

    		intent.putExtra(SettingsActivity.CONNECTION_SETTING, connectionType);
    		intent.putExtra(SettingsActivity.SOUND_SETTING, soundsEnabled);
    		
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
    		Log.v(TAG,"settings finished");
    		
    		if(resultcode==Activity.RESULT_OK){ //Save settings, load settings
    			Log.v(TAG,"result ok");
    			connectionType=data.getExtras().getInt(SettingsActivity.CONNECTION_SETTING);
    			soundsEnabled=data.getExtras().getBoolean(SettingsActivity.SOUND_SETTING);
    			ch1Color=data.getExtras().getInt(SettingsActivity.COLOR_CH1);
    			ch2Color=data.getExtras().getInt(SettingsActivity.COLOR_CH2);
    			logColor=data.getExtras().getInt(SettingsActivity.COLOR_LOGCH);
    			overlayColor=data.getExtras().getInt(SettingsActivity.COLOR_OVERLAY);
    			backColor=data.getExtras().getInt(SettingsActivity.COLOR_BACK);
    			
    			
    			Log.v(TAG,"settings received");
    			
    			SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
    			SharedPreferences.Editor editor = mPrefs.edit();
    			
    			editor.putInt("connectionType",connectionType);
    			editor.putBoolean("soundsEnabled", soundsEnabled);
    			
    			editor.putInt("ch1Color", ch1Color);
    			editor.putInt("ch2Color", ch2Color);
    			editor.putInt("logColor", logColor);
    			editor.putInt("overlayColor", overlayColor);
    			editor.putInt("backColor", backColor);
    			
    			editor.commit();
    			loadPrefs();
    		} else Log.v(TAG,"result: " + resultcode);
    		break;
    	}
    }
    
    /** Set Volts/division for channel1 */
    private void setDivVoltCh1()
    {    	
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select div Ch1")
    		.setCancelable(true)
    		.setSingleChoiceItems(VOLT_DIVS, SELECTED_DIV_CH1, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SELECTED_DIV_CH1=which;
					ch1Div.setText(getString(R.string.ch1Div) + " " + VOLT_DIVS[which]);
					//dialog.dismiss();
				}
			});
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Set Volts/division for channel2 */
    private void setDivVoltCh2()
    {
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select div Ch2")
    		.setCancelable(true)
    		.setSingleChoiceItems(VOLT_DIVS, SELECTED_DIV_CH2, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SELECTED_DIV_CH2=which;
					ch2Div.setText(getString(R.string.ch2Div) + " " + VOLT_DIVS[which]);
					//dialog.dismiss();
				}
			});
    	optionsDialog = optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /** Set Seconds/division for all channels */
    private void setDivTime()
    {
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select time div")
    		.setCancelable(true)
    		.setSingleChoiceItems(TIME_DIVS, SELECTED_DIV_TIME, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SELECTED_DIV_TIME=which;
					timeDiv.setText(getString(R.string.timeDiv) + " " + TIME_DIVS[which]);
					//dialog.dismiss();
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
    
    /** Display dialog to enable channels */
    private void selectChannelDialog(){
    	final CharSequence[] items = {"Channel 1","Channel 2", "Logic probe"};
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
							if(ttsAvailable && soundsEnabled)
							mTts.speak("Channel 1: enabled", TextToSpeech.QUEUE_FLUSH, null);
							channel1.setEnabled(true);
						}
						else{ 
							chan1.setText(R.string.ch1off);
							if(ttsAvailable && soundsEnabled)
							mTts.speak("Channel 1: disabled", TextToSpeech.QUEUE_FLUSH, null);
							channel1.setEnabled(false);
						}
						break;
					case 1:
						if(isChecked){
							chan2.setText(R.string.ch2on);
							if(ttsAvailable && soundsEnabled)
							mTts.speak("Channel 2: enabled", TextToSpeech.QUEUE_FLUSH, null);
							channel2.setEnabled(true);
						}
						else{
							chan2.setText(R.string.ch2off);
							if(ttsAvailable && soundsEnabled)
							mTts.speak("Channel 2: disabled", TextToSpeech.QUEUE_FLUSH, null);
							channel2.setEnabled(false);
						}
						break;
					case 2:
						if(isChecked){
							logChan.setText(R.string.logon);
							if(ttsAvailable && soundsEnabled)
								mTts.speak("Logic channel: enabled", TextToSpeech.QUEUE_FLUSH, null);
						}
						else{
							logChan.setText(R.string.logoff);
							if(ttsAvailable && soundsEnabled)
								mTts.speak("Logic channel: disabled", TextToSpeech.QUEUE_FLUSH, null);
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
     * TODO rewrite measurements to select measurement and source
     */
    private void selectMeasurementsDialog(){
    	final CharSequence[] items = {"Delta-T","Delta-V",
				"Minimum","Maximum","Pk-Pk","Frequency"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select Measurements")
    		.setCancelable(true)
    		.setMultiChoiceItems(items, enabledMeasurements, 
    				new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which,
						boolean isChecked) {
					enabledMeasurements[which]=isChecked;
					switch(which){
					case 0:
						if(isChecked){
							deltaT.setVisibility(View.VISIBLE);
						}
						else{
							deltaT.setVisibility(View.INVISIBLE);
						}
						break;
					case 1:
						if(isChecked){
							deltaV.setVisibility(View.VISIBLE);
						}
						else{
							deltaV.setVisibility(View.INVISIBLE);
						}
						break;
					case 2:
						if(isChecked){
							minV.setVisibility(View.VISIBLE);
						}
						else{
							minV.setVisibility(View.INVISIBLE);
						}
						break;
					case 3:
						if(isChecked){
							maxV.setVisibility(View.VISIBLE);
						}
						else maxV.setVisibility(View.INVISIBLE);
						break;
					case 4:
						if(isChecked){
							PkPkV.setVisibility(View.VISIBLE);
						}
						else PkPkV.setVisibility(View.INVISIBLE);
						break;
					case 5:
						if(isChecked){
							freq.setVisibility(View.VISIBLE);
						}
						else{
							freq.setVisibility(View.INVISIBLE);
						}
						break;
					}
					
				}
			});
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
    
    /** Display dialog to select Trigger Source */
    private void selectTriggerSource()
    {
    	final CharSequence[] items={"Channel 1","Channel 2","Logic probe"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Trigger source")
    		.setCancelable(true)
    		.setSingleChoiceItems(items, TRIG_SOURCE, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which){
					case CHANNEL1:
						TRIG_SOURCE=CHANNEL1;
						break;
					case CHANNEL2:
						TRIG_SOURCE=CHANNEL2;
						break;
					case LOGICPROBE:
						TRIG_SOURCE=LOGICPROBE;
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
						break;
					case FALLING_EDGE:
						TRIG_MODE=FALLING_EDGE;
						break;
					}
					dialog.dismiss();
				}
			});
    	optionsDialog=optionsBuilder.create();
    	optionsDialog.show();
    }
    
    /**
     * Handler to carry messages from other classes to this main activity.
     * handleMessage implements the handling of the messages
     */
    private Handler mHandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		
    		//TODO
    	}
    };
}