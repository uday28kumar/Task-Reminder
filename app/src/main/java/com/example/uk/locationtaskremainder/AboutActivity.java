package com.example.uk.locationtaskremainder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;

/**
 * Displays the about screen for the app.
 *
 * @author shilpi
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView feedbackTv;
    private TextView versionTv;
    private FloatingActionButton rateFab;
    private TextView privacyPolicyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setActionBar();
        initiateViews();
    }

    public void setActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_about));
    }

    public void initiateViews() {
        feedbackTv = findViewById(R.id.text_feedback);
        rateFab = findViewById(R.id.fab_rate);
        versionTv = findViewById(R.id.text_app_version);
        privacyPolicyTv = findViewById(R.id.text_privacy_policy);

        // Set on click listeners.
        feedbackTv.setOnClickListener(this);
        rateFab.setOnClickListener(this);
        privacyPolicyTv.setOnClickListener(this);
        versionTv.setText("v" + BuildConfig.VERSION_NAME);

        if (BuildConfig.DEBUG) {
            findViewById(R.id.image_launcher).setOnClickListener(v -> {
                togglePremium();
                startActivity(new Intent(AboutActivity.this, OnboardingActivity.class));
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_feedback:
                AppUtils.sendFeedbackEmail(this);
                break;

            case R.id.fab_rate:
                AppUtils.rateApp(this);
                break;

            case R.id.text_privacy_policy :
                showPrivacyPolicy();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                String packageName = getPackageName();
                // Generated using GooglePlayUrlBuilder on Google analytics website.
                String referrer = "&referrer=utm_source%3Dshareapp";
                String appUrl = getString(R.string.play_store_base_url) + packageName + referrer;

                // Share message.
                String shareMessage = String.format(getString(R.string.share_message), appUrl);
                // Share intent.
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

                if (shareIntent.resolveActivity(AboutActivity.this.getPackageManager()) != null)
                    startActivity(shareIntent);
                else
                    Toast.makeText(AboutActivity.this, "No app found to share the app!", Toast
                            .LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
/*
    // Needed to make global.
    BillingManager billingManager;

    private void consumePurchase() {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        String purchaseToken = defaultPref.getString(getString(R.string
                .pref_upgrade_purchase_token), null);
        BillingManager.BillingUpdatesListener listener = new BillingManager
                .BillingUpdatesListener() {
            @Override
            public void onBillingClientSetupFinished() {
                Toast.makeText(AboutActivity.this, "Consuming", Toast.LENGTH_SHORT).show();
                billingManager.consumePurchasedProduct(purchaseToken);
                AppUtils.setPremium(AboutActivity.this, false);
            }

            @Override
            public void onItemPurchased(@Nullable Purchase purchase) {
            }
        };
        billingManager = new BillingManager(this, listener);
    }
*/
    /**
     * Allows us to toggle the app's premium status for testing. Works only in debug version.
     */
    private void togglePremium() {
        // Toggles premium status on button click.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                (this);
        boolean currentPremiumStatus = prefs.getBoolean(getString(R.string
                .pref_is_premium_user_key), false);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean(getString(R.string.pref_is_premium_user_key), !currentPremiumStatus);
        ed.apply();
        if (currentPremiumStatus) {
            Toast.makeText(this, "Converted to NON-premium", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Converted to premium", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Allows user to view privacy policy of task nearby in a browser.
     */
    private void showPrivacyPolicy() {
        Intent privacyPolicyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)));
        startActivity(privacyPolicyIntent);
    }
}
