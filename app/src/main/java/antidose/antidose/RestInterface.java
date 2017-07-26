package antidose.antidose;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by graeme on 2017-07-04.
 */

public class RestInterface {

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
        double latitude;
        double longitude;


        public Alert(String IMEI, double latitude, double longitude) {
            this.IMEI = IMEI;
            this.latitude = latitude;
            this.longitude = longitude;
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
        String inc_id;
        boolean has_kit;
        boolean is_going;

        public Responder(String api_token, String inc_id, boolean hasKit, boolean isGoing) {
            this.api_token = api_token;
            this.inc_id = inc_id;
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
        Location location;

        public ResponderGeometry(String api_token, Location location) {
            this.api_token = api_token;
            this.location = location;
        }
    }

    class UpdateFirebase{
        String api_token;
        String firebase_token;

        public UpdateFirebase(String api, String fb){
            this.api_token = api;
            this.firebase_token = fb;
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

            @SerializedName("dist")
            @Expose
            private float dist;
            @SerializedName("time")
            @Expose
            private float time;

            public float getDist() {
                return dist;
            }

            public float getTime() {
                return time;
            }
        }
    class ResponderIncLatLong{
        String api_token;
        String inc_id;
        double latitude;
        double longitude;

        public ResponderIncLatLong(String api_token, String inc_id, double latitude,double longitude) {
            this.api_token = api_token;
            this.inc_id = inc_id;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    class startIncidentResponse{


            @SerializedName("incident_id")
            @Expose
            private String incidentId;
            @SerializedName("num_notified")
            @Expose
            private int numNotified;
            @SerializedName("radius")
            @Expose
            private int radius;

            public String getIncidentId() {
                return incidentId;
            }

            public int getNumNotified() {
                return numNotified;
            }

            public int getRadius() {
                return radius;
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

        @POST("startIncident")
        Call<startIncidentResponse> sendHelp(@Body Alert alert);

        @POST("stopIncident")
        Call<ResponseBody> cancelSearch(@Body CancelSearch cancel);

        @POST("respondIncident")
        Call<IncidentLocation> respondIncident(@Body Responder response);

        @POST("numResponders")
        Call<NumberResponders> numberResponders(@Body ApiToken token);

        @POST("getInfoResponder")
        Call<MapInformation> requestInfo(@Body ResponderIncLatLong responder);

        @POST("location")
        Call<ResponseBody> sendLocationUpdate(@Body ResponderLatLong responder);

        @POST("updateFirebaseToken")
        Call<ResponseBody> UpdateFirebaseToken(@Body UpdateFirebase updater);

    }
}
