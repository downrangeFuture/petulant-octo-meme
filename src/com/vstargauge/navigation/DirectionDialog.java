/**
 * 
 */
package com.vstargauge.navigation;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.downrangeproductions.vstargauge.R;
import com.google.android.gms.maps.model.LatLng;
import com.vstargauge.MapActivity;
import com.vstargauge.util.Constants;

/**
 * @author PyleC1
 *
 */
public class DirectionDialog extends DialogFragment implements OnClickListener, Constants {


	// =============================================
	// Private/Protected variables
	
//	private Route mRoute = null;
	private EditText startAddress;
	private EditText destAddress;
	private CheckBox useCurrentLocation;
	private Button okButton;
	private Button cancelButton;
	private LatLng location = null;

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	// =============================================
	// Constructors
	
	// Default Blank C'tor
//	private DirectionDialog() {}
	
	public static DirectionDialog newInstance(LatLng location){
		DirectionDialog frag = new DirectionDialog();
		Bundle args = new Bundle();
		args.putParcelable(LOCATION, location);
		frag.setArguments(args);
		return frag;
	}

	// =============================================
	// Overrides
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.navigation_dialog, container);
		
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey(Util.LOCATION)){
				location = savedInstanceState.getParcelable(LOCATION);
			}
		}
		
		destAddress = (EditText) view.findViewById(R.id.destinationAddress);
		startAddress = (EditText) view.findViewById(R.id.startAddress);
		useCurrentLocation = (CheckBox) view.findViewById(R.id.useCurrentLocation);
		okButton = (Button) view.findViewById(R.id.okButton);
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		
		if (location == null){
			useCurrentLocation.setChecked(false);
			useCurrentLocation.setEnabled(false);
			startAddress.setEnabled(true);
			startAddress.setHint("Start Address");
		}
		
		return view;
	}
	
	@Override
	public void onClick(View v){
		switch(v.getId()){
			case R.id.okButton:
				MapActivity map = (MapActivity) getActivity().getFragmentManager().findFragmentByTag(Util.MAP_FRAGMENT_TAG);
				if(map == null){
					Toast.makeText(getActivity(), "Map fragment not available?", Toast.LENGTH_LONG).show();
					this.dismiss();
				} else {
					if(useCurrentLocation.isChecked() == false){
						map.route.setOrigin(startAddress.getText().toString());
					} else {
						map.route.setFromLat(location.latitude);
						map.route.setFromLong(location.longitude);
					}
					
					map.route.setDestination(destAddress.getText().toString());
					map.route.getRoute();
					this.dismiss();
				}
				break;
			case R.id.cancelButton:
				this.dismiss();
				break;
		};
	}
	
	// =============================================
	// Methods
	
	// =============================================
	// Getters/Setters
	
	public void setLocation(LatLng location){
		this.location = location;
		//If the new location is not equal to null, set the check box to checked and enabled
		useCurrentLocation.setChecked(this.location != null);
		useCurrentLocation.setEnabled(this.location != null);
	}

	// =============================================
	// Private inner Classes
}
