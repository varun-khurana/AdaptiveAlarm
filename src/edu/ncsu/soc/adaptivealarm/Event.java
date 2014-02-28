package edu.ncsu.soc.adaptivealarm;

public class Event{
	int eventId;
	long eventStartTime;
	String title;	
	String eventLocation;

	public Event(int eventId, long eventStartTime, String title, String eventLocation) {
		super();
		this.eventId = eventId;
		this.eventStartTime = eventStartTime;
		this.title = title;
		this.eventLocation = eventLocation;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getEventLocation() {
		return eventLocation;
	}
	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}
	public int getEventId() {
		return eventId;
	}
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	public long getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(long eventStartTime) {
		this.eventStartTime = eventStartTime;
	}

}
