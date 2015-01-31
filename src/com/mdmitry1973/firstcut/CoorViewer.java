package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CoorViewer extends View
{
	private Canvas drawCanvas;
	private Bitmap canvasBitmap;
	private Paint backgroundPaint, canvasPaint, lineXPaint, lineYPaint;
	private Bitmap coord_pos_2 = BitmapFactory.decodeResource(getResources(), R.drawable.coord_pos_2);
	private Matrix drawMatrix = new Matrix();
	
	float xdpi = 0;
	float ydpi = 0;
	
	int nRotate = 0;
	boolean bFlipVer = false;
	boolean bFlipHoz = false;
	
	public CoorViewer(Context context) {
		super(context);
		
		Init();
	}

	public CoorViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Init();
	}

	public CoorViewer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		Init();
	}
	
	public void setVal(int rotate, boolean flipv, boolean fliph)
	{
		nRotate = rotate;
		bFlipVer = flipv;
		bFlipHoz = fliph;
	}
	
	public int getRotate()
	{
		return nRotate;
	}
	
	public boolean getFlipVer()
	{
		return bFlipVer;
	}
	
	public boolean getFlipHoz()
	{
		return bFlipHoz;
	}
	
	public void setReset()
	{
		nRotate = 0;
		bFlipVer = false;
		bFlipHoz = false;
		
		drawMatrix.reset();
		
		DrawBitmap();
		invalidate();
	}
	
	public void setFlipVer()
	{
		bFlipVer = !bFlipVer;
		
		applyData();
	}
	
	public void setFlipHor()
	{
		bFlipHoz = !bFlipHoz;
		
		applyData();
	}
	
	public void setRotate(int rotate)
	{
		if (rotate == 0)
		{
			nRotate = nRotate + 90;
		}
		else
			if (rotate == 1)
			{
				nRotate = nRotate + 180;
			}
			else
				if (rotate == 2)
				{
					nRotate = nRotate + 270;
				}
		
		if (nRotate >= 360)
		{
			nRotate = nRotate - 360;
		}
		
		applyData();
	}
	
	public void applyData()
	{
		int width = drawCanvas.getWidth(); 
		int height = drawCanvas.getHeight(); 
		
		drawMatrix.setRotate(nRotate, width/2, height/2);
		drawMatrix.postScale(bFlipHoz ? -1 : 1, bFlipVer ? -1 : 1, width/2, height/2);
		
		DrawBitmap();
		invalidate();
	}
	
	public void Init()
	{
		xdpi = getResources().getDisplayMetrics().xdpi;
		ydpi = getResources().getDisplayMetrics().ydpi;
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.WHITE);
		
		lineXPaint = new Paint();
		lineXPaint.setStrokeWidth(xdpi/10);
		lineXPaint.setStyle(Paint.Style.STROKE);
		lineXPaint.setColor(Color.RED);
		
		lineYPaint = new Paint();
		lineYPaint.setStrokeWidth(xdpi/10);
		lineYPaint.setStyle(Paint.Style.STROKE);
		lineYPaint.setColor(Color.BLUE);
		
		drawMatrix.reset();
	}
	
	public Path setCoorPath(int nCoorSide, boolean nCoorDir)
	{
		Path path = new Path();
		float linestartX, linestartY, linestopX, linestopY;
		float dirX, dirY;
		int width = drawCanvas.getWidth(); 
		int height = drawCanvas.getHeight(); 
		
		if (nCoorSide == 0)//top
		{
			linestartX = 0; 
			linestartY = 0; 
			linestopX = width; 
			linestopY = 0;
		}
		else
			if (nCoorSide == 1)//right
			{
				linestartX = width; 
				linestartY = height; 
				linestopX = width; 
				linestopY = 0;
			}
			else
				if (nCoorSide == 2)//bottom
				{
					linestartX = 0; 
					linestartY = height; 
					linestopX = width; 
					linestopY = height;
				}
				else
					{
						linestartX = 0; 
						linestartY = height; 
						linestopX = 0; 
						linestopY = 0;
					}
		
	
		if (nCoorDir)
		{
			dirX = linestartX;
			dirY = linestartY;
		}
		else
		{
			dirX = linestopX;
			dirY = linestopY;
		}
		
		path.moveTo(linestartX, linestartY);
		path.lineTo(linestopX, linestopY);
		path.addCircle(dirX, dirY, 10, Direction.CW);
		path.close();
		
		return path;
	}
	
	public void DrawBitmap()
	{
		drawCanvas.setMatrix(drawMatrix);
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		drawCanvas.drawBitmap(coord_pos_2, 0, 0, backgroundPaint);
	}

	//size assigned to view
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		Log.v("onSizeChanged", "onSizeChanged");
		
		int width = getWidth();
		int height = getHeight();
		
		canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		
		DrawBitmap();
	}

	//draw the view - will be called after touch event
	@Override
	protected void onDraw(Canvas canvas) {
		
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	}

	
	@Override
	protected void  onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}

