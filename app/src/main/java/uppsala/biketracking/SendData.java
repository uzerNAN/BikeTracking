package uppsala.biketracking;



import android.util.*;
import com.google.android.gms.internal.*;
import java.io.*;
import java.net.*;

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
			URL url = new URL("http://130.243.235.172:8080/MyServer/MyServlet");
			URLConnection connection = url.openConnection();
			
			connection.setDoOutput(true);
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			Log.i("InputString",this.input);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(this.input);
			out.close();
			

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((this.input = in.readLine()) != null){
				this.output += this.input;
			}
			in.close();
			if(!this.output.equals("")){
				Log.i("UPLOAD", "IS SUCCESSFUL");
				//this.output = "SUCCESS";
				if(WakefulService.mainService != null){
					WakefulService.mainService.successfullyUploaded();
				}
			}
			else{
				Log.i("UPLOAD", "FAILED");
				//this.output = "FAIL";
				if(WakefulService.mainService != null){
					WakefulService.mainService.failedToUpload();
				}
			}
			
		}
		catch(Exception e) {
			Log.i("UploadException",e.toString());
			if(WakefulService.mainService != null){
				WakefulService.mainService.failedToUpload();
			}
		}
		this.setUploading(false);
		}
	}
}
