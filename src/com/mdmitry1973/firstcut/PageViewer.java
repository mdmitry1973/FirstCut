package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.gesture.Gesture;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;

public class PageViewer extends View implements OnScaleGestureListener
{
	private Canvas drawCanvas = null;
	private Bitmap canvasBitmap = null;
	private Paint backgroundPaint, backgroundDrawCanvas, canvasPaint;
	private ArrayList<CutObject> listPath;
	private ArrayList<PointF> currentPath;
	private Paint currentPaint;
	private MainActivity.ToolType currentToolType;
	private PointF offsetPoint = new PointF(0, 0);
	private float zoomFactor = 1.0f;
	private float zoomFactorMax = 4.0f;
	private float zoomFactorMin = 0.6f;
	private PointF focusScale = new PointF(0, 0); 
	
	private RulerViewer rulerVer = null;
	private RulerViewer rulerHor = null;
	
	private double widthRealSize = 0;
	private double heighRealSizet = 0;
	
	private ScaleGestureDetector scaleGestureDetector;
	
	public void setVertical(RulerViewer rulerVer)
	{
		this.rulerVer = rulerVer;
	}
	
	public void setHorizantal(RulerViewer rulerHor)
	{
		this.rulerHor = rulerHor;
	}
	
	public PageViewer(Context context) {
		super(context);
		
		Init();
	}

	public PageViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Init();
	}

	public PageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		Init();
	}
	
	public void Init()
	{
		currentToolType = MainActivity.ToolType.Line;
		
		backgroundDrawCanvas = new Paint();
		backgroundDrawCanvas.setColor(Color.WHITE);
		
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLUE);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		
		listPath = new ArrayList<CutObject>();
		
		currentPath = new ArrayList<PointF>();
		currentPaint = new Paint();
		
		currentPaint.setColor(Color.BLACK);
		currentPaint.setAntiAlias(true);
		currentPaint.setStrokeWidth(3);
		currentPaint.setStyle(Paint.Style.STROKE);
		currentPaint.setStrokeJoin(Paint.Join.ROUND);
		currentPaint.setStrokeCap(Paint.Cap.ROUND);
		
		offsetPoint.x = 0;
		offsetPoint.y = 0;
		
		scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
	}
	
	public ArrayList<CutObject> getObjects()
	{
		return listPath;
	}
	
	public void Clear()
	{
		listPath.clear();
		currentPath.clear();
		invalidate();
	}
	
	public void SetCurrentTool(MainActivity.ToolType toolType)
	{
		currentToolType = toolType;
		currentPath.clear();
		invalidate();
	}
	
	//size assigned to view
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		//canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		//drawCanvas = new Canvas(canvasBitmap);
		
		PrepareBitmap();
		RecalcSize();
		
		Log.v("onSizeChanged", "onSizeChanged");
	}
	
	public void RecalcSize()
	{
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		widthRealSize = 11;
		heighRealSizet = 8.9;
		
		widthRealSize = widthRealSize * metrics.densityDpi;
		heighRealSizet = heighRealSizet * metrics.densityDpi;
		
		widthRealSize = widthRealSize * zoomFactor;
		heighRealSizet = heighRealSizet * zoomFactor;
		
		//offsetPoint.x = (offsetPoint.x * zoomFactor) - focusScale.x/2;
		//offsetPoint.y = (offsetPoint.y * zoomFactor) - focusScale.y/2;
		
		Matrix matrix = new Matrix();
		
		matrix.setScale(zoomFactor, zoomFactor);
	
		drawCanvas.setMatrix(matrix);
		
		zoomFactorMin = (float) (getWidth()/widthRealSize);
		
		if (zoomFactorMin > 0.9f)
		{
			zoomFactorMin = 0.8f;
		}
	}
	
	public void PrepareBitmap()
	{
		try {
			
			canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			
			drawCanvas = new Canvas(canvasBitmap);
			
		} catch (Exception e) {
	           
			Log.v("PageViewer", "Error" + e);
	    }
	}
	
	public void DrawBitmap()
	{
		try {
			
			//drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
			drawCanvas.drawRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight(), backgroundDrawCanvas);
			
			for (int i = 0; i < listPath.size(); i++)
			{
				CutObject path = listPath.get(i);
				
				for (int n = 1; n < path.size(); n++)
				{
					PointF p1 = path.get(n - 1);
					PointF p2 = path.get(n);
					
					drawCanvas.drawLine(p1.x + offsetPoint.x, p1.y + offsetPoint.y, 
							p2.x + offsetPoint.x, p2.y + offsetPoint.y, currentPaint);
				}
			}
		} catch (Exception e) {
	           
			Log.v("PageViewer", "Error" + e);
	    }
	}

	//draw the view - will be called after touch event
	@Override
	protected void onDraw(Canvas canvas) 
	{
		try {
		
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
			
			canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		
			for (int n = 1; n < currentPath.size(); n++)
			{
				PointF p1 = currentPath.get(n - 1);
				PointF p2 = currentPath.get(n);
				
				canvas.drawLine(p1.x + offsetPoint.x, p1.y + offsetPoint.y, 
						p2.x + offsetPoint.x, p2.y + offsetPoint.y, currentPaint);
			}
		} catch (Exception e) {
	           
			Log.v("PageViewer", "Error" + e);
	    }
	}
	
	//register user touches as drawing action
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		//Log.d("PageViewer","onTouchEvent: " + event.toString()); 
		
		//this.mDetector.onTouchEvent(event);
		
		//return super.onTouchEvent(event);

		float touchX = event.getX();
		float touchY = event.getY();
		
		if (currentToolType == MainActivity.ToolType.Hand)
		{
			if (event.getAction() != MotionEvent.ACTION_MOVE || event.getPointerCount() > 1) {
				boolean res = scaleGestureDetector.onTouchEvent(event);
				
				if (res == true)
				{
					return true;
				}
			}
		}
	
		Log.v("onTouchEvent", "action=" + event.getAction());
		//respond to down, move and up events
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//if (currentObjectType == ObjectType.Line || currentObjectType == ObjectType.Pen)
			//{
			//	currentPath.moveTo(touchX, touchY);
			//}
			currentPath.clear();
			currentPath.add(new PointF(touchX + Math.abs(offsetPoint.x), touchY + Math.abs(offsetPoint.y)));
			Log.d("onTouchEvent", "ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
			if (currentToolType == MainActivity.ToolType.Pen)
			{
				currentPath.add(new PointF(touchX + Math.abs(offsetPoint.x), touchY + Math.abs(offsetPoint.y)));
			}
			else
			if (currentToolType == MainActivity.ToolType.Line)
			{
				if (currentPath.size() > 1)
				{
					currentPath.remove(currentPath.size() - 1);
				}
				
				currentPath.add(new PointF(touchX + Math.abs(offsetPoint.x), touchY + Math.abs(offsetPoint.y)));
			}
			else
			if (currentToolType == MainActivity.ToolType.Hand)
			{
				float touchOldX = 0;
				float touchOldY = 0;
				
				int historySize = event.getHistorySize();
				
				if (historySize > 0)
				{
					PointF offsetPointTemp = new PointF();
					
					offsetPointTemp.x = offsetPoint.x;
					offsetPointTemp.y = offsetPoint.y;
					
					touchOldX = event.getHistoricalX(0);
					touchOldY = event.getHistoricalY(0);
					
					offsetPointTemp.x = offsetPointTemp.x + (touchX - touchOldX);
					offsetPointTemp.y = offsetPointTemp.y + (touchY - touchOldY);
					
					int diffWidth = (int)widthRealSize - getWidth();
					int diffHeight = (int)heighRealSizet - getHeight();
					
					if (offsetPointTemp.x > -diffWidth &&  
						offsetPointTemp.x <= 0)
					{
						offsetPoint.x = offsetPointTemp.x;
					}
					
					if (offsetPointTemp.y > -diffHeight &&  
						offsetPointTemp.y <= 0)
					{
						offsetPoint.y = offsetPointTemp.y;
					}
					
					rulerVer.setOffset(offsetPoint.y);
					rulerHor.setOffset(offsetPoint.x);
					
					rulerVer.invalidate();
					rulerHor.invalidate();
					
					RecalcSize();
					DrawBitmap();
				}
			}
			
			Log.d("onTouchEvent", "ACTION_MOVE");
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			CutObject.CutObjectType currentObjectType = CutObject.CutObjectType.Line;
			
			if (currentToolType == MainActivity.ToolType.Hand)
			{
				
			}
			else
			if (currentToolType == MainActivity.ToolType.Pen ||
				currentToolType == MainActivity.ToolType.Line)
			{
				if (currentToolType == MainActivity.ToolType.Pen)
				{
					currentPath.add(new PointF(touchX + Math.abs(offsetPoint.x), touchY + Math.abs(offsetPoint.y)));
					currentObjectType = CutObject.CutObjectType.Pen;
				
				}
				else
					if (currentToolType == MainActivity.ToolType.Line)
					{
						if (currentPath.size() > 1)
						{
							currentPath.remove(currentPath.size() - 1);
						}
						
						currentPath.add(new PointF(touchX + Math.abs(offsetPoint.x), touchY + Math.abs(offsetPoint.y)));
						currentObjectType = CutObject.CutObjectType.Line;
					}
				
				listPath.add(new CutObject((ArrayList<PointF>)currentPath.clone(), currentObjectType));
				currentPath.clear();
				
				DrawBitmap();
			}
			
			Log.d("onTouchEvent", "ACTION_UP");
			invalidate();
			break;
		default:
		{
			
		}
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void  onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		Log.d("onMeasure", "widthMeasureSpec=" + widthMeasureSpec);
		Log.d("onMeasure", "heightMeasureSpec=" + heightMeasureSpec);
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		try {
		
			Log.d("PageViewer", "onScale: " + detector.toString());
			Log.d("PageViewer", "onScale getScaleFactor=" + detector.getScaleFactor());
			float scaleFactor = detector.getScaleFactor();
			float zoomFactorTemp = zoomFactor + (scaleFactor - 1.0f);
			
			if (scaleFactor != 1.0f && (zoomFactorTemp < zoomFactorMax && zoomFactorTemp > zoomFactorMin))
			{
				zoomFactor = zoomFactorTemp;
				
				focusScale.x = detector.getFocusX();
				focusScale.y = detector.getFocusY();
				
				RecalcSize();
				DrawBitmap();
		       
				invalidate();
				
				rulerVer.setZoom(zoomFactor);
				rulerHor.setZoom(zoomFactor);
				
				rulerVer.invalidate();
				rulerHor.invalidate();
			}
			
		} catch (Exception e) {
	           
			Log.v("PageViewer", "Error" + e);
	    }
		
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.d("PageViewer", "onScaleBegin: " + detector.toString());
		
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		
		Log.d("PageViewer", "onScaleEnd: " + detector.toString());
		Log.d("PageViewer", "onScaleEnd getCurrentSpan=" + detector.getCurrentSpan());
		Log.d("PageViewer", "onScaleEnd getEventTime=" + detector.getEventTime());
		Log.d("PageViewer", "onScaleEnd getFocusX=" + detector.getFocusX());
		Log.d("PageViewer", "onScaleEnd getFocusY=" + detector.getFocusY());
		Log.d("PageViewer", "onScaleEnd getPreviousSpan=" + detector.getPreviousSpan());
		Log.d("PageViewer", "onScaleEnd getScaleFactor=" + detector.getScaleFactor());
		
		try {
			float scaleFactor = detector.getScaleFactor();
			float zoomFactorTemp = zoomFactor + (scaleFactor - 1.0f);
			
			if (scaleFactor != 1.0f && (zoomFactorTemp < zoomFactorMax && zoomFactorTemp > zoomFactorMin))
			{
				zoomFactor = zoomFactorTemp;
				
				focusScale.x = detector.getFocusX();
				focusScale.y = detector.getFocusY();
				
				RecalcSize();
				DrawBitmap();
		       
				invalidate();
				
				rulerVer.setZoom(zoomFactor);
				rulerHor.setZoom(zoomFactor);
				
				rulerVer.invalidate();
				rulerHor.invalidate();
			}
		} catch (Exception e) {
	           
			Log.v("PageViewer", "Error" + e);
	    }
	}
}

