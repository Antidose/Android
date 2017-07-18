package antidose.antidose;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

/**
 * Created by graeme on 2017-07-18.
 */

public class FirebaseMessagingIDService extends FirebaseInstanceIdService{
    public static final String FIREBASE_ID = "Firebase_ID";



    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.d("Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences settings = getSharedPreferences(FIREBASE_ID, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(FIREBASE_ID, refreshedToken);
        editor.commit();
    }

}
