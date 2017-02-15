package thehabitslab.com.codebase;


import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Helper for the energy database.
 * Steps to reproduce can be found in the tutorial
 * <a href="https://developer.android.com/training/basics/data-storage/databases.html">here</a>.
 * <p/>
 * In this file, the students should write the queries to carry out necessary operations. Currently,
 * they are not required to write queries to create the database and the contract is given to them
 * in order to maintain consistency among solutions.
 * <p/>
 * Created by William on 12/29/2016
 */
public class EnergyDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    /* ********************************* DATABASE STRUCTURE *********************************** */
    // DB metadata
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "energy.db";

    // SQL instructions for creation and deletion of the table
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EnergyEntry.TABLE_NAME + " (" +
                    EnergyEntry.COLUMN_NAME_ENERGY + " DOUBLE, " +
                    EnergyEntry.COLUMN_NAME_TIME + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EnergyEntry.TABLE_NAME;

    /**
     * Maintains the instance for the singleton model
     */
    private static EnergyDBHelper instance = null;

    /**
     * Returns the current instance of the DBHelper
     *
     * @param context of the app
     * @return the helper
     */
    public static EnergyDBHelper getInstance(Context context) {
        if (instance == null) instance = new EnergyDBHelper(context);
        return instance;
    }

    /**
     * Inner class for table contents
     */
    public class EnergyEntry implements BaseColumns {
        public static final String TABLE_NAME = "entries";
        public static final String COLUMN_NAME_ENERGY = "energy";
        public static final String COLUMN_NAME_TIME = "date";
    }

    private EnergyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Called when the database version increases
     *
     * @param db         database
     * @param oldVersion previous version number
     * @param newVersion current version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Called when the database version decreases
     *
     * @param db         database
     * @param oldVersion previous version number
     * @param newVersion current version number
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /* *********************************** INTERRACTION METHODS ****************************** */

    /**
     * Adds an energy value to the current database
     *
     * @param energy  class instance representing the energy
     * @param context of the application
     */
    public static void enterEnergy(EnergyReading energy, Context context) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EnergyEntry.COLUMN_NAME_ENERGY, energy.getEnergy());
        values.put(EnergyEntry.COLUMN_NAME_TIME, energy.getDate());

        db.insert(EnergyEntry.TABLE_NAME, null, values);
    }

    /**
     * Queries the database for the last 60 entries by datetime. Method taken from tutorial
     * <a href="https://developer.android.com/training/basics/data-storage/databases.html#DbHelper>here</a>.
     *
     * @param context of the application
     * @return a @Cursor containing the data
     */
    public static Cursor getLatest60Entries(Context context) {

        SQLiteDatabase db = getInstance(context).getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                EnergyEntry.COLUMN_NAME_ENERGY,
                EnergyEntry.COLUMN_NAME_TIME
        };

        // Sort by descending time so newest on top
        String sortOrder =
                EnergyEntry.COLUMN_NAME_TIME + " DESC";

        // Limit to get only latest 60
        String limit = "60";

        Cursor cursor = db.query(
                EnergyEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                limit
        );

        return cursor;
    }

    /**
     * Queries for the first 60 entries by datetime. For info about the query() method, see
     * <a href="https://developer.android.com/training/basics/data-storage/databases.html#DbHelper">this</a>
     * link.
     *
     * @param context of the application
     * @return a Cursor containing the data
     */
    public static Cursor getFirst60Entries(Context context) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                EnergyEntry.COLUMN_NAME_ENERGY,
                EnergyEntry.COLUMN_NAME_TIME
        };

        // Sort by ascending time so oldest on top
        String sortOrder =
                EnergyEntry.COLUMN_NAME_TIME + " ASC";

        // Limit to get only oldest 60
        String limit = "60";

        Cursor cursor = db.query(
                EnergyEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                limit
        );

        return cursor;
    }

    /**
     * Deletes the n oldest entries from the sqlite database. This should be used after successfully
     * backing up the entries to the server
     *
     * @param context of the application
     * @param n       number of entries to delete
     */
    public static void deleteNEntries(Context context, int n) {

        SQLiteDatabase db = getInstance(context).getWritableDatabase();

        String SQL_DELETE_N_ENTRIES =
                "DELETE FROM " + EnergyEntry.TABLE_NAME +
                        " WHERE " + EnergyEntry.COLUMN_NAME_TIME +
                        " IN (SELECT " + EnergyEntry.COLUMN_NAME_TIME +
                        " FROM " + EnergyEntry.TABLE_NAME + " ORDER BY "
                        + EnergyEntry.COLUMN_NAME_TIME + " ASC LIMIT " + n + ")";

        db.execSQL(SQL_DELETE_N_ENTRIES);

    }
}

