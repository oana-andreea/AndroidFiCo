package ro.moa.financial.consultant;

/**
 * Created by oana on 5/10/15.
 */

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class Utils {

    public static String getPreferredStringValue(Context context, int id, int defaultValueId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(id), context.getString(defaultValueId));
    }

    public static boolean getPreferredBooleanValue(Context context, int id, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(id), defaultValue);
    }


    public static void notifyUser(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static int getValueOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }

    public static long getValueOrDefault(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }

    public static double getValueOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }

    public static String getValueOrDefault(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }
    public static int getValueOrDefault(EditText text, int defaultValue) {
        return getValueOrDefault(String.valueOf(text != null ? text.getText() : null), defaultValue);
    }

    public static double getValueOrDefault(EditText text, double defaultValue) {
        return getValueOrDefault(String.valueOf(text != null ? text.getText() : null), defaultValue);
    }

    public static Calendar getCurrentDate() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }
}
