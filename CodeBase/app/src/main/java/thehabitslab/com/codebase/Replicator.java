package thehabitslab.com.codebase;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.net.HttpURLConnection;


/**
 * Sends the energy data to the back end when requested.
 * The Replicator keeps track of when it is replicating and does not replicate multiple times.
 * The replicated data is deleted from the local database.
 * To replicate, this class should be instantiated and execute() should be called.
 * <p/>
 * Students are required to write the meat of the transmission of data to the back end.
 * <p/>
 * Created by William on 12/30/2016.
 */
public class Replicator extends AsyncTask<Void, Void, Object> {
    private static final String TAG = "Replicator";
    private static boolean isReplicating = false;
    private boolean isCanceled = false;

    private Context context;

    public Replicator(Context context) {
        this.context = context;
    }

    @Override
    /**
     * When execute() is called, this happens first
     */
    protected void onPreExecute() {
        isCanceled = isReplicating;
        isReplicating = true;
    }

    @Override
    /**
     * When execute() is called, this happens second
     */
    protected Void doInBackground(Void... params) {

        int backed_up_counter = 0;

        // Don't do anything if the execution is canceled
        if (!isCanceled) {
            // Query the database and package the data
            Cursor c = EnergyDBHelper.getFirst60Entries(context);
            int timeCol = c.getColumnIndex(EnergyDBHelper.EnergyEntry.COLUMN_NAME_TIME);
            int energyCol = c.getColumnIndex(EnergyDBHelper.EnergyEntry.COLUMN_NAME_ENERGY);

            String id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            //c.moveToFirst();
            OutputStreamWriter writer = null;
            BufferedReader reader;
            try {
                try {
                    while (c.moveToNext()) {
                        backed_up_counter++;

                        // collect energy and time
                        String time = c.getString(timeCol);
                        String energy = c.getString(energyCol);
                        String data = "";

                        // construct data string
                        try {
                            data = URLEncoder.encode("mac", "UTF-8")
                                    + "=" + URLEncoder.encode(id, "UTF-8");

                            data += "&" + URLEncoder.encode("time", "UTF-8") + "="
                                    + URLEncoder.encode(time, "UTF-8");

                            data += "&" + URLEncoder.encode("energy", "UTF-8")
                                    + "=" + URLEncoder.encode(energy, "UTF-8");
                        } catch (UnsupportedEncodingException u) {}

                        Log.v(TAG, data);

                        // TRYING ALTERNATE METHOD
                        // citing tutorial here: http://stackoverflow.com/questions/2938502/sending-post-data-in-android
                        URL url;
                        String response = "";
                        try {
                            url = new URL("http://murphy.wot.eecs.northwestern.edu/~rjm240/SQLGateway.py");

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setReadTimeout(15000);
                            conn.setConnectTimeout(15000);
                            conn.setRequestMethod("GET");
                            conn.setDoInput(true);
                            conn.setDoOutput(true);

                            OutputStream os = conn.getOutputStream();
                            BufferedWriter w = new BufferedWriter(
                                    new OutputStreamWriter(os, "UTF-8"));
                            w.write(data);

                            w.flush();
                            w.close();
                            os.close();
                            int responseCode=conn.getResponseCode();

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                String line;
                                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                while ((line=br.readLine()) != null) {
                                    response+=line;
                                }
                            }
                            else {
                                response="";

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Method Will posted on Piazza that I couldn't get to work
                        /*
                        // send the request
                        Log.v(TAG, "should be sending request to back end");
                        URL url = null;
                        try {
                            url = new URL("http://murphy.wot.eecs.northwestern.edu/~rjm240/SQLGateway.py");
                        }catch (MalformedURLException m) {}

                        // Send POST data request
                        try {
                            URLConnection conn = url.openConnection();
                            conn.setDoOutput(true);
                            writer = new OutputStreamWriter(conn.getOutputStream());
                            writer.write( data );
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                } finally {
                    c.close();
                }
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // clear local database
        EnergyDBHelper.deleteNEntries(context, backed_up_counter+1);

        return null;
    }

    @Override
    /**
     * When execute is called, this happens third
     */
    protected void onPostExecute(Object result) {
        isReplicating = false;
    }

}