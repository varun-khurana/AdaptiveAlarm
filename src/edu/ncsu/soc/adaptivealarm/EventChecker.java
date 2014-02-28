package edu.ncsu.soc.adaptivealarm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.widget.EditText;

public class EventChecker implements Runnable{

	public static final String TAG = "alarm";
	private Activity context;
	private Map<Integer, Event> eventMap = new HashMap<Integer, Event>();
	private Map<Integer, PendingIntent> eventIntentMap = new HashMap<Integer, PendingIntent>();
	private static List<Integer> seenEvents = new ArrayList<Integer>();
	private long travelTime;
	private long readyTime;

	private static int agency_id;
	private static int route_id;
	private static int stop_id;


	private final String[] EVENT_PROJECTION ={
			Events._ID,
			Events.DTSTART,
			Events.TITLE,			
			Events.EVENT_LOCATION	
	};

	public EventChecker(Activity context){
		this.context = context;
		Properties properties = new Properties();
		try {
			Resources resources = this.context.getResources();
			AssetManager assetManager = resources.getAssets();
			InputStream inputStream = assetManager.open("route.properties");
			properties.load(inputStream);
			agency_id = Integer.parseInt(properties.getProperty("agency_id"));
			route_id = Integer.parseInt(properties.getProperty("route_id"));
			stop_id = Integer.parseInt(properties.getProperty("stop_id"));
			Log.i(TAG, "agency_id " + agency_id);
			Log.i(TAG, "route_id " + route_id);
			Log.i(TAG, "stop_id " + stop_id);
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}



	@Override
	public void run() {

		EditText travelTimeText = (EditText) this.context.findViewById(R.id.travelTime);
		EditText readyTimeText = (EditText) this.context.findViewById(R.id.readyTime);

		travelTime = Integer.parseInt(travelTimeText.getText().toString()) * 60 * 1000;
		readyTime = Integer.parseInt(readyTimeText.getText().toString()) * 60 * 1000;

		while(true){
			unregisterAlarms();
			getCurrentEvents();
			registerAlarms();
			try{
				Thread.sleep(1*60*1000);
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}
		}



	}

	private void registerAlarms() {
		if(eventMap != null && eventMap.size() > 0){
			eventIntentMap = new HashMap<Integer, PendingIntent>();
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Iterator<Integer> eventIterator = eventMap.keySet().iterator();
			while(eventIterator.hasNext()){
				int eventId = eventIterator.next();
				Event event = eventMap.get(eventId);

				long eventStartTime = event.getEventStartTime();

				long latestBusTime = BusHelper.getLatestBusTime(agency_id,route_id, stop_id, eventStartTime - travelTime);

				Log.i(TAG, "Latest Bus Time :: " + new Date(latestBusTime));
				Log.i(TAG, "Event start time :: " + new Date(eventStartTime));				
				long alarmTime = latestBusTime - readyTime;
				Log.i(TAG, "Alarm time set to :: " + new Date(alarmTime));

				Intent intent = new Intent(context, AlarmActivity.class);
				intent.putExtra("eventId", eventId);
				intent.putExtra("eventTitle", event.getTitle());
				intent.putExtra("eventLocation", event.getEventLocation());
				intent.putExtra("alarmTime", alarmTime);
				intent.putExtra("latestBusTime", latestBusTime);

				PendingIntent pending = PendingIntent.getActivity(context, eventId, intent, PendingIntent.FLAG_ONE_SHOT);
				am.set(AlarmManager.RTC_WAKEUP, alarmTime, pending);
				eventIntentMap.put(eventId, pending);
			}

		}
	}

	private void getCurrentEvents() {
		eventMap = new HashMap<Integer, Event>();
		Cursor cur = null;
		ContentResolver cr = context.getContentResolver();
		Uri uri = Events.CONTENT_URI;   
		String selection = "((" + Events.CALENDAR_ID + " = ?) AND("
				+ Events.DTSTART + ">= ?) AND ("
				+ Events.DELETED + "= ?))";
		String[] selectionArgs = new String[] {"3", String.valueOf(System.currentTimeMillis()), "0"}; 
		// Submit the query and get a Cursor object back. 

		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, Events.DTSTART);
		/*Log.i(TAG, String.valueOf(cur.getCount()));
		Log.i(TAG, seenEvents.toString());*/
		while (cur.moveToNext()) {


			// Do something with the values...
			/*Log.i(TAG,"--------------------------------------");
			Log.i(TAG,cur.getString(0));
			Log.i(TAG,cur.getString(1));
			Log.i(TAG,cur.getString(2));
			Log.i(TAG,cur.getString(3));
			//Log.i(TAG,cur.getString(2));

			//events.add(cur.getString(0));
			Log.i(TAG,"--------------------------------------");
			 */
			int eventId = cur.getInt(0);
			long startTime = cur.getLong(1);
			String title = cur.getString(2);
			String location = cur.getString(3);

			if(!(seenEvents.contains(eventId))){				
				eventMap.put(cur.getInt(0), new Event(eventId, startTime, title, location));
			}
		}		

	}

	private void unregisterAlarms() {
		//Log.i(TAG, "unregisterAlarms");
		if(eventIntentMap != null && eventIntentMap.size() > 0){
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Iterator<Integer> eventIterator = eventIntentMap.keySet().iterator();
			while(eventIterator.hasNext()){
				am.cancel(eventIntentMap.get(eventIterator.next()));
			}
		}

	}


	public static void addSeenEvent(int eventId){
		seenEvents.add(eventId);
	}

}
