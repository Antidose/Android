package antidose.antidose;

import antidose.antidose.RestInterface.*;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class RegistrationActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextPhoneNumber;
    TextView textViewError;
    String lastChar = " ";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        updateFonts();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public void updateFonts(){

            Typeface custom_font = Typeface.createFromAsset(getAssets(), "font/gravitylight.otf");
            Typeface custom_font_regular = Typeface.createFromAsset(getAssets(), "font/gravityregular.otf");

            TextView tx = (TextView)findViewById(R.id.textViewRegister);
            tx.setTypeface(custom_font);

            EditText ex1 = (EditText)findViewById(R.id.editTextFirstName);
            EditText ex2 = (EditText)findViewById(R.id.editTextLastName);
            EditText ex3 = (EditText)findViewById(R.id.editTextPhoneNumber);

            ex1.setTypeface(custom_font);
            ex2.setTypeface(custom_font);
            ex3.setTypeface(custom_font);

            Button b1 = (Button)findViewById(R.id.buttonRegister);
            b1.setTypeface(custom_font);

        }


    public void register(View view) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getText(R.string.server_url).toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface.restInterface apiService =
                retrofit.create(RestInterface.restInterface.class);

        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        Call<ResponseBody> call = apiService.createUser(new RestInterface().new User(firstName, lastName, phoneNumber));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        Timber.d("Registration successful: " + response.body().string());
                        verification();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("D", "User registration failed :(");
                Log.d("D", t.toString());
                textViewError = (TextView) findViewById(R.id.textViewError);
                textViewError.setText(t.toString());
                textViewError.setVisibility(View.VISIBLE);
            }
        });
    }



    public void verification(){
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra(EXTRA_MESSAGE, phoneNumber);
        startActivity(intent);
    }
}
