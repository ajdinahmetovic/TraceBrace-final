package tracebrace.tracebrace_fin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.List;


public class CodeScanner extends Fragment {
    final int RequestCameraPermissionID = 1001;
    CameraSource cameraSource;
    SurfaceView cameraPreview;
    BarcodeDetector barcodeDetector;
    CardView container;
    TinyDB localDb;


    WifiManager wifiManager;
    List<ScanResult> scanResults;

    FragmentManager manager;
    FragmentTransaction transaction;

    public CodeScanner() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        getActivity().recreate();
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_code_scanner, container, false);
        manager = getFragmentManager();

        localDb = new TinyDB(getContext());

        final Intent intent = new Intent(getContext(), MessagesActivity.class);
/*
        cameraPreview = (SurfaceView) view.findViewById(R.id.cameraPreview);

        barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();




        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.CAMERA},RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if(qrcodes.size() != 0) {
                    long[] vibratePattern = {0,100,100};
                    Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(vibratePattern,-1);

                    //System.out.println(qrcodes.valueAt(0).displayValue);
                    if(qrcodes.valueAt(0).displayValue.length()==17) {
                        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                       // sharedPref.edit().putString("ID",qrcodes.valueAt(0).displayValue).commit();
                        System.out.println(qrcodes.valueAt(0).displayValue);
                        localDb.putString("macAddr", qrcodes.valueAt(0).displayValue);
                        vibrator.vibrate(1000);
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.frame, new OnBoarding_second()).commit();


                       // startActivity(intent);

                        frame.removeAllViews();
                        frame.addView(sucess);

                    }

                }
            }
        });
*/

        wifiManager = (WifiManager)  getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        System.out.println("KUGLAAA");

            new Thread(new Runnable() {
                @Override
                public void run() {

                    System.out.println("KUGLAAA");
                    boolean a = true;

                    while (a) {

                        //System.out.println("SCANN");
                        wifiManager.startScan();
                        scanResults = wifiManager.getScanResults();

                        for (int  i = 0;i<scanResults.size();i++){
                            System.out.println(scanResults.get(i).SSID);
                            if(scanResults.get(i).SSID.equals("traceBrace")){
                                System.out.println("NASOOOOO");
                               // Toast.makeText(getContext(), "TraceBrace is connected", Toast.LENGTH_LONG).show();

                                a = false;
                                localDb.putString("macAddr", scanResults.get(i).BSSID);
                                localDb.putBoolean("justConnected", true);
                                startActivity(intent);


                            }

                        }

                    }
                }
            }).start();
        return view;
    }
}
