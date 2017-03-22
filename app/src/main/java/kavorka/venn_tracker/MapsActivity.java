package kavorka.venn_tracker;

import kavorka.venn_tracker.MapInfoWindow.MapWrapperLayout;
import kavorka.venn_tracker.MapInfoWindow.OnInfoWindowElemTouchListener;
import kavorka.venn_tracker.Tutorials.TutorialsActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;


import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.PopupMenu;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import java.util.ArrayList;
import java.util.Calendar;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PopupMenu.OnMenuItemClickListener {

    private GoogleMap mMap;
    private Button mMenu;
    private ImageButton mButtonCircleGreen;
    private ImageButton mButtonCircleRed;
    private boolean mMarkerVisibility;
    private GoogleApiClient mGoogleApiClient;
    private static Location mLastLocation;
    private Marker mCurrLocationMarker;
    private ArrayList<LatLng> mPolygonPointsGreen;
    private SharedPreferences mSharedPref;
    private boolean mMoveCamera;
    private boolean mCircleSnap;
    private boolean mFirstTime;
    private boolean mMarkerTransparency;
    private boolean mLoadDistance;
    private int mLoadDistanceInt;
    private Button mButtonMarkerDelete;
    private Button mButtonMarkerAddTime;
    private Button mButtonMarkerDescription;
    private Button mButtonMarkerDescriptionOK;
    private EditText mMarkerDescriptionText;
    private OnInfoWindowElemTouchListener mInfoButtonTimeListener;
    private OnInfoWindowElemTouchListener mInfoButtonDeleteListener;
    private OnInfoWindowElemTouchListener mInfoDescriptionListener;
    private ViewGroup mInfoWindow;
    private int mFeatureCode;
    private RelativeLayout mMapClickMenu;
    private RelativeLayout mMarkerDescriptionLayout;
    private ImageButton mMapClickMarker;
    private ImageButton mMapClickAdd;
    private ImageButton mMapClickSubtract;
    private ImageButton mMapClickCancel;

    private ImageButton mOverlayedButtonGreen;
    private ImageButton mOverlayedButtonRed;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;

    public final static int REQUEST_CODE_OVERLAY = 6563;
    public final static int REQUEST_CODE_WRITE = 2909;
    private int mCircleResolution;
    private View topLeftView;

    private String STATIC_MAP_API_ENDPOINT;
    String path;

    private ImageView mMapImageView;
    private boolean mMoveOverlay;
    private Bitmap mOverlayBitmap;
    private ArrayList<Marker> overlayMarkers;
    private ArrayList<Marker> spawnPoints;
    private Bitmap bmp = null;
    private String mOverlayMapKey = "&key=AIzaSyCShhJ7WOGddqbTcDhfXe2NcWVfjruD0FI";
    private String mOverlayMapCenter;
    private String mOverlayMapPolygon;
    private String mOverlayMapMarkers;
    private String mOverlayMapZoom;
    private String mOverlayMapLocation;

    private String mOverlayMapFormat;
    private String mOverlayMapStyle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        // Window manager for overlay
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // Setup buttons for overlay
        mOverlayedButtonGreen = new ImageButton(MapsActivity.this);
        mOverlayedButtonRed = new ImageButton(MapsActivity.this);
        mMapImageView = new ImageView(MapsActivity.this);
        mOverlayedButtonGreen.setBackgroundResource(R.drawable.ic_circle_green_overlay);
        mOverlayedButtonRed.setBackgroundResource(R.drawable.ic_circle_red_overlay);









        mSharedPref = MapsActivity.this
                .getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);



        // Initialize custom marker window
        this.mInfoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.marker_window, null);

        // Initialize buttons for marker menu
        mButtonMarkerAddTime = (Button) mInfoWindow.findViewById(R.id.markerAddTime);
        mButtonMarkerDelete = (Button) mInfoWindow.findViewById(R.id.markerDelete);
        mButtonMarkerDescription = (Button) mInfoWindow.findViewById(R.id.markerDescription);

        //Initialize buttons
        mButtonCircleGreen = (ImageButton) findViewById(R.id.button_circle_green_overlay);
        mButtonCircleRed = (ImageButton) findViewById(R.id.button_circle_red);

        // Initialize menu button
        mMenu = (Button) findViewById(R.id.buttonMenu);

        // Initialize map touch menu and buttons
        mMapClickMenu = (RelativeLayout) findViewById(R.id.onMapTouchLayout);
        mMarkerDescriptionLayout = (RelativeLayout) findViewById(R.id.markerDescriptionLayout);

        mMapClickMarker = (ImageButton) findViewById(R.id.button_map_click_marker);
        mMapClickAdd = (ImageButton) findViewById(R.id.button_map_click_add);
        mMapClickSubtract = (ImageButton) findViewById(R.id.button_map_click_subtract);
        mMapClickCancel = (ImageButton) findViewById(R.id.button_map_click_cancel);

        mButtonMarkerDescriptionOK = (Button) findViewById(R.id.markerDescriptionButtonOk);
        mMarkerDescriptionText = (EditText) findViewById(R.id.markerDescriptionText);


        /*Check the stored feature code against app version
        if feature code is less, this is the first time opening the app with the new version.
        Show user any new features and make feature code equal to app version.*/
        mFeatureCode = mSharedPref.getInt("feature_code", 0);
        mFirstTime = mSharedPref.getBoolean("first_time", true);

        if (mFirstTime){
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage(R.string.First_Time);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                   showNewFeatures();
                }
            });
            builder.show();
        }
        else {
            showNewFeatures();
        }
    }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady (final GoogleMap googleMap){
            mMap = googleMap;

            // Check if any of our spawn points are active every minute
            final Handler spawnTimeHandler = new Handler();
            spawnTimeHandler.postDelayed(new Runnable() {
                public void run() {
                    SpawnLocation.checkSpawnTimes();
                    spawnTimeHandler.postDelayed(this, 1000 * 30); //Check every 30 seconds
                }
            }, 1000 * 30);



            final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);

            // MapWrapperLayout initialization
            // 39 - default marker height
            // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
            mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));


            //Initialize Google Play Services
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }

            overlayMarkers = new ArrayList<>();
            spawnPoints = SpawnLocation.getSpawnPoints();


            //Check for user settings and load data if available
            mFirstTime = mSharedPref.getBoolean("first_time", false);

            // Check for camera update settings
            mMoveCamera = mSharedPref.getBoolean("camera_update", false);
            mMoveOverlay = mSharedPref.getBoolean("overlay_update", true);

            // Check for circle snap settings
            mCircleSnap = mSharedPref.getBoolean("circle_snap", true);

            // Check for marker transparency settings
            mMarkerTransparency = mSharedPref.getBoolean("marker_in_circle", true);


            mLoadDistance = mSharedPref.getBoolean("load_distance", true);
            mLoadDistanceInt = mSharedPref.getInt("load_distance_int", 1000);

            mCircleResolution = getCircleResolution(mSharedPref.getInt("circle_resolution", 1));


            // Set first time to false.
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("first_time",false);
            editor.apply();




            // Create a listener that detects when the user long presses the screen
            // Allows the user to add a spawn point or add/subtract circle from location
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(final LatLng latLng) {
                    SpawnLocation.hideAllMarkerWindows();
                    final Location location = new Location("");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);

                    mMapClickMenu.setVisibility(View.VISIBLE);

                    // Set click listeners for items in map click menu
                    mMapClickMarker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SpawnLocation.setSpawnPoint(MapsActivity.this, mMap, latLng);
                            mMapClickMenu.setVisibility(View.GONE);
                            if (mMapImageView.getParent() != null) {
                                updateOverlayMap();
                            }
                        }
                    });

                    mMapClickAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addCircle(location);
                            mMapClickMenu.setVisibility(View.GONE);
                            if (mMapImageView.getParent() != null) {
                                updateOverlayMap();
                            }
                        }
                    });

                    mMapClickSubtract.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            subtractCircle(location);
                            mMapClickMenu.setVisibility(View.GONE);
                            if (mMapImageView.getParent() != null) {
                                updateOverlayMap();
                            }
                        }
                    });

                    mMapClickCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mMapClickMenu.setVisibility(View.GONE);
                        }
                    });
                }
            });



            // Show marker options when a user clicks on it
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    return true;
                }
            });


            //Add listeners to buttons
            mButtonCircleGreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Update location prior to placing circle
                    getLocation();
                    addCircle(mLastLocation);
                    SpawnLocation.checkSpawnTimes();
                }
            });
            // Scale button when pressed
            mButtonCircleGreen.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mButtonCircleGreen.setScaleX(1.25f);
                        mButtonCircleGreen.setScaleY(1.25f);
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        mButtonCircleGreen.setScaleX(1.0f);
                        mButtonCircleGreen.setScaleY(1.0f);
                    }
                    return false;
                }
            });

            mButtonCircleRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Update location prior to placing circle
                    getLocation();
                    subtractCircle(mLastLocation);
                    SpawnLocation.checkSpawnTimes();
                }
            });
            // Scale button when pressed
            mButtonCircleRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mButtonCircleRed.setScaleX(1.25f);
                        mButtonCircleRed.setScaleY(1.25f);
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        mButtonCircleRed.setScaleX(1.0f);
                        mButtonCircleRed.setScaleY(1.0f);
                    }
                    return false;
                }
            });

            mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMenu(v);
                }
            });



            // Listeners for marker menu
            mInfoButtonDeleteListener = new OnInfoWindowElemTouchListener(mButtonMarkerDelete,
                    getResources().getDrawable(R.drawable.btn_default_normal_holo_light),
                    getResources().getDrawable(R.drawable.btn_default_pressed_holo_light)) {

                @Override
                protected void onClickConfirmed(View v, Marker marker) {
                    // Remove the spawn point that was clicked
                    SpawnLocation.removeSpawnPoint(MapsActivity.this, marker);

                }
            };
            mButtonMarkerDelete.setOnTouchListener(mInfoButtonDeleteListener);

            mInfoDescriptionListener = new OnInfoWindowElemTouchListener(mButtonMarkerDescription,
                    getResources().getDrawable(R.drawable.btn_default_normal_holo_light),
                    getResources().getDrawable(R.drawable.btn_default_pressed_holo_light)) {
                @Override
                protected void onClickConfirmed(View v, final Marker marker) {
                    marker.hideInfoWindow();
                    final String markerDescription = SpawnLocation.getMarkerDescription(MapsActivity.this, marker);
                    mMarkerDescriptionText.setText(markerDescription);
                    mMarkerDescriptionLayout.setVisibility(View.VISIBLE);

                    //Listeners for marker description window
                    mButtonMarkerDescriptionOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String markerDescriptionNew = mMarkerDescriptionText.getText().toString();
                            SpawnLocation.addDescriptionToDb(MapsActivity.this, marker, markerDescriptionNew);
                            mMarkerDescriptionLayout.setVisibility(View.GONE);
                        }
                    });
                }
            };
            mButtonMarkerDescription.setOnTouchListener(mInfoDescriptionListener);


            mInfoButtonTimeListener = new OnInfoWindowElemTouchListener(mButtonMarkerAddTime,
                    getResources().getDrawable(R.drawable.btn_default_normal_holo_light),
                    getResources().getDrawable(R.drawable.btn_default_pressed_holo_light))
            {
                @Override
                protected void onClickConfirmed(View v, final Marker marker) {
                    // Here we can perform some action triggered after clicking the button
                    marker.hideInfoWindow();
                    final NumberPicker np = new NumberPicker(MapsActivity.this);
                    np.setMinValue(0);
                    np.setMaxValue(59);
                    if (marker.getTag() != null && marker.getTag() != "") {
                        np.setValue(Integer.parseInt(marker.getTag().toString()));
                    }
                    final double latitude = marker.getPosition().latitude;
                    final double longitude = marker.getPosition().longitude;
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MapsActivity.this);
                    builder
                            .setTitle("Set time for Spawn Point")
                            .setView(np)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHelper myDb = DatabaseHelper.getInstance(MapsActivity.this);
                                    myDb.addTime(latitude, longitude, np.getValue() + "");
                                    marker.setTag(np.getValue() + "");
                                    SpawnLocation.addMarkerToSpawnTimes(marker);
                                    SpawnLocation.checkSpawnTimes();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            };
            mButtonMarkerAddTime.setOnTouchListener(mInfoButtonTimeListener);




            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    // Setting up the mInfoWindow with current's marker info
                    mInfoButtonDeleteListener.setMarker(marker);
                    mInfoDescriptionListener.setMarker(marker);
                    mInfoButtonTimeListener.setMarker(marker);

                    // We must call this to set the current marker and mInfoWindow references
                    // to the MapWrapperLayout
                    mapWrapperLayout.setMarkerWithInfoWindow(marker, mInfoWindow);
                    return mInfoWindow;
                }
            });

    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1500);
        locationRequest.setFastestInterval(1500);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
        getLocation();

        // Display # of spawn points loaded.
        // Make sure we have a location first.
        // If we run loadSpawnPoints before Google Play Services finds the location, we get a crash.
        if (mLastLocation != null) {
            loadSpawnPoints();
        }
        else {
            Toast.makeText(MapsActivity.this,
                    "Location could not be detected, no spawn points loaded (try reloading from menu)",
                    Toast.LENGTH_LONG).show();
        }
        mPolygonPointsGreen = Circles.loadAllPolygons(MapsActivity.this, mMap, mMarkerTransparency);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16)
                .build();

        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        if (!mMoveCamera) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public  boolean isStorageWritePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    private void getLocation() {
        /*if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        checkLocationPermission();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCircleSnap && mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(MapsActivity.this, v);

        popup.setOnMenuItemClickListener(MapsActivity.this);
        popup.inflate(R.menu.activity_main);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        boolean canWrite;
        DatabaseHelper myDb = DatabaseHelper.getInstance(MapsActivity.this.getApplicationContext());

        switch(item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
                if (mOverlayedButtonGreen.getParent() != null) {
                    removeOverlay();
                }
                break;
            case R.id.clear_shapes:
                Circles.clearPolygons(MapsActivity.this);
                break;
            case R.id.toggle_markers:
                SpawnLocation.toggleMarkerVisibility();
                break;
            case R.id.load_spawn_points:
                mMap.clear();
                Circles.clearPolygons(MapsActivity.this);
                getLocation();
                loadSpawnPoints();
                break;
            case R.id.tutorials:
                startActivity(new Intent(MapsActivity.this, TutorialsActivity.class));
                break;
            case R.id.overlay:
                checkDrawOverlayPermission();
                break;
            case R.id.edit_ui:
                break;
            case R.id.contact:
                sendEmail();
                break;
            case R.id.load_database:
                canWrite = isStorageWritePermissionGranted();
                if (canWrite) {
                    File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    myDb.loadCsv(downloadDir, MapsActivity.this);
                    mMap.clear();
                    Circles.clearPolygons(MapsActivity.this);
                    getLocation();
                    loadSpawnPoints();
                }
                break;
            case R.id.export_database:
                canWrite = isStorageWritePermissionGranted();
                if (canWrite) {
                    File file = myDb.exportCsv(MapsActivity.this.getApplicationContext());
                    Intent intent =
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file));
                    sendBroadcast(intent);
                }
                break;
            case R.id.delete_spawn_locations:
                SpawnLocation.removeAllSpawnPointFromDb(MapsActivity.this);
                mMap.clear();
                Circles.loadAllPolygons(MapsActivity.this, mMap, mMarkerTransparency);
                break;
        }
        myDb.close();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void loadSpawnPoints() {
        int spawnCount = 0;
        // Loop through database and load all spawn locations
        spawnCount = SpawnLocation.loadAllSpawnPoints(MapsActivity.this, mMap, mLastLocation, mLoadDistance, mLoadDistanceInt, mMarkerTransparency);
        // Once loaded, check if any spawn points are active
        SpawnLocation.checkSpawnTimes();
        // Show the user how many spawn points were loaded
        Toast.makeText(this, "loaded " + spawnCount + " Spawn Points", Toast.LENGTH_LONG).show();
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void showNewFeatures() {
        if (mFeatureCode < BuildConfig.VERSION_CODE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage(R.string.New_Features);
            builder.setTitle("Update");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt("feature_code", BuildConfig.VERSION_CODE);
            editor.apply();
        }
    }

    public void saveOverlayPosition() {
        int[] greenLocation = new int[2];
        int[] redLocation = new int[2];
        mOverlayedButtonGreen.getLocationOnScreen(greenLocation);
        mOverlayedButtonRed.getLocationOnScreen(redLocation);

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt("overlay_green_x", greenLocation[0]);
        editor.putInt("overlay_green_y", greenLocation[1]);
        editor.putInt("overlay_red_x", redLocation[0]);
        editor.putInt("overlay_red_y", redLocation[1]);
        editor.commit();
    }

    public void sendEmail() {
        /* Create the Intent */
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        /* Fill it with Data */
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"venntracker@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Venn Tracker");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

        /* Send it off to the Activity-Chooser */
        MapsActivity.this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE_OVERLAY) {
            // ** if so check once again if we have permission */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // continue here - permission was granted
                    startOverlay();
                }
            }
        }
    }

    public int getCircleResolution(int circleResolution) {
        int resolution = 90;
        switch (circleResolution) {
            case 0:
                resolution = 90;
                break;
            case 1:
                resolution = 120;
                break;
            case 2:
                resolution = 180;
                break;
        }
        return resolution;
    }

    private void subtractCircle(Location location) {
        mPolygonPointsGreen = Circles.subtractPolygonRed(mMap, location, MapsActivity.this, mCircleResolution);

        if (mPolygonPointsGreen.size() >0) {
            Circles.savePolygonToDB("Temp Polygon1", mPolygonPointsGreen, MapsActivity.this);
            Circles.saveHolesToDB(MapsActivity.this);
            // Create an store latLng so we can check if any red circles intersect
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Circles.drawCircleRed(latLng);
            Circles.saveRedCirclesToDb(MapsActivity.this);
            Circles.checkIntersecting(MapsActivity.this, mMap);
            if (mMarkerTransparency) {
                SpawnLocation.markerInCircle(mPolygonPointsGreen);
            }
        } else {
            // Our polygon is gone, reset all marker's transparency
            SpawnLocation.markerResetTransparency();
        }
    }

    private void addCircle(Location location) {
        mPolygonPointsGreen = Circles.drawPolygonGreen(mMap, location, MapsActivity.this, mCircleResolution);

        Circles.savePolygonToDB("Temp Polygon1", mPolygonPointsGreen, MapsActivity.this);
        Circles.saveHolesToDB(MapsActivity.this);

        if (mMarkerTransparency) {
            SpawnLocation.markerInCircle(mPolygonPointsGreen);
        }
    }

    private void startOverlay() {

        updateOverlayMap();

        if (mMoveOverlay) {
            final Handler updateHandler = new Handler();
            final int delay = 4000; //milliseconds

            updateHandler.postDelayed(new Runnable() {
                public void run() {
                    /*//do something
                    overlayMarkers.clear();
                    double latitude;
                    double longitude;
                    float[] distance = new float[2];*/

                    updateOverlayMap();
                    /*double myLatitude = mLastLocation.getLatitude();
                    double myLongitude = mLastLocation.getLongitude()
                            ;
                    for (Marker marker : spawnPoints) {
                        latitude = marker.getPosition().latitude;
                        longitude = marker.getPosition().longitude;

                        Location.distanceBetween(latitude, longitude, myLatitude,
                                myLongitude, distance);
                        if (distance[0] < 400) {
                            overlayMarkers.add(marker);
                        }
                    }*/
                    updateHandler.postDelayed(this, delay);
                }
            }, delay);
        }




        // Hide main buttons so they do not interfere with overlay
        mButtonCircleRed.setVisibility(View.GONE);
        mButtonCircleGreen.setVisibility(View.GONE);
        // Set Listeners for overlay buttons
        mOverlayedButtonGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
                addCircle(mLastLocation);
                updateOverlayMap();
                mOverlayedButtonRed.setOnTouchListener(null);
            }
        });

        mOverlayedButtonGreen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mOverlayedButtonRed.setOnTouchListener(null);
                mOverlayedButtonGreen.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        // Disable onTouch after 5 seconds of not touching it
                        Runnable runnable;
                        Handler handler = new Handler();
                        runnable = new Runnable() {

                            @Override
                            public void run() {
                                mOverlayedButtonRed.setOnTouchListener(null);
                                saveOverlayPosition();
                            }
                        };

                        handler.removeCallbacks(runnable);
                        // After 5 seconds, Turn off onTouchListener
                        // This is reset on every move event
                        handler.postDelayed(runnable, 5000);



                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            handler.removeCallbacks(runnable);
                            float x = event.getRawX();
                            float y = event.getRawY();

                            moving = false;

                            int[] location = new int[2];
                            mOverlayedButtonGreen.getLocationOnScreen(location);

                            originalXPos = location[0];
                            originalYPos = location[1];

                            offsetX = originalXPos - x;
                            offsetY = originalYPos - y;

                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            handler.removeCallbacks(runnable);
                            // Reset timer every time we move
                            handler.removeCallbacks(runnable);
                            int[] topLeftLocationOnScreen = new int[2];
                            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

                            System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
                            System.out.println("originalY="+originalYPos);

                            float x = event.getRawX();
                            float y = event.getRawY();

                            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mOverlayedButtonGreen.getLayoutParams();

                            int newX = (int) (offsetX + x);
                            int newY = (int) (offsetY + y);

                            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                                return false;
                            }

                            params.x = newX - (topLeftLocationOnScreen[0]);
                            params.y = newY - (topLeftLocationOnScreen[1]);

                            wm.updateViewLayout(mOverlayedButtonGreen, params);
                            moving = true;
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            handler.postDelayed(runnable, 5000);
                            if (moving) {
                                return true;
                            }
                        }

                        return false;
                    }
                });
                return false;
            }
        });
        mOverlayedButtonRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
                subtractCircle(mLastLocation);
                mOverlayedButtonGreen.setOnTouchListener(null);
                updateOverlayMap();
            }
        });



        mOverlayedButtonRed.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mOverlayedButtonGreen.setOnTouchListener(null);
                mOverlayedButtonRed.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        // Disable onTouch after 5 seconds of not touching it
                        Runnable runnable;
                        Handler handler = new Handler();
                        runnable = new Runnable() {

                            @Override
                            public void run() {
                                mOverlayedButtonRed.setOnTouchListener(null);
                                saveOverlayPosition();
                            }
                        };

                        handler.removeCallbacks(runnable);
                        // After 5 seconds, Turn off onTouchListener
                        // This is reset on every move event
                        handler.postDelayed(runnable, 5000);

                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            handler.removeCallbacks(runnable);
                            float x = event.getRawX();
                            float y = event.getRawY();

                            moving = false;

                            int[] location = new int[2];
                            mOverlayedButtonRed.getLocationOnScreen(location);

                            originalXPos = location[0];
                            originalYPos = location[1];

                            offsetX = originalXPos - x;
                            offsetY = originalYPos - y;

                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            // Reset timer every time we move
                            handler.removeCallbacks(runnable);
                            int[] topLeftLocationOnScreen = new int[2];
                            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

                            float x = event.getRawX();
                            float y = event.getRawY();

                            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mOverlayedButtonRed.getLayoutParams();

                            int newX = (int) (offsetX + x);
                            int newY = (int) (offsetY + y);

                            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                                return false;
                            }

                            params.x = newX - (topLeftLocationOnScreen[0]);
                            params.y = newY - (topLeftLocationOnScreen[1]);

                            wm.updateViewLayout(mOverlayedButtonRed, params);
                            moving = true;
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            handler.postDelayed(runnable, 5000);
                            if (moving) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                return false;
            }
        });

        // Binder ensures our view doesnt disapear on lifecycle events
        Binder binder = new Binder();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.width = 150;
        params.height = 150;
        params.token =  binder;

        WindowManager.LayoutParams greenParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams redParams = new WindowManager.LayoutParams();
        greenParams.copyFrom(params);
        redParams.copyFrom(params);

        greenParams.x = mSharedPref.getInt("overlay_green_x", (int) mButtonCircleGreen.getX());
        greenParams.y = mSharedPref.getInt("overlay_green_y", (int) mButtonCircleGreen.getY());
        redParams.x = mSharedPref.getInt("overlay_red_x", (int) mButtonCircleRed.getX());
        redParams.y = mSharedPref.getInt("overlay_red_y", (int) mButtonCircleRed.getY());

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        topLeftParams.token = binder;

        WindowManager.LayoutParams mapParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mapParams.gravity = Gravity.LEFT | Gravity.TOP;
        mapParams.x = 10;
        mapParams.y = 10;
        mapParams.width = getPixelsFromDp(MapsActivity.this, 100);
        mapParams.height = getPixelsFromDp(MapsActivity.this, 100);
        mapParams.alpha = 30;
        mapParams.token = binder;


        wm.addView(topLeftView, topLeftParams);
        wm.addView(mOverlayedButtonGreen, greenParams);
        wm.addView(mOverlayedButtonRed, redParams);
        wm.addView(mMapImageView, mapParams);

    }

    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(MapsActivity.this)) {
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE_OVERLAY);
            }
            else {
                if (mOverlayedButtonGreen.getParent() != null) {
                    removeOverlay();
                }
                else {
                    overlayDialog();
                }
            }
        }
        else {
            if (mOverlayedButtonGreen.getParent() != null) {
                removeOverlay();
            }
            else {
                overlayDialog();
            }
        }

    }

    public void overlayDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MapsActivity.this);
        builder
                .setTitle("Start Overlay")
                .setMessage("This overlay is currently in BETA." +
                        " \n\nIf you press and hold the circle buttons, you can move them around." +
                        " After releasing them for about 5 seconds, they will stay where thay are." +
                        " \n\nThe buttons will interact with your map just like the buttons on the main screen.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Yes button clicked, do something
                        startOverlay();
                    }
                })
                .show();
    }

    private void removeOverlay() {
        mButtonCircleGreen.setVisibility(View.VISIBLE);
        mButtonCircleRed.setVisibility(View.VISIBLE);

        wm.removeView(mOverlayedButtonGreen);
        wm.removeView(mOverlayedButtonRed);
        wm.removeView(topLeftView);
        wm.removeView(mMapImageView);
    }

    private void updateOverlayMap() {
        try {
            checkLocationPermission();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size=170x200&path=";

            mOverlayMapCenter = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
            mOverlayMapPolygon = "";
            mOverlayMapMarkers = "";
            mOverlayMapZoom = "&zoom=15";
            mOverlayMapLocation = "&markers=";
            mOverlayMapFormat = "&format=jpg";




            if (mPolygonPointsGreen.size() > 0) {
                ArrayList<Marker> spawnPointsInCircle = SpawnLocation.getSpawnPointsInCircle();
                mOverlayMapMarkers = "color:red|size:small";
                for (Marker marker : spawnPointsInCircle) {
                    mOverlayMapMarkers = mOverlayMapMarkers + "|" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                }
            } else {
                mOverlayMapMarkers = "color:red|size:small";
                for (Marker marker : spawnPoints) {
                    mOverlayMapMarkers = mOverlayMapMarkers + "|" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                }
            }

            for (LatLng latLng : mPolygonPointsGreen) {
                mOverlayMapPolygon = mOverlayMapPolygon + "|" + latLng.latitude + "," + latLng.longitude;
            }

            path = "weight:2|fillcolor:0x3EBD1B50|color:0x3EBD1B50|geodesic:true";

            path = path + mOverlayMapPolygon;
            path = URLEncoder.encode(path, "UTF-8");
            if (!mOverlayMapMarkers.equals("")) {
                mOverlayMapMarkers = URLEncoder.encode(mOverlayMapMarkers, "UTF-8");
                mOverlayMapMarkers = "&markers=" + mOverlayMapMarkers;
            }
            mOverlayMapLocation = mOverlayMapLocation + URLEncoder.encode("color:blue|size:tiny", "UTF-8")+
                    URLEncoder.encode("|" + mOverlayMapCenter, "UTF-8");
            mOverlayMapCenter = "&center=" + URLEncoder.encode(mOverlayMapCenter, "UTF-8");

            mOverlayMapStyle = "&style=" +
                    URLEncoder.encode("element:labels|visibility:off", "UTF-8") + "&mOverlayMapStyle=" +
                    URLEncoder.encode("feature:all|saturation:-50", "UTF-8") + "&mOverlayMapStyle=" +
                    URLEncoder.encode("element:all.stroke|visibility:off", "UTF-8");
            //  + "&markers=" + marker_me + "&markers=" + marker_dest

            STATIC_MAP_API_ENDPOINT = STATIC_MAP_API_ENDPOINT
                    + path
                    + mOverlayMapCenter
                    + mOverlayMapZoom
                    + mOverlayMapFormat
                    + mOverlayMapStyle
                    + mOverlayMapMarkers
                    + mOverlayMapLocation
                    + mOverlayMapKey;


            AsyncTask<Void, Void, Bitmap> setImageFromUrl = new AsyncTask<Void, Void, Bitmap>(){
                @Override
                protected Bitmap doInBackground(Void... params) {
                    //Bitmap bmp = null;
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet request = new HttpGet(STATIC_MAP_API_ENDPOINT);

                    InputStream in;
                    try {
                        HttpResponse response = httpclient.execute(request);
                        in = response.getEntity().getContent();
                        bmp = BitmapFactory.decodeStream(in);
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return bmp;
                }
                protected void onPostExecute(Bitmap bmp) {
                    if (bmp!=null) {
                        mMapImageView.setImageBitmap(bmp);
                        mMapImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                }
            };

            setImageFromUrl.execute();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


}