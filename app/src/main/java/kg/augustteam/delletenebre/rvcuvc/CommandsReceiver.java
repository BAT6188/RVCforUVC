package kg.augustteam.delletenebre.rvcuvc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandsReceiver extends BroadcastReceiver {
    private static final String RIM = "org.kangaroo.rim.action.ACTION_DATA_RECEIVE";

    private APP mApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        mApp = APP.getInstance();

        if ( intent.getAction().equals(RIM) ) {
            String command = intent.getStringExtra("org.kangaroo.rim.device.EXTRA_COMMAND").toLowerCase();
            String args = intent.getStringExtra("org.kangaroo.rim.device.EXTRA_ARGS").toLowerCase();

            Log.d("******************", command);
            Log.d("******************", args);

            if (command.equals("transmissiongearposition")) {
                if (args.equals("reverse")) {
                    Intent cameraIntent = new Intent(context.getApplicationContext(), CameraActivity.class);
                    cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(cameraIntent);

                } else if (!args.equals("reverse") && mApp.getCameraActivity() != null ) {
                    mApp.getCameraActivity().finish();
                }


            } else if ( command.equals("parkingsensorsdata") ) {
                ParkingSensorsView view = mApp.getParkingSensorsView();
                if ( view != null ) {
                    view.setSensorsData("rear", args);
                }
            }
        }
    }
}
