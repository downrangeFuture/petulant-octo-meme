/**
 * 
 */
package views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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

	// =============================================
	// Statics

	// =============================================
	// Public variables

	// =============================================
	// Interfaces/Listeners

	// =============================================
	// Constructors
	
	public TurnIndicatorView(Context context, AttributeSet attrs){
		super(context, attrs);
		
		background = context.getResources().getDrawable(R.drawable.turn_indicator_background);
	}

	// =============================================
	// Overrides

	@Override
	public void onMeasure(int widthSpec, int heightSpec){
		final int widthCode = View.MeasureSpec.getMode(widthSpec);
		final int widthSize = View.MeasureSpec.getSize(widthSpec);
		final int heightCode = View.MeasureSpec.getMode(heightSpec);
		final int heightSize = View.MeasureSpec.getSize(heightSpec);
		
		final int desiredWidth = 200;
		final int desiredHeight = 200;
		
		int width, height;
		
		switch (widthCode){
			case View.MeasureSpec.AT_MOST:
				width = Math.min(desiredWidth, widthSize);
				break;
			case View.MeasureSpec.EXACTLY:
				width = widthSize;
				break;
			case View.MeasureSpec.UNSPECIFIED:
			default:
				width = desiredWidth;
				break;	
		}
		switch (heightCode){
			case View.MeasureSpec.AT_MOST:
				height = Math.min(desiredHeight, heightSize);
				break;
			case View.MeasureSpec.EXACTLY:
				height = heightSize;
				break;
			case View.MeasureSpec.UNSPECIFIED:
			default:
				height = desiredHeight;
				break;	
		}
		
		if(width != height){
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
	public void onDraw(Canvas c){
		super.onDraw(c);
		
		background.setBounds(0, 0, mWidth, mHeight);
		background.draw(c);
	}

	// =============================================
	// Private inner Classes
}
