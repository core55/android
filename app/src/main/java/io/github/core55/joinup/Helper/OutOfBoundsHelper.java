package io.github.core55.joinup.Helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import io.github.core55.joinup.R;

/**
 * Created by eschmar on 11.05.17.
 */

public class OutOfBoundsHelper {
    /**
     * Calculates the angle component of polar coordinates belonging to a marker
     * with respect to the center of the map bounds.
     *
     * @param bounds
     * @param marker
     * @return
     */
    public static double calculatePolarCoordinateTheta(LatLngBounds bounds, Marker marker) {
        LatLng center = bounds.getCenter();
        LatLng location = marker.getPosition();
        return Math.atan2(location.latitude - center.latitude, location.longitude - center.longitude);
    }

    /**
     * Linear mapping between two intervals
     *
     * @param x
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    public static double linearMapping(double x, double a, double b, double c, double d) {
        return (x - a) / (b - a) * (d - c) + c;
    }

    /**
     * Generate a new position indicator.
     *
     * @param label
     * @param id
     * @param context
     * @return TextView
     */
    public static TextView generatePositionIdicator(String label, int id, Context context) {
        TextView indicator = new TextView(context);
        indicator.setBackgroundResource(R.drawable.out_of_bounds_indicator);
        indicator.setText(label);
        indicator.setTextColor(Color.BLACK);
        indicator.setTypeface(null, Typeface.BOLD);
        return indicator;
    }

    /**
     * Updates the out of bounds indicator position according
     * to the user marker position.
     *
     * @param bounds
     * @param marker
     * @param indicator
     * @param camera
     */
    public static void setIndicatorPosition(LatLngBounds bounds, Marker marker, TextView indicator, CameraPosition camera) {
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

        double offsetTop = new Double(0);
        double offsetLeft = new Double(0);

        // align and offset to correct side
        if (theta <= threeFourthPi && theta >= fourthPi) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            offsetLeft = linearMapping(theta, fourthPi, threeFourthPi, width - indicator.getWidth(), 0);

        } else if (theta > -1 * fourthPi && theta < fourthPi) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            offsetTop = linearMapping(theta, -1 * fourthPi, fourthPi, height - indicator.getHeight(), 0);

        } else if (theta >= -1 * threeFourthPi && theta <= -1 * fourthPi) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            offsetLeft = linearMapping(theta, -1 * threeFourthPi, -1 * fourthPi, 0, width - indicator.getWidth());

        } else if ((theta <= Math.PI && theta >= threeFourthPi) || (theta <= -1 * threeFourthPi && theta >= -1 * Math.PI)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            if (theta <= Math.PI && theta >= threeFourthPi) {
                offsetTop = linearMapping(theta, threeFourthPi, Math.PI, 0, height / 2);
            } else {
                offsetTop = linearMapping(theta, -1 * Math.PI, -1 * threeFourthPi, height / 2, height - indicator.getHeight());
            }
        }

        double offsetTopAdjusted = offsetTop - (indicator.getMeasuredHeight() / 2);
        offsetTop = offsetTopAdjusted < 0 ? 0 : offsetTopAdjusted;

        params.setMargins((int) offsetLeft, (int) offsetTop, 0, 0);
        indicator.setLayoutParams(params);
    }
}