package com.example.magnus.lab_3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BallView class : creates a view that contains a frame and a circle
 * Moves circle based on accelerometer sensor from MainActivity
 */

public class BallView extends View {

    private final int   FRAME_MARGIN = 10;
    private final float DRAG = 0.5f;
    private final float DELTA = 0.30f;
    private final float FLAT_SURFACE_MIN_VALUE = 9.70f;
    private final float FLAT_SURFACE_MAX_VALUE = 11.00f;

    private Paint    mRectPaint;
    private Rect     mFrame;

    private Vibrator mVibrator;

    private int      screenSizeWidth;
    private int      screenSizeHeight;

    private Paint    mCirclePaint;
    private int      circleRadius;
    private float    xPosCircle;
    private float    yPosCircle;
    private float    velX        = 0.0f;
    private float    velY        = 0.0f;

    private Boolean  onFlatSurfaceStartUp = false;

    private Boolean  hasCollided = false;
    private Boolean  hasVibrated = false;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private ToneGenerator toneGenerator;


    /**
     * Constructor initializes the view
     * @param context context from MainActivity
     * @param screenW Screen Width
     * @param screenH Screen Height
     * @param vibrator Vibrator
     */
    public BallView(Context context, int screenW, int screenH, Vibrator vibrator) {
        super(context);

        this.toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        this.screenSizeWidth = screenW;
        this.screenSizeHeight = screenH;
        this.mVibrator = vibrator;
        InitCircle();
        initFrame();
    }

    /**
     * to Avoid warning constructor
     */
    public BallView(Context context) {
        super(context);
    }

    /**
     * to avoid warning constructor
     */
    public BallView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Initialize circle
     */
    private void InitCircle() {
        // Set circle position
        this.xPosCircle = this.screenSizeWidth / 2;
        this.yPosCircle = this.screenSizeHeight / 2;

        // Circle
        this.mCirclePaint = new Paint();
        this.mCirclePaint.setColor(Color.WHITE);
        this.circleRadius = (int)(0.05 * (float)screenSizeWidth);
    }

    /**
     * Initialize frame
     */
    private void initFrame() {
        this.mFrame = new Rect(FRAME_MARGIN, FRAME_MARGIN, screenSizeWidth - FRAME_MARGIN, screenSizeHeight - FRAME_MARGIN);
        this.mRectPaint = new Paint();
        this.mRectPaint.setColor(Color.BLACK);
    }


    /**
     * Update velocity, circle position and check collision
     * @param event SensorEvent
     */
    public void update(SensorEvent event){

       float sensorX = event.values[0];
       float sensorY = event.values[1];
       float sensorZ = event.values[2];

      // this.velX -= sensorX;
      // this.velY += sensorY;
        this.velX += sensorY;
        this.velY += sensorX;


        // When app is started on a flat surface the ball will not move from center until the user starts tilting the phone.
        if(sensorZ >= FLAT_SURFACE_MIN_VALUE && sensorZ <= FLAT_SURFACE_MAX_VALUE){
            if(!this.onFlatSurfaceStartUp){
                this.velX = 0.0f;
                this.velY = 0.0f;

                this.xPosCircle += this.velX;
                this.yPosCircle += this.velY;
            } else {
                this.xPosCircle += this.velX * DELTA;
                this.yPosCircle += this.velY * DELTA;
            }
        } else {
            this.onFlatSurfaceStartUp = true;
            this.xPosCircle += this.velX * DELTA ;
            this.yPosCircle += this.velY * DELTA ;
        }

       checkCollision();

        // Vibrate and play sound once per collision
        if(this.hasCollided){
            if(!this.hasVibrated) {
                pingSound();
                vibrate();
                this.hasVibrated = true;
            }
        }
    }

    /**
     * Check when and where Circle collides with frame and adds bounce
     */
    private void checkCollision() {
        if (this.xPosCircle <= this.circleRadius + this.FRAME_MARGIN) {
            this.xPosCircle =  this.circleRadius + this.FRAME_MARGIN; // LEFT
            this.velX = -velX * DRAG;
           // this.velY = -velY * DRAG;
            hasCollided = true;

            checkCollisionCorner();
        }
        else if (this.xPosCircle >= this.screenSizeWidth - this.circleRadius - this.FRAME_MARGIN) {
            this.xPosCircle = this.screenSizeWidth - this.circleRadius - this.FRAME_MARGIN; // RIGHT
            this.velX = -velX * DRAG;
            //this.velY = -velY * DRAG;
            hasCollided = true;

            checkCollisionCorner();
        }
        else if (this.yPosCircle <= this.circleRadius + this.FRAME_MARGIN) {
            this.yPosCircle = this.circleRadius + this.FRAME_MARGIN; // TOP
            this.velY = -velY * DRAG;
          //  this.velX = -velX * DRAG;
            hasCollided = true;
        }
        else if (this.yPosCircle >= this.screenSizeHeight - this.circleRadius - this.FRAME_MARGIN) {
            this.yPosCircle = this.screenSizeHeight - this.circleRadius - this.FRAME_MARGIN; // BOTTOM
            this.velY = -velY * DRAG;
           // this.velX = -velX * DRAG;
            hasCollided = true;
        } else {
            this.hasCollided = false;
            this.hasVibrated = false;
        }
    }

    /**
     * Check collision for top and bottom, used to check when colliding with left
     * or right and also colliding with top or bottom
     */
    private void checkCollisionCorner() {
        // Had some problems with the corners, this fixed it.
        if (this.yPosCircle <= this.circleRadius + this.FRAME_MARGIN) {
            this.yPosCircle = this.circleRadius + this.FRAME_MARGIN; // TOP
            this.velY = -velY * DRAG;
        }

        if (this.yPosCircle >= this.screenSizeHeight - this.circleRadius - this.FRAME_MARGIN) {
            this.yPosCircle = this.screenSizeHeight - this.circleRadius - this.FRAME_MARGIN; // BOTTOM
            this.velY = -velY * DRAG;
        }
    }


    /**
     * Executes a new runnable to play a short sound
     */
    private void pingSound(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
            }
        });
    }


    /**
     * Executes a new runnable to vibrate
     * Looked at https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
     */
    private void vibrate(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mVibrator.vibrate(100);
            }
        });
    }

    /**
     * Draws Frame and Circle
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(this.mFrame,this.mRectPaint);
        canvas.drawCircle(this.xPosCircle, this.yPosCircle,this.circleRadius,this.mCirclePaint);

        invalidate();
    }

}