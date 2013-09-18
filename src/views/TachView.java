/**
 * 
 */
package views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.downrangeproductions.vstargauge.R;
import com.vstargauge.Values;
import com.vstargauge.navigation.Util;

/**
 * @author PyleC1
 * 
 */
public class TachView extends View {
	// =============================================
	// Private/Protected variables

	private Context mContext = null;
	private int mColor = 0;
	private int mTextSize = 15; // A nice scaled pixel size. This is the default
								// size for a text view.
	private int mWidth;
	private int mHeight;
	private Drawable mBackground = null;
	private Paint mPaint = null;
	private RectF mOval1 = null;
	private RectF mOval2 = null;
	private RectF mNeutralRect = null;
	private RectF mTurnSignalRect = null;
	private Rect scratchRect = null;
	private boolean mTurnSignalLit;
	private boolean mNeutralLit;
	private float mTachValue;
	private float mSpeedValue;
	

	// =============================================
	// Statics
	
	/*
	 * These are scalars for the indicator lights on the image
	 * Since we know the underlying image, but not what it was
	 * scaled to we need to know what to multiply the new width/height values
	 * by 
	 */
	private static final float X1_NEUTRAL = 299/400;
	private static final float Y1_NEUTRAL = 330/640;
	private static final float X2_NEUTRAL = 342/400;
	private static final float Y2_NEUTRAL = 372/640;
	
	private static final float X1_TURNSIGNAL = 319/400;
	private static final float Y1_TURNSIGNAL = 379/640;
	private static final float X2_TURNSIGNAL = 361/400;
	private static final float Y2_TURNSIGNAL = 421/640;
	
	/*
	 * The same set, but in landscape
	 */
	private static final float X1_NEUTRAL_LAND = 329/640;
	private static final float Y1_NEUTRAL_LAND = 59/400;
	private static final float X2_NEUTRAL_LAND = 371/640;
	private static final float Y2_NEUTRAL_LAND = 101/400;
	
	private static final float X1_TURNSIGNAL_LAND = 379/640;
	private static final float Y1_TURNSIGNAL_LAND = 39/400;
	private static final float X2_TURNSIGNAL_LAND = 421/640;
	private static final float Y2_TURNSIGNAL_LAND = 81/400;
	

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			Values values = (Values) intent.getExtras().getParcelable(Util.UPDATE_VALUES);
			
			mTurnSignalLit = values.turnSignal;
			mNeutralLit = values.neutral;
			mTachValue = values.rpm;
			mSpeedValue = values.mph;
			
			forceRedraw();
		}
	};
	
	// =============================================
	// Constructors

	public TachView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		TypedArray a = mContext.obtainStyledAttributes(attrs,
				R.styleable.TachView);

		try {
			mTextSize = a.getDimensionPixelSize(R.styleable.TachView_textSize,
					mTextSize);
			mColor = a.getColor(R.styleable.TachView_logoColor, -1);
			mBackground = a.getDrawable(R.styleable.TachView_background);
		} finally {
			a.recycle();
		}

		if (mBackground == null) {
			final Display display = ((WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			if (display.getRotation() == Surface.ROTATION_0
					|| display.getRotation() == Surface.ROTATION_180)
				mBackground = getResources().getDrawable(
						R.drawable.v_star_gauge);
			else
				mBackground = getResources().getDrawable(
						R.drawable.v_star_gauge_horizontal);
		}
		
		if(mColor == -1){
			mColor = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(Util.LOGO_COLOR_KEY, Color.BLACK);
		}
		
		mPaint = new Paint();
		LocalBroadcastManager.getInstance(mContext)
			.registerReceiver(mReceiver, new IntentFilter(Util.UPDATE_VALUES));
		mOval1 = new RectF();
		mOval2 = new RectF();
		mNeutralRect = new RectF();
		mTurnSignalRect = new RectF();
		scratchRect = new Rect();
	}

	// =============================================
	// Overrides
	
	@Override
	public void onMeasure(int widthSpec, int heightSpec){
		int widthMode = MeasureSpec.getMode(widthSpec);
		int heightMode = MeasureSpec.getMode(heightSpec);
		int widthSize = MeasureSpec.getSize(widthSpec);
		int heightSize = MeasureSpec.getSize(heightSpec);
		
		int width;
		int height;
		
		int desiredWidth = mBackground.getMinimumWidth();
		int desiredHeight = mBackground.getMinimumHeight();
		
		if(widthMode == MeasureSpec.EXACTLY){
			width = widthSize;
		} else {
			width = desiredWidth;
			if(widthMode == MeasureSpec.AT_MOST)
				width = Math.min(widthSize, width);
		}
		
		if(heightMode == MeasureSpec.EXACTLY){
			height = heightSize;
		} else {
			height = desiredHeight;
			if(heightMode == MeasureSpec.AT_MOST){
				height = Math.min(heightSize, height);
			}
		}
		
		mWidth = width;
		mHeight = height;
		
		setMeasuredDimension(width, height);
		
		if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			mOval1.set(0,0, mWidth, mHeight/2);
			mOval2.set(0,mHeight/2, mWidth, mHeight);
			mNeutralRect.set(mWidth * TachView.X1_NEUTRAL, mHeight * TachView.Y1_NEUTRAL,
					         mWidth * TachView.X2_NEUTRAL, mHeight * TachView.Y2_NEUTRAL);
			mTurnSignalRect.set(mWidth * TachView.X1_TURNSIGNAL, mHeight * TachView.Y1_TURNSIGNAL,
					            mWidth * TachView.X2_TURNSIGNAL, mHeight * TachView.Y2_TURNSIGNAL);
		} else {
			mOval1.set(0, 0, mWidth/2, mHeight);
			mOval2.set(mWidth/2, 0, mWidth, mHeight);
			mNeutralRect.set(mWidth * TachView.X1_NEUTRAL_LAND, mHeight * TachView.Y1_NEUTRAL_LAND,
		            	     mWidth * TachView.X2_NEUTRAL_LAND, mHeight * TachView.Y2_NEUTRAL_LAND);
			mTurnSignalRect.set(mWidth * TachView.X1_TURNSIGNAL_LAND, mHeight * TachView.Y1_TURNSIGNAL_LAND,
		               			mWidth * TachView.X2_TURNSIGNAL_LAND, mHeight * TachView.Y2_TURNSIGNAL_LAND);
		}
		
		mBackground.setBounds(0, 0, mWidth, mHeight);
	}
	
	// =============================================
	// Methods
	
	@Override
	public void onDraw(Canvas canvas){
		if(mBackground == null){
			return;
		}
		
		mPaint.setColor(mColor);
		canvas.drawArc(mOval1, // Bounding rectangle for oval
				       0, // Start angle. Uses geometric 0 
				       361, // How many degrees to draw the arc in
				       true, // Draw a wedge instead of an arc
				       mPaint); // The paint object that describes how to fill the arc
		canvas.drawArc(mOval2, 180, 180, true, mPaint);
		
		if(mTurnSignalLit)
			mPaint.setColor(Color.GREEN);
		 else
			mPaint.setColor(Color.BLACK);
		
		canvas.drawRect(mTurnSignalRect, mPaint);
		
		if(mNeutralLit)
			mPaint.setColor(Color.GREEN);
		else
			mPaint.setColor(Color.BLACK);
		
		canvas.drawRect(mNeutralRect, mPaint);
				
		
//		mBackground.setBounds(scratchRect);
		mBackground.draw(canvas);
		
	}
	
	public void forceRedraw(){
		this.invalidate();
	}
	
	@Override
	public void onDetachedFromWindow(){
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
		super.onDetachedFromWindow();
	}

	// =============================================
	// Private inner Classes
}
