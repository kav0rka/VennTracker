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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

    /*public static ArrayList<LatLng> drawPolygonGreen(GoogleMap gmap, Location center, Context context, int resolution) {
        PolygonOptions polygonOptionsNew = new PolygonOptions()
                .fillColor(0x503EBD1B)
                .strokeColor(0x503EBD1B)
                .geodesic(isGeoDisc);

        if (mPolygonPointsGreen.size() == 0) {
            if (mCirclePointsHole1.size() == 0) {
                mCirclePointsHole1 = getCirclePoints(mHoleRadius, center, resolution);
            }
            return createNewGreenCircle(gmap, center, polygonOptionsNew, resolution);
        }

        // Check if our new green circle is outside of our original
        // If so, do nothing
        ArrayList<LatLng> newCircle = getCirclePoints(mGreenRadius, center, resolution);
        ArrayList<LatLng> newPolygonPointsGreen = combinePolygonGreen(mPolygonPointsGreen, newCircle, false);


        boolean combineHoles = false;

        if (mCirclePointsHole1.size() == 0) {
            mCirclePointsHole1 = getCirclePoints(mHoleRadius, center, resolution);
        } else {
            ArrayList<LatLng> circlePointsHoleNew;
            boolean intersect1 = false;
            boolean intersect2 = false;
            boolean intersect3 = false;
            boolean intersect4 = false;
            boolean intersect5 = false;
            circlePointsHoleNew = getCirclePoints(mHoleRadius, center, resolution);

            // Check which holes our new hole intersects with so we know how to combine it.
            for (LatLng latLng : circlePointsHoleNew) {
                if (!intersect1 && mCirclePointsHole1.size() != 0) {
                    if (PolyUtil.containsLocation(latLng, mCirclePointsHole1, true)) {
                        combineHoles = true;
                        intersect1 = true;
                    }
                }
                if (!intersect2 && mCirclePointsHole2.size() != 0) {
                    if (PolyUtil.containsLocation(latLng, mCirclePointsHole2, true)) {
                        combineHoles = true;
                        intersect2 = true;
                    }
                }
                if (!intersect3 && mCirclePointsHole3.size() != 0) {
                    if (PolyUtil.containsLocation(latLng, mCirclePointsHole3, true)) {
                        combineHoles = true;
                        intersect3 = true;
                    }
                }
                if (!intersect4 && mCirclePointsHole4.size() != 0) {
                    if (PolyUtil.containsLocation(latLng, mCirclePointsHole4, true)) {
                        combineHoles = true;
                        intersect4 = true;
                    }
                }
                if (!intersect5 && mCirclePointsHole5.size() != 0) {
                    if (PolyUtil.containsLocation(latLng, mCirclePointsHole5, true)) {
                        combineHoles = true;
                        intersect5 = true;
                    }
                }
            }

            if (intersect1) {
                mCirclePointsHole1 = combinePolygonGreen(circlePointsHoleNew, mCirclePointsHole1, true);
                if (intersect2) {
                    mCirclePointsHole1 = combinePolygonGreen(mCirclePointsHole2, mCirclePointsHole1, true);
                    mCirclePointsHole2.clear();
                }
                if (intersect3) {
                    mCirclePointsHole1 = combinePolygonGreen(mCirclePointsHole3, mCirclePointsHole1, true);
                    mCirclePointsHole3.clear();
                }
                if (intersect4) {
                    mCirclePointsHole1 = combinePolygonGreen(mCirclePointsHole4, mCirclePointsHole1, true);
                    mCirclePointsHole4.clear();
                }
                if (intersect5) {
                    mCirclePointsHole1 = combinePolygonGreen(mCirclePointsHole5, mCirclePointsHole1, true);
                    mCirclePointsHole5.clear();
                }
            } else if (intersect2) {
                mCirclePointsHole2 = combinePolygonGreen(circlePointsHoleNew, mCirclePointsHole2, true);
                if (intersect3) {
                    mCirclePointsHole2 = combinePolygonGreen(mCirclePointsHole3, mCirclePointsHole2, true);
                    mCirclePointsHole3.clear();
                }
                if (intersect4) {
                    mCirclePointsHole2 = combinePolygonGreen(mCirclePointsHole4, mCirclePointsHole2, true);
                    mCirclePointsHole4.clear();
                }
                if (intersect5) {
                    mCirclePointsHole2 = combinePolygonGreen(mCirclePointsHole5, mCirclePointsHole2, true);
                    mCirclePointsHole5.clear();
                }
            } else if (intersect3){
                mCirclePointsHole3 = combinePolygonGreen(circlePointsHoleNew, mCirclePointsHole3, true);
                if (intersect4) {
                    mCirclePointsHole3 = combinePolygonGreen(mCirclePointsHole4, mCirclePointsHole3, true);
                    mCirclePointsHole4.clear();
                }
                if (intersect5) {
                    mCirclePointsHole3 = combinePolygonGreen(mCirclePointsHole5, mCirclePointsHole3, true);
                    mCirclePointsHole5.clear();
                }
            } else if (intersect4) {
                mCirclePointsHole4 = combinePolygonGreen(circlePointsHoleNew, mCirclePointsHole4, true);
                if (intersect5) {
                    mCirclePointsHole4 = combinePolygonGreen(mCirclePointsHole5, mCirclePointsHole4, true);
                    mCirclePointsHole5.clear();
                }
            } else if (intersect5) {
                mCirclePointsHole5 = combinePolygonGreen(circlePointsHoleNew, mCirclePointsHole5, true);
            }

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
            if (!combineHoles && !holeOutside) {
                if (mCirclePointsHole2.size() == 0) {
                    mCirclePointsHole2 = circlePointsHoleNew;
                } else if(mCirclePointsHole3.size() == 0) {
                    mCirclePointsHole3 = circlePointsHoleNew;
                } else  if (mCirclePointsHole4.size() == 0) {
                    mCirclePointsHole4 = circlePointsHoleNew;
                } else if (mCirclePointsHole5.size() == 0) {
                    mCirclePointsHole5 = circlePointsHoleNew;
                }
            }
        }

        // Check if any of our new holes intersect with our polygon
        // If so, subtract them
        if (mCirclePointsHole1.size() != 0) {
            for (LatLng latLng : newPolygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole1, isGeoDisc)) {
                    newPolygonPointsGreen = subtractCircleNew(newPolygonPointsGreen, mCirclePointsHole1, context);
                    mCirclePointsHole1.clear();
                    break;
                }
            }
        }
        if (mCirclePointsHole2.size() != 0) {
            for (LatLng latLng : newPolygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole2, isGeoDisc)) {
                    newPolygonPointsGreen = subtractCircleNew(newPolygonPointsGreen, mCirclePointsHole2, context);
                    mCirclePointsHole2.clear();
                    break;
                }
            }
        }
        if (mCirclePointsHole3.size() != 0) {
            for (LatLng latLng : newPolygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole3, isGeoDisc)) {
                    newPolygonPointsGreen = subtractCircleNew(newPolygonPointsGreen, mCirclePointsHole3, context);
                    mCirclePointsHole3.clear();
                    break;
                }
            }
        }
        if (mCirclePointsHole4.size() != 0) {
            for (LatLng latLng : newPolygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole4, isGeoDisc)) {
                    newPolygonPointsGreen = subtractCircleNew(newPolygonPointsGreen, mCirclePointsHole4, context);
                    mCirclePointsHole4.clear();
                    break;
                }
            }
        }
        if (mCirclePointsHole5.size() != 0) {
            for (LatLng latLng : newPolygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole5, isGeoDisc)) {
                    newPolygonPointsGreen = subtractCircleNew(newPolygonPointsGreen, mCirclePointsHole5, context);
                    mCirclePointsHole5.clear();
                    break;
                }
            }
        }


        // Only add a hole if it is completely inside of our polygon.
        if (mCirclePointsHole1.size() != 0) {
            int count = 0;
            for (LatLng latLng : mCirclePointsHole1) {
                if (PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    count++;
                }
            }
            if (count == mCirclePointsHole1.size()) {
                polygonOptionsNew.addHole(mCirclePointsHole1);
            }
        }
        if (mCirclePointsHole2.size() !=0) {
            int count = 0;
            for (LatLng latLng : mCirclePointsHole2) {
                if (PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    count++;
                }
            }
            if (count == mCirclePointsHole2.size()) {
                polygonOptionsNew.addHole(mCirclePointsHole2);
            }
        }
        if (mCirclePointsHole3.size() !=0) {
            int count = 0;
            for (LatLng latLng : mCirclePointsHole3) {
                if (PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    count++;
                }
            }
            if (count == mCirclePointsHole3.size()) {
                polygonOptionsNew.addHole(mCirclePointsHole3);
            }
        }
        if (mCirclePointsHole4.size() !=0) {
            int count = 0;
            for (LatLng latLng : mCirclePointsHole4) {
                if (PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    count++;
                }
            }
            if (count == mCirclePointsHole4.size()) {
                polygonOptionsNew.addHole(mCirclePointsHole4);
            }
        }
        if (mCirclePointsHole5.size() !=0) {
            int count = 0;
            for (LatLng latLng : mCirclePointsHole5) {
                if (PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    count++;
                }
            }
            if (count == mCirclePointsHole5.size()) {
                polygonOptionsNew.addHole(mCirclePointsHole5);
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
    }*/

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
            boolean intersect1 = false;
            boolean intersect2 = false;
            boolean intersect3 = false;
            boolean intersect4 = false;
            boolean intersect5 = false;
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

            /*// Check which holes our new hole intersects with so we know how to combine it.
            for (LatLng latLng : circlePointsHoleNew) {
                if (!intersect1 && mHoles.size() > 0) {
                    if (PolyUtil.containsLocation(latLng, mHoles.get(0), true)) {
                        combineHoles = true;
                        intersect1 = true;
                    }
                }
                if (!intersect2 && mHoles.size() > 1) {
                    if (PolyUtil.containsLocation(latLng, mHoles.get(1), true)) {
                        combineHoles = true;
                        intersect2 = true;
                    }
                }
                if (!intersect3 && mHoles.size() > 2) {
                    if (PolyUtil.containsLocation(latLng, mHoles.get(2), true)) {
                        combineHoles = true;
                        intersect3 = true;
                    }
                }
                if (!intersect4 && mHoles.size() > 3) {
                    if (PolyUtil.containsLocation(latLng, mHoles.get(3), true)) {
                        combineHoles = true;
                        intersect4 = true;
                    }
                }
                if (!intersect5 && mHoles.size() > 4) {
                    if (PolyUtil.containsLocation(latLng, mHoles.get(4), true)) {
                        combineHoles = true;
                        intersect5 = true;
                    }
                }
            }

            if (intersect1) {
                mHoles.set(0, combinePolygonGreen(circlePointsHoleNew, mHoles.get(0), true));
                if (intersect2) {
                    mHoles.set(0, combinePolygonGreen(circlePointsHoleNew, mHoles.get(0), true));
                    mHoles.remove(1);
                }
                if (intersect3) {
                    mHoles.set(0, combinePolygonGreen(mHoles.get(2), mHoles.get(0), true));
                    mHoles.remove(2);
                }
                if (intersect4) {
                    mHoles.set(0, combinePolygonGreen(mHoles.get(3), mHoles.get(0), true));
                    mHoles.remove(3);
                }
                if (intersect5) {
                    mHoles.set(0, combinePolygonGreen(mHoles.get(4), mHoles.get(0), true));
                    mHoles.remove(4);
                }
            } else if (intersect2) {
                mHoles.set(1, combinePolygonGreen(circlePointsHoleNew, mHoles.get(1), true));
                if (intersect3) {
                    mHoles.set(1, combinePolygonGreen(mHoles.get(2), mHoles.get(1), true));
                    mHoles.remove(2);
                }
                if (intersect4) {
                    mHoles.set(1, combinePolygonGreen(mHoles.get(3), mHoles.get(1), true));
                    mHoles.remove(3);
                }
                if (intersect5) {
                    mHoles.set(1, combinePolygonGreen(mHoles.get(4), mHoles.get(1), true));
                    mHoles.remove(4);
                }
            } else if (intersect3){
                mHoles.set(2, combinePolygonGreen(circlePointsHoleNew, mHoles.get(2), true));
                if (intersect4) {
                    mHoles.set(2, combinePolygonGreen(mHoles.get(3), mHoles.get(2), true));
                    mHoles.remove(3);
                }
                if (intersect5) {
                    mHoles.set(2, combinePolygonGreen(mHoles.get(4), mHoles.get(2), true));
                    mHoles.remove(4);
                }
            } else if (intersect4) {
                mHoles.set(3, combinePolygonGreen(circlePointsHoleNew, mHoles.get(3), true));
                if (intersect5) {
                    mHoles.set(3, combinePolygonGreen(mHoles.get(4), mHoles.get(3), true));
                    mHoles.remove(4);
                }
            } else if (intersect5) {
                mHoles.set(4, combinePolygonGreen(circlePointsHoleNew, mHoles.get(4), true));
            }*/

            // Check if our hole falls outside of the polygon
            // Don't add it if it does
            /*boolean holeOutside = false;
            for (LatLng latLng : circlePointsHoleNew) {
                if (!PolyUtil.containsLocation(latLng, newPolygonPointsGreen, isGeoDisc)) {
                    holeOutside = true;
                }
                else {
                    holeOutside = false;
                    break;
                }
            }*/

        }

        // Check if any of our new holes intersect with our polygon
        ArrayList<ArrayList<LatLng>> holesToRemove = new ArrayList<>();
        for (int i = 0 ; i < mHoles.size() ; i++) {
            for (LatLng latLng : newPolygonPointsGreen) {
                if (PolyUtil.containsLocation(latLng, mHoles.get(i), isGeoDisc)) {
                    newPolygonPointsGreen = subtractCircleNew(newPolygonPointsGreen, mHoles.get(i), context);
                    // Store hole to remove from mHoles later
                    holesToRemove.add(mHoles.get(i));
                    System.out.println(mHoles.get(i).size());
                    break;
                }
            }
        }

        // remove holes from mHoles that were subtracted from our polygon
        for (int i = 0 ; i < mHoles.size() ; i++){
            mHoles.removeAll(holesToRemove);
        }



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

        ArrayList<LatLng> polygonPointsGreen = subtractCircleNew(mPolygonPointsGreen, circleRed, context);


        if (polygonPointsGreen.size() == 0) {
            clearPolygons(context);
            return mPolygonPointsGreen;
        }

        // Check if we need to subtract any holes
        boolean subtractHole1 = false;
        boolean subtractHole2 = false;
        boolean subtractHole3 = false;
        boolean subtractHole4 = false;
        boolean subtractHole5 = false;
        for (LatLng latLng : polygonPointsGreen) {
            if (mCirclePointsHole1.size() !=0 && !subtractHole1) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole1, isGeoDisc)) {
                    subtractHole1 = true;
                }
            }
            if (mCirclePointsHole2.size() !=0 && !subtractHole2) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole2, isGeoDisc)) {
                    subtractHole2 = true;
                }
            }
            if (mCirclePointsHole3.size() !=0 && !subtractHole3) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole3, isGeoDisc)) {
                    subtractHole3 = true;
                }
            }
            if (mCirclePointsHole4.size() !=0 && !subtractHole4) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole4, isGeoDisc)) {
                    subtractHole4 = true;
                }
            }
            if (mCirclePointsHole5.size() !=0 && !subtractHole5) {
                if (PolyUtil.containsLocation(latLng, mCirclePointsHole5, isGeoDisc)) {
                    subtractHole5 = true;
                }
            }
        }

        // If there is a hole subtract it from our new polygon
        if (mCirclePointsHole1.size() != 0 && subtractHole1) {
            polygonPointsGreen = subtractCircleNew(polygonPointsGreen, mCirclePointsHole1, context);
            mCirclePointsHole1.clear();
        }
        if (mCirclePointsHole2.size() != 0 && subtractHole2) {
            polygonPointsGreen = subtractCircleNew(polygonPointsGreen, mCirclePointsHole2, context);
            mCirclePointsHole2.clear();
        }
        if (mCirclePointsHole3.size() != 0 && subtractHole3) {
            polygonPointsGreen = subtractCircleNew(polygonPointsGreen, mCirclePointsHole3, context);
            mCirclePointsHole3.clear();
        }
        if (mCirclePointsHole4.size() != 0 && subtractHole4) {
            polygonPointsGreen = subtractCircleNew(polygonPointsGreen, mCirclePointsHole4, context);
            mCirclePointsHole4.clear();
        }
        if (mCirclePointsHole5.size() != 0 && subtractHole5) {
            polygonPointsGreen = subtractCircleNew(polygonPointsGreen, mCirclePointsHole5, context);
            mCirclePointsHole5.clear();
        }

        // Check if our holes are completely within our red circle
        int holeCount = 0;
        if (mCirclePointsHole1.size() !=0) {
            for (LatLng latLng : mCirclePointsHole1) {
                if (PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)){
                    holeCount += 1;
                }
            }
            if (holeCount == mCirclePointsHole1.size()) {
                mCirclePointsHole1.clear();
                holeCount = 0;
            }
        }
        if (mCirclePointsHole2.size() !=0) {
            for (LatLng latLng : mCirclePointsHole2) {
                if (PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)){
                    holeCount += 1;
                }
            }
            if (holeCount == mCirclePointsHole2.size()) {
                mCirclePointsHole2.clear();
                holeCount = 0;
            }
        }
        if (mCirclePointsHole3.size() !=0) {
            for (LatLng latLng : mCirclePointsHole3) {
                if (PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)){
                    holeCount += 1;
                }
            }
            if (holeCount == mCirclePointsHole3.size()) {
                mCirclePointsHole3.clear();
                holeCount = 0;
            }
        }
        if (mCirclePointsHole4.size() !=0) {
            for (LatLng latLng : mCirclePointsHole4) {
                if (PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)){
                    holeCount += 1;
                }
            }
            if (holeCount == mCirclePointsHole4.size()) {
                mCirclePointsHole4.clear();
                holeCount = 0;
            }
        }
        if (mCirclePointsHole5.size() !=0) {
            for (LatLng latLng : mCirclePointsHole5) {
                if (PolyUtil.containsLocation(latLng, circleRed, isGeoDisc)){
                    holeCount += 1;
                }
            }
            if (holeCount == mCirclePointsHole5.size()) {
                mCirclePointsHole5.clear();
                holeCount = 0;
            }
        }



        // Remove any polygons we currently have before drawing our new polygon
        for (Polygon polygon : mPolygonsToClear) {
            polygon.remove();
        }
        // Make sure we clear our green points before adding the new points
        mPolygonPointsGreen.clear();

        // Check if we need to add any holes
        if (mCirclePointsHole1.size() != 0 && !subtractHole1) {
            polygonOptionsNew.addHole(mCirclePointsHole1);
        }
        if (mCirclePointsHole2.size() != 0 && !subtractHole2) {
            polygonOptionsNew.addHole(mCirclePointsHole2);
        }
        if (mCirclePointsHole3.size() != 0 && !subtractHole3) {
            polygonOptionsNew.addHole(mCirclePointsHole3);
        }
        if (mCirclePointsHole4.size() != 0 && !subtractHole4) {
            polygonOptionsNew.addHole(mCirclePointsHole4);
        }
        if (mCirclePointsHole5.size() != 0 && !subtractHole5) {
            polygonOptionsNew.addHole(mCirclePointsHole5);
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

    public static ArrayList<LatLng> subtractCircleNew (ArrayList<LatLng> polygonGreen, ArrayList<LatLng> polygonSubtract, Context context) {

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
        //endIndex2 = getLastIndexRed(polygonGreen, polygonSubtract, endIndex1);
        //polygonSubtract = reorderPolygon(polygonSubtract, endIndex2);

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



    public static ArrayList<LatLng> reverseIfNeeded(ArrayList<LatLng> checkAgainst, ArrayList<LatLng> listToReverse) {
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

    public static int getLastIndex(ArrayList<LatLng> circlePoints, ArrayList<LatLng> checkAgainst) {
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

    public static int getLastIndexRed(ArrayList<LatLng> pointsGreen,ArrayList<LatLng> newPointsRed, int greenIndex) {
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


    public static ArrayList<LatLng> reorderPolygon (ArrayList<LatLng> polygonToSort, int indexLast){
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

    public static ArrayList<LatLng> getCirclePoints(int radius, Location center, int resolution) {
        ArrayList<LatLng> circlePoints = new ArrayList<>();
        double EARTH_RADIUS = 6378100.0;
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
