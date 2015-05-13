package uppsala.biketracking;

import android.content.*;
import android.util.*;
import android.widget.*;
import java.util.*;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements
Filterable {
	private final String TAG = "PlacesAutoCompleteAdapter";
    private ArrayList<String> resultList;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }
	
	// Don't forget to uncomment autocomplete() in Tracking class and AutoCompleteTexView in both Tracking class and in titlebar.xml to see how it does work.
	// Make sure you know the limits of the Google Places API requests, it is 1000/day for an "unverified" google account and much more for verified.
	/*@Override
    public Filter getFilter() {
		
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length()%3 == 1) {
					Log.i(TAG, "constraint not null");
                    // Retrieve the autocomplete results.
                    resultList = Tracking.autocomplete(constraint
															.toString());
					Log.i(TAG, "resultList.size="+resultList.size());
                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint,
										  FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }*/
}
