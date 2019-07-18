package com.example.uk.locationtaskremainder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.Purchase;
import com.google.firebase.analytics.FirebaseAnalytics;

import app.tasknearby.yashcreations.com.tasknearby.billing.BillingManager;
import app.tasknearby.yashcreations.com.tasknearby.billing.ProductIdConstants;
import app.tasknearby.yashcreations.com.tasknearby.notification.NotificationConstants;
import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

/**
 * Shows a list of premium features and provides an option to purchase them.
 *
 * @author vermayash8
 */
public class UpgradeActivity extends AppCompatActivity implements BillingManager
        .BillingUpdatesListener {

    private static final String TAG = UpgradeActivity.class.getSimpleName();

    private String[] mItemNames;

    private final int[] mItemIcons = {
            // These are in order as mentioned in the itemNames array.
            R.drawable.ic_pro_add_image,
            R.drawable.ic_pro_note,
            R.drawable.ic_pro_time_range,
            R.drawable.ic_pro_date_interval,
            R.drawable.ic_pro_snooze,
            R.drawable.ic_pro_voice_alerts,
            R.drawable.ic_pro_repeat_40dp,
            R.drawable.ic_pro_route_generate
    };

    private BillingManager mBillingManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);

        mBillingManager = new BillingManager(this, this);

        mItemNames = getResources().getStringArray(R.array.upgrade_premium_items);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new PremiumAdapter(this));
        listView.setDividerHeight(0);

        findViewById(R.id.button_cross).setOnClickListener(v -> finish());
        findViewById(R.id.button_purchase).setOnClickListener(v -> {
            // This analytic will let us know if the users are dropping off on seeing the price.
            analytics.logEvent(AnalyticsConstants.PREMIUM_DIALOG_USER_CLICKED_BUY, new Bundle());
            purchasePremium();
        });

        analytics.logEvent(AnalyticsConstants.PREMIUM_DIALOG_SHOWN, new Bundle());
        if (getIntent().hasExtra(NotificationConstants.EXTRA_DISCOUNT_NOTIFICATION)) {
            analytics.logEvent(AnalyticsConstants.NOTIFICATION_DISCOUNT_CLICK, new Bundle());
        }
    }

    /**
     * Allows us to start this activity just by calling <code> UpgradeActivity.show(); </code>
     */
    public static void show(Context context) {
        context.startActivity(new Intent(context, UpgradeActivity.class));
    }

    private void purchasePremium() {
        if (mBillingManager.isConnectedToService()) {
            mBillingManager.initiatePurchaseFlow(ProductIdConstants.PREMIUM_PRODUCT_ID);
        } else {
            Log.w(TAG, "Purchase button clicked, but BillingManager is not connected.");
        }
    }

    @Override
    public void onBillingClientSetupFinished() {
        // Do nothing.
    }

    @Override
    public void onItemPurchased(Purchase purchase) {
        if (purchase.getSku().equals(ProductIdConstants.PREMIUM_PRODUCT_ID)) {
            AppUtils.setPremium(this, true);
            AppUtils.savePurchaseDetails(this, purchase);
            Toast.makeText(this, R.string.msg_thanks_for_buying, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Adapter class for setting the premium items list.
     */
    private class PremiumAdapter extends ArrayAdapter<String> {

        PremiumAdapter(@NonNull Context context) {
            super(context, R.layout.list_item_premium_items, mItemNames);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.list_item_premium_items, parent, false);
            }
            TextView itemNameTv = v.findViewById(R.id.text_item_name);
            ImageView imageView = v.findViewById(R.id.icon_image);
            itemNameTv.setText(mItemNames[position]);
            imageView.setImageResource(mItemIcons[position]);
            return v;
        }
    }
}
