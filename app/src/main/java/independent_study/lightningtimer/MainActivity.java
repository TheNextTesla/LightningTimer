package independent_study.lightningtimer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Blaine Huey
 */

public class MainActivity extends AppCompatActivity
{
    private int REQUEST_COARSE_LOCATION = 18129;

    private TextView timeTextView;
    private Button startStopButton;
    private Handler handler;
    private Runnable runnable;

    private WeatherInfoManager weatherInfoManager;
    private SharedPreferences sharedPreferences;
    private LocationManager locationManager;

    private boolean buttonState;
    private long timerCount;
    private long startCount;
    private long lastCount;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Lightning Timer", "Main Activity onCreate()");

        timeTextView = (TextView) findViewById(R.id.TimerTextView);
        startStopButton = (Button) findViewById(R.id.StartStopButton);
        handler = new Handler();

        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                lastCount = System.currentTimeMillis();
                timerCount = lastCount - startCount;
                timeTextView.setText(Long.toString(timerCount));
                handler.postDelayed(this, 0);
            }
        };

        buttonState = false;
        timerCount = 0L;
        startCount = System.currentTimeMillis();
        lastCount = startCount;

        //https://stackoverflow.com/questions/8977212/button-click-listeners-in-android
        startStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!buttonState)
                {
                    buttonState = true;
                    startStopButton.setText("Stop Timer");
                    activateTimer();
                }
                else
                {
                    buttonState = false;
                    startStopButton.setText("Start Timer");
                    deactivateTimer();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            }
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //https://developer.android.com/guide/topics/resources/string-resource.html
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        weatherInfoManager = new WeatherInfoManager(sharedPreferences, locationManager, getString(R.string.api_key_string));
    }

    /**
     *
     * @see "https://developer.android.com/training/appbar/setting-up.html"
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     * @see "https://stackoverflow.com/questions/15126290/adding-settings-to-android-app"
     * @see "https://google-developer-training.gitbooks.io/android-developer-fundamentals-course-practicals/content/en/Unit%204/92_p_adding_settings_to_an_app.html"
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     */
    private void activateTimer()
    {
        Log.d("Lightning Timer", "Button Activated");

        timerCount = 0;
        startCount = System.currentTimeMillis();

        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);
    }

    /**
     *
     */
    private void deactivateTimer()
    {
        Log.d("Lightning Timer", "Button De-Activated");
        handler.removeCallbacks(runnable);
        simpleLightningFunction();
    }

    /**
     *
     * @see "https://math.stackexchange.com/questions/1169881/if-i-hear-thunder-5-seconds-after-i-see-the-lighting-can-i-calculate-the-distan"
     * @see "http://www.csgnetwork.com/lightningdistcalc.html"
     */
    private void simpleLightningFunction()
    {
        double temperature = weatherInfoManager.getCurrentTemperature(-75, 40);
        //double speedOfSound = Utilities.speedOfSoundAtTemperature(Utilities.convertTemperature(72, Utilities.TEMPERATURE_CONVERT.FAHRENHEIT_TO_KELVIN));
        double speedOfSound = Utilities.speedOfSoundAtTemperature(temperature);
        double distanceFromLightning = speedOfSound * (timerCount / 1000.0);
        double milesFromLightning = Utilities.convertDistance(distanceFromLightning, Utilities.DISTANCE_CONVERT.METERS_TO_MILES);

        Toast.makeText(getApplicationContext(), "Miles From Lightning: " + String.format("%3f", milesFromLightning), Toast.LENGTH_LONG).show();
    }
}
