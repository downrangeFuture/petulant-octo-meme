/**
 * 
 */
package com.vstargauge.util;

/**
 * @author PyleC1
 *
 */
public interface Constants {

	public static final int NOT_SET = 0x80000000;
	public static final double PI_HALF = Math.PI / 2;
	public static final long DISTANCE_FOR_NEXT_TURN = 5;
	public static final float METERS_CONVERSION_FACTOR = 0.000621371192F;
	
	public static final int MAIN   = 0;
	public static final int MAP    = 1;
	public static final int EXTRAS = 2;
	
	public static final float DEFAULT_ZOOM = 18f;
	public static final float DEFAULT_TILT = 45f;
	public static final float NORTH = 0f;
	public static final float EAST = 90f;
	public static final float SOUTH = 180f;
	public static final float WEST = 270f;
	public static final float GESTURE_THRESHOLD_DIP = 16f;
	
	public static final long ONE_TENTH_SECOND_IN_NANOS = (long) 1e8;
	public static final long ONE_TENTH_SECOND_IN_MILLIS = 1000l;
	
	// ===========================================================
	// Tags
	
	public static final String TACHOMETER_FRAGMENT_TAG = "tachometer_fragment";
	public static final String MAP_FRAGMENT_TAG = "map_fragment";
	public static final String EXTRAS_FRAGMENT_TAG = "extras_fragment";
	public static final String ROUTE = "route";
	public static final String LOCATION = "location";
	public static final String DIRECTION_DIALOG = "direction_dialog";
	public static final String STEP_INDEX = "step_index";
	public static final String NAV_ITEM_STATE = "nav_item_state";
	public static final String UPDATE_INTENT = "perform_values_update";
	public static final String UPDATE_VALUES = "update_values";
	
	// ============================================================
	// Settings Keys
	
	public static final String PREFERENCES = "prefs";
	public static final String TRIP_KEY = "trip";
	public static final String LOGO_COLOR_KEY = "logo_color";
	public static final String UNITS_KEY = "units";
	public static final String ODOMETER_KEY = "odometer";
	public static final String MPG_KEY = "mpg_key";
	public static final String LAST_MPG_KEY = "last_mpg_key";
}
