package uppsala.biketracking;

import android.content.*;
import android.net.*;
import android.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class CollectedData
{
	
	private List<Data> data, update;
	//private boolean temp = true;
	private Context context;
	//private String temp;
	public CollectedData(Context c){
		this.context = c;
		this.data = new ArrayList<Data>();
		this.update = new ArrayList<Data>();
		//this.temp = "";
		this.importActivityFile();
		//){
		//	this.uploadData();
		//}
	}
	//public String getTemp(){
	//	return this.temp;
	//}
	public boolean uploading(){
		boolean uploading = false;
		if(this.send != null){
			if(this.send.getOutput()!=null){
				switch(this.send.getOutput()){
					case("SUCCESS"):
						this.send.resetOutput();
						break;
					case("FAIL"):
						this.send.resetOutput();
						break;
					default:
						uploading = true;
						break;
				}
			}
		}
		return uploading;
	}
	public boolean updateIsEmpty(){
		return this.update.size() == 0;
	}
	public void add(int id, double latitude, double longitude, long time){
		this.data.add(new Data(id, latitude, longitude, time));
	//}
	//public void addUpdate(int id, double latitude, double longitude, long time){
		this.update.add(this.data.get(this.data.size()-1));
		//Log.i("Add", this.data.get(this.data.size()-1).toString());
	}
	public List<Data> getBySID(int sid){
		List<Data> iter = new ArrayList<Data>();
		for(int i = 0; i < data.size(); i++){
			if(data.get(i).getSID() == sid){
				iter.add(data.get(i));
			}
		}
		return iter;
	}
	public Data get(int id){
		Data iter = null;
		if(id < data.size()){
			iter = data.get(id);
		}
		return iter;
	}
	public boolean isOnline(){
		/*boolean online = false;
		try{
			InetAddress ipAddr = InetAddress.getByName("130.243.235.172");
			online = !ipAddr.equals("");
		}
		catch(Exception e){
			Log.i("IsOnlineException",e.toString());
		}*/
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNI = cm.getActiveNetworkInfo();
		return activeNI != null && activeNI.isConnected();
		//return online;
	}
	public void resetUpdate(){
		this.update = new ArrayList<Data>();
		Log.i("ResetUpdate","UPDATE RESETED");
		//this.uploading = false;
	}
	public void uploadData(){
		//boolean success = false;
		if(this.isOnline() && this.update.size() > 0){
			send = new SendData(this.updateToString());
			new Thread(send).start();
			Log.i("UploadData","THREAD STARTED");
			//this.uploading = true;
		}
		else{
			Log.i("UploadData","UNABLE TO START THREAD: isOnline="+this.isOnline()+", update.size="+this.update.size());
		}
	}
	
	public void setIdSequence(int sid, int index){
		int prvId = -1;// = sid;
		if(index < this.data.size()){
			for(int i = index; i < this.data.size(); i++){
				if(prvId == -1){
					prvId = this.data.get(i).getSID();
				}
				else if(prvId < this.data.get(i).getSID()){
					sid++;
				}
				this.data.get(i).setSID(sid);
			}
		}
	}
	
	SendData send;
	
	public boolean exportActivityFile(){
		boolean imprt = true;
		File updateLog = new File(WakefulService.filePath);
		if(!updateLog.exists()){
			try{
				updateLog.createNewFile();
			}
			catch(IOException e){
				imprt = false;
				e.printStackTrace();
			}
		}
		if(imprt){
			try{
				BufferedWriter buf = new BufferedWriter(new FileWriter(updateLog, true));
				for(int i = 0; i < this.data.size(); i++){
					buf.append(this.data.get(i).toString());
					buf.newLine();
				}
				buf.append("UPLOADED");
				buf.newLine();
				buf.close();
			}
			catch(IOException e){
				imprt = false;
				e.printStackTrace();
			}
		}
		return imprt;
	}
	
	public boolean importActivityFile(){
		boolean imprt = true;
		File updateLog = new File(WakefulService.filePath);
		if(updateLog.exists()){
			try{
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitSID, splitTime, splitLatitude, splitLongitude;
				//int sid = 0;
				//Data item;
				//buf.
				while((line = buf.readLine()) != null){
					if(line.equals("UPLOADED")){
						//this.data.addAll(this.update);
						this.resetUpdate();
						//update = new ArrayList<Data>();
					}
					else if(line.matches("[A-Z0-9.| ]*")){
						splitLine = line.split("\\|");
						splitSID = splitLine[0].split(" ");
						splitLatitude = splitLine[1].split(" ");
						splitLongitude = splitLine[2].split(" ");
						splitTime = splitLine[3].split(" ");
						if(splitSID[0].equals("SID") && splitLatitude[0].equals("LATITUDE") && splitLongitude[0].equals("LONGITUDE") && splitTime[0].equals("TIME")){
							this.add(Integer.parseInt(splitSID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]), Long.parseLong(splitTime[1]));
						}
						else{
							Log.i("ImportActivityFile","FILE LINE HAS DIFFERENT CONTENT");
						}
					}
					else{
						Log.i("ImportActivityFile","UNKNOWN LINE: "+line);
						imprt = false;
						break;
					}
				}
				buf.close();
			}
			catch(ArrayIndexOutOfBoundsException e){
				imprt = false;
				e.printStackTrace();
				//given file has different structure
			}
			catch(IOException e){
				imprt = false;
				e.printStackTrace();
			}
			catch(Exception e){
				imprt = false;
				Log.i("ImportActivityFile",e.toString());
			}
		}
		if(imprt){
			this.uploadData();
		}
		else{
			this.resetUpdate();
		}
		Log.i("ImportActivityFile", "imprt="+imprt);
		
		//::://
		//if(this.temp){
		//	this.temp = !this.exportActivityFile();
		//}
		//::://
		
		return imprt;
	}
	public String toString(){
		return "{\"data\":"+this.dataToString()+",\"update\":"+this.updateToString()+"}";
	}
	public String dataToString(){
		String result = "[";
		if(this.data.size() > 0){
			for(int i = 0; i < this.data.size(); i++){
				result += "\""+this.data.get(i).toString()+"\"";
				if(i+1 < this.data.size()){
					result += ",";
				}
			}
		}
		result += "]";
		return result;
	}
	public String updateToString(){
		String result = "[";
		if(this.update.size() > 0){
			for(int i = 0; i < this.update.size(); i++){
				result += "\""+this.update.get(i).toString()+"\"";
				if(i+1 < this.update.size()){
					result += ",";
				}
			}
		}
		result += "]";
		return result;
	}
}
