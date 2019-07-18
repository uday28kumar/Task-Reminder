package com.example.uk.locationtaskremainder;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.database.AppDatabase;
import app.tasknearby.yashcreations.com.tasknearby.models.LocationModel;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;

/**
 * Handles all data operations
 *
 * @author shilpi on 27/12/17.
 */

public class TaskRepository {

    private static final String TAG = TaskRepository.class.getSimpleName();

    /**
     * AppDatabase instance.
     */
    private AppDatabase mDatabase;

    /**
     * Constructor for getting the instance of AppDatabase using context.
     *
     * @param context using which AppDatabase will be retrieved.
     */
    public TaskRepository(Context context) {
        mDatabase = AppDatabase.getAppDatabase(context);
    }

    /**
     * Fetches all the tasks from database.
     */
    public List<TaskModel> getAllTasks() {
        return mDatabase.taskDao().getAllTasks();
    }

    /**
     * Fetches the task with the given id.
     */
    public TaskModel getTaskWithId(long taskId) {
        return mDatabase.taskDao().getTaskWithId(taskId);
    }

    /**
     * Saves the task to the database.
     *
     * @return the id of the saved task.
     */
    public long saveTask(TaskModel task) {
        return mDatabase.taskDao().insertTask(task);
    }

    /**
     * Updates the task with taskId of the task passed as param.
     */
    public void updateTask(TaskModel task) {
        mDatabase.taskDao().updateTask(task);
    }

    /**
     * Deletes the task from the database.
     */
    public void removeTask(TaskModel task) {
        mDatabase.taskDao().deleteTask(task);
    }

    /**
     * Returns a location object with the given id from the database.
     */
    public LocationModel getLocationById(long locationId) {
        return mDatabase.locationDao().getLocationWithId(locationId);
    }

    /**
     * Returns all locations present in the database.
     */
    public List<LocationModel> getAllLocations() {
        return mDatabase.locationDao().getAllLocations();
    }

    /**
     * Saves the location to the database.
     */
    public long saveLocation(LocationModel locationModel) {
        return mDatabase.locationDao().insertLocation(locationModel);
    }

    /**
     * Updates the location from the database.
     */
    public void updateLocation(LocationModel locationModel) {
        mDatabase.locationDao().updateLocation(locationModel);
    }

    /**
     * Query to fetch the tasks not marked as done and active for today.
     */
    public List<TaskModel> getNotDoneTasksForToday() {
        LocalDate today = LocalDate.fromDateFields(new Date());
        return mDatabase.taskDao().getNotDoneTasksForToday(today.toString());
    }

    public LiveData<List<TaskModel>> getAllTasksWithUpdates() {
        return mDatabase.taskDao().getAllTasksWithUpdates();
    }

    /**
     * Batch update.
     */
    public void updateTasks(List<TaskModel> tasks) {
        mDatabase.taskDao().updateTasks(tasks);
    }

}
