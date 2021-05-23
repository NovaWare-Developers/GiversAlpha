package giversapdc.util;

public class MapMarker {

	private long lat;
	private long lon;
	private String eventId;
	private String eventName;
	
	public MapMarker(long lat, long lon, String eventId, String eventName) {
		this.lat = lat;
		this.lon = lon;
		this.eventId = eventId;
		this.eventName = eventName;
	}
	
}
