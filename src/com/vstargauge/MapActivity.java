package com.vstargauge;

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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.vstargauge.navigation.DirectionDialog;
import com.vstargauge.navigation.Navigation;
import com.vstargauge.navigation.Navigation.RouteListener;
import com.vstargauge.navigation.Route;
import com.vstargauge.navigation.Route.GetRouteCompleteListener;
import com.vstargauge.navigation.Util;
import com.vstargauge.util.Constants;
//import android.location.LocationListener;

public class MapActivity extends Fragment
		implements
			LocationListener,
			ConnectionCallbacks,
			OnConnectionFailedListener,
			RouteListener,
			GetRouteCompleteListener,
			Constants {
	// ========================================================
	// private/protected variables

	private LocationClient mLocationClient;
	private MapFragment mMapFragment;
	private GoogleMap mMap;
	private Location lastLocation;
	private Navigation navHandler;
	private MenuItem stopNav;
	private int stepIndex = 0;

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
		setHasOptionsMenu(true);

		navHandler = new Navigation();
		if(savedInstanceState.containsKey(Util.ROUTE)){
			route = savedInstanceState.getParcelable(Util.ROUTE);
			navHandler.changeRoute(route);
			navHandler.setRunning(true);
		} else {
			route = new Route(this.getActivity());
			navHandler.setRunning(false);
		}
		route.setOnRouteTaskCompleteListener(this);
		
		if(savedInstanceState.containsKey(Util.STEP_INDEX)){
			stepIndex = savedInstanceState.getInt(Util.STEP_INDEX);
			navHandler.setNextStepIndex(stepIndex);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.map_fragment, container);

		mMapFragment = (MapFragment) getActivity().getFragmentManager()
				.findFragmentById(R.id.mapFragment);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map_menu, menu);
		
		stopNav = (MenuItem) getActivity().findViewById(R.id.stop_navigation);
		if(navHandler.isRunning()){
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
				LatLng location = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
				
				DirectionDialog dialog = DirectionDialog.newInstance(location);
				dialog.show(getFragmentManager(), Util.DIRECTION_DIALOG);
				stopNav.setEnabled(true);
				stopNav.setTitle(R.string.stop_nav);
				return true;
			case R.id.stop_navigation :
				if (item.getTitle() == getActivity().getResources().getString(R.string.stop_nav)){
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
		
	}

	@Override
	public void onWaypointReached() {

	}

	@Override
	public void onDestinationReached() {

	}

	@Override
	public void onGetRouteComplete(int routeStatus) {
		AlertDialog.Builder builder;

		switch (routeStatus) {
			case Route.ROUTE_OK :
				navHandler.changeRoute(route);
				navHandler.setRunning(true);
				navHandler.setRouteListener(this);
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
		//Do nothing
	}

	// ========================================================
	// Methods
	
	/**
	 * 
	 */
	private void removeRoute() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 */
	private void showRoute() {
		
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
		
		if (navHandler.isRunning()){
			this.showRoute();
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
