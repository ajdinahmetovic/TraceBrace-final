package tracebrace.tracebrace_final;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    View []views = new View[3];
    int currIndex = 0;
    TabLayout.Tab tab;
    TabLayout tabDots;
    FragmentManager manager;
    FragmentTransaction transaction;
    private Boolean firstTime = null;
    TinyDB localDb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isMyServiceRunning(BackgroundService.class)){
            startService(new Intent(getApplicationContext(), BackgroundService.class));
            System.out.println("Stary");
        }

        final FrameLayout frameLayout = findViewById(R.id.frame);

        localDb=new TinyDB(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //Request permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},1001);

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},1002);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CHANGE_WIFI_STATE},1003);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.VIBRATE},1004);

        }




        isFirstTime();
        if(!firstTime /*&& !localDb.getString("macAddr").isEmpty()*/){
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
        }

        final View onboardingFirst =  LayoutInflater.from(this).inflate(R.layout.onboarding_first, null);
        final View onboardingSecond =  LayoutInflater.from(this).inflate(R.layout.onboarding_second, null);
        final View onboardingThird = LayoutInflater.from(this).inflate(R.layout.onboarding_third, null);

        views[0] = onboardingFirst;
        views[1] = onboardingSecond;
        views[2] = onboardingThird;

        frameLayout.addView(views[0]);


         manager = getSupportFragmentManager();
         transaction = manager.beginTransaction();
        //transaction.replace(R.id.frame, new CodeScanner()).commit();


        //tabDots = findViewById(R.id.tabDots);

        /*
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                frameLayout.addView(onboardingSecond);
            }
        });
*/
        frameLayout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this){
            public void onSwipeLeft() {
                currIndex++;
                if(currIndex<views.length){
                    views[currIndex-1].animate();
                    frameLayout.removeAllViews();
                    frameLayout.addView(views[currIndex]);
                    //Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rtol);
                    //views[currIndex].startAnimation(animation);
                } else if(currIndex == 3){
                    frameLayout.removeAllViews();
                    transaction = manager.beginTransaction();
                    transaction.replace(R.id.frame, new CodeScanner()).commit();
                }
            }
            public void onSwipeRight() {
                if(currIndex>0){
                    currIndex--;
                    frameLayout.removeAllViews();
                    frameLayout.addView(views[currIndex]);
                    //Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ltor);
                    //views[currIndex].startAnimation(animation);
                }
            }

        });



    }




    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                TinyDB base = new TinyDB(this);
                base.putInt("messageCount", 0);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




}
