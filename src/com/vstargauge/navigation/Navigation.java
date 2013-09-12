/**
 * 
 */
package com.vstargauge.navigation;

import java.util.ArrayList;

import android.graphics.Point;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.vstargauge.util.Constants;

/**
 * @author PyleC1
 * 
 */
public class Navigation implements Constants {
	// =============================================
	// Private/Protected variables

	private Location lastLocation;
	// private boolean locationUpdated = false;
	private boolean running = true;
	// private volatile GoogleMap mMap;
	private RouteListener mListener = null;
	private boolean ticking = false;
	private volatile byte mRouteStatus = ROUTESTATUS_UNKNOWN;
	private volatile Route mRoute;
	private volatile int mCurrentSearchIndexCount = NavAlgorithm.BASE_SEARCHINDEX_COUNT;
	private volatile LatLng mMyProjectedLocationMapPoint;

	// TODO Add audible command manager
	// TODO Add direction voice command listener

	private int mNextRoutePointIndex = NOT_SET;
	private int mNextTurnPointIndex = NOT_SET;
	// private int mNextTurnPointIndexInRoute = NOT_SET;
	private int mDistanceToNextTurnPoint = NOT_SET;
	private long mDistanceToDestination = NOT_SET;
	private final long mTickDelay = 0;
	// private float mPercentageDone = 0;

	/**
	 * Supposed to hold the Angle of the next turn.<br/>
	 * <code>0°;</code> straight<br/>
	 * <code>+x°;</code> left turn (<code>0 < x <= 180</code>)<br/>
	 * <code>-x°;</code> right turn (<code>0 > x >= -180</code>)
	 */
	private float mTurnAngle = NOT_SET;
	// private boolean[] mWaypointsPassed;
	private Thread mNavRunnerThread;

	// =============================================
	// Statics

	// private static final float OFF_ROUTE_DISTANCE = (float) 45.72;
	public static final byte OFF_ROUTE = 1;
	public static final byte ON_ROUTE = 2;
	public static final byte ROUTESTATUS_UNKNOWN = 4;

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	public interface RouteListener {
		/**
		 * This is called whenever the last location given as a tick is too far
		 * away from the route. Keep in mind that this might be called
		 * frequently. New route requests should be kept to a minimum if you're
		 * using online routing.
		 */
		public void onRouteMissed();
		/**
		 * This is called whenever the user is on route and the location has
		 * changed. May be called after onRouteMissed() if the user has since
		 * come back on route.
		 */
		public void onRouteResumed();
		/**
		 * Called when the user has reached a turn. You should remove old
		 * polylines and update the UI to show the next turn. You should not
		 * call voice guidance here because this is only called when the
		 * location is within 5 meters or so of the turn. You may call {@link
		 * getDistanceToNextTurn()} to determine when to call route guidance
		 * methods.
		 */
		public void onWaypointReached();
		/**
		 * Called when the last waypoint in the directions has been reached.
		 * Perform any cleanup and stop routing services.
		 */
		public void onDestinationReached();
	}

	// =============================================
	// Constructors

	public Navigation() {

	}

	public Navigation(Route route) {
		// mMap = map;
		changeRoute(route);
	}

	public Navigation(Route route, RouteListener listener) {
		// mMap = map;
		changeRoute(route);
		mListener = listener;
	}

	// =============================================
	// Overrides

	// =============================================
	// Methods

	// public synchronized void updateLocation(Location location) {
	// lastLocation = location;
	// locationUpdated = true;
	// }

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Changes the current route to a new one. Should be called before calling
	 * <code>tick()</code> if you didn't supply one with the constructor.
	 * 
	 * @param route
	 *            The new {@link Route} to change to.
	 * @see {@link Navigation.tick(Location)}
	 */
	public void changeRoute(final Route route) {
		mRoute = route;
		// if (mRoute.getRouteLength() > 0) {
		// mWaypointsPassed = new boolean[mRoute.getRouteLength()];
		// } else {
		// mWaypointsPassed = new boolean[0];
		// }

		mNextRoutePointIndex = NOT_SET;
		mNextTurnPointIndex = NOT_SET;
		// mNextTurnPointIndexInRoute = NOT_SET;
		mDistanceToNextTurnPoint = NOT_SET;
		mDistanceToDestination = NOT_SET;
	}

	/**
	 * Gets the length of the current leg rounded off to a meter.
	 * 
	 * @return The current leg length in meters.
	 */
	public int getDistanceBetweenNextAndUpperNextWaypoint() {
		if (mNextTurnPointIndex == NOT_SET) {
			return Integer.MAX_VALUE;
		}

		if (mNextTurnPointIndex >= mRoute.getRouteLength() - 1) {
			return Integer.MAX_VALUE;
		}

		return Math.round(mRoute.getDistance(mNextTurnPointIndex));
	}

	/**
	 * Forces the navigator to call it's RouteListener.offRoute method if set on
	 * the next tick
	 */
	public void forceOffRouteListenerNextTick() {
		mRouteStatus = ROUTESTATUS_UNKNOWN;
	}

	/**
	 * The main function of the class. This was designed to be called on every
	 * location update from whatever location listener you're using. If the
	 * current tick isn't completed when this method is called, it will simply
	 * exit and do nothing. This keeps only one thread modifying the navigator
	 * at a time. It also does nothing if the location has not changed from the
	 * previous location.
	 * 
	 * @param newLocation
	 *            The {@link Location} object that contains the location update.
	 */
	public void tick(final Location newLocation) {
		// We only want to process the tick if:
		if (running && //We're enabled
				!ticking && //We're not in the middle of a tick
				mRoute != null) { // And we have a valid route
			// If the location is null or we haven't moved return
			if (newLocation == null || newLocation.equals(lastLocation)) {
				return;
			}

			// Otherwise...
			if (newLocation != null) {
				// Set so we only do one tick at a time
				ticking = true;
				// Swap over the location
				lastLocation = newLocation;

				// And fire off our worker thread
				mNavRunnerThread = new Thread(new NavRunner(),
						"NavRunner_Thread");
				mNavRunnerThread.start();
			}
		}
	}

	// =============================================
	// Getters/Setters
	
	public LatLng getProjectedLatLng(){
		return this.mMyProjectedLocationMapPoint;
	}
	
	public int getNextTurnPointIndex(){
		return this.mNextRoutePointIndex;
	}
	
	public Thread getNavThread() {
		return mNavRunnerThread;
	}

	public void setRouteListener(RouteListener listener) {
		mListener = listener;
	}

	public boolean isTicking() {
		return ticking;
	}

	public long getTickDelay() {
		return mTickDelay;
	}

	public long getDistanceToDestination() {
		return mDistanceToDestination;
	}

	public int getDistanceToNextTurn() {
		return mDistanceToNextTurnPoint;
	}

	// public synchronized void setNextRoutePointIndex(final int newIndex) {
	// final int oldIndex = mNextRoutePointIndex;
	// mNextRoutePointIndex = newIndex;
	//
	// if (oldIndex == newIndex || mListener == null) {
	// return;
	// }
	//
	// final int routeInstructionCount = mRoute.getRouteLength();
	// // boolean changed = false;
	// boolean targetReached = false;
	//
	// for (int i = 0; i < routeInstructionCount; i++) {
	// if (i == routeInstructionCount - 1) {
	// changed = true;
	// targetReached = true;
	// } else if (!mWaypointsPassed[i]) {
	// changed = true;
	// mWaypointsPassed[i] = true;
	// }
	// }
	// if (changed) {
	//
	// }
	// }

	/**
	 * Provides the angle of the next turn.<br/>
	 * <code>0°;</code> straight<br/>
	 * <code>+x°;</code> left turn (<code>0 < x <= 180</code>)<br/>
	 * <code>-x°;</code> right turn (<code>0 > x >= -180</code>)
	 */
	public float getTurnAngle() {
		return this.mTurnAngle;
	}

	// =============================================
	// Private inner Classes

	protected class NavRunner implements Runnable {

		@Override
		public void run() {
			try {
				int beforeNextIndexRoutePoint = Math.max(0,
						Navigation.this.mNextRoutePointIndex);
				final ArrayList<LatLng> polyline = mRoute
						.getPolyline(mNextRoutePointIndex - 1);

				beforeNextIndexRoutePoint = Math.min(polyline.size() - 1,
						beforeNextIndexRoutePoint);

				final Point myGPSPositionPoint = Util
						.geoPoint2Point(lastLocation);
				final int indexOfClosest = Math.max(0, NavAlgorithm
						.getClosestIndex(
								mRoute.getPolyline(mNextRoutePointIndex - 1),
								beforeNextIndexRoutePoint, myGPSPositionPoint,
								Navigation.this.mCurrentSearchIndexCount));
				if (indexOfClosest == NOT_SET) {
					return;
				}

				/* Calculate our location projected onto the route */
				final int firstIndex = Math.max(0, indexOfClosest - 1);
				final int lastIndex = Math.min(firstIndex + 1,
						polyline.size() - 1);

				mMyProjectedLocationMapPoint = Util.getProjectedLatLng(
						polyline.get(firstIndex), polyline.get(lastIndex),
						lastLocation);

				if (Navigation.this.mMyProjectedLocationMapPoint == null) {
					Navigation.this.mMyProjectedLocationMapPoint = polyline
							.get(indexOfClosest);
				} else {
					final int distanceToRouteInMeters = Util.distanceBetween(
							mMyProjectedLocationMapPoint, lastLocation);

					/* Check to see if we're off the route */
					if (NavAlgorithm.DISTANCE_TO_TOGGLE_OFFROUTE < distanceToRouteInMeters
							- lastLocation.getAccuracy()) {
						/* Increase the number of indices to search in */
						mCurrentSearchIndexCount = Math.min(
								mCurrentSearchIndexCount + 5,
								NavAlgorithm.MAX_SEARCHINDEX_COUNT);

						/* Check if we were on the route before or unset */
						if (mListener != null) {
							if (mRouteStatus == ON_ROUTE
									|| mRouteStatus == ROUTESTATUS_UNKNOWN) {
								mRouteStatus = OFF_ROUTE;
								mListener.onRouteMissed();
							}
						}
					} else {
						/* We're on route */
						/* Set the number of indices to search to a minimum */
						mCurrentSearchIndexCount = Math.max(
								mCurrentSearchIndexCount - 5,
								NavAlgorithm.BASE_SEARCHINDEX_COUNT);
						/* Check if we were on the route before or unset */
						if (mListener != null) {
							if (mRouteStatus == ON_ROUTE
									|| mRouteStatus == ROUTESTATUS_UNKNOWN) {
								mRouteStatus = ON_ROUTE;
								mListener.onRouteResumed();
							}
						}
					}
				}

				/* Calculate distance to next turn */
				mDistanceToNextTurnPoint = Util.distanceBetween(
						mRoute.getEndPoint(mNextRoutePointIndex - 1),
						lastLocation);
				if (mDistanceToNextTurnPoint < DISTANCE_FOR_NEXT_TURN) {
					mNextRoutePointIndex = Math.max(mNextRoutePointIndex + 1,
							mRoute.getRouteLength());
					if (mNextRoutePointIndex == mRoute.getRouteLength()) {
						mListener.onDestinationReached();
					} else {
						mListener.onWaypointReached();
					}
				}

				final int length = mRoute.getRouteLength();
				long remainingLength = mDistanceToNextTurnPoint;
				for (int i = mNextRoutePointIndex; i < length; i++) {
					remainingLength += mRoute.getDistance(i);
				}

				mDistanceToDestination = remainingLength;

			} catch (Exception e) {
				Log.d("VStarGauge.Navigation.NavRunner", e.getMessage());
			}

			ticking = false;
		}
	}
}
