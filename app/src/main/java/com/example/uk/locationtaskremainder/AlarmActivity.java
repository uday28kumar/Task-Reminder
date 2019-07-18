package com.example.uk.locationtaskremainder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskActionUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.alarm.AlarmRinger;
import app.tasknearby.yashcreations.com.tasknearby.utils.alarm.AlarmVibrator;
import app.tasknearby.yashcreations.com.tasknearby.utils.alarm.VoiceAlarmRinger;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

/**
 * Shows the alarm screen with ringing the alarm tone selected by the user.
 *
 * @author vermayash8
 */
public class AlarmActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = AlarmActivity.class.getSimpleName();

    /**
     * Extra for the intent using which the taskId will be passed to this activity.
     */
    private static final String EXTRA_TASK_ID = "taskIdForAlarm";

    private AlarmVibrator mAlarmVibrator;

    private AlarmRinger mAlarmRinger;
    private VoiceAlarmRinger mVoiceAlarmRinger;

    /**
     * For interacting with the database.
     */
    private TaskRepository mTaskRepository;

    /**
     * Data models retrieved from the database.
     */
    private TaskModel mTask;
    private LocationModel mTaskLocation;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean isVoiceReminderEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setWindowFlags();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            Log.w(TAG, "No task id has been passed.");
            return;
        }

        // Fetch data.
        mTaskRepository = new TaskRepository(getApplicationContext());
        mTask = mTaskRepository.getTaskWithId(taskId);
        mTaskLocation = mTaskRepository.getLocationById(mTask.getLocationId());

        // Check if voice reminders are enabled.
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        isVoiceReminderEnabled = defaultPref.getBoolean(getString(R.string
                .pref_voice_alarm_key), false);
        if (isVoiceReminderEnabled) {
            mVoiceAlarmRinger = new VoiceAlarmRinger(this, mTask, mTaskLocation);
        } else {
            // Initialize the ringer and vibrator.
            mAlarmVibrator = new AlarmVibrator(this);
            mAlarmRinger = new AlarmRinger(this);
        }

        setDataToUi();
        setMap();
        setClickListeners();

        // Analytics.
        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ALARM_RING, new Bundle());
    }

    /**
     * This will be used to generate the intent containing taskId when starting AlarmActivity.
     *
     * @param context context of the calling activity.
     * @param taskId  taskId for which alarm will ring.
     * @return the intent that can be passed to startActivity() method.
     */
    public static Intent getStartingIntent(@NonNull Context context, long taskId) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        return intent;
    }

    /**
     * To show this activity even when the screen is locked.
     */
    private void setWindowFlags() {
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= 27) {
            // In API level 27 setting these via window flags is deprecated.
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void setDataToUi() {
        // Find Views by id.
        TextView taskNameView = findViewById(R.id.text_task_name);
        TextView locationNameView = findViewById(R.id.text_location_name);
        TextView lastDistanceView = findViewById(R.id.text_last_distance);
        TextView repeatView = findViewById(R.id.text_repeat);
        TextView noteView = findViewById(R.id.text_note);
        ImageView noteIcon = findViewById(R.id.icon_note);
        ImageView coverImageView = findViewById(R.id.imageViewCover);

        // Set taskDetails.
        taskNameView.setText(mTask.getTaskName());
        locationNameView.setText(mTaskLocation.getPlaceName());
        lastDistanceView.setText(DistanceUtils.getFormattedDistanceString(this, mTask
                .getLastDistance()));
        noteView.setText(mTask.getNote());
        if (mTask.getNote() == null || TextUtils.isEmpty(mTask.getNote())) {
            // Hide note when no note.
            noteIcon.setVisibility(View.GONE);
            noteView.setVisibility(View.GONE);
        }
        // Set image.
        if (mTask.getImageUri() != null) {
            Picasso.with(this)
                    .load("file://" + mTask.getImageUri())
                    .error(R.drawable.default_task_image)
                    .fit()
                    .centerCrop()
                    .into(coverImageView);
            // Set onClick listener.
            coverImageView.setOnClickListener(v -> {
                Intent intent = ShowImageActivity.getStartingIntent(AlarmActivity.this,
                        mTask.getTaskName(), mTask.getImageUri());
                startActivity(intent);
            });
        }
        // Set repeat options, not using for now.
        String[] repeatOptions = getResources().getStringArray(R.array.creator_repeat_options);
        repeatView.setText(repeatOptions[mTask.getRepeatType()]);
    }

    private void setMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id
                .map_fragment);
        mapFragment.getMapAsync(this);
    }

    private void setClickListeners() {
        findViewById(R.id.button_mark_done).setOnClickListener(v -> {
            TaskActionUtils.onTaskMarkedDone(getApplicationContext(), mTask);
            mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ALARM_MARK_DONE, new Bundle());
            finish();
        });
        findViewById(R.id.button_snooze).setOnClickListener(v -> {
            TaskActionUtils.onTaskSnoozed(getApplicationContext(), mTask);
            mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ALARM_SNOOZE, new Bundle());
            finish();
        });
        findViewById(R.id.button_show_map).setOnClickListener(v -> {
            userRequestedMap((Button) v);
            mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ALARM_SHOW_MAP, new Bundle());
        });
        findViewById(R.id.text_notification_only).setOnClickListener(v -> {
            TaskActionUtils.setAsNotificationOnly(getApplicationContext(), mTask);
            mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ALARM_TO_NOTIFICATION, new
                    Bundle());
            finish();
        });
    }

    public void userRequestedMap(Button showMapButton) {
        ImageView coverImage = findViewById(R.id.imageViewCover);
        if (coverImage.getVisibility() == View.VISIBLE) {
            coverImage.setVisibility(View.GONE);
            showMapButton.setText(R.string.alarm_show_image);
        } else {
            coverImage.setVisibility(View.VISIBLE);
            showMapButton.setText(R.string.alarm_show_map);
        }
    }

    /**
     * Centers the map to the current location and adds a marker to it.
     */
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            Log.w(TAG, "onMapReady: null map returned");
            return;
        }
        googleMap.setMyLocationEnabled(true);
        LatLng latLng = new LatLng(mTaskLocation.getLatitude(), mTaskLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        // Remove any pre-existing markers.
        googleMap.clear();
        // Set marker.
        googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_a400_36dp))
                .anchor(0.5f, 1.0f) // bottom middle corner.
                .position(latLng));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isVoiceReminderEnabled) {
            mVoiceAlarmRinger.startVoiceAlarms();
        } else {
            mAlarmVibrator.startVibrating();
            mAlarmRinger.startRinging();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isVoiceReminderEnabled) {
            mVoiceAlarmRinger.stopVoiceAlarms();
        } else {
            mAlarmVibrator.stopVibrationg();
            mAlarmRinger.stopRinging();
        }
    }
}
