package com.example.uk.locationtaskremainder.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * @author vermayash8
 */
public class TopicSubscriber {

    public static final String TAG = TopicSubscriber.class.getSimpleName();

    private static final String[] TOPICS = {
            "global",
            "promotion"
    };

    public static void subscribeToAllTopics() {
        for (String topic : TOPICS) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
            Log.d(TAG, "Subscribed to " + topic);
        }
    }
}
