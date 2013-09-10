package com.downrangeproductions.vstargauge;

import android.app.Fragment;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.downrangeproductions.navigation.Route;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapFragment;

public class MapActivity extends Fragment implements LocationListener {
	// ========================================================
	// private/protected variables
	
	private Route route;
	private LocationClient mLocationClient;
	private MapFragment mMap;
	private Location lastLocation;
	
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
	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.map_fragment);
//		
//	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.map_fragment, container);
		route = new Route(this.getActivity());
		
		mMap = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapFragment);
		mLocationClient.requestLocationUpdates(REQUEST, this);
		
		return view;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
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
