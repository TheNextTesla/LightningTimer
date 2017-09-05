package independent_study.lightningtimer;

/**
 * Created by Blaine Huey on 9/5/2017
 * Static Utilities Static Methods Class
 */

public final class Utilities
{
    public enum TEMPERATURE_CONVERT {FAHRENHEIT_TO_CELSIUS, CELSIUS_TO_FAHRENHEIT, CELSIUS_TO_KELVIN, KELVIN_TO_CELSIUS}

    private Utilities() {   }

    /**
     * Converting Several Type of Temperature to One Another
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

            default:
                return Double.NaN;
        }
    }
}
