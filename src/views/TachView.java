/**
 * 
 */
package views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.downrangeproductions.vstargauge.R;
import com.vstargauge.navigation.Util;

public class TachView extends SurfaceView implements SurfaceHolder.Callback {

	protected class TachViewThread extends Thread {
		private SurfaceHolder mSurfaceHolder;
		private Handler mHandler;
		private Context mContext;
		private Resources mRes;
		private Paint mPaint;

		private Bitmap mBackground;
		private Bitmap mNeedle;

		private RectF scratchRect;

		private boolean mRun = true;

		private float mSpeedNeedleAngle = 0;
		private float mSpeedNeedleRequestedAngle = 0;
		private float mTachNeedleAngle = 0;
		private float mTachNeedleRequestedAngle = 0;
		
		private float mBackgroundOffsetLeft = 0;
		private float mBackgroundOffsetTop = 0;

		private long mElapsedAnimationTime = 0;
		private long mPreviousTimeStamp = 0;

		private int mCanvasHeight = 0;
		private int mCanvasWidth = 0;
		private int mColor;

		private float mNeutralLeft, mNeutralTop, mNeutralRight, mNeutralBottom;
		private float mTurnLeft, mTurnTop, mTurnRight, mTurnBottom;
		private int mOriginBackgroundWidth, mOriginBackgroundHeight;
		private int mOriginNeedleWidth, mOriginNeedleHeight;

		// ============================================================
		// Constants

		// 10 milliseconds converted to nanoseconds
		private static final long ANIMATION_LENGTH = (long) (10e6);

		private static final float SPEEDO_ANGLE_MIN = (180f - 52.45f);
		private static final float SPEEDO_ANGLE_MIN_LAND = 52.75f;
		private static final float SPEEDO_ANGLE_MAX = 52.8f;
		private static final float SPEEDO_ANGLE_MAX_LAND = 302.25F;

		private static final float SPEED_SCALE_FACTOR = 30f / 10f;

		private static final float TACH_ANGLE_MIN = 180f;
		private static final float TACH_ANGLE_MIN_LAND = 90f;
		private static final float TACH_ANGLE_MAX = 0f;
		private static final float TACH_ANGLE_MAX_LAND = 270f;

		private static final float TACH_SCALE_FACTOR = 180f / 6000f;
		
		private static final float NEUTRAL_LEFT_PERCENT = (float) 6.9444444444e-1;
		private static final float NEUTRAL_TOP_PERCENT = (float) 5.3125e-1;
		private static final float NEUTRAL_WIDTH_PERCENT = (float) 1.1111111111e-1;
		private static final float NEUTRAL_HEIGHT_PERCENT = (float) 6.25e-2;
		
		private static final float TURN_LEFT_PERCENT = (float) 7.7777777777777777e-1;
		private static final float TURN_TOP_PERCENT = (float) 5.9375e-1;
		private static final float TURN_WIDTH_PERCENT = NEUTRAL_WIDTH_PERCENT;
		private static final float TURN_HEIGHT_PERCENT = NEUTRAL_HEIGHT_PERCENT;

		// =============================================
		// Ctors

		public TachViewThread(SurfaceHolder holder, Handler handler,
				Context context) {
			mSurfaceHolder = holder;
			mHandler = handler;
			mContext = context;
			mRes = mContext.getResources();

			mBackground = (mRes.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
					? BitmapFactory.decodeResource(mRes,
							R.drawable.v_star_gauge) : BitmapFactory
							.decodeResource(mRes,
									R.drawable.v_star_gauge_horizontal);
			this.mOriginBackgroundWidth = mBackground.getWidth();
			this.mOriginBackgroundHeight = mBackground.getHeight();

			mNeedle = BitmapFactory.decodeResource(mRes,
					R.drawable.small_needle);

			this.mOriginNeedleWidth = mNeedle.getWidth();
			this.mOriginNeedleHeight = mNeedle.getHeight();

			if (isInEditMode()) {
				mColor = Color.MAGENTA;
			} else {
				mColor = PreferenceManager
						.getDefaultSharedPreferences(mContext).getInt(
								Util.LOGO_COLOR_KEY, Color.BLACK);
			}
			
			mPaint = new Paint();
			scratchRect = new RectF();
		}

		// =============================================
		// Methods

		/**
		 * Solves for x in n1/d1 = x/d2
		 * 
		 * X will be scaled to the original ratio. The known values should be
		 * the denominators, but which it is doesn't matter as long as they're
		 * the same. In other words 16:9 or 9:16 is irrelevant.
		 * 
		 * @param n1
		 *            Origin numerator
		 * @param d1
		 *            Origin denominator
		 * @param d2
		 *            Scaled denominator
		 * @return The scaled value rounded to an integer for use with pixel
		 *         widths
		 */
		private int getScaledRatio(float n1, float d1, int d2) {
			return Math.round(d2 * (n1 / d1));
		}

		private void doDraw(Canvas canvas) {
			mPaint.setColor(Color.BLACK);
			canvas.drawPaint(mPaint);
			mPaint.setColor(Color.WHITE);

			float temp = (float) (mCanvasHeight) * (mBackground.getWidth() / mBackground.getHeight());

			scratchRect.set(mBackgroundOffsetLeft,
					        mBackgroundOffsetTop,
					        mCanvasWidth - mBackgroundOffsetLeft,
					        temp);
			canvas.drawArc(scratchRect, 0, 361, true, mPaint);

			temp = (float) (mCanvasHeight)
					- ((float) mCanvasHeight * (mBackground.getWidth() / mBackground.getHeight()));

			scratchRect.set(mBackgroundOffsetLeft,
					        temp + mBackgroundOffsetTop,
					        mCanvasWidth - mBackgroundOffsetLeft,
					        mCanvasHeight - mBackgroundOffsetTop);
			canvas.drawArc(scratchRect, 0, 361, true, mPaint);

			mPaint.setColor(Color.GREEN);
			scratchRect.set(mNeutralLeft, mNeutralTop, mNeutralRight, mNeutralBottom);
			canvas.drawRect(scratchRect, mPaint);
			
			mPaint.setColor(Color.RED);
			scratchRect.set(mTurnLeft, mTurnTop, mTurnRight, mTurnBottom);
			canvas.drawRect(scratchRect, mPaint);

//			canvas.drawBitmap(mBackground, mBackgroundOffsetLeft, mBackgroundOffsetTop, null);
		}

		public void run() {
			int errCount = 0;
			while (mRun) {
				Canvas c = null;

				try {
					c = mSurfaceHolder.lockCanvas(null);
					if (c == null) {
						synchronized (this) {
							this.wait(10);
						}
						errCount++;
						if (errCount > 10) {
							mRun = false;
							Log.d("TachViewLoop",
									"Unable to obtain canvas for ten frames");
						}
					} else {
						errCount = 0;
						doDraw(c);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (c != null)
						mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}

		// ============================================
		// Getters/Setters

		/**
		 * Shuts down the thread gracefully. After setting this to false, thread
		 * will be in an invalid state and cannot be used!!
		 * 
		 * @param running
		 *            True if thread should be running
		 */
		public void setRunning(boolean running) {
			mRun = running;
		}
		
		public boolean isRunning(){
			return mRun;
		}

		/**
		 * Changes the surface size
		 * 
		 * @param width
		 *            The new surface width
		 * @param height
		 *            The new surface height
		 */
		public void setSurfaceSize(int width, int height) {
			mCanvasWidth = width;
			mCanvasHeight = height;

			mBackground = Bitmap.createScaledBitmap(
					mBackground,
					getScaledRatio(mBackground.getWidth(),
							mBackground.getHeight(), mCanvasHeight),
							mCanvasHeight, true);
			
			mBackgroundOffsetLeft = mCanvasWidth - mBackground.getWidth();
			mBackgroundOffsetLeft /= 2;
			
			mBackgroundOffsetTop = 0;
			
			//Offsets and such change based on which orientation we're in.
			if (mRes.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				mNeutralLeft = mBackgroundOffsetLeft + (mBackground.getWidth() * NEUTRAL_LEFT_PERCENT);
				mNeutralTop = mBackgroundOffsetTop + (mBackground.getHeight() * NEUTRAL_TOP_PERCENT);
				mNeutralRight = mNeutralLeft + (mCanvasWidth * NEUTRAL_WIDTH_PERCENT);
				mNeutralBottom = mNeutralTop + (mCanvasHeight * NEUTRAL_HEIGHT_PERCENT);
				
				mTurnLeft = mBackgroundOffsetLeft + (mBackground.getWidth() * TURN_LEFT_PERCENT);
				mTurnTop = mBackgroundOffsetTop + (mBackground.getHeight() * TURN_TOP_PERCENT);
				mTurnRight = mNeutralLeft + (mCanvasWidth * TURN_WIDTH_PERCENT);
				mTurnBottom = mNeutralTop + (mCanvasHeight * TURN_HEIGHT_PERCENT);
			} else {
				mNeutralLeft = getScaledRatio(mBackground.getWidth(), mOriginBackgroundWidth, 330);
				mNeutralTop = getScaledRatio(mBackground.getHeight(), mOriginBackgroundHeight, 60);
				mNeutralRight = mNeutralLeft + getScaledRatio(mBackground.getWidth(), mOriginBackgroundWidth, 40);
				mNeutralBottom = mNeutralTop + getScaledRatio(mBackground.getHeight(), mOriginBackgroundHeight, 40);
			}
		}
	} // ============ end TachViewThread ====================

	private TachViewThread mThread;
	private Context mContext;

	/**
	 * @param context
	 * @param attrs
	 */
	public TachView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		mThread = new TachViewThread(holder, makeHandler(), context);
	}

	/*
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call. We don't want to kill the whole
	 * view and we'll kill the thread lazily.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// If we're losing focus, we want to kill the thread so we let it exit its run method
		// and be subject to GC as needed.
		if (!hasWindowFocus) {
			if (mThread != null)
				mThread.setRunning(false);
		} else {
			// There are two very likely states the thread will be in when we come back to it
			// Null because GC cleaned the thread up; or in some unknown state of GC.
			// Either state would be invalid to run, so we re-make the thread. This is fine 
			// because there is nothing we really need to save. No game state or such.
			if (mThread == null || mThread.isRunning() == false){
				SurfaceHolder holder = getHolder();
				holder.addCallback(this);
				
				mThread = new TachViewThread(holder, makeHandler(), mContext);
			}
		}
	}

	public TachViewThread getThread() {
		return mThread;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try{
			mThread.setRunning(true);
			mThread.start();
		} catch (IllegalStateException e){
			mThread = new TachViewThread(holder, makeHandler(), mContext);
			mThread.setRunning(true);
			mThread.start();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mThread.setSurfaceSize(width, height);
	}

	/*
	 * This is called when the whole view is being destroyed for GC. We need to force the thread
	 * to end and not leave it dangling, or it'll cause leaks.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;

		while (retry) {
			try {
				// If we couldn't join the thread, then we'll skip
				// retry = false; and try again.
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler makeHandler(){
		return new Handler() {
			@Override
			public void handleMessage(Message m) {
				// TODO Any TachViewThread outgoing messages should go here
			}
		};
	}
}