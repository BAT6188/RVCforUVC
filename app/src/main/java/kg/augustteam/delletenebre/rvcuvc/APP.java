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

    private TextViewCircle mFrontTextView;
    public void setFrontTextView(TextViewCircle view) {
        mFrontTextView = view;
    }
    public TextViewCircle getFrontTextView() {
        return mFrontTextView;
    }

    private TextViewCircle mRearTextView;
    public void setRearTextView(TextViewCircle view) {
        mRearTextView = view;
    }
    public TextViewCircle getRearTextView() {
        return mRearTextView;
    }

    public TextViewCircle getRingView(String side) {
        side = side.toLowerCase();
        if ( side.equals("front") ) {
            return mFrontTextView;
        }
        if ( side.equals("rear") ) {
            return mRearTextView;
        }

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
