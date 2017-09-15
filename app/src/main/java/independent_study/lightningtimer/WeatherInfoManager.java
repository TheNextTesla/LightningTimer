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
 * @see "http://openweathermap.org/current"
 */

class WeatherInfoManager
{
    private String openweatherapiCoordinateFormat;
    private String openweatherapiZipcodeFormat;
    private boolean internetTemperature;
    private boolean locationServices;
    private double defaultTemperature;
    private String defaultZip;

    private final Thread internetUpdater;
    private final LocationManager locationManager;
    private Location location;
    private String apiCall;

    enum PROCESS_STATUS {ONLINE_LOCATION, ONLINE_ZIP, OFFLINE_DEFAULT}
    private PROCESS_STATUS processStatus;

    WeatherInfoManager(SharedPreferences sharedPreferences, LocationManager locationManagerEx, String apiKey)
    {
        //Gathers Information About User Settings
        internetTemperature = sharedPreferences.getBoolean("internet_switch", false);
        locationServices = sharedPreferences.getBoolean("location_switch", false);
        defaultTemperature = Utilities.convertTemperature(Double.parseDouble(sharedPreferences.getString("default_temperature_text", "75")), Utilities.TEMPERATURE_CONVERT.FAHRENHEIT_TO_KELVIN);
        defaultZip = sharedPreferences.getString("default_location_text", "19355");

        //The Link that Will Pass Back Current Weather Conditions Given Coordinates or zip-code Respectively
        openweatherapiCoordinateFormat = "https://api.openweathermap.org/data/2.5/weather?lat=%5f&lon=%5f&appid=" + apiKey;
        openweatherapiZipcodeFormat = "https://api.openweathermap.org/data/2.5/weather?zip=%5s,us&appid=" + apiKey;

        locationManager = locationManagerEx;
        processStatus = PROCESS_STATUS.OFFLINE_DEFAULT;

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
            {
                internetTemperature = sharedPreferences.getBoolean("internet_switch", false);
                locationServices = sharedPreferences.getBoolean("location_switch", false);
                defaultTemperature = Utilities.convertTemperature(Double.parseDouble(sharedPreferences.getString("default_temperature_text", "75")), Utilities.TEMPERATURE_CONVERT.FAHRENHEIT_TO_KELVIN);
                defaultZip = sharedPreferences.getString("default_location_text", "19355");
            }
        });

        //Criterion for How to Find Location
        //https://developer.android.com/reference/android/location/Criteria.html
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        //The Thread that Gathers Temperature Data if Internet is Online
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
                        //Tries to Get Location
                        try
                        {
                            //https://stackoverflow.com/questions/20438627/getlastknownlocation-returns-null
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        catch(SecurityException se)
                        {
                            se.printStackTrace();
                        }

                        //Checks Whether Location Attempt Worked
                        if(location != null)
                        {
                            Log.d("Weather Info Manager", "Changed Location to be " + location.getLatitude() + " " + location.getLongitude());
                            defaultTemperature = getCurrentTemperature(location.getLatitude(), location.getLongitude());
                            Log.d("Weather Info Manager", "Changed (COR) Temperature to be " + defaultTemperature + " K");
                            processStatus = PROCESS_STATUS.ONLINE_LOCATION;
                        }
                        else
                        {
                            //If Location Attempt Fails, Run the Same Code as If Location Were Off (See Below)
                            defaultTemperature = getCurrentTemperature(defaultZip);
                            Log.d("Weather Info Manager", "Changed (ZIP) Temperature to be " + defaultTemperature + " K");
                            processStatus = PROCESS_STATUS.ONLINE_ZIP;
                        }
                    }
                    else if(internetTemperature)
                    {
                        //Code to Run Using Default Zip Code
                        double tempDefaultTemperature = getCurrentTemperature(defaultZip);

                        if(tempDefaultTemperature != Double.NaN)
                        {
                            defaultTemperature = tempDefaultTemperature;
                            Log.d("Weather Info Manager", "Changed (ZIP) Temperature to be " + defaultTemperature + " K");
                            processStatus = PROCESS_STATUS.ONLINE_ZIP;
                        }
                    }

                    //No Else Necessary..., It will Just Use the Original Default Temperature

                    //Wait a Few Seconds
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

        //Tell the Internet Updater to Start the run
        internetUpdater.start();
    }

    /**
     * Returns An Estimate of the Speed of Sound (M/s) based on defaultTemperature
     * @return An Estimate of the Speed of Sound (M/s) based on defaultTemperature
     */
    double getCurrentSpeedOfSound()
    {
        return Utilities.speedOfSoundAtTemperature(defaultTemperature);
    }

    /**
     * Returns Default Temp
     * @return Default Temp
     */
    double getBestTemperatureEstimate()
    {
        return defaultTemperature;
    }

    /**
     * Returns the Current State of the WeatherInfoManager
     * @return the Current State of the WeatherInfoManager
     */
    PROCESS_STATUS getProcessStatus()
    {
        return processStatus;
    }

    /**
     * Calls the Internet OpenWeatherAPI for Temperature (K) based on Lat and Lon
     * @param lat - Latitude
     * @param lon - Longitude
     * @return Temperature at Location in K
     */
    private double getCurrentTemperature(final double lat, final double lon)
    {
        apiCall = null;
        double tempKelvin = Double.NaN;

        try
        {
            apiCall = Utilities.getFromHTML(String.format(Locale.US, openweatherapiCoordinateFormat, lat, lon));
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

    /**
     * Calls the Internet OpenWeatherAPI for Temperature (K) based on Zip Code
     * @param zipCode - US Zip Code
     * @return Temperature at Location in K
     */
    private double getCurrentTemperature(String zipCode)
    {
        apiCall = null;
        double tempKelvin = Double.NaN;

        try
        {
            apiCall = Utilities.getFromHTML(String.format(Locale.US, openweatherapiZipcodeFormat, zipCode));
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
