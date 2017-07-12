package antidose.antidose;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import timber.log.Timber;



public class RegistrationActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextPhoneNumber;
    TextView textViewError;
    String lastChar = " ";
    public class User {

        String first_name;
        String last_name;
        String phone_number;
        //String current_status;

        public User(String firstName, String lastName, String phoneNumber ) {
            this.first_name = firstName;
            this.last_name = lastName;
            this.phone_number = phoneNumber;
        }
    }
    public interface restInterface {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter


        @POST("register")
        Call<User> createUser(@Body User user);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int digits = editTextPhoneNumber.getText().toString().length();
                if (digits > 1)
                    lastChar = editTextPhoneNumber.getText().toString().substring(digits-1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int digits = editTextPhoneNumber.getText().toString().length();
                Log.d("LENGTH",""+digits);
                if (!lastChar.equals("-")) {
                    if (digits == 3 || digits == 7) {
                        editTextPhoneNumber.append("-");
                    }
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

    public void register(View view) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restInterface apiService =
                retrofit.create(restInterface.class);

        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        User user = new User(firstName, lastName, phoneNumber);
        Call<User> call = apiService.createUser(user);
        Log.d("D", user.first_name + " " + user.last_name + " " + user.phone_number);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("D", response.toString());
                verification();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("D", "User registration failed :(");
                Log.d("D", t.toString());
                textViewError = (TextView) findViewById(R.id.textViewError);
                textViewError.setText(t.toString());
                textViewError.setVisibility(View.VISIBLE);
            }
        });

        //// TODO: 2017-07-06 fix callbacks and handle the proper responses
        verification();
    }

    public void verification(){
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra(EXTRA_MESSAGE, phoneNumber);
        startActivity(intent);
    }
}
