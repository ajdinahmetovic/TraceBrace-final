package tracebrace.tracebrace_fin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {



    WifiManager wifiManager;
    List<ScanResult> scanResults;


    ScanResult traceBrace;

    boolean detected;

    ProgressBar sync;


    View []views = new View[4];
    int currIndex = 0;
    TabLayout.Tab tab;
    boolean braceGone;
    TabLayout tabDots;
    FragmentManager manager;
    FragmentTransaction transaction;
    private Boolean firstTime = null;
    TinyDB localDb;

    TextView syncMsg;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localDb=new TinyDB(getApplicationContext());
        isFirstTime();



        if(!localDb.getBoolean("conf")) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }

        if(!firstTime && !localDb.getString("macAddr").isEmpty()){
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
            finish();
        } else if(localDb.getBoolean("conf")) {





            sync = findViewById(R.id.sync);
            sync.setVisibility(View.GONE);
            final Intent intent2 = new Intent(this, MessagesActivity.class);

            final WifiManager manager;

            final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "In order to use TraceBrace app, it is neccessary to allow the use of Location services ! ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }


            manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            scanResults = manager.getScanResults();




            final BroadcastReceiver connectionReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                        scanResults = manager.getScanResults();


                        for(int i = 0;i<scanResults.size();i++){
                            //System.out.println(macAddr);
                            System.out.println(scanResults.get(i).SSID);
                            if(scanResults.get(i).SSID.equals("traceBrace")){
                                localDb.putString("macAddr", scanResults.get(i).BSSID);
                                sync.setVisibility(View.VISIBLE);
                                System.out.println("Barce is saved");
                                syncMsg.setText(R.string.syncMsg);
                                traceBrace = scanResults.get(i);
                                // startActivity(intent2);
                                detected = true;


                            }
                        }


                        if(braceGone) {
                            sync.setVisibility(View.GONE);
                            // unregisterReceiver(connectionReciever);
                            System.out.println("Brace is gone");
                            detected = false;
                            startActivity(intent2);
                            finish();
                        }





                    }
                }
            };




            new Thread(new Runnable() {
                @Override
                public void run() {

                    detected = false;




                    try{
                        unregisterReceiver(connectionReciever);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    try {
                        registerReceiver(connectionReciever,
                                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                    while (!detected){

                        System.out.println("I am tracking");
                        manager.startScan();

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }


                    while (detected){


                        braceGone = false;
                        System.out.println("Connecting");
                        manager.startScan();

                        int f = 0;

                        for(int i = 0;i<scanResults.size();i++){

                            //System.out.println(macAddr);
                            // System.out.println(scanResults.get(i).SSID);

                            if(!(scanResults.get(i).SSID.equals("traceBrace"))){
                                f++;
                            }

                        }

                        System.out.println(f);
                        System.out.println(scanResults.size());

                        if(f == scanResults.size()){
                            System.out.println("DONE");
                            braceGone = true;
                            detected = false;
                        }


                    }
                }
            }).start();


        }
        final FrameLayout frameLayout = findViewById(R.id.frame);


        final View onboardingFirst =  LayoutInflater.from(this).inflate(R.layout.onboarding_first, null);
        final View onboardingSecond =  LayoutInflater.from(this).inflate(R.layout.onboarding_second, null);
        final View onboardingThird = LayoutInflater.from(this).inflate(R.layout.onboarding_third, null);
        final View onboardingFour = LayoutInflater.from(this).inflate(R.layout.onboarding_four, null);


        syncMsg = onboardingFour.findViewById(R.id.syncMsg);

        views[0] = onboardingFirst;
        views[1] = onboardingSecond;
        views[2] = onboardingThird;
        views[3] = onboardingFour;

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
                if(currIndex<4){
                    currIndex++;

                }
                if(currIndex<views.length){
                    views[currIndex-1].animate();
                    frameLayout.removeAllViews();
                    frameLayout.addView(views[currIndex]);
                    //Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rtol);
                    //views[currIndex].startAnimation(animation);
                } /* else if(currIndex == 3){
                    frameLayout.removeAllViews();
                    transaction = manager.beginTransaction();
                    transaction.replace(R.id.frame, new CodeScanner()).commit();
                }
                */
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
                base.putBoolean("conf", false);
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
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        localDb.putBoolean("conf", true);

        recreate();


    }

    private void turnGPSOn()
    {

            final Intent enablegps = new Intent();
            enablegps.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            enablegps.addCategory(Intent.CATEGORY_ALTERNATIVE);
            enablegps.setData(Uri.parse("3"));
            sendBroadcast(enablegps);

    }


}
