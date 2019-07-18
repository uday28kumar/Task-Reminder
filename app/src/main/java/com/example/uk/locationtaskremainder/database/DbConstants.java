package com.example.uk.locationtaskremainder.database;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stores all the database related constants
 *
 * @author shilpi
 */

public class DbConstants {

    /**
     * Name of app database created.
     */
    public static final String APP_DATABASE_NAME = "task_database.db";

    /**
     * Activity based alarm constants.
     */
    public static final int ACTIVITY_ANYTHING = 0;
    public static final int WALKING = 1;
    public static final int DRIVING = 2;

    /**
     * Movement type constants
     */
    public static final int BOTH_ENTER_EXIT = 0;
    public static final int ENTER = 1;
    public static final int EXIT = 2;

    /**
     * Repeat type constants
     */
    public static final int NO_REPEAT = 0;
    public static final int REPEAT_DAILY = 1;
    public static final int REPEAT_WEEKLY = 2;
    public static final int REPEAT_MONTHLY = 3;

    /**
     * Repeat days codes.
     * The index for the days are chosen as per the {@link java.time.DayOfWeek} enum in Java 8
     * and Joda Time's {@link org.joda.time.DateTimeConstants} class. Hence, MONDAY is the first
     * day of the week with index = 1. Please note that these are different from Calendar API
     * which has SUNDAY as the first day.
     */
    public static final int REPEAT_CODE_NONE = 0;

    /**
     * Annotation for Activity Modes.
     */
    @IntDef({ACTIVITY_ANYTHING, WALKING, DRIVING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActivityModes {
    }

    /**
     * Annotation for Movement Types.
     */
    @IntDef({BOTH_ENTER_EXIT, ENTER, EXIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MovementTypes {
    }

    /**
     * Annotation for Repeat Types.
     */
    @IntDef({NO_REPEAT, REPEAT_DAILY, REPEAT_WEEKLY, REPEAT_MONTHLY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatTypes {
    }

}
