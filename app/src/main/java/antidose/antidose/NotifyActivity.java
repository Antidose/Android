package antidose.antidose;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class NotifyActivity extends AppCompatActivity {

    public static final String TOKEN_PREFS_NAME = "User_Token";
    //int incidentID = getIntent().getExtra("INCIDENT_ID");
    //temp
    int incidentID = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        TextView distance = (TextView) findViewById(R.id.textViewDistance);
        TextView direction = (TextView) findViewById(R.id.textViewDirection);
        TextView time = (TextView) findViewById(R.id.textViewTime);

        updateDistance(distance);
        updateDirection(direction);
        updateTime(time);

    }

    public void canGo(View view) {
        String hasKit = view.getTag().toString();
        if (hasKit == "true") {
            makeAPICallRespond(true, true);
        } else {
            makeAPICallRespond(false, true);
        }
    }

    public void cannotGo(View view) {
        makeAPICallRespond(false, false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void updateDistance(TextView text) {

        //get the distance from server
        int KM = 10;
        text.setText(KM + " KM");
    }

    public void updateDirection(TextView text) {

        //get cardinality from server
        String dir = "NORTH";
        text.setText(dir);

    }

    public void updateTime(TextView text) {
        //get time to drive from server
        String time = "10:25";  //might be a time object or something
        text.setText(time + " DRIVE");
    }

    public void makeAPICallRespond(boolean hasKit, final boolean isGoing){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);

        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
        String token = settings.getString("Token", null);

        Call<RestInterface.IncidentLocation> call = apiService.respondIncident(new RestInterface().new Responder(token, hasKit, isGoing, incidentID));

        call.enqueue(new Callback<RestInterface.IncidentLocation>() {
            @Override
            public void onResponse(Call<RestInterface.IncidentLocation> call, Response<RestInterface.IncidentLocation> response) {
                if (response.isSuccessful()) {
                        Timber.d("Responding to incident successful: ");
                        if (!isGoing) {
                            //person clicked 'no im not going'
                            //return to caller, caller redirects to main
                            return;
                        }

                        Location incidentLocation = response.body().getLocation();

                        Intent intent = new Intent(NotifyActivity.this, NavigationActivity.class);
                        intent.putExtra("incident-location", incidentLocation);
                        startActivity(intent);

                    }
                }

            @Override
            public void onFailure(Call<RestInterface.IncidentLocation> call, Throwable t) {
                Log.d("D", "User registration failed :(");
                Log.d("D", t.toString());
            }
        });

    }
}

