package antidose.antidose;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class AntidoseNotifications extends FirebaseMessagingService {
    private FusedLocationProviderClient mFusedLocationClient;
    Location end = new Location("");
    int max,incidentId = 0;
    String notification = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        max = 0;
        incidentId = 0;
        Float lat, lon;

        // Create jsonobject
        Map<String, String> params = remoteMessage.getData();
        JSONObject jsonObject = new JSONObject(params);

        // Determine if it is a dismissal message
        try {
            notification = jsonObject.getString("notification");
        }catch(JSONException e){
            return;
        }

        if (notification.equals("help")) {
            try{
                lat = Float.parseFloat(jsonObject.getString("lat"));
                lon = Float.parseFloat(jsonObject.getString("lon"));
                max = jsonObject.getInt("max");
                incidentId = jsonObject.getInt("incident_id");
            }catch(JSONException e){
                return;
            }

            end.setLatitude(lat);
            end.setLongitude(lon);

            LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            createNotification(location, max, incidentId);

            /*mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                createNotification(location, max, incidentId);
                            }
                        }
                    });*/

        } else if(notification.equals("dismiss")){
            try{
                incidentId = jsonObject.getInt("incident_id");
            }catch (JSONException e) {

            }
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(incidentId);
        }
    }

    public void createNotification(Location location, int max, int incidentId){
        Float bearing = location.bearingTo(end);
        Float distance = location.distanceTo(end);

        if (distance < max){
            // /location endpoint update location
            return;
        }

        Intent resultIntent = new Intent(this, NotifyActivity.class);
        resultIntent.putExtra("BEARING", bearing);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Someone is experiencing an overdose " + distance + "m away")
                        .setContentText("Click to respond");

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = incidentId;

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
