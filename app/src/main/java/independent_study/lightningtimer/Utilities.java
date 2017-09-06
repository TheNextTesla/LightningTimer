package independent_study.lightningtimer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Blaine Huey on 9/5/2017
 * Static Utilities Static Methods Class
 */

public final class Utilities
{
    public enum TEMPERATURE_CONVERT {FAHRENHEIT_TO_CELSIUS, CELSIUS_TO_FAHRENHEIT, CELSIUS_TO_KELVIN, KELVIN_TO_CELSIUS, FAHRENHEIT_TO_KELVIN, KELVIN_TO_FAHRENHEIT}
    public enum VELOCITY_CONVERT {MPS_TO_MPH, MPH_TO_MPS}
    public enum DISTANCE_CONVERT {METERS_TO_FEET, FEET_TO_METERS, MILES_TO_FEET, FEET_TO_MILES, METERS_TO_MILES, MILES_TO_METERS}

    public static final long SPEED_OF_LIGHT_MPS = 299_792_458;

    private Utilities() {   }

    /**
     * Converting Several Types of Temperature to One Another
     * @see "http://www.rapidtables.com/convert/temperature/how-celsius-to-kelvin.htm"
     * @see "http://www.rapidtables.com/convert/temperature/how-fahrenheit-to-celsius.htm"
     * @param temperature - Temperature Given
     * @param temperatureConvert - Enum for the Conversion Type
     * @return Converted Temperature
     */
    public static double convertTemperature(double temperature, TEMPERATURE_CONVERT temperatureConvert)
    {
        switch(temperatureConvert)
        {
            case FAHRENHEIT_TO_CELSIUS:
                return ((temperature - 32) * (5 / 9));

            case CELSIUS_TO_FAHRENHEIT:
                return ((temperature * (9 / 5)) + 32);

            case CELSIUS_TO_KELVIN:
                return (temperature + 273.15);

            case KELVIN_TO_CELSIUS:
                return (temperature - 273.15);

            case FAHRENHEIT_TO_KELVIN:
                return convertTemperature(convertTemperature(temperature, TEMPERATURE_CONVERT.FAHRENHEIT_TO_CELSIUS), TEMPERATURE_CONVERT.CELSIUS_TO_KELVIN);

            case KELVIN_TO_FAHRENHEIT:
                return convertTemperature(convertTemperature(temperature, TEMPERATURE_CONVERT.KELVIN_TO_CELSIUS), TEMPERATURE_CONVERT.CELSIUS_TO_FAHRENHEIT);

            default:
                return Double.NaN;
        }
    }

    /**
     * Converting Several Types of Velocity to One Another
     * @see "http://sciencing.com/per-second-miles-per-hour-4821647.html"
     * @param speed - Velocity Given
     * @param velocityConvert - Enum for the Conversion Type
     * @return Converted Velocity
     */
    public static double convertVelocity(double speed, VELOCITY_CONVERT velocityConvert)
    {
        switch (velocityConvert)
        {
            case MPS_TO_MPH:
                return speed * 2.2369;

            case MPH_TO_MPS:
                return speed / 2.2369;

            default:
                return Double.NaN;
        }
    }

    /**
     * Converting Several Types of Distance to One Another
     * @see "http://www.metric-conversions.org/length/feet-to-meters.htm"
     * @param distance - Distance Given
     * @param distanceConvert - Enum for the Conversion Type
     * @return Converted Distance
     */
    public static double convertDistance(double distance, DISTANCE_CONVERT distanceConvert)
    {
        switch (distanceConvert)
        {
            case METERS_TO_FEET:
                return distance * 3.2808;

            case FEET_TO_METERS:
                return distance / 3.2808;

            case MILES_TO_FEET:
                return distance * 5280;

            case FEET_TO_MILES:
                return distance / 5280;

            case METERS_TO_MILES:
                return convertDistance(convertDistance(distance, DISTANCE_CONVERT.METERS_TO_FEET), DISTANCE_CONVERT.FEET_TO_MILES);

            case MILES_TO_METERS:
                return convertDistance(convertDistance(distance, DISTANCE_CONVERT.MILES_TO_FEET), DISTANCE_CONVERT.FEET_TO_METERS);

            default:
                return Double.NaN;
        }
    }

    /**
     * Finds the Speed of Sound in M/s at a Temperature Kelvin
     * @see "https://www.weather.gov/media/epz/wxcalc/speedOfSound.pdf"
     * @see "http://www.weather.gov/media/epz/wxcalc/windConversion.pdf"
     * @param temperatureKelvin - Temperature in Kelvin
     * @return - Speed of Sound in M/s
     */
    public static double speedOfSoundAtTemperature(double temperatureKelvin)
    {
        return (643.855 * Math.pow((temperatureKelvin / 273.15), 0.5)) * 0.5144444;
    }

    /**
     * Retrieves API Responses from the Internet
     * @see "Some Really Cool Thing From StackOverflow But I Lost the Link"
     * @param urlToRead - A Web URL Call to an API
     * @return - A String of JSON from an Internet API
     * @throws Exception - Cannot Connect or Find Output
     */
    public String getFromHTML(String urlToRead) throws Exception
    {
        StringBuilder result = new StringBuilder();

        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        InputStream inputStream = conn.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        BufferedReader rd = new BufferedReader(inputStreamReader);

        String line;

        while ((line = rd.readLine()) != null)
        {
            result.append(line);
        }

        return result.toString();
    }
}
