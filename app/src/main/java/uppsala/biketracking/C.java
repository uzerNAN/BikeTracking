package uppsala.biketracking;



import android.os.Environment;

import com.google.android.gms.location.DetectedActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Sergej Maleev on 2015-12-10.
 */
public class C {
    private C(){ }

    public static final int AR_TIME = 60000; // in milliseconds
    public static final int AR_FASTEST_TIME = 30000;

    public static final int LU_TIME = 30000;
    public static final int LU_FASTEST_TIME = 10000;

    public static final int RL_TIME = 60000;
    public static final int RL_FASTEST_TIME = 15000;

    public final static int SESSION_TIMEOUT = 600000;//ms = 10 min

    public final static long WAIT_FOR_NEXT_BATTERY_CHECK = 3600000;//ms = 1 h

    public final static int BATTERY_CRITICAL = 14;
    public final static int BATTERY_GOOD = 20;

    public final static int POWER_ID = 6972;
    public static final int AR_ID = 6973;
    public static final int RL_ID = 6974;

    public static final String ROOT = "BikeTrackingData";
    public static final String SETTINGS = "record_settings.txt";
    public static final String TEMPORARY_DATA = "temporary_points.txt";
    public static final String WAITING_TEMPORARY_DATA = "waiting_temporary_points.txt";
    public static final String WAITING_CORRECTED_DATA = "waiting_points_to_upload.txt";
    public static final String CORRECTED_DATA = "points_to_upload.txt";
    public static final String RAW_DATA = "points_to_correct.txt";
    public static final String UPLOAD_URL = "http://biketracking.duckdns.org:3000/GPSdata";
    public static final String CORRECT_URL = "https://roads.googleapis.com/v1/snapToRoads";

    public static final String MATCH_SYMBOLS = "[A-Za-z0-9.| ]*";

    public static final String ACTIVITY_NAME_TXT = "ACTIVITY_NAME";
    public static final String RECOGNITION_DATA_TXT = "RECOGNITION_DATA";
    public static final String IN_VEHICLE_TXT = "IN VEHICLE";
    public static final String ON_BICYCLE_TXT = "ON BICYCLE";
    public static final String ON_FOOT_TXT = "ON FOOT";
    public static final String STILL_TXT = "STILL";
    public static final String UNKNOWN_TXT = "UNKNOWN";
    public static final String TILTING_TXT = "TILTING";
    public static final String NO_ACTIVITY_TXT = "NO ACTIVITY";
    //public static final String DATA_UPLOADED_TXT = "DATA_UPLOADED";
    public static final String UPLOAD_ERROR_TXT = "UPLOAD_ERROR";
    public static final String FILE_ERROR_TXT = "FILE_ERROR";
    public static final String LOCATION_DATA_TXT = "LOCATION_DATA";
    public static final String LAT_TXT = "LAT";
    public static final String LON_TXT = "LON";
    public static final String SPACE = " ";
    public static final String ERROR_MAP_TXT = "Sorry! Unable to create maps";
    public static final String ERROR_FILE_TXT = "UNABLE TO WRITE IN A FILE";
    public static final String ERROR_UPLOAD_TXT = "UNABLE TO UPLOAD DATA";
    public static final String ERROR_CORRECT_TXT = "UNABLE TO CORRECT DATA";
    public static final String SUCCESS_UPLOAD_TXT = "Data was successfully uploaded to server";
    public static final String SUCCESS_CORRECT_TXT = "Data was successfully corrected with webservice Roads.API";
    public static final String SID_TXT = "SID";
    public static final String SPLIT = "|";
    public static final String TIME_TXT = "TIME";
    public static final String LAST_LOCATION_TXT = "LAST_LOCATION";
    public static final String ACCURACY_TXT = "ACCURACY";
    public static final String SPEED_TXT = "SPEED";
    public static final String NEW_LINE = "\r\n";
    public static final String FILE_PERMISSION_TXT = "FILE_PERMISSION";
    public static final String BATTERY_CHECK_TXT = "BATTERY_CHECK";
    public static final String UNKNOWN_LINE_TXT = "UNKNOWN LINE";
    public static final String ImportSettings_TXT = "ImportSettings";
    public static final String COLON = ":";
    public static final String UNKNOWN_CONTENT_TXT = "FILE LINE HAS DIFFERENT CONTENT";
    public static final String DATA_RESULT_TXT = "DATA_RESULT";
    //public static final String CORRECTED_TXT = "CORRECTED";
    //public static final String UPLOADED_TXT = "UPLOADED";
    public static final String CHECK_BATTERY_STATE_TXT = "CHECK_BATTERY_STATE";
    //public static final String BATTERY_HIGH_TXT = "BATTERY_HIGH";
    //public static final String BATTERY_LOW_TXT = "BATTERY_LOW";
    public static final String JSON_OBJ_START = "{\"";
    public static final String JSON_VAR_OBJ_END = "}";
    public static final String JSON_STR_OBJ_END = "\"}";
    public static final String JSON_STR_EQUALS = "\":\"";
    public static final String JSON_VAR_EQUALS = "\":";
    public static final String JSON_STR_COMMA = "\",\"";
    public static final String JSON_LIST_COMMA = ",";
    public static final String JSON_LIST_START = "[";
    public static final String JSON_LIST_END = "]";
    public static final String JSON_EMPTY = "{}";
    public static final String data_TXT = "data";
    public static final String EMPTY = "";
    public static final String NETWORK_NOTIFICATION_TXT = "NETWORK_NOTIFICATION";
    //public static final String AVAILABLE_TXT = "AVAILABLE";
    public static final String ACTIVITY_RECOGNITION_TXT = "ACTIVITY_RECOGNITION";
    public static final String RECORD_LOCATION_TXT = "RECORD_LOCATION";
    public static final String LOCATION_UPDATES_TXT = "LOCATION_UPDATES";
    public static final String START_TXT = "START";
    //public static final String DATA_CORRECTED_TXT = "DATA_CORRECTED";
    public static final String EQ = "=";
    public static final String InputString_TXT = "InputString";
    public static final String OutputLine_TXT = "OutputLine";
    public static final String OK_TXT = "OK";
    public static final String UPLOAD_TXT = "UPLOAD";
    public static final String SUCCESS_TXT = "SUCCESS";
    public static final String FAILED_TXT = "FAILED";
    public static final String ContentType_TXT = "Content-Type";
    public static final String RequestProperty_TXT = "application/x-www-form-urlencoded;charset=UTF-8";
    public static final String AND = "&";
    public static final String interpolate_TXT = "interpolate";
    public static final String key_TXT = "key";
    public static final String false_TXT = "false";
    public static final String path_TXT = "path";
    public static final String Q = "?";
    public static final String CORRECT_TXT = "CORRECT";
    public static final String COMMA = ",";
    public static final String snappedPoints_TXT = "snappedPoints";
    public static final String originalIndex_TXT = "originalIndex";
    public static final String location_TXT = "location";
    public static final String latitude_TXT = "latitude";
    public static final String longitude_TXT = "longitude";
    public static final String placeId_TXT = "placeId";
    public static final String PID_TXT = "PID";
    public static final String GOOGLE_SERVICES_UNAVAILABLE_TXT = "GOOGLE_SERVICES_UNAVAILABLE";
    public static final String STATE_TXT = "STATE";
    public static final String BATTERY_ALARM_TXT = "BATTERY_ALARM";
    //public static final String FLUSH_BUFFER_TXT = "FLUSH_BUFFER";
    public static final String SNAP_API_TXT = "SNAP_API";
    public static final String Start_AR_TXT = "Start AR";
    public static final String Stop_AR_TXT = "Stop AR";
    public static final String Start_RL_TXT = "Start RL";
    public static final String Stop_RL_TXT = "Stop RL";
    public static final String Correct_ON_TXT = "Correct ON";
    public static final String Correct_The_Data_TXT = "Correct Data";
    public static final String Upload_ON_TXT = "Upload ON";
    public static final String Upload_The_Data_TXT = "Upload Data";
    public static final String CONNECTION_UNAVAILABLE_TXT = "CONNECTION UNAVAILABLE";
    public static final String UPDATE_BUTTON_TXT = "UPDATE_BUTTON";
    public static final String BUTTON_TXT = "BUTTON";
    public static final String NOT_OK_TXT = "NOT OK";
    public static final String NO_PID = "NO_PID";
    public static final String _TXT = "";

    public static File getSaveDirectory(){
        File appDir = new File(Environment.getExternalStorageDirectory()+File.separator+ROOT);
        if(!appDir.exists() || !appDir.isDirectory()){
            appDir.mkdir();
        }
        return appDir;
    }

    public static String getActivityName(int type){
        String name;
        switch(type){
            case(DetectedActivity.IN_VEHICLE):
                name = IN_VEHICLE_TXT;
                break;
            case(DetectedActivity.ON_BICYCLE):
                name = ON_BICYCLE_TXT;
                break;
            case(DetectedActivity.ON_FOOT):
                name = ON_FOOT_TXT;
                break;
            case(DetectedActivity.STILL):
                name = STILL_TXT;
                break;
            case(DetectedActivity.UNKNOWN):
                name = UNKNOWN_TXT;
                break;
            case(DetectedActivity.TILTING):
                name = TILTING_TXT;
                break;
            default:
                name = NO_ACTIVITY_TXT;
                break;
        }
        return name;
    }

    public static synchronized boolean appendFileTo(String from, String to){
        File fileFrom = new File(getSaveDirectory(), from);
        File fileTo = new File(getSaveDirectory(), to);
        boolean fromExists = fileFrom.exists() && fileFrom.length() != 0;
        if(fromExists){
            if (fileTo.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(fileFrom));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileTo, true));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.append(line);
                    }
                    reader.close();
                    writer.close();
                    fileFrom.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                fileFrom.renameTo(fileTo);
            }
        } else if(fileFrom.length() == 0){
            fileFrom.delete();
        }
        return fromExists;
    }

    public static synchronized boolean writeFile(String path, String line, boolean append){
        boolean write = false;
        File updateLog = new File(getSaveDirectory(), path);

        try{
            if(!updateLog.exists()){
                updateLog.createNewFile();
            }
            BufferedWriter buf = new BufferedWriter(new FileWriter(updateLog, append));
            if(append){
                buf.append(line);
            }
            else{
                buf.write(line);
            }
            buf.close();
            write = true;
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return write;
    }
    public static long long_diff(long l1, long l2){
        return (l1-l2);
    }
}
