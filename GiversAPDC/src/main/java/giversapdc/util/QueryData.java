package giversapdc.util;

public class QueryData {

	public String name;
	public String interests;
	public boolean newFirst;
	
	public AuthToken at;
	
	public String startCursorString;
	
	//This var is used for the query of events to select which time to show: -1 ended, 0 all, 1 future, 2 ongoing
	public int queryTime;
	
	public QueryData() { }
	
}
