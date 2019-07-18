package com.example.uk.locationtaskremainder.database.converters;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.LocalDate;

import java.util.Date;

/**
 * Converts Date to/from Long
 *
 * @author shilpi
 */


public class DateConverter {
    @TypeConverter
    public String dateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public LocalDate stringToDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
