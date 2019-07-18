package com.example.uk.locationtaskremainder.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.database.converters.DateConverter;
import app.tasknearby.yashcreations.com.tasknearby.database.converters.TimeConverter;

/**
 * Models the 'Task' object. Each tasks has the attributes present in this class. It serves as an
 * entity that will be stored by {@link android.arch.persistence.room.Room} into the SQLite
 * database.
 *
 * @author shilpi
 */
@Entity(tableName = "tasks",
        foreignKeys = {
                @ForeignKey(entity = LocationModel.class,
                        parentColumns = "id",
                        childColumns = "location_id")
        })
@TypeConverters({DateConverter.class, TimeConverter.class})
public class TaskModel {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "task_name")
    private String taskName;

    @ColumnInfo(name = "location_id")
    private long locationId;

    @ColumnInfo(name = "image_uri")
    private String imageUri;

    @ColumnInfo(name = "is_done")
    private int isDone;

    @ColumnInfo(name = "is_alarm_set")
    private int isAlarmSet;

    @ColumnInfo(name = "reminder_range")
    private int reminderRange;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "start_time")
    private LocalTime startTime;

    @ColumnInfo(name = "end_time")
    private LocalTime endTime;

    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    @ColumnInfo(name = "next_start_date")
    private LocalDate nextStartDate;

    /**
     * Repeat type as NO REPEAT(0), REPEAT_DAILY(1), REPEAT_WEEKLY(2), REPEAT_MONTHLY(3).
     */
    @ColumnInfo(name = "repeat_type")
    private int repeatType;

    /**
     * Movement Type as BOTH ENTER AND EXIT(0), ENTER(1), EXIT(2).
     */
    @ColumnInfo(name = "movement_type")
    private int movementType;

    /**
     * Activity Type as ACTIVITY_ANYTHING(0), WALKING(1), DRIVING(2).
     */
    @ColumnInfo(name = "activity_type")
    private int activityType;

    @ColumnInfo(name = "last_distance")
    private float lastDistance;

    @ColumnInfo(name = "last_triggered")
    private LocalDate lastTriggered;

    @ColumnInfo(name = "snoozed_at")
    private Long snoozedAt;

    @ColumnInfo(name = "date_added")
    private LocalDate dateAdded;

    /**
     * Repeat code according to the repeat type. The code is to reflect daily repeat pattern for
     * now.
     */
    @ColumnInfo(name = "repeat_code")
    private int repeatCode;


    public TaskModel() {
    }

    @Ignore
    private TaskModel(String taskName, long locationId, String imageUri, int isDone,
            int isAlarmSet, int reminderRange, String note, LocalTime startTime,
            LocalTime endTime, LocalDate startDate, LocalDate endDate, LocalDate nextStartDate,
            int repeatType, int repeatCode, int movementType, int activityType, float lastDistance,
            LocalDate lastTriggered, Long snoozedAt, LocalDate dateAdded) {
        this.taskName = taskName;
        this.locationId = locationId;
        this.imageUri = imageUri;
        this.isDone = isDone;
        this.isAlarmSet = isAlarmSet;
        this.reminderRange = reminderRange;
        this.note = note;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextStartDate = nextStartDate;
        this.repeatType = repeatType;
        this.repeatCode = repeatCode;
        this.movementType = movementType;
        this.activityType = activityType;
        this.lastDistance = lastDistance;
        this.lastTriggered = lastTriggered;
        this.snoozedAt = snoozedAt;
        this.dateAdded = dateAdded;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    @Nullable
    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(@Nullable String imageUri) {
        this.imageUri = imageUri;
    }

    public int getIsDone() {
        return isDone;
    }

    public void setIsDone(int isDone) {
        this.isDone = isDone;
    }

    public int getIsAlarmSet() {
        return isAlarmSet;
    }

    public void setIsAlarmSet(int isAlarmSet) {
        this.isAlarmSet = isAlarmSet;
    }

    public int getReminderRange() {
        return reminderRange;
    }

    public void setReminderRange(int reminderRange) {
        this.reminderRange = reminderRange;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @NonNull
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(@NonNull LocalTime startTime) {
        this.startTime = startTime;
    }

    @NonNull
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(@NonNull LocalTime endTime) {
        this.endTime = endTime;
    }

    @NonNull
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NonNull LocalDate startDate) {
        this.startDate = startDate;
    }

    @Nullable
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(@Nullable LocalDate endDate) {
        this.endDate = endDate;
    }

    @NonNull
    public LocalDate getNextStartDate() {
        return nextStartDate;
    }

    public void setNextStartDate(@NonNull LocalDate nextStartDate) {
        this.nextStartDate = nextStartDate;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(@DbConstants.RepeatTypes int repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatCode() {
        return this.repeatCode;
    }

    public void setRepeatCode(@NonNull int repeatCode) {
        this.repeatCode = repeatCode;
    }

    public int getMovementType() {
        return movementType;
    }

    public void setMovementType(@DbConstants.MovementTypes int movementType) {
        this.movementType = movementType;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(@DbConstants.ActivityModes int activityType) {
        this.activityType = activityType;
    }

    public float getLastDistance() {
        return lastDistance;
    }

    public void setLastDistance(float lastDistance) {
        this.lastDistance = lastDistance;
    }

    public LocalDate getLastTriggered() {
        return lastTriggered;
    }

    public void setLastTriggered(LocalDate lastTriggered) {
        this.lastTriggered = lastTriggered;
    }

    public Long getSnoozedAt() {
        return snoozedAt;
    }

    public void setSnoozedAt(Long snoozedAt) {
        this.snoozedAt = snoozedAt;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(@NonNull LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    /**
     * Builder class for Task object. TaskName and locationId are the compulsory arguments. Others
     * have a default value.
     */
    public static class Builder {

        private String taskName;
        private long locationId;
        // Assigning default values to the remaining.
        private String imageUri = null;
        private int isDone = 0;
        private int isAlarmSet = 1;
        private int reminderRange;
        private String note = null;
        private LocalTime startTime = new LocalTime(0, 0); // 00:00
        private LocalTime endTime = new LocalTime(23, 59); // 23:59
        private LocalDate startDate = new LocalDate();
        private LocalDate endDate = null;
        /**
         * If we set nextStartDate here, then we'll have to update it whenever we update startDate.
         * Setting it in the setStartDate() method will overwrite it if it has already been set
         * by the setNextStartDate() method. Therefore, we're initially setting it as null so
         * that we can set it equal to startDate if it's still null at the time of building.
         */
        private LocalDate nextStartDate = null;
        private int repeatType = DbConstants.NO_REPEAT;
        private int repeatCode = DbConstants.REPEAT_CODE_NONE;
        private int movementType = DbConstants.BOTH_ENTER_EXIT;
        private int activityType = DbConstants.ACTIVITY_ANYTHING;
        private float lastDistance = Integer.MAX_VALUE;
        private LocalDate lastTriggered = null;
        private long snoozedAt = -1L;
        private LocalDate dateAdded = new LocalDate();

        /**
         * Instantiates the builder object.
         */
        public Builder(@NonNull Context context, @NonNull String taskName, long locationId) {
            this.taskName = taskName;
            this.locationId = locationId;
            setReminderRangeFromSettings(context);
        }

        /**
         * Used for setting the default value of the reminder range.
         */
        private void setReminderRangeFromSettings(Context context) {
            // Setting reminder range from settings (shared preferences).
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            reminderRange = Integer.parseInt(prefs.getString(context.getString(R.string
                            .pref_distance_range_key),
                    context.getString(R.string.pref_distance_range_default)));
        }

        public Builder setTaskName(@NonNull String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder setLocationId(long locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder setImageUri(String imageUri) {
            this.imageUri = imageUri;
            return this;
        }

        public Builder setIsDone(int isDone) {
            this.isDone = isDone;
            return this;
        }

        public Builder setIsAlarmSet(int isAlarmSet) {
            this.isAlarmSet = isAlarmSet;
            return this;
        }

        public Builder setReminderRange(int reminderRange) {
            this.reminderRange = reminderRange;
            return this;
        }

        public Builder setNote(@Nullable String note) {
            this.note = note;
            return this;
        }

        public Builder setStartTime(@NonNull LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(@NonNull LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder setStartDate(@NonNull LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder setNextStartDate(@NonNull LocalDate nextStartDate) {
            this.nextStartDate = nextStartDate;
            return this;
        }

        public Builder setRepeatType(@DbConstants.RepeatTypes int repeatType) {
            this.repeatType = repeatType;
            return this;
        }

        public Builder setRepeatCode(int repeatCode) {
            this.repeatCode = repeatCode;
            return this;
        }

        public Builder setMovementType(@DbConstants.MovementTypes int movementType) {
            this.movementType = movementType;
            return this;
        }

        public Builder setActivityType(@DbConstants.ActivityModes int activityType) {
            this.activityType = activityType;
            return this;
        }

        public Builder setLastDistance(float lastDistance) {
            this.lastDistance = lastDistance;
            return this;
        }

        public Builder setLastTriggered(@Nullable LocalDate lastTriggered) {
            this.lastTriggered = lastTriggered;
            return this;
        }

        public Builder setSnoozedAt(long snoozedAt) {
            this.snoozedAt = snoozedAt;
            return this;
        }

        public Builder setDateAdded(LocalDate dateAdded) {
            this.dateAdded = dateAdded;
            return this;
        }

        /**
         * Builds and returns the Task object with the required parameters.
         */
        public TaskModel build() {
            nextStartDate = (nextStartDate == null) ? startDate : nextStartDate;
            // call the private constructor.
            return new TaskModel(taskName, locationId, imageUri, isDone, isAlarmSet,
                    reminderRange, note,
                    startTime, endTime, startDate, endDate, nextStartDate, repeatType,
                    repeatCode, movementType, activityType, lastDistance, lastTriggered,
                    snoozedAt, dateAdded);
        }

    }

}
