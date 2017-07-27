package antidose.antidose;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by graeme on 2017-07-25.
 */

public class NotificationDismissedReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String incidentId = intent.getStringExtra("INCIDENT_ID");
        makeAPICallRespond(context, incidentId);
    }

    public void makeAPICallRespond(Context context, String incidentId){


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);

        SharedPreferences settings = context.getSharedPreferences(MainActivity.TOKEN_PREFS_NAME, 0);
        String token = settings.getString("Token", null);

        if(token == null){
            Log.d("D","User has no token");
            return;
        }

        Call<RestInterface.IncidentLocation> call = apiService.respondIncident(new RestInterface().new Responder(token,incidentId,  false, false));

        call.enqueue(new Callback<RestInterface.IncidentLocation>() {
            @Override
            public void onResponse(Call<RestInterface.IncidentLocation> call, retrofit2.Response<RestInterface.IncidentLocation> response) {
                if (response.isSuccessful()) {
                    Timber.d("Responding to incident successful: ");
                    return;
                }
            }

            @Override
            public void onFailure(Call<RestInterface.IncidentLocation> call, Throwable t) {
                Log.d("D", "Responding to incident failed :(");
                Log.d("D", t.toString());
            }
        });

    }
}
