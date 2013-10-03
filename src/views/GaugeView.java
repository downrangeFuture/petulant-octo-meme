/**
 * 
 */
package views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.downrangeproductions.vstargauge.R;

/**
 * @author PyleC1
 *
 */
public class GaugeView extends RelativeLayout {

	// =============================================
	// Private/Protected variables
	
//	private float mMinSpeedAngle, mMaxSpeedAngle, mMinTachAngle, mMaxTachAngle;
	private Drawable mBackground = null;
	private ImageView vBackground = null;
	private NeedleView vSpeedNeedle = null;
	private NeedleView vTachNeedle = null;
	private ImageView vNeutral = null;
	private ImageView vTurnSignal = null;
	private TextView vOdometerValue = null;
	private TextView vTripValue = null;
	private TextView vVDC = null;
	private Context mContext;

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	// =============================================
	// Constructors
	
	/**
	 * @param context
	 * @param attrs
	 */
	public GaugeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(attrs != null){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeComponent);
			
			final int count = a.getIndexCount();
			for(int i = 0; i < count; i++){
				int attr = a.getIndex(i);
				switch(attr){
					case R.styleable.GaugeComponent_background:
						mBackground = a.getDrawable(attr);
						break;
				}
			}
			
			a.recycle();
		}
		
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.gauge_component, this);
	}

	// =============================================
	// Overrides
	

	// =============================================
	// Methods
	
	@Override
	public void onFinishInflate(){
		vBackground = (ImageView) this.findViewById(R.id.gaugeBackground);
		vSpeedNeedle = (NeedleView) findViewById(R.id.speedNeedle);
		vTachNeedle = (NeedleView) findViewById(R.id.tachNeedle);
		vNeutral = (ImageView) findViewById(R.id.neutralLight);
		vTurnSignal = (ImageView) findViewById(R.id.turnLight);
		vOdometerValue = (TextView) findViewById(R.id.odometerValueGauge);
		vTripValue = (TextView) findViewById(R.id.tripValueGauge);
		vVDC = (TextView) findViewById(R.id.vdcText);
		
		neutralLit(false);
		turnSignalLit(false);
		
		if(mBackground != null)
			vBackground.setImageDrawable(mBackground);
	}
	
	public void updateSpeed(float speed){
		vSpeedNeedle.updateValue(speed);
		this.invalidate();
	}
	
	public void updateTach(float tach){
		vTachNeedle.updateValue(tach);
		this.invalidate();
	}
	
	public void neutralLit(boolean isLit){
		if(isLit)
			vNeutral.setVisibility(View.VISIBLE);
		else
			vNeutral.setVisibility(View.INVISIBLE);
		
		this.invalidate();
	}
	
	public void turnSignalLit(boolean isLit){
		if(isLit)
			vTurnSignal.setVisibility(View.VISIBLE);
		else
			vTurnSignal.setVisibility(View.INVISIBLE);
		
		this.invalidate();
	}
	
	public void updateOdometer(float odometer){
		String temp = "";
		
		if(odometer < 10000){
			temp += " ";
			if(odometer < 1000){
				temp += " ";
				if(odometer < 100){
					temp += " ";
					if(odometer < 10){
						temp += " ";
						if(odometer < 1){
							temp += "0";
							vOdometerValue.setText(temp);
							this.invalidate();
							return;
						}
					}
				}
			}
		}
		
		temp += String.format("%5.0f", odometer);
		vOdometerValue.setText(temp);
		this.invalidate();
	}
	
	public void updateTrip(float trip){
		String temp = "";
		
		if(trip < 100){
			temp += " ";
			if(trip < 10){
				temp += " ";
			}
		}
		
		temp += String.format("%3.1f", trip);
		vTripValue.setText(temp);
		this.invalidate();
	}
	
	public void updateVDC(float vdc){
		String temp = "";
		
		if(vdc < 10 && vdc > 0 )
			temp += "  ";
		if(vdc > 10)
			temp += " ";
		if(vdc < 0 && vdc > -9.9)
			temp += " ";
		
		temp += String.format("%2.1f", vdc);
		
		temp += " :VDC";
		vVDC.setText(temp);
		
		this.invalidate();
	}
	
	// =============================================
	// Private inner Classes
}
