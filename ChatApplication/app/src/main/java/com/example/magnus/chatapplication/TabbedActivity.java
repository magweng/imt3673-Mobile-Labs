package com.example.magnus.chatapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;


/**
 * TabbesActivity initialized message and user fragments.
 */
public class TabbedActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private final MessagesFragment mMessageFragment = new MessagesFragment();
    private final UsersFragment    mUserFragment    = new UsersFragment();

    private String  mUsername;
    private Boolean mCurrentUserAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
    }

    /**
     * Get authentication from, login activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                // user sign in
                this.mCurrentUserAuth = data.getBooleanExtra(LoginActivity.AUTHENTICATION,false);
                this.mUsername = data.getStringExtra(LoginActivity.USERNAME);

                sendAuthenticationToService(this.mCurrentUserAuth);
            }
        }
    }

    /**
     * Send authentication status to service
     */
    private void sendAuthenticationToService(Boolean signal) {
        Intent intent = new Intent();
        intent.setAction(MessageService.ALIVE_SIGNAL);
        intent.putExtra(MessageService.WAKEUP_TYPE,"AUTH");
        intent.putExtra(MessageService.WAKEUP_AUTH, signal);
        sendBroadcast(intent);
    }

    /**
     * Initialize tabs and fragments window
     */
    private void initTabsAndFragments() {
        ViewPager mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Initialize service and start service if it's not running
     */
    private void InitService() {
        // start service if it does not exist
        MessageService mMessageService = new MessageService(this);
        Intent mServiceIntent = new Intent(this, mMessageService.getClass());
        if(!isServiceRunning(mMessageService.getClass())) {
            Log.i("MAIN", "Starting service");
            mServiceIntent.putExtra(MessageService.WAKEUP_SIGNAL_START, true);
            startService(mServiceIntent);
        }
    }

    /**
     * Initialise tabs and service when resumes
     */
    @Override
    protected void onResume() {

        Log.i("MAIN","authentication = " + this.mCurrentUserAuth);

        if(!this.mCurrentUserAuth){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }

        initTabsAndFragments();
        InitService();
        super.onResume();
    }

    /**
     * Check if service is running
     * Taken from https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
     * @param serviceClass Class to check
     * @return true if service is running false if not
     */
    private boolean isServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
                if(serviceClass.getName().equals(service.service.getClassName())){
                    Log.i("MAIN","service is running");
                    sendAuthenticationToService(this.mCurrentUserAuth);

                    return true;
                }
            }
        }
        Log.i("MAIN","service is not running");
        return false;
    }


    /**
     * Setup view pager by adding fragments
     */
    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(this.mMessageFragment,"Messages");
        adapter.addFragment(this.mUserFragment,"Users");
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        sendWakeUpSignal();
      //  stopService(this.mServiceIntent);
        Log.i("MAIN","ON DESTROY = " + this.mCurrentUserAuth);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        sendWakeUpSignal();
        Log.i("MAIN","ON PAUSE = " + this.mCurrentUserAuth);
        super.onPause();
    }


    /**
     * Send signal to MessageService that application is sleeping
     */
    private void sendWakeUpSignal() {
        Intent intent = new Intent();
        intent.setAction(MessageService.ALIVE_SIGNAL);
        intent.putExtra(MessageService.WAKEUP_TYPE,"WAKEUP");
        intent.putExtra(MessageService.WAKEUP_SIGNAL,false);
        sendBroadcast(intent);
    }

    /**
     * Returns username
     * @return userName
     */
    public String getUserName(){
        return this.mUsername;
    }

}
