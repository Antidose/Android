package antidose.antidose;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by graeme on 2017-07-04.
 */

public class RestInterface {
    public static class User {

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

    public static class UserVerify {

        String token;
        String phone_number;

        public UserVerify(String phoneNumber, String token) {
            this.phone_number = phoneNumber;
            this.token = token;
        }
    }

    public class ApiToken {

        @SerializedName("api_token")
        @Expose
        private String apiToken;

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

    }


    public interface restInterface {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter


        @POST("register")
        Call<ResponseBody> createUser(@Body User user);

        @POST("verify")
        Call<ApiToken> verifyUser(@Body UserVerify user);

    }
}
