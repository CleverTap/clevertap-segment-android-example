package com.clevertap.segmenttest;

import android.app.Application;
import android.util.Log;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.SyncListener;
import com.segment.analytics.Analytics;
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration;

import org.json.JSONObject;

public class CleverTapSegmentApplication extends Application implements SyncListener {

    private static final String TAG = String.format("%s.%s", "CLEVERTAP", CleverTapSegmentApplication.class.getName());
    private static final String WRITE_KEY = "YOUR_SEGMENT_WRITE_KEY";
    private static final String CLEVERTAP_KEY = "CleverTap";
    public static boolean sCleverTapSegmentEnabled = false;

    private CleverTapAPI clevertap;

    @Override public void onCreate() {
        super.onCreate();

        CleverTapAPI.setDebugLevel(1);

        Analytics analytics = new Analytics.Builder(getApplicationContext(), WRITE_KEY)
                .logLevel(Analytics.LogLevel.DEBUG)
                .use(CleverTapIntegration.FACTORY)
                .build();

        analytics.onIntegrationReady(CLEVERTAP_KEY, new Analytics.Callback<CleverTapAPI>() {
            @Override
            public void onReady(CleverTapAPI instance) {
                Log.i(TAG, "analytics.onIntegrationReady() called");
                CleverTapIntegrationReady(instance);
            }
        });

        Analytics.setSingletonInstance(analytics);
    }

    private void CleverTapIntegrationReady(CleverTapAPI instance) {
        instance.enablePersonalization();
        sCleverTapSegmentEnabled = true;
        clevertap = instance;
        clevertap.setSyncListener(this);
        //on initial app install, a call to getCleverTapID will return NULL until the profile is fully initialized
        // rely on the profileDidInitialize callback in that case
        String clevertapID = clevertap.getCleverTapID();
        Log.d("CLEVERTAP_ID", clevertapID != null ? clevertapID : "NULL");
    }

    // SyncListener
    public void profileDidInitialize(String CleverTapID){
        Log.d("CLEVERTAP_INITIALIZED", CleverTapID);
        Log.d("CLEVERTAP_ID", clevertap.getCleverTapID());
    }

    public void profileDataUpdated(JSONObject updates) {
        Log.d("CT_PROFILE_UPDATES", updates.toString());
    }
}
