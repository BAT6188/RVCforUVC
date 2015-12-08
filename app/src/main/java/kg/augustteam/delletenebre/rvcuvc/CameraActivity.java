package kg.augustteam.delletenebre.rvcuvc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
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


    private ImageView carImageView;
    private ParkingSensorsView parkingSensorsView;
    private UsbDevice mUsbDevice;
    private String USBDeviceName;

    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("suicide", false)) {
            finish();
        }
    }

    private FrameLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mAPP = APP.getInstance();
        mAPP.setCameraActivity(this);

        _settings = PreferenceManager.getDefaultSharedPreferences(this);
        USBDeviceName = _settings.getString("usb_device_name", "");


        mUVCCameraView = (UVCCameraView) findViewById(R.id.camera_view);
        mUVCCameraView.setAspectRatio(
                UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mUVCCameraView.setSurfaceTextureListener(mSurfaceTextureListener);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);


        layoutParkingSensors = (RelativeLayout) findViewById(R.id.parking_sensors_layout);
        parkingSensorsView = (ParkingSensorsView) findViewById(R.id.parkingSensors);
        carImageView = (ImageView) findViewById(R.id.carView);

        mAPP.setFrontTextView((TextViewCircle) findViewById(R.id.frontTextView));
        mAPP.setRearTextView((TextViewCircle) findViewById(R.id.rearTextView));

        Iterator<UsbDevice> deviceIterator = mUSBMonitor.getDevices();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            if (USBDeviceName.isEmpty() || device.getDeviceName().equals(USBDeviceName)) {
                mUsbDevice = device;
                mUSBMonitor.requestPermission(mUsbDevice);
                break;
            }
//            Log.d(TAG, device.getDeviceName() + " " + mUSBMonitor.hasPermission(device));
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
        mMainLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }

                Iterator<UsbDevice> deviceIterator = mUSBMonitor.getDevices();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();

                    if (USBDeviceName.isEmpty() || device.getDeviceName().equals(USBDeviceName)) {
                        mUSBMonitor.requestPermission(device);
                        break;
                    }
                }
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

        if (mUVCCameraView != null) {
            boolean mirrored = _settings.getBoolean("camera_mirror", false);
            mUVCCameraView.setScaleX(mirrored ? -1 : 1);

            int cameraViewGravity = Gravity.START;

            switch (_settings.getString("camera_view_position", "left")) {
                case "left":
                    cameraViewGravity = Gravity.START|Gravity.CENTER_VERTICAL;
                    break;
                case "center":
                    cameraViewGravity = Gravity.CENTER;
                    break;
                case "right":
                    cameraViewGravity = Gravity.END|Gravity.CENTER_VERTICAL;
                    break;
            }
            ((FrameLayout.LayoutParams) mUVCCameraView.getLayoutParams()).gravity = cameraViewGravity;
        }


        if ( _settings.getBoolean("ps_enable", false) ) {
            layoutParkingSensors.setVisibility(RelativeLayout.VISIBLE);
            DisplayMetrics metrics = this.getResources().getDisplayMetrics();

            int cameraPercentageWidth = _settings.getInt("camera_width", 70);

            mUVCCameraView.getLayoutParams().width =
                    metrics.widthPixels / 100 * cameraPercentageWidth;

            layoutParkingSensors.getLayoutParams().width =
                    metrics.widthPixels / 100 * (100 - cameraPercentageWidth);



            String position = _settings.getString("ps_position", "left");
            int gravityPosition = Gravity.START;

            switch ( position ) {
                case "left":
                    gravityPosition = Gravity.START;
                    break;
                case "center":
                    gravityPosition = Gravity.CENTER;
                    break;
                case "right":
                    gravityPosition = Gravity.END;
                    break;
            }
            ((FrameLayout.LayoutParams) layoutParkingSensors.getLayoutParams()).gravity = gravityPosition;



            int height = metrics.heightPixels;
            carImageView.getLayoutParams().height = height / 100 * _settings.getInt("car_height", 30);

            double aspectRatio = 0.4998053527980535;//width / height of car1.xml
            carImageView.getLayoutParams().width = (int)(carImageView.getLayoutParams().height * aspectRatio);

            int frontSensorsCount = _settings.getInt("car_front_sensors_count", 0),
                rearSensorsCount = _settings.getInt("car_rear_sensors_count", 0);
            String frontIndicatorPosition = _settings.getString("ps_front_indicator_position", "right"),
                    rearIndicatorPosition = _settings.getString("ps_rear_indicator_position", "right");

            parkingSensorsView.setFrontSensorsCount(frontSensorsCount);
            mAPP.getFrontTextView().setVisibility(
                    (frontSensorsCount == 0 || frontIndicatorPosition.equals("disabled"))
                            ? View.GONE
                            : View.VISIBLE);
            parkingSensorsView.setRearSensorsCount(rearSensorsCount);
            mAPP.getRearTextView().setVisibility(
                    (rearSensorsCount == 0 || rearIndicatorPosition.equals("disabled"))
                            ? View.GONE
                            : View.VISIBLE );


            parkingSensorsView.setCarHeight(height / 100 * _settings.getInt("car_height", 30));
            parkingSensorsView.setUnits(_settings.getString("units", "см"));
            parkingSensorsView.setMinMaxDistances(
                    Integer.parseInt(_settings.getString("ps_min", getString(R.string.pref_default_ps_min))),
                    Integer.parseInt(_settings.getString("ps_max", getString(R.string.pref_default_ps_max))));

            int fontSize = _settings.getInt("car_font_size", 30);
            float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            int viewSize = (int) (fontSize * 3 * scaledDensity);
            int margins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    Integer.parseInt(_settings.getString("ps_indicators_margin", "10")),
                    getResources().getDisplayMetrics());



            TextViewCircle ringView = mAPP.getRingView("rear");
            ringView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ringView.getLayoutParams();
            params.width = viewSize;
            params.height = viewSize;
            params.setMargins(margins, margins, margins, margins);
            if ( rearIndicatorPosition.equals("left") ) {
                params.addRule(RelativeLayout.RIGHT_OF, 0);
                params.addRule(RelativeLayout.LEFT_OF, R.id.carView);
            } else if ( rearIndicatorPosition.equals("right") ) {
                params.addRule(RelativeLayout.RIGHT_OF, R.id.carView);
                params.addRule(RelativeLayout.LEFT_OF, 0);
            }
            ringView.setLayoutParams(params);


            ringView = mAPP.getRingView("front");
            ringView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            params = (RelativeLayout.LayoutParams) ringView.getLayoutParams();
            params.width = viewSize;
            params.height = viewSize;
            params.setMargins(margins, margins, margins, margins);
            if ( frontIndicatorPosition.equals("left") ) {
                params.addRule(RelativeLayout.RIGHT_OF, 0);
                params.addRule(RelativeLayout.LEFT_OF, R.id.carView);
            } else if ( frontIndicatorPosition.equals("right") ) {
                params.addRule(RelativeLayout.RIGHT_OF, R.id.carView);
                params.addRule(RelativeLayout.LEFT_OF, 0);
            }
            ringView.setLayoutParams(params);


        } else {
            layoutParkingSensors.setVisibility(RelativeLayout.GONE);
            mUVCCameraView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }

        USBDeviceName = _settings.getString("usb_device_name", "");
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

        super.onDestroy();
    }

    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
            }
            mUVCCamera = new UVCCamera();
            EXECUTER.execute(new Runnable() {
                @Override
                public void run() {
                    mUVCCamera.open(ctrlBlock);
                    //if (DEBUG) Log.i(TAG, "supportedSize:" + mUVCCamera.getSupportedSize());
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                    try {
                        mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        try {
                            // fallback to YUV mode
                            mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
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
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
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
        public void onDettach(final UsbDevice device) { }

        @Override
        public void onCancel() { }
    };


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }

            if (mUsbDevice != null && mUSBMonitor != null) {
                mUSBMonitor.requestPermission(mUsbDevice);
            }

//            Iterator<UsbDevice> deviceIterator = mUSBMonitor.getDevices();
//            while (deviceIterator.hasNext()) {
//                UsbDevice device = deviceIterator.next();
//
//                if (USBDeviceName.isEmpty() || device.getDeviceName().equals(USBDeviceName)) {
//                    mUSBMonitor.requestPermission(device);
//                    break;
//                }
//            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {

        }
    };
}
