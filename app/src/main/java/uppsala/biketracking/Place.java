package uppsala.biketracking;

/**
 * Created by Sergej on 04/02/2016.
 */
public class Place {
    private final int pid;
    private final double latitude, longitude;
    public Place(int p, double lat, double lon){
        pid = p;
        latitude = lat;
        longitude = lon;
    }
    public int PID(){
        return pid;
    }
    public double LATITUDE(){
        return latitude;
    }
    public double LONGITUDE(){
        return longitude;
    }
    @Override
    public boolean equals(Object o){
        boolean equals = false;
        if(o instanceof Place){
            Place object = (Place) o;
            equals = (pid == object.PID()
                    && latitude == object.LATITUDE()
                    && longitude == object.LONGITUDE());
        }
        return equals;
    }

    public String toJSONString(){
        return C.JSON_OBJ_START
                + C.PID_TXT + C.JSON_STR_EQUALS + pid + C.JSON_STR_COMMA
                + C.LAT_TXT + C.JSON_STR_EQUALS + latitude + C.JSON_STR_COMMA
                + C.LON_TXT + C.JSON_STR_EQUALS + longitude
                + C.JSON_STR_OBJ_END;
    }

    public String toFileString(){
        return    C.PID_TXT + C.SPACE + pid + C.SPLIT
                + C.LAT_TXT + C.SPACE +  latitude + C.SPLIT
                + C.LON_TXT + C.SPACE + longitude + C.NEW_LINE;
    }
    @Override
    public String toString(){
        return toJSONString();
    }
}
