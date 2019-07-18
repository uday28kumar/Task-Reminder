package com.example.uk.locationtaskremainder.fcm;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Generates FCM token and subscribes to topics.
 *
 * @author vermayash8
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    private static final String PREF_FCM_TOKEN = "pref_fcm_token";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(PREF_FCM_TOKEN, refreshedToken).apply();
        Log.i(TAG, "Token has been refreshed to : " + refreshedToken);
        TopicSubscriber.subscribeToAllTopics();
    }
}
