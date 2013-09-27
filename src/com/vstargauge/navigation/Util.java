/**
 * 
 */
package com.vstargauge.navigation;

import android.graphics.Point;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.vstargauge.util.Constants;
import com.vstargauge.util.GraphicsPoint;

/**
 * @author PyleC1
 * 
 */
public class Util implements Constants {
	// =============================================
	// Private/Protected variables

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	// =============================================
	// Constructors

	// =============================================
	// Overrides

	// =============================================
	// Methods



	public static Point geoPoint2Point(Location loc) {
		int longE6 = (int) (loc.getLongitude() * 1e6);
		int latE6 = (int) (loc.getLatitude() * 1e6);
		return new Point(latE6, longE6);
	}

	/**
	 * @param linePointA
	 * @param linePointB
	 * @param lastLocation
	 * @return
	 */
	public static LatLng getProjectedLatLng(LatLng linePointA,
			LatLng linePointB, Location lastLocation) {

		return getProjectedLatLng(linePointA, linePointB,
				new Point((int) (lastLocation.getLongitude() * 1e6),
						(int) (lastLocation.getLatitude() * 1e6)));
	}

	public static LatLng getProjectedLatLng(final LatLng linePointA,
			final LatLng linePointB, final Point p) {
		final Point a = new Point((int) (linePointA.longitude * 1e6),
				(int) (linePointA.latitude * 1e6));
		final Point b = new Point((int) (linePointB.longitude * 1e6),
				(int) (linePointB.latitude * 1e6));

		if (a.x == b.x && a.y == b.y) {
			return null;
		}

		/* s is the vector from a to b */
		final Point s = GraphicsPoint.difference(b, a);
		final float length_s = (float) Math.sqrt(((long) s.x) * s.x
				+ ((long) s.y) * s.y);

		/* r is the vector from a to p */
		final Point r = GraphicsPoint.difference(p, a);

		/* The case when the angle at a is 'overstretched' */
		/* Determine the angle between s and r. */
		final double angleAtA = Math.acos(GraphicsPoint.dotProduct(r, s)
				/ (length_s * Math
						.sqrt(((long) r.x) * r.x + ((long) r.y) * r.y)));
		/* If it is bigger than |90°| return null. */
		if (Math.abs(angleAtA) > PI_HALF) {
			return null;
		} else if (Double.isNaN(angleAtA)) {
			return new LatLng(p.y, p.x); /*
										 * MapPoint is defined as
										 * Latitude(Y),Longitude(X).
										 */
		}

		/* Attention: s now points to the other direction! */
		s.negate();

		/* t is the vector from b to p. */
		final Point t = GraphicsPoint.difference(p, b);

		/* The case when the angle at b is 'overstretched' */
		/* Determine the angle between s and r. */
		final float angleAtB = (float) Math.acos(GraphicsPoint.dotProduct(s, t)
				/ (length_s * Math
						.sqrt(((long) t.x) * t.x + ((long) t.y) * t.y)));
		/* If it is bigger than |90°| return b */
		if (Math.abs(angleAtB) > PI_HALF) {
			return null;
			// NOTE: Not needed because angleAtA would also be NaN !
			// else if(Double.isNaN(angleAtB))
			// return new MapPoint(p.x,p.y);
		}

		/* Attention: s now points back to the original direction! */
		s.negate();

		/* Do the actual projection */
		/*
		 * First: Calculate the geometric distance. |(p-a) x b| / |b|
		 */
		final float distance = (float) (GraphicsPoint.crossProduct(
				GraphicsPoint.difference(p, a), s) / Math
				.sqrt(((long) s.x * s.x) + ((long) s.y) * s.y));

		/*
		 * Calculate the gradient of the line from a to be (what already is
		 * 's').
		 */
		final float angleOfOrthogonalRad = (float) Math.atan2(-s.x, s.y);

		/* NOTE: MapPoint is defined as Latitude(Y),Longitude(X). */
		return new LatLng(p.y
				- Math.round(distance * Math.sin(angleOfOrthogonalRad)), p.x
				- Math.round(distance * (float) Math.cos(angleOfOrthogonalRad)));
	}

	/**
	 * Calculates the distance from a line spanned between two MapPoints to
	 * another MapPoint. This is only the geometric distance, when the
	 * search-point is located like: P or Q, but not like X,Y or Z:
	 * 
	 * <pre>
	 * ^
	 * |   Y |   P |
	 * |     |     |
	 * |     A-----B
	 * |     |     |  Z
	 * |  X  |  Q  |
	 * |
	 * 0----------------->
	 * </pre>
	 * 
	 * In cases of X and Y, the distance to A would be returned. In the case of
	 * Z, the distance to B would be returned.
	 * 
	 * @param linePointA
	 *            First (Map)Point on the line
	 * @param linePointB
	 *            Second (Map)Point on the line
	 * @param p
	 *            Point to determine the distance to the line
	 * @return distance from a Point to a line
	 */
	public static float getDistanceToLine(final LatLng linePointA,
			final LatLng linePointB, final Point p) {
		/* a: Point A on the line. */
		final Point a = new Point((int) (linePointA.longitude * 1e6),
				(int) (linePointA.latitude * 1e6));
		/* b: Point B on the line. */
		final Point b = new Point((int) (linePointB.longitude * 1e6),
				(int) (linePointB.latitude));
		return getDistanceToLine(a, b, p);
	}

	public static float getDistanceToLine(final Point a, final Point b,
			final Point p) {

		/* If a is the same point as b then return the distance from p to a. */
		if (a.x == b.x && a.y == b.y) {
			final int dx = p.x - a.x;
			final int dy = p.y - a.y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

		/* s is the vector from a to b. */
		final Point s = com.vstargauge.util.GraphicsPoint.difference(b, a);
		final float lenght_s = (float) Math.sqrt(((long) s.x) * s.x
				+ ((long) s.y) * s.y);

		/* r is the vector from a to p. */
		final Point r = com.vstargauge.util.GraphicsPoint.difference(p, a);

		/* The case when the angle at a is 'overstretched' */
		/* Determine the angle between s and r. */
		final double angleAtA = Math.acos(com.vstargauge.util.GraphicsPoint
				.dotProduct(r, s)
				/ (lenght_s * Math
						.sqrt(((long) r.x) * r.x + ((long) r.y) * r.y)));
		/* If it is bigger than |90°| return distance from p to a */
		if (Math.abs(angleAtA) > PI_HALF) {
			final int dx = p.x - a.x;
			final int dy = p.y - a.y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

		/* Attention: s now points to the other direction! */
		s.negate();

		/* t is the vector from b to p. */
		final Point t = com.vstargauge.util.GraphicsPoint.difference(p, b);

		/* The case when the angle at b is 'overstretched' */
		/* Determine the angle between s and r. */
		final double angleAtB = Math.acos(com.vstargauge.util.GraphicsPoint
				.dotProduct(s, t)
				/ (lenght_s * Math
						.sqrt(((long) t.x * t.x) + ((long) t.y) * t.y)));
		/* If it is bigger than |90°| return distance from p to a */
		if (Math.abs(angleAtB) > PI_HALF) {
			final int dx = p.x - b.x;
			final int dy = p.y - b.y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

		/* Check if Point is exactly on the line. */
		if (Double.isNaN(angleAtA)) {
			// || Double.isNaN(angleAtB) // NOTE: Not needed because angleAtA
			// would also be NaN !
			return 0.0f;
		}

		/*
		 * Calculate the geometric distance. |(p-a) x b| / |b|
		 */
		return (float) (Math.abs(com.vstargauge.util.GraphicsPoint
				.crossProduct(
						com.vstargauge.util.GraphicsPoint.difference(p, a), s)) / Math
				.sqrt(((long) s.x * s.x) + ((long) s.y) * s.y));
	}

	/**
	 * @param mMyProjectedLocationMapPoint
	 * @param lastLocation
	 * @return
	 */
	public static int distanceBetween(LatLng pointA, Location pointB) {
		float[] results = new float[1];
		Location.distanceBetween(pointA.latitude, pointA.longitude,
				pointB.getLatitude(), pointB.getLongitude(), results);
		
		
		return Math.round(results[0]);
	}

	// =============================================
	// Private inner Classes
}
