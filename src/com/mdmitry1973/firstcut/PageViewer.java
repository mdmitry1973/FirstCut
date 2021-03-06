package com.mdmitry1973.firstcut;

import java.util.ArrayList;
import java.util.List;

import com.mdmitry1973.firstcut.CutObject.CutObjectType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewGroup;
import android.gesture.Gesture;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class PageViewer extends View 
							implements OnScaleGestureListener,
										OnGestureListener,
										OnDoubleTapListener
{
	private MainActivity mainActivity = null;
	private Canvas drawCanvas = null;
	private Bitmap canvasBitmap = null;
	private Paint backgroundPaint, backgroundDrawCanvas, canvasPaint, selRectPaint, selCirclePaint, textPaint;
	private ArrayList<CutObject> listPath;
	private CutObject currentPath = null;
	private Paint currentPaint;
	private int selPathIndex = -1;
	private RectF boundSelRect = null; 
	private MainActivity.ToolType currentToolType;
	private PointF offsetPoint = new PointF(0, 0);
	private float zoomFactor = 1.0f;
	private float zoomFactorMax = 4.0f;
	private float zoomFactorMin = 0.6f;
	private PointF focusScale = new PointF(0, 0); 
	private Matrix matrixToDraw = null; 
	private Matrix matrixToReal = null;
	private PointF downPoint = new PointF(0, 0);
	
	private float fPaperWidth = 11f;
	private float fPaperHeigh = 8.9f;
	
	private RulerViewer rulerVer = null;
	private RulerViewer rulerHor = null;
	
	private float widthRealSize = 0;
	private float heighRealSizet = 0;
	
	private ScaleGestureDetector scaleGestureDetector;
	private GestureDetectorCompat mDetector; 
	
	private float marginPress = 10.0f;
	
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
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		marginPress = 0.12f*metrics.xdpi;//0.3 cm
		
		currentToolType = MainActivity.ToolType.Line;
		
		backgroundDrawCanvas = new Paint();
		backgroundDrawCanvas.setColor(Color.WHITE);
		
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLUE);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		
		listPath = new ArrayList<CutObject>();
		
		currentPaint = new Paint();
		
		currentPaint.setColor(Color.BLACK);
		currentPaint.setAntiAlias(true);
		currentPaint.setStrokeWidth(3);
		currentPaint.setStyle(Paint.Style.STROKE);
		currentPaint.setStrokeJoin(Paint.Join.ROUND);
		currentPaint.setStrokeCap(Paint.Cap.BUTT );
		
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
		
		
		textPaint = new Paint(Paint.DITHER_FLAG);
		textPaint.setColor(Color.BLACK);
		//textPaint.setAntiAlias(true);
		//textPaint.setStrokeWidth(1);
		//textPaint.setStyle(Paint.Style.STROKE);
		
		offsetPoint.x = 0;
		offsetPoint.y = 0;
		
		ResetPaperSize();
		
		scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
		
		mDetector = new GestureDetectorCompat(getContext(), this);
        mDetector.setOnDoubleTapListener(this);
	}
	
	public void ResetPaperSize()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		int currentPaperIndex = sharedPrefs.getInt("currentPaperIndex", 0);
		String strPapers = sharedPrefs.getString("Papers", "");
		
		if (!strPapers.isEmpty())
	   	{
			List<String> arrLines = new ArrayList<String>();
		   	String []arrPaperLines = strPapers.split("\n");
	   		
	   		for (int i = 0; i < arrPaperLines.length; i++) 
	   		{
	   			if (i == currentPaperIndex)
	   			{
			   	    String []arr = arrPaperLines[i].split(";");
			   	    
			   	    if (arr.length > 2)
			   	    {
			   	    	fPaperWidth = Float.parseFloat(arr[1]);
			   			fPaperHeigh = Float.parseFloat(arr[2]);
			   	    }
			   	    
			   	    break;
	   			}
		   	}
	   	}
	}
	
	public ArrayList<CutObject> getObjects()
	{
		return listPath;
	}
	
	public void setObjects(ArrayList<CutObject> listPath)
	{
		this.listPath = listPath;
	}
	
	public void RemoveAll()
	{
		selPathIndex = -1;
		listPath.clear();
		
		if (currentPath != null)
		{
			currentPath.clear();
		}
		
		DrawBitmap();
		invalidate();
	}
	
	public void RemoveCurrent()
	{
		if (selPathIndex != -1 && boundSelRect != null)
		{
			listPath.remove(selPathIndex);
			selPathIndex = -1;
			DrawBitmap();
			invalidate();
		}
	}
	
	public void SetCurrentTool(MainActivity.ToolType toolType)
	{
		currentToolType = toolType;
		currentPath = null;
		
		invalidate();
	}
	
	//size assigned to view
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		PrepareBitmap();
		RecalcSize();
		DrawBitmap();
	}
	
	public float getZoomMax()
	{
		return zoomFactorMax;
	}
	
	public float getZoomMin()
	{
		return zoomFactorMin;
	}
	
	public float getZoom()
	{
		return zoomFactor;
	}
	
	public void setZoom(float zoom)
	{
		zoomFactor = zoom;
		
		rulerVer.setZoom(zoomFactor);
		rulerHor.setZoom(zoomFactor);
		
		RecalcSize();
		DrawBitmap();
		
		invalidate();
		rulerVer.invalidate();
		rulerHor.invalidate();
	}
	
	public void RecalcSize()
	{
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		
		widthRealSize = fPaperWidth;
		heighRealSizet = fPaperHeigh;
		
		widthRealSize = widthRealSize * metrics.xdpi;
		heighRealSizet = heighRealSizet * metrics.ydpi;
		
		{
			matrixToDraw = new Matrix();
			RectF rectSrc = new RectF(0, 0, widthRealSize, heighRealSizet);
			RectF rectDst = new RectF(offsetPoint.x, offsetPoint.y, 
					(widthRealSize*zoomFactor) + offsetPoint.x, (heighRealSizet*zoomFactor) + offsetPoint.y);
			
			matrixToDraw.setRectToRect(rectSrc, rectDst, Matrix.ScaleToFit.FILL);
		}
		
		{
			matrixToReal = new Matrix();
			RectF rectDst = new RectF(0, 0, widthRealSize, heighRealSizet);
			RectF rectSrc = new RectF(offsetPoint.x, offsetPoint.y, 
					(widthRealSize*zoomFactor) + offsetPoint.x, (heighRealSizet*zoomFactor) + offsetPoint.y);
			
			matrixToReal.setRectToRect(rectSrc, rectDst, Matrix.ScaleToFit.FILL);
		}
		
		drawCanvas.setMatrix(matrixToDraw);
		
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
	
	public Path setStar(float x, float y, float radius, float innerRadius, int numOfPt)
	{
		  
		double section = 2.0 * Math.PI/numOfPt;
			  
		Path path = new Path();
		  path.moveTo(
		   (float)(x + radius * Math.cos(0)), 
		   (float)(y + radius * Math.sin(0)));
		  path.lineTo(
		   (float)(x + innerRadius * Math.cos(0 + section/2.0)), 
		   (float)(y + innerRadius * Math.sin(0 + section/2.0)));
		  
		  for(int ii = 1; ii < numOfPt; ii++)
		  {
		   path.lineTo(
		    (float)(x + radius * Math.cos(section * ii)), 
		    (float)(y + radius * Math.sin(section * ii)));
		   path.lineTo(
		     (float)(x + innerRadius * Math.cos(section * ii + section/2.0)), 
		     (float)(y + innerRadius * Math.sin(section * ii + section/2.0)));
		  }
		  
		  path.close();
		  
		  return path;
	}
	
	public void DrawBitmap()
	{
		try {
			
			//drawCanvas.drawRect(-offsetPoint.x, -offsetPoint.y, 
			//		((-offsetPoint.x) + drawCanvas.getWidth())*zoomFactor, 
			///		((-offsetPoint.y) + drawCanvas.getHeight())*zoomFactor, 
			//		backgroundDrawCanvas);
			
			drawCanvas.drawRect(0, 0,//-offsetPoint.x*100, -offsetPoint.y*100, 
					drawCanvas.getWidth()*20, 
					drawCanvas.getHeight()*20, 
					backgroundDrawCanvas);//backgroundPaint);
			
			for (int i = 0; i < listPath.size(); i++)
			{
				CutObject obj = listPath.get(i);
				
				if (obj != null)
				{
					/*
					CutObjectText textObject = (CutObjectText)obj;
					
					if (textObject != null)
					{
						ArrayList<Path> pathes = textObject.getTextPathes("Example");
						
						for(int n = 0; n < pathes.size(); n++)
						{
							Path path = pathes.get(n);
							
							if (path != null)
							{
								drawCanvas.drawPath(path, currentPaint);
							}
						}
					}
					else
					*/
					{	
						Path path = obj.getDrawPath();
						
						if (path != null)
						{
							if (false)
							{
								ArrayList<PointF> outputPoints = new ArrayList<PointF>();
							
								SendDataTask.pathToPoints(path, outputPoints);
							

								int tempColor = currentPaint.getColor();
								int rr = 0;
								int gg = 0; 
								int bb = 0;
								
								for(int ii = 0; ii < outputPoints.size() - 1; ii++)
								{
									PointF p1 = outputPoints.get(ii);
									PointF p2 = outputPoints.get(ii + 1);
									
									 currentPaint.setARGB(255, rr, gg, bb);
									 rr = rr + 1;
									
									//drawCanvas.drawCircle(p1.x, p1.y, 15, currentPaint);
									drawCanvas.drawLine(p1.x, p1.y, p2.x, p2.y, currentPaint);
									
									currentPaint.setColor(Color.YELLOW);
									
									drawCanvas.drawCircle(p1.x, p1.y, 15, currentPaint);
								}
								
								currentPaint.setColor(Color.GREEN);
								
								PointF p11 = outputPoints.get(outputPoints.size() - 1);
								drawCanvas.drawCircle(p11.x, p11.y, 30, currentPaint);
								
								currentPaint.setColor(Color.BLUE);
								
								PointF p22 = outputPoints.get(0);
								drawCanvas.drawCircle(p22.x, p22.y, 50, currentPaint);
								
								currentPaint.setColor(tempColor);
							}
							else
							{
								drawCanvas.drawPath(path, currentPaint);
							}
						}
					}
				}
			}
			
			if (selPathIndex != -1 && boundSelRect != null)
			{
				drawCanvas.drawRect(boundSelRect, selRectPaint);
				
				drawCanvas.drawCircle(boundSelRect.left, boundSelRect.top, marginPress, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.left, boundSelRect.bottom, marginPress, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.right, boundSelRect.top, marginPress, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.right, boundSelRect.bottom, marginPress, selCirclePaint);
				
				drawCanvas.drawCircle(boundSelRect.left, boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2, marginPress, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2, boundSelRect.top, marginPress, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.right, boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2, marginPress, selCirclePaint);
				drawCanvas.drawCircle(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2, boundSelRect.bottom, marginPress, selCirclePaint);
				
				PointF centerRect = new PointF(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2,
												boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2);
				
				Bitmap bitmapCenter = BitmapFactory.decodeResource(getResources(), R.drawable.center_move);
				
				drawCanvas.drawBitmap(bitmapCenter, centerRect.x - bitmapCenter.getWidth()/2, centerRect.y - bitmapCenter.getHeight()/2, null);
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
		
			//canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
			
			canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
			
			if (currentPath != null)
			{
				/*
				CutObjectText textObject = (CutObjectText)currentPath;
				
				if (textObject != null)
				{
					ArrayList<Path> pathes = textObject.getTextPathes("Example");
					
					for(int n = 0; n < pathes.size(); n++)
					{
						Path path = pathes.get(n);
						
						if (path != null)
						{
							path.transform(matrixToDraw);
							canvas.drawPath(path, currentPaint);
						}
					}
				}
				else
				*/
				{
					Path path = currentPath.getDrawPath();
					
					if (path != null)
					{
						path.transform(matrixToDraw);
						canvas.drawPath(path, currentPaint);
					}
					else
					{
						assert path != null;
					}
				}
			}
		} 
		catch (Exception e) 
		{
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
			boolean resScale = scaleGestureDetector.onTouchEvent(event);
			
			if (scaleGestureDetector.isInProgress())
			{
				if (currentPath != null)
				{
					currentPath.clear();
				}
				
				return resScale;
			}
	    }
		
		boolean resDetector = mDetector.onTouchEvent(event);
		
		if (resDetector == true)
		{
			return true;
		}
		else
		{
			float [] pts = {touchX, touchY};
			
			matrixToReal.mapPoints(pts);
			
			touchX = pts[0];
			touchY = pts[1];
			
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				if (currentToolType == MainActivity.ToolType.Resize)
				{
					
				}
				else
				if (currentToolType == MainActivity.ToolType.Hand)
				{
					
				}
				else
				if ((currentToolType == MainActivity.ToolType.Pen ||
					currentToolType == MainActivity.ToolType.Line ||
					currentToolType == MainActivity.ToolType.Box ||
					currentToolType == MainActivity.ToolType.Star ||
					currentToolType == MainActivity.ToolType.Arrow ||
					currentToolType == MainActivity.ToolType.Circle ||
					currentToolType == MainActivity.ToolType.Text) &&
					currentPath.size() > 0)
				{
					if (currentToolType == MainActivity.ToolType.Pen)
					{
						currentPath.add(new PointF(touchX, touchY));
					}
					else
						if (currentToolType == MainActivity.ToolType.Line ||
							currentToolType == MainActivity.ToolType.Box ||
							currentToolType == MainActivity.ToolType.Star ||
							currentToolType == MainActivity.ToolType.Arrow ||
							currentToolType == MainActivity.ToolType.Circle ||
							currentToolType == MainActivity.ToolType.Text)
						{
							if (currentPath.size() > 1)
							{
								currentPath.remove(currentPath.size() - 1);
							}
							
							currentPath.add(new PointF(touchX, touchY));
						}
					
					//ask for text
					if (currentToolType == MainActivity.ToolType.Text)
					{
						final AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
					    final EditText input = new EditText(mainActivity);
					    alert.setView(input);
					    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
					            String value = input.getText().toString().trim();
					           
					            if (!value.isEmpty())
					            {
						            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
									CutObject object = CutObject.CreateObject(currentPath.getListPoints(), currentPath.getType());
									
									((CutObjectText)object).setFontManger(mainActivity);
									
									object.setCurrentPrefs(sharedPrefs);
									((CutObjectText)object).setText(value);
									
									listPath.add(object);
					            }
								
								currentPath = null;
								
								DrawBitmap();
								
								invalidate();
					        }
					    });

					    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
					        	
					            dialog.cancel();
					            
					            currentPath = null;
					            
					            DrawBitmap();
					            
					            invalidate();
					        }
					    });
					    alert.show();
					    return false;
					}
					
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
					CutObject object = CutObject.CreateObject(currentPath.getListPoints(), currentPath.getType());
					
					object.setCurrentPrefs(sharedPrefs);
					
					listPath.add(object);
					
					currentPath = null;
					
					DrawBitmap();
				}
				
				Log.d("onTouchEvent", "ACTION_UP");
				invalidate();
			}
			
			Log.d("onTouchEvent", "event=" + event);
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
				
				mainActivity.ResetUIInfo();
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
				
				rulerVer.setZoom(zoomFactor);
				rulerHor.setZoom(zoomFactor);
				
				DrawBitmap();
				
				invalidate();
				
				rulerVer.invalidate();
				rulerHor.invalidate();
				
				mainActivity.ResetUIInfo();
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
        
        downPoint.x = event.getX() - offsetPoint.x;
        downPoint.y = event.getY() - offsetPoint.y;
        
        float touchX = event.getX();
		float touchY = event.getY();
		
		float [] pts = {touchX, touchY};
		
		matrixToReal.mapPoints(pts);
		
		touchX = pts[0];
		touchY = pts[1];
		
		scaleType = ScaleType.none;
		
		if (currentToolType == MainActivity.ToolType.Pen ||
			currentToolType == MainActivity.ToolType.Line ||
			currentToolType == MainActivity.ToolType.Box ||
			currentToolType == MainActivity.ToolType.Star ||
			currentToolType == MainActivity.ToolType.Arrow ||
        	currentToolType == MainActivity.ToolType.Circle ||
        	currentToolType == MainActivity.ToolType.Text)
		{
			selPathIndex = -1;
			
			CutObject.CutObjectType currentObjectType = CutObject.CutObjectType.Pen;
			
			if (currentToolType == MainActivity.ToolType.Pen)
				currentObjectType = CutObject.CutObjectType.Pen;
			if (currentToolType == MainActivity.ToolType.Line)
				currentObjectType = CutObject.CutObjectType.Line;
			if (currentToolType == MainActivity.ToolType.Box)
				currentObjectType = CutObject.CutObjectType.Box;
			if (currentToolType == MainActivity.ToolType.Circle)
				currentObjectType = CutObject.CutObjectType.Circle;
			if (currentToolType == MainActivity.ToolType.Text)
				currentObjectType = CutObject.CutObjectType.Text;
			if (currentToolType == MainActivity.ToolType.Arrow)
				currentObjectType = CutObject.CutObjectType.Arrow;
			if (currentToolType == MainActivity.ToolType.Star)
				currentObjectType = CutObject.CutObjectType.Star;
			if (currentToolType == MainActivity.ToolType.Text)
				currentObjectType = CutObject.CutObjectType.Text;
			
			currentPath = CutObject.CreateObject(currentObjectType);
			
			currentPath.add(new PointF(touchX, touchY));
			
			if (currentToolType == MainActivity.ToolType.Text)
			{
				((CutObjectText)currentPath).setFontManger(mainActivity);
			}
			
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			
			currentPath.setCurrentPrefs(sharedPrefs);
			
			invalidate();
		}
		else
		if (currentToolType == MainActivity.ToolType.Resize)
		{
			if (selPathIndex != -1 && boundSelRect != null)
			{
				RectF boundRect = new RectF(boundSelRect.left - marginPress, boundSelRect.top - marginPress,
											boundSelRect.right + marginPress, boundSelRect.bottom + marginPress);
				
				if (boundRect.contains(touchX, touchY) == true)
				{
					RectF boundRect1 = new RectF(boundSelRect.left - marginPress, boundSelRect.top - marginPress,
													boundSelRect.left + marginPress, boundSelRect.top + marginPress);
					RectF boundRect2 = new RectF(boundSelRect.left - marginPress, boundSelRect.bottom - marginPress,
													boundSelRect.left + marginPress, boundSelRect.bottom + marginPress);
					RectF boundRect3 = new RectF(boundSelRect.right - marginPress, boundSelRect.top - marginPress, 
													boundSelRect.right + marginPress, boundSelRect.top + marginPress);
					RectF boundRect4 = new RectF(boundSelRect.right - marginPress, boundSelRect.bottom - marginPress,
													boundSelRect.right + marginPress, boundSelRect.bottom + marginPress);
					
					RectF boundRect5 = new RectF(boundSelRect.left + boundSelRect.width()/2 - marginPress, boundSelRect.top - marginPress,
												boundSelRect.left + boundSelRect.width()/2+ marginPress, boundSelRect.top + marginPress);
					RectF boundRect6 = new RectF(boundSelRect.left - marginPress, boundSelRect.top + boundSelRect.height()/2 - marginPress,
												boundSelRect.left + marginPress, boundSelRect.top + boundSelRect.height()/2 + marginPress);
					RectF boundRect7 = new RectF(boundSelRect.right - boundSelRect.width()/2 - marginPress, boundSelRect.bottom - marginPress,
												boundSelRect.right - boundSelRect.width()/2 + marginPress, boundSelRect.bottom + marginPress);
					RectF boundRect8= new RectF(boundSelRect.right - marginPress, boundSelRect.bottom - boundSelRect.height()/2 - marginPress,
												boundSelRect.right + marginPress, boundSelRect.bottom - boundSelRect.height()/2 + marginPress);
					
					PointF centerRect = new PointF(boundSelRect.left + (boundSelRect.right - boundSelRect.left)/2,
													boundSelRect.top + (boundSelRect.bottom - boundSelRect.top)/2);
					
					RectF boundRect9 = new RectF(centerRect.x - marginPress, centerRect.y - marginPress, 
							centerRect.x + marginPress, centerRect.y + marginPress);
				
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
		else
		{
			selPathIndex = -1;
			DrawBitmap();
			invalidate();
		}
		
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, 
            float velocityX, float velocityY) {
        Log.d("PageViewer", "onFling: " + event1.toString()+event2.toString());
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d("PageViewer", "onLongPress: " + event.toString()); 
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
      
        if (currentToolType == MainActivity.ToolType.Pen ||
        	currentToolType == MainActivity.ToolType.Line ||
        	currentToolType == MainActivity.ToolType.Box ||
        	currentToolType == MainActivity.ToolType.Star ||
        	currentToolType == MainActivity.ToolType.Arrow ||
        	currentToolType == MainActivity.ToolType.Circle ||
        	currentToolType == MainActivity.ToolType.Text)
		{
        	float touchX = e2.getX();
    		float touchY = e2.getY();
    		
    		float [] pts = {touchX, touchY};
    		
    		matrixToReal.mapPoints(pts);
    		
    		touchX = pts[0];
    		touchY = pts[1];
    		
    		if (currentToolType == MainActivity.ToolType.Line ||
    			currentToolType == MainActivity.ToolType.Box ||
    			currentToolType == MainActivity.ToolType.Star ||
    		    currentToolType == MainActivity.ToolType.Arrow ||
    	        currentToolType == MainActivity.ToolType.Circle ||
    	        currentToolType == MainActivity.ToolType.Text)
    		{
	        	if (currentPath.size() > 1)
				{
					currentPath.remove(currentPath.size() - 1);
				}
    		}
    		
			currentPath.add(new PointF(touchX, touchY));
			
			mainActivity.ResetUIInfo();
		}
		else
		if (currentToolType == MainActivity.ToolType.Hand)
		{
			PointF offsetPointTemp = new PointF();
			PointF offsetPointTemp2 = new PointF();
		
			float [] pts = {downPoint.x, downPoint.y};
			
			offsetPointTemp.x = pts[0];
			offsetPointTemp.y = pts[1];
			
			pts[0] = e2.getX();
			pts[1] = e2.getY();
			
			offsetPointTemp2.x = pts[0];
			offsetPointTemp2.y = pts[1];
			
			offsetPointTemp.x = (offsetPointTemp2.x - offsetPointTemp.x);
			offsetPointTemp.y = (offsetPointTemp2.y - offsetPointTemp.y);
			
			int diffWidth = (int)(widthRealSize*zoomFactor) - getWidth();
			int diffHeight = (int)(heighRealSizet*zoomFactor) - getHeight();
			
			if (offsetPointTemp.x < -diffWidth)
			{
				offsetPoint.x = -diffWidth;
			}
			else
			if (offsetPointTemp.x > 0)
			{
				offsetPoint.x = 0;
			}
			else
			{
				offsetPoint.x = offsetPointTemp.x;
			}
			
			if (offsetPointTemp.y < -diffHeight)
			{
				offsetPoint.y = -diffHeight;
			}
			else
			if (offsetPointTemp.y > 0)
			{
				offsetPoint.y = 0;
			}
			else
			{
				offsetPoint.y = offsetPointTemp.y;
			}
			
			rulerVer.setOffset(offsetPoint.y);
			rulerHor.setOffset(offsetPoint.x);
			
			RecalcSize();
			DrawBitmap();
			
			rulerVer.invalidate();
			rulerHor.invalidate();
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
						 
						matrixToDraw.mapRect(newTemp);
						 
						newTemp.top =  newTemp.top - distanceY;
						newTemp.bottom =  newTemp.bottom - distanceY;
						newTemp.left =  newTemp.left - distanceX;
						newTemp.right =  newTemp.right - distanceX;
						 
						matrixToReal.mapRect(newTemp);
						 
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
						 
						Matrix matrix = new Matrix();
						 
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
						 
						 matrixToDraw.mapRect(newTemp);
						 
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
						 
						 matrixToReal.mapRect(newTemp);
						 
						 Matrix matrix = new Matrix();// listPath.get(selPathIndex).getMatrix();
						 
						 matrix.setRectToRect(boundSelRect, newTemp, Matrix.ScaleToFit.FILL);
						 
						 listPath.get(selPathIndex).setMatrix(matrix);
					}
					 
					boundSelRect = listPath.get(selPathIndex).getComputeBounds();
					 
					mainActivity.ResetUIInfo();
							 
					DrawBitmap();
				}
			}
		}
        
        invalidate();
        
        return true;
    }
    
    public void resetSelBox()
    {
    	if (selPathIndex != -1 && selPathIndex < listPath.size())
    	{
    		boundSelRect = listPath.get(selPathIndex).getComputeBounds();
    	}
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d("PageViewer", "onShowPress: " + event.toString());
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        
        CutObject.CutObjectType currentObjectType = CutObject.CutObjectType.Line;
        
        float touchX = event.getX();
		float touchY = event.getY();
		
		float [] pts = {touchX, touchY};
		
		matrixToReal.mapPoints(pts);
		
		touchX = pts[0];
		touchY = pts[1];
		
		if (currentToolType == MainActivity.ToolType.Resize)
		{
			boolean reDraw = false;
			
			if (selPathIndex != -1)
			{
				reDraw = true;
			}
			
			if (currentPath != null)
			{
				currentPath.clear();
			}
			
			boundSelRect = null;
			selPathIndex = -1;
			
			{
				for(int i = 0; i < listPath.size(); i++)
				{
					ArrayList<PointF> path = listPath.get(i).getListPoints();
					
					RectF boundRect = listPath.get(i).getComputeBounds();
					RectF boundTempRect = new RectF(boundRect);
					
					boundTempRect.left = boundTempRect.left - marginPress;
					boundTempRect.top = boundTempRect.top - marginPress;
					boundTempRect.right = boundTempRect.right + marginPress;
					boundTempRect.bottom = boundTempRect.bottom + marginPress;
					
					if (boundTempRect.contains(touchX, touchY) == true)
					{
						if (listPath.get(i).getType() == CutObject.CutObjectType.Box ||
							listPath.get(i).getType() == CutObject.CutObjectType.Circle ||
							listPath.get(i).getType() == CutObject.CutObjectType.Star ||
							listPath.get(i).getType() == CutObject.CutObjectType.Arrow ||
							listPath.get(i).getType() == CutObject.CutObjectType.Text)
						{
							boundSelRect = new RectF(boundRect);
							
							selPathIndex = i;
							
							mainActivity.ResetUIInfo();
							
							DrawBitmap();
							invalidate();
							break;
						}
						else
						{
							for(int j = 1; j < path.size(); j++)
							{
								PointF lineStaPt = path.get(j - 1);
								PointF lineEndPt = path.get(j);
								PointF p = new PointF(touchX, touchY);
								
								double disMin = segmentDistToPoint(lineStaPt, lineEndPt, p);
								
								if (disMin < marginPress)
								{
									boundSelRect = new RectF(boundRect);
									
									selPathIndex = i;
									
									mainActivity.ResetUIInfo();
									
									DrawBitmap();
									invalidate();
									break;
								}
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
				currentPath.add(new PointF(touchX, touchY));
				currentObjectType = CutObject.CutObjectType.Pen;
			
			}
			else
				if (currentToolType == MainActivity.ToolType.Line)
				{
					if (currentPath.size() > 1)
					{
						currentPath.remove(currentPath.size() - 1);
					}
					
					currentPath.add(new PointF(touchX, touchY));
					currentObjectType = CutObject.CutObjectType.Line;
				}
			
			listPath.add(CutObject.CreateObject(currentPath.getListPoints(), currentObjectType));
			
			currentPath.clear();
			
			DrawBitmap();
		}
		
		//Log.d("onTouchEvent", "ACTION_UP");
		invalidate();
		
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        
        if (currentToolType == MainActivity.ToolType.Hand)
        {
	        float zoomFactorTemp = zoomFactor + 0.5f;
			
			if ((zoomFactorTemp < zoomFactorMax && zoomFactorTemp > zoomFactorMin))
			{
				zoomFactor = zoomFactorTemp;
				
				focusScale.x = event.getX();
				focusScale.y = event.getY();
				
				RecalcSize();
				DrawBitmap();
				
				rulerVer.setZoom(zoomFactor);
				rulerHor.setZoom(zoomFactor);
				
				rulerVer.invalidate();
				rulerHor.invalidate();
			}
        }
		
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
    
    public CutObject getCurrentObject()
    {
    	if (currentPath == null)
    	{
    		if (selPathIndex != -1 && selPathIndex < listPath.size())
    		{
    			return listPath.get(selPathIndex);
    		}
    	}
    	
    	return currentPath;
    }
    
    // void setCurrentObject(CutObject obj)
   // {
    //	currentPath = obj;
    //}
}

