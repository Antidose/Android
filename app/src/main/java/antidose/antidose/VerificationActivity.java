package antidose.antidose;

import antidose.antidose.RestInterface.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class VerificationActivity extends AppCompatActivity {
    String phoneNumber;

    EditText editTextVerify;
    ImageView imgVerifyFail;
    TextView  textVerifyFail;
    public static final String PREFS_NAME = "User_Token";
    public static final String FIREBASE_ID = "Firebase_ID";
    String lastChar = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra(RegistrationActivity.EXTRA_MESSAGE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        updateFonts();

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        editTextVerify = (EditText) findViewById(R.id.editTextVerify);
        editTextVerify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /*int digits = editTextVerify.getText().toString().length();
                if (digits > 1)
                    lastChar = editTextVerify.getText().toString().substring(digits-1);*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*int digits = editTextVerify.getText().toString().length();
                Log.d("LENGTH",""+digits);
                if (!lastChar.equals("-")) {
                    if (digits == 3 || digits == 7) {
                        editTextVerify.append("-");
                    }
                }*/
                if (editTextVerify.getText().toString().length() > 5){
                    register();
                    editTextVerify.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void register() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restInterface apiService =
                retrofit.create(restInterface.class);

        String verifyNumber = editTextVerify.getText().toString().trim();

        // Get Firebase ID
        SharedPreferences settings = getSharedPreferences(FIREBASE_ID, 0);
        String firebaseId = settings.getString(FIREBASE_ID, "");

        UserVerify userVerify = new RestInterface().new UserVerify(phoneNumber, verifyNumber, firebaseId);
        Call<ApiToken> call = apiService.verifyUser(userVerify);
        Timber.d(userVerify.phone_number + " " + userVerify.token );


        call.enqueue(new Callback<ApiToken>() {
            @Override
            public void onResponse(Call<ApiToken> call, Response<ApiToken> response) {
                if (response.isSuccessful()){
                    Timber.d("Verification successful: " + response.toString());
                    String apiToken = response.body().getApiToken();

                    // Show success symbol

                    // We need an Editor object to make preference changes.
                    // All objects are from android.context.Context
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("Token", apiToken);

                    // Commit the edits!
                    editor.commit();

                    Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Timber.d("User verification failed: ");
                    editTextVerify.setText("");
                    editTextVerify.setEnabled(true);

                    imgVerifyFail = (ImageView) findViewById(R.id.imageViewX);
                    imgVerifyFail.setVisibility(View.VISIBLE);

                    textVerifyFail = (TextView) findViewById(R.id.textViewX);
                    textVerifyFail.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiToken> call, Throwable t) {

                Timber.d("User verification failed: " + t.toString());
                editTextVerify.setText("");
                editTextVerify.setEnabled(true);

                imgVerifyFail = (ImageView) findViewById(R.id.imageViewX);
                imgVerifyFail.setVisibility(View.VISIBLE);

                textVerifyFail = (TextView) findViewById(R.id.textViewX);
                textVerifyFail.setVisibility(View.VISIBLE);
            }
        });

    }
    public void updateFonts(){

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "font/gravitylight.otf");

        TextView tx = (TextView)findViewById(R.id.textView5);
        tx.setTypeface(custom_font);

        EditText ex1 = (EditText)findViewById(R.id.editTextVerify);
        ex1.setTypeface(custom_font);

        Button b1 = (Button)findViewById(R.id.button);
        b1.setTypeface(custom_font);

    }


    //temp should be on main page
    public void goSetting(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);

    }
}
