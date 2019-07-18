package com.example.uk.locationtaskremainder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;

/**
 * Lists all distance utility functions.
 *
 * @author shilpi
 */

public class DistanceUtils {

    /**
     * Returns formatted distance string in metric/imperial units according to user's settings.
     */
    public static String getFormattedDistanceString(Context context, double distance) {

        // Check for units in settings.
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
        String unitsPref = defaultPref.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_default));

        // Check if units is metric or imperial.
        if (unitsPref.equals(context.getString(R.string.pref_unit_metric))) {
            return getFormattedMetricString(context, distance);
        } else {
            return getFormattedImperialString(context, distance);
        }
    }

    /**
     * Returns distance in metric format. For meters, it uses integer distance. For kms, it uses
     * double distance.
     */
    public static String getFormattedMetricString(Context context, double distance) {
        if (distance < 1000) {
            int distInM = (int) distance;
            return String.format(context.getString(R.string.distance_format_m), distInM);
        } else {
            double distInKm = distance / 1000;
            return String.format(context.getString(R.string.distance_format_km), distInKm);
        }
    }

    /**
     * Returns distance in imperial format. For yards, it uses integer distance. For miles, it uses
     * double distance.
     */
    public static String getFormattedImperialString(Context context, double distance) {
        // Changing to yards.
        distance = distance * 1.09361;
        if (distance < 1760) {
            int distInY = (int) distance;
            return context.getString(R.string.distance_format_yd, distInY);
        } else {
            double distInMi = distance / 1760;
            return context.getString(R.string.distance_format_mi, distInMi);
        }
    }

    /**
     * Returns distance to be saved in the database according to user's settings.
     */
    public static double getDistanceToSave(Context context, double distance) {
        // Check for units in settings.
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
        String unitsPref = defaultPref.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_default));
        if (unitsPref.equals(context.getString(R.string.pref_unit_default))) {
            return distance;
        } else {
            return yardsToMeters(distance);
        }

    }

    public static double yardsToMeters(double yards) {
        return yards * 0.9144;
    }

    /**
     * Returns the distance of given Location from the task location.
     */
    public static float getDistance(Location currentLocation, LocationModel locationModel) {
        Location taskLocation = new Location(locationModel.getPlaceName());
        taskLocation.setLatitude(locationModel.getLatitude());
        taskLocation.setLongitude(locationModel.getLongitude());
        return currentLocation.distanceTo(taskLocation);
    }

}
