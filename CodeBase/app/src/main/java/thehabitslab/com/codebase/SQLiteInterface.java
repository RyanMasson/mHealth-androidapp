package thehabitslab.com.codebase;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

/**
 * Simplifies SQLite operations for interacting with the database.
 * Other classes should not access Replicator or EnergyDBHelper directly. Instead, they
 * should go through this interface first.
 * <p/>
 * This class is given to students as is and should not need modification.
 * <p/>
 * Created by William on 12/28/2016.
 */
public class SQLiteInterface {
    private static final String TAG = "SQLiteInterface";


    /* **************************** PUBLIC DATA INTERACTION METHODS **************************** */

    /**
     * Adds the energy reading to the SQLite database corresponding to this app
     * Context dependent -- must be called from Activity or Service or other Context subclass
     *
     * @param energy  reading to be added
     * @param context for determining the unique id of the device
     * @throws IOException
     */
    public static void addEnergyReading(EnergyReading energy, Context context)
            throws IOException {
        EnergyDBHelper.enterEnergy(energy, context);
    }

    /**
     * Represents the current document as a string
     *
     * @return the string representation of the last hour
     */
    public static String getCurrentTableString(Context context) {
        Cursor c = EnergyDBHelper.getLatest60Entries(context);
        c.moveToFirst();
        StringBuilder builder = new StringBuilder();
        int energyIndex = c.getColumnIndex(EnergyDBHelper.EnergyEntry.COLUMN_NAME_ENERGY);
        Log.v(TAG, "Column names: " + Arrays.toString(c.getColumnNames()));
        int dateIndex = c.getColumnIndex(EnergyDBHelper.EnergyEntry.COLUMN_NAME_TIME);
        while (!c.isAfterLast()) {
            builder.append("Energy: ");
            builder.append(c.getDouble(energyIndex));
            builder.append("\n  Date: ");
            builder.append(c.getString(dateIndex));
            builder.append("\n");
            c.moveToNext();
        }
        return builder.toString();
    }

    /**
     * Sends 60 entries of data to the back end.
     *
     * @param context of the application
     */
    public static void sendDataToBackend(Context context) {
        new Replicator(context).execute();
    }

}
