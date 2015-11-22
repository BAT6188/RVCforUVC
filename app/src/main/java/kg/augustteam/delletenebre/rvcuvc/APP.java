package kg.augustteam.delletenebre.rvcuvc;

import android.app.Activity;
import android.app.Application;

public class APP extends Application {
    protected static final boolean DEBUG = true;

    private static APP instance = new APP();
    public static APP getInstance() {
        return instance;
    }

    private Activity mCameraActivity;
    public void setCameraActivity(Activity activity) {
        mCameraActivity = activity;
    }
    public Activity getCameraActivity() {
        return mCameraActivity;
    }

    private ParkingSensorsView mParkingSensorsView;
    public void setParkingSensorsView(ParkingSensorsView view) {
        mParkingSensorsView = view;
    }
    public ParkingSensorsView getParkingSensorsView() {
        return mParkingSensorsView;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
