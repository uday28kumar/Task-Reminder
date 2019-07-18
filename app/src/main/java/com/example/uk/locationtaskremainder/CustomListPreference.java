package com.example.uk.locationtaskremainder;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;

/**
 * Custom Preference for Snooze selection which inhibits snooze time change on non-premium version.
 *
 * @author shilpi
 */
public class CustomListPreference extends ListPreference {

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(final ViewGroup parent) {
        final View view = super.onCreateView(parent);
        view.setOnClickListener(v -> {
            if (!AppUtils.isPremiumUser(getContext())) {
                UpgradeActivity.show(getContext());
            } else {
                CustomListPreference.super.onClick();
            }
        });
        return view;
    }
}
