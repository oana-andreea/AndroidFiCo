package ro.moa.financial.consultant.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import ro.moa.financial.consultant.Utils;
import ro.moa.financial.consultant.contract.FinancialContract;


public class CAENIntentService extends IntentService {

    private static final String LOG_TAG = CAENIntentService.class.getSimpleName();
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String LOCATION = "ro.moa.financial.consultant.service.action.ACTION";
    public static final String STATUS = "ro.moa.financial.consultant.service.action.STATUS";
    private static final String DEFAULT_LOCATION = "Cluj-Napoca";

    public CAENIntentService() {
        super("CAENIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String location = Utils.getValueOrDefault(intent.getStringExtra(LOCATION), DEFAULT_LOCATION);
            if (!hasData(location)) {
                getData(location);
            }
            sendStatus(location);
        }
    }

    private boolean hasData(String location) {
        final Integer locationId = LocationIntentService.getLocationId(getBaseContext(), location);
        if (locationId == null) {
            return false;
        }
        final Cursor cursor = getBaseContext().getContentResolver().query(FinancialContract.CodeEntry.CONTENT_URI,
                new String[]{FinancialContract.CodeEntry._ID},
                FinancialContract.CodeEntry.COLUMN_LOC_KEY + " = ?",
                new String[]{locationId + ""}, null);
        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
    }



    private void sendStatus(String location) {
        final Intent intent = new Intent();
        intent.setAction(STATUS);
        intent.putExtra(LOCATION, location);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

    }


    private void getData(String locationQuery) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String locationJsonStr = null;

        try {
            final String LOCATION_URL =
                    "http://pfac.us.to/?";
            final String CMD_PARAM = "cmd";
            final String LOCATIONS = "location";
            final String LOCATION_PARAM = "value";

            Uri builtUri = Uri.parse(LOCATION_URL).buildUpon()
                    .appendQueryParameter(CMD_PARAM, LOCATIONS)
                    .appendQueryParameter(LOCATION_PARAM, locationQuery)
                    .build();


            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            locationJsonStr = buffer.toString();
            //Log.i(LOG_TAG, locationJsonStr);
            //getCAENDataFromJson(locationJsonStr);
            getCAENDataFromJson(locationJsonStr, locationQuery);
        } catch (IOException e)

        {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    private int getLocationId(String location) {
        final Cursor cursor = getBaseContext().getContentResolver().query(FinancialContract.LocationEntry.CONTENT_URI,
                new String[]{FinancialContract.LocationEntry._ID},
                FinancialContract.LocationEntry.COLUMN_CITY_NAME + " LIKE ?",
                new String[]{location},
                null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return -1;
    }

    /**
     * Take the String representing the complete CAEN data in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getCAENDataFromJson(String locationsJsonStr, String location)
            throws JSONException {
        final int locationId = getLocationId(location);
        try {
            String s = locationsJsonStr.replace("\\", "")
                    .replace("u00c3u0085u00c2u009f", "s")
                    .replace("u00c3u0085u00c2u00a3", "t")
                    .replace("u00c3u0084u00c2u0083", "a")
                    .replace("u00c3u00ae", "i")
                    .replace("u00c3u00a2", "a")
                    .replace("u00c3u008e", "I")
                    .replace("u00c3u0082u00c2u00a0", "")
                    .replace("u00c3u0085u00c2u00a2", "T");
            JSONArray codesArray = new JSONArray(s);
            // Insert the new location information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(codesArray.length());

            for (int i = 0; i < codesArray.length(); i++) {
                JSONObject rowCode;
                rowCode = codesArray.getJSONObject(i);
                ContentValues codeValues = new ContentValues();
                codeValues.put(FinancialContract.CodeEntry.COLUMN_LOC_KEY, locationId);
                codeValues.put(FinancialContract.CodeEntry.COLUMN_ACTIVITY, rowCode.getString("activity").trim());
                codeValues.put(FinancialContract.CodeEntry.COLUMN_CODE, rowCode.getString("code").trim());
                codeValues.put(FinancialContract.CodeEntry.COLUMN_DESCRIPTION, rowCode.getString("description"));
                codeValues.put(FinancialContract.CodeEntry.COLUMN_VALUE, Utils.getValueOrDefault(rowCode.getString("value").replace(".", "").trim(), 1));
                cVVector.add(codeValues);
            }

            // add to database
            if (cVVector.size() > 0) {
                getBaseContext().getContentResolver().bulkInsert(FinancialContract.CodeEntry.CONTENT_URI, cVVector.toArray(new ContentValues[cVVector.size()]));
            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


}
