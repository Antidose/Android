package antidose.antidose;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
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
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
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
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.directions.v5.models.RouteLeg;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.categorical;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.ARRIVE_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.DEPART_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.HIGH_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.LOW_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.MEDIUM_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.NONE_ALERT_LEVEL;
/*public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
    }
}*/

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback,
        /*MapboxMap.OnMapClickListener,*/ ProgressChangeListener, NavigationEventListener, LocationEngineListener,
        OffRouteListener{

    // Map variables
    @BindView(R.id.mapView)
    MapView mapView;


    private MapboxMap mapboxMap;

    private LocationManager locationManager;

    private List<Marker> pathMarkers = new ArrayList<>();

    // Navigation related variables
    private LocationEngine locationEngine;
    private MapboxNavigation navigation;
    private DirectionsRoute route;
    private LocationLayerPlugin locationLayerPlugin;

    @StyleRes
    private int styleRes;
    @ColorInt
    private int routeDefaultColor;
    @ColorInt
    private int routeModerateColor;
    @ColorInt
    private int routeSevereColor;

    // Map UI
    private static final String CONGESTION_KEY = "congestion";
    private List<String> layerIds;
    private boolean visible;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: 2017-07-11  add checkPermission
        //checkPermission()
        layerIds = new ArrayList<>();
        super.onCreate(savedInstanceState);

        navigation = new MapboxNavigation(this, "pk.eyJ1IjoiZmFicmljYXNpYW4iLCJhIjoiY2ozN3hsd3J1MDE3czJxcXB0bjA4YTJjaCJ9.1ngrjbfPAflOdbG79fEqQg");
        Mapbox.getInstance(this, "pk.eyJ1IjoiZmFicmljYXNpYW4iLCJhIjoiY2ozN3hsd3J1MDE3czJxcXB0bjA4YTJjaCJ9.1ngrjbfPAflOdbG79fEqQg");
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        //header
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationEngine = LostLocationEngine.getLocationEngine(this);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.setSmallestDisplacement(0);
        locationEngine.setInterval(0);
        locationEngine.addLocationEngineListener(this);
        locationEngine.activate();
        locationEngine.requestLocationUpdates();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);




    }



    @Override
    public void onMapReady(MapboxMap mapboxMap){
        this.mapboxMap = mapboxMap;

        locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(LocationLayerMode.NAVIGATION);
        //mapboxMap.setOnMapClickListener(this);
        mapboxMap.setLocationSource(locationEngine);

        //mapboxMap.setLocationSource(locationEngine);
        //newOrigin();
        LatLng latLng = new LatLng();

        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        mapboxMap.setMyLocationEnabled(true);
        mapboxMap.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);

        initialize();

        calculateRoute();
        navigation.addNavigationEventListener(this);
        navigation.addOffRouteListener(this);
        navigation.addProgressChangeListener(this);
        navigation.addAlertLevelChangeListener(new AlertLevelChangeListener() {
            @Override
            public void onAlertLevelChange(int alertLevel, RouteProgress routeProgress) {
                switch (alertLevel) {
                    case HIGH_ALERT_LEVEL:
                        Toast.makeText(NavigationActivity.this, "HIGH", Toast.LENGTH_LONG).show();
                        break;
                    case MEDIUM_ALERT_LEVEL:
                        Toast.makeText(NavigationActivity.this, "MEDIUM", Toast.LENGTH_LONG).show();
                        break;
                    case LOW_ALERT_LEVEL:
                        Toast.makeText(NavigationActivity.this, "LOW", Toast.LENGTH_LONG).show();
                        break;
                    case ARRIVE_ALERT_LEVEL:
                        Toast.makeText(NavigationActivity.this, "ARRIVE", Toast.LENGTH_LONG).show();
                        break;
                    case NONE_ALERT_LEVEL:
                        Toast.makeText(NavigationActivity.this, "NONE", Toast.LENGTH_LONG).show();
                        break;
                    case DEPART_ALERT_LEVEL:
                        Toast.makeText(NavigationActivity.this, "DEPART", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

/*    @Override
    public void onMapClick(@NonNull LatLng point) {

    }*/


    private void newOrigin() {
        if (mapboxMap != null) {
            Location lastLocation = mapboxMap.getMyLocation();
            LatLng latLng = new LatLng();
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            mapboxMap.setMyLocationEnabled(true);
            mapboxMap.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
        }
    }

    private void calculateRoute(){
        Location userLocation = mapboxMap.getMyLocation();
        Position origin = Position.fromCoordinates(userLocation.getLongitude(), userLocation.getLatitude());
        Position destination = Position.fromCoordinates(-123.416981, 48.426303);
        //Position origin = Position.fromCoordinates(locationEngine.getLastLocation().getLatitude(),
        //locationEngine.getLastLocation().getLongitude());
        //navigation.setLocationEngine(locationEngine);
        navigation.getRoute(origin, destination, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(
                    Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d("D",response.toString());
                if (response.body() != null) {
                    if (response.body().getRoutes().size() > 0) {
                        DirectionsRoute route = response.body().getRoutes().get(0);
                        addRoute(route);
                    }
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.d("D",t.toString());
            }

        });
    }

    public void userOffRoute(Location location){

    }

    //public void

    public void onProgressChange(Location location, RouteProgress routeProgress) {
        locationLayerPlugin.forceLocationUpdate(location);
        Timber.d("onProgressChange: fraction of route traveled: %f", routeProgress.getFractionTraveled());
    }

    public void onLocationChanged(Location location){

    }

    public void onConnected(){

    }
    @Override
    public void onRunning(boolean running) {
        if (running) {
            Timber.d("onRunning: Started");
        } else {
            Timber.d("onRunning: Stopped");
        }
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
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        navigation.onStart();

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
        navigation.onStop();
        locationLayerPlugin.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //navigation.removeAlertLevelChangeListener(this);
        navigation.removeNavigationEventListener(this);
        navigation.removeProgressChangeListener(this);
        //navigation.removeOffRouteListener(this);
        navigation.onDestroy();
        // End the navigation session
        navigation.endNavigation();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    private void initialize() {
        layerIds = new ArrayList<>();

        addSource(route);

        TypedArray typedArray = mapView.getContext().obtainStyledAttributes(styleRes, R.styleable.NavigationMapRoute);

        routeDefaultColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeColor,
                ContextCompat.getColor(mapView.getContext(), R.color.mapbox_navigation_route_layer_blue));
        routeModerateColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeModerateCongestionColor,
                ContextCompat.getColor(mapView.getContext(), R.color.mapbox_navigation_route_layer_congestion_yellow));
        routeSevereColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeSevereCongestionColor,
                ContextCompat.getColor(mapView.getContext(), R.color.mapbox_navigation_route_layer_congestion_red));
        @ColorInt int routeShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_routeShieldColor,
                ContextCompat.getColor(mapView.getContext(), R.color.mapbox_navigation_route_shield_layer_color));
        float routeScale = typedArray.getFloat(R.styleable.NavigationMapRoute_routeScale, 1.0f);

        addNavigationRouteLayer(routeScale);
        addNavigationRouteShieldLayer(routeShieldColor, routeScale);
        typedArray.recycle();
    }


    /**
     * Add the route layer to the map either using the custom style values or the default.
     */
    private void addNavigationRouteLayer(float scale) {
        Layer routeLayer = new LineLayer(NavigationMapLayers.NAVIGATION_ROUTE_LAYER,
                NavigationMapSources.NAVIGATION_ROUTE_SOURCE).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                PropertyFactory.lineJoin(Property.LINE_CAP_SQUARE),
                PropertyFactory.visibility(Property.NONE),
                PropertyFactory.lineWidth(Function.zoom(
                        exponential(
                                stop(4f, PropertyFactory.lineWidth(2f * scale)),
                                stop(10f, PropertyFactory.lineWidth(3f * scale)),
                                stop(13f, PropertyFactory.lineWidth(4f * scale)),
                                stop(16f, PropertyFactory.lineWidth(7f * scale)),
                                stop(19f, PropertyFactory.lineWidth(14f * scale)),
                                stop(22f, PropertyFactory.lineWidth(18f * scale))
                        ).withBase(1.5f))
                ),
                PropertyFactory.lineColor(
                        Function.property(CONGESTION_KEY, categorical(
                                stop("moderate", PropertyFactory.lineColor(routeModerateColor)),
                                stop("heavy", PropertyFactory.lineColor(routeSevereColor)),
                                stop("severe", PropertyFactory.lineColor(routeSevereColor))
                        )).withDefaultValue(PropertyFactory.lineColor(routeDefaultColor)))
        );
        addLayerToMap(routeLayer, placeRouteBelow());
    }

    /**
     * Add the route shield layer to the map either using the custom style values or the default.
     */
    private void addNavigationRouteShieldLayer(@ColorInt int routeShieldColor, float scale) {
        Layer routeLayer = new LineLayer(NavigationMapLayers.NAVIGATION_ROUTE_SHIELD_LAYER,
                NavigationMapSources.NAVIGATION_ROUTE_SOURCE).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.visibility(Property.NONE),
                PropertyFactory.lineWidth(Function.zoom(
                        exponential(
                                stop(16f, PropertyFactory.lineWidth(0f)),
                                stop(16.5f, PropertyFactory.lineWidth(10.5f * scale)),
                                stop(19f, PropertyFactory.lineWidth(21f * scale)),
                                stop(22f, PropertyFactory.lineWidth(27f * scale))
                        ).withBase(1.5f))
                ),
                PropertyFactory.lineColor(routeShieldColor)
        );
        addLayerToMap(routeLayer, NavigationMapLayers.NAVIGATION_ROUTE_LAYER);
    }

    /**
     * Generic method for adding layers to the map.
     */
    private void addLayerToMap(@NonNull Layer layer, @Nullable String idBelowLayer) {
        if (idBelowLayer == null) {
            mapboxMap.addLayer(layer);
        } else {
            mapboxMap.addLayerBelow(layer, idBelowLayer);
        }
        layerIds.add(layer.getId());
    }

    /**
     * Iterate through map style layers backwards till the first not-symbol layer is found.
     */
    private String placeRouteBelow() {
        List<Layer> styleLayers = mapboxMap.getLayers();
        for (int i = styleLayers.size() - 1; i >= 0; i--) {
            if (!(styleLayers.get(i) instanceof SymbolLayer)) {
                return styleLayers.get(i).getId();
            }
        }
        return null;
    }


    public void addRoute(@NonNull DirectionsRoute route) {
        this.route = route;
        addSource(route);
        setLayerVisibility(true);
    }

    /**
     * Adds the route source to the map.
     */
    private void addSource(@Nullable DirectionsRoute route) {
        FeatureCollection routeLineFeature;
        // Either add an empty GeoJson featureCollection or the route's Geometry
        if (route == null) {
            routeLineFeature = FeatureCollection.fromFeatures(new Feature[] {});
        } else {
            routeLineFeature = addTrafficToSource(route);
        }

        // Determine whether the source needs to be added or updated
        GeoJsonSource source = mapboxMap.getSourceAs(NavigationMapSources.NAVIGATION_ROUTE_SOURCE);
        if (source == null) {
            GeoJsonSource routeSource = new GeoJsonSource(NavigationMapSources.NAVIGATION_ROUTE_SOURCE, routeLineFeature);
            mapboxMap.addSource(routeSource);
        } else {
            source.setGeoJson(routeLineFeature);
        }
    }

    private FeatureCollection addTrafficToSource(DirectionsRoute route) {
        List<Feature> features = new ArrayList<>();
        LineString originalGeometry = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
        features.add(Feature.fromGeometry(originalGeometry));

        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
        for (RouteLeg leg : route.getLegs()) {
            if (leg.getAnnotation() != null) {
                if (leg.getAnnotation().getCongestion() != null) {
                    for (int i = 0; i < leg.getAnnotation().getCongestion().length; i++) {
                        double[] startCoord = lineString.getCoordinates().get(i).getCoordinates();
                        double[] endCoord = lineString.getCoordinates().get(i + 1).getCoordinates();

                        LineString congestionLineString = LineString.fromCoordinates(new double[][] {startCoord, endCoord});
                        Feature feature = Feature.fromGeometry(congestionLineString);
                        feature.addStringProperty(CONGESTION_KEY, leg.getAnnotation().getCongestion()[i]);
                        features.add(feature);
                    }
                }
            } else {
                Feature feature = Feature.fromGeometry(lineString);
                features.add(feature);
            }
        }
        return FeatureCollection.fromFeatures(features);
    }


    private void setLayerVisibility(boolean visible) {
        this.visible = visible;
        List<Layer> layers = mapboxMap.getLayers();
        String id;

        for (Layer layer : layers) {
            id = layer.getId();
            if (layerIds.contains(layer.getId())) {
                if (id.equals(NavigationMapLayers.NAVIGATION_ROUTE_LAYER)
                        || id.equals(NavigationMapLayers.NAVIGATION_ROUTE_SHIELD_LAYER)) {
                    layer.setProperties(PropertyFactory.visibility(visible ? Property.VISIBLE : Property.NONE));
                }
            }
        }
    }

    static class NavigationMapLayers {
        static final String NAVIGATION_ROUTE_SHIELD_LAYER = "mapbox-plugin-navigation-route-shield-layer";
        static final String NAVIGATION_ROUTE_LAYER = "mapbox-plugin-navigation-route-layer";
    }

    static class NavigationMapSources {
        static final String NAVIGATION_ROUTE_SOURCE = "mapbox-plugin-navigation-route-source";
    }
}