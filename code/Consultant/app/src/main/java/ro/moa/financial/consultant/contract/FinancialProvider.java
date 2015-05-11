package ro.moa.financial.consultant.contract;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import ro.moa.financial.consultant.contract.FinancialContract.CodeEntry;
import ro.moa.financial.consultant.contract.FinancialContract.LocationEntry;
import ro.moa.financial.consultant.contract.FinancialContract.RiskEntry;


/**
 * Created by oana on 4/12/15.
 */

public class FinancialProvider extends ContentProvider {
    private static final int LOCATION = 1;
    private static final int CODE = 2;
    private static final int RISK = 3;
    private static final UriMatcher URI_MATCHER = UriMatcherHolder.URI_MATCHER;
    private SQLiteOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new FinancialDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor result;
        switch (URI_MATCHER.match(uri)) {
            case CODE:
                result = dbHelper.getReadableDatabase().query(CodeEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case LOCATION:
                result = dbHelper.getReadableDatabase().query(LocationEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            case RISK:
                result = dbHelper.getReadableDatabase().query(RiskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final Uri result;
        long statusRowId;
        switch (URI_MATCHER.match(uri)) {
            case LOCATION:
                statusRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
                if (statusRowId > 0) {
                    result = LocationEntry.buildLocationUri(statusRowId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            case CODE:
                statusRowId = db.insert(CodeEntry.TABLE_NAME, null, values);
                if (statusRowId > 0) {
                    result = CodeEntry.buildCodeLocation(statusRowId);
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }
                break;
            case RISK:
                statusRowId = db.insert(RiskEntry.TABLE_NAME, null, values);
                if (statusRowId > 0) {
                    result = RiskEntry.buildRiskUri(statusRowId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CODE:
                return CodeEntry.CONTENT_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case RISK:
                return RiskEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;
        if (selection == null) {
            selection = "1";
        }
        switch (URI_MATCHER.match(uri)) {
            case LOCATION:

                deletedRows = db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CODE:
                deletedRows= db.delete(CodeEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case RISK:
                deletedRows= db.delete(RiskEntry.TABLE_NAME,selection,selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }

        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedRows = 0;
        switch (URI_MATCHER.match(uri)) {
            case LOCATION:
                updatedRows = db.update(LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CODE:
                updatedRows = db.update(CodeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case RISK:
                updatedRows = db.update(RiskEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedRows != 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }

    private static class UriMatcherHolder {
        private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

        static {
            URI_MATCHER.addURI(FinancialContract.CONTENT_AUTHORITY, FinancialContract.PATH_CODE,CODE);
            URI_MATCHER.addURI(FinancialContract.CONTENT_AUTHORITY, FinancialContract.PATH_LOCATION, LOCATION);
            URI_MATCHER.addURI(FinancialContract.CONTENT_AUTHORITY, FinancialContract.PATH_RISK,RISK);
        }
    }
}

