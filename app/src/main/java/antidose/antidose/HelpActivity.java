package antidose.antidose;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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

public class HelpActivity extends AppCompatActivity implements cancelSearchFragment.CancelSearchListener, confirmHelpFragment.ConfirmHelpListener{

    public static final String TOKEN_PREFS_NAME = "User_Token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        TextView radius = (TextView) findViewById(R.id.textViewDistance);
        TextView resCount = (TextView) findViewById(R.id.textViewResponderCount);
        TextView OTW = (TextView) findViewById(R.id.textViewOTW);

        updateRadius(radius);
        updateResCount(resCount);
        updateOTWCount(OTW);

    }

    public void updateRadius(TextView text) {

        //// TODO: 2017-07-13 get search radius from server
        String radius = "000";
        text.setText(radius);

    }

    public void updateResCount(TextView text) {

        // TODO: 2017-07-13 get responder count in radius from server
        String count = "000";
        text.setText(count);

    }

    public void updateOTWCount(TextView text) {

        // TODO: 2017-07-13 get number of responders on the way from server
        String OTW = "000";
        text.setText(OTW);

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
        // TODO: 2017-07-13 cancel the search
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
    public void helpArrived(View view) {

//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);

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

}
