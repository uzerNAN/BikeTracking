package uppsala.biketracking;

//import android.content.*;

public class Data
{
	private int SID;
	private final double LATITUDE, LONGITUDE;
	//private final int latitude;
	private final long TIME;
	private final float ACCURACY;
	private float SPEED;
	public Data(int sid, double latitude, double longitude, long time, float speed, float accuracy){
		this.SID = sid;
		this.LATITUDE = latitude;
		this.LONGITUDE = longitude;
		this.TIME = time;
		this.SPEED = speed;
		this.ACCURACY = accuracy;
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
	public float getSpeed(){
		return this.SPEED;
	}
	public void setSpeed(float speed){
		this.SPEED = speed;
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
				   && this.ACCURACY == object.getAccuracy()
				   && this.SPEED == object.getSpeed());
		}
		return equals;
	}
	@Override
	public String toString(){
		return "{\"SID\":\""+this.SID+"\",\"LATITUDE\":\""+this.LATITUDE+"\",\"LONGITUDE\":\""+this.LONGITUDE+"\",\"TIME\":\""+this.TIME+"\",\"SPEED\":\""+this.SPEED+"\",\"ACCURACY\":\""+this.ACCURACY+"\"}";
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
