package uppsala.biketracking;



import android.util.*;
import com.google.android.gms.internal.*;
import java.io.*;
import java.net.*;

public class SendData implements Runnable
{
	private String input, output = null;
	public SendData(String i){
		this.input = i;
	}
	public void resetOutput(){
		this.output = null;
	}
	public String getOutput(){
		return this.output;
	}
	public void run() {
		try{
			URL url = new URL("http://130.243.235.172:8080/MyServer/MyServlet");
			URLConnection connection = url.openConnection();
			
			connection.setDoOutput(true);
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			Log.i("InputString",this.input);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(this.input);
			out.close();
			this.output = "";

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((this.input = in.readLine()) != null){
				this.output += this.input;
			}
			in.close();
			if(!this.output.equals("")){
				Log.i("UPLOAD", "IS SUCCESSFUL");
				this.output = "SUCCESS";
				if(WakefulService.mainService != null){
					WakefulService.mainService.successfullyUploaded();
				}
			}
			else{
				Log.i("UPLOAD", "FAILED");
				this.output = "FAIL";
				if(WakefulService.mainService != null){
					WakefulService.mainService.failedToUpload();
				}
			}
			//this.resetOutput();
			
		}
		catch(Exception e) {
			Log.i("UploadException",e.toString());
			if(WakefulService.mainService != null){
				WakefulService.mainService.failedToUpload();
			}
		}
	}
}
