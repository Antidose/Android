package antidose.antidose;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.github.filosganga.geogson.model.Geometry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by graeme on 2017-07-04.
 */

public class RestInterface extends AppCompatActivity{

     class User {

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

    class UserVerify {

        String token;
        String phone_number;
        String firebase_id;

        public UserVerify(String phoneNumber, String token, String firebase_id) {
            this.phone_number = phoneNumber;
            this.token = token;
            this.firebase_id = firebase_id;
        }
    }

    class ApiToken {

        @SerializedName("api_token")
        @Expose
        private String apiToken;

        public ApiToken(String token) {
            this.apiToken = token;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

    }

    class TokenStatus {

        String api_token;
        String status;

        public TokenStatus(String token, String status) {
            this.api_token = token;
            this.status = status;
        }
    }

    class Alert {

        String IMEI;
        Location location;

        public Alert(String IMEI, Location location) {
            this.IMEI = IMEI;
            this.location = location;
        }
    }

    class CancelSearch{

        String IMEI;
        boolean is_resolved;

        public CancelSearch(String IMEI, boolean isResolved) {
            this.IMEI = IMEI;
            this.is_resolved = isResolved;

        }

    }

    class Responder{

        String api_token;
        boolean has_kit;
        boolean is_going;

        public Responder(String api_token, boolean hasKit, boolean isGoing) {
            this.api_token = api_token;
            this.has_kit = hasKit;
            this.is_going = isGoing;

        }

    }

    class IncidentLocation{

        private float latitude;
        private float longitude;

        public IncidentLocation(float lat, float lon) {
            this.latitude = lat;
            this.longitude = lon;
        }

        public float getLatitude() {
            return latitude;
        }
        public float getLongitude() {
            return longitude;
        }

    }

    class ResponderLocation{
        String api_token;
        Location location;

        public ResponderLocation(String api_token, Location location) {
            this.api_token = api_token;
            this.location = location;
        }
    }

    class ResponderGeometry{
        String api_token;
        Geometry location;

        public ResponderGeometry(String api_token, Geometry location) {
            this.api_token = api_token;
            this.location = location;
        }
    }

    class NumberResponders{

        @Expose
        private int responders;

        public NumberResponders(int coming) {
            this.responders = coming;
        }

        public int getResponders() {
            return this.responders;
        }

    }

    class MapInformation{

        @Expose
        private float distance;
        private float duration;

        public MapInformation(float dist, float dur) {
            this.distance = dist;
            this.duration = dur;
        }

        public float getDistance() {
            return distance;
        }

        public float getDuration() {
            return duration;

        }

    }

    class ResponderLatLong{
        String api_token;
        double latitude;
        double longitude;

        public ResponderLatLong(String api_token, double latitude,double longitude) {
            this.api_token = api_token;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    interface restInterface {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter


        @POST("register")
        Call<ResponseBody> createUser(@Body User user);

        @POST("verify")
        Call<ApiToken> verifyUser(@Body UserVerify user);

        @POST("userStatus")
        Call<ResponseBody> updateStatus(@Body TokenStatus tokstat);

        @POST("deleteAccount")
        Call<ResponseBody> deleteAccount(@Body ApiToken token);

        @POST("alert")
        Call<ResponseBody> sendHelp(@Body Alert alert);

        @POST("stopIncident")
        Call<ResponseBody> cancelSearch(@Body CancelSearch cancel);

        @POST("respondIncident")
        Call<IncidentLocation> respondIncident(@Body Responder response);

        @POST("numResponders")
        Call<NumberResponders> numberResponders(@Body ApiToken token);

        @POST("requestInfo")
        Call<MapInformation> requestInfo(@Body ResponderLocation responder);

        @POST("location")
        Call<ResponseBody> sendLocationUpdate(@Body ResponderLatLong responder);

    }
}
