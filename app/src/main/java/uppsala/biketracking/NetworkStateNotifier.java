package uppsala.biketracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Sergej on 17/12/2015.
 */
public class NetworkStateNotifier extends BroadcastReceiver {
    private static boolean available = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (connection_available(context)) {
            if(!available) {
                if ( ApiService.active() && ( ( ApiService.do_upload() && !Upload.uploading() ) || ( ApiService.do_correct() && !Correct.correcting() ) ) ) {
                    context.startService(new Intent(context, ApiService.class).setAction(C.NETWORK_NOTIFICATION_TXT));
                }
                available = true;
            }
        } else {
            available = false;
        }
    }
    private boolean connection_available(Context context){
        NetworkInfo activeNetwork = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }
    public static boolean available(){
        return available;
    }
}
