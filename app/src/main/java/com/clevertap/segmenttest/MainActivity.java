package com.clevertap.segmenttest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.Toast;

import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.Properties.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements CTInboxListener {

    Random mRandom = new Random();
    private static final String TAG = String.format("%s.%s", "CLEVERTAP", MainActivity.class.getName());
    private static final String CLEVERTAP_KEY = "CleverTap";
    private CleverTapAPI clevertap;
    Button inboxButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inboxButton = findViewById(R.id.inboxButton);
        if (inboxButton != null) {
            inboxButton.setVisibility(View.GONE);
        }

        if (CleverTapAPI.getDefaultInstance(getApplicationContext()) != null) {
            CleverTapIntegrationReady();
        } else {
            Analytics analytics = Analytics.with(getApplicationContext());
            analytics.onIntegrationReady(CLEVERTAP_KEY, new Analytics.Callback<CleverTapAPI>() {
                @Override
                public void onReady(CleverTapAPI instance) {
                    Log.i(TAG, "analytics.onIntegrationReady() called");
                    CleverTapIntegrationReady();
                }
            });
        }

        Button identifyButton = findViewById(R.id.identifyButton);

        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String newUser = Integer.toString(Math.abs(mRandom.nextInt()));
                Toast.makeText(getApplicationContext(), "identify() called with user id: " + newUser + ".", Toast.LENGTH_LONG).show();
                ArrayList<String> testArr = new ArrayList<>();
                testArr.add("one");
                testArr.add("two");
                testArr.add("three");
                Traits traits = new Traits();
                traits.putEmail("foo@foo.com");
                traits.putName("FooName");
                traits.putGender("male");
                traits.putPhone("+14155551234");
                traits.put("boolean", true);
                traits.put("integer", 50);
                traits.put("float", 1.5);
                traits.put("long", 12345L);
                traits.put("string", "hello");
                traits.put("stringInt", "1");
                traits.put("testStringArr", testArr);
                Analytics.with(getApplicationContext()).identify(newUser, traits, null);
                Analytics.with(getApplicationContext()).screen("Home Screen");
            }
        });

        Button trackButton = findViewById(R.id.trackButton);
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(getApplicationContext(), "track() called for custom event 'testEvent'.", Toast.LENGTH_LONG).show();

                Analytics.with(getApplicationContext()).track("testEvent",
                        new Properties().putValue("value", "testValue")
                        .putValue("testDate", new Date(System.currentTimeMillis()))
                );

                final String orderId = "123456";
                final int revenue = 100;
                Properties properties = new Properties();
                properties.putValue("orderId", orderId).putValue("revenue", revenue);

                Product product1 = new Product("id1", "sku1", 100);
                Product product2 = new Product("id2", "sku2", 200);
                properties.putProducts(product1, product2);

                Analytics.with(getApplicationContext()).track("Order Completed", properties);
            }
        });

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                Log.d("INTENT_URI", data.toString());
                handleDeepLink(data);
            }
        }
    }

    // handle deep links
    private void handleDeepLink(Uri data) {
        //To get scheme.
        String scheme = data.getScheme();
        Log.d("DEEP_LINK", scheme);

    }

    private void CleverTapIntegrationReady() {
        if (clevertap == null) {
            clevertap = CleverTapAPI.getDefaultInstance(getApplicationContext());
        }
        if (clevertap != null) {
            clevertap.setCTNotificationInboxListener(MainActivity.this);
            clevertap.initializeInbox();
        }
    }

    private long mLastInboxClickTime = 0;
    @Override
    public void inboxDidInitialize() {

        if (inboxButton == null) {
            return;
        }

        inboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clevertap == null) {
                    return;
                }
                if (SystemClock.elapsedRealtime() - mLastInboxClickTime < 1000){
                    return;
                }
                mLastInboxClickTime = SystemClock.elapsedRealtime();

                ArrayList<String> tabs = new ArrayList<>();
                tabs.add("Promotions");
                tabs.add("Offers");
                tabs.add("Will Not Show");//Anything after the first 2 will be ignored
                CTInboxStyleConfig styleConfig = new CTInboxStyleConfig();
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
                clevertap.showAppInbox(styleConfig); //With Tabs
                //clevertap.showAppInbox();//Opens Activity with default style configs
            }
        });
        updateInboxButton();
    }

    @Override
    public void inboxMessagesDidUpdate() {
        updateInboxButton();
    }

    private void updateInboxButton() {
        if (clevertap == null) {
            return;
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (inboxButton == null) {
                    return;
                }
                final int messageCount = clevertap.getInboxMessageCount();
                final int unreadMessageCount = clevertap.getInboxMessageUnreadCount();
                inboxButton.setText(String.format(Locale.getDefault(),"Inbox: %d messages /%d unread", messageCount, unreadMessageCount));
                inboxButton.setVisibility(View.VISIBLE);
            }
        });
    }
}
