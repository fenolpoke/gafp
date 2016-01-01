import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by COMPAQ on 01/01/2016.
 */
public class MainBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
        }
    }
}
