package uppsala.biketracking;

import android.content.*;
import android.location.*;
import android.util.*;
import java.io.*;
import java.util.*;

public class CollectedData
{
	
	private List<Data> data;
	private int size = 0;
	private Context context;
	private SendData send;
	public CollectedData(Context c){
		this.context = c;
		this.send = new SendData();
		this.data = new ArrayList<Data>();
	}
	
	public boolean uploading(){
		boolean uploading = false;
		if(this.send != null){
			uploading = this.send.uploading();
		}
		return uploading;
	}
	public boolean dataIsEmpty(){
		return this.size == 0;
	}
	public void add(int id, double latitude, double longitude, long time, float speed, float accuracy){
		this.data.add(this.size, new Data(id, latitude, longitude, time, speed, accuracy));
		this.size++;
	}
	/*public List<Data> getBySID(int sid){
		List<Data> iter = new ArrayList<Data>();
		for(int i = 0; i < this.size; i++){
			if(this.data.get(i).getSID() == sid){
				iter.add(this.data.get(i));
			}
		}
		return iter;
	}*/
	public Data get(int id){
		Data iter = null;
		if(id < this.size){
			iter = this.data.get(id);
		}
		return iter;
	}
	public boolean isOnline(){
		return MainService.isOnline(this.context);
	}
	public void resetData(){
		this.removeLastData(this.size);
		Log.i("ResetUpdate","UPDATE RESETED");
	}
	public int getLastSID(){
		int sid = 0;
		if(!this.dataIsEmpty()){
			sid = this.data.get(this.size-1).getSID();
		}
		return sid;
	}
	public long getLastTime(){
		long time = 0;
		if(!this.dataIsEmpty()){
			time = this.data.get(this.size-1).getTime();
		}
		return time;
	}
	public boolean uploadData(){
		boolean success = false;
		if(!this.uploading() && this.isOnline()){
			//if(this.dataIsEmpty()) { this.importFile(WakefulService.filePath); }
			if(!this.dataIsEmpty()){
				this.send.setInput(this.dataToString());
				new Thread(this.send).start();
				Log.i("UploadData","THREAD STARTED");
				//this.uploading = true;
				success = true;
			}
		}
		else{
			Log.i("UploadData","UNABLE TO START THREAD: isOnline="+this.isOnline()+", data.size="+this.size+", uploading="+this.uploading());
		}
		return success;
	}
	
	private void removeLastData(int last){
		if(last > 0 && !this.dataIsEmpty() && this.size >= last){
			boolean finalization = true;
			for(int i = this.size-1; i >= (this.size-last); i--){
				if(finalization){
					finalization = this.data.get(i).finalization();
					if(!finalization){
						Log.i("RemoveLastUpdate","UNABLE TO FINALIZE DATA OBJECT, STOPPING FINALIZING, ONLY REMOVE FROM LISTS");
					}
				}
				this.data.remove(i);
			}
			this.size -= last;
			Log.i("RemoveLastUpdate","SUCCESSFULLY REMOVED "+last+" LAST OBJECTS FROM LISTS; finalization="+finalization);
		}
		else{
			Log.i("RemoveLastUpdate","FAIL: last="+last+", data.size="+this.size);
		}
	}
	
	public boolean importFile(String path){
		boolean imprt = true; int counter = 0;
		File updateLog = new File(path);
		if(updateLog.exists()){
			try{
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitSID, splitTime, splitSpeed, splitAccuracy, splitLatitude, splitLongitude;
				while((line = buf.readLine()) != null){
					if(line.matches("[A-Z0-9.| ]*")){
						splitLine = line.split("\\|");
						splitSID = splitLine[0].split(" ");
						splitLatitude = splitLine[1].split(" ");
						splitLongitude = splitLine[2].split(" ");
						splitTime = splitLine[3].split(" ");
						splitSpeed = splitLine[4].split(" ");
						splitAccuracy = splitLine[5].split(" ");
						if(splitSID[0].equals("SID")
						&& splitLatitude[0].equals("LATITUDE") 
						&& splitLongitude[0].equals("LONGITUDE") 
						&& splitTime[0].equals("TIME") 
						&& splitSpeed[0].equals("SPEED")
						&& splitAccuracy[0].equals("ACCURACY")){
							this.add(Integer.parseInt(splitSID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]), Long.parseLong(splitTime[1]), Float.parseFloat(splitSpeed[1]), Float.parseFloat(splitAccuracy[1]));
							counter++;
						}
						else{
							Log.i("ImportActivityFile","FILE LINE HAS DIFFERENT CONTENT");
							imprt = false;
							break;
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
			}
			catch(IOException e){
				imprt = false;
				e.printStackTrace();
			}
			catch(Exception e){
				imprt = false;
				e.printStackTrace();
			}
		}
		if(!imprt){
			this.removeLastData(counter);
		}
		//Log.i("TO STRING", this.dataToString());
		Log.i("ImportActivityFile", "imprt="+imprt);
		return imprt;
	}
	public String toString(){
		return "{\"data\":"+this.dataToString()+"}";
	}
	public String dataToString(){
		String result = "[";
		if(this.size > 0){
			for(int i = 0; i < this.size; i++){
				result += this.data.get(i).toString();
				if(i+1 < this.size){
					result += ",";
				}
			}
		}
		result += "]";
		return result;
	}
	public void finalization(){
		this.resetData();
		try{
			this.finalize();
		}
		catch(java.lang.Throwable e){
			e.printStackTrace();
		}
	}
	
	
	/*public void setIdSequence(int newsid, int index){
		int sid = newsid;
		long prvTime = 0;
		if(index < this.size){
			for(int i = index; i < this.size; i++){
				if(Long.compare(prvTime, 0) > 0 && Long.compare((this.data.get(i).getTime()-prvTime), WakefulService.sessionTimeout) > 0){
					sid++;
				}
				prvTime = this.data.get(i).getTime();
				this.data.get(i).setSID(sid);
			}
		}
	}
	
	private void setSpeed(){
		Location l1 = new Location("previous location");
		l1.setLatitude(this.data.get(0).getLatitude());
		l1.setLongitude(this.data.get(0).getLongitude());
		long time = this.data.get(0).getTime();
		Location l2 = new Location("current location");
		for(int i = 1; i < this.size; i++){
			l2.setLatitude(this.data.get(i).getLatitude());
			l2.setLongitude(this.data.get(i).getLongitude());
			this.data.get(i).setSpeed(1000*l2.distanceTo(l1)/(this.data.get(i).getTime()-time));
			l1.set(l2);
			time = this.data.get(i).getTime();
		}
	}
	
	public boolean exportFile(String path){
		boolean imprt = true;
		//this.setIdSequence(0,0);
		//this.setSpeed();
		File updateLog = new File(path);
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
				FileWriter fil = new FileWriter(updateLog);
				//fil.write(this.dataToString());
				fil.write("");
				fil.close();
				BufferedWriter buf = new BufferedWriter(new FileWriter(updateLog, true));
				for(int i = 0; i < this.size; i++){
					buf.append("SID "+this.data.get(i).getSID()
							 +"|LATITUDE "+this.data.get(i).getLatitude()
							 +"|LONGITUDE "+this.data.get(i).getLongitude()
							 +"|TIME "+this.data.get(i).getTime()
							 +"|SPEED "+this.data.get(i).getSpeed()
							 +"|ACCURACY "+this.data.get(i).getAccuracy());
					buf.newLine();
				}
				//buf.append("UPLOADED");
				//buf.newLine();
				buf.close();
			}
			catch(IOException e){
				imprt = false;
				e.printStackTrace();
			}
		}
		return imprt;
	}*/
}
