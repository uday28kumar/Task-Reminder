package com.example.uk.locationtaskremainder.database.converters;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.LocalTime;


/**
 * Converts LocalTime to/from String
 *
 * @author shilpi
 */

public class TimeConverter {

    @TypeConverter
    public LocalTime stringToLocalTime(String value) {
        return value == null ? null : LocalTime.parse(value);
    }

    @TypeConverter
    public String localTimeToString(LocalTime time) {
        return time == null ? null : time.toString();
    }
}
