package com.example.uk.locationtaskremainder.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Date;

import app.tasknearby.yashcreations.com.tasknearby.TaskRepository;
import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.notification.NotificationHelper;

/**
 * Contains actions performed commonly on tasks. Both AlarmActivity and Notifications will be
 * using this.
 *
 * @author vernmyash8
 */
public final class TaskActionUtils {

    public static void onTaskMarkedDone(@NonNull Context appContext, TaskModel task) {
        task.setLastTriggered(LocalDate.fromDateFields(new Date()));
        if (task.getRepeatType() == DbConstants.REPEAT_DAILY) {
            LocalDate today = LocalDate.fromDateFields(new Date());
            // If it's a repeatable reminder we've to check that it's last date is not today. If it
            // is, then it won't be upcoming anymore and hence should be marked as done.
            if (task.getEndDate() != null && task.getEndDate().compareTo(today) <= 0) {
                task.setIsDone(1);
            } else {
                // If the reminder is done only for today, we'll increment the nextStartDate only.
                task.setNextStartDate(today.plusDays(1));
            }
        } else {
            // Mark it as done for non-repeatable reminders.
            task.setIsDone(1);
        }
        TaskRepository repository = new TaskRepository(appContext);
        repository.updateTask(task);
    }

    public static void onTaskSnoozed(@NonNull Context appContext, TaskModel task) {
        task.setSnoozedAt(System.currentTimeMillis());
        task.setLastTriggered(new LocalDate());
        TaskRepository repository = new TaskRepository(appContext);
        repository.updateTask(task);
    }

    public static void setAsNotificationOnly(Context appContext, TaskModel task) {
        task.setIsAlarmSet(0);
        new NotificationHelper(appContext).showReminderNotification(task);
        TaskRepository repository = new TaskRepository(appContext);
        repository.updateTask(task);
    }
}
