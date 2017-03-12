package kavorka.venn_tracker;


import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;

public class Circles {
    private static ArrayList<Circle> circlesToClear = new ArrayList<>();
    private static ArrayList<Polygon> mPolygonsToClear = new ArrayList<>();
    private static ArrayList<LatLng> mPolygonPointsGreen = new ArrayList<>();
    private static ArrayList<LatLng> mCirclePointsHole1 = new ArrayList<>();
    private static ArrayList<LatLng> mCirclePointsHole2 = new ArrayList<>();
    private static ArrayList<LatLng> mCirclePointsHole3 = new ArrayList<>();
    private static ArrayList<LatLng> mCirclePointsHole4 = new ArrayList<>();
    private static ArrayList<LatLng> mCirclePointsHole5 = new ArrayList<>();
    private static ArrayList<ArrayList<LatLng>> mHoles = new ArrayList<>();


    private static int mGreenRadius = 200;
    private static int mRedRadius = 200;
    private static int mHoleRadius = 40;
    private static boolean isGeoDisc = true;



    // Clear all green and red circles
    public static void clearCircles(Context context) {
        for (Circle circle : circlesToClear) {
            circle.remove();
        }
        SpawnLocation.markerResetTransparency();
    }


    public static ArrayList<LatLng> loadAllPolygons(Context context, GoogleMap gmap, boolean transparency) {
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        mHoles.clear();
        mPolygonPointsGreen.clear();

        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        double latitude;
        double longitude;
        String type;
        Cursor res = myDb.getAllLocations();

        // For each saved polygon
        // Get the latitude and longitude and put them into a location
        while (res.moveToNext()) {
            type = res.getString(1);
            latitude = res.getDouble(2);
            longitude = res.getDouble(3);



            if (type.equals("Temp Polygon1")) {
                LatLng latLng = new LatLng(latitude, longitude);
                mPolygonPointsGreen.add(latLng);
            }
        }

        PolygonOptions polygonOptionsGreen = new PolygonOptions()
                .fillColor(0x503EBD1B)
                .strokeColor(0x503EBD1B)
                .geodesic(isGeoDisc);

        if (mPolygonPointsGreen.size() > 0) {
            polygonOptionsGreen.addAll(mPolygonPointsGreen);


            Polygon polygon = gmap.addPolygon(polygonOptionsGreen);
            mPolygonsToClear.add(polygon);
            if (transparency) {
                SpawnLocation.markerInCircle(mPolygonPointsGreen);
            }
        }
        return mPolygonPointsGreen;
    }

    public static ArrayList<LatLng> drawPolygonGreen(GoogleMap gmap, Location center, Context context, int resolution) {
        PolygonOptions polygonOptionsNew = new PolygonOptions()
                .fillColor(0x503EBD1B)
                .strokeColor(0x503EBD1B)
                .geodesic(isGeoDisc);

        if (mPolygonPointsGreen.size() == 0) {
            if (mHoles.size() == 0) {
                ArrayList<LatLng> hole = getCirclePoints(mHoleRadius, center, resolution);
                mHoles.add(hole);
            }
            return createNewGreenCircle(gmap, center, polygonOptionsNew, resolution);
        }

        // Check if our new green circle is outside of our original
        // If so, do nothing
        ArrayList<LatLng> newCircle = getCirclePoints(mGreenRadius, center, resolution);
        ArrayList<LatLng> newPolygonPointsGreen = combinePolygonGreen(mPolygonPointsGreen, newCircle, false);


        boolean combineHoles = false;

        if (mHoles.size() == 0) {
            ArrayList<LatLng> hole = getCirclePoints(mHoleRadius, center, resolution);
            mHoles.add(hole);
        } else {
            ArrayList<LatLng> circlePointsHoleNew;
            circlePointsHoleNew = getCirclePoints(mHoleRadius, center, resolution);

            boolean holeOutside = false;
            for (LatLng latLng : circlePointsHoleNew) {
                if (!PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    holeOutside = true;
                }
                else {
                    holeOutside = false;
                    break;
                }
            }
            if (!holeOutside) {
                mHoles.add(circlePointsHoleNew);
            }
            if (mHoles.size() > 1) {
                mHoles = combineHoles(mHoles);
            }
        }

        // Subtract any holes that intersect with out new polygon
        // Also remove them from mHoles
        newPolygonPointsGreen = subtractHoles(newPolygonPointsGreen, context);



        // Only add a hole if it is completely inside of our polygon.
        for (int i = 0 ; i < mHoles.size() ; i++) {
            int count = 0;
            for (LatLng latLng : mHoles.get(i)) {
                if (PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    count++;
                }
            }
            if (count == mHoles.get(i).size()) {
                polygonOptionsNew.addHole(mHoles.get(i));
            }
        }



        // Remove any polygons we currently have before drawing our new polygon
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        mPolygonsToClear.clear();
        mPolygonPointsGreen.clear();

        // Prepare our new polygon
        for (LatLng latLng : newPolygonPointsGreen) {
            mPolygonPointsGreen.add(latLng);
            polygonOptionsNew.add(latLng);
        }

        Polygon polygon = gmap.addPolygon(polygonOptionsNew);
        mPolygonsToClear.add(polygon);

        return mPolygonPointsGreen;
    }

    private static ArrayList<ArrayList<LatLng>> combineHoles(ArrayList<ArrayList<LatLng>> holes) {
        // Loop from last element
        // Each loop checks it against all other elements
        for (int i = (holes.size() - 1); i > 0; i--) {
            // loop from first element and combine checked element from above loop
            for (int j = 0 ; j < i ; j ++) {
                // Check to see if any point in j falls within i
                for (LatLng latLng : holes.get(i)) {
                    // If it does, combine i to j and delete i
                    if (PolyUtil.containsLocation(latLng, holes.get(j), true)) {
                        holes.set(j, combinePolygonGreen(holes.get(j), holes.get(i), true));
                        holes.remove(i);
                        // Recursivly run algorithm until no holes intersect
                        return combineHoles(holes);
                    }
                }
            }
        }
        return holes;
    }


    private static ArrayList<LatLng> createNewGreenCircle(GoogleMap gmap, Location center, PolygonOptions polygonOptionsNew, int resolution) {
        int circleResolution = resolution;
        int circleHoleResolution = Math.round(resolution / 2);;

        mPolygonPointsGreen = getCirclePoints(mGreenRadius, center, circleResolution);
        if (mCirclePointsHole1.size() == 0) {
            mCirclePointsHole1 = getCirclePoints(mHoleRadius, center, circleHoleResolution);
        }

        // Remove any polygons we currently have before drawing our new polygon
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        mPolygonsToClear.clear();

        if (mPolygonPointsGreen.size() > 0) {
            polygonOptionsNew.addAll(mPolygonPointsGreen);
            polygonOptionsNew.addHole(mCirclePointsHole1);
            Polygon polygon = gmap.addPolygon(polygonOptionsNew);
            mPolygonsToClear.add(polygon);
        }
        return mPolygonPointsGreen;
    }

    private static ArrayList<LatLng> combinePolygonGreen(ArrayList<LatLng> polygon1, ArrayList<LatLng> polygon2, boolean hole) {

        ArrayList<LatLng> newCirclePointsGreen = new ArrayList<>();
        ArrayList<LatLng> greenIntersection1 = new ArrayList<>();
        ArrayList<LatLng> greenIntersection2 = new ArrayList<>();

        int endIndexCircle1 = getLastIndex(polygon1, polygon2);
        int endIndexCircle2 = getLastIndex(polygon2, polygon1);

        polygon1 = reorderPolygon(polygon1, endIndexCircle1);
        polygon2 = reorderPolygon(polygon2, endIndexCircle2);

        if (!hole) {
            for (LatLng latLng : polygon2) {
                if (PolyUtil.containsLocation(latLng, polygon1, isGeoDisc)) {
                    greenIntersection1.add(latLng);
                }
            }

            // Check if our new polygon contains all points from our current polygon
            // If so, stop and do nothing.
            if (greenIntersection1.size() == polygon1.size()) {
                return polygon1;
            }

            for (LatLng latLng : polygon1) {
                if (PolyUtil.containsLocation(latLng, polygon2, isGeoDisc)) {
                    greenIntersection2.add(latLng);
                }
            }

            // Check if our new circle is outside of our original green circle.
            // If so, stop and do nothing.
            if (greenIntersection1.size() == 0 || greenIntersection2.size() == 0) {
                return polygon1;
            }
        } else {
            for (LatLng latLng : polygon2) {
                if (!PolyUtil.containsLocation(latLng, polygon1, isGeoDisc)) {
                    greenIntersection1.add(latLng);
                }
            }

            // Our holes don't intersect.  Return new hole so we can add 2 distinct holes.
            if (greenIntersection1.size() == 0) {
                return polygon2;
            }

            for (LatLng latLng : polygon1) {
                if (!PolyUtil.containsLocation(latLng, polygon2, isGeoDisc)) {
                    greenIntersection2.add(latLng);
                }
            }
        }

        // Combine all points that don't intersect and return result
        for (LatLng latLng : greenIntersection1) {
            newCirclePointsGreen.add(latLng);
        }
        for (LatLng latLng : greenIntersection2) {
            newCirclePointsGreen.add(latLng);
        }
        return newCirclePointsGreen;
    }


    public static ArrayList<LatLng> subtractPolygonRed(GoogleMap gmap, Location center,Context context, int resolution) {
        int circleResolution = resolution;
        PolygonOptions polygonOptionsNew = new PolygonOptions()
                .fillColor(0x503EBD1B)
                .strokeColor(0x503EBD1B)
                .geodesic(isGeoDisc);

        // Get the points of our new circle
        ArrayList<LatLng> circleRed = getCirclePoints(mRedRadius, center, circleResolution);

        int countInRed = 0;
        for (LatLng latLng : mPolygonPointsGreen) {
            if (PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)) {
                countInRed++;
            }
        }
        if (countInRed == mPolygonPointsGreen.size()) {
            clearPolygons(context);
            return mPolygonPointsGreen;
        }

        if (countInRed == 0) {
            return mPolygonPointsGreen;
        }

        ArrayList<LatLng> polygonPointsGreen = subtractCircle(mPolygonPointsGreen, circleRed, context);


        if (polygonPointsGreen.size() == 0) {
            clearPolygons(context);
            return mPolygonPointsGreen;
        }

        // Subtract any holes that intersect with out new polygon
        // Also remove them from mHoles
        polygonPointsGreen = subtractHoles(polygonPointsGreen, context);

        // Remove any polygons we currently have before drawing our new polygon
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        // Make sure we clear our green points before adding the new points
        mPolygonPointsGreen.clear();

        // Add all remaning holes to our new polygon
        for (ArrayList<LatLng> hole : mHoles) {
            polygonOptionsNew.addHole(hole);
        }

        // Prepare our new polygon
        for (LatLng latLng : polygonPointsGreen) {
            mPolygonPointsGreen.add(latLng);
            polygonOptionsNew.add(latLng);
        }

        Polygon polygon = gmap.addPolygon(polygonOptionsNew);
        mPolygonsToClear.add(polygon);

        return mPolygonPointsGreen;
    }

    private static ArrayList<LatLng> subtractHoles(ArrayList<LatLng> polygonPointsGreen, Context context) {
        for (int i = mHoles.size() - 1 ; i >= 0 ; i--) {
            for (LatLng latLng : polygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mHoles.get(i), isGeoDisc)) {
                    polygonPointsGreen = subtractCircle(polygonPointsGreen, mHoles.get(i), context);
                    mHoles.remove(i);
                    break;
                }
            }

        }
        return polygonPointsGreen;
    }

    private static ArrayList<LatLng> subtractCircle(ArrayList<LatLng> polygonGreen, ArrayList<LatLng> polygonSubtract, Context context) {

        int endIndex1 = 0;
        int endIndex2 = 0;
        if (polygonGreen.size() == 0) {
            return polygonGreen;
        }

        if (polygonSubtract.size() == 0) {
            return polygonGreen;
        }
        ArrayList<LatLng> newPolygonPoints = new ArrayList<>();

        endIndex1 = getLastIndex(polygonGreen, polygonSubtract);

        // Reorder both polygons so they get added correctly
        polygonGreen = reorderPolygon(polygonGreen, endIndex1);

        // Find the new line where the points of our subtraction circle are within our green circle
        for (LatLng latLng : polygonSubtract) {
            if (PolyUtil.containsLocation(latLng, polygonGreen, isGeoDisc)) {
                newPolygonPoints.add(latLng);
            }
        }

        // Find point on new line that is closest to the first point on the remaining green circle
        // Re-order line so that point is the last point on the line
        if (newPolygonPoints.size() != 0){
            endIndex2 = getLastIndexRed(polygonGreen, newPolygonPoints, endIndex1);
            newPolygonPoints = reorderPolygon(newPolygonPoints, endIndex2);
        } else {
            return polygonGreen;
        }

        // Find the points of our green circle that are not in our red circle
        ArrayList<LatLng> newPolygonPointsGreen = new ArrayList<>();
        for (LatLng latLng : polygonGreen) {
            if (!PolyUtil.containsLocation(latLng, polygonSubtract, isGeoDisc)) {
                newPolygonPointsGreen.add(latLng);
            }
        }

        // After subtracting check if we removed all of our green circle
        // If so, no need to continue. Return empty list
        if (newPolygonPointsGreen.size() == 0) {
            clearPolygons(context);
            return polygonGreen;
        }

        newPolygonPoints = reverseIfNeeded(polygonGreen, newPolygonPoints);

        for (LatLng latLng : newPolygonPoints) {
            newPolygonPointsGreen.add(latLng);
        }

        if (newPolygonPointsGreen.size() != 0) {
            return newPolygonPointsGreen;
        }
        return polygonGreen;
    }



    private static ArrayList<LatLng> reverseIfNeeded(ArrayList<LatLng> checkAgainst, ArrayList<LatLng> listToReverse) {
        if (checkAgainst.size() < 2 || listToReverse.size() < 2) {
            return listToReverse;
        }
        float[] distance1 = new float[2];
        float[] distance2 = new float[2];
        LatLng toCheckLast = checkAgainst.get(checkAgainst.size() - 1);
        LatLng toReverseLast = listToReverse.get(listToReverse.size() - 1);
        LatLng toReverseFirst = listToReverse.get(0);
        Location.distanceBetween(toCheckLast.latitude, toCheckLast.longitude, toReverseLast.latitude,
                toReverseLast.longitude, distance1);
        Location.distanceBetween(toCheckLast.latitude, toCheckLast.longitude, toReverseFirst.latitude, toReverseFirst.longitude, distance2);
        if (distance1[0] < distance2[0]) {
            Collections.reverse(listToReverse);
        }
        return listToReverse;
    }

    private static int getLastIndex(ArrayList<LatLng> circlePoints, ArrayList<LatLng> checkAgainst) {
        int endIndex = 0;
        boolean stopChecking = false;
        for (LatLng latLng : circlePoints) {

            if (!PolyUtil.containsLocation(latLng, checkAgainst, isGeoDisc)) {
                endIndex = circlePoints.indexOf(latLng);
                stopChecking = true;
            } else {
                if (stopChecking) {
                    break;
                }
            }
        }
        return endIndex;
    }

    private static int getLastIndexRed(ArrayList<LatLng> pointsGreen,ArrayList<LatLng> newPointsRed, int greenIndex) {
        int lastIndex = 0;
        double latitudeGreen = pointsGreen.get(0).latitude;
        double longitudeGreen = pointsGreen.get(0).longitude;
        double latitudeRed = newPointsRed.get(0).latitude;
        double longitudeRed = newPointsRed.get(0).longitude;
        float[] distance1 = new float[2];
        float[] distance2 = new float[2];

        Location.distanceBetween(latitudeGreen, longitudeGreen, latitudeRed, longitudeRed, distance1);

        for (LatLng latLng : newPointsRed) {
            latitudeRed = latLng.latitude;
            longitudeRed = latLng.longitude;
            Location.distanceBetween(latitudeGreen, longitudeGreen, latitudeRed, longitudeRed, distance2);
            if (distance1[0] >= distance2[0]) {
                distance1[0] = distance2[0];
                lastIndex = newPointsRed.indexOf(latLng);
            }
        }

        return lastIndex;
    }


    private static ArrayList<LatLng> reorderPolygon (ArrayList<LatLng> polygonToSort, int indexLast){
        ArrayList<LatLng> firstList = new ArrayList<>();
        ArrayList<LatLng> lastList = new ArrayList<>();
        ArrayList<LatLng> newList = new ArrayList<>();

        for (LatLng latLng : polygonToSort) {
            if (polygonToSort.indexOf(latLng) <= indexLast) {
                lastList.add(latLng);
            }
            else {
                firstList.add(latLng);
            }
        }
        newList.addAll(firstList);
        newList.addAll(lastList);
        return newList;
    }

    private static ArrayList<LatLng> getCirclePoints(int radius, Location center, int resolution) {
        ArrayList<LatLng> circlePoints = new ArrayList<>();
        final double EARTH_RADIUS = 6378100.0;
        double slice = 2 * Math.PI / resolution;

        double lat = center.getLatitude() * Math.PI / 180.0;
        double lon = center.getLongitude() * Math.PI / 180.0;

        // Get all points that form a circle from your current location
        for (int i = 1; i <= resolution; i++) {
            double angle = slice * i;
            // y
            double latPoint = lat + (radius / EARTH_RADIUS) * Math.sin(angle);
            // x
            double lonPoint = lon + (radius / EARTH_RADIUS) * Math.cos(angle) / Math.cos(lat);

            LatLng p = new LatLng(latPoint * 180.0 / Math.PI, lonPoint * 180.0 / Math.PI);
            circlePoints.add(p);
        }
        return circlePoints;
    }

    public static void clearPolygons(Context context) {
        // Remove all polygons from map
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        // Clear ArrayList holding polygons
        mPolygonsToClear.clear();
        // Clear ArrayList holding polygon point LatLng objects
        mPolygonPointsGreen.clear();

        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        // Clear ArrayList containing hole LatLng objects
        mHoles.clear();
        // Reset transparency on all markers
        SpawnLocation.markerResetTransparency();

        // Remove from database
        myDb.removeAllHoles();
        myDb.removePolygons();;
        myDb.close();
    }

    public static boolean getGeoDisc() {
        return isGeoDisc;
    }

    public static ArrayList<ArrayList<LatLng>> getHoles() {
        return mHoles;
    }

    public static void savePolygonToDB(Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.addPolygon("Temp Polygon1", mPolygonPointsGreen, context);
        myDb.close();
    }

    public static void saveHolesToDB(Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.removeAllHoles();

        for (int i = 0 ; i < mHoles.size() ; i++) {
            myDb.addHole("Temp Hole" + i ,mHoles.get(i));
        }
        myDb.close();
    }
}