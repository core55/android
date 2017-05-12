package io.github.core55.joinup.Helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import io.github.core55.joinup.R;

/**
 * Created by eschmar on 11.05.17.
 */

public class OutOfBoundsHelper {
    /*
     *  Calculates polar coorindates
     * */
    public static double calculatePolarCoordinateTheta(LatLngBounds bounds, MarkerOptions marker) {
        LatLng center = bounds.getCenter();
        LatLng location = marker.getPosition();
        return Math.atan2(location.latitude - center.latitude, location.longitude - center.longitude);
    }

    /*
     *  Linear mapping between two interwals
     * */
    public static double linearMapping(double x, double a, double b, double c, double d) {
        return (x - a) / (b - a) * (d - c) + c;
    }

    /*
     *  Generate a new position indicator.
     * */
    public static TextView generatePositionIdicator(String label, int id, Context context) {
        TextView indicator = new TextView(context);
        indicator.setBackgroundResource(R.drawable.out_of_bounds_indicator);
        indicator.setText(label);
        indicator.setTextColor(Color.WHITE);
        indicator.setTypeface(null, Typeface.BOLD);
        return indicator;
    }

    public static void setIndicatorPosition(LatLngBounds bounds, MarkerOptions marker, TextView indicator, CameraPosition camera) {
        double theta = calculatePolarCoordinateTheta(bounds, marker);
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // todo: adjust for bearing
        /*double degrees = Math.toDegrees(theta);
        double bearing = (double) camera.bearing;

        // experimental
        double bearingAngleToNorth = Math.abs(bearing - 360) % 360;
        theta += Math.toRadians(bearingAngleToNorth);*/

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) indicator.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);

        double fourthPi = Math.PI / 4;
        double threeFourthPi = 3 * fourthPi;
        double offset = new Double(0);

        // align to correct side
        if (theta <= threeFourthPi && theta >= fourthPi) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            offset = linearMapping(theta, fourthPi, threeFourthPi, width - indicator.getWidth(), 0);
            params.setMargins((int) offset, 0, 0, 0);
            Log.d("MapActivity", " -> TOP ");

        }else if (theta > -1 * fourthPi && theta < fourthPi) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            offset = linearMapping(theta, -1 * fourthPi, fourthPi, height - indicator.getHeight(), 0);
            params.setMargins(0, (int) offset, 0, 0);
            Log.d("MapActivity", " -> RIGHT ");

        }else if (theta >= -1 * threeFourthPi && theta <= -1 * fourthPi) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            offset = linearMapping(theta, -1 * threeFourthPi, -1 * fourthPi, 0, width - indicator.getWidth());
            params.setMargins((int) offset, 0, 0, 0);
            Log.d("MapActivity", " -> BOTTOM ");

        }else if ((theta <= Math.PI && theta >= threeFourthPi) || (theta <= -1 * threeFourthPi && theta >= -1 * Math.PI)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            if (theta <= Math.PI && theta >= threeFourthPi) {
                offset = linearMapping(theta, threeFourthPi, Math.PI, 0, height / 2);
            }else {
                offset = linearMapping(theta, -1 * Math.PI, -1 * threeFourthPi, height / 2, height - indicator.getHeight());
            }

            params.setMargins(0, (int) offset, 0, 0);
            Log.d("MapActivity", " -> LEFT ");
        }

        indicator.setLayoutParams(params);
    }
}