package com.kvw.oscdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;

import com.kvw.oscdroid.ColorPickerDialog.OnColorChangedListener;

public class SettingsActivity extends Activity{
	
	private static final String TAG="SettingsActivity";
	
	private RadioGroup connectionSetting;
	private RadioGroup soundSetting;
	
	private View ch1Color;
	private View ch2Color;
	private View logColor;
	private View overlayColor;
	private View backColor;
	
	private int ch1;
	private int ch2;
	private int logchan;
	private int overlay;
	private int back;
	
	private String changingColor="";
	
	public static final String CONNECTION_SETTING = "connection";
	public static final String SOUND_SETTING="sound";
	public static final String COLOR_CH1="ch1_color";
	public static final String COLOR_CH2="ch2_color";
	public static final String COLOR_LOGCH="log_color";
	public static final String COLOR_OVERLAY="overlay_color";
	public static final String COLOR_BACK="back_color";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//setResult(Activity.RESULT_);
		
		setContentView(R.layout.settings);
		
		connectionSetting = (RadioGroup) findViewById(R.id.connectionsgroup);
		soundSetting = (RadioGroup) findViewById(R.id.soundgroup);
		
		ch1Color = (View) findViewById(R.id.colorCh1);
		ch2Color = (View) findViewById(R.id.colorCh2);
		logColor = (View) findViewById(R.id.colorLog);
		overlayColor = (View) findViewById(R.id.colorOverlay);
		backColor = (View) findViewById(R.id.colorBackground);
		
		Intent intent = getIntent();
		
		if(intent.getExtras().getInt(CONNECTION_SETTING)==1)
			connectionSetting.check(R.id.wifi_connection);
		else if(intent.getExtras().getInt(CONNECTION_SETTING)==2)
			connectionSetting.check(R.id.usb_connection);
		
		if(intent.getExtras().getBoolean(SOUND_SETTING))
			soundSetting.check(R.id.soundOn);
		else if (!intent.getExtras().getBoolean(SOUND_SETTING))
			soundSetting.check(R.id.soundOff);
		
		Log.v(TAG,"Initializing settings");
		
		ch1 = intent.getExtras().getInt(COLOR_CH1);
		ch2 = intent.getExtras().getInt(COLOR_CH2);
		logchan = intent.getExtras().getInt(COLOR_LOGCH);
		overlay = intent.getExtras().getInt(COLOR_OVERLAY);
		back = intent.getExtras().getInt(COLOR_BACK);
		
		
		ch1Color.setBackgroundColor(ch1);
		ch2Color.setBackgroundColor(ch2);
		logColor.setBackgroundColor(logchan);
		overlayColor.setBackgroundColor(overlay);
		backColor.setBackgroundColor(back);
		
		
		//TODO Implement initializing of color settings
	}
	
	private OnColorChangedListener mListener = new OnColorChangedListener(){

		@Override
		public void colorChanged(int color) {
			// TODO Auto-generated method stub
		if (changingColor.equals(COLOR_CH1)){
			ch1Color.setBackgroundColor(color);
			ch1=color;
		}else if (changingColor.equals(COLOR_CH2)){
			ch2Color.setBackgroundColor(color);
			ch2=color;
		}else if (changingColor.equals(COLOR_LOGCH)){
			logColor.setBackgroundColor(color);
			logchan=color;
		}else if (changingColor.equals(COLOR_OVERLAY)){
			overlayColor.setBackgroundColor(color);
			overlay=color;
		}else if (changingColor.equals(COLOR_BACK)){
			backColor.setBackgroundColor(color);
			back=color;
		}
		}		
	};
	

	public void changeCh1Color(View v)
	{
		ColorPickerDialog dialog = new ColorPickerDialog(this,mListener,ch1);
		changingColor=COLOR_CH1;
		dialog.show();
	}
	
	public void changeCh2Color(View v)
	{
		ColorPickerDialog dialog = new ColorPickerDialog(this,mListener,ch2);
		changingColor=COLOR_CH2;
		dialog.show();
	}
	
	public void changeLogColor(View v)
	{
		ColorPickerDialog dialog = new ColorPickerDialog(this,mListener,logchan);
		changingColor=COLOR_LOGCH;
		dialog.show();
	}
	
	public void changeOverlayColor(View v)
	{
		ColorPickerDialog dialog = new ColorPickerDialog(this,mListener,overlay);
		changingColor=COLOR_OVERLAY;
		dialog.show();
	}
	
	public void changeBackColor(View v)
	{
    	final CharSequence[] items = {"White","Black"};
    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);
    	optionsBuilder.setTitle("Select background color")
    		.setCancelable(true)
    		.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which==0){ //White
						back=Color.WHITE;
						backColor.setBackgroundColor(Color.WHITE);
					}
					if(which==1){ //Black
						back=Color.BLACK;
						backColor.setBackgroundColor(Color.BLACK);
					}
				}
			} );
    	AlertDialog Dialog=optionsBuilder.create();
    	Dialog.show();
	}
	
	public void resetColors(View v)
	{
		ch1=Color.YELLOW;
		ch2=Color.BLUE;
		logchan=Color.GREEN;
		overlay=Color.RED;
		back=Color.BLACK;
		
		ch1Color.setBackgroundColor(ch1);
		ch2Color.setBackgroundColor(ch2);
		logColor.setBackgroundColor(logchan);
		overlayColor.setBackgroundColor(overlay);
		backColor.setBackgroundColor(back);
	}
	
	public void saveSettings(View v)
	{
		int connection=1;
		boolean sounds=false;
		
		
		if(connectionSetting.getCheckedRadioButtonId()==R.id.usb_connection)
			connection=2;
		else if(connectionSetting.getCheckedRadioButtonId()==R.id.wifi_connection)
			connection=1;
		
		if(soundSetting.getCheckedRadioButtonId()==R.id.soundOn)
			sounds=true;
		else if (soundSetting.getCheckedRadioButtonId()==R.id.soundOff)
			sounds=false;
		
		Intent intent = new Intent();
		intent.putExtra(CONNECTION_SETTING,connection);
		intent.putExtra(SOUND_SETTING,sounds);
		intent.putExtra(COLOR_CH1, ch1);
		intent.putExtra(COLOR_CH2, ch2);
		intent.putExtra(COLOR_LOGCH, logchan);
		intent.putExtra(COLOR_OVERLAY, overlay);
		intent.putExtra(COLOR_BACK, back);
		
		
		//TODO save settings to file for device preferences??
		
		Log.v(TAG,"Sending settings");
		setResult(Activity.RESULT_OK,intent);
		finish();
	}
	
	

}
