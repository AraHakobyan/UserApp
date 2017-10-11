package com.example.aropc.myapplication.fragment;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.example.aro_pc.myapplication.R;
import com.mapbox.androidsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.Constants;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.services.android.telemetry.location.LocationEnginePriority.HIGH_ACCURACY;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, LocationEngineListener, Callback<DirectionsResponse>, MapboxMap.OnMapClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int CAMERA_ANIMATION_DURATION = 1000;

    private LocationLayerPlugin locationLayer;
    private LocationEngine locationEngine;
    private NavigationMapRoute mapRoute;
    private MapboxMap mapboxMap;

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.launchRouteBtn)
    Button launchRouteBtn;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.launchCoordinatesBtn)
    Button launchCoordinatesBtn;

    private CheckBox simulateCheckBox;
    private boolean isSimulatedOn = false;

    private Marker currentMarker;
    private LatLng currentLatLng;
    private Position currentPosition;
    private Position destination;
    private DirectionsRoute route;

    private boolean locationFound;
    private BuildingPlugin buildingPlugin;
    private Button building;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(getActivity());
        mapView = (MapView) view.findViewById(R.id.mapView);
        launchRouteBtn = (Button) view.findViewById(R.id.launchRouteBtn);
        loading = view.findViewById(R.id.loading);
        launchCoordinatesBtn = view.findViewById(R.id.launchCoordinatesBtn);
        simulateCheckBox = (CheckBox) view.findViewById(R.id.simulate_navigation);
        simulateCheckBox.setOnCheckedChangeListener(this);
        building = view.findViewById(R.id.building);

        launchRouteBtn.setOnClickListener(this);
        launchCoordinatesBtn.setOnClickListener(this);
        building.setOnClickListener(this);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }


    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        if (locationLayer != null) {
            locationLayer.onStart();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationEngine != null) {
            locationEngine.addLocationEngineListener(this);
            if (!locationEngine.isConnected()) {
                locationEngine.activate();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);

        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationLayer != null) {
            locationLayer.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
            locationEngine.deactivate();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    public void onCoordinatesLaunchClick() {
        launchNavigationWithCoordinates();
    }
    public void onBuildingClick() {
        showBuildings();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.setOnMapClickListener(this);
//        mapboxMap.getUiSettings().setZoomGesturesEnabled(true);
//        mapboxMap.getUiSettings().setTiltGesturesEnabled(false);
        buildingPlugin = new BuildingPlugin(mapView, mapboxMap);
        buildingPlugin.setMinZoomLevel(15);
        initLocationEngine();
        initLocationLayer();
        initMapRoute();
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentPosition = Position.fromCoordinates(location.getLongitude(), location.getLatitude());
        onLocationFound(location);

    }

    @Override
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        if (validRouteResponse(response)) {
            route = response.body().getRoutes().get(0);
            launchRouteBtn.setEnabled(true);
            mapRoute.addRoute(route);
            boundCameraToRoute();
            hideLoading();
        }
    }

    @Override
    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Timber.e(throwable.getMessage());
    }

    @SuppressWarnings({"MissingPermission"})
    private void initLocationEngine() {
        locationEngine = new LocationSource(getActivity());
        locationEngine.setPriority(HIGH_ACCURACY);
        locationEngine.setInterval(0);
        locationEngine.setFastestInterval(1000);
        locationEngine.addLocationEngineListener(this);
        locationEngine.activate();

        if (locationEngine.getLastLocation() != null) {
            Location lastLocation = locationEngine.getLastLocation();
            currentPosition = Position.fromCoordinates(lastLocation.getLongitude(), lastLocation.getLatitude());
            currentLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void initLocationLayer() {
        locationLayer = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
        locationLayer.setLocationLayerEnabled(LocationLayerMode.COMPASS);
    }

    private void initMapRoute() {
        mapRoute = new NavigationMapRoute(mapView, mapboxMap);
    }

    private void fetchRoute() {
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(currentPosition)
                .destination(destination)
                .build()
                .getRoute(this);
        loading.setVisibility(View.VISIBLE);
    }

    private void launchNavigationWithRoute() {
        if (route != null) {
            NavigationLauncher.startNavigation(getActivity(), route,
                    "ru", isSimulatedOn);
        }
//        Voice voice = new Voice();
//        voice.setLanguageCode("ru-RU");
    }

    private void launchNavigationWithCoordinates() {
        if (currentPosition != null && destination != null) {
            NavigationLauncher.startNavigation(getActivity(), currentPosition, destination,
                    null, isSimulatedOn);
        }
    }

    private boolean isBuidings3D = false;

    private void showBuildings(){
        if (!isBuidings3D){

            setBuildings3D();
        } else {
            setBuildings2d();

        }
    }

    private void setBuildings3D() {
        isBuidings3D = true;
        buildingPlugin.setVisibility(true);
//        animateCamera(currentLatLng, 4);
        setTilt(60, CAMERA_ANIMATION_DURATION,16);
    }

    private void setBuildings2d() {
        isBuidings3D = false;
        buildingPlugin.setVisibility(false);
        setTilt(0, CAMERA_ANIMATION_DURATION,16);
        boundCameraToRoute();

    }


    private boolean validRouteResponse(Response<DirectionsResponse> response) {
        return response.body() != null
                && response.body().getRoutes() != null
                && response.body().getRoutes().size() > 0;
    }

    private void hideLoading() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    private void onLocationFound(Location location) {
        if (!locationFound) {
            animateCamera(new LatLng(location.getLatitude(), location.getLongitude()),16);
            locationFound = true;
            hideLoading();
        }
    }

    public void boundCameraToRoute() {
        if (route != null) {
            List<Position> routeCoords = LineString.fromPolyline(route.getGeometry(),
                    Constants.PRECISION_6).getCoordinates();
            List<LatLng> bboxPoints = new ArrayList<>();
            for (Position position : routeCoords) {
                bboxPoints.add(new LatLng(position.getLatitude(), position.getLongitude()));
            }
            LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
            animateCameraBbox(bounds, CAMERA_ANIMATION_DURATION, new int[] {50, 100, 50, 100});
        }
    }

    private void animateCameraBbox(LatLngBounds bounds, int animationTime, int[] padding) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                padding[0], padding[1], padding[2], padding[3]), animationTime);
    }

    private void animateCamera(LatLng point, int zoomLevel) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoomLevel));
    }

    private void setTilt(double tilt, int duration,int zoomLevel){
        CameraPosition cameraPosition = new CameraPosition.Builder().tilt(tilt).zoom(zoomLevel).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), duration);
    }

    private void setCurrentMarkerPosition(LatLng position) {
        if (position != null) {
            if (currentMarker == null) {
                MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                        .position(position);
                currentMarker = mapboxMap.addMarker(markerViewOptions);
            } else {
                currentMarker.setPosition(position);
            }
        }
    }


    @Override
    public void onMapClick(@NonNull LatLng point) {
        destination = Position.fromCoordinates(point.getLongitude(), point.getLatitude());
        launchCoordinatesBtn.setEnabled(true);
        launchRouteBtn.setEnabled(false);
        loading.setVisibility(View.VISIBLE);
        setCurrentMarkerPosition(point);
        if (currentPosition != null) {
            fetchRoute();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.building:
                onBuildingClick();
                break;
            case R.id.launchCoordinatesBtn:
                launchNavigationWithCoordinates();
                break;
            case R.id.launchRouteBtn:
                launchNavigationWithRoute();

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        isSimulatedOn = b;
    }
}
