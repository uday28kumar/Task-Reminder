package com.example.uk.locationtaskremainder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.billingclient.api.Purchase;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.analytics.FirebaseAnalytics;

import app.tasknearby.yashcreations.com.tasknearby.billing.BillingManager;
import app.tasknearby.yashcreations.com.tasknearby.billing.ProductIdConstants;
import app.tasknearby.yashcreations.com.tasknearby.fcm.TopicSubscriber;
import app.tasknearby.yashcreations.com.tasknearby.fragments.TasksFragment;
import app.tasknearby.yashcreations.com.tasknearby.services.FusedLocationService;
import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;
import app.tasknearby.yashcreations.com.tasknearby.utils.firebase.AnalyticsConstants;

/**
 * Shows the list of tasks segregated into categories when the app loads. This activity also
 * contains the switch that will turn the app's service on or off.
 *
 * @author vermayash8
 */
public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, BillingManager.BillingUpdatesListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSIONS = 123;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 1000;

    private SettingsClient mSettingsClient;

    private FirebaseAnalytics mFirebaseAnalytics;

    private boolean isAppInitialized = false;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    private SharedPreferences prefs;

    private BillingManager mBillingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!AppUtils.hasUserSeenOnboarding(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            // For preventing multiple instances of MainActivity and permission dialog on
            // onboarding screen.
            finish();
        }

        setContentView(R.layout.activity_main2);
        // Device's location settings.
        mSettingsClient = LocationServices.getSettingsClient(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        logAnalytics();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize SharedPreferences.
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setVersionPreference();
        setupNavDrawer();
        setupFcm();

        findViewById(R.id.fab).setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, TaskCreatorActivity.class)));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new TasksFragment())
                .commit();

        mBillingManager = new BillingManager(this, this);
    }

    @Override
    protected void onStart() {
        if (checkPermissions()) {
            // Check permissions will automatically request permissions if they're not present.
            startServiceIfAppEnabled();
        }
        super.onStart();
    }

    private void startServiceIfAppEnabled() {
        SwitchCompat appSwitch = findViewById(R.id.switch_app_status);
        boolean isAppEnabled = prefs.getString(getString(R.string.pref_status_key), getString(
                R.string.pref_status_default)).equals(getString(R.string.pref_status_enabled));
        appSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                onAppStatusChanged(isChecked));
        appSwitch.setChecked(isAppEnabled); // This will also trigger the onClickListener.
        // If app is enabled, check for device's location settings.
        if (isAppEnabled) {
            checkLocationSettings();
        }
    }

    private void onAppStatusChanged(boolean status) {
        SharedPreferences.Editor editor = prefs.edit();
        if (status) {
            // Put enabled string in SharedPreferences.
            editor.putString(getString(R.string.pref_status_key),
                    getString(R.string.pref_status_enabled));
            if(isAppInitialized) {
                Toast.makeText(this, R.string.app_enabled, Toast.LENGTH_SHORT).show();
            }
            mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_APP_ENABLED, new Bundle());
            AppUtils.startService(this);
        } else {
            // Put disabled string in shared preferences.
            editor.putString(getString(R.string.pref_status_key),
                    getString(R.string.pref_status_disabled));
            if(isAppInitialized) {
                Toast.makeText(this, R.string.app_disabled, Toast.LENGTH_SHORT).show();
            }
            mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_APP_DISABLED, new Bundle());
            AppUtils.stopService(this);
        }
        isAppInitialized = true;
        editor.apply();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                        .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE_PERMISSIONS);
        // Now the onRequestPermissionsResult method will take care of the rest.
        // If permissions are granted, it'll start the startAppIfEnabled method.
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, continue app.
                startServiceIfAppEnabled();
            } else {
                // Handle permission request denied.
                // Permission requests can be denied in 2 ways: a) Deny  b) Never ask again.
                // 1. Deny case:
                // shouldShowRequestPermissionRationale tells us if the user clicked deny and
                // hence we should show an explanation for the permission request.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    // Permissions were denied, so asking again (Ideally we should show an
                    // explanation).
                    checkPermissions();
                } else {
                    // User clicked never ask again.
                    // Show a persistent dialog to enable the permissions from settings.
                    showPermissionsFromSettingsDialog();
                    // When the settings screen will close, onStart will be called and it'll
                    // start the service after checking permissions.
                }
            }
        }
    }

    private void showPermissionsFromSettingsDialog() {
        AlertDialog permissionsDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_permission_title)
                .setMessage(R.string.dialog_permission_message)
                .setPositiveButton(R.string.dialog_grant_permission_button, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setCancelable(false)
                .create();
        permissionsDialog.show();
    }

    private void setupNavDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, findViewById(R.id
                .toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Hide upgrade to premium if already upgraded.
        if (AppUtils.isPremiumUser(this)) {
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_group_premium).setVisible(false);
        }
    }

    /**
     * Sets the version preference.
     */
    public void setVersionPreference() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.pref_version_key), getString(R.string.app_version));
        editor.apply();
    }

    /**
     * It's fine to subscribe on app start, see this: https://stackoverflow.com/a/40055267/4857588
     */
    private void setupFcm() {
        TopicSubscriber.subscribeToAllTopics();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_feedback:
                AppUtils.sendFeedbackEmail(this);
                break;
            case R.id.nav_share:
                AppUtils.rateApp(this);
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.premium:
                UpgradeActivity.show(this);
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Checks for required location settings according to setting's power saver preference.
     */
    public void checkLocationSettings() {
        buildLocationsSettingsRequest();
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnFailureListener(e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to " +
                                    "upgrade location settings ");
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MainActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = getString(R.string.error_fix_location_settings);
                            Log.e(TAG, errorMessage);
                            Toast.makeText(MainActivity.this, errorMessage, Toast
                                    .LENGTH_LONG).show();
                    }
                });
    }

    public void buildLocationsSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        LocationRequest locationRequest = FusedLocationService.createLocationRequest(this,
                FusedLocationService.DEFAULT_LOCATION_UPDATE_INTERVAL);
        builder.addLocationRequest(locationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void logAnalytics() {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isPowerSaver = defaultPref.getBoolean(getString(R.string.pref_power_saver_key),
                false);
        Bundle bundle = new Bundle();
        bundle.putBoolean(AnalyticsConstants.ANALYTICS_PARAM_IS_POWER_SAVER_ON, isPowerSaver);
        mFirebaseAnalytics.logEvent(AnalyticsConstants.ANALYTICS_APP_START, bundle);
    }


    /**
     * Callbacks for the BillingManager class.
     */
    @Override
    public void onBillingClientSetupFinished() {
        mBillingManager.queryPurchases();
    }

    @Override
    public void onItemPurchased(Purchase purchase) {
        if (AppUtils.isPremiumUser(this)) {
            if (purchase == null) {
                // This user is a premium user but he has not purchased anything.
                reportPremiumWithoutPurchase();
            }
        } else if (purchase != null
                && purchase.getSku().equals(ProductIdConstants.PREMIUM_PRODUCT_ID)) {
            // If queried purchases include premium upgrade, then make it premium.
            AppUtils.setPremium(this, true);
            AppUtils.savePurchaseDetails(this, purchase);
        }
    }

    private void reportPremiumWithoutPurchase() {
        String orderId = prefs.getString(getString(R.string.pref_upgrade_order_id), null);
        String purchaseToken = prefs.getString(getString(R.string.pref_upgrade_purchase_token),
                null);
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.EXTRA_ORDER_ID, orderId);
        bundle.putString(AnalyticsConstants.EXTRA_PURCHASE_TOKEN, purchaseToken);
        mFirebaseAnalytics.logEvent(AnalyticsConstants.NOT_PURCHASED_BUT_PREMIUM, bundle);
    }

    /**
     * Starts the service when device is rebooted.
     * The applications are placed in a 'Stopped' state after install and AFTER Force stop TOO.
     * When an application is in the stopped state, it won't receive any broadcasts, no matter what!
     * Hence, when this app is killed by the user, it won't receive any boot completed broadcast.
     */
    public static class BootCompletedReceiver extends BroadcastReceiver {

        public BootCompletedReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: Received BOOT_COMPLETED.");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            // Check if app is enabled or not.
            boolean isAppEnabled = prefs.getString(context.getString(R.string.pref_status_key),
                    context.getString(R.string.pref_status_default))
                    .equals(context.getString(R.string.pref_status_enabled));
            // Also check if location permissions are available or not.
            if (isAppEnabled && ActivityCompat.checkSelfPermission(context, Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                AppUtils.startService(context);
            }
        }
    }
}
