package uppsala.biketracking;

//import android.content.*;

public class Data
{
	private int SID;
	private final double LATITUDE, LONGITUDE;
	//private final int latitude;
	private final long TIME;
	private final float ACCURACY;
	public Data(int i, double lat, double lon, long t, float ac){
		this.SID = i;
		this.LATITUDE = lat;
		this.LONGITUDE = lon;
		this.TIME = t;
		this.ACCURACY = ac;
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
	public float getAccuracy(){
		return this.ACCURACY;
	}
	@Override
	public boolean equals(Object o){
		boolean equals = false;
		if(o instanceof Data){
			Data object = (Data) o;
			equals = (this.SID == object.getSID()
		 		   && this.LATITUDE == object.getLatitude()
		 		   && this.LONGITUDE == object.getLongitude()
				   && this.TIME == object.getTime()
				   && this.ACCURACY == object.getAccuracy());
		}
		return equals;
	}
	@Override
	public String toString(){
		return "{\"SID\":\""+this.SID+"\",\"LATITUDE\":\""+this.LATITUDE+"\",\"LONGITUDE\":\""+this.LONGITUDE+"\",\"TIME\":\""+this.TIME+"\",\"ACCURACY\":\""+this.ACCURACY+"\"}";
	}
	//@Override
	public boolean finalization(){
		boolean finalization = true;
		try{
			this.finalize();
		}
		catch(java.lang.Throwable e){
			finalization = false;
			e.printStackTrace();
		}
		return finalization;
	}

}
