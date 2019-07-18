package com.example.uk.locationtaskremainder.utils.firebase;

/**
 * @author shilpi
 */

public class AnalyticsConstants {

    /**
     * Constants for alarms.
     */
    public static final String ANALYTICS_ALARM_RING = "alarm_ring";
    public static final String ANALYTICS_ALARM_MARK_DONE = "alarm_mark_done";
    public static final String ANALYTICS_ALARM_SNOOZE = "alarm_snooze";
    public static final String ANALYTICS_ALARM_TO_NOTIFICATION = "alarm_to_notification";
    public static final String ANALYTICS_ALARM_SHOW_MAP = "alarm_show_map";

    /**
     * Constants for notification alarm.
     */
    public static final String ANALYTICS_NOTIFICATION_SHOWN = "notification_shown";
    public static final String ANALYTICS_NOTIFICATION_MARK_DONE = "notification_mark_done";
    public static final String ANALYTICS_NOTIFICATION_SNOOZE = "notification_snooze";
    public static final String NOTIFICATION_DISCOUNT_SHOWN = "notification_discount_shown";
    public static final String NOTIFICATION_DISCOUNT_CLICK = "notification_discount_click";

    /**
     * Constants for navigation button from detail activity.
     */
    public static final String ANALYTICS_SHOW_MAP_FROM_DETAIL = "show_map_from_detail";

    /**
     * Constants for logging task creation events.
     */
    public static final String ANALYTICS_SAVE_NEW_TASK = "save_new_task";
    public static final String ANALYTICS_PARAM_START_TIME = "task_start_time";
    public static final String ANALYTICS_PARAM_END_TIME = "task_end_time";
    public static final String ANALYTICS_PARAM_IS_DEADLINE_SET = "is_deadline_set";
    public static final String ANALYTICS_PARAM_IS_NOTE_ADDED = "is_note_added";
    public static final String ANALYTICS_PARAM_IS_ANYTIME_SET = "is_anytime_set";

    public static final String ANALYTICS_EDIT_TASK = "task_edit";
    public static final String ANALYTICS_ADD_IMAGE = "add_image_button";

    public static final String PLACE_PICKER_EXCEPTION = "place_picker_exception";
    public static final String PLACE_PICKER_FATAL = "place_picker_fatal";

    /**
     * Constants for app start.
     */
    public static final String ANALYTICS_APP_START = "app_start";
    public static final String ANALYTICS_APP_ENABLED = "app_enabled";
    public static final String ANALYTICS_APP_DISABLED = "app_disabled";

    public static final String ANALYTICS_PARAM_IS_POWER_SAVER_ON = "is_power_saver_on";
    /**
     * Premium upgrade dialog/activity constants.
     */
    public static final String PREMIUM_DIALOG_REQUESTED_BY_BUTTON = "premium_lock_layout_clicked";
    public static final String PREMIUM_DIALOG_SHOWN = "premium_dialog_shown";
    public static final String PREMIUM_DIALOG_BUTTON_CLICK = "premium_dialog_purchase_clicked";

    public static final String PREMIUM_DIALOG_USER_CLICKED_BUY = "premium_dialog_user_clicked_buy";
    public static final String NOT_PURCHASED_BUT_PREMIUM = "not_purchased_but_premium";
    public static final String EXTRA_ORDER_ID = "order_id";

    public static final String EXTRA_PURCHASE_TOKEN = "purchase_token";

    /**
     * Show Image Activity
     */
    public static final String ANALYTICS_SHOW_TASK_IMAGE = "show_task_image";

    /**
     * Power saver settings click.
     */
    public static final String POWER_SAVER_TURN_ON = "power_saver_turn_on";
    public static final String POWER_SAVER_TURN_OFF = "power_saver_turn_off";
}
