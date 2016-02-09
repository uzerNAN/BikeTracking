package uppsala.biketracking;

//import android.content.*;

import java.io.Serializable;

public class PlaceRange implements Serializable
{
    private int sid, pid;
    private double startLat, startLon, stopLat, stopLon;
    private long startTime, stopTime;
    private float accuracy, speed;

    //public PlaceRange(){ set(0,0,0,0,0,0,-1); }
    public PlaceRange(int sid, double startLat, double startLon, double stopLat, double stopLon, long startTime, long stopTime, float speed, float accuracy, int pid){
        set(sid, startLat, startLon, stopLat, stopLon, startTime, stopTime, speed, accuracy, pid);
    }
    public void set(int sId, double startLa, double startLo, double stopLa, double stopLo, long startTim, long stopTim, float spd, float acc, int pId){
        sid = sId; startLat = startLa; startLon = startLo; stopLat = stopLa; stopLon = stopLo; startTime = startTim; stopTime = stopTim; speed = spd; accuracy = acc; pid = pId;
    }
    public int getSID(){
        return sid;
    }
    //public void setSID(int sid){ SID = sid; }
    public double getStartLat(){
        return startLat;
    }
    public double getStartLon(){
        return startLon;
    }
    public long getStartTime(){
        return startTime;
    }
    public double getStopLat(){
        return startLat;
    }
    public double getStopLon(){
        return startLon;
    }
    public long getStopTime(){
        return startTime;
    }
    public float getSpeed(){
        return speed;
    }

    public float getAccuracy(){
        return accuracy;
    }
    public int getPID(){
        return pid;
    }

    //public void correct(double lat, double lon, int pi){
    //    LATITUDE = lat; LONGITUDE = lon; placeId = pi;
    //}

    @Override
    public boolean equals(Object o){
        boolean equals = false;
        if(o instanceof Data){
            PlaceRange object = (PlaceRange) o;
            equals = (sid == object.getSID()
                    && startLat == object.getStartLat()
                    && startLon == object.getStartLon()
                    && startTime == object.getStartTime()
                    && stopLat == object.getStopLat()
                    && stopLon == object.getStopLon()
                    && stopTime == object.getStopTime()
                    && accuracy == object.getAccuracy()
                    && speed == object.getSpeed()
                    && pid == object.getPID());
        }
        return equals;
    }

    public String toJSONString(){
        return C.JSON_OBJ_START
                + C.SID_TXT + C.JSON_STR_EQUALS + sid + C.JSON_STR_COMMA
                + C.START_LAT_TXT + C.JSON_STR_EQUALS + startLat + C.JSON_STR_COMMA
                + C.START_LON_TXT + C.JSON_STR_EQUALS + startLon + C.JSON_STR_COMMA
                + C.STOP_LAT_TXT + C.JSON_STR_EQUALS + stopLat + C.JSON_STR_COMMA
                + C.STOP_LON_TXT + C.JSON_STR_EQUALS + stopLon + C.JSON_STR_COMMA
                + C.START_TIME_TXT + C.JSON_STR_EQUALS + startTime + C.JSON_STR_COMMA
                + C.STOP_TIME_TXT + C.JSON_STR_EQUALS + stopTime + C.JSON_STR_COMMA
                + C.SPEED_TXT + C.JSON_STR_EQUALS + speed + C.JSON_STR_COMMA
                + C.ACCURACY_TXT + C.JSON_STR_EQUALS + accuracy + C.JSON_STR_COMMA
                + C.PID_TXT + C.JSON_STR_EQUALS + pid
             + C.JSON_STR_OBJ_END;
    }

    public String toFileString(){
        return    C.SID_TXT + C.SPACE + sid + C.SPLIT
                + C.START_LAT_TXT + C.SPACE + startLat + C.SPLIT
                + C.START_LON_TXT + C.SPACE + startLon + C.SPLIT
                + C.STOP_LAT_TXT + C.SPACE + stopLat + C.SPLIT
                + C.STOP_LON_TXT + C.SPACE + stopLon + C.SPLIT
                + C.START_TIME_TXT + C.SPACE + startTime + C.SPLIT
                + C.STOP_TIME_TXT + C.SPACE + stopTime + C.SPLIT
                + C.SPEED_TXT + C.SPACE + speed + C.SPLIT
                + C.ACCURACY_TXT + C.SPACE + accuracy + C.SPLIT
                + C.PID_TXT + C.SPACE + pid + C.NEW_LINE;
    }
    @Override
    public String toString(){
        return toJSONString();
    }

    //public String toFileString(){
     //   return 	  C.SID_TXT + C.SPACE + SID + C.SPLIT
     //           + C.LAT_TXT + C.SPACE + LATITUDE + C.SPLIT
     //           + C.LON_TXT + C.SPACE + LONGITUDE + C.SPLIT
     //           + C.TIME_TXT + C.SPACE + TIME + C.SPLIT
    //            + C.SPEED_TXT + C.SPACE + SPEED + C.SPLIT
    //            + C.ACCURACY_TXT + C.SPACE + ACCURACY;
    //}

    //@Override
    //public boolean finalization(){
    //    boolean finalization = true;
    //    try{
    //        this.finalize();
    //    }
    //    catch(java.lang.Throwable e){
    //        finalization = false;
    //        e.printStackTrace();
    //    }
    //    return finalization;
    //}

}