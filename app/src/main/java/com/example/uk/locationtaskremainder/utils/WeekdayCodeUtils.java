package com.example.uk.locationtaskremainder.utils;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Provides codes for weekdays for setting repeatable reminders' days.
 */
public class WeekdayCodeUtils {

    private static final int DAY_CODE_MONDAY = 1;
    private static final int DAY_CODE_TUESDAY = 2;
    private static final int DAY_CODE_WEDNESDAY = 4;
    private static final int DAY_CODE_THURSDAY = 8;
    private static final int DAY_CODE_FRIDAY = 16;
    private static final int DAY_CODE_SATURDAY = 32;
    private static final int DAY_CODE_SUNDAY = 64;

    /**
     * Used for getting day code by passing in the index of the day.
     */
    private static final int[] DAY_CODES_ARRAY = {
            DAY_CODE_MONDAY,
            DAY_CODE_TUESDAY,
            DAY_CODE_WEDNESDAY,
            DAY_CODE_THURSDAY,
            DAY_CODE_FRIDAY,
            DAY_CODE_SATURDAY,
            DAY_CODE_SUNDAY
    };

    /**
     * Returns dayCode for the day index passed to this function. Assumes MONDAY is the first day
     * of the week.
     */
    public static int getDayCodeByIndex(int index) {
        // MONDAY is = 1 in input. So, get the (index - 1)th item.
        return DAY_CODES_ARRAY[index - 1];
    }

    /**
     * Returns dayCode for the calendar day index passed to this function assuming SUNDAY as the
     * first day
     * of the week. (Just like Java's calendar API.)
     */
    public static int getDayCodeByCalendarDayId(int calendarDayId) {
        int ourDayIndex = 7;
        if (calendarDayId != Calendar.SUNDAY) {
            ourDayIndex = calendarDayId - 1;
        }
        return getDayCodeByIndex(ourDayIndex);
    }

    /**
     * Returns the list of indices of days when repeat code is eligible, assuming MONDAY as
     * first day.
     */
    public static ArrayList<Integer> getDayIndexListToRepeat(int repeatCode) {
        ArrayList<Integer> dayIndices = new ArrayList<>();
        // MONDAY is day = 1.
        for (int day = DateTimeConstants.MONDAY; day <= DateTimeConstants.SUNDAY; day++) {
            if ((repeatCode & getDayCodeByIndex(day)) != 0) {
                dayIndices.add(day);
            }
        }
        return dayIndices;
    }

    /**
     * Returns the weekday's name by getting the index. 1 index is for Monday.
     */
    public static String getWeekdayNameById(int index) {
        switch (index) {
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "";
        }
    }

}
