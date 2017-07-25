package antidose.antidose;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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


public class NotifyActivity extends AppCompatActivity implements LocationListener {

    LocationManager mLocationManager;
    public static final String TOKEN_PREFS_NAME = "User_Token";
    private WebSocketClient mWebSocketClient;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        updateFonts();
        connectWebSocket();

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Button numComing = (Button) findViewById(R.id.buttonGoing);

        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
        token = settings.getString("Token", null);


        //grab location for distance calculations on the backend
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Last location in phone was 2 minutes ago
            // Do something with the recent location fix
            //  otherwise wait for the update below
            getInfoHandler(token, location);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        }
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
            getInfoHandler(token, location);
        }
    }

    // Required functions for implementing location services
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}


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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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


        Call<RestInterface.IncidentLocation> call = apiService.respondIncident(new RestInterface().new Responder(token, hasKit, isGoing));

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

                        float IncidentLatitude = response.body().getLatitude();
                        float IncidentLongitude = response.body().getLongitude();

                        Intent intent = new Intent(NotifyActivity.this, NavigationActivity.class);
                        intent.putExtra("incident-latitude", IncidentLatitude);
                        intent.putExtra("incident-longitude", IncidentLongitude);

                        startActivity(intent);

                    }
                }

            @Override
            public void onFailure(Call<RestInterface.IncidentLocation> call, Throwable t) {
                Log.d("D", "Responding to incident failed :(");
                Log.d("D", t.toString());
            }
        });

    }

    public void makeAPICallNumResponders(final TextView numComing, String token){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);


        Call<RestInterface.NumberResponders> call = apiService.numberResponders(new RestInterface().new ApiToken(token));

        call.enqueue(new Callback<RestInterface.NumberResponders>() {
            @Override
            public void onResponse(Call<RestInterface.NumberResponders> call, Response<RestInterface.NumberResponders> response) {
                if (response.isSuccessful()) {
                    Timber.d("Got number of responders: ");
                    numComing.setText(response.body().getResponders());
                }
            }

            @Override
            public void onFailure(Call<RestInterface.NumberResponders> call, Throwable t) {
                Log.d("D", "Getting number responders failed :(");
                Log.d("D", t.toString());
            }
        });
    }

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


        Call<RestInterface.MapInformation> call = apiService.requestInfo(new RestInterface().new ResponderLatLong(token, location.getLatitude(), location.getLongitude()));

        call.enqueue(new Callback<RestInterface.MapInformation>() {
            @Override
            public void onResponse(Call<RestInterface.MapInformation> call, Response<RestInterface.MapInformation> response) {
                if (response.isSuccessful()) {
                    Timber.d("Got map information: ");
                    //parse
                    String km = Integer.toString((int) (response.body().getDistance())/1000);
                    distance.setText(km + " KM");

                    String minutes = Integer.toString((int) (response.body().getDuration())/60);
                    String seconds = Integer.toString((int) (response.body().getDuration())%60);
                    duration.setText(minutes+":"+seconds+" MIN DRIVE");
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
                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = mngr.getDeviceId();
                // {incidentID: string(12),
                // ID: IMEI | Token}
                JSONObject req = new JSONObject();
                String incidentID = "abababababab"; // Gotta get this from server as response to alert.
                try {
                    req.put("incidentId", incidentID);
                    req.put("userId", IMEI);
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
                        //System.out.print(message);
                        //TextView textView = (TextView)findViewById(R.id.messages);
                        //textView.setText(textView.getText() + "\n" + message);
                        Button numComing = (Button) findViewById(R.id.buttonGoing);
                        updateOTWCount(numComing, message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
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

        Call<RestInterface.IncidentLocation> call = apiService.respondIncident(new RestInterface().new Responder(token, hasKit, isGoing));

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

                        //float lat = response.body().getLocation();

                        Intent intent = new Intent(NotifyActivity.this, NavigationActivity.class);
                       //intent.putExtra("incident-location", incidentLocation);
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
