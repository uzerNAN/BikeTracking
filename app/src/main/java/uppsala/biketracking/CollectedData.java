package uppsala.biketracking;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CollectedData
{
	
	private List<Data> data;
	public CollectedData(){
		this.data = new LinkedList<>();
	}

	public boolean isEmpty(){
		return data.size() == 0;
	}
	public int size(){
		return data.size();
	}
	public void add(int id, double latitude, double longitude, long time, float speed, float accuracy){
		this.data.add(new Data(id, latitude, longitude, time, speed, accuracy));
	}
	public void add(int id, double latitude, double longitude, long time, float speed, float accuracy, String placeId){
		this.data.add(new Data(id, latitude, longitude, time, speed, accuracy, placeId));
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
		if(0 <= id && id < data.size()){
			iter = this.data.get(id);
		}
		return iter;
	}
	//public boolean isOnline(){
	//	return RecordLocationService.isOnline(this.context);
	//}
	public void resetData(){
		data.clear();
	}
	/*public boolean uploadData(Context c){
		boolean success = false;
		if(!this.uploading()){
			//if(this.dataIsEmpty()) { this.importFile(WakefulService.filePath); }
			if(!this.isEmpty()){
				this.send.setInput(this.dataToString(), c);
				new Thread(this.send).start();
				Log.i(C.UPLOAD_TXT, "THREAD STARTED");
				//this.uploading = true;
				success = true;
			}
		}
		else{
			Log.i(C.UPLOAD_TXT,"UNABLE TO START THREAD: data.size="+this.size+", uploading="+this.uploading());
		}
		return success;
	}*/
	
	private void removeLastData(int last){
		if(last > 0 && !this.isEmpty() && data.size() >= last){
			boolean finalization = true;
			int size = data.size();
			for(int i = size-1; i >= (size-last); i--){
				if(finalization){
					finalization = this.data.get(i).finalization();
					if(!finalization){
						Log.i("RemoveLastUpdate", "UNABLE TO FINALIZE DATA OBJECT, STOPPING FINALIZING, ONLY REMOVE FROM LISTS");
					}
				}
				this.data.remove(i);
			}
			Log.i("RemoveLastUpdate","SUCCESSFULLY REMOVED "+last+" LAST OBJECTS FROM LISTS; finalization="+finalization);
		}
		else{
			Log.i("RemoveLastUpdate","FAIL: last="+last+", data.size="+data.size());
		}
	}
	
	public boolean importFile(String path, boolean corrected){
		boolean imprt = true; int counter = 0;
		File updateLog = new File(C.getSaveDirectory(), path);
		if(updateLog.exists()){
			try{
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitSID, splitTime, splitSpeed, splitAccuracy, splitLatitude, splitLongitude;
				while((line = buf.readLine()) != null){
					if(line.matches(C.MATCH_SYMBOLS)){
						splitLine = line.split("\\|");
						splitSID = splitLine[0].split(C.SPACE);
						splitLatitude = splitLine[1].split(C.SPACE);
						splitLongitude = splitLine[2].split(C.SPACE);
						splitTime = splitLine[3].split(C.SPACE);
						splitSpeed = splitLine[4].split(C.SPACE);
						splitAccuracy = splitLine[5].split(C.SPACE);
						if(splitSID[0].equals(C.SID_TXT)
						&& splitLatitude[0].equals(C.LAT_TXT)
						&& splitLongitude[0].equals(C.LON_TXT)
						&& splitTime[0].equals(C.TIME_TXT)
						&& splitSpeed[0].equals(C.SPEED_TXT)
						&& splitAccuracy[0].equals(C.ACCURACY_TXT)){
							if(corrected) {
								this.add(Integer.parseInt(splitSID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]), Long.parseLong(splitTime[1]), Float.parseFloat(splitSpeed[1]), Float.parseFloat(splitAccuracy[1]), (splitLine.length>=7)?splitLine[6]:C.NO_PID);
							}
							else{
								this.add(Integer.parseInt(splitSID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]), Long.parseLong(splitTime[1]), Float.parseFloat(splitSpeed[1]), Float.parseFloat(splitAccuracy[1]));
							}
							counter++;
						}
						else{
							imprt = false;
							break;
						}
					}
					else{
						imprt = false;
						break;
					}
				}
				buf.close();
			}
			catch(IOException e){
				imprt = false;
				e.printStackTrace();
			}
		}
		if(!imprt){
			this.removeLastData(counter);
		}
		return imprt;
	}
	public String toString(){
		return C.JSON_OBJ_START
				+ C.data_TXT + C.JSON_VAR_EQUALS + this.dataToString()
			 + C.JSON_VAR_OBJ_END;
	}
	public String dataToString(){
		String result = C.JSON_LIST_START;
		if(data.size() > 0){
			for(int i = 0; i < data.size(); i++){
				result += this.data.get(i).toString();
				if(i+1 < data.size()){
					result += C.JSON_LIST_COMMA;
				}
			}
		}
		result += C.JSON_LIST_END;
		return result;
	}

	public String dataToFileString(boolean corrected){
		String result = C.EMPTY, pid = C.EMPTY;

		if(data.size() > 0){
			for(int i = 0; i < data.size(); i++){
				if(corrected){
					pid = C.SPLIT + data.get(i).getPID();
				}
				result += data.get(i).toFileString(pid + C.NEW_LINE);
			}
		}
		return result;
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
