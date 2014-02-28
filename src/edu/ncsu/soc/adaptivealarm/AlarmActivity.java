package edu.ncsu.soc.adaptivealarm;

import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends Activity {
	public static final String TAG = "alarm"; 
	private WakeLock wl;
	private MediaPlayer mp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventChecker.addSeenEvent(this.getIntent().getExtras().getInt("eventId"));
		//Log.i("alarm", "System Time during alarm " + System.currentTimeMillis());
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wake lock");
		wl.acquire();
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,

				WindowManager.LayoutParams.FLAG_FULLSCREEN | 
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		String title = this.getIntent().getExtras().getString("eventTitle");		
		String location = this.getIntent().getExtras().getString("eventLocation");
		long aTime = this.getIntent().getExtras().getLong("alarmTime");
		long busTime = this.getIntent().getExtras().getLong("latestBusTime");
		
		Log.i(TAG, "Title : " + title);
		Log.i(TAG, "location : " + location);
		Log.i(TAG, "Alarm Time : " + aTime);
		Log.i(TAG, "Bus Time : " + busTime);
		setContentView(R.layout.activity_alarm);
		
		TextView eventTitle = (TextView) findViewById(R.id.eventTitle);
		
		eventTitle.setText("Event : " + title);
		TextView eventLocation = (TextView) findViewById(R.id.eventLocation);
		eventLocation.setText("At : " + location);
		TextView alarmTime = (TextView) findViewById(R.id.alarmTime);
		alarmTime.setText("Alarm Time : " + new Date(aTime).toString());
		TextView latestBusTime = (TextView) findViewById(R.id.latestBusTime);
		latestBusTime.setText("Latest Bus Time : " + new Date(busTime).toString());		

		Button stopAlarm = (Button) findViewById(R.id.stopAlarm);

		stopAlarm.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {	
				mp.stop();
				finish();				
			}
		});

		playSound(this, getAlarmUri());
	}

	private void playSound(Context context, Uri alert) {
		mp = new MediaPlayer();
		try{
			mp.setDataSource(context, alert);
			final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
			if(am.getStreamVolume(AudioManager.STREAM_ALARM) != 0){
				mp.setAudioStreamType(AudioManager.STREAM_ALARM);
				mp.prepare();
				mp.start();
			}
		}catch(IOException ex){
			Log.i(MainActivity.TAG, "No audio file found");
		}

	}

	private Uri getAlarmUri() {
		Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if(alarm == null){
			alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if(alarm == null){
				alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		return alarm;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_alarm, menu);
		return true;
	}

	protected void onStop(){
		super.onStop();
		if(wl != null && wl.isHeld()){
			try{
				Log.i("alarm", "Releasing wake lock");
				wl.release();
			}catch(Exception ex){
				//ex.printStackTrace();
			}
		}
	}

}
