package uppsala.biketracking;

import android.content.*;
import android.os.*;
import android.support.v4.content.*;
import android.util.*;
import android.widget.*;

public class WakefulReceiver extends WakefulBroadcastReceiver
{
	
	@Override
	public void onReceive(Context context, Intent intent){
		Bundle bundle = intent.getExtras();
		String name = "";
		int confidence = 0;
		int type = 0;
		if (bundle != null) {
			name = bundle.getString("name");
			confidence = bundle.getInt("confidence");
			type = bundle.getInt("type");
			//if (resultCode == RESULT_OK) {
			Log.i("WakefulReceiver",type + " : '" + name + "' | " + confidence+"%");
			Toast.makeText(context,
						  type + " : '" + name + "' | " + confidence+"%", Toast.LENGTH_LONG).show();
			//} else {
			//	Toast.makeText(MainActivity.this, "Download failed",
			//				   Toast.LENGTH_LONG).show();
			//	textView.setText("Download failed");
			//}
		}
		else {
			Toast.makeText(context,
						   "NO ACTIVITY RESULT", Toast.LENGTH_LONG).show();
		}
		//intent = new Intent(context, ActivityRecognitionIS.class);
		//intent.putExtra("name", name);
		//intent.putExtra("confidence", confidence);
		//intent.putExtra("type", type);
		//startWakefulService(context, intent);
	}
	
}
