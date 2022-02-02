package com.bttest;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.ThreeDSecureAdditionalInformation;
import com.braintreepayments.api.ThreeDSecurePostalAddress;
import com.braintreepayments.api.ThreeDSecureRequest;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

// documentation for Android Drop In https://javadoc.io/doc/com.braintreepayments.api/drop-in/6.0.0-beta2/index.html
// general documentation for Braintree Android SDK https://www.javadoc.io/doc/com.braintreepayments.api/braintree/latest/index.html

public class BTDropIn extends ReactContextBaseJavaModule {
    Promise mPromise;
    Activity mActivity;
    private final int DROP_IN_REQUEST_CODE = 100; // arbitrary number used to trace activities

    // constructor
    BTDropIn(ReactApplicationContext context) {
        super(context);
        ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                super.onActivityResult(activity, requestCode, resultCode, data);
                if (requestCode == DROP_IN_REQUEST_CODE) {
                    if (resultCode == RESULT_OK) {
                        DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                        PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();
                        if (paymentMethodNonce != null) {
                            try {
                                final String nonce = paymentMethodNonce.getString();
                                mPromise.resolve(nonce); // Send nonce to React Native
                            } catch (Exception exception) {
                                mPromise.reject("NONCE_ERROR", "Could not send the nonce");
                            }
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        mPromise.reject("CANCELED", "User closed drop in before purchase");
                    } else {
                        mPromise.reject("ERROR", "Unknown error occurred");

                    }
                }
                mPromise = null;

            }

            @Override
            public void onNewIntent(Intent intent) {
                super.onNewIntent(intent);
                Log.d("err","something intent");
                if (mActivity != null){
                    mActivity.setIntent(intent);
                }
            }
        };
        context.addActivityEventListener(mActivityEventListener);
    }


    @NonNull
    @Override
    public String getName() {
        return "BTDropIn";
    }

    @ReactMethod
    public void show(ReadableMap options, Promise promise) {
        mPromise = promise;
        mActivity = getCurrentActivity(); // get root React view
        ReactApplicationContext context = getReactApplicationContext();
        if (mActivity == null) {
            promise.reject("Error", "No activity");
            return;
        }
        if (!options.hasKey("clientToken")) {
            promise.reject("NO_CLIENT_TOKEN", "You must provide a client token");
            return;
        }

        String clientToken = options.getString("clientToken");

        // 3DSecure address
        ThreeDSecurePostalAddress address = new ThreeDSecurePostalAddress();
        address.setGivenName(options.getString("forename"));
        address.setSurname(options.getString("surname"));
        address.setStreetAddress(options.getString("addressLine1"));
        address.setLocality(options.getString("city"));
        address.setPostalCode(options.getString("postcode"));

        // 3DSecure additional info -- the more info the less likely customer is presented with a challenge from their bank
        ThreeDSecureAdditionalInformation additionalInfo = new ThreeDSecureAdditionalInformation();
        additionalInfo.setShippingAddress(address);

        // init 3DSecure request
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        String amount = options.getString("amount");
        threeDSecureRequest.setAmount(amount);
        threeDSecureRequest.setVersionRequested(ThreeDSecureRequest.VERSION_2);
        threeDSecureRequest.setBillingAddress(address);
        threeDSecureRequest.setEmail(options.getString("email"));
        threeDSecureRequest.setAdditionalInformation(additionalInfo);

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setPayPalDisabled(true); // it is enabled by default
        dropInRequest.setVaultManagerEnabled((false)); // if we allow deletion of stored cards it will break recurring billing
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        DropInClient dropInClient = new DropInClient(context, clientToken, dropInRequest);

        try{
            // cast type Activity to FragmentActivity to satisfy method parameter
            dropInClient.launchDropInForResult((FragmentActivity) mActivity,
                    DROP_IN_REQUEST_CODE);
        } catch (Exception exception) {
            mPromise.reject("DROP_IN_NOT_LAUNCHED", "The drop in could not launch");
        }

    }

}
