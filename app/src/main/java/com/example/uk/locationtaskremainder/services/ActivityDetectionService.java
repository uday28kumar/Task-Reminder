package com.example.uk.locationtaskremainder.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * IntentService for handling incoming intents that are generated as a result of requesting
 * device activity updates.
 *
 * @author shilpi
 */

public class ActivityDetectionService extends IntentService {

    protected static final String TAG = ActivityDetectionService.class.getSimpleName();

    public ActivityDetectionService() {
        // Used TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents. Broadcasts the list of detected activities.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        ActivityRecognitionResult activityRecognitionResult = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivityList = (ArrayList<DetectedActivity>) activityRecognitionResult.getProbableActivities();

        Intent detectedActivityIntent = new Intent(ServiceConstants.ACTION_DETECTED_ACTIVITIES);
        detectedActivityIntent.putParcelableArrayListExtra(ServiceConstants.EXTRA_DETECTED_ACTIVITIES, detectedActivityList);

        // Broadcast the intent.
        LocalBroadcastManager.getInstance(this).sendBroadcast(detectedActivityIntent);
    }
}