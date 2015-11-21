package kg.augustteam.delletenebre.rvcuvc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandsReceiver extends BroadcastReceiver {
    private static final String RIM = "org.kangaroo.rim.action.ACTION_DATA_RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent.getAction() == RIM ) {
            String command = intent.getStringExtra("org.kangaroo.rim.device.EXTRA_COMMAND");
            String args = intent.getStringExtra("org.kangaroo.rim.device.EXTRA_ARGS");

            Log.d("******************", command);
            Log.d("******************", args);

            if (command.equals("TransmissionGearPosition")) {
//                Intent cameraIntent = new Intent(context.getApplicationContext(), CameraActivity.class);
//
//                if (args.equals("reverse")) {
//                    cameraIntent.setFlags(
//                              Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                } else {
//                    cameraIntent.setFlags(
//                              Intent.FLAG_ACTIVITY_SINGLE_TOP
//                            | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    cameraIntent.putExtra("suicide", true);
//                }
//
//                context.startActivity(cameraIntent);
            }
        }
    }
}
