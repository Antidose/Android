package antidose.antidose;

import android.Manifest;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import butterknife.OnClick;

import static antidose.antidose.R.id.action_info;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CancelRequestFragment.CancelRequestListener {

    LocationManager mLocationManager;
    static String IMEI;

    public static final String TOKEN_PREFS_NAME = "User_Token";

    AnimationDrawable alertAnimation;

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

    protected void onActive()
    {
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
        String token = settings.getString("Token", null);

        if (token != null) {
            Button regButton = (Button) findViewById(R.id.button_login);
            regButton.setVisibility(View.INVISIBLE);
            Button settingButton = (Button) findViewById(R.id.button_settings);
            settingButton.setVisibility(View.VISIBLE);
            //service should only run when the user is registered
            Intent intent = new Intent(this, PollingService.class);
            startService(intent);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE}, 1);
        }

        //get users imei to make the request


        updateFonts();
        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //animation
        ImageButton alertImage = (ImageButton) findViewById(R.id.imageButton);
        alertImage.setImageResource(R.drawable.animatedalert);
        alertImage.setScaleType(ScaleType.FIT_CENTER);
        alertAnimation = (AnimationDrawable) alertImage.getDrawable();
        alertAnimation.start();

        String frag = getIntent().getStringExtra("CANCEL_FRAGMENT");
        if (frag != null) {
            showCancelRequestDialog();
        }
    }

    public void showCancelRequestDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CancelRequestFragment();
        dialog.show(getSupportFragmentManager(), "CancelRequestFragment");
    }


    @Override
    public void onDialogPositiveClickCancelRequest(DialogFragment dialog) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
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

    public void sendAlert(View view) {
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = mngr.getDeviceId();

        //bring up loading thing, this could take a sec
        Button loadButton = (Button) findViewById(R.id.buttonLoading);
        loadButton.setVisibility(View.VISIBLE);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE}, 1);
            return;
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNETWORK = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        if (locationGPS != null && locationGPS.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Last location in phone was 2 minutes ago
            // Do something with the recent location fix
            //  otherwise wait for the update below
            makeAPICall(IMEI, locationGPS);
        } else if (locationNETWORK != null && locationNETWORK.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000){
            // Last location in phone was 2 minutes ago
            // Do something with the recent location fix
            //  otherwise wait for the update below
            makeAPICall(IMEI, locationNETWORK);
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
            makeAPICall(IMEI, location);
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
            makeAPICall(IMEI, location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public void updateFonts(){

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "font/gravitylight.otf");
        Typeface custom_font_regular = Typeface.createFromAsset(getAssets(), "font/gravityregular.otf");

        TextView tx = (TextView)findViewById(R.id.textView);
        tx.setTypeface(custom_font_regular);
        TextView tx1 = (TextView)findViewById(R.id.textView2);
        tx1.setTypeface(custom_font);
        TextView tx2 = (TextView)findViewById(R.id.textView3);
        tx2.setTypeface(custom_font);

        Button b1 = (Button)findViewById(R.id.button_login);
        b1.setTypeface(custom_font);
        Button b2 = (Button)findViewById(R.id.button_settings);
        b2.setTypeface(custom_font);
    }

    public void goNavigate(View view) {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    public void goNotify(View view) {
        Intent intent = new Intent(this, NotifyActivity.class);
        startActivity(intent);
    }

    public void goSettings(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void goInfo() {
        Intent intent = new Intent(this, InformationActivity.class);
        startActivity(intent);
    }

    public void makeAPICall(String IMEI, Location location){
        final String theIMEI = IMEI;
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
        String token = settings.getString("Token", "");

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);


        Call<RestInterface.startIncidentResponse> call = apiService.sendHelp(new RestInterface().new Alert(IMEI, token, location.getLatitude(), location.getLongitude()));

        call.enqueue(new Callback<RestInterface.startIncidentResponse>() {
            @Override
            public void onResponse(Call<RestInterface.startIncidentResponse> call, Response<RestInterface.startIncidentResponse> response) {
                if (response.isSuccessful()){
                    Timber.d("Alert request successful");

                    Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                    intent.putExtra("INCIDENT_ID", response.body().getIncidentId().toString());
                    intent.putExtra("RADIUS", Integer.toString(response.body().getRadius()/1000));
                    intent.putExtra("NUM_RESPONDERS", Integer.toString(response.body().getNumNotified()));

                    Button loadButton = (Button) findViewById(R.id.buttonLoading);
                    loadButton.setVisibility(View.INVISIBLE);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<RestInterface.startIncidentResponse> call, Throwable t) {
                Log.d("D", "Alert Request failed :(");
                Log.d("D", t.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

}
