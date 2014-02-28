package edu.ncsu.soc.adaptivealarm;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;

public class MainActivity extends Activity {
	public static final String TAG = "alarm";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button createEvent = (Button) findViewById(R.id.createEvent);
		
		createEvent.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {		
				Thread eventChecker = new Thread(new EventChecker(MainActivity.this));
				eventChecker.start();		
				long startMillis=System.currentTimeMillis();				
				Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
				builder.appendPath("time");
				ContentUris.appendId(builder, startMillis);
				Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
				startActivity(intent);
			}
		});
	}
}
