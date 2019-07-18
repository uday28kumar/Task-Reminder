package com.example.uk.locationtaskremainder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.WeekdayCodeUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;


/**
 * Creates a new task and also responsible for editing an old one. For editing, we need to use
 * the getEditModeIntent() method to get the starting intent.
 *
 * @author vermayash8
 */
public class TaskCreatorActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TaskCreatorActivity.class.getSimpleName();

    /**
     * Since this activity serves both edit and add task operations, when this extra is set in
     * the calling intent, it will be started in edit mode.
     */
    private static final String EXTRA_EDIT_MODE_TASK_ID = "editTaskIdTaskCreatorActivity";

    /**
     * Request code constants.
     */
    private static final int REQUEST_CODE_PLACE_PICKER = 0;
    private static final int REQUEST_CODE_LOCATION_SELECTION = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_GALLERY_IMAGE_PICKER = 4;

    private EditText taskNameInput;
    private EditText locationNameInput;
    private EditText reminderRangeInput;
    private EditText noteInput;
    private TextView startTimeTv, endTimeTv;
    private TextView startDateTv, endDateTv;
    private TextView unitsTv;
    private ImageView taskImageView, arrowAttachmentImage, arrowScheduleImage;
    private Switch alarmSwitch;
    private Switch anytimeSwitch;
    private Switch repeatSwitch;
    private ViewStub weekdaysStub;
    private LinearLayout selectLocationLayout, selectImageLayout, attachmentTitleLayout,
            scheduleTitleLayout, timeIntervalLayout, startTimeLayout, endTimeLayout,
            startDateLayout, endDateLayout, lockLayoutAttachment, lockLayoutSchdule;
    private FrameLayout scheduleFrameLayout, attachmentFrameLayout;
    private RecyclerView locationRecyclerView;
    private Button saveButton, upgradeAttachmentButton, upgradeScheduleButton;
    private WeekdaysDataSource wds;

    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Tells if the task present is being edited or a new one is being created.
     */
    private TaskModel taskBeingEdited = null;

    /**
     * For keeping track of selected location.
     */
    private boolean hasSelectedLocation = false;
    private LocationModel mSelectedLocation;

    private TaskRepository mTaskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creator);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mTaskRepository = new TaskRepository(getApplicationContext());

        setActionBar();
        // Find views and set click listeners.
        initializeViews();
        setRecentPlaces();

        // check if activity has been started for editing a task.
        if (getIntent().hasExtra(EXTRA_EDIT_MODE_TASK_ID)) {
            long taskId = getIntent().getLongExtra(EXTRA_EDIT_MODE_TASK_ID, -1);
            taskBeingEdited = mTaskRepository.getTaskWithId(taskId);
            fillDataForEditing(taskBeingEdited);
            getSupportActionBar().setTitle(getString(R.string.title_edit_task));
        }

    }

    /**
     * This will be used to get the intent to start this activity when we need to edit the task.
     *
     * @param context context of the calling activity.
     * @param taskId  taskId of the task to be edited.
     * @return intent that can be used in startActivity.
     */
    public static Intent getEditModeIntent(Context context, long taskId) {
        Intent intent = new Intent(context, TaskCreatorActivity.class);
        intent.putExtra(EXTRA_EDIT_MODE_TASK_ID, taskId);
        return intent;
    }


    /**
     * Sets the toolbar as actionBar and also sets the up button.
     */
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setElevation(0);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }
    }


    /**
     * Finds views by id and sets OnClickListener to them.
     */
    private void initializeViews() {

        // initializing all views
        taskNameInput = findViewById(R.id.edit_text_task_name);
        locationNameInput = findViewById(R.id.editText_location_name);
        reminderRangeInput = findViewById(R.id.edit_text_reminder_range);
        noteInput = findViewById(R.id.edit_text_note);
        taskImageView = findViewById(R.id.image_selected_image);
        alarmSwitch = findViewById(R.id.switch_alarm);
        attachmentFrameLayout = findViewById(R.id.frame_layout_attachment);
        scheduleFrameLayout = findViewById(R.id.frame_layout_schedule);
        selectLocationLayout = findViewById(R.id.layout_select_location);
        selectImageLayout = findViewById(R.id.layout_select_image);
        attachmentTitleLayout = findViewById(R.id.layout_title_attachment);
        scheduleTitleLayout = findViewById(R.id.layout_title_schedule);
        arrowAttachmentImage = findViewById(R.id.image_arrow_attachment);
        arrowScheduleImage = findViewById(R.id.image_arrow_schedule);
        unitsTv = findViewById(R.id.text_units);
        anytimeSwitch = findViewById(R.id.switch_anytime);
        timeIntervalLayout = findViewById(R.id.layout_time_interval);
        startTimeLayout = findViewById(R.id.layout_time_from);
        endTimeLayout = findViewById(R.id.layout_time_to);
        startTimeTv = findViewById(R.id.text_time_from);
        endTimeTv = findViewById(R.id.text_time_to);
        startDateLayout = findViewById(R.id.layout_date_from);
        endDateLayout = findViewById(R.id.layout_date_to);
        startDateTv = findViewById(R.id.text_date_from);
        endDateTv = findViewById(R.id.text_date_to);
        repeatSwitch = findViewById(R.id.switch_repeat);
        weekdaysStub = findViewById(R.id.viewStub_repeat);
        locationRecyclerView = findViewById(R.id.recycler_view_location);
        saveButton = findViewById(R.id.button_save);
        upgradeAttachmentButton = findViewById(R.id.button_upgrade_attachment);
        upgradeScheduleButton = findViewById(R.id.button_upgrade_schedule);
        lockLayoutAttachment = findViewById(R.id.ll_premium_overlay_lock_attachment);
        lockLayoutSchdule = findViewById(R.id.ll_premium_overlay_lock_schedule);

        // setting on click listeners
        attachmentTitleLayout.setOnClickListener(this);
        scheduleTitleLayout.setOnClickListener(this);
        selectImageLayout.setOnClickListener(this);
        startDateLayout.setOnClickListener(this);
        endDateLayout.setOnClickListener(this);
        startTimeLayout.setOnClickListener(this);
        endTimeLayout.setOnClickListener(this);
        selectLocationLayout.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        upgradeScheduleButton.setOnClickListener(this);
        upgradeAttachmentButton.setOnClickListener(this);
        lockLayoutAttachment.setOnClickListener(this);
        lockLayoutSchdule.setOnClickListener(this);

        // setting defaults and other settings

        // setting default distance range
        String defReminderRange = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_distance_range_key),
                        getString(R.string.pref_distance_range_default));
        reminderRangeInput.setText(defReminderRange);

        // setting units
        setReminderRangeUnits();

        // setting anytime switch
        anytimeSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            timeIntervalLayout.setVisibility((isChecked) ? View.GONE : View.VISIBLE);
        }));

        // setting time interval tags with default value
        startTimeTv.setTag(new LocalTime(0, 0));
        endTimeTv.setTag(new LocalTime(23, 59));

        // setting date interval tags with default value
        startDateTv.setTag(new LocalDate());
        endDateTv.setTag(null);

        // setting repeat switch
        repeatSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                weekdaysStub.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        // setting weekday stub
        setupWeekdayBar();
    }

    /**
     * Specifies the action to be taken when a view is clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.layout_title_attachment:
                if (attachmentFrameLayout.getVisibility() == View.GONE) {
                    expandAttachment();
                } else {
                    collapseAttachment();
                }
                break;
            case R.id.layout_title_schedule:
                if (scheduleFrameLayout.getVisibility() == View.GONE) {
                    expandSchedule();
                } else {
                    collapseSchedule();
                }
                break;
            case R.id.layout_select_image:
                addTaskImage();
                break;
            case R.id.layout_time_from:
                timeSelectionTriggered(startTimeTv);
                break;
            case R.id.layout_time_to:
                timeSelectionTriggered(endTimeTv);
                break;
            case R.id.layout_date_from:
                dateSelectionTriggered(startDateTv);
                break;
            case R.id.layout_date_to:
                dateSelectionTriggered(endDateTv);
                break;
            case R.id.layout_select_location:
                onPlacePickerRequested();
                break;
            case R.id.button_save:
                saveTask();
                break;
            case R.id.button_upgrade_attachment:
            case R.id.button_upgrade_schedule:
            case R.id.ll_premium_overlay_lock_attachment:
            case R.id.ll_premium_overlay_lock_schedule:
                mFirebaseAnalytics.logEvent(AnalyticsConstants.PREMIUM_DIALOG_REQUESTED_BY_BUTTON,
                        new Bundle());
                UpgradeActivity.show(TaskCreatorActivity.this);
                break;

        }
    }

    /**
     * Returns the slideup animation.
     */
    private Animation getSlideUpAnimation() {
        return AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
    }

    /**
     * Returns the slidedown animation.
     */
    private Animation getSlideDownAnimation() {
        return AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
    }

    /**
     * Expands the attachment layout
     */
    private void expandAttachment() {
        attachmentFrameLayout.setVisibility(View.VISIBLE);
        attachmentFrameLayout.startAnimation(getSlideDownAnimation());
        arrowAttachmentImage.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24px);
    }

    /**
     * Collapses attachment layout.
     */
    private void collapseAttachment() {
        attachmentFrameLayout.setVisibility(View.GONE);
        attachmentFrameLayout.startAnimation(getSlideUpAnimation());
        arrowAttachmentImage.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24px);
    }

    /**
     * Expands schedule layout.
     */
    private void expandSchedule() {
        scheduleFrameLayout.setVisibility(View.VISIBLE);
        scheduleFrameLayout.startAnimation(getSlideDownAnimation());
        arrowScheduleImage.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24px);
    }

    /**
     * Collapses schedule layout.
     */
    private void collapseSchedule() {
        scheduleFrameLayout.setVisibility(View.GONE);
        scheduleFrameLayout.startAnimation(getSlideUpAnimation());
        arrowScheduleImage.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24px);
    }

    /**
     * Handles the adding of the task image.This also checks and requests if required permissions
     * are not available.
     */
    private void addTaskImage() {
        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_ADD_IMAGE, new Bundle());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            // Permission is available.
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images
                    .Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_CODE_GALLERY_IMAGE_PICKER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addTaskImage();
                } else {
                    Toast.makeText(this, R.string.creator_error_image_permission,
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_GALLERY_IMAGE_PICKER:
                if (resultCode == RESULT_OK) {
                    onTaskImageSelected(data);
                }
                break;
            case REQUEST_CODE_PLACE_PICKER:
                if (resultCode == RESULT_OK) {
                    onPlacePickerSuccess(data);
                }
                break;
            case REQUEST_CODE_LOCATION_SELECTION:
                if (resultCode == RESULT_OK) {
                    onSavedPlacesSuccess(data);
                }
                break;
            default:
                Log.w(TAG, "Unknown request code in onActivityResult.");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Action to take when user selectes the image.
     */
    private void onTaskImageSelected(Intent data) {
        if (data.getData() == null) {
            Toast.makeText(this, R.string.creator_msg_image_selection_failed, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Uri selectedImageUri = data.getData();
        taskImageView.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(selectedImageUri)
                .fit()
                .centerCrop()
                .into(taskImageView);
        // We need to generate the image file path from the uri.
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn,
                null, null, null);
        cursor.moveToFirst();
        String imageFilePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
        cursor.close();
        // Set the path as a tag to the imageView for storing in the database
        // for retrieval later on.
        taskImageView.setTag(imageFilePath);
    }

    /**
     * sets units for reminder range.
     */
    private void setReminderRangeUnits() {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitsPref = defaultPref.getString(getString(R.string.pref_unit_key), getString(R
                .string.pref_unit_default));
        if (unitsPref.equals(getString(R.string.pref_unit_metric))) {
            unitsTv.setText(getString(R.string.unit_metres));
        } else {
            unitsTv.setText(getString(R.string.unit_yards));
        }
    }

    private void setupWeekdayBar() {
        // Assumption: No day is selected initially.
        weekdaysStub.setTag(0);
        wds = new WeekdaysDataSource(this, R.id.viewStub_repeat)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setUnselectedColorRes(R.color.dark_grey)
                .start(new WeekdaysDataSource.Callback() {
                    /**
                     * Called every time an item is clicked (selected or deselected).
                     *
                     * @param weekdaysDataItem calling getCalendarDayId() on this returns the
                     *                         day's index as in Java Calendar API. Sunday = 1,
                     *                         Monday = 2....
                     */
                    @Override
                    public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {
                        int dayCode = WeekdayCodeUtils
                                .getDayCodeByCalendarDayId(weekdaysDataItem.getCalendarDayId());
                        int selection = (int) weekdaysStub.getTag();
                        // Doing an XOR here so that if tapped again, then the day is removed.
                        selection ^= dayCode;
                        weekdaysStub.setTag(selection);
                        Log.d(TAG, "Selected days : " + selection);
                    }

                    @Override
                    public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {
                    }
                });
        // Need to explicitly make it GONE in code.
        weekdaysStub.setVisibility(View.GONE);
    }

    private void timeSelectionTriggered(TextView v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    Log.d(TAG, "Time selected, " + hourOfDay + ":" + minute);
                    // storing the time object in the textView itself.
                    LocalTime localTime = new LocalTime(hourOfDay, minute);
                    v.setTag(localTime);
                    // set selected Time on textView.
                    v.setText(AppUtils.getReadableTime(TaskCreatorActivity.this, localTime));
                }, 12, 0, false); // time at which timepicker opens.
        timePickerDialog.show();
    }

    /**
     * Called when user clicks on Date display.
     */
    private void dateSelectionTriggered(TextView v) {
        Calendar calendar = Calendar.getInstance();
        // what to do when date is set.
        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month,
                                                                dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            v.setTag(LocalDate.fromCalendarFields(calendar));
            v.setText(AppUtils.getReadableDate(this, calendar.getTime()));
            Log.d(TAG, "Date selected: " + calendar.getTime().toString());
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                calendar.get(Calendar.YEAR),            // current year.
                calendar.get(Calendar.MONTH),           // current month (0 indexed)
                calendar.get(Calendar.DAY_OF_MONTH));   // current day.
        datePickerDialog.show();
    }

    /**
     * Triggered when the user clicks on the Pick Place button.
     */
    private void onPlacePickerRequested() {
        if (!isInternetConnected())
            return;
        PlacePicker.IntentBuilder placePickerIntent = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(placePickerIntent.build(this), REQUEST_CODE_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            mFirebaseAnalytics.logEvent(AnalyticsConstants.PLACE_PICKER_EXCEPTION, new Bundle());
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            mFirebaseAnalytics.logEvent(AnalyticsConstants.PLACE_PICKER_FATAL, new Bundle());
            e.printStackTrace();
        }
    }

    /**
     * Checks for internet permission. If internet is not connected, it shows a snackbar and
     * return false.
     */
    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() == null) {
            // No internet connection present. Show snackbar.
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R
                            .string.creator_no_internet_error),
                    Snackbar.LENGTH_SHORT);
            snackbar.show();
            return false;
        }
        return true;
    }

    /**
     * Initializes the location with place picker returned data. Also sets that to the UI.
     */
    private void onPlacePickerSuccess(Intent data) {
        Place place = PlacePicker.getPlace(this, data);
        // Create a new location object with use count = 1
        mSelectedLocation = new LocationModel(place.getName().toString(),
                place.getLatLng().latitude,
                place.getLatLng().longitude,
                1, 0, new LocalDate());
        hasSelectedLocation = true;
        onLocationSelected();
    }

    /**
     * Sets the selected location's name to the input textView.
     */
    private void onLocationSelected() {
        locationNameInput.setText(mSelectedLocation.getPlaceName());
        locationNameInput.setVisibility(View.VISIBLE);
    }

    /**
     * Gets the result from saved places selection activity and sets the location.
     */
    private void onSavedPlacesSuccess(Intent data) {
        if (data == null || !data.hasExtra(SavedPlacesActivity.EXTRA_LOCATION_ID)) {
            Log.w(TAG, "No location id was returned by SavedPlacesActivity");
            return;
        }
        long locationId = data.getLongExtra(SavedPlacesActivity.EXTRA_LOCATION_ID, -1);
        mSelectedLocation = mTaskRepository.getLocationById(locationId);
        hasSelectedLocation = true;
        onLocationSelected();
        // TODO: check this. Do we need this or not.
        setRecentPlaces();
    }

    /**
     * Set the recent places recycler view if there is any recent place present
     */
    private void setRecentPlaces() {
        List<LocationModel> locations = mTaskRepository.getAllLocations();
        if (locations.size() == 0) {
            return;
        }

        locationRecyclerView.setVisibility(View.VISIBLE);
        // Sort in descending order of use count.
        Collections.sort(locations, (o1, o2) -> o2.getUseCount() - o1.getUseCount());

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        locationRecyclerView.setLayoutManager(horizontalLayoutManager);
        if (locations.size() > 5) {
            locationRecyclerView.setAdapter(new RecyclerAdapter(locations.subList(0, 5)));
        } else {
            locationRecyclerView.setAdapter(new RecyclerAdapter(locations));
        }

    }

    /**
     * Validates the input entered by the user.
     */
    private boolean isInputValid() {
        String errorMsg = null;
        if (TextUtils.isEmpty(taskNameInput.getText())) {
            errorMsg = getString(R.string.creator_error_empty_taskname);
        } else if (TextUtils.isEmpty(locationNameInput.getText()) || !hasSelectedLocation) {
            errorMsg = getString(R.string.creator_error_empty_location);
        } else if (TextUtils.isEmpty(reminderRangeInput.getText())) {
            errorMsg = getString(R.string.creator_error_empty_range);
        } else if (repeatSwitch.isChecked() && (int) weekdaysStub.getTag() == 0) {
            errorMsg = getString(R.string.creator_error_no_weekday);
        } else if (!AppUtils.isReminderRangeValid(this, reminderRangeInput.getText().toString())) {

        } else {
            return true;
        }
        // If reminder range is not valid, no toast has to be shown. In that case, error msg will
        // be empty.
        if (errorMsg != null && !errorMsg.isEmpty()) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void saveTask() {
        if (!isInputValid()) {
            return;
        }
        String taskName = taskNameInput.getText().toString();
        String locationName = locationNameInput.getText().toString();
        int enteredReminderRange = Integer.parseInt(reminderRangeInput.getText().toString());
        int reminderRange = (int) DistanceUtils.getDistanceToSave(this, enteredReminderRange);
        boolean isAlarmEnabled = alarmSwitch.isChecked();
        String imagePath = null;
        String selectedImagePath = (String) taskImageView.getTag();
        if (selectedImagePath != null) {
            imagePath = selectedImagePath;
        }
        // There can be a case when user selects a time and then turns on the anytime switch.
        // So, we need to check the anytime switch first.
        LocalTime startTime, endTime;
        if (anytimeSwitch.isChecked()) {
            // Alarm can ring anytime. Therefore, we can set the times internally to be from
            // 00:00 to 23:59
            startTime = new LocalTime(0, 0);
            endTime = new LocalTime(23, 59);
        } else {
            // See what times are set on the textViews.
            startTime = (LocalTime) startTimeTv.getTag();
            endTime = (LocalTime) endTimeTv.getTag();
        }

        LocalDate startDate = (LocalDate) startDateTv.getTag();
        // end date will be stored as null only.
        LocalDate endDate = (LocalDate) endDateTv.getTag();

        String note = noteInput.getText().toString();
        if (TextUtils.isEmpty(note)) {
            note = null;
        }

        int repeatType = repeatSwitch.isChecked()
                ? DbConstants.REPEAT_DAILY : DbConstants.NO_REPEAT;
        int repeatCode = (int) weekdaysStub.getTag();

        long locationId;
        if (mSelectedLocation.getId() != 0
                && mSelectedLocation.getPlaceName().equals(locationName)) {
            // Location was selected from saved places and the name was not changed.
            // auto-increment numbering starts from 1.
            // We can also set place picker to return location with id = -1.
            locationId = mSelectedLocation.getId();
            // Since this location is already picked up from the database, we just need
            // to update the location use count.
            mSelectedLocation.setUseCount(mSelectedLocation.getUseCount() + 1);
            mTaskRepository.updateLocation(mSelectedLocation);
        } else {
            mSelectedLocation.setPlaceName(locationName);
            // Need to set this id because if it's a location chosen from saved places, it
            // already has an id that causes problems in inserting it again.
            mSelectedLocation.setId(0);
            // TODO: Check if place with same name already exists to improve UX.
            // Doing this when place picker gave the location. i.e. new location with use_count = 1.
            locationId = mTaskRepository.saveLocation(mSelectedLocation);
        }

        TaskModel task = new TaskModel.Builder(this, taskName, locationId)
                .setReminderRange(reminderRange)
                .setIsAlarmSet(isAlarmEnabled ? 1 : 0)
                .setImageUri(imagePath)
                .setNote(note)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setRepeatType(repeatType)
                .setRepeatCode(repeatCode)
                .build();

        if (taskBeingEdited == null) {
            // add new task.
            mTaskRepository.saveTask(task);
            logAnalytics(task);
        } else {
            // update task.
            task.setId(taskBeingEdited.getId());
            mTaskRepository.updateTask(task);
        }
        // Service is restarted to update tasks distance and accordingly trigger
        // alarm/notification at that instant.
        // TODO: This is not the optimized way. Change this later.
        restartService();
        finish();
    }

    /**
     * Restarts service.
     */
    private void restartService() {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAppEnabled = defaultPref.getString(getString(R.string.pref_status_key),
                getString(R.string.pref_status_default)).equals(getString(R.string
                .pref_status_enabled));
        if (isAppEnabled) {
            AppUtils.stopService(this);
            AppUtils.startService(this);
        }
    }


    /**
     * Log task creation events.
     */
    private void logAnalytics(TaskModel task) {
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.ANALYTICS_PARAM_START_TIME, task.getStartTime()
                .toString());
        bundle.putString(AnalyticsConstants.ANALYTICS_PARAM_END_TIME, task.getEndTime().toString());
        boolean isDeadlineSet = task.getEndDate() != null;
        bundle.putBoolean(AnalyticsConstants.ANALYTICS_PARAM_IS_DEADLINE_SET, isDeadlineSet);
        boolean isNoteAdded = task.getNote() != null;
        bundle.putBoolean(AnalyticsConstants.ANALYTICS_PARAM_IS_NOTE_ADDED, isNoteAdded);
        boolean isAnytimeOn = anytimeSwitch.isChecked();
        bundle.putBoolean(AnalyticsConstants.ANALYTICS_PARAM_IS_ANYTIME_SET, isAnytimeOn);
        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_SAVE_NEW_TASK, bundle);
    }

    /**
     * When we've a task that is being edited, we've to fill it's attributes into the input fields.
     *
     * @param task The task that is being edited.
     */
    private void fillDataForEditing(final TaskModel task) {
        taskNameInput.setText(task.getTaskName());
        // Set location
        mSelectedLocation = mTaskRepository.getLocationById(task.getLocationId());
        // Shows the location name and makes it visible.
        onLocationSelected();
        hasSelectedLocation = true;
        // Set reminder range
        reminderRangeInput.setText(String.valueOf(task.getReminderRange()));
        // Set note
        noteInput.setText(task.getNote());
        // Setup time.
        boolean anytime = task.getStartTime().equals(new LocalTime(0, 0))
                && task.getEndTime().equals(new LocalTime(23, 59));
        anytimeSwitch.setChecked(anytime);
        startTimeTv.setText(AppUtils.getReadableTime(this, task.getStartTime()));
        endTimeTv.setText(AppUtils.getReadableTime(this, task.getEndTime()));
        startTimeTv.setTag(task.getStartTime());
        endTimeTv.setTag(task.getEndTime());

        // Set date.
        startDateTv.setText(AppUtils.getReadableLocalDate(this, task.getStartDate()));
        endDateTv.setText(AppUtils.getReadableLocalDate(this, task.getEndDate()));
        startDateTv.setTag(task.getStartDate());
        endDateTv.setTag(task.getEndDate());

        // Repeat options.
        repeatSwitch.setChecked(task.getRepeatType() == DbConstants.REPEAT_DAILY);
        if (wds != null) {
            int repeatCode = task.getRepeatCode();
            weekdaysStub.setTag(repeatCode);
            // Get the day indices to repeat.
            ArrayList<Integer> dayIndices = WeekdayCodeUtils.getDayIndexListToRepeat(repeatCode);
            for (int day : dayIndices) {
                wds.setSelectedDays(day - 1);
            }
        }

        // Alarm switch
        alarmSwitch.setChecked(task.getIsAlarmSet() != 0);
        // Cover image
        if (task.getImageUri() != null) {
            taskImageView.setVisibility(View.VISIBLE);
            taskImageView.setImageURI(Uri.parse(task.getImageUri()));
            taskImageView.setTag(task.getImageUri());
        }
        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_EDIT_TASK, new Bundle());
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Setting in onStart so that when the upgrade activity closes, the lock layout refreshes
        // taking into account the purchase(if any).
        setPremiumLock();
    }

    private void setPremiumLock() {
        if (AppUtils.isPremiumUser(this)) {
            lockLayoutSchdule.setVisibility(View.GONE);
            lockLayoutAttachment.setVisibility(View.GONE);
            noteInput.setFocusable(true);
            noteInput.setFocusableInTouchMode(true);
        } else {
            lockLayoutSchdule.setVisibility(View.VISIBLE);
            lockLayoutAttachment.setVisibility(View.VISIBLE);
            noteInput.setFocusable(true);
            // The note input can still gain focus by clicking enter button on keyboard,
            // to avoid this, set it as not focusable.
            noteInput.setFocusable(false);
        }
    }


    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.LocationViewHolder> {

        private List<LocationModel> mLocations;

        public RecyclerAdapter(List<LocationModel> locationModels) {

            this.mLocations = locationModels;
        }

        @NonNull
        @Override
        public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
                viewType) {
            View v = getLayoutInflater().inflate(R.layout.list_item_location_chip, parent, false);
            return new LocationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LocationViewHolder holder, int
                position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return mLocations.size() + 1;
        }

        public class LocationViewHolder extends RecyclerView.ViewHolder implements View
                .OnClickListener {
            TextView textView;

            public LocationViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.text_view);
                textView.setOnClickListener(this);
            }

            public void bind(int position) {

                if (position == mLocations.size()) {
                    textView.setText("More");
                    textView.setTextColor(Color.BLUE);
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
//                    textView.setBackground(null);
                } else {
                    textView.setText(mLocations.get(position).getPlaceName());
                }
            }

            @Override
            public void onClick(View v) {
                int position = getLayoutPosition();
                if (v.getId() == R.id.text_view) {
                    if (position == mLocations.size()) {
                        Intent savedPlacesIntent = new Intent(TaskCreatorActivity.this,
                                SavedPlacesActivity.class);
                        startActivityForResult(savedPlacesIntent, REQUEST_CODE_LOCATION_SELECTION);
                    } else {
                        mSelectedLocation = mLocations.get(position);
                        hasSelectedLocation = true;
                        onLocationSelected();
                    }
                }
            }
        }
    }
}
