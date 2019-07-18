package com.example.uk.locationtaskremainder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Date;

import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskActionUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskStateUtil;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Constants.
     */
    private static final String TAG = DetailActivity.class.getSimpleName();
    private static final String EXTRA_TASK_ID = "taskIdForDetail";
    private static final String EXTRA_TASK_STATE = "taskStateForDetail";

    /**
     * Views.
     */
    private Button doneButton;
    private TextView taskNameTv, taskStateTv;

    private TaskModel mTask;
    private TaskRepository mTaskRepository;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setActionBar();
        // Passing application context to avoid memory leaks. 
        mTaskRepository = new TaskRepository(getApplicationContext());
        // Get the taskId passed to this activity whose details are to be shown.
        long taskId = this.getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            Log.e(TAG, "No taskId has been passed to DetailActivity.");
            return;
        }
        mTask = mTaskRepository.getTaskWithId(taskId);
        setData(mTask);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public static Intent getStartingIntent(Context context, long taskId,
            @TaskStateUtil.TaskState int state) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_STATE, state);
        return intent;
    }

    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }
    }

    private void setData(TaskModel task) {
        // Set task title on actionBar
        taskNameTv = findViewById(R.id.text_task_name);
        taskNameTv.setText(task.getTaskName());
        showLocationDetails(task);
        showCoverImage(task);
        showTimeDetails(task);
        showDateInterval(task);
        showAlarmStatus(task);
        showRepeatType(task);
        showNote(task);
        // Also set the mark as done button.
        doneButton = findViewById(R.id.btn_action_done);
        setDoneButton(task);
        // Set the directions FAB.
        findViewById(R.id.fab_directions).setOnClickListener(this);
        // Set task status.
        int taskState = getIntent().getIntExtra(EXTRA_TASK_STATE, 0);
        taskStateTv = findViewById(R.id.text_task_state);
        setTaskState(taskState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action_done:
                if (doneButton.getText().equals(getString(R.string.detail_button_mark_done))) {
                    markTaskAsDone(mTask);
                } else {
                    resetTask(mTask);
                }
                break;
            case R.id.fab_directions:
                showDirections();
                mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_SHOW_MAP_FROM_DETAIL,
                        new Bundle());
                break;
        }
    }

    /**
     * Sets the data to reminder range and location name textViews.
     */
    private void showLocationDetails(TaskModel task) {
        // Reminder range.
        TextView reminderRangeTv = findViewById(R.id.text_reminder_range);
        reminderRangeTv.setText(String.format(getString(R.string.detail_format_reminder_range),
                DistanceUtils.getFormattedDistanceString(this, task.getReminderRange())));
        // Set locationName.
        LocationModel locationModel = mTaskRepository.getLocationById(task.getLocationId());
        TextView locationNameTv = findViewById(R.id.text_location_name);
        locationNameTv.setText(locationModel.getPlaceName());
    }

    /**
     * Shows the task's cover image on detail layout.
     */
    private void showCoverImage(TaskModel task) {
        if (task.getImageUri() != null) {
            ImageView imageView = findViewById(R.id.image_task_cover);
            Picasso.with(this)
                    .load("file://" + task.getImageUri())
                    .error(R.drawable.default_task_image)
                    .fit()
                    .centerCrop()
                    .into(imageView);
            imageView.setOnClickListener(v -> {
                Intent intent = ShowImageActivity.getStartingIntent(DetailActivity.this,
                        task.getTaskName(), task.getImageUri());
                startActivity(intent);
            });
        }
    }

    /**
     * Sets the user visible time string to the textView.
     */
    private void showTimeDetails(TaskModel task) {
        // Time range set.
        String timeDisplayString;
        LocalTime startTime = task.getStartTime();
        LocalTime endTime = task.getEndTime();
        if (startTime.getHourOfDay() == 0 && startTime.getMinuteOfHour() == 0
                && endTime.getHourOfDay() == 23 && endTime.getMinuteOfHour() == 59) {
            timeDisplayString = getString(R.string.detail_time_anytime);
        } else {
            timeDisplayString = String.format(getString(R.string.detail_time_format),
                    AppUtils.getReadableTime(this, startTime),
                    AppUtils.getReadableTime(this, endTime));
        }
        TextView timeTv = findViewById(R.id.text_time_range);
        timeTv.setText(timeDisplayString);
    }

    /**
     * Sets the user visible date interval to the textView.
     *
     * @param task The task for which details are being set.
     */
    private void showDateInterval(TaskModel task) {
        // Date range set.
        String dateIntervalString = String.format(getString(R.string.detail_date_format),
                AppUtils.getReadableLocalDate(this, task.getStartDate()),
                AppUtils.getReadableLocalDate(this, task.getEndDate()));
        TextView dateIntervalTv = findViewById(R.id.text_date_interval);
        dateIntervalTv.setText(dateIntervalString);
    }

    /**
     * Sets the alarm status to UI.
     *
     * @param task Task for which details are being set.
     */
    private void showAlarmStatus(TaskModel task) {
        String alarmStatus = getString(R.string.detail_alarm_on);
        if (task.getIsAlarmSet() == 0) {
            alarmStatus = getString(R.string.detail_alarm_off);
        }
        TextView alarmStatusTv = findViewById(R.id.text_alarm);
        alarmStatusTv.setText(String.format(getString(R.string.detail_alarm_format), alarmStatus));
    }

    /**
     * Sets the repeat type of the reminder to the UI.
     */
    private void showRepeatType(TaskModel task) {
        String displayString;
        if (task.getRepeatType() == DbConstants.NO_REPEAT) {
            displayString = getString(R.string.detail_does_not_repeat);
        } else {
            displayString = AppUtils.getRepeatDisplayString(getApplicationContext(), task);
        }
        TextView repeatStatusTv = findViewById(R.id.text_repeat);
        repeatStatusTv.setText(displayString);
    }

    /**
     * Sets the note to UI.
     */
    private void showNote(TaskModel task) {
        TextView noteView = findViewById(R.id.text_note);
        if (task.getNote() == null || TextUtils.isEmpty(task.getNote())) {
            findViewById(R.id.icon_note).setVisibility(View.GONE);
            noteView.setVisibility(View.GONE);
        }
        noteView.setText(task.getNote());
    }

    /**
     * Sets the text shown on done button.
     */
    private void setDoneButton(TaskModel task) {
        if (task.getIsDone() == 0) {
            LocalDate today = LocalDate.fromDateFields(new Date());
            // When the done field is 0, it's possible for a repeating task to be done for that
            // day. Hence, we'll see if the nextStartDate is ahead of today and startDate was
            // before today => It was done. We need to check for the start date to be before
            // because it's possible that the user sets a reminder for 10 days ahead. Then both
            // start and nextStart dates would be 10 days ahead and we'll end up showing done.
            if (task.getRepeatType() == DbConstants.REPEAT_DAILY
                    && task.getNextStartDate().compareTo(today) > 0
                    && task.getStartDate().compareTo(today) <= 0) {
                Log.i(TAG, "Task has been set as done.");
                doneButton.setText(getString(R.string.detail_button_reset));
            } else {
                Log.i(TAG, "Task has been set as not done.");
                doneButton.setText(R.string.detail_button_mark_done);
            }
        } else {
            Log.i(TAG, "Task has been set as done.");
            doneButton.setText(R.string.detail_button_reset);
        }
        doneButton.setOnClickListener(this);
    }

    /**
     * Shows the directions to the location on Google maps.
     */
    private void showDirections() {
        LocationModel locationModel = mTaskRepository.getLocationById(mTask.getLocationId());
        Uri uri = Uri.parse("google.navigation:q=" + locationModel.getLatitude() + ","
                + locationModel.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.error_no_app), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteTask();
                break;
            case R.id.action_edit:
                editTask();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes the task after user's confirmation.
     */
    private void deleteTask() {
        // Show an alert dialog for confirmation.
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.detail_delete_dialog_message)
                .setIcon(R.drawable.ic_delete_black_24dp)
                .setPositiveButton("Delete", (dialog, which) -> {
                    mTaskRepository.removeTask(mTask);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();
        alertDialog.show();
    }

    /**
     * Opens the TaskCreatorActivity for editing the task. Note that the taskId will be passed as
     * an extra args and {@link TaskCreatorActivity} will need to fill in the details initially.
     * Edit is a simplification of delete and add new. This will be the responsibility of
     * TaskCreatorActivity.
     */
    private void editTask() {
        Intent editIntent = TaskCreatorActivity.getEditModeIntent(this, mTask.getId());
        startActivity(editIntent);
        // Ideally we should not finish here and call the activity for result.
        // If successful, update the views. This has been left as a final touch.
        finish();
    }

    /**
     * Resets the task and sets the Done button.
     *
     * @param task
     */
    private void resetTask(TaskModel task) {
        // These are done so that correct state can be calculated.
        task.setIsDone(0);
        long taskSnoozedAt = task.getSnoozedAt();
        task.setSnoozedAt(-1L);

        // Note: This has been added to reset the repeatable reminders that have been marked done
        // by mistake. It is assumed that we don't need to set the nextStartDate back to what it
        // was in case of EXPIRED.
        task.setNextStartDate(LocalDate.fromDateFields(new Date()));

        int state = TaskStateUtil.getTaskState(this, task);
        if (state == TaskStateUtil.STATE_EXPIRED) {
            task.setIsDone(1);
            task.setSnoozedAt(taskSnoozedAt);
            Toast.makeText(this, R.string.detail_msg_expired_cant_reset, Toast.LENGTH_SHORT).show();
        } else {
            // TODO : If we're showing last triggered, then should we set this to null in
            // repeatable reminders too? Because a repeatable reminder was anyways validly last
            // triggered at some point of time. It's being reset only for the day. NOTE: This has
            // been left for now because we're not showing lastTriggered time currently.
            task.setLastTriggered(null);
            Toast.makeText(this, R.string.detail_msg_task_resetted, Toast.LENGTH_SHORT).show();
            setTaskState(state);
            mTaskRepository.updateTask(task);
            setDoneButton(task);
        }
    }

    /**
     * Sets the task as done and sets the Done button.
     */
    private void markTaskAsDone(TaskModel task) {
        TaskActionUtils.onTaskMarkedDone(getApplicationContext(), task);
        setDoneButton(task);
        Toast.makeText(this, R.string.detail_msg_task_marked_done, Toast.LENGTH_SHORT).show();
        // Update the task state.
        setTaskState(TaskStateUtil.getTaskState(this, task));
    }

    private void setTaskState(int state) {
        taskStateTv.setText(TaskStateUtil.stateToString(this, state));
    }

}

