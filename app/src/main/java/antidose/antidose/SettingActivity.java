package antidose.antidose;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.support.v4.content.ContextCompat;

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

public class SettingActivity extends AppCompatActivity implements DeleteConfirmFragment.DeleteDialogListener {

    public static final String TOKEN_PREFS_NAME = "User_Token";
    public static final String STATUS_PREFS_NAME = "Status";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //Active/Inactive Status Button
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        final TextView active = (TextView) findViewById(R.id.textViewActive);
        final TextView inactive = (TextView) findViewById(R.id.textViewInactive);

        //get status from prefs and set status no post call
        getStatus(toggle, active, inactive);


        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    //make a DB call to save state
                    setStatus(toggle, active, inactive, true);

                }
                else {
                    //make a DB call to save state
                    setStatus(toggle, active, inactive, false);

                }
            }
        });
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

    public void getStatus(final ToggleButton btn, final TextView on, final TextView off){

        //when the page loads, we need to set the button show what their pref is
        SharedPreferences settings = getSharedPreferences(STATUS_PREFS_NAME, 0);
        Boolean status = settings.getBoolean("Status", true);

        if(status){
            btn.setChecked(true);
            btn.setBackgroundResource(R.color.colorSuccess);
            off.setVisibility(View.INVISIBLE);
            on.setVisibility(View.VISIBLE);


        }else{
            btn.setChecked(false);
            btn.setBackgroundResource(R.color.colorPrimary);
            on.setVisibility(View.INVISIBLE);
            off.setVisibility(View.VISIBLE);
        }

    }


    public void setStatus(final ToggleButton btn, final TextView on, final TextView off, final boolean status){

        //make a DB call to get if the user is set to inactive or active
        //set the boolean and the rest of the code should be guuci

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

            if(token == null) {
                //dat bad
            }

            String stringStatus = status ? "active" : "inactive";

            Call<ResponseBody> call = apiService.updateStatus(new RestInterface().new TokenStatus(token, stringStatus));

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        try {
                            Timber.d("Update successful: " + response.body().string());

                            SharedPreferences settings = getSharedPreferences(STATUS_PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("Status", status);
                            editor.commit();

                            if(status){
                                btn.setChecked(true);
                                btn.setBackgroundResource(R.color.colorSuccess);
                                off.setVisibility(View.INVISIBLE);
                                on.setVisibility(View.VISIBLE);
                            }else{
                                btn.setChecked(false);
                                btn.setBackgroundResource(R.color.colorPrimary);
                                on.setVisibility(View.INVISIBLE);
                                off.setVisibility(View.VISIBLE);
                            }

                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("D", "User registration failed :(");
                    Log.d("D", t.toString());
                }
            });
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void showDeleteDialog(View view) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new DeleteConfirmFragment();
        dialog.show(getSupportFragmentManager(), "DeleteConfirmFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        //Do the delete
        deleteAccount();
        //return to main activity

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Cancel
    }


    public void deleteAccount(){
        //do server stuff here
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

        if(token == null) {
            //dat bad
        }

        Call<ResponseBody> call = apiService.deleteAccount(new RestInterface().new ApiToken(token));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        Timber.d("Delete successful: " + response.body().string());

                        //remove api token from user settings
                        SharedPreferences settings = getSharedPreferences(TOKEN_PREFS_NAME, 0);
                        settings.edit().clear().commit();

                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        startActivity(intent);


                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("D", "User registration failed :(");
                Log.d("D", t.toString());
            }
        });

    }

    }

