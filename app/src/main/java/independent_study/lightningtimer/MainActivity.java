package independent_study.lightningtimer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Blaine Huey
 * The Main UI 'Activity' of the Lightning Timer Application
 */

public class MainActivity extends AppCompatActivity
{
    private int REQUEST_COARSE_LOCATION = 18129;

    private TextView timeTextView;
    private TextView infoTextView;
    private Button startStopButton;
    private ListView historyListView;
    private Handler handler;
    private Runnable runnable;

    private WeatherInfoManager weatherInfoManager;
    private SharedPreferences sharedPreferences;
    private LocationManager locationManager;

    private ArrayList<String> historyArrayList;
    private WeatherInfoManager.PROCESS_STATUS lastProcessStatus;
    private boolean buttonState;
    private long timerCount;
    private long startCount;
    private long lastCount;
    private long originalCount;

    /**
     * Creates the Main Activity - Main Method
     * Required by Android for UI to Function
     * @param savedInstanceState - Variable Passed to Super
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Lightning Timer", "Main Activity onCreate()");

        //Accesses the Android UI Components and Creates Objects to Control them
        timeTextView = (TextView) findViewById(R.id.TimerTextView);
        infoTextView = (TextView) findViewById(R.id.InfoTextView);
        startStopButton = (Button) findViewById(R.id.StartStopButton);
        historyListView = (ListView) findViewById(R.id.HistoryListView);

        //Android "Thread-Safe" (Doesn't Mess with UI) Looping Function
        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                lastCount = System.currentTimeMillis();
                timerCount = lastCount - startCount;

                if(timerCount > 300_000)
                {
                    timeTextView.setText("WAY TOO FAR AWAY");

                    //Standard Stop Button Procedure
                    buttonState = false;
                    startStopButton.setText(getString(R.string.start));
                    deactivateTimer();
                }
                else
                {
                    timeTextView.setText(String.format(Locale.US, "%6.3f s", timerCount / 1000.0));
                    handler.postDelayed(this, 0);
                }

                if((lastProcessStatus != weatherInfoManager.getProcessStatus()) || ((lastCount - originalCount) % 10000 < 10))
                {
                    infoTextView.setText(String.format(Locale.US, "Net:%b\nLoc:%b\nTemp:%3.1f", weatherInfoManager.getProcessStatus() != WeatherInfoManager.PROCESS_STATUS.OFFLINE_DEFAULT, weatherInfoManager.getProcessStatus() == WeatherInfoManager.PROCESS_STATUS.ONLINE_LOCATION,  weatherInfoManager.getBestTemperatureEstimate()));
                }
            }
        };

        //Setup State Variables of the Activity
        buttonState = false;
        timerCount = 0L;
        startCount = System.currentTimeMillis();
        lastCount = startCount;
        originalCount = startCount;

        historyArrayList = new ArrayList<>();
        historyListView.setAdapter(new ArrayAdapter<>(this, R.layout.list_text_view, historyArrayList));

        //Sets the UI Response for the startStopButton
        //https://stackoverflow.com/questions/8977212/button-click-listeners-in-android
        startStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!buttonState)
                {
                    buttonState = true;
                    startStopButton.setText(getString(R.string.stop));
                    activateTimer();
                }
                else
                {
                    buttonState = false;
                    startStopButton.setText(getString(R.string.start));
                    deactivateTimer();
                }
            }
        });

        /*
         * Checks for Location Permissions from User
         * The Code Can Run With or Without Approval
         * @see "https://developer.android.com/training/permissions/requesting.html"
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            }
        }

        /*
         * Gets the LocationManager from the Android System
         * @see "https://developer.android.com/training/location/index.html"
         * @see "https://developer.android.com/reference/android/location/LocationManager.html"
         */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /*
         * Instantiates the Android Settings Monitor Object, and Passes it to the WeatherInfoManager on Creation
         * @see "https://developer.android.com/guide/topics/resources/string-resource.html"
         * @see "https://developer.android.com/guide/topics/location/index.html"
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherInfoManager = new WeatherInfoManager(sharedPreferences, locationManager, getString(R.string.api_key_string));
    }

    /**
     * Tells the Top Bar (the 'AppBar') to Show Settings when it is Opened
     * Required by Android for UI to Function
     * @see "https://developer.android.com/training/appbar/setting-up.html"
     * @param menu The App Bar Menu
     * @return Success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Allows Navigation Within Settings Menu
     * Required by Android for UI to Function
     * @see "https://stackoverflow.com/questions/15126290/adding-settings-to-android-app"
     * @see "https://google-developer-training.gitbooks.io/android-developer-fundamentals-course-practicals/content/en/Unit%204/92_p_adding_settings_to_an_app.html"
     * @param item - The Item Selected
     * @return Success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //TODO: Fix Exit Procedure
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called When the Timer is Started
     */
    private void activateTimer()
    {
        Log.d("Lightning Timer", "Button Activated");

        //Clears and Resets the counts
        timerCount = 0;
        startCount = System.currentTimeMillis();

        //Starts the Handler-Runnable "Thread-Safe" Loop
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    /**
     * Called When the Timer is Stopped
     */
    private void deactivateTimer()
    {
        Log.d("Lightning Timer", "Button De-Activated");

        //Stops the Handler-Runnable "Thread-Safe" Loop
        handler.removeCallbacks(runnable);

        //Calls Lightning Calculation Math
        lightningFunction();
    }

    /**
     * Does the Math for the Calculation of the Distance Away from Lightning
     * Apparently, the 1 sec = 1 mile rule is VERY WRONG (speed of sound at sea level =~= 0.211446403 miles per second)
     * @see "https://math.stackexchange.com/questions/1169881/if-i-hear-thunder-5-seconds-after-i-see-the-lighting-can-i-calculate-the-distan"
     * @see "http://www.csgnetwork.com/lightningdistcalc.html"
     */
    private void lightningFunction()
    {
        //Lightning Math
        double speedOfSound = Utilities.speedOfSoundAtTemperature(weatherInfoManager.getBestTemperatureEstimate());
        double distanceFromLightning = speedOfSound * (timerCount / 1000.0);
        double milesFromLightning = Utilities.convertDistance(distanceFromLightning, Utilities.DISTANCE_CONVERT.METERS_TO_MILES);

        //Displays Miles from Lighting
        Toast.makeText(getApplicationContext(), "Feet From Lightning: " + String.format(Locale.US, "%6.0f", Utilities.convertDistance(milesFromLightning, Utilities.DISTANCE_CONVERT.MILES_TO_FEET)), Toast.LENGTH_SHORT).show();

        //https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
        //https://stackoverflow.com/questions/3606530/listview-scroll-to-the-end-of-the-list-after-updating-the-list
        historyArrayList.add(String.format(Locale.US, "Miles From Strike: %6.3f, %3.1f Secs", milesFromLightning, timerCount / 1000.0));
        historyListView.setAdapter(new ArrayAdapter<>(this, R.layout.list_text_view, historyArrayList));
        historyListView.setSelection(historyArrayList.size() - 1);
    }
}
