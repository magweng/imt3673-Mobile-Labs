package com.example.magnus.chatapplication;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// long running service
// https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android used this to keep service alive even if app is destroyed
public class MessageService extends Service {

    public static final String MESSAGE_SERVICE      = "message_service";
    public static final String ALIVE_SIGNAL         = "alive_signal";
    public static final String WAKEUP_SIGNAL        = "wakeup";
    public static final String WAKEUP_SIGNAL_START  = "wakeup_start";
    public static final String MESSAGE_DATE         = "message_date";
    public static final String MESSAGE_USER         = "message_user";
    public static final String MESSAGE_MESSAGE      = "message_message";
    public static final String WAKEUP_TYPE          = "wakeup_type";
    public static final String WAKEUP_AUTH          = "wakeup_auth";
    public static final String USER                 = "user";
    public static final String USER_USERS           = "users_list";

    private static final String CHANNEL_ID          = "message_notification";


    private final ArrayList<String> mMessages      = new ArrayList<>();
    private final ArrayList<String> mUsers         = new ArrayList<>();
    private final ArrayList<String> mDates         = new ArrayList<>();
    private final ArrayList<String> mUsersNames    = new ArrayList<>();

    private Boolean mUserIsAuthenticated     = false;
    private Boolean mAppIsAlive              = false;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private final ServiceReceiver mReceiver  = new ServiceReceiver();

    private NotificationCompat.Builder mNotificationBuilderMessage;
    private NotificationManagerCompat  mNotificationManager;
    private int mNotificationID = 0;
    private Boolean mFirstStartUpNotification = true;
    private Boolean mServiceRestarted         = false;



    public MessageService() {
    }

    public MessageService(Context context){
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Check if service restarted or started from activity
        if(intent == null) {
            this.mAppIsAlive = false;
            this.mFirstStartUpNotification = true;
            Toast.makeText(this,"NULL IS USED ON RESTART",  Toast.LENGTH_SHORT).show();
        } else {
            this.mAppIsAlive = intent.getBooleanExtra(WAKEUP_SIGNAL_START, false);
        }

        initNotifications();

        // Set up receiver
        this.mIntentFilter.addAction(ALIVE_SIGNAL);
        registerReceiver(this.mReceiver,this.mIntentFilter);

        Toast.makeText(this,"Starting service",  Toast.LENGTH_SHORT).show();

        initFirebase();

        return START_STICKY;
    }

    /**
     * Initialize Firestore and add SnapshotListener on message and user collection
     */
    private void initFirebase() {
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        mDatabase.collection("messages")
                .orderBy("d")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("dbService", "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {

                            switch (dc.getType()) {
                                case ADDED:
                                    if(dc.getDocument().get("d") != null) {
                                        Date date =  (Date)dc.getDocument().get("d");
                                        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
                                        mDates.add(sfd.format(date));
                                    } else {
                                        mDates.add("no date");
                                    }
                                    mUsers.add(dc.getDocument().get("u").toString());
                                    mMessages.add(dc.getDocument().get("m").toString());
                                    break;
                            }
                        }
                        //Do not send notification first time service restarts
                        if(mAppIsAlive && mUserIsAuthenticated){
                            sendUpdateMessages();
                        } else {
                            if(!mFirstStartUpNotification || mServiceRestarted){
                                mNotificationManager.notify(mNotificationID++, mNotificationBuilderMessage.build());
                            }
                            mServiceRestarted = true;
                        }
                    }
                });

        mDatabase.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("dbService", "listen:error", e);
                            Toast.makeText(getApplicationContext(),"ERROR GETTING USERS",  Toast.LENGTH_LONG).show();
                            return;
                        }
                        for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {

                            switch (dc.getType()) {
                                case ADDED:

                                    if(!mUsersNames.contains(dc.getDocument().get("username").toString())){
                                        mUsersNames.add(dc.getDocument().get("username").toString());
                                    }
                                    break;
                            }
                        }
                        if(mAppIsAlive && mUserIsAuthenticated){
                            sendUpdateUsers();
                        }
                    }
                });
    }

    /**
     * Initialize Notifications
     */
    private void initNotifications() {
        Intent intent = new Intent(this,TabbedActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        this.mNotificationBuilderMessage = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Message!")
                .setContentText("New message in global chat room")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        this.mNotificationManager = NotificationManagerCompat.from(this);
    }

    /**
     * Sends signal to RestartServiceReceiver to restart service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this,"Stopping service",  Toast.LENGTH_LONG).show();
        unregisterReceiver(this.mReceiver);
    }

    /**
     * Send update to UserFragment
     */
    private void sendUpdateUsers(){
        Intent intent = new Intent();
        intent.setAction(USER);
        intent.putStringArrayListExtra(USER_USERS,this.mUsersNames);

        sendBroadcast(intent);
    }


    /**
     * Send update to MessageFragment
     */
    private void sendUpdateMessages(){
        Intent intent = new Intent();
        intent.setAction(MESSAGE_SERVICE);
        intent.putStringArrayListExtra(MESSAGE_DATE,this.mDates);
        intent.putStringArrayListExtra(MESSAGE_USER,this.mUsers);
        intent.putStringArrayListExtra(MESSAGE_MESSAGE,this.mMessages);
        sendBroadcast(intent);
    }

    /**
     * Receives signal if the application is onPause,destroyed and awake and authentication
     */
    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra(WAKEUP_TYPE);

            if("WAKEUP".equalsIgnoreCase(message))
                mAppIsAlive = intent.getBooleanExtra(WAKEUP_SIGNAL,false);

            if("AUTH".equalsIgnoreCase(message)){
                mUserIsAuthenticated = intent.getBooleanExtra(WAKEUP_AUTH,false);
                mAppIsAlive = true;
                mFirstStartUpNotification = false;
            }

            if(mAppIsAlive && mUserIsAuthenticated){
                sendUpdateMessages();
                sendUpdateUsers();
            }
        }
    }
}
