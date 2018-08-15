package com.example.magnus.lab_3;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * MainActivity that implements SensorEventListener
 * Updated ball view on sensor change from accelerometer
 * Inspiration https://gist.github.com/Jawnnypoo/fcceea44be628c2d5ae1
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private BallView      mBallView;
    private SensorManager mSensorManager;
    private Sensor        mAccelerometer;
    private int           screenSizeWidth;
    private int           screenSizeHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initScreenSize();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mBallView = new BallView(this,this.screenSizeWidth, this.screenSizeHeight, vibrator);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Hide action bar
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        // Set orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Set fullscreen and hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(mBallView);
    }

    /**
     * Initialize screen size
     */
    private void initScreenSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        this.screenSizeWidth = size.x;
        this.screenSizeHeight = size.y;
    }

    /**
     * When application starts register sensor
     */
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * When application stops unregister sensor
     */
    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    /**
     *Updates Ball View on sensor event from ACCELEROMETER
     */
    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mBallView.update(sensorEvent);
        }
    }

    /**
     *Not used
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}


