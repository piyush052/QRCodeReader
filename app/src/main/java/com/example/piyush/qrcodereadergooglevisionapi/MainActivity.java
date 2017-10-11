package com.example.piyush.qrcodereadergooglevisionapi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_FOR_CAMERA = 101;
    private  final String TAG = getClass().getSimpleName();
    SurfaceView mCameraView;
    BarcodeDetector mBarcodeDetector;
    CameraSource mCameraSource;
    SurfaceHolder mSurfaceHolder;
    TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.cameraView);
        mCameraView.setZOrderMediaOverlay(true);
        mSurfaceHolder = mCameraView.getHolder();
        mTextView=findViewById(R.id.textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
           initialSetUp();
        }else{
//            take run time permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_FOR_CAMERA);
        }
    }

    private void initialSetUp() {
        // check for google play service
        if (isGooglePlayServicesAvailable(MainActivity.this)) {
            // always finalSetup in onResume only
            finalSetup();
        } else {
            // take user to installation for google play services..
            Log.e(TAG, "onResume: ");
        }
    }

    private void finalSetup() {
        mBarcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if(!mBarcodeDetector.isOperational()){
            Toast.makeText(getApplicationContext(), "Couldn't Setup the detector", Toast.LENGTH_LONG).show();
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
                            mTextView.setText(barcodes.valueAt(0).displayValue);

                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCameraSource!=null)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FOR_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//                    Just refreshing this activity you can bulid your own logic....

                    Intent ss = new Intent(this, MainActivity.class);
                    ss.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    ss.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ss.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    ss.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(ss);

                } else {
                    Toast.makeText(MainActivity.this, "Grant camera permission ", Toast.LENGTH_SHORT).show();
                }

                return;
            }

        }
    }
}
