/**
 * 
 */
package views;

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

		private long mElapsedAnimationTime = 0;
		private long mPreviousTimeStamp = 0;
		
		private int mCanvasHeight = 0;
		private int mCanvasWidth = 0;
		private int mColor;

		// ============================================================
		// Constants

		// 10 milliseconds converted to nanoseconds
		private static final long ANIMATION_LENGTH = (long) (10e6);

		private static final float SPEEDO_ANGLE_MIN = (180f - 52.45f);
		private static final float SPEEDO_ANGLE_MIN_LAND = 52.75f;
		private static final float SPEEDO_ANGLE_MAX = 52.8f;
		private static final float SPEEDO_ANGLE_MAX_LAND = 302.25F;
		
		private static final float SPEED_SCALE_FACTOR = 30f/10f;

		private static final float TACH_ANGLE_MIN = 180f;
		private static final float TACH_ANGLE_MIN_LAND = 90f;
		private static final float TACH_ANGLE_MAX = 0f;
		private static final float TACH_ANGLE_MAX_LAND = 270f;
		
		private static final float TACH_SCALE_FACTOR = 180f/6000f;
				
		private static final float NEUTRAL_LIGHT_LEFT = (500f/720f); // 300/400
		private static final float NEUTRAL_LIGHT_LEFT_LAND = 0.515625f; // 330/640
		private static final float NEUTRAL_TOP = (680f/1280f);
		private static final float NEUTRAL_TOP_LAND = 0.15f; // 60/400
		
		private static final float LIGHT_SIZE_LONG = (80f/1280f);
		private static final float LIGHT_SIZE_SHORT = (80f/720f);
		
		private static final float TURN_LIGHT_LEFT = (560f/720f);
		private static final float TURN_LIGHT_LEFT_LAND = 0.59375f;
		private static final float TURN_LIGHT_TOP = (760f/1280f);
		private static final float TURN_LIGHT_TOP_LAND = 0.0625f;
		

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
			mNeedle = BitmapFactory.decodeResource(mRes,
					R.drawable.small_needle);

			mPaint = new Paint();
			scratchRect = new RectF();
			
			if(isInEditMode()){
				mColor = Color.MAGENTA;
			} else {
				mColor = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(Util.LOGO_COLOR_KEY, Color.BLACK);
			}
		}

		// =============================================
		// Methods

		private void doDraw(Canvas canvas) {
			mPaint.setColor(mColor);
			
			float temp = (float)(mCanvasHeight) * (720f/1280f);
			
			scratchRect.set(0, 0, mCanvasWidth, temp);
			canvas.drawArc(scratchRect, 0, 361, true, mPaint);
			
			temp = (float)(mCanvasHeight) - ((float)mCanvasHeight * (720f/1280f));
			
			scratchRect.set(0, temp, mCanvasWidth, mCanvasHeight );
			canvas.drawArc(scratchRect, 0, 361, true, mPaint);
			
			mPaint.setColor(Color.GREEN);
			scratchRect.set((mCanvasWidth * NEUTRAL_LIGHT_LEFT), (mCanvasHeight * NEUTRAL_TOP),
					         (mCanvasWidth * NEUTRAL_LIGHT_LEFT) + 80, (mCanvasHeight * NEUTRAL_TOP) + 80);
			canvas.drawRect(scratchRect, mPaint);
			
			scratchRect.set(0, 0, mCanvasWidth, mCanvasHeight);
//			canvas.drawBitmap(mBackground, null, scratchRect, mPaint);
			
		}

		public void run() {
			int errCount = 0;
			while (mRun) {
				Canvas c = null;

				try {
					c = mSurfaceHolder.lockCanvas(null);
					if(c == null){
	//					this.wait(10);
						errCount++;
						if(errCount > 10){
							mRun = false;
							Log.d("TachViewLoop", "Unable to obtain canvas for ten frames");
						}
					} else {
						errCount = 0;
						doDraw(c);
					}
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
		}
	}

	// ============ end TachViewThread ====================

	private TachViewThread mThread;

	/**
	 * @param context
	 * @param attrs
	 */
	public TachView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		mThread = new TachViewThread(holder, new Handler() {
			@Override
			public void handleMessage(Message m) {
				// TODO Any TachViewThread outgoing messages should go here
			}
		}, context);
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus) {
			if (mThread != null)
				mThread.setRunning(false);
		}
	}

	public TachViewThread getThread() {
		return mThread;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mThread.setRunning(true);
		mThread.start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mThread.setSurfaceSize(width, height);

	}

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
}