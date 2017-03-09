package kavorka.venn_tracker;

// Add user defined spawn locations

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
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
                        double latitude;
                        double longitude;
                        latitude = marker.getPosition().latitude;
                        longitude = marker.getPosition().longitude;
                        removeSpawnPointFromDb(context, marker);

                    }
                })
                .setNegativeButton("No", null)						//Do nothing on no
                .show();

    }

    public static void showSpawnLocations(GoogleMap googleMap, LatLng latLng, String description) {
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
        resetMarkers(context);
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        double latitude;
        double longitude;
        double myLatitude;
        double myLongitude;
        String description;
        int count = 0;
        String type;
        LatLng latLng;
        Cursor res = myDb.getAllLocations();

        myLatitude = myLocation.getLatitude();
        myLongitude = myLocation.getLongitude();


        // For each saved marker
        // Get the latitude and longitude and put them into a LatLng object
        // Use the LatLng object to place the marker.
        while (res.moveToNext()) {
            type = res.getString(1);
            latitude = res.getDouble(2);
            longitude = res.getDouble(3);


            latLng = new LatLng(latitude, longitude);

            // Check if spawn marker is within a certain distance from location
            float[] distance = new float[2];
            Location.distanceBetween(latitude, longitude, myLatitude,
                    myLongitude, distance);

            if (type.equals("Spawn Location")) {
                description = res.getString(5);
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
        }
        res.close();
        myDb.close();
        return count;
    }

    public static void removeAllSpawnPointFromDb(Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        Cursor res = myDb.getAllLocations();

        // Loop through db and store vales of row
        while (res.moveToNext()) {
            String dbId = res.getString(0);
            String dbType = res.getString(1);

            // Only remove spawn locations
            if (dbType.equals("Spawn Location")) {
                myDb.deleteLocation(dbId);
            }
        }
    }

    // When marker is removed from map, also remove from db
    public static void removeSpawnPointFromDb(Context context, Marker marker) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        Cursor res = myDb.getAllLocations();
        double latitude;
        double longitude;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        // Loop through db and store vales of row
        while (res.moveToNext()) {
            String dbId = res.getString(0);
            String dbType = res.getString(1);
            double dbLat = res.getDouble(2);
            double dbLon = res.getDouble(3);

            // Only remove spawn locations
            if (dbType.equals("Spawn Location")) {
                // Find marker in db with same position and remove
                if (dbLat == latitude && dbLon == longitude) {
                    myDb.deleteLocation(dbId);
                }
            }
        }
    }

    // Add marker description to db
    public static void addDescriptionToDb(Context context, Marker marker, String description) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        Cursor res = myDb.getAllLocations();
        double latitude;
        double longitude;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        // Loop through db and store vales of row
        while (res.moveToNext()) {
            String dbId = res.getString(0);
            String dbType = res.getString(1);
            double dbLat = res.getDouble(2);
            double dbLon = res.getDouble(3);

            if (dbType.equals("Spawn Location")) {
                if (dbLat == latitude && dbLon == longitude) {
                    myDb.addDescription(dbId, dbType, dbLat, dbLon, description);
                    break;
                }
            }

        }
        myDb.close();
    }

    public static String getMarkerDescription(Context context, Marker marker) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        Cursor res = myDb.getAllLocations();
        double latitude;
        double longitude;
        String description;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        // Loop through db and store vales of row
        while (res.moveToNext()) {
            String dbId = res.getString(0);
            String dbType = res.getString(1);
            double dbLat = res.getDouble(2);
            double dbLon = res.getDouble(3);
            if (res.getString(5) == null) {
                description = "No Description";
            } else {
                description = res.getString(5);
            }

            if (dbType.equals("Spawn Location")) {
                if (dbLat == latitude && dbLon == longitude) {
                    return description;
                }
            }
        }
        myDb.close();
        return "No Description";
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
            ArrayList<LatLng> hole;
            for (int i = 1 ; i <=5 ; i++) {
                hole = Circles.getPolygonPointsHole(i);
                if (PolyUtil.containsLocation(marker.getPosition(), hole, Circles.getGeoDisc())) {
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

    public static void markerTransparency() {
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

    public static void resetMarkers(Context context) {
        mSpawnPoints.clear();
        mSpawnPointsNotInCircle.clear();
        mSpawnPointsInCircle.clear();
        mMarkerVisibility = true;
        Circles.clearCircles(context);
    }

    public static void hideAllMarkerWindows() {
        for (Marker marker : mSpawnPoints) {
            marker.hideInfoWindow();
        }
    }
}
