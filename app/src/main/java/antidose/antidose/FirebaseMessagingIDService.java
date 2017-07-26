package antidose.antidose;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by graeme on 2017-07-18.
 */

public class FirebaseMessagingIDService extends FirebaseInstanceIdService{
    public static final String FIREBASE_ID = "Firebase_ID";
    public static final String TOKEN_PREFS_NAME = "User_Token";


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

        //check if new user or updating user
        SharedPreferences usettings = getSharedPreferences(MainActivity.TOKEN_PREFS_NAME, 0);
        String token = usettings.getString("Token", null);
        if(token != null){
            //id has been updated, user is not new
            makeApiCallUpdateFirebase(token, refreshedToken);
        }

    }

    public void makeApiCallUpdateFirebase(String token, String firebaseToken){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);

        Call<ResponseBody> call = apiService.UpdateFirebaseToken(new RestInterface().new UpdateFirebase(token, firebaseToken));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Timber.d("Updated successfully ");
                    //parse
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("D", "Update failed :(");
                Log.d("D", t.toString());
            }
        });
    }

}
