package tracebrace.tracebrace_final;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;

import java.util.List;


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
                longitude = location.getLongitude();
                latitude = location.getLatitude();
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
        System.out.println("I am tracking");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){

                    //System.out.println("I am tracking");
                    manager.startScan();
                    scanResults = manager.getScanResults();

                    for(int i = 0;i<scanResults.size();i++){
                        System.out.println(macAddr);
                       // System.out.println(scanResults.get(i).BSSID);

                        if(scanResults.get(i).BSSID.equals(macAddr)){
                            System.out.println("DETECTED");
                            System.out.println("https://www.google.com/maps/?q="+latitude+","+longitude);

                            if(localDb.getInt("messageCount")!=0){


                                for(int j  = 0;j<localDb.getInt("messageCount");j++){
                                    smsManager.sendTextMessage(localDb.getListString("numbers").get(j), null,localDb.getListString("messages").get(j) + '\n' + "Lokacija: "+"https://www.google.com/maps/?q="+latitude+","+longitude, null, null );
                                }

                            }


                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            try {
                                Thread.sleep(60000);
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
        // TODO Auto-generated method stub
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
