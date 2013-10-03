/**
 * 
 */
package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.downrangeproductions.vstargauge.R;

/**
 * @author PyleC1
 * 
 */
public class NeedleView extends ImageView {

	// =============================================
	// Private/Protected variables

	// private int mImage;
	private float mMinAngle;
	private float mMaxAngle;
	private float mMaxValue;
	private float mMinValue;
	private float mAngleScalar;
	private float mRequestedValue = 0;
	private float mRequestedAngle = 0;
//	private float mCurrentAngle;
//	private long mAnimationTime = 0;
//	private Drawable mDrawable;
//	private boolean mAnimationReset = false;
	private boolean mRotateClockwise;
//	private long mLastTimeStamp = 0;
//	private float mAnimationStartAngle;

	// =============================================
	// Statics

//	private static final long ANIMATION_LENGTH = (long) 2.5e6;

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
	public NeedleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.Needle);
			final int count = a.getIndexCount();
			for(int i = 0; i < count; i++) {
				int attr = a.getIndex(i);
				switch (attr) {
					case R.styleable.Needle_image :
						Drawable d = a.getDrawable(attr);
						if (d != null) {
							setImageDrawable(d);
						}
						break;
					case R.styleable.Needle_min_angle :
						mMinAngle = a.getFloat(attr, 120f);
						break;
					case R.styleable.Needle_max_angle :
						mMaxAngle = a.getFloat(attr, 300f);
						break;
					case R.styleable.Needle_rotate_clockwise :
						mRotateClockwise = a.getBoolean(attr, true);
						break;
					case R.styleable.Needle_max_value :
						mMaxValue = a.getFloat(attr, 120f);
						break;
					case R.styleable.Needle_min_value :
						mMinValue = a.getFloat(attr, 0f);
						break;
				}
			}

			a.recycle();
		}

		calculateScalar();
		this.invalidate();
	}

	// =============================================
	// Overrides

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onDraw(Canvas c) {
		c.save();
		c.rotate(mMinAngle, this.getWidth() / 2, this.getHeight() / 2);
		
		c.rotate(mRequestedAngle, this.getWidth() / 2, this.getHeight() / 2);

		super.onDraw(c);

		c.restore();
	}

	// =============================================
	// Methods

	public void updateValue(float needleValue) {
		if(needleValue > mMaxValue)
			mRequestedValue = mMaxValue;
		else if(needleValue < mMinValue)
			mRequestedValue = mMinValue;
		else
			mRequestedValue = needleValue;
		
		mRequestedAngle = mRequestedValue * mAngleScalar;
				
		if (!mRotateClockwise) {
			mRequestedAngle = 0 - mRequestedAngle;
		}
		
		this.invalidate();
	}

	private void calculateScalar() {
		float sweep, range;

		mMinAngle += 90;
		mMaxAngle += 90;

		if (mRotateClockwise) {
			if (mMaxAngle < mMinAngle)
				mMaxAngle += 360;

			sweep = mMaxAngle - mMinAngle;
			range = mMaxValue - mMinValue;

			mAngleScalar = sweep / range;
		} else {
			if (mMaxAngle > mMinAngle) {
				mMaxAngle = 0 - (360 - mMaxAngle);
			}

			sweep = mMinAngle - mMaxAngle;
			range = mMaxValue - mMinValue;

			mAngleScalar = sweep / range;
		}
		
		this.invalidate();
	}

	public void setMaxValue(float maxValue) {
		mMaxValue = maxValue;
		calculateScalar();
	}

	public void setMinValue(float minValue) {
		mMinValue = minValue;
		calculateScalar();
	}

	public void setMaxAngle(float maxAngle) {
		mMinValue = maxAngle;
		calculateScalar();
	}

	public void setMinAngle(float minAngle) {
		mMinAngle = minAngle;
		calculateScalar();
	}

	public void setImage(Drawable d) {
		setImageDrawable(d);
		this.invalidate();
	}

	// =============================================
	// Private inner Classes
}
