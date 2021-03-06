package antidose.antidose;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.lang.Thread;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class HelpActivity extends AppCompatActivity implements cancelSearchFragment.CancelSearchListener, confirmHelpFragment.ConfirmHelpListener{

    AudioManager audioManager;
    MediaPlayer mp;
    public static final String TOKEN_PREFS_NAME = "User_Token";
    private WebSocketClient mWebSocketClient;
    Button smsButton;
    final private int MY_PERMISSIONS_REQUEST_SEND_SMS = 123;
    String emsTextContents = "There is an opioid overdose occuring at my location, please send help.";

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

    public void onActive()
    {
        setContentView(R.layout.activity_help);

        updateFonts();
        connectWebSocket();

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Button radius = (Button) findViewById(R.id.buttonRadius);
        Button resCount = (Button) findViewById(R.id.buttonResponderCount);
        Button OTW = (Button) findViewById(R.id.buttonComing);

        updateRadius(radius);
        updateResCount(resCount);
        updateOTWCount(OTW, "0");

        if(checkSMSPermissions()) {
            sendSMS();
        }
        else {
            // Do nothing
        }

        audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        mp = MediaPlayer.create(this, R.raw.alarm);
        mp.setLooping(true);

        Button dialerButton = (Button) findViewById(R.id.buttonDialEMS);
        dialerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:411"));
                startActivity(callIntent);
            }
        });

        Button infoButton = (Button) findViewById(R.id.buttonTips);
        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                goInfo();
            }
        });
    }

    protected boolean checkSMSPermissions(){
        int checkSMSPermission = ContextCompat.checkSelfPermission(HelpActivity.this, android.Manifest.permission.SEND_SMS);
        if (checkSMSPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HelpActivity.this, new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            return false;
        }
        else {
            // Permission Granted
            return true;
        }
    }

    protected void sendSMS(){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("4038912963", null, emsTextContents, null, null);
        Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSMS();
                }
                else {
                    // permission denied, do nothing
                }
            }
        }
    }

    public void playAlarm() {
        while(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        }
        mp.start();
    }
    public void stopAlarm() {
        mp.stop();
    }

    public void updateRadius(TextView text) {

        //// TODO: 2017-07-13 get search radius from server
        String radius = "00";
        radius = getIntent().getStringExtra("RADIUS");
        text.setText(radius);

    }

    public void updateResCount(TextView text) {

        // TODO: 2017-07-13 get responder count in radius from server
        String count = "00";
        count = getIntent().getStringExtra("NUM_RESPONDERS");
        text.setText(count);

    }

    public void updateOTWCount(TextView text, String s){

        // TODO: 2017-07-13 get number of responders on the way from server


        text.setText(s);

    }

    // DIALOG WHEN YOU HIT CANCEL SEARCH
    //OPEN FUNCTION AND INTERFACES
    public void showCancelDialog(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new cancelSearchFragment();
        dialog.show(getSupportFragmentManager(), "cancelSearchFragment");
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDialogPositiveClickCancel(DialogFragment dialog) {
        // User touched the dialog's positive button
        makeAPICancel(false);
    }

    @Override
    public void onDialogNegativeClickCancel(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Cancel
    }


    // DIALOG WHEN YOU HIT HELP ARRIVED
    //OPEN FUNCTION AND INTERFACES

    public void showHelpDialog(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new confirmHelpFragment();
        dialog.show(getSupportFragmentManager(), "confirmHelpFragment");
    }


    @Override
    public void onDialogPositiveClickHelp(DialogFragment dialog) {
        // User touched the dialog's positive button
        //return to main activity
        makeAPICancel(true);
    }

    @Override
    public void onDialogNegativeClickHelp(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Cancel
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);

    }

    //on startup to change all fonts
    public void updateFonts(){
        TextView tx = (TextView)findViewById(R.id.textView7);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "font/gravitylight.otf");
        Typeface custom_font_regular = Typeface.createFromAsset(getAssets(), "font/gravityregular.otf");

        tx.setTypeface(custom_font_regular);

        TextView tx2 = (TextView)findViewById(R.id.textView30);
        tx2.setTypeface(custom_font);

        TextView tx3 = (TextView)findViewById(R.id.textView31);
        tx3.setTypeface(custom_font);

        TextView tx4 = (TextView)findViewById(R.id.textView32);
        tx4.setTypeface(custom_font);

        Button b1 = (Button)findViewById(R.id.buttonTips);
        b1.setTypeface(custom_font);

        Button b2 = (Button)findViewById(R.id.buttonHelpArrived);
        b2.setTypeface(custom_font);

        Button b3 = (Button)findViewById(R.id.buttonCancel);
        b3.setTypeface(custom_font);

        Button b4 = (Button)findViewById(R.id.buttonDialEMS);
        b4.setTypeface(custom_font);
    }

    public void makeAPICancel(boolean isResolved){
        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
        String token = settings.getString("Token", null);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String IMEI = mngr.getDeviceId();

        Call<ResponseBody> call = apiService.cancelSearch(new RestInterface().new CancelSearch(IMEI, isResolved));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        Timber.d("Incident Complete successful: " + response.body().string());

                        Intent intent = new Intent(HelpActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return;

                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("D", "Incident Complete Failed:(");
                Log.d("D", t.toString());
            }
        });
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
                String incidentID = getIntent().getStringExtra("INCIDENT_ID");
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
                        if(message.equals("cancel")) {
                            //toss it

                        }else if(message.trim().isEmpty()) {
                        //skip
                        }else {
                            //System.out.print(message);
                            //TextView textView = (TextView)findViewById(R.id.messages);
                            //textView.setText(textView.getText() + "\n" + message);
                            Button OTW = (Button) findViewById(R.id.buttonComing);
                            updateOTWCount(OTW, message);
                        }
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
}

