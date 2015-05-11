package ro.moa.financial.consultant.contract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ro.moa.financial.consultant.contract.FinancialContract.LocationEntry;
import ro.moa.financial.consultant.contract.FinancialContract.CodeEntry;
import ro.moa.financial.consultant.contract.FinancialContract.RiskEntry;

/**
 * Created by oana on 4/12/15.
 */

public class FinancialDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "financial.db";

    public FinancialDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LAST_UPDATE + " INTEGER NOT NULL, " +
                "UNIQUE (" + LocationEntry.COLUMN_CITY_NAME + ") ON CONFLICT REPLACE );";


        final String SQL_CREATE_CODE_TABLE = "CREATE TABLE " + CodeEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                FinancialContract.CodeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                CodeEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                CodeEntry.COLUMN_ACTIVITY + " TEXT NOT NULL, " +
                CodeEntry.COLUMN_CODE + " TEXT NOT NULL, " +
                CodeEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                CodeEntry.COLUMN_VALUE + " INTEGER NOT NULL," +


                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + CodeEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + CodeEntry.COLUMN_ACTIVITY + ", " + CodeEntry.COLUMN_CODE + ", " +
                CodeEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_RISK_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s %s, %s %s, %s %s, %s %s, UNIQUE (%s, %s) ON CONFLICT REPLACE);",
                RiskEntry.TABLE_NAME, RiskEntry._ID,
                RiskEntry.COLUMN_ACTIVITY, "TEXT NOT NULL",
                RiskEntry.COLUMN_CODE, "TEXT NOT NULL",
                RiskEntry.COLUMN_RISK, "TEXT NOT NULL",
                RiskEntry.COLUMN_TARIFF, "TEXT NOT NULL",
                RiskEntry.COLUMN_ACTIVITY, RiskEntry.COLUMN_CODE
        );
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CODE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RISK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CodeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RiskEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
