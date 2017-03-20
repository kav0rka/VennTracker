package kavorka.venn_tracker;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;

public class Circles {
    private static ArrayList<Polygon> mPolygonsToClear = new ArrayList<>();
    private static ArrayList<LatLng> mPolygonPointsGreen = new ArrayList<>();
    private static ArrayList<LatLng> mPolygonsRed =  new ArrayList<>();
    private static ArrayList<Circle> mPolygonsRedToClear =  new ArrayList<>();
    private static ArrayList<ArrayList<LatLng>> mHoles = new ArrayList<>();
    private static ArrayList<LatLng> mIntersecting = new ArrayList<>();
    private static ArrayList<Marker> mIntersectingToClear = new ArrayList<>();


    private static int mGreenRadius = 200;
    private static int mRedRadius = 200;
    private static int mHoleRadius = 40;
    private static boolean isGeoDisc = true;


    public static ArrayList<LatLng> getPolygonsRed() {
        return mPolygonsRed;
    }

    public static ArrayList<LatLng> loadAllPolygons(Context context, GoogleMap gmap, boolean transparency) {
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        for (Circle circle : mPolygonsRedToClear) {
            circle.remove();
        }
        // TODO also clear markers for intersections before reloading
        mHoles.clear();
        mPolygonPointsGreen.clear();
        mPolygonsRed.clear();
        mIntersecting.clear();

        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        mHoles = myDb.getAllHoles();
        mPolygonsRed = myDb.getAllRedPolygons("Temp Polygon Red");
        mIntersecting = myDb.getIntersections();
        for (LatLng latLng : mIntersecting) {
            setIntersection(context, gmap, latLng);
        }

        // TODO do above for green polygons as well
        double latitude;
        double longitude;
        Cursor res = myDb.getAllLocations("Temp Polygon1");

        // For each saved polygon
        // Get the latitude and longitude and put them into a location
        while (res.moveToNext()) {
            latitude = res.getDouble(2);
            longitude = res.getDouble(3);
            LatLng latLng = new LatLng(latitude, longitude);
            mPolygonPointsGreen.add(latLng);
        }

        PolygonOptions polygonOptionsGreen = new PolygonOptions()
                .fillColor(0x503EBD1B)
                .strokeColor(0x503EBD1B)
                .geodesic(isGeoDisc);

        if (mPolygonPointsGreen.size() > 0) {
            polygonOptionsGreen.addAll(mPolygonPointsGreen);

            for (ArrayList<LatLng> hole : mHoles) {
                polygonOptionsGreen.addHole(hole);
            }


            Polygon polygon = gmap.addPolygon(polygonOptionsGreen);
            mPolygonsToClear.add(polygon);
            if (transparency) {
                SpawnLocation.markerInCircle(mPolygonPointsGreen);
            }
        }
        res.close();
        return mPolygonPointsGreen;
    }

    // TODO temp code
    public static void drawCircleRed(LatLng latLng) {
        mPolygonsRed.add(latLng);
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


        if (mHoles.size() == 0) {
            ArrayList<LatLng> hole = getCirclePoints(mHoleRadius, center, resolution);
            mHoles.add(hole);
        } else {
            // Add new hole
            mHoles.add(getCirclePoints(mHoleRadius, center, resolution));


            // Check if any hole falls  completely outside of our polygon, if so remove it
            for (int i = mHoles.size() -1 ; i >= 0 ; i--) {
                if (isOutside(mHoles.get(i), newPolygonPointsGreen)) {
                    mHoles.remove(i);
                }
            }
            // If we have more than 1 hole, check if any interect.  If so, combine them
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
        int circleHoleResolution = Math.round(resolution / 2);

        mPolygonPointsGreen = getCirclePoints(mGreenRadius, center, resolution);
        if (mHoles.size() == 0) {
            mHoles.add(getCirclePoints(mHoleRadius, center, circleHoleResolution));
        }

        // Remove any polygons we currently have before drawing our new polygon
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        mPolygonsToClear.clear();

        if (mPolygonPointsGreen.size() > 0) {
            polygonOptionsNew.addAll(mPolygonPointsGreen);
            if (mHoles.size() > 0) {
                for (ArrayList<LatLng> hole : mHoles) {
                    polygonOptionsNew.addHole(hole);
                }
            }
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


        // TODO if red circle falls outside of our green polygon, ignore
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

        // Remove holes that fall completely within our red circle
        for (int i = mHoles.size() - 1 ; i >= 0 ; i--) {
            boolean isInside = true;
            for (LatLng latLng : mHoles.get(i)) {
                if (!PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)) {
                    isInside = false;
                    break;
                }
            }
            if (isInside) {
                mHoles.remove(i);

            }
        }

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

    public static void checkIntersecting(Context context, GoogleMap gMap) {
        for (int i = 0 ; i < mPolygonsRed.size() - 1 ; i++) {
            for (int j = 0; j < mPolygonsRed.size(); j++) {
                if (i == j) {
                    continue;
                }

                float[] distance = new float[2];
                LatLng circle1 = mPolygonsRed.get(i);
                LatLng circle2 = mPolygonsRed.get(i + 1);
                Location.distanceBetween(circle1.latitude, circle1.longitude, circle2.latitude, circle2.longitude, distance);

                if (distance[0] < mRedRadius * 2) {
                    Location circle1Center = new Location("");
                    Location circle2Center = new Location("");
                    circle1Center.setLatitude(circle1.latitude);
                    circle1Center.setLongitude(circle1.longitude);
                    circle2Center.setLatitude(circle2.latitude);
                    circle2Center.setLongitude(circle2.longitude);
                    // Create new circles that are slightly larger than the circle we are subtracting with
                    // This will ensure that our intersections fall within our green polygon
                    ArrayList<LatLng> newCircle1 = getCirclePoints(mRedRadius + 4, circle1Center, 360);
                    ArrayList<LatLng> newCircle2 = getCirclePoints(mRedRadius + 4, circle2Center, 360);

                    /*
                     * Find all points on our circles that intersect with a margin of error
                     * Save all of those points that fall within our green polygon in intersections
                     * This will leave us with some false positives that we remove with simplifyIntersections()
                     */
                    for (LatLng latLng : newCircle1) {
                        if (PolyUtil.containsLocation(latLng, mPolygonPointsGreen, isGeoDisc)) {
                            if (PolyUtil.isLocationOnEdge(latLng, newCircle2, isGeoDisc, 4)) {
                                mIntersecting.add(latLng);
                            }
                        }
                    }
                    // Simplify intersections, we should only have 0, 1, or 2 intersections possible
                    mIntersecting = simplifyIntersections(mIntersecting);
                    if (mIntersecting.size() > 0) {
                        for (LatLng latLng : mIntersecting) {
                            setIntersection(context, gMap, latLng);
                        }
                        Toast.makeText(context, "Narrowed down location of the Pokemon!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /*
     * When we run checkIntersecting() we can get several points around each intersection
     * We want to remove any points within 10 meters of each other so we only have 1 or 2 intersections
     * This will get us the closest to our true intersections without running complicated trigonometry
     */

    // leave m intersecting but do calculations on intersecting and pass it into simplify
    private static ArrayList<LatLng> simplifyIntersections(ArrayList<LatLng> intersections) {
        // If the size is 1, no need to check anything
        if (intersections.size() > 1) {
            // Loop from the end so we can remove a without an error
            for (int a = intersections.size() - 1; a > 0; a--) {
                for (int b = 0; b < intersections.size(); b++) {
                    // don't check itself
                    if (a == b) {
                        continue;
                    }
                    // Get the distance between the 2 points
                    float[] distanceIntersections = new float[2];
                    Location.distanceBetween(
                            intersections.get(a).latitude,
                            intersections.get(a).longitude,
                            intersections.get(b).latitude,
                            intersections.get(b).longitude,
                            distanceIntersections);
                    // Our margin of error is 20 meters
                    //This isn't perfect but it will get us a close enough solution
                    if (distanceIntersections[0] < 20) {
                        // Too close, remove it
                        intersections.remove(a);
                        if (intersections.size() > 1) {
                            // Recursivly remove locations
                            return simplifyIntersections(intersections);
                        }
                    }
                }
            }
        }
        return intersections;
    }

    // Create marker at intersection
    private static void setIntersection(Context context, GoogleMap gMap, LatLng latLng) {
        Marker marker = gMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow)));
        mIntersectingToClear.add(marker);
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.saveIntersections(mIntersecting);
        myDb.close();
    }

    public static void saveRedCirclesToDb(Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.removeCircles();
        for (int i = 0 ; i < mPolygonsRed.size() ; i++) {
            myDb.addLocation("Temp Polygon Red", mPolygonsRed.get(i).latitude, mPolygonsRed.get(i).longitude, "");
        }
        myDb.close();
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
        for (Circle circle : mPolygonsRedToClear) {
            circle.remove();
        }
        for (Marker marker : mIntersectingToClear) {
            marker.remove();
        }
        mIntersecting.clear();
        mIntersectingToClear.clear();
        mPolygonsRedToClear.clear();
        mPolygonsRed.clear();
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
        myDb.removePolygons();
        myDb.removeCircles();
        myDb.removeIntersections();
        myDb.close();
    }

    public static boolean getGeoDisc() {
        return isGeoDisc;
    }

    public static ArrayList<ArrayList<LatLng>> getHoles() {
        return mHoles;
    }

    private static boolean isOutside(ArrayList<LatLng> hole, ArrayList<LatLng> polygon) {
        boolean isOutside = false;
        for (LatLng latLng : hole) {
            if (!PolyUtil.containsLocation(latLng, polygon, isGeoDisc)) {
                isOutside = true;
            }
            else {
                return false;
            }
        }
        return isOutside;
    }

    public static void savePolygonToDB(String type, ArrayList<LatLng> polygon,  Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.addPolygon(type, polygon);
        myDb.close();
    }

    public static void saveHolesToDB(Context context) {
        DatabaseHelper myDb = DatabaseHelper.getInstance(context);
        myDb.removeAllHoles();

        for (int i = 0 ; i < mHoles.size() ; i++) {
            myDb.addHole("Temp Hole" + (i + 1), mHoles.get(i));
        }
        myDb.close();
    }


}