package com.vstargauge;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.downrangeproductions.vstargauge.R;
import com.vstargauge.navigation.Util;
import com.vstargauge.util.Constants;

public class MainActivity extends IOIOActivity
		implements
			ActionBar.OnNavigationListener,
			Constants {

	public static float rotationsPerMile;
	public static final String UPDATE_INTENT = "perform_values_update";
	public static final String UPDATE_VALUES = "update_values";

	protected int navigationItemState = Util.MAIN;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[]{
								getString(R.string.title_section1),
								getString(R.string.title_section2),
								getString(R.string.title_section3)}), this);

		if (savedInstanceState != null) {
			if (savedInstanceState
					.containsKey(MainActivity.STATE_SELECTED_NAVIGATION_ITEM)) {
				navigationItemState = savedInstanceState
						.getInt(MainActivity.STATE_SELECTED_NAVIGATION_ITEM);
			}
		}

//		switchFragment(this.navigationItemState, true);

		rotationsPerMile = calcRotationsPerMile(170F, 80f, 15f);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			final int navItem = savedInstanceState
					.getInt(STATE_SELECTED_NAVIGATION_ITEM);
			getActionBar().setSelectedNavigationItem(navItem);
			navigationItemState = navItem;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings :
				// TODO swap settings into fragment container
				return true;
			case R.id.reset_trip :
				// TODO reset trip meter
				return true;
			case R.id.calculate_mpg :
				// TODO implement calculate mpg dialog
				return true;
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Switches the currently displayed fragment in R.id.container
	 * 
	 * @param fragment
	 *            The index of the fragment to change. Values are in
	 *            Util.Constants
	 * @param forceReplace
	 *            If true, this will replace the fragment even if the requested
	 *            fragment is the one currently being displayed. Useful for
	 *            screen orientation changes and such.
	 * @return True if the fragment was switched.
	 */
	public boolean switchFragment(int fragment, boolean forceReplace) {		
		Fragment frag = null;
		FragmentTransaction transaction = null;
		switch (fragment) {
			case Util.MAIN : // Main
				if (this.navigationItemState == Util.MAIN
						&& forceReplace == false) {
					return true;
				} else {
					frag = new TachFragment();
					transaction = getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, frag,
							Util.TACHOMETER_FRAGMENT_TAG);
					transaction.commit();
					this.navigationItemState = Util.MAP;
					return true;
				}
			case Util.MAP : // Map
				if (this.navigationItemState == Util.MAP
						&& forceReplace == false) {
					return true;
				} else {
					frag = new MapActivity();
					transaction = getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, frag,
							Util.MAP_FRAGMENT_TAG);
					transaction.commit();
					this.navigationItemState = Util.MAP;
					return true;
				}
			case Util.EXTRAS : // Extras
				if (this.navigationItemState == Util.EXTRAS
						&& forceReplace == false) {
					return true;
				} else {
					frag = new ExtrasFragment();
					transaction = getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, frag,
							Util.EXTRAS_FRAGMENT_TAG);
					transaction.commit();
					this.navigationItemState = Util.EXTRAS;
					return true;
				}
		}
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.

		return switchFragment(position, false);

	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));

			return rootView;
		}
	}

	public class Looper extends BaseIOIOLooper {
		// public static final int DEFAULT_FREQ = 10000;
		public static final int SPEEDO_PIN = 27;
		public static final int TACH_PIN = 28;
		public static final int NEUTRAL_PIN = 29;
		public static final int THROTTLE_POSITION_PIN = 32;
		public static final int TURN_SIGNAL_PIN = 34;
		public static final int VDC_READ_PIN = 37;

		private PulseInput speedSignal;
		private PulseInput tachSignal;
		private DigitalInput neutralSignal;
		private AnalogInput throttleSignal;
		private DigitalInput turnSignal;
		private AnalogInput vdcSignal;

		private long lastNanoTime, elapsedTime = 0;

		private float speed, tach, throttlePosition, vdc;
		private boolean neutral, blinker;

		@Override
		public void setup() throws ConnectionLostException {
			speedSignal = ioio_.openPulseInput(
					new DigitalInput.Spec(SPEEDO_PIN),
					PulseInput.ClockRate.RATE_62KHz, PulseMode.FREQ, false);
			tachSignal = ioio_.openPulseInput(new DigitalInput.Spec(TACH_PIN),
					PulseInput.ClockRate.RATE_62KHz, PulseMode.FREQ, false);
			neutralSignal = ioio_.openDigitalInput(NEUTRAL_PIN);
			throttleSignal = ioio_.openAnalogInput(THROTTLE_POSITION_PIN);
			turnSignal = ioio_.openDigitalInput(TURN_SIGNAL_PIN);
			vdcSignal = ioio_.openAnalogInput(VDC_READ_PIN);
			// vdcReadable = true;

			lastNanoTime = System.nanoTime();
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			final long currentNanoTime = System.nanoTime();
			elapsedTime += (currentNanoTime - lastNanoTime);

			if (elapsedTime > Util.ONE_TENTH_SECOND_IN_NANOS) {
				try {
					speed = speedSignal.getFrequency();
					tach = tachSignal.getFrequency();
					throttlePosition = throttleSignal.read();
					// if(vdcReadable)
					vdc = vdcSignal.read();
					neutral = neutralSignal.read();
					blinker = turnSignal.read();
				} catch (ConnectionLostException e) {
					throw e;
				} catch (InterruptedException e) {
					throw e;
				}

				processInputs(speed, tach, throttlePosition, vdc, neutral,
						blinker);

				elapsedTime = 0l;
			} else {
				try {
					this.wait(Util.ONE_TENTH_SECOND_IN_MILLIS);
				} catch (InterruptedException e) {
					// just start processing again
				}
			}

			lastNanoTime = currentNanoTime;
		}

	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	public void ouch() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getApplicationContext());
		builder.setTitle(R.string.ouch_title);
		builder.setMessage(R.string.ouch);
		builder.setPositiveButton(R.string.retry,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						overVoltAck = true;
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void vdcHigh() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getApplicationContext());
		builder.setTitle(R.string.vdc_high_title);
		builder.setMessage(R.string.vdc_high);
		builder.setPositiveButton(R.string.OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						overVoltAck = true;
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// May be set as a preference later
	public static final float MAX_VDC = 14.25f;
	// May be set as a preference later
	public static final int MIN_VDC = 12;
	// May be set as a preference later
	public static final int OVER_VDC = 17;
	// Number is irrational. Stored this way to preserve data
	public static final float VOLTS_SCALAR = (1.0f / 3.3f);
	// Done this way if MAX_VDC becomes a preference
	public static final float MAX_VDC_SCALED = (VOLTS_SCALAR * (MAX_VDC * 1 / (10 + 1)));
	// Done this way if MIN_VDC becomes a preference
	public static final float MIN_VDC_SCALED = (VOLTS_SCALAR * (MIN_VDC * 1 / (10 + 1)));
	// Done this way if OVER_VDC becomes a preference
	public static final float OVER_VDC_SCALED = (VOLTS_SCALAR * (OVER_VDC * 1 / (10 + 1)));
	// Multiply scalar by this to get read world VDC
	public static final float PERCENT_TO_VDC = 36.3f / (VOLTS_SCALAR * (36.3f * 1 / (10 + 1)));

	// If we've already shown the current over volt condition to the user,
	// we don't want to keep bugging them.
	public boolean overVoltAck = false;

	/**
	 * Takes the inputs from the IOIOLooper and sends updates where needed.
	 * 
	 * @param speed
	 *            Raw speed frequency from the speed sensor
	 * @param tach
	 *            Raw tach frequency from the crankshaft sensor
	 * @param throttlePosition
	 *            A scalar from 0 to 1 representing the 0-3.3VDC on the pin from
	 *            the throttle position sensor. Will be run through a voltage
	 *            divider.
	 * @param vdc
	 *            A scalar from 0 to 1 representing the 0-3.3VDC on the pin.
	 *            Will be run through a voltage divider from the battery.
	 * @param neutral
	 *            Boolean representation of neutral light state
	 * @param blinker
	 *            Boolean representation of blinker light state
	 */
	public void processInputs(float speed, float tach, float throttlePosition,
			float vdc, boolean neutral, boolean blinker) {

		Values values = new Values();

		float mph = (((speed * 3600f) // Convert transmission revs per sec to
										// revs per hour
		/ 2.875f) // Divide by final drive ratio to find wheel revs per hour
		/ rotationsPerMile); // Divide RPH by the number of rotations to make a
								// mile
								// Giving us MPH

		values.mph = mph;

		if (vdc > MAX_VDC_SCALED && !overVoltAck) { // Over 14vdc
			if (vdc > OVER_VDC_SCALED) { // Over 17vdc
				ouch();
			} else {
				vdcHigh();
				// TODO make VDC flash
			}
		} else {
			overVoltAck = false;
			// TODO make VDC not flash
		}

		values.vdc = vdc * PERCENT_TO_VDC;
		values.rpm = tach * 60f;
		// Throttle position is a percentage of the current battery vdc.
		// So we divide the current voltage from the throttle position sensor
		// by the current battery voltage to determine the decimal percentage
		// of the current throttle position.
		values.throttlePos = throttlePosition / vdc;
		values.neutral = neutral;
		values.turnSignal = blinker;

		Intent intent = new Intent(UPDATE_INTENT);
		intent.putExtra(UPDATE_VALUES, values);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(intent);
	}

	/**
	 * Calculates the number of rotations the tire would make per mile based on
	 * its size
	 * 
	 * @param width
	 *            The width or first number of the tire size
	 * @param profile
	 *            The profile in percentage, or the second number of the tire
	 *            size
	 * @param wheelDiameter
	 *            The wheel diameter, or the third number of the tire size
	 * @return The number of revolutions per mile
	 */
	protected float calcRotationsPerMile(float width, float profile,
			float wheelDiameter) {
		double circumference;
		double fullDiameter;

		// The full diameter of the wheel is the rim diameter + the height
		// Strangely, the diameter of the wheel is in inches, but the tire
		// width is in mm and the height is a percentage of the width
		fullDiameter = wheelDiameter
		// The height of the tire is the width times the profile percent
		// The profile is in percent so we convert to decimal
				+ (((width * (profile * 0.01))
				// Double it because the profile is only for one side
				* 2)
				// Convert mm to inches
				/ 25.4);
		// Find the circumference in inches
		circumference = fullDiameter * Math.PI;

		// Then we'll divide the number of inches in a mile by our
		// circumference to determine the number of rotations the wheel will
		// make in a mile
		return (float) (63360D / circumference);
	}
}
