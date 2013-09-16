package com.vstargauge;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.downrangeproductions.vstargauge.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vstargauge.navigation.DirectionDialog;
import com.vstargauge.navigation.Navigation;
import com.vstargauge.navigation.Navigation.RouteListener;
import com.vstargauge.navigation.Route;
import com.vstargauge.navigation.Route.GetRouteCompleteListener;
import com.vstargauge.navigation.Util;
import com.vstargauge.util.Constants;
//import android.location.LocationListener;

//TODO Setup onSaveInstanceState(Bundle)

public class MapActivity extends Fragment
		implements
			LocationListener,
			ConnectionCallbacks,
			OnConnectionFailedListener,
			RouteListener,
			GetRouteCompleteListener,
			Constants,
			OnCameraChangeListener {
	// ========================================================
	// private/protected variables

	private LocationClient mLocationClient;
	private MapFragment mMapFragment;
	private GoogleMap mMap;
	private Location lastLocation;
	private Navigation navHandler;
	private MenuItem stopNav;
	private int stepIndex = 0;
	private float mZoom = Util.DEFAULT_ZOOM;
	private float mTilt = Util.DEFAULT_TILT;
//	private float mBearing;
	private Polyline mPolyline;

	// ========================================================
	// Statics

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000).setFastestInterval(16)
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	// ========================================================
	// Public variables

	public Route route;

	// ========================================================
	// Interfaces

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;
		if(navHandler.isRunning() == false){
			CameraPosition.Builder builder = new CameraPosition.Builder();
			builder.bearing(Util.NORTH)
			       .target(new LatLng(location.getLatitude(), location.getLongitude()))
				   .tilt(mTilt)
				   .zoom(mZoom);

			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder
					.build()));
			
		}
		navHandler.tick(lastLocation);
	}

	// ========================================================
	// Constructors

	public MapActivity() { /* Blank c'tor */
	}

	// ========================================================
	// Overrides

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		navHandler = new Navigation();

		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(Util.ROUTE)) {
				route = savedInstanceState.getParcelable(Util.ROUTE);
				navHandler.changeRoute(route);
				navHandler.setRunning(true);
			}

			if (savedInstanceState.containsKey(Util.STEP_INDEX)) {
				stepIndex = savedInstanceState.getInt(Util.STEP_INDEX);
				navHandler.setNextStepIndex(stepIndex);
			}
		} else {
			route = new Route(this.getActivity());
			navHandler.setRunning(false);
		}
		route.setOnRouteTaskCompleteListener(this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.map_fragment, null);

		mMapFragment = (MapFragment) getActivity().getFragmentManager()
				.findFragmentById(R.id.mapFragment);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map_menu, menu);

		stopNav = menu.findItem(R.id.stop_navigation);

		if (navHandler.isRunning()) {
			stopNav.setEnabled(true);
			stopNav.setTitle(R.string.stop_nav);
		} else if (route.getRouteLength() <= 0) {
			stopNav.setEnabled(false);
		} else {
			stopNav.setTitle(R.string.start_nav);
			stopNav.setEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.get_directions :
				LatLng location = new LatLng(lastLocation.getLatitude(),
						lastLocation.getLongitude());

				DirectionDialog dialog = DirectionDialog.newInstance(location);
				dialog.show(getFragmentManager(), Util.DIRECTION_DIALOG);
				stopNav.setEnabled(true);
				stopNav.setTitle(R.string.stop_nav);
				return true;
			case R.id.stop_navigation :
				if (item.getTitle() == getActivity().getResources().getString(
						R.string.stop_nav)) {
					navHandler.setRunning(false);
					this.removeRoute();
				}
				return true;
			default :
				// Not our menu item
				return false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
	}

	@Override
	public void onRouteMissed() {

	}

	@Override
	public void onRouteResumed() {
		final LatLng projectedLocation = navHandler.getProjectedLatLng();

		CameraPosition.Builder builder = new CameraPosition.Builder();
		builder.bearing(lastLocation.getBearing()).target(projectedLocation)
				.tilt(mTilt).zoom(mZoom);

		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder
				.build()));

		// Adjust the route line
		final int currStep = navHandler.getNextTurnPointIndex() - 1;
		int closestPoint = navHandler.getIndexOfClosestPolyPoint();

		showRoute(currStep, closestPoint, projectedLocation, mPolyline);
	}

	@Override
	public void onWaypointReached() {

	}

	@Override
	public void onDestinationReached() {

	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if(position.tilt != mTilt)
			mTilt = position.tilt;
		
		if(position.zoom != mZoom)
			mZoom = position.zoom;
	}

	@Override
	public void onGetRouteComplete(int routeStatus) {
		AlertDialog.Builder builder;

		switch (routeStatus) {
			case Route.ROUTE_OK :
				navHandler.changeRoute(route);
				navHandler.setRunning(true);
				navHandler.setRouteListener(this);
				mPolyline = this.startNavigation();
				break;
			case Route.NO_ROUTE :
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("No Destination or Origin Specified")
						.setMessage(
								"Please provide both destination and start points.");
				builder.show();
				break;
			case Route.ROUTE_MALFORMED_JSON :
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Directions Error")
						.setMessage(
								"The directions were received but were corrupt. Try requesting directions again and check your internet connection.");
				builder.show();
				break;
			case Route.ROUTE_NO_RESPONSE :
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("No Response from Google")
						.setMessage(
								"No response from Google servers. Check that you can access the internet and try again. Also ensure you are in a region that has access to Google Directions.");
				builder.show();
				break;
			case Route.ROUTE_NOT_FOUND :
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Could Not Calcluate a Route")
						.setMessage(
								"Google could not calculate a route between your start and end points. Check the address and try again.");
				builder.show();
				break;
			default :
				Log.wtf("onGetRouteComplete",
						"Route object contained unknown route status.");
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
	 * #onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks
	 * #onDisconnected()
	 */
	@Override
	public void onDisconnected() {
		// Do nothing
	}

	// ========================================================
	// Methods

	/**
	 * 
	 */
	private void removeRoute() {
		mMap.clear();
	}
	
	private Polyline startNavigation(){
		PolylineOptions options = new PolylineOptions();
		
		options.addAll(route.getOverallPolyline());
		
		return mMap.addPolyline(options);
	}
	
	/**
	 * @param currStep
	 * @param closestPoint
	 * @param projectedLocation
	 */
	private void showRoute(int currStep, int closestPoint,
			LatLng projectedLocation, Polyline pPolyline) {

		ArrayList<LatLng> routeLine = new ArrayList<LatLng>();
		// First we add our current projected location so that the line starts
		// with our location
		routeLine.add(projectedLocation);

		ArrayList<LatLng> currStepLine = route.getPolyline(currStep);

		// If we don't have a bearing, then we don't know really what's "ahead"
		// or "behind" so just skip this and draw the calculated current point.
		// A line may show up behind us, but what can you do?
		if (lastLocation.hasBearing()) {
			// But if we do, and the closest point is behind us
			if (isClosestPointBehind(new LatLng(lastLocation.getLatitude(),
					lastLocation.getLongitude()), projectedLocation,
					lastLocation.getBearing())) {
				// Then we want to draw the line from where we're at to the next
				// point in the series to avoid having a line appear behind us
				// while we're moving.
				closestPoint++;
			}
		}

		// Add all the points from the closest point onward in the current
		// polyline
		for (int i = closestPoint; i <= currStepLine.size(); i++) {
			routeLine.add(currStepLine.get(i));
		}
		// Then add all the remaining polylines to the end of the route.
		for (int i = currStep + 1; i <= route.getRouteLength(); i++) {
			routeLine.addAll(route.getPolyline(i));
		}
		
		pPolyline.setPoints(routeLine);
	}

	/**
	 * Uses spherical trigonmetery to determine if a point is behind us in a
	 * relative sense. It does this by determining the bearing from the
	 * currentPoint to the closestPoint in true bearing, then calculating if
	 * that true bearing falls within + or - 90 degrees of the provided bearing.
	 * 
	 * @param currentPoint
	 *            The current point in LatLng degrees
	 * @param closestPoint
	 *            The point to determine if it's in front of you
	 * @param bearing
	 *            Your current bearing in True North heading.
	 * @return True if closestPoint is behind you
	 */
	public boolean isClosestPointBehind(LatLng currentPoint,
			LatLng closestPoint, double bearing) {
		double lat1 = (currentPoint.latitude * Math.PI) / 180.0;
		double lat2 = (closestPoint.latitude * Math.PI) / 180.0;
		double dLon = ((closestPoint.longitude - currentPoint.longitude) * Math.PI) / 180.0;

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(lat2) * Math.cos(dLon);
		double brng = Math.atan2(y, x);

		brng = (brng * 180) / Math.PI;
		brng = (brng + 360) % 360;
		/*
		 * To determine if the closest point is in front of us or behind us, we
		 * determined the true initial bearing to the closest point. Now we need
		 * to determine if that point falls within plus or minus 90 degress of
		 * the bearing we're heading.
		 */
		double relPlus = (bearing + 90.0) % 360;
		double relMinus = (bearing - 90.0);
		if (relMinus < 0) {
			relMinus += 360;
		}

		/*
		 * But since a circle is divided into 360 degrees that roll over when
		 * you pass 0 or 360, we need to determine if one of points rolled over.
		 * If one of the points rolled over, the exclusive set (or outer set) is
		 * the set containing the bearings relatively in front of us. However if
		 * we didn't, then the inclusive set (inner set) contains the bearings
		 * in front of us.
		 */
		if (relMinus > relPlus) {
			if (brng >= relMinus || brng <= relPlus)
				return false;
		} else if (brng >= relMinus && brng <= relPlus) {
			return false;
		}

		return true;
	}

	protected void setUpMapIfNeeded() {
		if (mMapFragment == null) {
			mMapFragment = (MapFragment) getActivity().getFragmentManager()
					.findFragmentById(R.id.mapFragment);
		}
		mMap = mMapFragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setAllGesturesEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);

		if (navHandler.isRunning()) {
			mPolyline = this.startNavigation();
		}
	}

	protected void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity()
					.getApplicationContext(), this, this);
		}
	}

	// ========================================================
	// Private inner classes
}
