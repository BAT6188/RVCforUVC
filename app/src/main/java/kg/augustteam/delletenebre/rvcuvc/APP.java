package kg.augustteam.delletenebre.rvcuvc;

import android.app.Activity;
import android.app.Application;
import android.widget.TextView;

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

    private TextView mFrontTextView;
    public void setFrontTextView(TextView view) {
        mFrontTextView = view;
    }
    public TextView getFrontTextView() {
        return mFrontTextView;
    }

    private TextView mRearTextView;
    public void setRearTextView(TextView view) {
        mRearTextView = view;
    }
    public TextView getRearTextView() {
        return mRearTextView;
    }

    public TextView getRingView(String side) {
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
