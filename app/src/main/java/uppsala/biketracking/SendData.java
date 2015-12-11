package uppsala.biketracking;



import android.content.Context;
import android.content.Intent;
import android.util.*;
import com.google.android.gms.internal.*;
import java.io.*;
import java.net.*;
import org.apache.http.entity.*;

public class SendData implements Runnable
{
	private String input, output;
	private Context context;
	boolean uploading;
	public SendData(){
		input = "";
		output = "";
		uploading = false;
	}
	public void setInput(String i, Context c){
		input = i;
		context = c;

	}
	private void setUploading(boolean uploading){
		this.uploading = uploading;
		if(!uploading){
			this.output = "";
			setInput("", null);
		}
	}
	public boolean uploading(){
		return this.uploading;
	}
	//private String getOutput(){
	//	return this.output;
	//}
	public void run() {
		boolean result = false;
		if(!input.equals("")){
			setUploading(true);
			try{
				URL url = new URL("http://biketracking.duckdns.org:3000/GPSdata");
				URLConnection connection = url.openConnection();
				//connection.setDoInput(true);
				connection.setDoOutput(true);
				//connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				//connection.setConnectTimeout(5000);
				//connection.setReadTimeout(10000);
				//connection.connect();
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				//this.input = this.input;
				out.write("data="+this.input);
				out.flush();
				out.close();
				Log.i("InputString",this.input);
				InputStreamReader reader = new InputStreamReader(connection.getInputStream());
				BufferedReader in =
									new BufferedReader(reader);
				while((this.input = in.readLine()) != null){
					this.output += this.input;
					Log.i("OutputLine",this.input);
				}

				in.close();
				//out.close();
				if(this.output.equals("OK")){
					Log.i("UPLOAD", "IS SUCCESSFUL");
					result = true;

				}
				else{
					Log.i("UPLOAD", "FAILED: "+this.output);
				}
			}
			catch(Exception e) {
				Log.i("UploadException","Down here");
				e.printStackTrace();
			}
			this.setUploading(false);
			context.startService(new Intent(context, ApiService.class).putExtra("SUCCESS", result));
		}
	}
}
