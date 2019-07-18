package com.example.uk.locationtaskremainder.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import app.tasknearby.yashcreations.com.tasknearby.DetailActivity;
import app.tasknearby.yashcreations.com.tasknearby.MainActivity;
import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.TaskRepository;
import app.tasknearby.yashcreations.com.tasknearby.UpgradeActivity;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskStateUtil;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

/**
 * Handles the notifications thrown by the app and their associated actions.
 *
 * @author vermayash8
 */
public class NotificationHelper {

    public static final String TAG = NotificationHelper.class.getSimpleName();

    private static final String CHANNEL_REMINDER = "Reminders";
    private static final String CHANNEL_SERVICE_RUNNING = "Foreground Service";
    private static final String CHANNEL_DISCOUNT = "Discount Channel";

    private Context mAppContext;
    private NotificationManager mNotificationManager;

    private FirebaseAnalytics mFirebaseAnalytics;

    public NotificationHelper(Context appContext) {
        this.mAppContext = appContext;
        mNotificationManager = (NotificationManager) appContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
        // Instantiate Firebase Analytics.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mAppContext);
        // Create notification channels. (for API level >= 26)
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannels();
        }
    }

    public Notification getForegroundServiceNotification() {
        Intent intent = new Intent(mAppContext, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mAppContext, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder nb = new NotificationCompat
                .Builder(mAppContext, CHANNEL_SERVICE_RUNNING)
                .setContentText(mAppContext.getString(R.string.msg_notif_foreground_service))
                .setContentIntent(pi)
                .setShowWhen(false) // won't show the timestamp
                .setSmallIcon(R.drawable.ic_stat_notification_small)
                .setPriority(Notification.PRIORITY_MIN);

        if (Build.VERSION.SDK_INT >= 21) {
            nb.setCategory(Notification.CATEGORY_SERVICE);
        }
        return nb.build();
    }

    public void showReminderNotification(TaskModel task) {
        Notification notification = createReminderNotification(task);
        int notificationId = (int) task.getId();
        // This will cause the notification to be shown only if it is not present. However, if we
        // want to beep constantly, remove this.
        mNotificationManager.notify(notificationId, notification);

    }

    private Notification createReminderNotification(TaskModel task) {
        NotificationCompat.Builder nb = new NotificationCompat
                .Builder(mAppContext, CHANNEL_REMINDER)
                .setContentTitle(task.getTaskName())
                .setContentText(getNotificationContentText(task))
                .setContentIntent(getNotificationClickPi(task.getId()))
                .setColor(ContextCompat.getColor(mAppContext, R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setOnlyAlertOnce(true)
                .addAction(getMarkDoneAction(task))
                .addAction(getSnoozeAction(task))
                // These are deprecated in O. (Using notification channels for them.)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= 21) {
            nb.setSmallIcon(R.drawable.ic_stat_notification_small)
                    .setCategory(Notification.CATEGORY_EVENT);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            nb.setBadgeIconType(R.drawable.ic_stat_notification_small);
        }

        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_NOTIFICATION_SHOWN, new Bundle());
        return nb.build();
    }

    /**
     * Gets the text to display below the notification title.
     * Example: 34 m, Hyatt Residence
     */
    private CharSequence getNotificationContentText(TaskModel task) {
        String d = DistanceUtils.getFormattedDistanceString(mAppContext, task.getLastDistance());
        String locationName = new TaskRepository(mAppContext).getLocationById(task.getLocationId())
                .getPlaceName();
        return d + ", " + locationName;
    }

    /**
     * Returns the PendingIntent to start the DetailActivity when notification is clicked.
     */
    private PendingIntent getNotificationClickPi(long taskId) {
        // Intent for starting DetailActivity. State assumed to be active.
        Intent detailIntent = DetailActivity.getStartingIntent(mAppContext, taskId,
                TaskStateUtil.STATE_ACTIVE_NOT_SNOOZED);
        // The returned PendingIntent will have a request code = taskId. (to distinguish)
        return PendingIntent.getActivity(mAppContext, (int) taskId, detailIntent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    private NotificationCompat.Action getMarkDoneAction(TaskModel task) {
        int idAsInt = (int) task.getId();
        Intent intent = new Intent(mAppContext, NotificationClickHandler.class)
                .setAction(NotificationConstants.ACTION_MARK_DONE)
                .putExtra(NotificationConstants.EXTRA_TASK_ID, task.getId());
        // Pending intents need to have their request codes as different, otherwise they'll
        // override each other. So, using taskId(int) as the request code.
        PendingIntent pi = PendingIntent.getBroadcast(mAppContext, idAsInt, intent, PendingIntent
                .FLAG_ONE_SHOT);
        return new NotificationCompat.Action
                .Builder(R.drawable.ic_check_grey_24dp, mAppContext.getString(R.string
                .action_mark_done), pi)
                .build();
    }

    private NotificationCompat.Action getSnoozeAction(TaskModel task) {
        int idAsInt = (int) task.getId();
        Intent intent = new Intent(mAppContext, NotificationClickHandler.class)
                .setAction(NotificationConstants.ACTION_SNOOZE)
                .putExtra(NotificationConstants.EXTRA_TASK_ID, task.getId());
        PendingIntent pi = PendingIntent.getBroadcast(mAppContext, idAsInt, intent, PendingIntent
                .FLAG_ONE_SHOT);
        return new NotificationCompat.Action
                .Builder(R.drawable.ic_replay_black_24dp, mAppContext.getString(R.string
                .action_snooze), pi)
                .build();
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationChannel remindersChannel = new NotificationChannel(CHANNEL_REMINDER,
                "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
        // remindersChannel.setShowBadge(true);
        remindersChannel.enableLights(true);
        remindersChannel.enableVibration(true);
        remindersChannel.setVibrationPattern(new long[]{100, 500, 200, 500});
        remindersChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(remindersChannel);

        NotificationChannel appForegroundChannel = new NotificationChannel
                (CHANNEL_SERVICE_RUNNING, "App Status", NotificationManager.IMPORTANCE_NONE);
        mNotificationManager.createNotificationChannel(appForegroundChannel);

        NotificationChannel discountChannel = new NotificationChannel(
                CHANNEL_DISCOUNT, "Discounts", NotificationManager.IMPORTANCE_DEFAULT);
        discountChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        discountChannel.enableVibration(true);
        mNotificationManager.createNotificationChannel(discountChannel);
    }

    public void notifyAboutDiscount(String title, String message) {
        NotificationCompat.Builder nb = new NotificationCompat
                .Builder(mAppContext, CHANNEL_DISCOUNT)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getDiscountPi())
                .setColor(ContextCompat.getColor(mAppContext, R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setOnlyAlertOnce(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(""))
                // These are deprecated in O. (Using notification channels for them.)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= 21) {
            nb.setSmallIcon(R.drawable.ic_stat_notification_small)
                    .setCategory(Notification.CATEGORY_PROMO);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            nb.setBadgeIconType(R.drawable.ic_stat_notification_small);
        }

        mNotificationManager.notify(1000091, nb.build());
        mFirebaseAnalytics.logEvent(AnalyticsConstants.NOTIFICATION_DISCOUNT_SHOWN, new Bundle());
    }

    private PendingIntent getDiscountPi() {
        Intent intent = new Intent(mAppContext, UpgradeActivity.class);
        intent.putExtra(NotificationConstants.EXTRA_DISCOUNT_NOTIFICATION, true);
        return PendingIntent.getActivity(mAppContext, 1000090, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
