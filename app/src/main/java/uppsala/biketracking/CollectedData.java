package uppsala.biketracking;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CollectedData
{
	
	private List<Data> data;
	private List<PlaceRange> ranges;
	private List<String> roads;
	private List<Place> places;
	public CollectedData(){
		this.data = new LinkedList<Data>();
		this.ranges = new LinkedList<PlaceRange>();
		this.roads = new LinkedList<String>();
		this.places = new LinkedList<Place>();
	}

	public boolean isEmpty(){
		return data.size() == 0;
	}
	public int size(){
		return data.size();
	}
	public int addRoad(String road){
		int out = roads.indexOf(road);
		switch(out){
			case -1 :
				out = roads.size();
				roads.add(road);
				break;
			default :
				break;
		}
		return out;
	}
	public void addRange(int id, double startLat, double startLon, double stopLat, double stopLon, long startTime, long stopTime, float speed, float accuracy, int pid){
		this.ranges.add(new PlaceRange(id, startLat, startLon, stopLat, stopLon, startTime, stopTime, speed, accuracy, pid));
	}
	public void add(int id, double lat, double lon, long time, float speed, float accuracy){
		this.data.add(new Data(id, lat, lon, time, speed, accuracy));
	}
	public void addPlace(int pid, double latitude, double longitude){
		Place place = new Place(pid, latitude, longitude);
		if(!places.contains(place)){
			this.places.add(new Place(pid, latitude, longitude));
		}
	}
	public int addRoadPlace(String pid, double latitude, double longitude){
		int out = addRoad(pid);
		addPlace(out, latitude, longitude);
		return out;
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
		ranges.clear();
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
	
	/*private void removeLastData(int last){
		if(last > 0 && !this.isEmpty() && data.size() >= last){
			boolean finalization = true;
			int size = data.size();
			for(int i = size-1; i >= (size-last); i--){
				if(finalization){
					finalization = this.data.get(i).finalization();
					//if(!finalization){
					//	Log.i("RemoveLastUpdate", "UNABLE TO FINALIZE DATA OBJECT, STOPPING FINALIZING, ONLY REMOVE FROM LISTS");
					//}
				}
				this.data.remove(i);
			}
			//Log.i("RemoveLastUpdate","SUCCESSFULLY REMOVED "+last+" LAST OBJECTS FROM LISTS; finalization="+finalization);
		}
		//else{
		//	Log.i("RemoveLastUpdate","FAIL: last="+last+", data.size="+data.size());
		//}
	}*/
	
	public boolean importFile(String path){
		boolean imprt = true;
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
							this.add(Integer.parseInt(splitSID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]), Long.parseLong(splitTime[1]), Float.parseFloat(splitSpeed[1]), Float.parseFloat(splitAccuracy[1]));
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
			this.resetData();
		}
		return imprt;
	}

	public boolean importRangeFile(String path){
		boolean imprt = true;
		File updateLog = new File(C.getSaveDirectory(), path);
		if(updateLog.exists()){
			try{
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, sssid, sspid, sstime, setime, sspeed, sacc, sslat, sslon, selat, selon;
				while((line = buf.readLine()) != null){
					if(line.matches(C.MATCH_SYMBOLS)){
						splitLine = line.split("\\|");
						sssid = splitLine[0].split(C.SPACE);
						sslat = splitLine[1].split(C.SPACE);
						sslon = splitLine[2].split(C.SPACE);
						selat = splitLine[3].split(C.SPACE);
						selon = splitLine[4].split(C.SPACE);
						sstime = splitLine[5].split(C.SPACE);
						setime = splitLine[6].split(C.SPACE);
						sspeed = splitLine[7].split(C.SPACE);
						sacc = splitLine[8].split(C.SPACE);
						sspid = splitLine[9].split(C.SPACE);
						if(sssid[0].equals(C.SID_TXT)
						&& sslat[0].equals(C.START_LAT_TXT)
						&& sslon[0].equals(C.START_LON_TXT)
						&& selat[0].equals(C.STOP_LAT_TXT)
						&& selon[0].equals(C.STOP_LON_TXT)
						&& sstime[0].equals(C.START_TIME_TXT)
						&& setime[0].equals(C.STOP_TIME_TXT)
						&& sspeed[0].equals(C.SPEED_TXT)
						&& sacc[0].equals(C.ACCURACY_TXT)
						&& sspid[0].equals(C.PID_TXT)){
							this.addRange(Integer.parseInt(sssid[1]), Double.parseDouble(sslat[1]), Double.parseDouble(sslon[1]), Double.parseDouble(selat[1]), Double.parseDouble(selon[1]), Long.parseLong(sstime[1]), Long.parseLong(setime[1]), Float.parseFloat(sspeed[1]), Float.parseFloat(sacc[1]), Integer.parseInt(sspid[1]));
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
			this.resetData();
		}
		return imprt;
	}

	public void importPlaceFile(String path){
		boolean imprt = true;
		File updateLog = new File(C.getSaveDirectory(), path);
		if(updateLog.exists()){
			try{
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitPID, splitLatitude, splitLongitude;
				while((line = buf.readLine()) != null){
					if(line.matches(C.MATCH_SYMBOLS)){
						splitLine = line.split("\\|");
						splitPID = splitLine[0].split(C.SPACE);
						splitLatitude = splitLine[1].split(C.SPACE);
						splitLongitude = splitLine[2].split(C.SPACE);
						if(splitPID[0].equals(C.PID_TXT)
						&& splitLatitude[0].equals(C.LAT_TXT)
						&& splitLongitude[0].equals(C.LON_TXT)){
							this.addPlace(Integer.parseInt(splitPID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]));
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
			this.resetData();
		}
	}

	public void importRoadFile(String path){
		File updateLog = new File(C.getSaveDirectory(), path);
		if(updateLog.exists()){
			try{
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				while((line = buf.readLine()) != null){
					roads.add(line);
				}
				buf.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	public String toString(){
		return allToJSONObject();
	}
	/*public String dataToString(){
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
	}¨*/

	/*public String dataToFileString(){
		String result = C.EMPTY;

		if(data.size() > 0){
			for(int i = 0; i < data.size(); i++){
				result += data.get(i).toFileString();
			}
		}
		return result;
	}*/
	public String roadsToFileString(){
		String result = C.EMPTY;

		if(roads.size() > 0){
			for(Iterator i = roads.listIterator(); i.hasNext();){
				result += (i.next() + C.NEW_LINE);
			}
		}
		return result;
	}
	public String placesToFileString(){
		String result = C.EMPTY;

		if(places.size() > 0){
			for(Iterator i = places.listIterator(); i.hasNext();){
				result +=  ((Place)i.next()).toFileString();
			}
		}
		return result;
	}
	public String rangesToFileString(){
		String result = C.EMPTY;

		if(ranges.size() > 0){
			for(Iterator i = ranges.listIterator(); i.hasNext();){
				result +=  ((PlaceRange)i.next()).toFileString();
			}
		}
		return result;
	}
	public String roadsToJSONList(){
		String result = C.JSON_LIST_START;

		if(roads.size() > 0){
			int counter = 0;
			for(Iterator i = roads.listIterator(); i.hasNext();){
				result += C.JSON_OBJ_START
						+ C.ID_TXT + C.JSON_STR_EQUALS + counter + C.JSON_STR_COMMA
						+ C.PID_TXT + C.JSON_STR_EQUALS + i.next()
						+ C.JSON_STR_OBJ_END;

				if(i.hasNext()){
					result += C.JSON_LIST_COMMA;
				}
			}
		}
		result += C.JSON_LIST_END;
		return result;
	}
	public String placesToJSONList(){
		String result = C.JSON_LIST_START;

		if(places.size() > 0){
			for(Iterator i = places.listIterator(); i.hasNext();){
				result +=  i.next().toString();
				if(i.hasNext()){
					result += C.JSON_LIST_COMMA;
				}
			}
		}
		result += C.JSON_LIST_END;
		return result;
	}
	public String rangesToJSONList(){
		String result = C.JSON_LIST_START;

		if(ranges.size() > 0){
			for(Iterator i = ranges.listIterator(); i.hasNext();){
				result +=  i.next().toString();
				if(i.hasNext()){
					result += C.JSON_LIST_COMMA;
				}
			}
		}
		result += C.JSON_LIST_END;
		return result;
	}
	public String allToJSONObject(){
		return C.JSON_OBJ_START
				+ C.ROADS_TXT + C.JSON_VAR_EQUALS + roadsToJSONList() + C.JSON_VAR_COMMA
				+ C.PLACES_TXT + C.JSON_VAR_EQUALS + placesToJSONList() + C.JSON_VAR_COMMA
				+ C.RANGES_TXT + C.JSON_VAR_EQUALS + rangesToJSONList()
				+ C.JSON_VAR_OBJ_END;
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
