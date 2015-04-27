package uppsala.biketracking;

//import android.content.*;

public class Data
{
	private int SID;
	private final double LATITUDE, LONGITUDE;
	//private final int latitude;
	private final long TIME;
	public Data(int i, double lat, double lon, long t){
		this.SID = i;
		this.LATITUDE = lat;
		this.LONGITUDE = lon;
		this.TIME = t;
	}
	public int getSID(){
		return this.SID;
	}
	public void setSID(int sid){
		this.SID = sid;
	}
	public double getLatitude(){
		return this.LATITUDE;
	}
	public double getLongitude(){
		return this.LONGITUDE;
	}
	public long getTime(){
		return this.TIME;
	}
	
	@Override
	public boolean equals(Object o){
		boolean equals = false;
		if(o instanceof Data){
			Data object = (Data) o;
			equals = (this.SID == object.getSID() && this.LATITUDE == object.getLatitude() && this.LONGITUDE == object.getLongitude() && this.TIME == object.getTime());
		}
		return equals;
	}
	@Override
	public String toString(){
		return "{\"SID\":\""+this.SID+"\",\"LATITUDE\":\""+this.LATITUDE+"\",\"LONGITUDE\":\""+this.LONGITUDE+"\",\"TIME\":\""+this.TIME+"\"}";
	}

}
