package com.example.uk.locationtaskremainder.billing;

import android.content.Context;

/**
 * Security utils for the in-app billing procedure. This file should not be committed to the
 * version control. This file will have changes that won't be committed to version control
 * because of security risks. We'll have our public key signature verifying algorithms here.
 *
 * @author vermayash8
 */
class SecurityUtils {

    static boolean verifyValidSignature(Context context, String json, String signature) {
        return true;
    }
}
