/**
 * 
 */
package com.vstargauge;

import views.TachView;

import com.downrangeproductions.vstargauge.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author PyleC1
 *
 */
public class TachFragment extends Fragment {
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

	public TachFragment(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		
		return inflater.inflate(R.layout.tach_fragment, container, false);
	}
	
	// =============================================
	// Overrides

	// =============================================
	// Methods
	
	// =============================================
	// Private inner Classes
}
