/**
 * 
 */
package com.vstargauge;

import views.GaugeView;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.downrangeproductions.vstargauge.R;
import com.vstargauge.navigation.Util;

/**
 * @author PyleC1
 *
 */
public class TachFragment extends Fragment {
	// =============================================
	// Private/Protected variables
	
	private GaugeView mGauge;
	
	private float mSpeed = 0;
	private float mTach = 0;
	private float mVDC = 0;
	private float mTrip = 0;
	private float mOdometer = 0;
	private boolean mTurnSignal = false;
	private boolean mNeutral = false;
	private int updateCount = 0;
	
	private long mPreviousTimeStamp = 0;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			Values values = (Values) intent.getExtras().getParcelable(Util.UPDATE_VALUES);
			
			mTurnSignal = values.turnSignal;
			mNeutral    = values.neutral;
			mSpeed      = values.mph;
			mTach       = values.rpm;
			
			updateLocalValues();
		}
	};

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	// =============================================
	// Constructors

	public TachFragment(){}
	

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		mTrip = prefs.getFloat(Util.TRIP_KEY, 0f);
		mOdometer = prefs.getFloat(Util.ODOMETER_KEY, 0f);
		
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mReceiver, new IntentFilter(Util.UPDATE_VALUES));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.gauge_view_layout, container, false);
		mGauge = (GaugeView) v.findViewById(R.id.tachometer);
		
		updateLocalValues();
				
		return v;
	}
	
	// =============================================
	// Overrides
	
	@Override
	public void onDestroyView(){
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
		
		super.onDestroyView();
	}
	
	// =============================================
	// Methods
	
	/**
	 * 
	 */
	protected void updateLocalValues() {
		long elapsedTime = 0;
		long currentTime = 0;
		
		if(mPreviousTimeStamp == 0){
			mPreviousTimeStamp = System.nanoTime();
		} else {
			currentTime = System.nanoTime();
			elapsedTime += currentTime - elapsedTime;
		}
		
		final float speedScalar = (float) (((1 / 60) / 60) / 1e9);
		
		mOdometer += speedScalar * mSpeed * elapsedTime;
		mTrip += speedScalar * mSpeed * elapsedTime;
		
		if(updateCount > 100){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			Editor edit = prefs.edit();
			
			edit.putFloat(Util.ODOMETER_KEY, mOdometer);
			edit.putFloat(Util.TRIP_KEY, mTrip);
			
			edit.commit();
			
			updateCount = 0;
		} else {
			updateCount++;
		}
		
		mGauge.updateSpeed(mSpeed);
		mGauge.updateTach(mTach);
		mGauge.updateVDC(mVDC);
		mGauge.updateOdometer(mOdometer);
		mGauge.updateTrip(mTrip);
		mGauge.neutralLit(mNeutral);
		mGauge.turnSignalLit(mTurnSignal);
		
		mPreviousTimeStamp = currentTime;
	}
	
	// =============================================
	// Private inner Classes
}
