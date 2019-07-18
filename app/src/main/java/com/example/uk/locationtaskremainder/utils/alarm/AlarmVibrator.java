package com.example.uk.locationtaskremainder.utils.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import app.tasknearby.yashcreations.com.tasknearby.R;

/**
 * Vibrates as per the user's preferences.
 *
 * @author vermayash8
 */
public class AlarmVibrator {

    private static final String TAG = AlarmVibrator.class.getSimpleName();

    private final Context mContext;

    private Vibrator mVibrator;

    /**
     * Parameters for the vibration pattern and repeat type. Pattern tells the timings for on and
     * off stages of the motor. For eg. a pattern of {1000, 1000, 1000, 1000} specifies:
     * 1. Start for 1 sec.
     * 2. Stop for 1 sec.
     * 3. Start for 1 sec.
     * 4. Stop for 1 sec.
     * If repeat has been set to 0, this pattern repeats itself. -1 means it won't.
     */
    private long[] mPattern;
    private int mRepeat = 0;

    /**
     * Initializes the Vibrator and the pattern needed.
     */
    public AlarmVibrator(Context context) {
        mContext = context;
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        setParameters();
    }

    /**
     * Starts the vibration as per the user's preferences.
     */
    public void startVibrating() {
        // If the settings is 'Don't Vibrate'.
        if (mPattern.length == 0)
            return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mVibrator.vibrate(mPattern, mRepeat);
        } else {
            VibrationEffect vibe = VibrationEffect.createWaveform(mPattern, mRepeat);
            mVibrator.vibrate(vibe);
        }
    }

    /**
     * Stops the vibration.
     */
    public void stopVibrationg() {
        if (mVibrator.hasVibrator()) {
            mVibrator.cancel();
        }
    }

    private void setParameters() {
        // Get vibration pattern from shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int vibrateCode = Integer.parseInt(prefs.getString(mContext.getString(R.string
                .pref_vibrate_key), mContext.getString(R.string.pref_vibrate_default)));
        switch (vibrateCode) {
            case 100:
                // Loop indefinitely.
                mPattern = new long[]{1000, 1000};
                mRepeat = 0;
                break;
            case 5:
                mPattern = new long[]{1000, 1000, 1000, 1000, 1000, 1000};
                mRepeat = -1;
                break;
            case 3:
                mPattern = new long[]{1000, 1000, 1000, 1000};
                mRepeat = -1;
                break;
            default:
                mPattern = new long[]{};
                mRepeat = -1;
                break;
        }
    }
}
