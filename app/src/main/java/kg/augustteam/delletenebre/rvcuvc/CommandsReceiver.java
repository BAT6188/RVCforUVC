package kg.augustteam.delletenebre.rvcuvc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandsReceiver extends BroadcastReceiver {
    private static final String RIM = "org.kangaroo.rim.action.ACTION_DATA_RECEIVE";

    private APP mAPP;

    @Override
    public void onReceive(Context context, Intent intent) {
        mAPP = APP.getInstance();

        if ( intent.getAction().equals(RIM) ) {
            String command = intent.getStringExtra("org.kangaroo.rim.device.EXTRA_COMMAND").toLowerCase();
            String args = intent.getStringExtra("org.kangaroo.rim.device.EXTRA_ARGS").toLowerCase();

//            Log.d("******************", command);
//            Log.d("******************", args);

            if (command.equals("transmissiongearposition")) {
                if (args.equals("reverse")) {
                    if (mAPP.getCameraActivity() == null ) {
                        Intent cameraIntent = new Intent(context.getApplicationContext(), CameraActivity.class);
                        cameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.getApplicationContext().startActivity(cameraIntent);
                    }

                } else if (!args.equals("reverse") && mAPP.getCameraActivity() != null ) {
                    mAPP.getCameraActivity().finish();
                    mAPP.setCameraActivity(null);
                }


            } else if ( command.equals("parkingsensorsdata") ) {
                ParkingSensorsView view = mAPP.getParkingSensorsView();
                if ( view != null ) {
                    view.setSensorsData("rear", args);
                }
            }
        }
    }
}
