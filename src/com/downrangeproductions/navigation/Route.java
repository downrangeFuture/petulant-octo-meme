package com.downrangeproductions.navigation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class Route {
	// =======================================================================
	// Private Variables
	// =======================================================================
	private StringBuffer urlString = new StringBuffer();
	private RouteStep[] route;
	private OnGetRouteCompleteListener mListener = null;

	private double fromLat = -999.0;
	private double fromLong = -999.0;
	private double toLong = -999.0;
	private double toLat = -999.0;
	private String destination = "";
	private String origin = "";
	private Context mContext;

	protected int routeStatus = NO_ROUTE;

	// =======================================================================
	// Publics and Statics
	// =======================================================================
	public static final int ROUTE_OK = 0x01;
	public static final int ROUTE_NOT_FOUND = 0x02;
	public static final int ROUTE_NO_RESPONSE = 0x03;
	public static final int ROUTE_MALFORMED_JSON = 0x04;
	public static final int NO_ROUTE = 0x05;

	// =======================================================================
	// Interfaces
	// =======================================================================

	/**
	 * Listener to notify interested parties that the directions are retrieved
	 * and parsed.
	 * 
	 */
	public interface OnGetRouteCompleteListener {
		public void onGetRouteComplete(int routeStatus);
	}

	// =======================================================================
	// Ctors
	// =======================================================================

	public Route(Context context) {

	}

	public Route(Context context, double fromLat, double fromLong, String destination) {
		mContext = context;
		this.fromLat = fromLat;
		this.fromLong = fromLong;
		this.destination = destination;
	}

	public Route(Context context, String origin, String destination) {
		mContext = context;
		this.origin = origin;
		this.destination = destination;
	}

	public Route(Context context, String origin, double toLat, double toLong) {
		mContext = context;
		this.origin = origin;
		this.toLat = toLat;
		this.toLong = toLong;
	}

	public Route(Context context, double fromLat, double fromLong, double toLat, double toLong) {
		mContext = context;
		this.fromLat = fromLat;
		this.fromLong = fromLong;
		this.toLat = toLat;
		this.toLong = toLong;
	}

	public Route(Context context, LatLng fromLatLng, String destination,
			Route.OnGetRouteCompleteListener listener) {
		mContext = context;
		this.fromLat = fromLatLng.latitude;
		this.fromLong = fromLatLng.longitude;
		this.destination = destination;
		mListener = listener;
	}

	// =======================================================================
	// Overrides
	// =======================================================================

	// =======================================================================
	// Methods
	// =======================================================================

	/**
	 * Starts the async task that gets the route. Must be called and the async
	 * task completed before anything is done with the route.
	 */
	public void getRoute() {
		try{
			getUrl();
		} catch (DirectionsUrlException e){
			Toast toast = Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG);
			toast.show();
			
			urlString = new StringBuffer();
			
			return;
		}
	
		new RouteTask().execute(urlString.toString());		
	}

	protected void getUrl() throws DirectionsUrlException {
		urlString
				.append("https://maps.googleapis.com/maps/api/directions/json?origin=");

		if (fromLat <= 85.0 && fromLat >= -85.0) {
			urlString.append(fromLat);
			if(fromLong > 180.0 || fromLong < -180.0) {
				throw new DirectionsUrlException("Origin Lat/Long out of bounds!");
			}
			urlString.append("," + fromLong);
		} else {
			if (origin != "") {
				urlString.append(origin);
			} else {
				throw new DirectionsUrlException("No origin specified!");
			}
		}

		urlString.append("&destination=");

		if (toLat <= 85.0 && toLat >= -85.0) {
			urlString.append(toLat);
			if(toLong > 180.0 || toLong < -180.0) {
				throw new DirectionsUrlException("Destination Lat/Long out of bounds!");
			}
			urlString.append("," + toLong);
		} else {
			if (destination != "") {
				urlString.append(destination.replace(" ", "_"));
			} else {
				throw new DirectionsUrlException("No destination specified!");
			}
		}

		urlString.append("&sensor=true&mode=driving&avoid=tolls");
	}

	// =======================================================================
	// Getters and Setters
	// =======================================================================

	public LatLng getStartPoint(int stepNum) {
		if(stepNum < 0 || stepNum > route.length - 1)
			return null;
		else
			return route[stepNum].getStartPoint();
	}

	public LatLng getEndPoint(int stepNum) {
		if(stepNum < 0 || stepNum > route.length - 1)
			return null;
		else
			return route[stepNum].getEndPoint();
	}

	public long getDistance(int stepNum) {
		if(stepNum < 0 || stepNum > route.length - 1)
			return -1;
		else
			return route[stepNum].getDistance();
	}

	public long getDuration(int stepNum) {
		if(stepNum < 0 || stepNum > route.length - 1)
			return -1;
		else
			return route[stepNum].getDuration();
	}

	public ArrayList<LatLng> getPolyline(int stepNum) {
		if(stepNum < 0 || stepNum > route.length - 1)
			return null;
		else
			return route[stepNum].getPolyLine();
	}

	public String getInstructions(int stepNum) {
		if(stepNum < 0 || stepNum > route.length - 1)
			return null;
		else
			return route[stepNum].getInstructions();
	}
	
	public int getRouteLength(){
		return route.length;
	}

	public void setOnRouteTaskCompleteListener(
			Route.OnGetRouteCompleteListener listener) {
		mListener = listener;
	}

	// =======================================================================
	// Private inner classes
	// =======================================================================

	protected class RouteTask extends AsyncTask<String, Void, RouteStep[]> {
		/**
		 * 
		 * @param url
		 * @return
		 * @throws JSONException
		 */
		protected JSONObject getDirections(String url) throws JSONException {
			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpPost httppost = new HttpPost(url);
			JSONObject jObject;

			// httppost.setHeader(name, value)

			InputStream is = null;
			String result = null;
			try {
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				is = entity.getContent();
				// JSON is UTF-8 by default
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				result = sb.toString();
			} catch (Exception e) { // squash
				Log.d(this.toString(), e.getMessage());
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (Exception squish) {
					Log.d(this.toString(), squish.getMessage());
				}
			}

			jObject = new JSONObject(result);

			return jObject;
		}

		/**
		 * Parses the directions it received earlier into a DirectionsStep
		 * array. If there is a JSONException thrown during parsing, the array
		 * may be malformed or incomplete.
		 * 
		 * @return The DirectionsStep array or null if the JSONObject it
		 *         received was null
		 */
		public RouteStep[] parseDirections(JSONObject directionsJSON) {
			RouteStep[] directions = null;
			try {
				// First we want to make sure there are actually directions to
				// parse
				String status = directionsJSON.getString("status");
				if (status.contentEquals("OK")) {
					// If so we'll grab the steps array

					JSONArray steps = directionsJSON.getJSONArray("routes")
							.getJSONObject(0).getJSONArray("legs")
							.getJSONObject(0).getJSONArray("steps");

					// Iterator
					int i = 0;

					// How many steps in this leg
					int numSteps = steps.length();
					// Make our array of DirectionStep objects
					directions = new RouteStep[numSteps];

					/*
					 * So we'll iterate through each step in the directions and
					 * store the relevant information
					 */
					while (i < numSteps) {
						directions[i] = new RouteStep();
						JSONObject temp = steps.getJSONObject(i);
						LatLng latLngTemp;

						// Get the step distance in meters
						directions[i].setDistance((long) temp.getJSONObject(
								"distance").getInt("value"));
						// Get the step duration in seconds
						directions[i].setDuration((long) temp.getJSONObject(
								"duration").getInt("value"));
						// Get the start location
						latLngTemp = new LatLng(temp.getJSONObject(
								"start_location").getDouble("lat"), temp
								.getJSONObject("start_location").getDouble(
										"lng"));
						directions[i].setStartPoint(latLngTemp);
						// Get the step end location
						latLngTemp = new LatLng(temp.getJSONObject(
								"end_location").getDouble("lat"), temp
								.getJSONObject("end_location").getDouble("lng"));
						directions[i].setEndPoint(latLngTemp);
						// Get and decode the polyline
						directions[i].setPolyLine(temp
								.getJSONObject("polyline").getString("points"));
						// Get and decode the instructions
						directions[i].setInstructions(temp
								.getString("html_instructions"));

						i++;
					}
				} else {
					Log.e(this.toString(),
							"Directions status was not \"OK\" and returned a result of "
									+ directionsJSON.get("status"));
					directions = null;
					routeStatus = ROUTE_NOT_FOUND;
				}
			} catch (JSONException e) {
				// squish
				e.printStackTrace();

				Log.e(this.toString(),
						"JSON Parsing of directions failed with result: "
								+ e.getMessage());
				directions = null;
				routeStatus = ROUTE_MALFORMED_JSON;
			}

			if (directions != null)
				routeStatus = ROUTE_OK;

			return directions;
		}

		@Override
		protected RouteStep[] doInBackground(String... url) {
			try {
				JSONObject temp;
				temp = getDirections(url[0]);
				return parseDirections(temp);
			} catch (Exception e) {
				// squish
				Log.d(this.toString(), e.getMessage());
				e.printStackTrace();

				routeStatus = ROUTE_NOT_FOUND;
				// mListener.onGetRouteComplete(routeStatus);

				return null;
			}
		}

		protected void onPostExecute(RouteStep[] result) {
			route = result;

			if (mListener != null) {
				mListener.onGetRouteComplete(routeStatus);
			}

			return;
		}
	}
}
