package com.example.uk.locationtaskremainder.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.notification.NotificationHelper;

/**
 * Set location updates on based on detected activities.
 *
 * @author shilpi
 */

public class FusedLocationService extends Service {

    /**
     * Constants for creating location requests.
     */
    public static final long DEFAULT_LOCATION_UPDATE_INTERVAL = 5000;           // 5 seconds.
    public static final long FASTEST_LOCATION_UPDATE_INTERVAL = 3000;           // 3 seconds.

    /**
     * Constants for activity detection.
     */
    public static final long ACTIVITY_DETECTION_INTERVAL = 2 * 1000;                // 2 seconds.

    /**
     * Constants for update time intervals for different detected activities.
     */
    public static final long DRIVING_LOCATION_UPDATE_INTERVAL = 3 * 1000;           // 3 seconds.
    public static final long RUNNING_LOCATION_UPDATE_INTERVAL = 7 * 1000;          // 7 seconds.
    public static final long FAST_RUNNING_LOCATION_UPDATE_INTERVAL = 5 * 1000;      // 5 seconds.
    public static final long WALKING_LOCATION_UPDATE_INTERVAL = 10 * 1000;          // 10 seconds.
    public static final long UNKNOWN_LOCATION_UPDATE_INTERVAL = 7 * 1000;          // 7 seconds.

    public static final String TAG = FusedLocationService.class.getSimpleName();

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivityDetectionReceiver mActivityDetectionReceiver;
    private boolean isReceivingLocationUpdates;

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Update variable.
        isReceivingLocationUpdates = false;

        // Creating LocationCallback, LocationRequest and LocationSettingsRequest objects.
        createLocationCallback();
        mLocationRequest = createLocationRequest(this, DEFAULT_LOCATION_UPDATE_INTERVAL);

        // Set up activity detection receiver.
        mActivityDetectionReceiver = new ActivityDetectionReceiver();
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mActivityDetectionReceiver,
                        new IntentFilter(ServiceConstants.ACTION_DETECTED_ACTIVITIES));

        // Moved here from onStartCommand so that if the startService function is called on an
        // already started service, it'll just call onStartCommandMethod and not trigger any
        // listeners.
        startLocationUpdates();
        startActivityDetection();
        startServiceInForeground();
    }

    /**
     * Invoked when another component (such as an activity) requests that the service be started.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }


    /**
     * Invoked the service is no longer used and is being destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the listener.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityDetectionReceiver);
        // Stop service and updates.
        stopLocationUpdates();
        stopActivityDetection();
        stopServiceInForeground();
        Log.d(TAG, "Service destroyed. Will now terminate.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Creates location request to be used by fused location client.
     */
    public static LocationRequest createLocationRequest(Context context, long updateInterval) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL);
        locationRequest = setLocationRequestPriority(context, locationRequest);
        return locationRequest;
    }

    /**
     * Creates a callback for receiving location events.
     */
    public void createLocationCallback() {
        mLocationCallback = new LocationResultCallback(getApplicationContext());
    }

    /**
     * Checks for device settings and starts location updates.
     */
    public void startLocationUpdates() {
        // Permission check.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing permissions");
            return;
        }

        Task<Void> task = mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());

        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Update the variable.
                    isReceivingLocationUpdates = true;
                } else {
                    Log.e(TAG, "Location Update Request failed.");
                }
            }
        });

    }

    /**
     * Stops location updates.
     */
    public void stopLocationUpdates() {
        if (isReceivingLocationUpdates) {
            Log.d(TAG, "Stopping location updates.");
            mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnSuccessListener
                    (new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isReceivingLocationUpdates = false;
                        }
                    });
        }
    }

    /**
     * Starts activity recognition.
     */
    public void startActivityDetection() {
        Log.d(TAG, "Starting activity detection.");
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mActivityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL,
                getActivityDetectionPendingIntent());
    }

    /**
     * Returns a pending intent for ActivityDetection.
     */
    public PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, ActivityDetectionService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Stops activity recognition.
     */
    public void stopActivityDetection() {
        Log.d(TAG, "Stopping activity detection.");
        mActivityRecognitionClient.removeActivityUpdates(getActivityDetectionPendingIntent());
    }


    /**
     * Restarts location updates with new update interval.
     */
    public void restartLocationUpdates(long updateInterval) {
        if (mLocationRequest != null && mLocationRequest.getInterval() != updateInterval) {
            Log.i(TAG, "Restarting location updates with updateInterval:" + updateInterval);
            stopLocationUpdates();
            createLocationRequest(this, updateInterval);
            startLocationUpdates();
        }
    }

    /**
     * Starts service in foreground.
     */
    public void startServiceInForeground() {
        Notification notification = new NotificationHelper(this.getApplicationContext())
                .getForegroundServiceNotification();
        startForeground(101, notification);
    }

    /**
     * Stops foreground service.
     */
    public void stopServiceInForeground() {
        stopForeground(true);
    }

    /**
     * Sets the priority for location updates based on power saving mode setting.
     */
    public static LocationRequest setLocationRequestPriority(Context context, LocationRequest
            locationRequest) {
        if (locationRequest == null) {
            Log.e(TAG, "Location Request null while setting priority");
            return null;
        }
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean powerSaverMode = defaultPref.getBoolean(context.getString(R.string
                .pref_power_saver_key), true);
        if (powerSaverMode) {
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        } else {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return locationRequest;
    }

    /**
     * This is called when user removes app's task from the list of recent apps. In that case,
     * We restart the service if app is enabled.
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Check if app is enabled or not.
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAppEnabled = defaultPref.getString(getString(R.string.pref_status_key), getString(
                R.string.pref_status_default)).equals(getString(R.string.pref_status_enabled));
        if (isAppEnabled) {
            Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
            restartServiceTask.setPackage(getPackageName());
            PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(),
                    1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(
                    Context.ALARM_SERVICE);
            alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500,
                    restartPendingIntent);
        }
    }

    /**
     * Receives the broadcasted intent by {@link ActivityDetectionService}.
     */
    public class ActivityDetectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Activity Detection Intent received.");

            ArrayList<DetectedActivity> detectedActivityList =
                    intent.getParcelableArrayListExtra(ServiceConstants.EXTRA_DETECTED_ACTIVITIES);
            adjustLocationUpdates(detectedActivityList);
        }

        /**
         * Adjusts location updates based on the detected activity of the device.
         */
        public void adjustLocationUpdates(ArrayList<DetectedActivity> detectedActivityList) {

            for (DetectedActivity detectedActivity : detectedActivityList) {
                int confidence = detectedActivity.getConfidence();
                switch (detectedActivity.getType()) {
                    case DetectedActivity.STILL:
                        if (confidence > 50) {
                            stopLocationUpdates();
                        }
                        break;

                    case DetectedActivity.IN_VEHICLE:
                        if (confidence > 50) {
                            restartLocationUpdates(DRIVING_LOCATION_UPDATE_INTERVAL);
                        }
                        break;

                    case DetectedActivity.RUNNING:
                    case DetectedActivity.ON_BICYCLE:
                        if (confidence > 60) {
                            restartLocationUpdates(FAST_RUNNING_LOCATION_UPDATE_INTERVAL);
                        } else if (confidence > 50) {
                            restartLocationUpdates(RUNNING_LOCATION_UPDATE_INTERVAL);
                        }
                        break;

                    case DetectedActivity.ON_FOOT:
                    case DetectedActivity.WALKING:
                        if (confidence > 50) {
                            restartLocationUpdates(WALKING_LOCATION_UPDATE_INTERVAL);
                        }
                        break;

                    default:
                        restartLocationUpdates(UNKNOWN_LOCATION_UPDATE_INTERVAL);
                }
            }
        }
    }
}
