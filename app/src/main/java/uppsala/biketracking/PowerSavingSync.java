package uppsala.biketracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sergej Maleev on 2015-12-11.
 */
public class PowerSavingSync extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, PowerSavingModule.class).setAction("BATTERY_CHECK"));
    }
}
