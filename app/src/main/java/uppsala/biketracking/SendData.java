package uppsala.biketracking;



import android.util.*;
import com.google.android.gms.internal.*;
import java.io.*;
import java.net.*;
import org.apache.http.entity.*;

public class SendData implements Runnable
{
	private String input, output;
	boolean uploading;
	public SendData(){
		this.input = "";
		this.output = "";
		this.uploading = false;
	}
	public void setInput(String i){
		this.input = i;
	}
	private void setUploading(boolean uploading){
		this.uploading = uploading;
		if(!uploading){
			this.output = "";
			this.setInput("");
		}
	}
	public boolean uploading(){
		return this.uploading;
	}
	//private String getOutput(){
	//	return this.output;
	//}
	public void run() {
		if(!this.input.equals("")){
			//this.output = "";
			this.setUploading(true);
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
				//this.output = "SUCCESS";
				if(ActivityRecognitionService.active){
					ActivityRecognitionService.successfullyUploaded();
				}
			}
			else{
				Log.i("UPLOAD", "FAILED: "+this.output);
				//this.output = "FAIL";
				if(ActivityRecognitionService.active){
					ActivityRecognitionService.failedToUpload();
				}
			}
			
		}
		catch(Exception e) {
			Log.i("UploadException","Down here");
			e.printStackTrace();
			if(ActivityRecognitionService.active){
				ActivityRecognitionService.failedToUpload();
			}
		}
		this.setUploading(false);
		}
	}
}
