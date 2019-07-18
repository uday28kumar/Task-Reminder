package com.example.uk.locationtaskremainder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.database.DbConstants;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.DistanceUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskStateUtil;

/**
 * Adapter for tasks.
 *
 * @author vermayash8
 */
public final class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.CustomViewHolder> {

    private static final String TAG = TaskAdapter.class.getSimpleName();

    private Activity activity;
    private List<TaskStateWrapper> mTaskStateWrappers;

    public TaskAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setData(List<TaskStateWrapper> wrappers) {
        mTaskStateWrappers = wrappers;
        notifyDataSetChanged();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.list_item_task, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        holder.bindView(mTaskStateWrappers.get(position));
    }

    @Override
    public int getItemCount() {
        if (mTaskStateWrappers == null) {
            return 0;
        }
        return mTaskStateWrappers.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView taskNameTv;
        private TextView locationNameTv;
        private TextView lastDistanceTv;
        private TextView stateTv;
        private ImageView repeatableIconView;

        CustomViewHolder(View v) {
            super(v);
            taskNameTv = v.findViewById(R.id.text_task_name);
            locationNameTv = v.findViewById(R.id.text_location_name);
            lastDistanceTv = v.findViewById(R.id.text_last_distance);
            stateTv = v.findViewById(R.id.text_state);
            repeatableIconView = v.findViewById(R.id.icon_repeatable);
            v.setOnClickListener(v1 -> {
                // Get the view position.
                int pos = getLayoutPosition();
                // Get the extra parameter.
                long taskId = mTaskStateWrappers.get(pos).getTask().getId();
                int state = mTaskStateWrappers.get(pos).getState();
                // Start Detail activity
                activity.startActivity(DetailActivity.getStartingIntent(activity, taskId, state));
            });
        }

        void bindView(TaskStateWrapper listItem) {
            TaskModel task = listItem.getTask();
            taskNameTv.setText(task.getTaskName());
            locationNameTv.setText(listItem.getLocationName());
            if (listItem.getState() == TaskStateUtil.STATE_ACTIVE_NOT_SNOOZED
                    && task.getLastDistance() != Integer.MAX_VALUE) {
                // Show last distance only when task is active and last distance has been
                // calculated. If it's active but last distance isn't present, badge is shown.
                bindLastDistance(task.getLastDistance());
            } else {
                bindTaskState(listItem);
            }
            if (task.getRepeatType() == DbConstants.NO_REPEAT) {
                repeatableIconView.setVisibility(View.GONE);
            } else {
                repeatableIconView.setVisibility(View.VISIBLE);
            }
        }

        private void bindLastDistance(float lastDistance) {
            stateTv.setVisibility(View.GONE);
            lastDistanceTv.setVisibility(View.VISIBLE);
            // Set the last Distance.
            lastDistanceTv.setText(
                    DistanceUtils.getFormattedDistanceString(activity, lastDistance));
        }

        private void bindTaskState(TaskStateWrapper listItem) {
            stateTv.setVisibility(View.VISIBLE);
            lastDistanceTv.setVisibility(View.GONE);
            stateTv.setText(TaskStateUtil.stateToString(activity, listItem.getState()));
        }
    }
}
