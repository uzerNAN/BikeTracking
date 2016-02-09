package uppsala.biketracking;



import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class Upload implements Runnable
{
	private static Context context = null;
	private CollectedData correct;
	public Upload(Context c){ context = c; correct = new CollectedData(); }
	public static boolean uploading(){
		return context != null;
	}
	public void run() {
		boolean result = true;
		if(!Correct.correcting()) {
			boolean not_done = true;
			while (not_done) {
				if (!ApiService.rl_is_active() && !Correct.correcting()) {
					RecordLocationSensor.resetSettings();
					correct.importRoadFile(C.CORRECTED_ROADS);
					correct.importPlaceFile(C.CORRECTED_PLACES);
					correct.importRangeFile(C.CORRECTED_RANGES);
					if (!correct.isEmpty()) {
						try {
							URL url = new URL(C.UPLOAD_URL);
							URLConnection connection = url.openConnection();
							//connection.setDoInput(true);
							connection.setDoOutput(true);
							//connection.setRequestProperty("Content-Type", "application/json");
							connection.setRequestProperty(C.ContentType_TXT, C.RequestProperty_TXT);
							//connection.setConnectTimeout(5000);
							//connection.setReadTimeout(10000);
							//connection.connect();
							OutputStream outstream = connection.getOutputStream();
							if (outstream != null) {
								OutputStreamWriter out = new OutputStreamWriter(outstream);
								//this.input = this.input;
								String input = C.data_TXT + C.EQ + correct.toString();
								out.write(input);
								out.flush();
								out.close();
								Log.i(C.InputString_TXT, input);
								InputStreamReader reader = new InputStreamReader(connection.getInputStream());
								BufferedReader in =
										new BufferedReader(reader);
								String output = C.EMPTY;
								while ((input = in.readLine()) != null) {
									output += input;
									Log.i(C.OutputLine_TXT, input);
								}

								in.close();
								//out.close();
								if (output.equals(C.OK_TXT)) {
									Log.i(C.UPLOAD_TXT, C.SUCCESS_TXT);

									new File(C.getSaveDirectory(), C.CORRECTED_ROADS).delete();
									new File(C.getSaveDirectory(), C.CORRECTED_PLACES).delete();
									new File(C.getSaveDirectory(), C.CORRECTED_RANGES).delete();
								} else {
									Log.i(C.UPLOAD_TXT, C.FAILED_TXT + C.COLON + C.SPACE + output);
									result = false;
									not_done = false;
								}
							} else {
								result = false;
								not_done = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							result = false;
							not_done = false;
						}
					} else {
						not_done = false;
					}
				} else {
					not_done = false;
					result = false;
				}
			}
		}
		context.startService(new Intent(context, ApiService.class).setAction(C.UPLOAD_TXT).putExtra(C.SUCCESS_TXT, result));
		context = null;
	}
}