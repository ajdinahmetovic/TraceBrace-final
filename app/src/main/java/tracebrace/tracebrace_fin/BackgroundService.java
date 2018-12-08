package tracebrace.tracebrace_fin;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BackgroundService extends Service {

    WifiManager manager;
    List<ScanResult> scanResults;

    TinyDB localDb;
    String macAddr;

    SmsManager smsManager;

    double longitude, latitude;

    LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getApplication().getSystemService(LOCATION_SERVICE);

        smsManager = SmsManager.getDefault();

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);


        localDb = new TinyDB(getApplicationContext());

        macAddr = localDb.getString("macAddr");



        manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        scanResults = manager.getScanResults();
        System.out.println("I am tracking");
        System.out.println(isNetworkAvailable());
        new Thread(new Runnable() {
            @Override
            public void run() {



                final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context c, Intent intent) {
                        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                            scanResults = manager.getScanResults();

                        }
                    }
                };


                registerReceiver(mWifiScanReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "In order to use TraceBrace app, it is neccessary to allow the use of Location services ! ", Toast.LENGTH_SHORT).show();

                        }
                    });
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }

                 BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

                            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                                Handler handler = new Handler(Looper.getMainLooper());

                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "In order to use TraceBrace app, it is neccessary to allow the use of Location services ! ", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        }
                    }
                };

                registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

                while (true){

                    System.out.println("I am tracking");
                    //manager.startScan();
                    //scanResults = manager.getScanResults();

                    manager.startScan();

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    for(int i = 0;i<scanResults.size();i++){
                        //System.out.println(macAddr);
                       // System.out.println(scanResults.get(i).BSSID);
                        if(scanResults.get(i).BSSID.equals(macAddr)){
                            System.out.println("DETECTED");
                            System.out.println("https://www.google.com/maps/?q="+latitude+","+longitude);

                            if(localDb.getInt("messageCount")!=0){
                                for(int j  = 0;j<localDb.getInt("messageCount");j++){
                                    smsManager.sendTextMessage(localDb.getListString("numbers").get(j), null,localDb.getListString("messages").get(j) + '\n' + "Lokacija: "+"https://www.google.com/maps/?q="+latitude+","+longitude, null, null );
                                //    System.out.println(buildMessage(j));
                                }

                            }
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            try {
                                Thread.sleep(300000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }
            }
        }).start();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }

    String buildMessage (int id){

        String message;

        TinyDB localDb = new TinyDB(this);
        ArrayList<String> messages = localDb.getListString("messages");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        message = messages.get(id);

        if(sharedPref.getBoolean("gps_allow", false) && !isNetworkAvailable()){
            message += "\n"+getString(R.string.location)+": "+"https://www.google.com/maps/?q="+latitude+","+longitude;
        } else if (sharedPref.getBoolean("live_allow", false) && isNetworkAvailable()){
            UUID uuid = UUID.randomUUID();
            message += "\n"+getString(R.string.location) + "localhost:4200/"+uuid;
        } else if (sharedPref.getBoolean("gps_allow", false) && isNetworkAvailable()){
            UUID uuid = UUID.randomUUID();
            message += "\n"+getString(R.string.location)+": "+"https://www.google.com/maps/?q="+latitude+","+longitude;

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("tracebrace-36ee6");
            ref.setValue(uuid);



        }
        return message;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }









}
