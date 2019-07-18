package com.example.uk.locationtaskremainder.utils.alarm;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;

/**
 * Voice alerts(reminders).
 *
 * @author shilpi
 */

public class VoiceAlarmRinger implements TextToSpeech.OnInitListener {

    public static final String TAG = VoiceAlarmRinger.class.getSimpleName();
    private TextToSpeech mTts;
    private TaskModel mTask;
    private LocationModel mLocation;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private HashMap<String,String> ttsParams;

    public VoiceAlarmRinger(Context context, TaskModel task, LocationModel location) {
        mContext = context;
        mTask = task;
        mLocation = location;
    }

    private void startSpeaking() {
        mTts = new TextToSpeech(mContext, this);
    }

    private void stopSpeaking() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    /**
     * Called when initialisation of tts is completed.
     *
     * @param status
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech
                    .LANG_NOT_SUPPORTED) {
                Log.e(TAG, Locale.getDefault().getLanguage() + " Language is not supported");
                showSnackbar();
            }
            // Necessary to give it an utterance Id for callback function to run.
            ttsParams = new HashMap<String, String>();
            ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mContext.getPackageName());
            // Callback method for text to speech completion.
            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    // Playing end sound.
                    startSound(false);
                }

                @Override
                public void onError(String utteranceId) {

                }
            });

        } else {
            Log.e(TAG, "Initialization Failed!");
            showSnackbar();
        }
        speakOut();
    }

    private void speakOut() {
        // Text to speak.
        String pause = "... ";
        String text = "Task NearBy " + mContext.getString(R.string.reminder) + pause
                + mTask.getTaskName() + pause + " at " + mLocation.getPlaceName() + pause;
        // Add note.
        if (mTask.getNote() != null) {
            text += mTask.getNote();
        }
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams);
    }

    /**
     * Shows snackbar to set google text to speech settings.
     */
    private void showSnackbar() {
        Snackbar snackbar = Snackbar.make(((Activity) mContext).findViewById(android.R.id
                .content), mContext.getString(R.string
                .error_tts), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void startSound(boolean isStartSound) {
        mediaPlayer = MediaPlayer.create(mContext, R.raw.sound);
        mediaPlayer.setOnCompletionListener(mp -> {
            // Speak voice alarm only if start sound is played.
            if(isStartSound) {
                startSpeaking();
            }
        });
        mediaPlayer.start();
    }

    private void stopSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void startVoiceAlarms() {
        startSound(true);
    }

    public void stopVoiceAlarms() {
        stopSpeaking();
        stopSound();
    }

}
