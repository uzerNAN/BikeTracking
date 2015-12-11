package uppsala.biketracking;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Created by Sergej Maleev on 2015-12-11.
 */
public class PowerSavingModule extends Service {
    public static boolean battery_low = false;
    public static boolean battery_high = false;
    public static boolean active = false;
    private static boolean alarm_is_active = false;
    //private static PowerSavingModule module = null;
    AlarmManager manager = null;
    @Override
    public void onCreate(){
        super.onCreate();
        active = true;
        manager = ((AlarmManager) getSystemService(Context.ALARM_SERVICE));
        startBatteryCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            switch (action) {
            case "BATTERY_CHECK" :
                updateBooleans();
                break;
            default :
                break;
            }
        }
        return START_STICKY_COMPATIBILITY;
    }

    private void startBatteryCheck(){
        if(!alarm_is_active) {
            manager.setInexactRepeating(
                    AlarmManager.RTC,
                    System.currentTimeMillis(),
                    AlarmManager.INTERVAL_HOUR,
                    PendingIntent.getService(
                            getApplicationContext(),
                            0,
                            new Intent(this, PowerSavingModule.class).setAction("BATTERY_CHECK"),
                            PendingIntent.FLAG_UPDATE_CURRENT));
            alarm_is_active = true;
        }
    }

    private void stopBatteryCheck(){
        if(alarm_is_active) {
            manager.cancel(PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    new Intent(this, PowerSavingModule.class).setAction("BATTERY_CHECK"),
                    PendingIntent.FLAG_UPDATE_CURRENT));
            alarm_is_active = false;
        }
    }

    private void updateBooleans(){
        int b = getBatteryInPercent();
        if(b > Constants.BATTERY_HALF){
            battery_high = true;
            battery_low = false;
        }
        else{
            battery_high = false;
            if(battery_low){
                if(b > Constants.BATTERY_LOW) {
                    battery_low = false;
                    if(!ApiService.ar_is_active) {
                        startService(new Intent(this, ApiService.class).setAction("START_AR"));
                    }
                }
            }
            else{
                if(b <= Constants.BATTERY_LOW){
                    battery_low = true;
                    if(!MainActivity.active && ApiService.active){
                        stopService(new Intent(this, ApiService.class));
                    }
                }
            }
        }
    }

    private int getBatteryInPercent(){
        Bundle values;
        int out = 0;
        Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(i!=null) {
            values = i.getExtras();
            out = ((values.getInt(BatteryManager.EXTRA_LEVEL) * 100) / values.getInt(BatteryManager.EXTRA_SCALE)); // %
        }
        return out;
    }

    @Override
    public void onDestroy(){
        stopBatteryCheck();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
