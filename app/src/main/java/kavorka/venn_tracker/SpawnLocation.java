package kavorka.venn_tracker;

// Add user defined spawn locations

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;

public class SpawnLocation {
    private static ArrayList<Marker> mSpawnPoints = new ArrayList<>();
    private static Boolean mMarkerVisibility = true;
    private static ArrayList<Marker> mSpawnPointsInCircle = new ArrayList<>();
    private static ArrayList<Marker> mSpawnPointsNotInCircle = new ArrayList<>();

    // Margin of error for distance +- from the circle boundary when making markers transparent
    private static int mDistanceErrorMargin = 15;



    public static void setSpawnPoint(final Context context, final GoogleMap googleMap, final LatLng latLng){
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng));
        mSpawnPoints.add(marker);
        // Put location into the database as 2 doubles with type Spawn Location.
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.addLocation("Spawn Location", latLng.latitude, latLng.longitude, "No Description");
        myDb.close();
    }


    public static ArrayList getSpawnPoints() {
        return mSpawnPoints;
    }

    public static ArrayList getSpawnPointsInCircle() {
        return mSpawnPointsInCircle;
    }

    public static void removeSpawnPoint(final Context context, final Marker marker) {
        //Put up the Yes/No message box
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle("Remove Pokemon spawn marker?")
                .setMessage("Are you sure?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Yes button clicked, do something
                        marker.remove();
                        mSpawnPoints.remove(marker);
                        removeSpawnPointFromDb(context, marker);

                    }
                })
                .setNegativeButton("No", null)						//Do nothing on no
                .show();

    }

    private static void showSpawnLocations(GoogleMap googleMap, LatLng latLng, String description) {
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));
            marker.setSnippet(description);
            mSpawnPoints.add(marker);
    }

    public static void toggleMarkerVisibility() {
        if (mMarkerVisibility) {
            for (Marker marker : mSpawnPoints) {
                marker.setVisible(false);
            }
            mMarkerVisibility = false;
        } else {
            for (Marker marker : mSpawnPoints) {
                marker.setVisible(true);
            }
            mMarkerVisibility = true;
        }
    }

    // Loops through database, loads and shows all markers that have been saved.
    public static int loadAllSpawnPoints(Context context, GoogleMap googleMap, Location myLocation,
                                         Boolean loadDistance, int spawnDistance, Boolean transparency) {
        resetMarkers();
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        double latitude;
        double longitude;
        double myLatitude;
        double myLongitude;
        String description;
        int count = 0;
        LatLng latLng;
        Cursor res = myDb.getAllLocations("Spawn Location");

        myLatitude = myLocation.getLatitude();
        myLongitude = myLocation.getLongitude();


        // For each saved marker
        // Get the latitude and longitude and put them into a LatLng object
        // Use the LatLng object to place the marker.
        while (res.moveToNext()) {
            latitude = res.getDouble(2);
            longitude = res.getDouble(3);


            latLng = new LatLng(latitude, longitude);

            // Check if spawn marker is within a certain distance from location
            float[] distance = new float[2];
            Location.distanceBetween(latitude, longitude, myLatitude,
                    myLongitude, distance);

            if (res.getString(5) == null) {
                description = "No Description";
            } else {
                description = res.getString(5);
            }
            if (loadDistance) {
                if (distance[0] < spawnDistance) {
                    count++;
                    showSpawnLocations(googleMap, latLng, description);
                }
            }
            else {
                    count++;
                    showSpawnLocations(googleMap, latLng, description);
                }
        }
        myDb.close();
        res.close();
        return count;
    }

    public static void removeAllSpawnPointFromDb(Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.removeAllSpawnLocations();
        myDb.close();
    }

    // When marker is removed from map, also remove from db
    private static void removeSpawnPointFromDb(Context context, Marker marker) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        double latitude = marker.getPosition().latitude;;
        double longitude = marker.getPosition().longitude;;
        myDb.removeSpawnLocation(latitude, longitude);
        myDb.close();
    }

    // Add spawn location description to db
    public static void addDescriptionToDb(Context context, Marker marker, String description) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        myDb.addDescription("'Spawn Location'", latitude, longitude, description);
    }

    // Get description of spawn location
    public static String getMarkerDescription(Context context, Marker marker) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        return myDb.getDescription("'Spawn Location'", latitude, longitude);
    }

    public static void markerInCircle(ArrayList<LatLng> polygonGreen) {
        if (mSpawnPointsInCircle.size() == 0){
        for (Marker marker : mSpawnPoints) {
                if (PolyUtil.containsLocation(marker.getPosition(), polygonGreen, Circles.getGeoDisc())) {
                    mSpawnPointsInCircle.add(marker);
                    }
                else {
                    mSpawnPointsNotInCircle.add(marker);
                }
            }
        } else {
            for (Marker marker : mSpawnPointsInCircle) {
                if (!PolyUtil.containsLocation(marker.getPosition(), polygonGreen, Circles.getGeoDisc())) {
                    mSpawnPointsNotInCircle.add(marker);
                }
            }
        }
        for (Marker marker : mSpawnPointsInCircle) {
            ArrayList<ArrayList<LatLng>> holes = Circles.getHoles();
            for (int i = 1 ; i < holes.size() ; i++) {
                if (PolyUtil.containsLocation(marker.getPosition(), holes.get(i), Circles.getGeoDisc())) {
                    mSpawnPointsNotInCircle.add(marker);
                }
            }
        }
        for (Marker marker : mSpawnPointsNotInCircle) {
            mSpawnPointsInCircle.remove(marker);
        }
        markerTransparency();
    }

    public static void clearInCircle() {
        mSpawnPointsInCircle.clear();
        mSpawnPointsNotInCircle.clear();
    }

    private static void markerTransparency() {
        for( Marker marker : mSpawnPointsNotInCircle) {
            marker.setAlpha(0.2f);
        }
        for (Marker marker : mSpawnPointsInCircle) {
            marker.setAlpha(1.0f);
        }
    }
    public static void markerResetTransparency() {
        for (Marker marker : mSpawnPoints) {
            marker.setAlpha(1.0f);
        }
        mSpawnPointsInCircle.clear();
        mSpawnPointsNotInCircle.clear();
    }

    private static void resetMarkers() {
        mSpawnPoints.clear();
        mSpawnPointsNotInCircle.clear();
        mSpawnPointsInCircle.clear();
        mMarkerVisibility = true;
    }

    public static void hideAllMarkerWindows() {
        for (Marker marker : mSpawnPoints) {
            marker.hideInfoWindow();
        }
    }
}
