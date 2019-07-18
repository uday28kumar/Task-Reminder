package com.example.uk.locationtaskremainder.fcm;

import android.app.PendingIntent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import app.tasknearby.yashcreations.com.tasknearby.notification.NotificationHelper;

/**
 * Handles the incoming FCM messages.
 *
 * @author vermayash8
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        Map<String, String> data = remoteMessage.getData();
        // Check if message contains a data payload.
        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + data);
            if (FcmConstants.TYPE_DISCOUNT.equals(data.get(FcmConstants.FIELD_TYPE))) {
                int index = Integer.parseInt(data.get(FcmConstants.FIELD_INDEX));
                String title = data.get(FcmConstants.FIELD_TITLE);
                String description = data.get(FcmConstants.FIELD_DESCRIPTION);
                DiscountNotificationManager.onDiscountInfoReceived(this, index, title, description);
            }
        } else {
            Log.d(TAG, "No data payload found in the message.");
        }
    }
}
