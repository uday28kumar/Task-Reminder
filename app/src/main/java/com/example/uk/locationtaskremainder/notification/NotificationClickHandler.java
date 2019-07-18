package com.example.uk.locationtaskremainder.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import app.tasknearby.yashcreations.com.tasknearby.TaskRepository;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskActionUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

/**
 * Handles the notification action button click events.
 *
 * @author vermayash8
 */
public class NotificationClickHandler extends BroadcastReceiver {

    private static final String TAG = NotificationClickHandler.class.getSimpleName();

    public NotificationClickHandler() {
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());
        // Get the taskId.
        long taskId = intent.getLongExtra("taskId", -1);
        if (taskId == -1) {
            Log.w(TAG, "No taskId has been passed from the notification action.");
            return;
        } else if (intent.getAction() == null) {
            return;
        }

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        // TaskRepository to get the task object.
        TaskRepository taskRepository = new TaskRepository(context);
        switch (intent.getAction()) {
            case NotificationConstants.ACTION_MARK_DONE:
                // Task was marked as done.
                TaskActionUtils.onTaskMarkedDone(context, taskRepository.getTaskWithId(taskId));
                // We need to cancel the notification explicitly.
                cancelNotification(context, taskId);
                firebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_NOTIFICATION_MARK_DONE,
                        new Bundle());
                break;
            case NotificationConstants.ACTION_SNOOZE:
                // Task was snoozed.
                TaskActionUtils.onTaskSnoozed(context, taskRepository.getTaskWithId(taskId));
                cancelNotification(context, taskId);
                firebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_NOTIFICATION_SNOOZE,
                        new Bundle());
                break;
        }
    }

    /**
     * Cancels the reminder notification on action button click. Note that this requires that the
     * notification be thrown with a notification id = (int) taskId.
     */
    private void cancelNotification(Context context, long taskId) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel((int) taskId);
    }
}