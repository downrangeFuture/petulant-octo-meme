package com.vstargauge.navigation;

import java.util.ArrayList;

import android.text.Html;

import com.google.android.gms.maps.model.LatLng;

public class RouteStep {
	public RouteStep() {
	}

	/**
	 * The location of the starting point of this step as a LatLng
	 */
	private LatLng start;
	/**
	 * The location of the end point of the step as a LatLng
	 */
	private LatLng end;
	/**
	 * Distance covered by this step until the next step. The underlying
	 * JSONObject holds 2 values: "value" is the distance in meters and "text"
	 * is the human readable representation of the distance, displayed in units
	 * based on the origin of the directions. IE, origins in the US display this
	 * in feet and miles, while origins in Europe display this in
	 * meters/kilometers. Here we're just using the meters since it's what the
	 * map expects and returns and converting to feet/miles is trivial.
	 */
	private long distance;
	/**
	 * The typical time required to perform this step in seconds.
	 */
	private long duration;
	/**
	 * The decoded polyLine, ready to pass to drawPolyLine
	 */
	private ArrayList<LatLng> polyLine;
	/**
	 * Human readable instructions for the current step. w00t
	 */
	private String instructions;

	// Getters
	public LatLng getStartPoint() {
		return start;
	}

	public LatLng getEndPoint() {
		return end;
	}

	public long getDistance() {
		return distance;
	}

	public long getDuration() {
		return duration;
	}

	public ArrayList<LatLng> getPolyLine() {
		return polyLine;
	}

	public String getInstructions() {
		return instructions;
	}

	// Setters
	public void setInstructions(String instructions) {
		this.instructions = Html.fromHtml(instructions).toString();
	}

	public void setPolyLine(String polyLine) {
		this.polyLine = decodeLine(polyLine);
	}

	public void setStartPoint(LatLng start) {
		this.start = start;
	}

	public void setEndPoint(LatLng end) {
		this.end = end;
	}

	public void setDistance(long distance) {
		this.distance = distance;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * Decodes the Polyline for the this step.
	 * 
	 * @param encoded
	 *            The encoded string to decode
	 * @return An ArrayList of {@link LatLng} that describes the Polyline
	 */
	public static ArrayList<LatLng> decodeLine(String encoded) {
		int len = encoded.length();
		int index = 0;
		int lat = 0;
		int lng = 0;
		ArrayList<LatLng> out = new ArrayList<LatLng>();

		encoded.replace("\\\\", "\\");

		try {
			while (index < len) {
				int b, shift = 0, result = 0;

				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);

				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;

				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);

				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				LatLng p = new LatLng(((double) lat / 1E5),
						((double) lng / 1E5));

				out.add(p);
			}
		} catch (Exception e) {
			out = null;
		}
		return out;
	}
}
