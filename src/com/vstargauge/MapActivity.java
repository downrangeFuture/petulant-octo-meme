package com.vstargauge;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.MapFragment;
import com.vstargauge.navigation.Navigation;
import com.vstargauge.navigation.Navigation.RouteListener;
import com.vstargauge.navigation.Route;
//import android.location.LocationListener;

public class MapActivity extends Fragment implements LocationListener, RouteListener {
	// ========================================================
	// private/protected variables
	
	private Route route;
	private LocationClient mLocationClient;
	private MapFragment mMap;
	private Location lastLocation;
	private Navigation navHandler;
	
	// ========================================================
	// Statics
	
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000).setFastestInterval(16)
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	private static final float METERS_CONVERSION_FACTOR = 0.000621371192F;
	
	
	// ========================================================
	// Public variables
	
	
	
	// ========================================================
	// Interfaces
	
	@Override
	public void onLocationChanged(Location location){
		lastLocation = location;
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
		
		mMap = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapFragment);
		mLocationClient.requestLocationUpdates(REQUEST, this);
		
		navHandler = new Navigation();
		
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
	
	// ========================================================
	// Methods
	
	protected void setUpMapIfNeeded(){
		
	}
	
	protected void setUpLocationClientIfNeeded(){
		
	}
	
	// ========================================================
	// Private inner classes
}
