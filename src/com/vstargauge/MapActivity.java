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
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.vstargauge.navigation.Navigation;
import com.vstargauge.navigation.Navigation.RouteListener;
import com.vstargauge.navigation.Route;
import com.vstargauge.navigation.Route.GetRouteCompleteListener;
import com.vstargauge.util.Constants;
//import android.location.LocationListener;

public class MapActivity extends Fragment implements LocationListener, RouteListener, GetRouteCompleteListener, Constants {
	// ========================================================
	// private/protected variables
	
	private Route route;
	private LocationClient mLocationClient;
	private MapFragment mMapFragment;
	private GoogleMap mMap;
	private Location lastLocation;
	private Navigation navHandler;
	
	// ========================================================
	// Statics
	
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000).setFastestInterval(16)
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	
	
	// ========================================================
	// Public variables
	
	
	
	// ========================================================
	// Interfaces
	
	@Override
	public void onLocationChanged(Location location){
		lastLocation = location;
		navHandler.tick(lastLocation);
	}
	
	// ========================================================
	// Constructors

	public MapActivity() { /* Blank c'tor */ }

	// ========================================================
	// Overrides
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.map_fragment, container);
		route = new Route(this.getActivity());
		
		mMapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapFragment);
		mLocationClient.requestLocationUpdates(REQUEST, this);
		
		navHandler = new Navigation();
		navHandler.setRunning(false);
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.map_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.get_directions:
				//TODO Get some directions and start navigation
				return true;
			case R.id.stop_navigation:
				//TODO Pause or resume the current navigation
				return true;
			default:
				//Not our menu item
				return false;
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
	}
	
	@Override
	public void onRouteMissed(){
		
	}
	
	@Override
	public void onRouteResumed(){
		
	}
	
	@Override
	public void onWaypointReached(){
		
	}
	
	@Override
	public void onDestinationReached(){
		
	}
	
	@Override
	public void onGetRouteComplete(int routeStatus){
		AlertDialog.Builder builder;
		
		switch (routeStatus){
			case Route.ROUTE_OK:
				navHandler.changeRoute(route);
				navHandler.setRunning(true);
				navHandler.setRouteListener(this);
				break;
			case Route.NO_ROUTE:
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("No Destination or Origin Specified")
				       .setMessage("Please provide both destination and start points.");
				builder.show();
				break;
			case Route.ROUTE_MALFORMED_JSON:
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Directions Error")
				       .setMessage("The directions were received but were corrupt. Try requesting directions again and check your internet connection.");
				builder.show();
				break;
			case Route.ROUTE_NO_RESPONSE:
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("No Response from Google")
				       .setMessage("No response from Google servers. Check that you can access the internet and try again. Also ensure you are in a region that has access to Google Directions.");
				builder.show();
				break;
			case Route.ROUTE_NOT_FOUND:
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Could Not Calcluate a Route")
				       .setMessage("Google could not calculate a route between your start and end points. Check the address and try again.");
				builder.show();
				break;
			default:
				Log.wtf("onGetRouteComplete", "Route object contained unknown route status.");
		}
	}
	
	// ========================================================
	// Methods
	
	protected void setUpMapIfNeeded(){
		if(mMapFragment == null){
			mMapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapFragment);
		}
		mMap = mMapFragment.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setAllGesturesEnabled(true);
	}
	
	protected void setUpLocationClientIfNeeded(){
		
	}
	
	// ========================================================
	// Private inner classes
}
