package com.example.uk.locationtaskremainder.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import org.joda.time.LocalDate;

import java.util.Date;

import app.tasknearby.yashcreations.com.tasknearby.database.converters.DateConverter;


/**
 * @author shilpi
 */

@Entity(tableName = "locations")
@TypeConverters({DateConverter.class})
public class LocationModel {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "place_name")
    private String placeName;

    private double latitude;

    private double longitude;

    @ColumnInfo(name = "use_count")
    private int useCount;

    @ColumnInfo(name = "is_hidden")
    private int isHidden;

    @ColumnInfo(name = "date_added")
    private LocalDate dateAdded;

    @Ignore
    public LocationModel() {

    }

    public LocationModel(String placeName, double latitude, double longitude, int useCount, int isHidden, LocalDate dateAdded) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.useCount = useCount;
        this.isHidden = isHidden;
        this.dateAdded = dateAdded;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public int getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(int isHidden) {
        this.isHidden = isHidden;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }
}
