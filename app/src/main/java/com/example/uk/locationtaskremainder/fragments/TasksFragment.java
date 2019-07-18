package com.example.uk.locationtaskremainder.fragments;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.TaskAdapter;
import app.tasknearby.yashcreations.com.tasknearby.TaskRepository;
import app.tasknearby.yashcreations.com.tasknearby.TaskStateWrapper;
import app.tasknearby.yashcreations.com.tasknearby.models.TaskModel;
import app.tasknearby.yashcreations.com.tasknearby.utils.TaskStateUtil;

/**
 * Fragment to display a list of tasks present in the database.
 *
 * @author vermayash8
 */
public class TasksFragment extends Fragment {

    private static final String TAG = TasksFragment.class.getSimpleName();

    private TaskRepository mTaskRepository;
    private TaskAdapter mTaskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Set adapter to recycler view.
        mTaskAdapter = new TaskAdapter(getActivity());
        recyclerView.setAdapter(mTaskAdapter);

        mTaskRepository = new TaskRepository(getActivity().getApplicationContext());
        // Fetch the live data object.
        LiveData<List<TaskModel>> liveData = mTaskRepository.getAllTasksWithUpdates();
        // observe the live data for changes.
        liveData.observe(this, taskModels -> {
            if (taskModels == null) {
                return;
            }
            processTaskModels(taskModels);
        });
        return rootView;
    }

    /**
     * Does the following things:
     * 1. Assigns state to all the tasks.
     * 2. Gets the locations for all the tasks. (Ideally this should be done on WorkerThread)
     * 3. Notifies the UI that data has changed.
     */
    private void processTaskModels(@NonNull List<TaskModel> taskModels) {
        List<TaskStateWrapper> stateWrappedTasks = TaskStateUtil.getTasksStateListWrapper(
                getActivity(), taskModels);
        // Add location to the tasks.
        addLocation(stateWrappedTasks);
        // Add to adapter and notify.
        mTaskAdapter.setData(stateWrappedTasks);
        mTaskAdapter.notifyDataSetChanged();
        // Set the no task view.
        setNoTasksView(mTaskAdapter.getItemCount());
    }

    /**
     * Assigns the locations to the list of TaskStateWrapper objects.
     */
    private void addLocation(List<TaskStateWrapper> stateWrappedTasks) {
        long locationId;
        for (TaskStateWrapper wrapper : stateWrappedTasks) {
            locationId = wrapper.getTask().getLocationId();
            wrapper.setLocationName(mTaskRepository.getLocationById(locationId).getPlaceName());
        }
    }

    /**
     * When no task is present, this shows the empty view.
     *
     * @param itemCount the number of tasks present in the database.
     */
    private void setNoTasksView(int itemCount) {
        if (getActivity() != null) {
            if (itemCount == 0) {
                getActivity().findViewById(R.id.no_task_view).setVisibility(View.VISIBLE);
            } else {
                getActivity().findViewById(R.id.no_task_view).setVisibility(View.GONE);
            }
        }
    }
}
