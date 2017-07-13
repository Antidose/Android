package antidose.antidose;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import static antidose.antidose.R.id.editTextPhoneNumber;
import static antidose.antidose.R.id.imgVerifySuccess;

public class VerificationActivity extends AppCompatActivity {
    String phoneNumber;

    EditText editTextVerify;
    ImageView imgVerifySuccess;
    String lastChar = "";

    public class UserVerify {

        String token;
        String phone_number;

        public UserVerify(String phoneNumber, String token) {
            this.phone_number = phoneNumber;
            this.token = token;
        }
    }
    public interface restInterface {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter


        @POST("verify")
        Call<UserVerify> verifyUser(@Body UserVerify user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra(RegistrationActivity.EXTRA_MESSAGE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

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


        UserVerify userVerify = new UserVerify(phoneNumber, verifyNumber);
        Call<UserVerify> call = apiService.verifyUser(userVerify);
        Log.d("D", userVerify.phone_number + " " + userVerify.token );
        call.enqueue(new Callback<UserVerify>() {
            @Override
            public void onResponse(Call<UserVerify> call, Response<UserVerify> response) {
                Log.d("D", response.toString());
                imgVerifySuccess = (ImageView) findViewById(R.id.imgVerifySuccess);
                imgVerifySuccess.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(Call<UserVerify> call, Throwable t) {
                Log.d("D", "User verification failed :(");
                Log.d("D", t.toString());
                editTextVerify.setText("");
                editTextVerify.setEnabled(true);
            }
        });

    }

    //temp should be on main page
    public void goSetting(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);

    }
}
