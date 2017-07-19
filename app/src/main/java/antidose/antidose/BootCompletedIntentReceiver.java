package antidose.antidose;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by graeme on 2017-07-19.
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, AntidoseNotifications.class);
            context.startService(pushIntent);
        }
    }
}