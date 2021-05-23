package giversapdc.util;

import java.util.List;

public class Pair {

	public List<?> list;
	public String cursor;
	
	public Pair(List<?> lt, String st) {
		this.list = lt;
		this.cursor = st;
	}
}
