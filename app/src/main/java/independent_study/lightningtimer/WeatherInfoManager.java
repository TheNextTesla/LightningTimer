package independent_study.lightningtimer;

import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by Blaine Huey on 9/8/2017.
 */

public class WeatherInfoManager
{
    //http://openweathermap.org/current
    private String OPENWEATHERAPI_COORDINATE_FORMAT;
    private String OPENWEATHERAPI_ZIPCODE_FORMAT;
    private boolean internetTemperature;
    private boolean locationServices;
    private double defaultTemp;
    private int defaultZip;

    private final Thread internetUpdater;
    private final LocationManager locationManager;
    private Location location;
    private String apiCall;

    public WeatherInfoManager(SharedPreferences sharedPreferences, LocationManager locationManagerEx, String apiKey)
    {
        internetTemperature = sharedPreferences.getBoolean("internet_switch", false);
        locationServices = sharedPreferences.getBoolean("location_switch", false);
        defaultTemp = Utilities.convertTemperature(sharedPreferences.getInt("default_temperature_text", 75), Utilities.TEMPERATURE_CONVERT.FAHRENHEIT_TO_KELVIN);
        defaultZip = sharedPreferences.getInt("default_location_text", 19355);

        OPENWEATHERAPI_COORDINATE_FORMAT = "https://api.openweathermap.org/data/2.5/weather?lat=%5f&lon=%5f&appid=" + apiKey;
        OPENWEATHERAPI_ZIPCODE_FORMAT = "https://api.openweathermap.org/data/2.5/weather?zip=%5d,us&appid=" + apiKey;

        locationManager = locationManagerEx;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        internetUpdater = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(internetTemperature)
                {
                    if(locationServices && internetTemperature)
                    {
                        try
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        catch(SecurityException se)
                        {
                            se.printStackTrace();
                        }

                        if(location != null)
                        {
                            defaultTemp = getCurrentTemperature(location.getLatitude(), location.getLongitude());
                        }
                        else
                        {
                            defaultTemp = getCurrentTemperature(defaultZip);
                        }
                    }
                    else if(internetTemperature)
                    {
                        defaultTemp = getCurrentTemperature(defaultZip);
                    }

                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException ie)
                    {
                        ie.printStackTrace();
                    }
                }
            }
        });

    }

    public double getCurrentSpeedOfSound()
    {
        return Utilities.speedOfSoundAtTemperature(defaultTemp);
    }

    public double getBestTemperatureEstimate()
    {
        return defaultTemp;
    }

    public double getCurrentTemperature(final double lat, final double lon)
    {
        apiCall = null;
        double tempKelvin = Double.NaN;

        try
        {
            apiCall = Utilities.getFromHTML(String.format(Locale.US, OPENWEATHERAPI_COORDINATE_FORMAT, lat, lon));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return tempKelvin;
        }

        try
        {
            JSONObject jsonObject = new JSONObject(apiCall);
            tempKelvin = jsonObject.getJSONObject("main").getDouble("temp");
        }
        catch(JSONException jse)
        {
            jse.printStackTrace();
            return tempKelvin;
        }

        return tempKelvin;
    }

    public double getCurrentTemperature(int zipCode)
    {
        apiCall = null;
        double tempKelvin = Double.NaN;

        try
        {
            apiCall = Utilities.getFromHTML(String.format(Locale.US, OPENWEATHERAPI_ZIPCODE_FORMAT, zipCode));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return tempKelvin;
        }

        try
        {
            JSONObject jsonObject = new JSONObject(apiCall);
            tempKelvin = jsonObject.getJSONObject("main").getDouble("temp");
        }
        catch(JSONException jse)
        {
            jse.printStackTrace();
            return tempKelvin;
        }

        return tempKelvin;
    }
}
