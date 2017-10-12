package com.farah.heavyservice;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;

public class AppRatingsActivity extends ListActivity implements OnAppRatingsDownloadCompleted {

    ListView mListView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private SharedPreferences ratingspref;
    private String LastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_ratings);
        // make sure that the screen layout is portrait
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ratingspref = getApplicationContext().getSharedPreferences(getString(R.string.ratings_preference), Context.MODE_PRIVATE);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(CommonVariables.userRegistered)
                    new DownloadAppRatingsTask(getApplicationContext(),AppRatingsActivity.this).execute(CommonVariables.DownloadRatingsURL);
                else
                    Toast.makeText(AppRatingsActivity.this, "User is not registered, try later", Toast.LENGTH_SHORT).show();
            }
        });
        mListView = (ListView) findViewById(android.R.id.list);
        LastUpdated = ratingspref.getString(getApplicationContext().getString(R.string.ratings_last_updated), "");

        if (LastUpdated.equals("") || (System.currentTimeMillis() - Long.valueOf(LastUpdated)) > 3600000) {
            new DownloadAppRatingsTask(getApplicationContext(), this).execute(CommonVariables.DownloadRatingsURL);
        } else {
           refreshMap();
        }
    }

    @Override
    public void OnTaskCompleted(boolean result) {
        refreshMap();
    }

    private void refreshMap(){
        try {
            CommonVariables.ratingsMap = Common.readAppRatingsListFromFile(CommonVariables.ratingsFile);
            mListView.setAdapter(new AppRatingsAdapter(getApplicationContext(), CommonVariables.ratingsMap));
            mSwipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
