/**
 * 
 */
package views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.downrangeproductions.vstargauge.R;
import com.vstargauge.util.Constants;

/**
 * @author PyleC1
 * 
 */
public class TurnIndicatorView extends View implements Constants {

	// =============================================
	// Private/Protected variables

	private int mWidth;
	private int mHeight;
	private Drawable background;
	private Drawable turnArrow;
	private Context mContext;

	public enum Arrow {
		LEFT, SHARP_LEFT, SLIGHT_LEFT, RIGHT, SHARP_RIGHT, SLIGHT_RIGHT, U_TURN, MERGE_LEFT, MERGE_RIGHT, EXIT, DESTINATION, NONE, STRAIGHT
	};

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	// =============================================
	// Constructors

	public TurnIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		background = mContext.getResources().getDrawable(
				R.drawable.turn_indicator_background);
	}

	// =============================================
	// Overrides

	@Override
	public void onMeasure(int widthSpec, int heightSpec) {
		final int widthCode = View.MeasureSpec.getMode(widthSpec);
		final int widthSize = View.MeasureSpec.getSize(widthSpec);
		final int heightCode = View.MeasureSpec.getMode(heightSpec);
		final int heightSize = View.MeasureSpec.getSize(heightSpec);

		final int desiredWidth = 200;
		final int desiredHeight = 200;

		int width, height;

		switch (widthCode) {
			case View.MeasureSpec.AT_MOST :
				width = Math.min(desiredWidth, widthSize);
				break;
			case View.MeasureSpec.EXACTLY :
				width = widthSize;
				break;
			case View.MeasureSpec.UNSPECIFIED :
			default :
				width = desiredWidth;
				break;
		}
		switch (heightCode) {
			case View.MeasureSpec.AT_MOST :
				height = Math.min(desiredHeight, heightSize);
				break;
			case View.MeasureSpec.EXACTLY :
				height = heightSize;
				break;
			case View.MeasureSpec.UNSPECIFIED :
			default :
				height = desiredHeight;
				break;
		}

		if (width != height) {
			width = Math.min(width, height);
			height = Math.min(width, height);
		}

		mWidth = width;
		mHeight = height;

		setMeasuredDimension(width, height);
	}

	// =============================================
	// Methods

	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);

		background.setBounds(0, 0, mWidth, mHeight);
		background.draw(c);
		if (turnArrow != null) {
			turnArrow.setBounds(5, 5, mWidth - 5, mHeight - 5);
			turnArrow.draw(c);
		}
	}

	public void changeTurnSymbol(Arrow arrow) {
		switch (arrow) {
			case EXIT :
			case MERGE_RIGHT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.merge_right);
				this.invalidate();
				break;
			case LEFT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.left_arrow);
				this.invalidate();
				break;
			case MERGE_LEFT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.merge_left);
				this.invalidate();
				break;
			case RIGHT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.right_arrow);
				this.invalidate();
				break;
			case SHARP_LEFT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.sharp_left);
				this.invalidate();
				break;
			case SHARP_RIGHT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.sharp_right);
				this.invalidate();
				break;
			case SLIGHT_LEFT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.slight_left);
				this.invalidate();
				break;
			case SLIGHT_RIGHT :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.slight_right);
				this.invalidate();
				break;
			case U_TURN :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.u_turn);
				this.invalidate();
				break;
			case DESTINATION :
				turnArrow = mContext.getResources().getDrawable(
						R.drawable.destination);
				this.invalidate();
				break;
			case NONE :
				turnArrow = null;
				this.invalidate();
				break;
			case STRAIGHT:
				turnArrow = mContext.getResources().getDrawable(R.drawable.straight);
			default :
				Log.wtf("turn indicator view",
						"Hit the default case in changeArrow(Arrow)");
				break;
		}
	}

	// =============================================
	// Private inner Classes
}
