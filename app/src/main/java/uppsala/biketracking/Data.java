package uppsala.biketracking;

//import android.content.*;

import java.io.Serializable;

public class Data implements Serializable
{
	private int SID;
	private double LATITUDE, LONGITUDE;
	private long TIME;
	private float ACCURACY, SPEED;
	private int placeId;

	public Data(){ set(0,0,0,0,0,0); }

	public Data(int sid, double latitude, double longitude, long time, float speed, float accuracy){
		set(sid, latitude, longitude, time, speed, accuracy);
	}
	public void set(int sid, double latitude, double longitude, long time, float speed, float accuracy) {
		SID = sid; LATITUDE = latitude; LONGITUDE = longitude; TIME = time; SPEED = speed; ACCURACY = accuracy;
	}
	public int getSID(){
		return SID;
	}
	//public void setSID(int sid){ SID = sid; }
	public double getLatitude(){
		return LATITUDE;
	}
	public double getLongitude(){ return LONGITUDE; }
	public long getTime(){
		return TIME;
	}
	public float getSpeed(){
		return SPEED;
	}
	//public void setSpeed(float speed){ SPEED = speed; }
	public float getAccuracy(){
		return ACCURACY;
	}
	public int getPID(){
		return placeId;
	}

	public Data correct(double lat, double lon, int pi){
		LATITUDE = lat; LONGITUDE = lon; placeId = pi;
		return this;
	}

	@Override
	public boolean equals(Object o){
		boolean equals = false;
		if(o instanceof Data){
			Data object = (Data) o;
			equals = (SID == object.getSID()
		 		   && LATITUDE == object.getLatitude()
		 		   && LONGITUDE == object.getLongitude()
				   && TIME == object.getTime()
				   && ACCURACY == object.getAccuracy()
				   && SPEED == object.getSpeed());
		}
		return equals;
	}
	@Override
	public String toString(){
		return C.JSON_OBJ_START
				+ C.SID_TXT + C.JSON_STR_EQUALS + SID + C.JSON_STR_COMMA
				+ C.LAT_TXT + C.JSON_STR_EQUALS + LATITUDE + C.JSON_STR_COMMA
				+ C.LON_TXT + C.JSON_STR_EQUALS + LONGITUDE + C.JSON_STR_COMMA
				+ C.TIME_TXT + C.JSON_STR_EQUALS + TIME + C.JSON_STR_COMMA
				+ C.SPEED_TXT + C.JSON_STR_EQUALS + SPEED + C.JSON_STR_COMMA
				+ C.ACCURACY_TXT + C.JSON_STR_EQUALS + ACCURACY + C.JSON_STR_COMMA
				+ C.PID_TXT + C.JSON_STR_EQUALS + placeId
			 + C.JSON_STR_OBJ_END;
	}

	public String toFileString(){
		return 	  C.SID_TXT + C.SPACE + SID + C.SPLIT
				+ C.LAT_TXT + C.SPACE + LATITUDE + C.SPLIT
				+ C.LON_TXT + C.SPACE + LONGITUDE + C.SPLIT
				+ C.TIME_TXT + C.SPACE + TIME + C.SPLIT
				+ C.SPEED_TXT + C.SPACE + SPEED + C.SPLIT
				+ C.ACCURACY_TXT + C.SPACE + ACCURACY + C.NEW_LINE;
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
