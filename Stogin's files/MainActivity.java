package thehabitslab.com.codebase;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

/**
 * This is the class for the main activity that the user interacts with.
 * <p/>
 * In this class, most of the workings are left in place since the methods called will need to be
 * implemented anyway. Students should figure out how to register the accelerometer to the
 * listener and do so, maintaining the fields in this class as documented.
 * <p/>
 * Created by William on 12/7/2016
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";

    // Sensor related fields
    private Sensor mAccel;
    private SensorManager mManager;
    private TextView statusText;
    /**
     * Maintains accelerometer registration state.
     * Should be updated every time you register/unregister outside of
     * activity lifecycle.
     */
    private boolean accelIsRegistered = false;
    private DataAccumulator dataManager = new DataAccumulator();

    /* ***************************** ACTIVITY CONTROL METHODS ********************************** */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccel = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        statusText = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the accelerometer was streaming before paused
        if (accelIsRegistered) {
            mManager.registerListener(accelListener, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
            statusText.setText(R.string.text_active);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Check if accelerometer should be paused
        if (accelIsRegistered) {
            mManager.unregisterListener(accelListener);
            statusText.setText(R.string.text_inactive);
        }
    }


    /* ****************************** USER INTERACTION HANDLING ******************************** */

    /**
     * Called when the accelerometer button is clicked
     */
    public void toggleAccelClicked(View v) {
        if (!accelIsRegistered) {
            mManager.registerListener(accelListener, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
            accelIsRegistered = true;
            statusText.setText(R.string.text_active);
        } else {
            mManager.unregisterListener(accelListener);
            accelIsRegistered = false;
            statusText.setText(R.string.text_inactive);
        }
    }

    /**
     * Called when show document button is clicked
     */
    public void showDocumentClicked(View v) {
        // Get the string representing the current table from SQLite
        String content = SQLiteInterface.getCurrentTableString(this);
        ((TextView) findViewById(R.id.docText)).setText(content);
    }

    /**
     * Called when replication button is clicked
     */
    public void onReplicateClicked(View v) {
        SQLiteInterface.sendDataToBackend(this.getBaseContext());
    }


    /* ********************************** WORKING PARTS **************************************** */
    /**
     * Custom implementation of SensorEventListener specific to what we want to do with
     * accelerometer data.
     */
    private SensorEventListener accelListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
//            Log.v(TAG, "Event received! X: " + Float.toString(event.values[0]));
            EnergyReading energy;
            if ((energy = dataManager.addEvent(event)) != null)
                handleEnergyValue(energy);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle change of accuracy
            Log.w(TAG, "Accuracy of accelerometer changed.");
        }


        /**
         * Performs operations on the energy value once it is obtained
         * @param energy returned from an accumulator
         */
        private void handleEnergyValue(EnergyReading energy) {
            try {
                SQLiteInterface.addEnergyReading(energy, MainActivity.this);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    };
}
