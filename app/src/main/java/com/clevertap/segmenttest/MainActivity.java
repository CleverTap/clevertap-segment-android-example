package com.clevertap.segmenttest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button identifyButton = (Button) findViewById(R.id.identifyButton);

        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String newUser = Integer.toString(Math.abs(mRandom.nextInt()));
                Toast.makeText(getApplicationContext(), "identify() called with user id: " + newUser + ".", Toast.LENGTH_LONG).show();
                Traits traits = new Traits();
                traits.putEmail("foo@foo.com");
                traits.putName("FooName");
                traits.putGender("male");
                traits.putPhone("5555555555");
                traits.put("boolean", true);
                traits.put("integer", 50);
                traits.put("float", 1.5);
                traits.put("long", 12345L);
                traits.put("string", "hello");
                Analytics.with(getApplicationContext()).identify(newUser, traits, null);
            }
        });

        Button trackButton = (Button) findViewById(R.id.trackButton);
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(getApplicationContext(), "track() called for custom event 'testEvent'.", Toast.LENGTH_LONG).show();
                Analytics.with(getApplicationContext()).track("testEvent", new Properties().putValue("value", "testValue"));
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
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
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
}
