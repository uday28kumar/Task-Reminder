package com.example.uk.locationtaskremainder.utils.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import app.tasknearby.yashcreations.com.tasknearby.R;

/**
 * Handles the ringing functionality for the alarm.
 *
 * @author vermayash8
 */
public class AlarmRinger {

    private static final String TAG = AlarmRinger.class.getSimpleName();

    private final Context mContext;

    private Ringtone ringtone;

    public AlarmRinger(@NonNull Context context) {
        mContext = context;
        setRingtone();
    }

    /**
     * Gets the ringtone from SharedPreferences, AlarmTone or the Ringtone (in decreasing order
     * of priority).
     */
    private void setRingtone() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String tonePath = prefs.getString(mContext.getString(R.string.pref_alarm_tone_key),
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
        if (tonePath == null || tonePath.isEmpty()) {
            // Will use the ringtone now instead of AlarmTone or the preferred tone.
            Log.i(TAG, "Alarm tone uri was null. Will use the Ringtone now.");
            tonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();
            // Do we need to resort to the old MediaPlayer method or is it always != null.
            // throw new IllegalArgumentException("There's no default AlarmTone supplied.");
        }
        Uri alarmToneUri = Uri.parse(tonePath);
        ringtone = RingtoneManager.getRingtone(mContext,alarmToneUri);
    }

    public void startRinging() {
        if (!ringtone.isPlaying()) {
            ringtone.play();
        }
    }

    public void stopRinging() {
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
