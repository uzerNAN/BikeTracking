package uppsala.biketracking;

import android.app.*;
import android.os.*;

public class NotMainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
