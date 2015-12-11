package uppsala.biketracking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Sergej Maleev on 2015-12-10.
 */
public class Constants {
    private Constants(){ }

    public static final int AR_TIME = 60000; // in milliseconds
    public static final int AR_FASTEST_TIME = 30000;

    public static final int LU_TIME = 30000;
    public static final int LU_FASTEST_TIME = 10000;

    public static final int RL_TIME = 60000;
    public static final int RL_FASTEST_TIME = 15000;

    public final static int SESSION_TIMEOUT = 600000;//ms = 10 min

    public final static long WAIT_FOR_NEXT_BATTERY_CHECK = 3600000;//ms = 1 h

    public final static int BATTERY_LOW = 14;
    public final static int BATTERY_HALF = 50;

    public static final String SETTINGS_PATH = "sdcard/recording_settings.txt";
    public static final String DATA_PATH = "sdcard/collected_data.txt";

    public static boolean writeFile(String path, String line, boolean append){
        boolean write = false;
        File updateLog = new File(path);

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
