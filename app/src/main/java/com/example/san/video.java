package com.example.san;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.san.MyUtils.rotateAtRelativeAngle;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import static com.example.san.MyUtils.concludeSpeak;
import static com.example.san.MyUtils.sleepy;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.EmotionsType;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.beans.headmotion.LocateAbsoluteAngleHeadMotion;
import com.sanbot.opensdk.function.beans.headmotion.RelativeAngleHeadMotion;
import com.sanbot.opensdk.function.beans.wheelmotion.DistanceWheelMotion;
import com.sanbot.opensdk.function.beans.wheelmotion.NoAngleWheelMotion;
import com.sanbot.opensdk.function.beans.wing.AbsoluteAngleWingMotion;
import com.sanbot.opensdk.function.beans.wing.NoAngleWingMotion;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.HeadMotionManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;
import com.sanbot.opensdk.function.unit.interfaces.hardware.ObstacleListener;
import com.sanbot.opensdk.function.unit.interfaces.hardware.PIRListener;
import com.sanbot.opensdk.function.unit.interfaces.speech.WakenListener;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class  video extends TopBaseActivity {
    private final static String TAG = "DIL-SPLASH";

    public static boolean busy = false;

    @BindView(R.id.videoView)
    VideoView videoView;



    private ModularMotionManager modularMotionManager; //wander
    private HardWareManager hardWareManager;
    private WheelMotionManager wheelMotionManager;
    private SpeechManager speechManager; //voice, speechRec
    private SystemManager systemManager; //emotions
    private HeadMotionManager headMotionManager;    //head movements
    private WingMotionManager wingMotionManager;

    public static boolean find=false;

    public static boolean available1=true;

    private Handler delayHandler = new Handler();


    NoAngleWingMotion noAngleWingMotionUP = new NoAngleWingMotion(NoAngleWingMotion.PART_RIGHT, 5, NoAngleWingMotion.ACTION_UP);
    NoAngleWingMotion noAngleWingMotionDOWN = new NoAngleWingMotion(NoAngleWingMotion.PART_RIGHT, 5, NoAngleWingMotion.ACTION_DOWN);
    NoAngleWingMotion noAngleWingMotionSTOP = new NoAngleWingMotion(NoAngleWingMotion.PART_RIGHT, 5, NoAngleWingMotion.ACTION_STOP);


    //head motion
    LocateAbsoluteAngleHeadMotion locateAbsoluteAngleHeadMotion = new LocateAbsoluteAngleHeadMotion(
            LocateAbsoluteAngleHeadMotion.ACTION_VERTICAL_LOCK,90,30
    );
    RelativeAngleHeadMotion relativeHeadMotionDOWN = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_DOWN, 30);
    RelativeAngleHeadMotion relativeAngleHeadMotionLeft = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_LEFT,10);
    RelativeAngleHeadMotion relativeAngleHeadMotionRight = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_RIGHT,10);





    //hands movements
    //head motion

    DistanceWheelMotion distanceWheelMotionforward= new DistanceWheelMotion(DistanceWheelMotion.ACTION_FORWARD_RUN,4,800);
    DistanceWheelMotion distanceWheelMotionfright= new DistanceWheelMotion(DistanceWheelMotion.ACTION_RIGHT_FORWARD_RUN,5,500);
    DistanceWheelMotion distanceWheelMotionfleft= new DistanceWheelMotion(DistanceWheelMotion.ACTION_LEFT_FORWARD_RUN,5,500);
    DistanceWheelMotion distanceWheelMotionStop= new DistanceWheelMotion(DistanceWheelMotion.ACTION_STOP_RUN,5,100);

    NoAngleWheelMotion noAngleWheelMotionleft=new NoAngleWheelMotion(NoAngleWheelMotion.ACTION_LEFT_FORWARD, 5,5000 );
    NoAngleWheelMotion noAngleWheelMotionRight=new NoAngleWheelMotion(NoAngleWheelMotion.ACTION_RIGHT_FORWARD, 5,500);


      @Override
    protected void onCreate(Bundle savedInstanceState) {


        try {
            register(ChoiceActivity.class);
            //screen always on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_video);
            ButterKnife.bind(this);
            speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
            systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
            modularMotionManager = (ModularMotionManager) getUnitManager(FuncConstant.MODULARMOTION_MANAGER);
            hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
            wheelMotionManager = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);
            headMotionManager = (HeadMotionManager) getUnitManager(FuncConstant.HEADMOTION_MANAGER);
            wingMotionManager = (WingMotionManager) getUnitManager(FuncConstant.WINGMOTION_MANAGER);
            //float button of the system
            systemManager.switchFloatBar(true, getClass().getName());

            //check app permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 12);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{INTERNET}, 12);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 12);

            }

            //LOAD handshakes stats
            MySettings.initializeXML();
            MySettings.loadHandshakes();

            handmotions();
            //initialize speak
            MySettings.initializeSpeak();

            available1=true;
            Thread objBgThread=new Thread(objrunnable);
            objBgThread.start();



            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video;
            Uri uri = Uri.parse(videoPath);
            videoView.setVideoURI(uri);

            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

            // Set an OnCompletionListener to start ChoiceActivity after video completion
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finish();
                    Intent intent = new Intent(video.this, ChoiceActivity.class);
                    startActivity(intent);
                }
            });

            // Start the video playback
            videoView.start();



        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();

        }


    }



    public void handmotions(){


        //motion without angle
        wingMotionManager.doNoAngleMotion(noAngleWingMotionUP);
        sleepy(0.5);
        wingMotionManager.doNoAngleMotion(noAngleWingMotionDOWN);
        sleepy(0.5);
        wingMotionManager.doNoAngleMotion(noAngleWingMotionUP);
        sleepy(0.5);
        wingMotionManager.doNoAngleMotion(noAngleWingMotionDOWN);
        sleepy(0.5);
        wingMotionManager.doNoAngleMotion(noAngleWingMotionUP);
        sleepy(0.5);
        wingMotionManager.doNoAngleMotion(noAngleWingMotionDOWN);



        //hands down (reset position)
        wingMotionManager.doNoAngleMotion(new NoAngleWingMotion(NoAngleWingMotion.PART_BOTH, 5,NoAngleWingMotion.ACTION_RESET));

        //back rotation
        rotateAtRelativeAngle(wheelMotionManager, 10);
        //rotate back head
        headMotionManager.doRelativeAngleMotion(relativeAngleHeadMotionLeft);
    }
    @Override
    protected void onMainServiceConnected() {

    }

    Runnable objrunnable=new Runnable() {
        @Override
        public void run() {

                hardWareManager.setOnHareWareListener(new ObstacleListener() {
                    @Override
                    public void onObstacleStatus(boolean b) {
                        if (!b && available1==true) {
                            Toast.makeText(video.this, "No One here", Toast.LENGTH_SHORT).show();
                            available1=false;
                            Intent intent = new Intent(video.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            systemManager.showEmotion(EmotionsType.SPEAK);
                            hardWareManager.setLED(new LED(LED.PART_ALL, LED.MODE_PINK));

                        }
                    }
                });
            }


    };



}