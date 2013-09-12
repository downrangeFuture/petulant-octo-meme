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
	
	public static final String TACHOMETER_FRAGMENT_TAG = "tachometer_fragment";
	public static final String MAP_FRAGMENT_TAG = "map_fragment";
	public static final String EXTRAS_FRAGMENT_TAG = "extras_fragment";
}
