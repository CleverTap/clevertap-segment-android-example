package com.clevertap.segmenttest;

import static com.clevertap.segmenttest.CleverTapSegmentApplication.SOURCE_ANDROID_KEY;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.segment.analytics.Analytics;
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration;

import java.util.ArrayList;
import com.segment.analytics.Properties;

import org.jetbrains.annotations.NotNull;

// note: this is just a temporary class to convert kotlin blocks to java for documentation. this will not work if added in manifest
public class TempActivity extends AppCompatActivity implements CTInboxListener {

    private CleverTapAPI ctApiGlobalInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        new Analytics
                .Builder(getApplicationContext(),SOURCE_ANDROID_KEY)
                .use(CleverTapIntegration.FACTORY)
                .build()
                .onIntegrationReady(
                        "CleverTap",
                        (Analytics.Callback<CleverTapAPI>) ctApi -> {
                            // set instance as global for future use
                            ctApiGlobalInstance = ctApi;

                            //Set the Notification Inbox Listener
                            ctApiGlobalInstance.setCTNotificationInboxListener(this);

                            //Initialize the inbox and wait for callbacks on overridden methods
                            ctApiGlobalInstance.initializeInbox();

                        }
                );
    }

    @Override
    public void inboxDidInitialize() {
        // add an on click to your inbox button to call showAppInbox() with custom or default styling
        Button yourInboxButton = null;
        if(yourInboxButton==null) return;
        yourInboxButton.setOnClickListener(v -> {
            ArrayList<String> tabs = new ArrayList<>();
            tabs.add("Promotions");
            tabs.add("Offers");//We support upto 2 tabs only. Additional tabs will be ignored

            CTInboxStyleConfig styleConfig = new CTInboxStyleConfig();
            styleConfig.setFirstTabTitle("First Tab");
            styleConfig.setTabs(tabs);//Do not use this if you don't want to use tabs
            styleConfig.setTabBackgroundColor("#FF0000");
            styleConfig.setSelectedTabIndicatorColor("#0000FF");
            styleConfig.setSelectedTabColor("#0000FF");
            styleConfig.setUnselectedTabColor("#FFFFFF");
            styleConfig.setBackButtonColor("#FF0000");
            styleConfig.setNavBarTitleColor("#FF0000");
            styleConfig.setNavBarTitle("MY INBOX");
            styleConfig.setNavBarColor("#FFFFFF");
            styleConfig.setInboxBackgroundColor("#ADD8E6");
            if (ctApiGlobalInstance != null) {
                ctApiGlobalInstance.showAppInbox(styleConfig); //With Tabs
                //ctApiGlobalInstance.showAppInbox();//Opens Activity with default style configs
            }
        });
    }
    @Override
    public void inboxMessagesDidUpdate() {
        //Callback on Inbox Message update/delete/read (any activity)


    }

    void raiseChargedEvent(@NotNull Analytics analytics){
        Properties properties = new  Properties();
        properties.putValue("orderId", "123456");
        properties.putValue("revenue", 100);
        properties.putProducts(
                        new Properties.Product("id1", "sku1", 100.0),
                        new Properties.Product("id2", "sku2", 200.0)
                );
        analytics.track("Order Completed", properties);
    }
}
