package uppsala.biketracking;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;


/**
 * Created by Sergej Maleev on 2015-12-11.
 */
public class PowerSavingModule extends IntentService {
    private static int previous_battery_state = 0;
    public PowerSavingModule(){ super(PowerSavingModule.class.getName()); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null) {
            switch (intent.getAction()) {
                case C.BATTERY_CHECK_TXT :
                    if(intent.getBooleanExtra(C.BATTERY_ALARM_TXT, true)) {
                        startBatteryAlarm();
                    } else {
                        stopBatteryAlarm();
                    }
                    updateBatteryState();
                    break;
                default :
                    break;
            }
        }
    }

    private void startBatteryAlarm(){
        if(PendingIntent.getService(
                getApplicationContext(),
                C.POWER_ID,
                new Intent(getApplicationContext(), PowerSavingModule.class).setAction(C.BATTERY_CHECK_TXT),
                PendingIntent.FLAG_NO_CREATE) == null) {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_HOUR,
                    PendingIntent.getService(
                            getApplicationContext(),
                            C.POWER_ID,
                            new Intent(getApplicationContext(), PowerSavingModule.class).setAction(C.BATTERY_CHECK_TXT),
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private void stopBatteryAlarm(){
        PendingIntent alarm = PendingIntent.getService(
                getApplicationContext(),
                C.POWER_ID,
                new Intent(getApplicationContext(), PowerSavingModule.class).setAction(C.BATTERY_CHECK_TXT),
                PendingIntent.FLAG_NO_CREATE);
        if(alarm != null) {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(alarm);
        }
    }

    public static boolean battery_high(){
        return (previous_battery_state > C.BATTERY_GOOD);
    }

    public static boolean battery_low(){
        return (previous_battery_state <= C.BATTERY_CRITICAL);
    }

    private void updateBatteryState(){
        int b = getBatteryInPercent();
        if((b > C.BATTERY_GOOD && (!ApiService.active() || !battery_high()))
        || (b > C.BATTERY_CRITICAL && (!ApiService.active() || battery_low() || battery_high()))
        || (b <= C.BATTERY_CRITICAL && ApiService.active() && (!battery_low()|| previous_battery_state == 0))){
            startService(new Intent(getApplicationContext(), ApiService.class).setAction(C.CHECK_BATTERY_STATE_TXT));
        }
        switch(previous_battery_state){
            case 0 :
                if(!(NetworkStateNotifier.available())){
                    sendBroadcast(new Intent(getApplicationContext(), NetworkStateNotifier.class));
                }
            default :
                previous_battery_state = b;
                break;
        }
    }

    private int getBatteryInPercent(){
        int out = 0;
        Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(i!=null) {
            out = ((i.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) * 100) / i.getIntExtra(BatteryManager.EXTRA_SCALE, 0)); // in %
        }
        return out;
    }
}
