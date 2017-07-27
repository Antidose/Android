package antidose.antidose;

import android.*;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import java.io.IOException;

import okhttp3.ResponseBody;


public class NotifyActivity extends AppCompatActivity {

    LocationManager mLocationManager;
    public static final String TOKEN_PREFS_NAME = "User_Token";
    private WebSocketClient mWebSocketClient;
    String token;

    @Override
    protected void onNewIntent(Intent savedIntent)
    {
        super.onNewIntent(savedIntent);
        onActive();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onActive();
    }

    protected void onActive() {
        setContentView(R.layout.activity_notify);

        /*// Cancel the notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(getIntent().getIntExtra("INCIDENT_ID", 0));*/

        updateFonts();
        connectWebSocket();

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Button numComing = (Button) findViewById(R.id.buttonGoing);

        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
        token = settings.getString("Token", null);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.READ_PHONE_STATE}, 1);
            return;
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNETWORK = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        if (locationGPS != null && locationGPS.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Last location in phone was 2 minutes ago
            // Do something with the recent location fix
            //  otherwise wait for the update below
            getInfoHandler(token, locationGPS);
        } else if (locationNETWORK != null && locationNETWORK.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000){
            // Last location in phone was 2 minutes ago
            // Do something with the recent location fix
            //  otherwise wait for the update below
            getInfoHandler(token, locationNETWORK);
        }

        else {

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

        }
    }


    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(locationListenerGps);
            getInfoHandler(token, location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(locationListenerNetwork);
            getInfoHandler(token, location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };


    public void goInfo() {
        Intent intent = new Intent(this, InformationActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_info:
                goInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void getInfoHandler(String token, Location location){
        //middle function just to grab the views after the location is figured out
        Button distance = (Button) findViewById(R.id.buttonDistance);
        Button time = (Button) findViewById(R.id.buttonTime);
        makeAPICallMapInfo(distance, time, token, location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);

    }



    public void canGo(View view) {
        String hasKit = view.getTag().toString();
        if (hasKit.equals("true")) {
            makeAPICallRespond(true, true, token);
        } else {
            makeAPICallRespond(false, true, token);
        }
    }

    public void cannotGo(View view) {
        makeAPICallRespond(false, false, token);

    }

    //on startup to change all fonts
    public void updateFonts(){
        TextView tx = (TextView)findViewById(R.id.textViewQuestion);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "font/gravitylight.otf");
        Typeface custom_font_regular = Typeface.createFromAsset(getAssets(), "font/gravityregular.otf");

        tx.setTypeface(custom_font);

        TextView tx2 = (TextView)findViewById(R.id.textView30);

        tx2.setTypeface(custom_font);

        TextView tx3 = (TextView)findViewById(R.id.textView31);

        tx3.setTypeface(custom_font);

        TextView tx4 = (TextView)findViewById(R.id.textView32);

        tx4.setTypeface(custom_font);

        TextView tx5 = (TextView)findViewById(R.id.textViewTitle);

        tx5.setTypeface(custom_font_regular);

        Button b1 = (Button)findViewById(R.id.buttonYesKit);
        b1.setTypeface(custom_font);

        Button b2 = (Button)findViewById(R.id.buttonYesNoKit);
        b2.setTypeface(custom_font);

        Button b3 = (Button)findViewById(R.id.buttonNo);
        b3.setTypeface(custom_font);
    }


    public void makeAPICallRespond(boolean hasKit, final boolean isGoing, String token){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);
        Intent intent = getIntent();
        String inc_id = intent.getStringExtra("INCIDENT_ID");
        if(inc_id == null){
            //error
            return;
        }

       //String inc_id = "1";

        Call<RestInterface.IncidentLocation> call = apiService.respondIncident(new RestInterface().new Responder(token, inc_id, hasKit, isGoing));

        call.enqueue(new Callback<RestInterface.IncidentLocation>() {
            @Override
            public void onResponse(Call<RestInterface.IncidentLocation> call, Response<RestInterface.IncidentLocation> response) {
                if (response.isSuccessful()) {
                        Timber.d("Responding to incident successful: ");
                        if (!isGoing) {
                            //person clicked 'no im not going'
                            //return to caller, caller redirects to main
                            mWebSocketClient.close();
                            boolean test = mWebSocketClient.isClosed();
                            Intent intent = new Intent(NotifyActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);

                        }else {

                            float IncidentLatitude = response.body().getLatitude();
                            float IncidentLongitude = response.body().getLongitude();

                            if(Math.signum(IncidentLatitude)==0 && Math.signum(IncidentLongitude)==0){
                                //then it has been cancelled
                                Intent intent = new Intent(NotifyActivity.this, MainActivity.class);
                                intent.putExtra("CANCEL_FRAGMENT", "TRUE");
                                startActivity(intent);

                            }else {
                                Intent intent = new Intent(NotifyActivity.this, NavigationActivity.class);
                                intent.putExtra("incident-latitude", IncidentLatitude);
                                intent.putExtra("incident-longitude", IncidentLongitude);

                                startActivity(intent);
                            }
                        }
                    }
                }

            @Override
            public void onFailure(Call<RestInterface.IncidentLocation> call, Throwable t) {
                Log.d("D", "Responding to incident failed :(");
                Log.d("D", t.toString());
            }
        });

    }

//    public void makeAPICallNumResponders(final TextView numComing, String token){
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(getResources().getText(R.string.server_url).toString())
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .build();
//
//        RestInterface.restInterface apiService =
//                retrofit.create(RestInterface.restInterface.class);
//
//
//        Call<RestInterface.NumberResponders> call = apiService.numberResponders(new RestInterface().new ApiToken(token));
//
//        call.enqueue(new Callback<RestInterface.NumberResponders>() {
//            @Override
//            public void onResponse(Call<RestInterface.NumberResponders> call, Response<RestInterface.NumberResponders> response) {
//                if (response.isSuccessful()) {
//                    Timber.d("Got number of responders: ");
//                    numComing.setText(response.body().getResponders());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<RestInterface.NumberResponders> call, Throwable t) {
//                Log.d("D", "Getting number responders failed :(");
//                Log.d("D", t.toString());
//            }
//        });
//    }

    public void makeAPICallMapInfo(final TextView distance, final TextView duration, String token, Location location){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);
        Intent intent = getIntent();
        String inc_id = intent.getStringExtra("INCIDENT_ID");
        if(inc_id == null){
            //error
            return;
        }

        Call<RestInterface.MapInformation> call = apiService.requestInfo(new RestInterface().new ResponderIncLatLong(token, inc_id, location.getLatitude(), location.getLongitude()));

        call.enqueue(new Callback<RestInterface.MapInformation>() {
            @Override
            public void onResponse(Call<RestInterface.MapInformation> call, Response<RestInterface.MapInformation> response) {
                if (response.isSuccessful()) {
                    Timber.d("Got map information: ");
                    //parse

                    String km = Integer.toString((int) (response.body().getDist())/1000);
                    distance.setText(km);

                    String minutes = Integer.toString((int)(response.body().getTime())/60);
                    String seconds = Integer.toString((int)(response.body().getDist())%60);

                    if(minutes.length() ==1 )
                        minutes = "0" + minutes;
                    if(seconds.length() == 1)
                        seconds = "0" + seconds;
                    duration.setText(minutes+":"+seconds);

                }
            }

            @Override
            public void onFailure(Call<RestInterface.MapInformation> call, Throwable t) {
                Log.d("D", "Getting map information failed :(");
                Log.d("D", t.toString());
            }
        });
    }


    public void updateOTWCount(TextView text, String s){

        // TODO: 2017-07-13 get number of responders on the way from server

        text.setText(s);

    }

    private void connectWebSocket() {
        URI uri;
        String url = "ws" + getResources().getText(R.string.server_url).toString().replaceAll("http(s?)", "") + "ws";
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");

                // {incidentID: string(12),
                // ID: IMEI | Token}
                JSONObject req = new JSONObject();
                String incidentID = getIntent().getStringExtra("INCIDENT_ID");
                try {
                    req.put("incidentId", incidentID);
                    req.put("userId", token);
                } catch (org.json.JSONException e) {
                    // IDK PASS
                }
                mWebSocketClient.send(req.toString());
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWebSocketClient.isOpen()) {
                            if (message.equals("cancel")) {

                                Intent intent = new Intent(NotifyActivity.this, MainActivity.class);
                                intent.putExtra("CANCEL_FRAGMENT", "TRUE");
                                startActivity(intent);


                            } else if (message.trim().isEmpty()) {
                                //skip
                            } else {
                                Button numComing = (Button) findViewById(R.id.buttonGoing);
                                updateOTWCount(numComing, message);
                            }
                        }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
//                Intent intent = new Intent(NotifyActivity.this, MainActivity.class);
//                intent.putExtra("CANCEL_FRAGMENT", "TRUE");
//                startActivity(intent);

            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }


}
