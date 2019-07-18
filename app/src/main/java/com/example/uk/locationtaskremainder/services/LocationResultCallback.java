package com.example.uk.locationtaskremainder.services;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;
import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.AlarmActivity;
import app.tasknearby.yashcreations.com.tasknearby.TaskRepository;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.notification.NotificationHelper;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskStateUtil;

/**
 * Receives location result callbacks and check for tasks for which an action has to be taken.
 *
 * @author shilpi
 */
public class LocationResultCallback extends LocationCallback {

    private static final String TAG = LocationResultCallback.class.getSimpleName();

    private Context mContext;
    private TaskRepository mTaskRepository;
    private Location mLastLocation;

    private NotificationHelper mNotificationHelper;

    LocationResultCallback(Context context) {
        mContext = context;
        mTaskRepository = new TaskRepository(context);
        mNotificationHelper = new NotificationHelper(mContext.getApplicationContext());
        mLastLocation = null;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Log.d(TAG, "LocationResult received.");
        Location currentLocation = locationResult.getLastLocation();
        if (mLastLocation == null || !isLocationSame(currentLocation, mLastLocation)) {
            // Performing all operations on a different thread.
            LocationCallbackRunnable callbackThread = new LocationCallbackRunnable(locationResult);
            new Thread(callbackThread).start();
            mLastLocation = locationResult.getLastLocation();
        }
    }

    private boolean isLocationSame(Location locationA, Location locationB) {
        return (locationA.getLongitude() == locationB.getLongitude() && locationA.getLatitude()
                == locationB.getLatitude());
    }


    private class LocationCallbackRunnable implements Runnable {

        private LocationResult mLocationResult;

        LocationCallbackRunnable(LocationResult locationResult) {
            mLocationResult = locationResult;
        }

        @Override
        public void run() {
            Location currentLocation = mLocationResult.getLastLocation();

            // Get all the tasks not marked done and active for today.
            List<TaskModel> tasks = mTaskRepository.getNotDoneTasksForToday();
            List<TaskModel> tasksToUpdate = new ArrayList<>();

            // For each Task:
            // 1. If it is active in the current time
            // 2. Calculate distance form task's location.
            // 3. Check if last distance is less than the reminder range.
            // 4. If it's a repeatable task, is it valid today.
            // 4. Check for snoozed or not. Proceed accordingly.
            // 5. Update the task.
            for (TaskModel task : tasks) {

                int taskState = TaskStateUtil.getTaskState(mContext, task);

                if (taskState == TaskStateUtil.STATE_ACTIVE_SNOOZED
                        || taskState == TaskStateUtil.STATE_ACTIVE_NOT_SNOOZED) {

                    // Get the distance from task's location.
                    LocationModel taskLocation = mTaskRepository.getLocationById(task
                            .getLocationId());
                    float lastDistance = DistanceUtils.getDistance(currentLocation, taskLocation);
                    // Set the last distance.
                    task.setLastDistance(lastDistance);

                    if (lastDistance <= task.getReminderRange()
                            && taskState == TaskStateUtil.STATE_ACTIVE_NOT_SNOOZED) {
                        alertUser(task);
                    }

                    // Add to the tasks to be updated list.
                    tasksToUpdate.add(task);
                }
            }
            // Batch update tasks.
            mTaskRepository.updateTasks(tasksToUpdate);
            Log.i(TAG, "Tasks updated successfully.");
        }

        /**
         * Alerts the user for the particular task after deciding on the basis of user's preference.
         */
        private void alertUser(TaskModel task) {
            if (task.getIsAlarmSet() == 1) {
                Intent alarmIntent = AlarmActivity.getStartingIntent(mContext, task.getId());
                alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(alarmIntent);
            } else {
                mNotificationHelper.showReminderNotification(task);
            }
        }
    }
}