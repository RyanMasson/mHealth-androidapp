package thehabitslab.com.codebase;

import android.hardware.SensorEvent;
import android.util.Log;
import java.util.LinkedList;

/**
 * Accumulates data and keeps it for the specified time interval.
 * <p/>
 * In this file, students should implement the mean squared error algorithm as a way to calculate
 * energy.
 * <p/>
 * Created by William on 12/7/2016.
 */
public class DataAccumulator {
    private static final String TAG = "DataAccumulator";
    private LinkedList<CustomSensorEvent> events = new LinkedList<>();
    /**
     * Interval for which the energy is calculated (in milliseconds)
     * 60000 corresponds to a minute
     */
    private final long TIME_INTERVAL = 5000;

    /**
     * Adds the given event to the list
     * If TIME_INTERVAL  has passed since first element, calculates the average sum of squared
     * differences of accelerometer values and returns it as an energy indicator.
     * Algorithm for calculating sum of squared differences was taken from
     * <a href="http://stackoverflow.com/questions/29027878/sum-of-squared-differences">this</a>
     * discussion.
     *
     * @param sensorEvent The accelerometer event to be added
     * @return null if a minute has not passed,
     * EnergyReading with the sum squared variance if a minute has passed
     */
    public EnergyReading addEvent(SensorEvent sensorEvent) {
        CustomSensorEvent e = new CustomSensorEvent(sensorEvent);
        events.addLast(e);

        if ((e.timestamp - events.getFirst().timestamp) < TIME_INTERVAL) { // Interval has not passed
            Log.v(TAG, "Time interval has not passed");
            return null;
        } else {
            Log.v(TAG, "Time interval has passed");
            float accumulatorX = 0;
            float accumulatorY = 0;
            float accumulatorZ = 0;

            CustomSensorEvent prev = null;

            for (CustomSensorEvent event :
                    events) {
                if (prev != null) {
                    accumulatorX += Math.pow(event.values[0] - prev.values[0], 2);
                    accumulatorY += Math.pow(event.values[1] - prev.values[1], 2);
                    accumulatorZ += Math.pow(event.values[2] - prev.values[2], 2);
                }
                prev = event;
            }

            events = new LinkedList<>();

            // return the arithmetic mean of the sum of squared differences
            return new EnergyReading((accumulatorX + accumulatorY + accumulatorZ)/3,
                    sensorEvent.timestamp);
        }
    }


    /**
     * Class with relevant fields of a SensorEvent
     */
    private class CustomSensorEvent {
        /**
         * Timestamp in milliseconds of instantiation
         */
        public long timestamp;
        /**
         * Data payload of the SensorEvent
         */
        float[] values;

        /**
         * Default constructor. Should never be called.
         */
        private CustomSensorEvent() {
            throw new IllegalArgumentException("This class should only be instantiated " +
                    "with a SensorEvent");
        }

        /**
         * Instantiates a CustomSensorEvent by copying the fields from e
         *
         * @param e SensorEvent (Accelerometer) to be copied
         */
        public CustomSensorEvent(SensorEvent e) {
            timestamp = System.currentTimeMillis();
            values = new float[]{e.values[0], e.values[1], e.values[2]};
        }
    }
}
