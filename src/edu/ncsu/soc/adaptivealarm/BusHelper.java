package edu.ncsu.soc.adaptivealarm;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;

public class BusHelper {
	public static final String TAG = "alarm"; 
	public static long getLatestBusTime(int agencyId, int routeId, int stopId, long timeToLeave){
		Log.i(TAG,String.valueOf(timeToLeave));
		Log.i(TAG,String.valueOf(new Date(timeToLeave)));
		List<String> arrivalTimes = getArrivalTimes(agencyId, routeId, stopId);
		long latestBusTime = System.currentTimeMillis();
		List<Long> usableBusTimes = new ArrayList<Long>();
		if(arrivalTimes != null && arrivalTimes.size() > 0){
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			for(String arrivalTime: arrivalTimes){				
				try {

					Date arrivalDate = df.parse(arrivalTime.substring(0, arrivalTime.lastIndexOf("-")));
					if(arrivalDate.getTime() <= timeToLeave){
						Log.i(TAG,String.valueOf(arrivalDate.getTime()));
						//As long as the bus arrival time is before time to  leave the house for the user, use this value
						usableBusTimes.add(arrivalDate.getTime());
					}
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}
		}

		if(usableBusTimes.size() > 0){
			latestBusTime = usableBusTimes.get(usableBusTimes.size() - 1);
		}

		Log.i(TAG,"Latest " + new Date(latestBusTime));
		return latestBusTime;
	}

	private static List<String> getArrivalTimes(int agencyId, int routeId, int stopId){
		StringBuffer data = new StringBuffer("");
		List<String> arrivalTimes = null;
		try {
			StringBuffer urlBuffer = new StringBuffer("http://api.transloc.com/1.1/arrival-estimates.json?agencies=" + agencyId);
			urlBuffer.append("&routes=" + routeId);
			urlBuffer.append("&stops=" + stopId);
			URL url = new URL(urlBuffer.toString());

			String str = null;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				while((str = reader.readLine()) != null){
					data.append(str);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(data != null){
			JSONObject json = (JSONObject) JSONValue.parse(data.toString());
			Log.i(TAG,String.valueOf(json));
			JSONArray dataElementArray = (JSONArray) json.get("data");
			if(dataElementArray != null && dataElementArray.size() > 0){
				JSONObject dataElement = (JSONObject) dataElementArray.get(0);
				JSONArray arrivals = (JSONArray) dataElement.get("arrivals");
				arrivalTimes = new ArrayList<String>();
				if(arrivals != null){
					for(int i=0;i<arrivals.size(); i++){
						JSONObject arrival = (JSONObject) arrivals.get(i);
						arrivalTimes.add((String) arrival.get("arrival_at"));
					}
				}
			}
			Log.i(TAG,String.valueOf(arrivalTimes));			
		}

		return arrivalTimes;
	}

	/*public static void main(String[] args){
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 3, 6, 23, 5);
		getLatestBusTime(4003118, 4099758, cal.getTimeInMillis());
	}*/
}
