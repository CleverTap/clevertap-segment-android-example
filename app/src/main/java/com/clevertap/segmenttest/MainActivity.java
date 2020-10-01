package com.clevertap.segmenttest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.Utils;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.Properties.Product;

import net.andreinc.mockneat.MockNeat;

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
        Button aliasButton = findViewById(R.id.aliasButton);
        Button resetButton = findViewById(R.id.resetButton);
        final EditText eTEmail = findViewById(R.id.eTEmail);
        final EditText eTId = findViewById(R.id.eTId);

        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pushProfile(eTId, eTEmail,false);
            }
        });

        aliasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pushProfile(eTId, eTEmail,true);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.with(getApplicationContext()).reset();
                Toast.makeText(getApplicationContext(), "reset() called ", Toast.LENGTH_LONG).show();
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

    private void pushProfile(EditText eTId, EditText eTEmail, boolean isMergeProfile) {

        MockNeat mock = MockNeat.threadLocal();
        String newUser = String.valueOf(mock.intSeq().get());//Integer.toString(Math.abs(mRandom.nextInt()));

        String userId = eTId.getText().toString();
        if (!TextUtils.isEmpty(userId.trim()))
        {
            newUser=userId;
        }

        ArrayList<String> testArr = new ArrayList<>();
        testArr.add("one");
        testArr.add("two");
        testArr.add("three");
        Traits traits = new Traits();
        String email = eTEmail.getText().toString();

        if (!TextUtils.isEmpty(email.trim()))
        {
            traits.putEmail(email);
        }else
        {
            traits.putEmail(mock.emails().get());
        }
        traits.putName(mock.names().full().get());
        traits.putGender(mock.genders().get());
        traits.putPhone("+14155551234");
        traits.put("boolean", true);
        traits.put("integer", 50);
        traits.put("float", 1.5);
        traits.put("long", 12345L);
        traits.put("string", "hello");
        traits.put("stringInt", "1");
        traits.put("testStringArr", testArr);

        if (isMergeProfile) {
            Analytics.with(getApplicationContext()).alias(newUser);
            Toast.makeText(getApplicationContext(), "alias() called with user id: " + newUser + ".", Toast.LENGTH_LONG).show();
        } else {
            //Analytics.with(getApplicationContext()).reset();
            Toast.makeText(getApplicationContext(), "identify() called with user id: " + newUser + ".", Toast.LENGTH_LONG).show();
            Analytics.with(getApplicationContext()).identify(newUser, traits, null);
        }
        Analytics.with(getApplicationContext()).screen("Home Screen");
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
