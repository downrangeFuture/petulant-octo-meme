/**
 * 
 */
package com.vstargauge;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.downrangeproductions.vstargauge.R;
import com.vstargauge.navigation.Util;

/**
 * @author PyleC1
 * 
 */
public class ExtrasFragment extends Fragment {
	// =============================================
	// Private/Protected variables

	private TextView mphValue;
	private TextView rpmValue;
	private TextView vdcValue;
	private TextView tripTextExtras;
	private TextView mpgText;
	private TextView lastMPGText;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Values values = (Values) intent.getExtras().getParcelable(
					Util.UPDATE_VALUES);

			final float mph = values.mph;
			String temp = "";
			if(mph == 0){
				temp = "---";
			} else if (mph < 100) {
				temp += " ";
				if (mph < 10) {
					temp += " ";
				}
			}
			temp += String.format("%3.0f", mph);
			mphValue.setText(temp);

			temp = "";
			final float rpm = values.rpm;
			if(rpm == 0){
				temp = "----";
			} else if (rpm < 1000) {
				temp += " ";
				if (rpm < 100) {
					temp += " ";
					if (rpm < 10) {
						temp += " ";
					}
				}
			}
			temp += String.format("%4.0f", rpm);
			rpmValue.setText(temp);

			final float vdc = values.vdc;
			temp = "";
			if (vdc == 0){
				temp = "--.-";
			} else {
				if (vdc < 0) {
					temp += "-";
				}
				if (Math.abs(vdc) < 10) {
					temp += " ";
				}
			}
			temp += String.format("%2.0f", vdc);
			vdcValue.setText(temp);

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

	public ExtrasFragment() {
	}

	// =============================================
	// Overrides

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mReceiver, new IntentFilter(Util.UPDATE_VALUES));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater
				.inflate(R.layout.extras_fragment, container, false);

		mphValue = (TextView) view.findViewById(R.id.mphValue);
		rpmValue = (TextView) view.findViewById(R.id.rpmValue);
		vdcValue = (TextView) view.findViewById(R.id.vdcValue);
		tripTextExtras = (TextView) view.findViewById(R.id.tripTextExtras);
		mpgText = (TextView) view.findViewById(R.id.mpgText);
		lastMPGText = (TextView) view.findViewById(R.id.lastMPGText);

		return view;
	}
	
	@Override
	public void onDestroyView(){
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
	}

	// =============================================
	// Methods

	/**
	 * 
	 */
	protected void updateLocalValues() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		float tempNumber = 0.0f;
		String tempString = "";
		
		tempNumber = prefs.getFloat(Util.TRIP_KEY, 0f);
		if(tempNumber < 100){
			tempString += " ";
			if(tempNumber < 10){
				tempString += " ";
			}
		}
		
		tempString += String.format("%3.1f", tempString);
		tripTextExtras.setText(tempString);
		
		tempString = "";
		tempNumber = prefs.getFloat(Util.MPG_KEY, 0f);
		if(tempNumber < 100){
			tempString += " ";
			if(tempNumber < 10){
				tempString += " ";
			}
		}
		tempString += String.format("%3.1f", tempString);
		mpgText.setText(tempString);
		
		tempString="";
		tempNumber = prefs.getFloat(Util.LAST_MPG_KEY, 0f);
		if(tempNumber < 100){
			tempString += " ";
			if(tempNumber < 10){
				tempString += " ";
			}
		}
		tempString += String.format("%3.1f", tempString);
		lastMPGText.setText(tempString);
	}

	// =============================================
	// Private inner Classes
}
