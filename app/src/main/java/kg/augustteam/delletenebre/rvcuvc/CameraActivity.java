package kg.augustteam.delletenebre.rvcuvc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends Activity {
    private final String TAG = getClass().getName();
    private APP mAPP;

    private SharedPreferences _settings;
    private FrameLayout mMainLayout;
    private RelativeLayout layoutParkingSensors;

    // for thread pool
    private static final int CORE_POOL_SIZE = 1;		// initial/minimum threads
    private static final int MAX_POOL_SIZE = 4;			// maximum threads
    private static final int KEEP_ALIVE_TIME = 10;		// time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private UVCCameraView mUVCCameraView;
    // for open&start / stop&close camera preview
    private Surface mPreviewSurface;


    ImageView carImageView;
    ParkingSensorsView parkingSensorsView;


    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("suicide", false)) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mAPP = APP.getInstance();
        mAPP.setCameraActivity(this);

        _settings = PreferenceManager.getDefaultSharedPreferences(this);

        mUVCCameraView = (UVCCameraView)findViewById(R.id.UVCCameraView);
        mUVCCameraView.setAspectRatio(
                UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        layoutParkingSensors = (RelativeLayout) findViewById(R.id.parking_sensors_layout);
        parkingSensorsView = (ParkingSensorsView) findViewById(R.id.parkingSensors);
        carImageView = (ImageView) findViewById(R.id.carView);
        carImageView.post(new Runnable() {
            @Override
            public void run() {
                parkingSensorsView.setCarSize(carImageView.getMeasuredWidth(),
                        carImageView.getMeasuredHeight());
            }
        });



        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.register();

        Iterator<UsbDevice> deviceIterator = mUSBMonitor.getDevices();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            mUSBMonitor.requestPermission(device);
            Log.d(TAG, device.getDeviceName() + " " + mUSBMonitor.hasPermission(device));
//            if (mAPP.DEBUG) {
//                Log.d(TAG, device.getDeviceName() + " " + mUSBMonitor.hasPermission(device));
//            }
        }

        mMainLayout = (FrameLayout)findViewById(R.id.main_layout);
        mMainLayout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                settingsIntent.putExtra(
                        PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        SettingsActivity.GeneralPreferenceFragment.class.getName());
                settingsIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                startActivity(settingsIntent);

                return false;
            }
        });

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        String fullscreenMode = _settings.getString("fullscreen", "hide_navigation_bar");
        if(fullscreenMode.equals("hide_navigation_bar")) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        } else if(fullscreenMode.equals("hide_title_bar")) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

    }



    @Override
    protected void onResume() {
        super.onResume();

        mUSBMonitor.register();
        if (mUVCCamera != null) {
            mUVCCamera.startPreview();
        }


        if ( _settings.getBoolean("ps_enable", false) ) {
            layoutParkingSensors.setVisibility(RelativeLayout.VISIBLE);

            String position = _settings.getString("ps_position", "left");
            int gravityPosition = Gravity.LEFT;

            switch ( position ) {
                case "left":
                    gravityPosition = Gravity.LEFT;
                    break;
                case "center":
                    gravityPosition = Gravity.CENTER;
                    Log.d(TAG, "center");
                    break;
                case "right":
                    gravityPosition = Gravity.RIGHT;
                    Log.d(TAG, "right");
                    break;
            }
            ((FrameLayout.LayoutParams) layoutParkingSensors.getLayoutParams()).gravity = gravityPosition;



        } else {
            layoutParkingSensors.setVisibility(RelativeLayout.GONE);
        }

    }

    @Override
    public void onPause() {
        if (mUVCCamera != null) {
            mUVCCamera.stopPreview();
        }
        mUSBMonitor.unregister();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mUVCCamera != null) {
            mUVCCamera.destroy();
            mUVCCamera = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;

        mAPP.setCameraActivity(null);

        super.onDestroy();
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {}

        @Override
        public void onConnect(final UsbDevice device,
                              final USBMonitor.UsbControlBlock ctrlBlock,
                              final boolean createNew) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
            }
            mUVCCamera = new UVCCamera();
            EXECUTER.execute(new Runnable() {
                @Override
                public void run() {
                    mUVCCamera.open(ctrlBlock);
//					mUVCCamera.setPreviewTexture(mUVCCameraView.getSurfaceTexture());
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }

                    try {
                        mUVCCamera.setPreviewSize(
                                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                                UVCCamera.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        // fallback to YUV mode
                        try {
                            mUVCCamera.setPreviewSize(
                                    UVCCamera.DEFAULT_PREVIEW_WIDTH,
                                    UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                                    UVCCamera.DEFAULT_PREVIEW_MODE);
                        } catch (final IllegalArgumentException e1) {
                            mUVCCamera.destroy();
                            mUVCCamera = null;
                        }
                    }
                    if (mUVCCamera != null) {
                        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                        if (st != null) {
                            mPreviewSurface = new Surface(st);
                        }
                        mUVCCamera.setPreviewDisplay(mPreviewSurface);
                        mUVCCamera.startPreview();
                    }
                }
            });
        }

        @Override
        public void onDisconnect(final UsbDevice device,
                                 final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            if (mUVCCamera != null) {
                mUVCCamera.close();
                if (mPreviewSurface != null) {
                    mPreviewSurface.release();
                    mPreviewSurface = null;
                }
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {}

        @Override
        public void onCancel() {}
    };
}
