package ro.moa.financial.consultant.contract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by oana on 4/9/15.
 */
public class FinancialContract {

    /**
     * Defines table and column names for the weather database.
     */


    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "ro.moa.financial.consultant";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_CODE = "weather";
    public static final String PATH_LOCATION = "location";
    public static final String PATH_RISK = "risk";

    /* Inner class that defines the table contents of the location table */
    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = "location";
        // Human readable location string, provided by the API.  Because for styling,
        // "Mountain View" is more recognizable than 94043.
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_LAST_UPDATE = "last_update";

        public static final String[] COLUMNS_LOCATION_ENTRY = {
                _ID,
                COLUMN_CITY_NAME,
                COLUMN_LAST_UPDATE
        };

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class CodeEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CODE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CODE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CODE;

        public static final String TABLE_NAME = "code";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";
        //The associated activity
        public static final String COLUMN_ACTIVITY = "activity";
        //the code
        public static final String COLUMN_CODE = "code";
        //the code description
        public static final String COLUMN_DESCRIPTION = "description";
        //the activity / code associated value
        public static final String COLUMN_VALUE = "value";

        public static final String[] COLUMNS_CODE_ENTRY = {
                _ID,
                COLUMN_LOC_KEY,
                COLUMN_ACTIVITY,
                COLUMN_CODE,
                COLUMN_VALUE
        };

        public static Uri buildCodeLocation(long locationId) {
            return ContentUris.withAppendedId(CONTENT_URI, locationId);
            //CONTENT_URI.buildUpon().appendPath(locationId).build();
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        public static Uri buildCodeLocationWithActivity(String location, String activity) {
            return CONTENT_URI.buildUpon().appendPath(location)
                    .appendQueryParameter(COLUMN_LOC_KEY, location)
                    .appendQueryParameter(COLUMN_ACTIVITY, activity)
                    .build()
                    ;

        }
    }

    /* Inner class that defines the table contents of the risk table */
    public static class RiskEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RISK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RISK;

        public static final String TABLE_NAME = "risk";

        //The associated activity
        public static final String COLUMN_ACTIVITY = "activity";
        //the code
        public static final String COLUMN_CODE = "code";
        //risk
        public static final String COLUMN_RISK = "risk";
        //tariff
        public static final String COLUMN_TARIFF = "tariff";

        public static Uri buildRiskUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
