package com.example.uk.locationtaskremainder.billing;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.utils.AppUtils;

/**
 * Manages the billing functionality for premium upgrade. The following features are provided by
 * this class:
 * 1. Querying the existing purchases of the user.
 * 2. Initiating the purchase flow for the given in-app product id.
 * 3. Consuming a purchase.
 *
 * @author vermayash8
 */
public class BillingManager implements PurchasesUpdatedListener {

    public static final String TAG = BillingManager.class.getSimpleName();

    /**
     * Activity on top of which Billing dialog will be shown.
     */
    private Activity mActivity;

    /**
     * Billing client for connecting to Google Play.
     */
    private BillingClient mBillingClient;

    /**
     * The calling activity(or class) that'll listen to the responses of itemsPurchased.
     */
    private BillingUpdatesListener mBillingUpdatesListener;

    private boolean mIsServiceConnected = false;

    /**
     * Constructor to build and connect the mBillingClient to the Billing service.
     */
    public BillingManager(Activity activity, final BillingUpdatesListener updatesListener) {

        mActivity = activity;
        mBillingUpdatesListener = updatesListener;
        // We need to pass a purchase listener too. This listener will be called for new Purchases.
        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).build();

        // Start the setup asynchronously.
        // The specified listener is called once setup completes.
        startServiceConnection(() -> {
            // Notify the listener that the billing client is ready.
            mBillingUpdatesListener.onBillingClientSetupFinished();
        });
    }

    /**
     * Starts establishing the connection to the billing service (asynchronously). Once
     * connected, it starts executing the runnable passed to it.
     *
     * @param executeOnSuccess The runnable that'll be executed on success.
     */
    private void startServiceConnection(Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int
                    billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The connection was established successfully.
                    mIsServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
            }
        });
    }

    /**
     * Tells us if the billing client has successfully connected to the Google play billing service.
     */
    public boolean isConnectedToService() {
        return mIsServiceConnected;
    }

    /**
     * Query's the list of purchases made by the user for this app. It returns all the purchases
     * one by one into the onItemPurchased callback of the listener.
     */
    public void queryPurchases() {
        Runnable queryToExecute = () -> {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases
                    (BillingClient.SkuType.INAPP);
            onQueryPurchasesFinished(purchasesResult);
        };
        executeServiceRequest(queryToExecute);
    }

    /**
     * Called when querying purchases has finished with the list of purchases. Note that these
     * purchases need to be checked for signature verification and response code.
     */
    private void onQueryPurchasesFinished(Purchase.PurchasesResult result) {
        if (mBillingClient == null
                || result.getResponseCode() != BillingClient.BillingResponse.OK) {
            Log.e(TAG, "Querying purchases failed with response code:" + result.getResponseCode());
            return;
        }
        // This method will be called to query the purchases already present.
        List<Purchase> purchases = result.getPurchasesList();
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase, false);
            }
            if (purchases.size() == 0) {
                // If the user has not purchased any items, we'll return null. This is not
                // necessary and not the ideal way for putting this. Please change this in future.
                mBillingUpdatesListener.onItemPurchased(null);
            }
        }
    }

    /**
     * Initiates the purchase flow for buying an in-app item.
     */
    public void initiatePurchaseFlow(final String skuId) {
        Runnable purchaseFlowRequest = () -> {
            BillingFlowParams.Builder mParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.INAPP);
            mBillingClient.launchBillingFlow(mActivity, mParams.build());
        };
        // Execute this runnable.
        executeServiceRequest(purchaseFlowRequest);
    }

    /**
     * Checks if the client is connected to Billing service and executes the passed runnable on
     * the same calling thread.
     */
    private void executeServiceRequest(Runnable runnable) {
        if (mIsServiceConnected) {
            runnable.run();
        } else {
            // If the billing service disconnects, try to reconnect once.
            startServiceConnection(runnable);
        }
    }

    /**
     * This listener is called anytime the purchases of a user are updated. This is not called
     * when querying items but when the user has initiated a purchase flow.
     *
     * @param responseCode the response code of the purchase flow.
     * @param purchases a nullable list of purchases that tells us what was purchased.
     */
    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        switch (responseCode) {
            case BillingClient.BillingResponse.OK:
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase, true);
                    }
                }
                break;
            case BillingClient.BillingResponse.USER_CANCELED:
                // If canceled we receive this event twice.
                // See https://github.com/googlesamples/android-play-billing/issues/83
                Log.i(TAG, "User has cancelled the purchase.");
                break;
            case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                // This case should never occur. However, if it occurs, set Premium to true.
                Log.i(TAG, "Item is already owned by the user.");
                AppUtils.setPremium(mActivity, true);
                break;
            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                Toast.makeText(mActivity, "Billing service is unavailable!", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                Toast.makeText(mActivity, "Unknown error while purchasing", Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

    /**
     * Checks each individual purchase for signature verification and informs the listener if
     * that's a valid purchase. It handles the purchases from both query and new purchases. When
     * the input is a new purchase, we initiate the addition to firebase too.
     *
     * @param purchase The purchase made by the user.
     * @param newPurchase If the purchase is a new one, or from queried list of results.
     */
    private void handlePurchase(Purchase purchase, boolean newPurchase) {
        Log.d(TAG, "handlePurchase():Purchase= " + purchase + ", newPurchase= " + newPurchase);
        // Verify the secure signature verification. The responses are signed by the private key
        // (different for each developer) owned by Google play. We have the public key and so we
        // can check the integrity against that. The verification algorithms are present in
        // SecurityUtils class. For using static responses (android.test.purchased) skip this
        // verification.
        if (!SecurityUtils.verifyValidSignature(mActivity, purchase.getOriginalJson(),
                purchase.getSignature())) {
            // Skip a purchase if the signature isn't valid.
            return;
        }
        // Notify that a new item was purchased.
        mBillingUpdatesListener.onItemPurchased(purchase);
    }

    /**
     * This is here just for testing purposes.
     */
    public void consumePurchasedProduct(String purchaseToken) {
        mBillingClient.consumeAsync(purchaseToken, (responseCode, purchaseToken1) -> {
            if (responseCode == BillingClient.BillingResponse.OK) {
                Log.e(TAG, "Consumed successfully.");
            } else {
                Log.e(TAG, "Consumption failed with response code : " + responseCode);
            }
        });
    }

    /**
     * Callback that the caller should implement. Since most of the processing here is asynchronous.
     */
    public interface BillingUpdatesListener {

        /**
         * Called whenever the billing client set up has been completed. This is used when we
         * initiate a query of pre-owned items.
         */
        void onBillingClientSetupFinished();

        /**
         * Called whenever a purchase is detected. Note that it is called for both queried
         * purchases as well as new items that are purchased.
         */
        void onItemPurchased(@Nullable Purchase purchase);
    }
}
