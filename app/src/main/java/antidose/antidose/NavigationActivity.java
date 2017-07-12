package antidose.antidose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.navigation.v5.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.NavigationConstants;
import com.mapbox.services.android.navigation.v5.RouteProgress;
import com.mapbox.services.android.navigation.v5.listeners.AlertLevelChangeListener;
import com.mapbox.services.android.navigation.v5.listeners.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.listeners.OffRouteListener;
import com.mapbox.services.android.navigation.v5.listeners.ProgressChangeListener;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
/*public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
    }
}*/

public class NavigationActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxNavigation navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //navigation = new MapboxNavigation(this, "pk.eyJ1IjoiZmFicmljYXNpYW4iLCJhIjoiY2ozN3hsd3J1MDE3czJxcXB0bjA4YTJjaCJ9.1ngrjbfPAflOdbG79fEqQg");
        Mapbox.getInstance(this, "pk.eyJ1IjoiZmFicmljYXNpYW4iLCJhIjoiY2ozN3hsd3J1MDE3czJxcXB0bjA4YTJjaCJ9.1ngrjbfPAflOdbG79fEqQg");
        setContentView(R.layout.activity_navigation);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        /*Position origin = Position.fromCoordinates(38.90992, -77.03613);
        Position destination = Position.fromCoordinates(38.8977, -77.0365);
        LocationEngine locationEngine = LostLocationEngine.getLocationEngine(this);
        navigation.setLocationEngine(locationEngine);
        navigation.getRoute(origin, destination, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(
                    Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

            }
        });*/

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
/*        navigation.removeAlertLevelChangeListener();
        navigation.removeNavigationEventListener(this);
        navigation.removeProgressChangeListener(this);
        navigation.removeOffRouteListener(this);

        // End the navigation session
        navigation.endNavigation();*/
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}