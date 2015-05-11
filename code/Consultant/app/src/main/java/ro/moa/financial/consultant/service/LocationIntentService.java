package ro.moa.financial.consultant.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.text.format.Time;
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
import java.util.concurrent.TimeUnit;

import ro.moa.financial.consultant.Utils;
import ro.moa.financial.consultant.contract.FinancialContract;
import ro.moa.financial.consultant.contract.FinancialContract.LocationEntry;
import ro.moa.financial.consultant.contract.FinancialProvider;


public class LocationIntentService extends IntentService {

    public final String LOG_TAG = LocationIntentService.class.getSimpleName();
    public static final long KEEP_INTERVAL = TimeUnit.DAYS.toMillis(365); // 1 year

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            if (!validateExistingLocations()) {
                getData();
            }

        }
    }

    private boolean validateExistingLocations() {
        int count = 0;
        final Cursor cursor = getBaseContext().getContentResolver().query(LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID},
                LocationEntry.COLUMN_LAST_UPDATE + " >= ?",
                new String[]{"" + (Utils.getCurrentDate().getTimeInMillis() - KEEP_INTERVAL)}, null);
        while (cursor.moveToNext()) {
            count++;
        }
        cursor.close();
        return count != 0;
    }

    private void getData() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String locationJsonStr = null;


        try {
            final String LOCATION_URL =
                    "http://pfac.us.to/?";
            final String CMD_PARAM = "cmd";
            final String LOCATION_PARAM = "locations";
            Uri builtUri = Uri.parse(LOCATION_URL).buildUpon()
                    .appendQueryParameter(CMD_PARAM, LOCATION_PARAM)
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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            locationJsonStr = buffer.toString();
            Log.i(LOG_TAG, locationJsonStr);
            getLocationDataFromJson(locationJsonStr);
        } catch (IOException e)

        {
            Log.e(LOG_TAG, "Error ", e);
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


    private void getLocationDataFromJson(String locationsJsonStr)
            throws JSONException {

        try {
            JSONArray locationsArray = new JSONArray(locationsJsonStr);
            // Insert the new location information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(locationsArray.length());
            String description;
            for (int i = 0; i < locationsArray.length(); i++) {
                description = (String) locationsArray.get(i);
                Log.i(LOG_TAG, i + "-" + description);

                ContentValues locationValues = new ContentValues();
                locationValues.put(LocationEntry.COLUMN_CITY_NAME, description);
                locationValues.put(LocationEntry.COLUMN_LAST_UPDATE, Utils.getCurrentDate().getTimeInMillis());

                cVVector.add(locationValues);
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                Context context = getBaseContext();
                ContentResolver cr = context.getContentResolver();
                Uri uri = LocationEntry.CONTENT_URI;
                cr.bulkInsert(uri, cvArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static Integer getLocationId(Context context, String location) {
        final Cursor cursor = context.getContentResolver().query(LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID},
                LocationEntry.COLUMN_LAST_UPDATE + " >= ? AND " + LocationEntry.COLUMN_CITY_NAME + " LIKE ?",
                new String[]{"" + (Utils.getCurrentDate().getTimeInMillis() - KEEP_INTERVAL), location}, null);
        Integer result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }
}
