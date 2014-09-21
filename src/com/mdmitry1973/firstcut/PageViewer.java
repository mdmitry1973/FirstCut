package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.gesture.Gesture;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector;

public class PageViewer extends View 
							implements OnScaleGestureListener,
										OnGestureListener,
										OnDoubleTapListener//,
										//SimpleOnGestureListener
										
{
	private MainActivity mainActivity = null;
	private Canvas drawCanvas = null;
	private Bitmap canvasBitmap = null;
	private Paint backgroundPaint, backgroundDrawCanvas, canvasPaint, selRectPaint, selCirclePaint;
	private ArrayList<CutObject> listPath;
	private ArrayList<PointF> currentPath;
	private Paint currentPaint;
	private int selPathIndex = -1;
	private RectF boundSelRect = null; 
	private MainActivity.ToolType currentToolType;
	private PointF offsetPoint = new PointF(0, 0);
	private float zoomFactor = 1.0f;
	private float zoomFactorMax = 4.0f;
	private float zoomFactorMin = 0.6f;
	private PointF focusScale = new PointF(0, 0); 
	
	private RulerViewer rulerVer = null;
	private RulerViewer rulerHor = null;
	
	private float widthRealSize = 0;
	private float heighRealSizet = 0;
	
	private ScaleGestureDetector scaleGestureDetector;
	private GestureDetectorCompat mDetector; 
	//private GestureDetector gd;
	
	enum ScaleType {
		top,
		bottom,
		left,
		right,
		top_left,
		top_rigth,
		bottom_left,
		bottom_right,
		center,
		none
	};
	
	private ScaleType scaleType = ScaleType.none;
	
	public void setMainActivity(MainActivity mainActivity)
	{
		this.mainActivity = mainActivity;
	}
	
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
		
		selRectPaint = new Paint(Paint.DITHER_FLAG);
		selRectPaint.setColor(Color.GREEN);
		selRectPaint.setAntiAlias(true);
		selRectPaint.setStrokeWidth(1);
		selRectPaint.setStyle(Paint.Style.STROKE);
		selRectPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
		//selRectPaint.setStrokeJoin(Paint.Join.ROUND);
		//selRectPaint.setStrokeCap(Paint.Cap.ROUND);
		
		selCirclePaint = new Paint(Paint.DITHER_FLAG);
		selCirclePaint.setColor(Color.GREEN);
		selCirclePaint.setAntiAlias(true);
		selCirclePaint.setStrokeWidth(1);
		selCirclePaint.setStyle(Paint.Style.STROKE);
		//selRectPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
		
		offsetPoint.x = 0;
		offsetPoint.y = 0;
		
		scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
		
		mDetector = new GestureDetectorCompat(getContext(), this);
        mDetector.setOnDoubleTapListener(this);
        
        //GestureDetector.SimpleOnGestureListener gestureListener = new MyOnGestureListener();
        //gd = new GestureDetector(this.getContext(), gestureListener);

	}
	
	public ArrayList<CutObject> getObjects()
	{
		return listPath;
	}
	
	public void setObjects(ArrayList<CutObject> listPath)
	{
		this.listPath = listPath;
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
		
		widthRealSize = 11f;
		heighRealSizet = 8.9f;
		
		widthRealSize = widthRealSize * metrics.densityDpi;
		heighRealSizet = heighRealSizet * metrics.densityDpi;
		
		Matrix matrix = new Matrix();
		
		matrix.setScale(zoomFactor, zoomFactor);
	
		drawCanvas.setMatrix(matrix);
		
		zoomFactorMin = (float) (getWidth()/widthRealSize*zoomFactor);
		
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
			
			Matrix matrix = new Matrix();
			RectF rectSrc = new RectF(0, 0, widthRealSize, heighRealSizet);
			RectF rectDst = new RectF(offsetPoint.x, offsetPoint.y, 
					(widthRealSize*zoomFactor) + offsetPoint.x, (heighRealSizet*zoomFactor) + offsetPoint.y);
			
			matrix.setRectToRect(rectSrc, rectDst, Matrix.ScaleToFit.FILL);
			
			drawCanvas.drawRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight(), backgroundDrawCanvas);
			drawCanvas.setMatrix(matrix);
			
			for (int i = 0; i < listPath.size(); i++)
			{
				ArrayList<PointF> points = listPath.get(i).getObjectPath();
				
				for (int n = 1; n < points.size(); n++)
				{
					PointF p1 = points.get(n - 1);
					PointF p2 = points.get(n);
					
					drawCanvas.drawLine(p1.x , p1.y, p2.x, p2.y, currentPaint);
				}
			}
			
			if (selPathIndex != -1 && boundSelRect != null)
			{
				drawCanvas.drawRect(boundSelRect, selRectPaint);
				
				drawCanvas.drawCircle(boundSelRect.left, boundSelRect.top, 5, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.left, boundSelRect.bottom, 5, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.right, boundSelRect.top, 5, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.right, boundSelRect.bottom, 5, selCirclePaint);
				
				drawCanvas.drawCircle(boundSelRect.left, boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2, 5, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2, boundSelRect.top, 5, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.right, boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2, 5, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2, boundSelRect.bottom, 5, selCirclePaint);
				
				PointF centerRect = new PointF(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2,
												boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2);
				
				Bitmap bitmapCenter = BitmapFactory.decodeResource(getResources(), R.drawable.center_move);
				
				drawCanvas.drawBitmap(bitmapCenter, centerRect.x - 8, centerRect.y - 8, null);
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
		
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			CutObject.CutObjectType currentObjectType = CutObject.CutObjectType.Line;
			
			if (currentToolType == MainActivity.ToolType.Resize)
			{
				
			}
			else
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
		}
        
		if (mDetector.onTouchEvent(event) == true)
		{
			return true;
		}
		
		return super.onTouchEvent(event);
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
	
	public double segmentDistToPoint(PointF segA, PointF segB, PointF p)
	{
		PointF p2 = new PointF(segB.x - segA.x, segB.y - segA.y);
	    float something = p2.x*p2.x + p2.y*p2.y;
	    float u = ((p.x - segA.x) * p2.x + (p.y - segA.y) * p2.y) / something;

	    if (u > 1)
	        u = 1;
	    else if (u < 0)
	        u = 0;

	    float x = segA.x + u * p2.x;
	    float y = segA.y + u * p2.y;

	    float dx = x - p.x;
	    float dy = y - p.y;

	    double dist = Math.sqrt(dx*dx + dy*dy);

	    return dist;
	}
	
	public boolean isPointOnLine(PointF lineStaPt, PointF lineEndPt, PointF point) {
	    final float EPSILON = 0.1f;//001f;
	    if (Math.abs(lineStaPt.x - lineEndPt.x) < EPSILON) {
	        // We've a vertical line, thus check only the x-value of the point.
	        return (Math.abs(point.x - lineStaPt.x) < EPSILON);
	    } else {
	        float m = (lineEndPt.y - lineStaPt.y) / (lineEndPt.x - lineStaPt.x);
	        float b = lineStaPt.y - m * lineStaPt.x;
	        return (Math.abs(point.y - (m * point.x + b)) < EPSILON);
	    }
	}
	
	public PointF getIntersectLines(PointF p1, PointF p2, PointF p3, PointF p4)
	{
		PointF res = new PointF();
		float ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x))/
					((p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y));
		
		float ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x))/
				((p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y));
		
		res.x = p1.x + ua *(p2.x - p1.x);
		res.y = p1.y + ua *(p2.y - p1.y);
		
		return res;
	}
	
	@Override
    public boolean onDown(MotionEvent event) { 
        Log.d("PageViewer","onDown: " + event.toString()); 
        
        float touchX = event.getX() - offsetPoint.x;
		float touchY = event.getY() - offsetPoint.y;
		
		currentPath.clear();
		scaleType = ScaleType.none;
		
		if (currentToolType == MainActivity.ToolType.Pen ||
			currentToolType == MainActivity.ToolType.Line)
		{
			currentPath.add(new PointF(touchX, touchY));
			invalidate();
		}
		else
		if (currentToolType == MainActivity.ToolType.Resize)
		{
			if (selPathIndex != -1 && boundSelRect != null)
			{
				RectF boundRect = new RectF(boundSelRect.left - 5, boundSelRect.top - 5,
											boundSelRect.right + 5, boundSelRect.bottom + 5);
				
				if (boundRect.contains(touchX, touchY) == true)
				{
					RectF boundRect1 = new RectF(boundSelRect.left - 5, boundSelRect.top - 5,
													boundSelRect.left + 5, boundSelRect.top + 5);
					RectF boundRect2 = new RectF(boundSelRect.left - 5, boundSelRect.bottom - 5,
													boundSelRect.left + 5, boundSelRect.bottom + 5);
					RectF boundRect3 = new RectF(boundSelRect.right - 5, boundSelRect.top - 5, 
													boundSelRect.right + 5, boundSelRect.top + 5);
					RectF boundRect4 = new RectF(boundSelRect.right - 5, boundSelRect.bottom - 5,
													boundSelRect.right + 5, boundSelRect.bottom + 5);
					
					RectF boundRect5 = new RectF(boundRect1);//boundSelRect.left, boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2, 5, selCirclePaint);
					boundRect5.offset((boundSelRect.right - boundSelRect.left)/2 + 5, 0);
					RectF boundRect6 = new RectF(boundRect1);//boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2, boundSelRect.top, 5, selCirclePaint);
					boundRect6.offset(0, (boundSelRect.bottom - boundSelRect.top)/2 + 5);
					RectF boundRect7 = new RectF(boundRect4);//boundSelRect.right, boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2, 5, selCirclePaint);
					boundRect7.offset(-((boundSelRect.right - boundSelRect.left)/2 + 5), 0);
					RectF boundRect8= new RectF(boundRect4);//boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2, boundSelRect.bottom, 5, selCirclePaint);
					boundRect8.offset(0, -((boundSelRect.bottom - boundSelRect.top)/2 + 5));
					
					PointF centerRect = new PointF(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2,
													boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2);
					
					RectF boundRect9 = new RectF(centerRect.x - 5, centerRect.y - 5, centerRect.x + 5, centerRect.y + 5);
				
					if (boundRect1.contains(touchX, touchY) == true)
					{
						scaleType = ScaleType.top_left;
					}
					else
						if (boundRect2.contains(touchX, touchY) == true)
						{
							scaleType = ScaleType.bottom_left;
						}
						else
							if (boundRect3.contains(touchX, touchY) == true)
							{
								scaleType = ScaleType.top_rigth;
							}
							else
								if (boundRect4.contains(touchX, touchY) == true)
								{
									scaleType = ScaleType.bottom_right;
								}
								else
					if (boundRect5.contains(touchX, touchY) == true)
					{
						scaleType = ScaleType.top;
					}
					else
						if (boundRect6.contains(touchX, touchY) == true)
						{
							scaleType = ScaleType.left;
						}
						else
							if (boundRect7.contains(touchX, touchY) == true)
							{
								scaleType = ScaleType.bottom;
							}
							else
								if (boundRect8.contains(touchX, touchY) == true)
								{
									scaleType = ScaleType.right;
								}
								else
									if (boundRect9.contains(touchX, touchY) == true)
									{
										scaleType = ScaleType.center;
									}
				}
			}
		}
		
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, 
            float velocityX, float velocityY) {
        Log.d("PageViewer", "onFling: " + event1.toString()+event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d("PageViewer", "onLongPress: " + event.toString()); 
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        Log.d("PageViewer", "onScroll: " + e1.toString()+e2.toString());
        
        float touchX = e2.getX();
		float touchY = e2.getY();
        
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
			
			int historySize = e2.getHistorySize();
			
			if (historySize > 0)
			{
				PointF offsetPointTemp = new PointF();
				
				offsetPointTemp.x = offsetPoint.x;
				offsetPointTemp.y = offsetPoint.y;
				
				touchOldX = e2.getHistoricalX(0);
				touchOldY = e2.getHistoricalY(0);
				
				offsetPointTemp.x = offsetPointTemp.x + (touchX - touchOldX);
				offsetPointTemp.y = offsetPointTemp.y + (touchY - touchOldY);
				
				int diffWidth = (int)(widthRealSize*zoomFactor) - getWidth();
				int diffHeight = (int)(heighRealSizet*zoomFactor) - getHeight();
				
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
		else
		if (currentToolType == MainActivity.ToolType.Resize)
		{
			if (selPathIndex != -1 && boundSelRect != null)
			{
				 if (scaleType != ScaleType.none)
				 {
					 if (scaleType == ScaleType.center)
					 {
						 RectF newTemp = new RectF(boundSelRect);
						 
						 newTemp.top =  newTemp.top - distanceY;
						 newTemp.bottom =  newTemp.bottom - distanceY;
						 newTemp.left =  newTemp.left - distanceX;
						 newTemp.right =  newTemp.right - distanceX;
						 
						 if (newTemp.top < 0)
						 {
							 newTemp.top = 0;
							 newTemp.bottom = boundSelRect.bottom - boundSelRect.top;
						 }
						 
						 if (newTemp.left < 0)
						 {
							 newTemp.left = 0;
							 newTemp.right = boundSelRect.right - boundSelRect.left;
						 }
						 
						 Matrix matrix = listPath.get(selPathIndex).getMatrix();
						 
						 matrix.setRectToRect(boundSelRect, newTemp, Matrix.ScaleToFit.FILL);
						 
						 listPath.get(selPathIndex).setMatrix(matrix);
					 }
					 else
					 if (scaleType == ScaleType.top ||
						 scaleType == ScaleType.bottom ||
						 scaleType == ScaleType.left ||
						 scaleType == ScaleType.right ||
						 scaleType == ScaleType.top_left ||
						 scaleType == ScaleType.top_rigth ||
						 scaleType == ScaleType.bottom_left ||
						 scaleType == ScaleType.bottom_right)
					 {
						 RectF newTemp = new RectF(boundSelRect);
						 
						 if (scaleType == ScaleType.top ||
							 scaleType == ScaleType.top_left ||
							 scaleType == ScaleType.top_rigth)
						 {
							 newTemp.top =  newTemp.top - distanceY;
							 if (newTemp.top > newTemp.bottom)
								 newTemp.top = newTemp.bottom - 1;
							 
							 if (newTemp.top < 0)
								 newTemp.top = 0;
						 }
						 
						 if (scaleType == ScaleType.bottom ||
							 scaleType == ScaleType.bottom_left ||
							 scaleType == ScaleType.bottom_right)
						 {
							 newTemp.bottom =  newTemp.bottom - distanceY;
							 if (newTemp.top > newTemp.bottom)
								 newTemp.bottom = newTemp.top + 1;
						 }
						 
						 if (scaleType == ScaleType.left ||
							 scaleType == ScaleType.top_left ||
							 scaleType == ScaleType.bottom_left)
						 {
							 newTemp.left =  newTemp.left - distanceX;
							 if (newTemp.left > newTemp.right)
								 newTemp.left = newTemp.right - 1;
							 
							 if (newTemp.left < 0)
								 newTemp.left = 0;
						 }
						 
						 if (scaleType == ScaleType.right ||
							 scaleType == ScaleType.top_rigth ||
							 scaleType == ScaleType.bottom_right)
						 {
							 newTemp.right =  newTemp.right - distanceX;
							 if (newTemp.left > newTemp.right)
								 newTemp.right = newTemp.left + 1;
						 }
						 
						 Matrix matrix = listPath.get(selPathIndex).getMatrix();
						 
						 matrix.setRectToRect(boundSelRect, newTemp, Matrix.ScaleToFit.FILL);
						 
						 listPath.get(selPathIndex).setMatrix(matrix);
					 }
					 
					 boundSelRect = listPath.get(selPathIndex).getComputeBounds();
							 
					 DrawBitmap();
				 }
			}
		}
        
        invalidate();
        
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d("PageViewer", "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d("PageViewer", "onSingleTapUp: " + event.toString());
        
        CutObject.CutObjectType currentObjectType = CutObject.CutObjectType.Line;
        
        float touchX = event.getX();
		float touchY = event.getY();
		
		if (currentToolType == MainActivity.ToolType.Resize)
		{
			boolean reDraw = false;
			
			if (selPathIndex != -1)
			{
				reDraw = true;
			}
			
			currentPath.clear();
			boundSelRect = null;
			selPathIndex = -1;
			
			{
				Matrix matrix = new Matrix();
				RectF rectDst = new RectF(0, 0, widthRealSize, heighRealSizet);
				RectF rectSrc = new RectF(offsetPoint.x, offsetPoint.y, 
						(widthRealSize*zoomFactor) + offsetPoint.x, (heighRealSizet*zoomFactor) + offsetPoint.y);
				
				matrix.setRectToRect(rectSrc, rectDst, Matrix.ScaleToFit.FILL);
				
				PointF p = new PointF(touchX, touchY);// - offsetPoint.x, touchY - offsetPoint.y);
				float [] pts = {p.x, p.y};
				
				matrix.mapPoints(pts);
				
				p.x = pts[0];
				p.y = pts[1];
				
				for(int i = 0; i < listPath.size(); i++)
				{
					ArrayList<PointF> path = listPath.get(i).getObjectPath();
					
					RectF bounds = listPath.get(i).getComputeBounds();
					
					bounds.left = bounds.left - 10;
					bounds.top = bounds.top - 10;
					bounds.right = bounds.right + 10;
					bounds.bottom = bounds.bottom + 10;
					
					if (bounds.contains(p.x, p.y) == true)
					{
						for(int j = 1; j < path.size(); j++)
						{
							PointF lineStaPt = path.get(j - 1);
							PointF lineEndPt = path.get(j);
							
							double disMin = segmentDistToPoint(lineStaPt, lineEndPt, p);
							
							if (disMin < 10)
							{
								boundSelRect = new RectF(bounds.left + 10, bounds.top + 10,
										bounds.right - 10, bounds.bottom - 10);
								
								selPathIndex = i;
								
								if (mainActivity != null)
								{
									mainActivity.setSelObjectCoor(boundSelRect.left, boundSelRect.top, 
											boundSelRect.right - boundSelRect.left, boundSelRect.bottom - boundSelRect.top);
								}
								
								DrawBitmap();
								invalidate();
								break;
							}
						}
					}
				}
			}
			
			if (reDraw && selPathIndex == -1)
			{
				DrawBitmap();
			}
		}
		else
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
		
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d("PageViewer", "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d("PageViewer", "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d("PageViewer", "onSingleTapConfirmed: " + event.toString());
        return true;
    }

}

