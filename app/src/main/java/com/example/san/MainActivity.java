package com.example.san;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.san.MyUtils.rotateAtRelativeAngle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sanbot.opensdk.function.beans.wing.RelativeAngleWingMotion;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.HeadMotionManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;
import com.sanbot.opensdk.function.unit.interfaces.hardware.InfrareListener;
import com.sanbot.opensdk.function.unit.interfaces.hardware.ObstacleListener;
import com.sanbot.opensdk.function.unit.interfaces.hardware.PIRListener;
import com.sanbot.opensdk.function.unit.interfaces.speech.WakenListener;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifImageView;

public class  MainActivity extends TopBaseActivity {
    private final static String TAG = "DIL-SPLASH";

    public static boolean busy = false;

    public static boolean detect=false;
    public static boolean irdetect=false;

    public static boolean pirdetect=false;

    @BindView(R.id.exitButton)
    Button exitButton;

    @BindView(R.id.handle)
    Button slider;

    @BindView(R.id.welcomeimg)
    ImageView welcome;

    @BindView(R.id.welcome)
    ImageView welcomeimg2;

    private ModularMotionManager modularMotionManager; //wander
    private HardWareManager hardWareManager;
    private WheelMotionManager wheelMotionManager;
    private SpeechManager speechManager; //voice, speechRec
    private SystemManager systemManager; //emotions
    private HeadMotionManager headMotionManager;    //head movements
    private WingMotionManager wingMotionManager;

    public static boolean find=false;
    public static boolean obstacle=false;

    public static boolean obstacle2=false;
    public static boolean slide=false;
    private MediaPlayer mediaPlayer;
    private Handler delayHandler = new Handler();

    private byte handAb = AbsoluteAngleWingMotion.PART_RIGHT;
    private byte handRe = RelativeAngleWingMotion.PART_RIGHT;

    //hands movements
    //head motion
    LocateAbsoluteAngleHeadMotion locateAbsoluteAngleHeadMotion = new LocateAbsoluteAngleHeadMotion(
            LocateAbsoluteAngleHeadMotion.ACTION_VERTICAL_LOCK,90,30
    );

    DistanceWheelMotion distanceWheelMotionforward= new DistanceWheelMotion(DistanceWheelMotion.ACTION_FORWARD_RUN,4,200);
    DistanceWheelMotion distanceWheelMotionfright= new DistanceWheelMotion(DistanceWheelMotion.ACTION_RIGHT_FORWARD_RUN,5,500);
    DistanceWheelMotion distanceWheelMotionfleft= new DistanceWheelMotion(DistanceWheelMotion.ACTION_LEFT_FORWARD_RUN,5,500);
    DistanceWheelMotion distanceWheelMotionStop= new DistanceWheelMotion(DistanceWheelMotion.ACTION_STOP_RUN,5,100);

    NoAngleWheelMotion noAngleWheelMotionleft=new NoAngleWheelMotion(NoAngleWheelMotion.ACTION_LEFT_FORWARD, 5,5000 );
    NoAngleWheelMotion noAngleWheelMotionRight=new NoAngleWheelMotion(NoAngleWheelMotion.ACTION_RIGHT_FORWARD, 5,500);


    RelativeAngleHeadMotion relativeHeadMotionDOWN = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_DOWN, 30);
    RelativeAngleHeadMotion relativeAngleHeadMotionLeft = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_LEFT,10);
    RelativeAngleHeadMotion relativeAngleHeadMotionRight = new RelativeAngleHeadMotion(RelativeAngleHeadMotion.ACTION_RIGHT,10);


    SlidingDrawer simpleSlidingDrawer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        try {
            register(ChoiceActivity.class);
            //screen always on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
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
            simpleSlidingDrawer=(SlidingDrawer) findViewById(R.id.slidingdrawer);

            mediaPlayer = MediaPlayer.create(this, R.raw.welcome);

            welcome.setVisibility(View.INVISIBLE);

            welcomeimg2.setVisibility(View.VISIBLE);
            detect=false;
            slide=false;
            irdetect=false;
            obstacle=false;
            pirdetect=false;
            obstacle2=false;

            busy = false;
            //LOAD handshakes stats
            MySettings.initializeXML();
            MySettings.loadHandshakes();

            //initialize speak
            MySettings.initializeSpeak();


            GifImageView gifImageView = findViewById(R.id.gifImageView);
            gifImageView.setImageResource(R.drawable.welcome);



            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                @OnClick(R.id.exitButton)
                public void onClick(View view) {
                    wanderOffNow();
                    finishAffinity();
                    System.exit(0);
                }
            });

            slider.setOnClickListener(new View.OnClickListener() {
                @Override
                @OnClick(R.id.handle)
                public void onClick(View view) {
                    if(slide==false){
                        simpleSlidingDrawer.animateOpen();
                        slide=true;
                    }
                    else {
                        simpleSlidingDrawer.animateClose();
                        slide=false;
                    }
                }
            });

            initHardwareListeners();

            //wanderOnNow();
            //initHardwareListeners();
            //initialize body

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //hands down
                    AbsoluteAngleWingMotion absoluteAngleWingMotion = new AbsoluteAngleWingMotion(AbsoluteAngleWingMotion.PART_BOTH, 8, 180);
                    wingMotionManager.doAbsoluteAngleMotion(absoluteAngleWingMotion);
                    //head up
                    headMotionManager.doAbsoluteLocateMotion(locateAbsoluteAngleHeadMotion);
                    //initially sets the wander to on
                }
            }, 1000);


        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();

        }


    }
    @Override
    protected void onMainServiceConnected() {

    }
    public void wanderOnNow() {
        Toast.makeText(MainActivity.this, "Wander is calling " + MySettings.isWanderAllowed()+" now", Toast.LENGTH_SHORT).show();
        if (!busy) {
            MySettings.setWanderAllowed(true);
            Toast.makeText(MainActivity.this, "Wander " + MySettings.isWanderAllowed()+" now", Toast.LENGTH_SHORT).show();
            modularMotionManager.switchWander(MySettings.isWanderAllowed());
            Log.i(TAG, "Wander " + MySettings.isWanderAllowed() + " now");
        }
    }
    public void wanderOffNow() {
        MySettings.setWanderAllowed(false);
        Toast.makeText(MainActivity.this, "Wander off now", Toast.LENGTH_SHORT).show();
        modularMotionManager.switchWander(false);
        Log.i(TAG, "Wander forced off now");
    }



    public void initobstaclelistner(){
        if (obstacle==false) {

            hardWareManager.setOnHareWareListener(new ObstacleListener() {
                    @Override
                    public void onObstacleStatus ( boolean b){
                    if (b && detect == false) {
                       // wanderOffNow();
                        welcome.setVisibility(View.VISIBLE);
                        welcomeimg2.setVisibility(View.INVISIBLE);
                        mediaPlayer.start();
                        //hand up
                        AbsoluteAngleWingMotion absoluteAngleWingMotion = new AbsoluteAngleWingMotion(handAb, 5, 70);
                        wingMotionManager.doAbsoluteAngleMotion(absoluteAngleWingMotion);
                        //rotate body
                        rotateAtRelativeAngle(wheelMotionManager, 350);
                        //rotate head
                        headMotionManager.doRelativeAngleMotion(relativeAngleHeadMotionRight);

                        obstacle = true;
                        detect = true;

                        Toast.makeText(MainActivity.this, "Smiling", Toast.LENGTH_SHORT).show();
                        systemManager.showEmotion(EmotionsType.SMILE);
                        //say hi


                        pirdetect = true;


                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    // wanderOffNow();
                                    Intent intent = new Intent(MainActivity.this, HandShake.class);
                                    startActivity(intent);
                                    mediaPlayer.stop();
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }, 4000);


                    } else {
                    }
                }

            });
            }



    }




    private void initHardwareListeners() {

        hardWareManager.setOnHareWareListener(new PIRListener() {
            @Override
            public void onPIRCheckResult(boolean isCheck, int part) {

                if (pirdetect == false) {


                    if (part == 1 && isCheck == true) {

                        welcome.setVisibility(View.VISIBLE);

                        //wanderOffNow();

                        //starts greeting with this person passing
                        busy = true;
                        Toast.makeText(MainActivity.this, "Smiling", Toast.LENGTH_SHORT).show();
                        systemManager.showEmotion(EmotionsType.SMILE);
                        //say hi

                        mediaPlayer.start();


                        pirdetect=true;


                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    Intent intent = new Intent(MainActivity.this, HandShake.class);
                                    startActivity(intent);
                                    mediaPlayer.stop();
                                    finish();
                                }catch(Exception e){
                                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }, 3000);

                    } else if (part != 1 && isCheck == true) {

                        welcome.setVisibility(View.VISIBLE);
                        wanderOffNow();
                        Toast.makeText(getApplicationContext(), "you are behind me", Toast.LENGTH_LONG).show();
                        //if it's the back PIR
                        Log.i(TAG, "PIR back triggered -> rotating");
                        MySettings.setSoundRotationAllowed(true);

                        mediaPlayer.start();
                        //flicker led
                        hardWareManager.setLED(new LED(LED.PART_ALL, LED.MODE_FLICKER_RED));
                        //rotate at angle
                        rotateAtRelativeAngle(wheelMotionManager, 180);

                        //starts greeting with this person passing
                        busy = true;
                        Toast.makeText(MainActivity.this, "Smiling", Toast.LENGTH_SHORT).show();
                        systemManager.showEmotion(EmotionsType.SMILE);
                        //say hi

                        pirdetect=true;

                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent intent = new Intent(MainActivity.this, HandShake.class);
                                    startActivity(intent);
                                    mediaPlayer.stop();
                                    finish();
                                }catch(Exception e){
                                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 3000);


                    } else {
                        wanderOnNow();
                        systemManager.showEmotion(EmotionsType.WHISTLE);


                    }
                }
            }
        });

    }



    public void distanceForward(){
        wheelMotionManager.doDistanceMotion(distanceWheelMotionforward);
        sleepy(0.5);
    }

    public void right(){
        wheelMotionManager.doNoAngleMotion(noAngleWheelMotionRight);
        sleepy(0.5);
    }

    public void left(){
        wheelMotionManager.doNoAngleMotion(noAngleWheelMotionleft);
        sleepy(0.5);
    }

    public void distanceStop(){
        wheelMotionManager.doDistanceMotion(distanceWheelMotionStop);
        sleepy(0.5);

    }


    private class WheelMotionAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // Run your wheel moving methods one after another here
            right();
            left();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // This method is called after doInBackground has completed
            // You can start the next method or perform other actions here
        }
    }


    public void obstacleStatusForward() {
        if (!find) {
            find=true;
            hardWareManager.setOnHareWareListener(new ObstacleListener() {
                @Override
                public void onObstacleStatus(boolean b) {
                    if (b) {
                        Toast.makeText(MainActivity.this, "Obstacle detected", Toast.LENGTH_SHORT).show();
                        distanceStop();
                        systemManager.showEmotion(EmotionsType.WHISTLE);
                        hardWareManager.setLED(new LED(LED.PART_ALL, LED.MODE_BLUE));
                    } else {
                        distanceForward();
                    }
                }
            });
        }
    }



    public void speech(){
        speechManager.setOnSpeechListener(new WakenListener() {
            @Override
            public void onWakeUp() {

            }

            @Override
            public void onSleep() {

            }

            @Override
            public void onWakeUpStatus(boolean b) {

            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the PIR sensor listener when the activity is destroyed
        hardWareManager.setOnHareWareListener(null);
    }


    public void initirlistner(){

        if(!irdetect) {
            hardWareManager.setOnHareWareListener(new InfrareListener() {
                @Override
                public void infrareDistance(int i, int i1) {
                    if (!irdetect) {
                        if (i == 11 || i == 12 || i == 15 || i == 13 || i == 17) {
                            if (i1 < 40) {

                                wanderOffNow();

                                //starts greeting with this person passing
                                busy = true;
                                Toast.makeText(MainActivity.this, "Smiling", Toast.LENGTH_SHORT).show();
                                systemManager.showEmotion(EmotionsType.SMILE);
                                //say hi

                                speechManager.startSpeak(getString(R.string.Welcome_to_Techno_2023_We_are_glad_that_you_are_here), MySettings.getSpeakDefaultOption());
                                concludeSpeak(speechManager);


                                // 50% say Good morning/afternoon/ecc...
                                double random_num = Math.random();
                                Log.i(TAG, "Random = " + random_num);
                                if (random_num < 0.5) {
                                    int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                    if (hours < 6) {
                                        speechManager.startSpeak(getString(R.string.Top_of_the_morning_to_you), MySettings.getSpeakDefaultOption());
                                    } else if (hours < 12) {
                                        speechManager.startSpeak(getString(R.string.Good_Morning), MySettings.getSpeakDefaultOption());
                                    } else if (hours < 16) {
                                        speechManager.startSpeak(getString(R.string.Good_Afternoon), MySettings.getSpeakDefaultOption());
                                    } else if (hours < 19) {
                                        speechManager.startSpeak(getString(R.string.Good_Evening), MySettings.getSpeakDefaultOption());
                                    }
                                    concludeSpeak(speechManager);
                                }

                                irdetect = true;
                                Intent intent = new Intent(MainActivity.this, HandShake.class);
                                startActivity(intent);
                                finish();
                                hardWareManager.setOnHareWareListener(null);

                            } else {
                                wanderOnNow();
                                systemManager.showEmotion(EmotionsType.WHISTLE);

                            }
                        } else {
                            wanderOnNow();
                            systemManager.showEmotion(EmotionsType.WHISTLE);

                        }

                    }
                }
            });

        }

    }


}