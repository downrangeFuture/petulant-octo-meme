/**
 * 
 */
package com.downrangeproductions.navigation;

import java.util.Observable;

import android.location.Location;

/**
 * @author PyleC1
 *
 */
public class Navigation extends Observable implements Runnable {
	// =============================================
	// Private/Protected variables
	
	private Location lastLocation;
	private boolean locationUpdated = false;
	private boolean running = true;

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners
	
	// =============================================
	// Constructors

	// =============================================
	// Overrides

	@Override
	public void run(){
		while (running){
			if(locationUpdated){
				locationUpdated = false;
				// TODO do some navigation stuff
			}
		}
	}
	
	// =============================================
	// Methods

	public synchronized void updateLocation(Location location){
		lastLocation = location;
		locationUpdated = true;
	}
	
	public synchronized void setRunning(boolean running){
		this.running = running;
	}
	
	// =============================================
	// Private inner Classes
}
