package com.example.san;


import static com.example.san.MyUtils.concludeSpeak;

import android.content.Context;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;
import android.widget.VideoView;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.EmotionsType;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.HeadMotionManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;
import com.sanbot.opensdk.function.unit.interfaces.hardware.ObstacleListener;

import butterknife.BindView;

public class Obstacle implements Runnable {

    @BindView(R.id.videoView)
    VideoView videoView;



    private boolean find;
    private Context context;


    public Obstacle(Context context) {
        this.context=context;

    }

    public static boolean busy = false;
    private SystemManager systemManager; //emotions
    private HardWareManager hardWareManager; //leds //touch sensors //voice locate //gyroscope


    @Override
    public void run() {
        find = true;
        hardWareManager.setOnHareWareListener(new ObstacleListener() {
            @Override
            public void onObstacleStatus(boolean b) {
                if(!b && find==true){
                    Toast.makeText(context, "obstacle listner called", Toast.LENGTH_LONG).show();
                    startAnotherActivity(context);

                }else{
                    find = false;
                }
            }
        });

    }
    public void startAnotherActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

}
