/**
 * 
 */
package com.vstargauge.util;

import android.graphics.Point;

/**
 * @author PyleC1
 *
 */
public class GraphicsPoint extends Point {
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

	public GraphicsPoint() {
	}

	public GraphicsPoint(final int longitude, final int latitude) {
		this.x = longitude;
		this.y = latitude;
	}
	
	// =============================================
	// Overrides

	// =============================================
	// Methods

	public static float dotProduct(final android.graphics.Point a, final android.graphics.Point b) {
		return (a.x * b.x + a.y * b.y);
	}

	public static float crossProduct(final android.graphics.Point a, final android.graphics.Point b) {
		return (a.x * b.y - a.y * b.x);
	}

	public static GraphicsPoint difference(final android.graphics.Point a, final android.graphics.Point b) {
		return new GraphicsPoint(a.x-b.x, a.y-b.y);
	}
	
	// =============================================
	// Private inner Classes
}
