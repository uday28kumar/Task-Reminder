package com.example.uk.locationtaskremainder.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;

/**
 * @author shilpi
 */
@Dao
public interface TaskDao {

    @Insert
    long insertTask(TaskModel task);

    @Insert
    List<Long> insertTasks(List<TaskModel> tasks);

    @Update
    void updateTask(TaskModel task);

    @Delete
    void deleteTask(TaskModel task);

    @Query("SELECT * FROM tasks")
    List<TaskModel> getAllTasks();

    @Query("SELECT * FROM  tasks WHERE id = :taskId")
    TaskModel getTaskWithId(long taskId);

    /**
     * Query to fetch the tasks not marked as done and active for today.
     */
    @Query("SELECT * FROM tasks WHERE is_done = 0 AND (end_date IS NULL OR end_date >= date(:today)) " +
            "AND start_date <= date(:today)")
    List<TaskModel> getNotDoneTasksForToday(String today);

    @Query("SELECT * FROM tasks")
    LiveData<List<TaskModel>> getAllTasksWithUpdates();

    @Update
    void updateTasks(List<TaskModel> tasks);
}
