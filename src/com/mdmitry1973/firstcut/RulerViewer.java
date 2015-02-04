package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RulerViewer extends View implements ChooseUnitInterface
{
	private Canvas drawCanvas;
	private Bitmap canvasBitmap;
	private Paint backgroundPaint, canvasPaint, linePaint, textPaint;
	
	private boolean bVertical = false;
	private double xdpi = 0;
	private double ydpi = 0;
	private int currentUnit = 0;
	private double offset = 0.0;
	private double zoom = 1.0;
	
	private RulerViewer rulerSecond = null;
	
	public void setZoom(double zoom)
	{
		this.zoom = zoom;
	}
	
	public void setOffset(double offset)
	{
		this.offset = offset;
	}
	
	public void setVertical(boolean bVertical)
	{
		this.bVertical = bVertical;
	}
	
	public void setCurrentUnit(int currentUnit)
	{
		this.currentUnit = currentUnit;
	}
	
	public void setSecondRuler(RulerViewer rulerSecond)
	{
		this.rulerSecond = rulerSecond;
	}
	
	public RulerViewer(Context context) {
		super(context);
		
		Init();
	}

	public RulerViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Init();
	}

	public RulerViewer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		Init();
	}
	
	public void Init()
	{
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.WHITE);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		
		linePaint = new Paint();
		linePaint.setColor(Color.BLACK);
		linePaint.setStrokeWidth(2);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setStrokeWidth(1);
		textPaint.setTextSize(30); 
		textPaint.setStyle(Paint.Style.STROKE);
		textPaint.setStrokeJoin(Paint.Join.ROUND);
		
		xdpi = getResources().getDisplayMetrics().xdpi;
    	ydpi = getResources().getDisplayMetrics().ydpi;
    	
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
    	currentUnit = sharedPrefs.getInt("unit", 0);
	}
	
	public void Clear()
	{
		invalidate();
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
	}

	//draw the view - will be called after touch event
	@Override
	protected void onDraw(Canvas canvas) {
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		
		int maxLen = 0;
		double dip = 0;
		float height = 10;
		float[] pts = new float[4];
		float[] ptsPre = new float[4];
		float[] ptsStep = new float[4];
		
		if (bVertical == true)
		{
			maxLen = canvasBitmap.getHeight();
			dip = ydpi;
			height = canvasBitmap.getWidth();
			
			pts[0] = canvasBitmap.getWidth();
			pts[1] = 0;
			pts[2] = canvasBitmap.getWidth();
			pts[3] = canvasBitmap.getHeight();
		}
		else
		{
			maxLen = canvasBitmap.getWidth();
			dip = xdpi;
			height = canvasBitmap.getHeight();
			
			pts[0] = 0;
			pts[1] = height;
			pts[2] = maxLen;
			pts[3] = height;
		}
		
		drawCanvas.drawLines(pts, linePaint);
		
		String unitString =  getResources().getString(R.string.inch);
		
		if (currentUnit == 1)//mm
		{
			unitString = getResources().getString(R.string.mm);
		}
		else
		if (currentUnit == 2)//cm
		{
			unitString = getResources().getString(R.string.cm);
		}
		
		int pixPoint = 0;
		double unitPoint = 0;
		
		while(pixPoint < maxLen)
		{
			double unitPointFinal = unitPoint;
			double remainderMM10 = 0;
			
			if (currentUnit == 1)//mm
			{
				unitPointFinal = (unitPoint/* + offset*/) * 0.0393701;
				remainderMM10 = Math.IEEEremainder(unitPoint, 10);
			}
			else
			if (currentUnit == 2)//cm
			{
				unitPointFinal = (unitPoint /*+ offset*/) * 0.393701;
			}
			else//inch
			{
				//unitPointFinal = (unitPoint/* + offset*/) * 0.0393701;
			}
			
			pixPoint = (int)(unitPointFinal*dip*zoom + offset);
			
			if (bVertical == true)
			{
				pts[0] = height*0.5f;
				pts[1] = pixPoint;
				pts[2] = height;
				pts[3] = pixPoint;
			}
			else
			{
				pts[0] = pixPoint;
				pts[1] = height*0.5f;
				pts[2] = pixPoint;
				pts[3] = height;
			}
			
			if (currentUnit == 1)//mm
			{
				if (remainderMM10 == 0.0)
				{
					drawCanvas.drawLines(pts, linePaint);
				}
				else
				{
					if (bVertical == true)
					{
						pts[0] = height*0.8f;
					}
					else
					{
						pts[1] = height*0.8f;
					}
					
					drawCanvas.drawLines(pts, linePaint);
				}
			}
			else
			if (currentUnit == 0)//inch
			{
				if (unitPoint != 0)
				{
					if (bVertical == true)
					{
						float step = (pts[1] - ptsPre[1])/32;
						boolean swichStep = false;
						int counter = 0;
						
						ptsStep[0] = ptsPre[0];
						ptsStep[1] = ptsPre[1];
						ptsStep[2] = ptsPre[2];
						ptsStep[3] = ptsPre[3];
						
						for(float s = ptsPre[1]; s < pts[1]; s = s + step, counter++)
						{
							ptsStep[1] = ptsStep[1] + step;
							ptsStep[3] = ptsStep[3] + step;
							
							if (swichStep == false)
							{
								ptsStep[0] = height*0.9f;
								swichStep = true;
							}
							else
							{
								ptsStep[0] = height*0.8f;
								swichStep = false;
							}
							
							if (counter == 15)
							{
								ptsStep[0] = height*0.7f;
							}
							
							drawCanvas.drawLines(ptsStep, linePaint);
						}
					}
					else
					{
						float step = (pts[0] - ptsPre[0])/32;
						boolean swichStep = false;
						int counter = 0;
						
						ptsStep[0] = ptsPre[0];
						ptsStep[1] = height*0.8f;
						ptsStep[2] = ptsPre[2];
						ptsStep[3] = ptsPre[3];
						
						for(float s = ptsPre[0]; s < pts[0]; s = s + step, counter++)
						{
							ptsStep[0] = ptsStep[0] + step;
							ptsStep[2] = ptsStep[2] + step;
							
							if (swichStep == false)
							{
								ptsStep[1] = height*0.9f;
								swichStep = true;
							}
							else
							{
								ptsStep[1] = height*0.8f;
								swichStep = false;
							}
							
							if (counter == 15)
							{
								ptsStep[1] = height*0.7f;
							}
							
							drawCanvas.drawLines(ptsStep, linePaint);
						}
					}
				}
				
				drawCanvas.drawLines(pts, linePaint);
				
				ptsPre[0] = pts[0];
				ptsPre[1] = pts[1];
				ptsPre[2] = pts[2];
				ptsPre[3] = pts[3];
			}
			else
			{
				drawCanvas.drawLines(pts, linePaint);
			}
			
			if (bVertical == true)
			{
				drawCanvas.save();
				
				if (currentUnit == 1)//mm
				{
					if (remainderMM10 == 0.0)
					{
						drawCanvas.rotate(-90, height*0.5f, pixPoint);
						drawCanvas.drawText("" + unitPoint + unitString, height*0.5f, pixPoint, textPaint);
					}
				}
				else
				{
					drawCanvas.rotate(-90, height*0.5f, pixPoint);
					drawCanvas.drawText("" + unitPoint + unitString, height*0.5f, pixPoint, textPaint);
				}
				
				drawCanvas.restore();
			}
			else
			{
				if (currentUnit == 1)//mm
				{
					if (remainderMM10 == 0.0)
					{
						drawCanvas.drawText("" + unitPoint + unitString, pixPoint, height*0.5f, textPaint);
					}
				}
				else
				{
					drawCanvas.drawText("" + unitPoint + unitString, pixPoint, height*0.5f, textPaint);
				}
			}
			
			unitPoint++;
		}
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
		
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();
		
		Log.v("onTouchEvent", "action=" + event.getAction());
		//respond to down, move and up events
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//if (currentObjectType == ObjectType.Line || currentObjectType == ObjectType.Pen)
			//{
			//	currentPath.moveTo(touchX, touchY);
			//}
			//currentPath.clear();
			//currentPath.add(new PointF(touchX, touchY));
			Log.d("onTouchEvent", "ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:			
			Log.d("onTouchEvent", "ACTION_MOVE");
			break;
		case MotionEvent.ACTION_UP:			
			Log.d("onTouchEvent", "ACTION_UP");
			
			ChooseUnit dialog = new ChooseUnit(getContext());
			dialog.SetChooseUnitInterface(this);
    		dialog.show();
    		
			break;
		default:
			return false;
		}
		
		return true;
	}
	
	@Override
	public void UnitChanged()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
    	currentUnit = sharedPrefs.getInt("unit", 0);
    	
    	if (rulerSecond != null)
    	{
    		rulerSecond.setCurrentUnit(currentUnit);
    		rulerSecond.invalidate();
    	}
    	
    	invalidate();
	}
	
	@Override
	protected void  onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}

