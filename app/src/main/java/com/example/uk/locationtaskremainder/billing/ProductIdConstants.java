package com.example.uk.locationtaskremainder.billing;

/**
 * Keeps the constants required for ProductId.
 *
 * @author vermayash8
 */
public class ProductIdConstants {

    public static final String PREMIUM_PRODUCT_ID = "premium_upgrade";

    /*
     * Note:
     * These have been kept for testing.
     * If you want to try in-app billing with static responses (without uploading to Google Play),
     * uncomment the purchase type and use its id as the skuId everywhere PREMIUM_PRODUCT_ID is
     * used.
     */
//    public static final String TEST_PURCHASED = "android.test.purchased";
//    public static final String TEST_CANCELED = "android.test.canceled";
//    public static final String TEST_REFUNDED = "android.test.refunded";
//    public static final String TEST_UNAVAILABLE = "android.test.item_unavailable";
}
