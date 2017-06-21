package antidose.antidose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

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
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextPhoneNumber;
    public class User {

        String first_name;
        String last_name;
        String phone_number;
        String current_status;

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


    public static final String BASE_URL = "http://c76d1510.ngrok.io/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
    }

    public void register(View view) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
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
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("D", "User registration failed :(");
                Log.d("D", t.toString());
            }
        });
        /*
        String formData = "username=<uname>&password=<pass>&grant_type=password";
        String header = "Basic " + Base64. ("<client_id>:<client_secret>");
        try {


            //URL url = "";

            HttpURLConnection connection
                    = (HttpURLConnection) new URL(tokenUrl).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.addRequestProperty("Authorization", header);
            connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(formData.length()));

            OutputStream out = connection.getOutputStream();
            out.write(formData.getBytes(StandardCharsets.UTF_8));

            InputStream in = connection.getInputStream();
            //JSONObject json = in.toString();
            //AccessToken token = new ObjectMapper().readValue(in, AccessToken.class);
            System.out.println(json);

            out.close();
            in.close();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }*/
    }
}
