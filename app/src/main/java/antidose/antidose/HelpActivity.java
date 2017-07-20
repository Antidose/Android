package antidose.antidose;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class HelpActivity extends AppCompatActivity implements cancelSearchFragment.CancelSearchListener, confirmHelpFragment.ConfirmHelpListener{

    public static final String TOKEN_PREFS_NAME = "User_Token";
    private WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        updateOTWCount(OTW, "00");

    }

    public void updateRadius(TextView text) {

        //// TODO: 2017-07-13 get search radius from server
        String radius = "00";
        text.setText(radius);

    }

    public void updateResCount(TextView text) {

        // TODO: 2017-07-13 get responder count in radius from server
        String count = "000";
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
    public void onDialogPositiveClickCancel(DialogFragment dialog) {
        // User touched the dialog's positive button
        makeAPICancel(false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDialogNegativeClickHelp(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Cancel
    }


    public void overdoseTips(View view) {

//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

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
    }

    public void makeAPICancel(boolean isResolved){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);

        Call<ResponseBody> call = apiService.cancelSearch(new RestInterface().new CancelSearch(MainActivity.IMEI, isResolved));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        Timber.d("Incident Complete successful: " + response.body().string());
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
        try {
            uri = new URI("ws://antidose-go.herokuapp.com/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
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
                        Button OTW = (Button) findViewById(R.id.buttonComing);
                        updateOTWCount(OTW, message);
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

