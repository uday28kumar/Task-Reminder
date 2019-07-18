package com.example.uk.locationtaskremainder.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import app.tasknearby.yashcreations.com.tasknearby.notification.NotificationHelper;
import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;

/**
 * Decides if the discount notification should be shown or not. If yes, then it triggers the
 * notification.
 *
 * @author vermayash8
 */
final class DiscountNotificationManager {

    private static final String TAG = DiscountNotificationManager.class.getSimpleName();

    private static final String PREF_LAST_NOTIFICATION_INDEX = "last_notification_index";

    static void onDiscountInfoReceived(Context context, int index, String title, String message) {
        if (!AppUtils.isPremiumUser(context)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            // What was the index of last shown notification.
            int lastNotificationIndex = prefs.getInt(PREF_LAST_NOTIFICATION_INDEX, -1);
            // Using an index based system for not showing duplicate notifications, if sent
            // multiple times. If the user has already received a push notification with the
            // index 'x', then he won't show any notification to the user with index <= 'x'.
            // Using this approach, we can freely send notifications frequently with the same
            // index for new users without showing it to the users who had already seen it 2 days
            // ago.
            if (lastNotificationIndex < index) {
                new NotificationHelper(context).notifyAboutDiscount(title, message);
                prefs.edit().putInt(PREF_LAST_NOTIFICATION_INDEX, index).apply();
            }
        }
    }
}
