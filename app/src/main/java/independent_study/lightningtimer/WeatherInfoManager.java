package independent_study.lightningtimer;

import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by Blaine Huey on 9/8/2017.
 * Gathers and Updates Current Weather Information
 */

class WeatherInfoManager
{
    //http://openweathermap.org/current
    private String OPENWEATHERAPI_COORDINATE_FORMAT;
    private String OPENWEATHERAPI_ZIPCODE_FORMAT;
    private boolean internetTemperature;
    private boolean locationServices;
    private double defaultTemp;
    private String defaultZip;

    private final Thread internetUpdater;
    private final LocationManager locationManager;
    private Location location;
    private String apiCall;

    WeatherInfoManager(SharedPreferences sharedPreferences, LocationManager locationManagerEx, String apiKey)
    {
        internetTemperature = sharedPreferences.getBoolean("internet_switch", false);
        locationServices = sharedPreferences.getBoolean("location_switch", false);
        defaultTemp = Utilities.convertTemperature(Double.parseDouble(sharedPreferences.getString("default_temperature_text", "75")), Utilities.TEMPERATURE_CONVERT.FAHRENHEIT_TO_KELVIN);
        defaultZip = sharedPreferences.getString("default_location_text", "19355");

        OPENWEATHERAPI_COORDINATE_FORMAT = "https://api.openweathermap.org/data/2.5/weather?lat=%5f&lon=%5f&appid=" + apiKey;
        OPENWEATHERAPI_ZIPCODE_FORMAT = "https://api.openweathermap.org/data/2.5/weather?zip=%5s,us&appid=" + apiKey;

        locationManager = locationManagerEx;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        internetUpdater = new Thread()
        {
            @Override
            public void run()
            {
                Log.d("Weather Info Manager", "Background Thread Loop Start: " + internetTemperature);
                while(internetTemperature)
                {
                    Log.d("Weather Info Manager", "Background Thread Loop Reset");

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
                            Log.d("Weather Info Manager", "Changed Location to be " + location.getLatitude() + " " + location.getLongitude());
                            defaultTemp = getCurrentTemperature(location.getLatitude(), location.getLongitude());
                            Log.d("Weather Info Manager", "Changed (COR) Temperature to be " + defaultTemp + " K");
                        }
                        else
                        {
                            defaultTemp = getCurrentTemperature(defaultZip);
                            Log.d("Weather Info Manager", "Changed (ZIP) Temperature to be " + defaultTemp + " K");
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
        };

        internetUpdater.start();
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

        Log.d("Weather Info Manager", "Found Temperature to be " + tempKelvin + "K");
        return tempKelvin;
    }

    public double getCurrentTemperature(String zipCode)
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

        Log.d("Weather Info Manager", "Found Temperature to be " + tempKelvin + "K");
        return tempKelvin;
    }
}
