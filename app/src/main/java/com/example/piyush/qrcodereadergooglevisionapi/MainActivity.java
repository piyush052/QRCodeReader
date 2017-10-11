package com.example.piyush.qrcodereadergooglevisionapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private  final String TAG = getClass().getSimpleName();
    SurfaceView mCameraView;
    BarcodeDetector mBarcodeDetector;
    CameraSource mCameraSource;
    SurfaceHolder mSurfaceHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.cameraView);
        mCameraView.setZOrderMediaOverlay(true);
        mSurfaceHolder = mCameraView.getHolder();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // check for google play service
        if(isGooglePlayServicesAvailable(MainActivity.this)) {
            // always setup in onResume only
            setup();
        }else{
            // take user to installation for google play services..
            Log.e(TAG, "onResume: " );
        }
    }

    private void setup() {
        mBarcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if(!mBarcodeDetector.isOperational()){
            Toast.makeText(getApplicationContext(), "Couldn't setup the detector", Toast.LENGTH_LONG).show();
            this.finish();
        }
        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1920,1024)
                .build();
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        mCameraSource.start(mCameraView.getHolder());
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Please grant camera permission ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes =  detections.getDetectedItems();
                if(barcodes.size() > 0){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this,  barcodes.valueAt(0).displayValue, Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraSource.release();
    }

    public boolean isGooglePlayServicesAvailable(MainActivity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
