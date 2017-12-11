package cz.intesys.trainalert.animation;

import org.osmdroid.util.GeoPoint;

public interface GeoPointInterpolator {
    GeoPoint interpolate(float fraction, GeoPoint a, GeoPoint b);

    class Linear implements GeoPointInterpolator {
        @Override
        public GeoPoint interpolate(float fraction, GeoPoint a, GeoPoint b) {
            double lat = (b.getLatitude() - a.getLatitude()) * fraction + a.getLatitude();
            double lng = (b.getLongitude() - a.getLongitude()) * fraction + a.getLongitude();
            return new GeoPoint(lat, lng);
        }
    }

    class LinearFixed implements GeoPointInterpolator {
        @Override
        public GeoPoint interpolate(float fraction, GeoPoint a, GeoPoint b) {
            double lat = (b.getLatitude() - a.getLatitude()) * fraction + a.getLatitude();
            double lngDelta = b.getLongitude() - a.getLongitude();

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + a.getLongitude();
            return new GeoPoint(lat, lng);
        }
    }
}