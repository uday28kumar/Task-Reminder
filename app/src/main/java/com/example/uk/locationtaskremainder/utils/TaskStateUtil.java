package com.example.uk.locationtaskremainder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.TaskStateWrapper;
import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;

/**
 * A utility class for task's state as {STATE_ACTIVE_SNOOZED, STATE_ACTIVE_NOT_SNOOZED,
 * STATE_UPCOMING, STATE_EXPIRED, STATE_DONE}.
 *
 * @author shilpi
 */

public class TaskStateUtil {

    // Defined task states.
    public static final int STATE_ACTIVE_SNOOZED = 0;
    public static final int STATE_ACTIVE_NOT_SNOOZED = 1;
    public static final int STATE_UPCOMING = 2;
    public static final int STATE_EXPIRED = 3;
    public static final int STATE_DONE = 4;

    @IntDef({STATE_ACTIVE_SNOOZED, STATE_ACTIVE_NOT_SNOOZED, STATE_UPCOMING, STATE_EXPIRED,
            STATE_DONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskState {
    }

    /**
     * Get the state of task passed as {STATE_ACTIVE_SNOOZED, STATE_ACTIVE_NOT_SNOOZED,
     * STATE_UPCOMING, STATE_EXPIRED, STATE_DONE}.
     */
    public static int getTaskState(Context context, TaskModel task) {

        // Check it's done status.
        if (task.getIsDone() == 1)
            return STATE_DONE;

        LocalDate today = LocalDate.fromDateFields(new Date());

        // NOTE: We need to compare dates by getting the DateOnlyInstance because Date also contains
        // information about the time and comparing them with .compare() also compares time.
        // This causes a reminder expiring today to be told as expired.

        // Check end date.
        LocalDate endDate = task.getEndDate();
        // If end date < today, retun expired. Else, proceed.
        if (endDate != null && endDate.compareTo(today) < 0) {
            return STATE_EXPIRED;
        }

        // Check task's start date.
        LocalDate nextStartDate = task.getNextStartDate();
        // If start date > today, return upcoming.Else, proceed.
        if (nextStartDate.compareTo(today) > 0)
            return STATE_UPCOMING;

        LocalTime currentTime = LocalTime.fromDateFields(new Date());
        // Check start time.
        LocalTime startTime = task.getStartTime();
        // If start time > current time, it is upcoming. Else proceed.
        if (startTime.compareTo(currentTime) > 0)
            return STATE_UPCOMING;

        // Check end Time.
        LocalTime endTime = task.getEndTime();
        // If end time is less than current time, it is expired. Else, proceed.
        if (endTime != null && endTime.compareTo(currentTime) < 0) {
            if (endDate != null && endDate.compareTo(today) == 0) {
                return STATE_EXPIRED;
            } else {
                return STATE_UPCOMING;
            }
        }

        if (task.getRepeatType() == DbConstants.REPEAT_DAILY
                && !isRepeatDailyEligible(task.getRepeatCode()))
            return STATE_UPCOMING;

        // Check if snoozed.
        // Get snooze time from settings.
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
        long snoozeTime = Long.parseLong(defaultPref.getString(context.getString(R.string
                .pref_snooze_time_key), context.getString(R.string.pref_snooze_time_default)));
        if (task.getSnoozedAt() != -1 && task.getSnoozedAt() + snoozeTime > System
                .currentTimeMillis())
            return STATE_ACTIVE_SNOOZED;

        return STATE_ACTIVE_NOT_SNOOZED;
    }

    /**
     * Returns an array list containing of task's list according to it's state.
     */
    public static ArrayList<List<TaskModel>> getTaskListState(Context context, List<TaskModel>
            tasks) {
        // Initialize Lists.
        ArrayList<List<TaskModel>> resultList = new ArrayList<>();
        List<TaskModel> activeNotSnoozedTasks = new ArrayList<>();
        List<TaskModel> activeSnoozedTasks = new ArrayList<>();
        List<TaskModel> upcomingTasks = new ArrayList<>();
        List<TaskModel> expiredTasks = new ArrayList<>();
        List<TaskModel> doneTasks = new ArrayList<>();

        for (TaskModel task : tasks) {
            int taskState = getTaskState(context, task);
            switch (taskState) {
                case STATE_ACTIVE_NOT_SNOOZED:
                    activeNotSnoozedTasks.add(task);
                    break;
                case STATE_ACTIVE_SNOOZED:
                    activeSnoozedTasks.add(task);
                    break;
                case STATE_UPCOMING:
                    upcomingTasks.add(task);
                    break;
                case STATE_EXPIRED:
                    expiredTasks.add(task);
                    break;
                case STATE_DONE:
                    doneTasks.add(task);
            }
        }
        resultList.add(activeSnoozedTasks);
        resultList.add(activeNotSnoozedTasks);
        resultList.add(upcomingTasks);
        resultList.add(expiredTasks);
        resultList.add(doneTasks);
        return resultList;
    }

    /**
     * It returns a sorted list of tasks wrapped with their state.
     */
    public static ArrayList<TaskStateWrapper> getTasksStateListWrapper(Context context,
            List<TaskModel> tasks) {

        // Get the state lists.
        ArrayList<List<TaskModel>> statesList = getTaskListState(context, tasks);
        // Create result list.
        ArrayList<TaskStateWrapper> resultList = new ArrayList<>();

        // Add all the active not snoozed tasks.
        for (TaskModel task : statesList.get(STATE_ACTIVE_NOT_SNOOZED)) {
            resultList.add(new TaskStateWrapper(task, STATE_ACTIVE_NOT_SNOOZED));
        }

        // Add all the active snoozed tasks.
        for (TaskModel task : statesList.get(STATE_ACTIVE_SNOOZED)) {
            resultList.add(new TaskStateWrapper(task, STATE_ACTIVE_SNOOZED));
        }

        // Sort the tasks in ascending order of their last distance.
        Collections.sort(resultList, new Comparator<TaskStateWrapper>() {
            @Override
            public int compare(TaskStateWrapper o1, TaskStateWrapper o2) {
                float o1Distance = o1.getTask().getLastDistance();
                float o2Distance = o2.getTask().getLastDistance();
                if (o1Distance < o2Distance)
                    return -1;
                else if (o1Distance > o2Distance)
                    return 1;
                return 0;
            }
        });

        // Sort upcoming tasks and add it to the result list.
        Collections.sort(statesList.get(STATE_UPCOMING), new DateAddedComparator());
        for (TaskModel task : statesList.get(STATE_UPCOMING)) {
            resultList.add(new TaskStateWrapper(task, STATE_UPCOMING));
        }

        // Sort expired tasks and add it to the result list.
        Collections.sort(statesList.get(STATE_EXPIRED), new DateAddedComparator());
        for (TaskModel task : statesList.get(STATE_EXPIRED)) {
            resultList.add(new TaskStateWrapper(task, STATE_EXPIRED));
        }

        // Sort done tasks and add it to the result list.
        Collections.sort(statesList.get(STATE_DONE), new DateAddedComparator());
        for (TaskModel task : statesList.get(STATE_DONE)) {
            resultList.add(new TaskStateWrapper(task, STATE_DONE));
        }

        return resultList;
    }

    public static class DateAddedComparator implements Comparator<TaskModel> {
        @Override
        public int compare(TaskModel o1, TaskModel o2) {
            return o2.getDateAdded().compareTo(o1.getDateAdded());
        }
    }

    /**
     * Returns the name for a taskState constant passed to this function.
     */
    public static String stateToString(Context context, @TaskState int taskState) {
        String[] taskStateNames = context.getResources().getStringArray(R.array.task_states);
        // Handle invalid case.
        if (taskState > taskStateNames.length) {
            taskState = taskStateNames.length - 1;
        }
        return taskStateNames[taskState];
    }

    private static boolean isRepeatDailyEligible(int repeatCode) {
        // Assumes dayOfWeek(Monday) = 1.
        LocalDate today = LocalDate.fromDateFields(new Date());
        int dayCode = WeekdayCodeUtils.getDayCodeByIndex(today.getDayOfWeek());
        return ((repeatCode & dayCode) != 0);
    }
}
