package com.mdmitry1973.firstcut;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class ToolOptionDialogArrow extends Dialog 
									implements OnClickListener, SeekBar.OnSeekBarChangeListener{
	
	private EditText editX;
	private EditText editY;
	private EditText editWidth;
	private EditText editHeight;
	private SeekBar seekRotate;
	private SeekBar seekTailWidth;
	private SeekBar seekTailLength;
	private SeekBar seekCapLength;
	
	private TextView textViewTailWidthVal;
	private TextView textViewTailLengthVal;
	private TextView textViewCapLengthVal;
	private TextView textViewRotateVal;
	
	private CutObjectArrow currentPath;
	private int currentUnit;
	private float xdpi;
	private float ydpi;
	
	private TextView textRotateVal;
	
	/*
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.port_settings, null));//
	    // Add action buttons
	       //    .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
	      //         @Override
	      //         public void onClick(DialogInterface dialog, int id) {
	     //              // sign in the user ...
	      //         }
	      //     })
	       //    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	       //        public void onClick(DialogInterface dialog, int id) {
	        //           LoginDialogFragment.this.getDialog().cancel();
	       //        }
	       //    });     
	    
	    ((Button)getDialog().findViewById(R.id.buttonOK)).setOnClickListener(this);
	    ((Button)getDialog().findViewById(R.id.buttonCancel)).setOnClickListener(this);
	    
	    return builder.create();
	}
	*/
	
	private ToolOptionInterface mFinishInterface;
	
	public void setToolOptionInterface(ToolOptionInterface finishInterface)
	{
		mFinishInterface = finishInterface;
	}
	
	public ToolOptionDialogArrow(Context context, CutObject currentPath) {
		super(context);
		
		setContentView(R.layout.tool_options_arrow);
		setTitle(R.string.ToolOptionTitle);
		setCanceledOnTouchOutside(false);
		
		this.currentPath = (CutObjectArrow)currentPath;
		
		editX = ((EditText)findViewById(R.id.editTextX));
		editY = ((EditText)findViewById(R.id.editTextY));
		editWidth = ((EditText)findViewById(R.id.editTextWidth));
		editHeight = ((EditText)findViewById(R.id.editTextHeight));
		
		seekRotate = ((SeekBar)findViewById(R.id.seekRotate));		
		seekTailWidth = ((SeekBar)findViewById(R.id.seekTailWidth));
		seekTailLength = ((SeekBar)findViewById(R.id.seekTailLength));
		seekCapLength = ((SeekBar)findViewById(R.id.seekCapLength));
		
		textViewTailWidthVal = ((TextView)findViewById(R.id.textViewTailWidthVal));
		textViewTailLengthVal = ((TextView)findViewById(R.id.textViewTailLengthVal));
		textViewCapLengthVal = ((TextView)findViewById(R.id.textViewCapLengthVal));
		textViewRotateVal = ((TextView)findViewById(R.id.textViewRotateVal));
		
		textRotateVal = ((TextView)findViewById(R.id.textViewRotateVal));
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		xdpi = metrics.xdpi;
		ydpi = metrics.ydpi;
		
		currentUnit = sharedPrefs.getInt("unit", 0);
	
		getWindow().getDecorView().getBackground().setAlpha(sharedPrefs.getInt("TransparentToolOption", 50));
		
		seekTailWidth.setMax(100);
		seekTailLength.setMax(100);
		seekCapLength.setMax(100);
		
		if (currentPath == null)//default settings
		{
			editX.setEnabled(false);
			editY.setEnabled(false);
			editWidth.setEnabled(false);
			editHeight.setEnabled(false);
			seekRotate.setEnabled(false);
			
			seekTailWidth.setProgress((int)(100 * sharedPrefs.getFloat("ArrowTailWidth", 0.5f)));
			seekTailLength.setProgress((int)(100 * sharedPrefs.getFloat("ArrowTailLength", 0.75f)));
			seekCapLength.setProgress((int)(100 * sharedPrefs.getFloat("ArrowCapLength", 0.75f)));
			
			seekRotate.setMax(360);
			seekRotate.setProgress((int) sharedPrefs.getLong("Rotate", 0));
			((Button)findViewById(R.id.buttonApply)).setEnabled(false);
		}
		else
		{
			RectF rect = currentPath.getComputeBounds();
			
			float xInch = (float)rect.left / xdpi;
			float yInch = (float)rect.top / ydpi;
			
			float widthInch = (float)rect.width() / xdpi;
			float heightInch = (float)rect.height() / ydpi;

			if (currentUnit == 1)//mm
			{
				xInch = xInch * 0.0393701f;
				yInch = yInch * 0.0393701f;
				widthInch = widthInch * 0.0393701f;
				heightInch = heightInch * 0.0393701f;
			}
			else
			if (currentUnit == 2)//cm
			{
				xInch = xInch * 0.393701f;
				yInch = yInch * 0.393701f;
				widthInch = widthInch * 0.393701f;
				heightInch = heightInch * 0.393701f;
			}
			
			editX.setText(String.format("%.2f", xInch));
			editY.setText(String.format("%.2f", yInch));
			editWidth.setText(String.format("%.2f", widthInch));
			editHeight.setText(String.format("%.2f", heightInch));
			
			seekRotate.setMax(360);
			seekRotate.setProgress((int)currentPath.getDegree());
			
			seekTailWidth.setProgress((int)(100 * this.currentPath.getTailWidth()));
			seekTailLength.setProgress((int)(100 * this.currentPath.getTailLength()));
			seekCapLength.setProgress((int)(100 * this.currentPath.getCapLength()));
		}
		
		((Button)findViewById(R.id.buttonOk)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonApply)).setOnClickListener(this);
		
		textRotateVal.setText(String.format(" %d�", seekRotate.getProgress()));
		textViewTailWidthVal.setText(String.format(" %d%%", seekTailWidth.getProgress()));
		textViewTailLengthVal.setText(String.format(" %d%%", seekTailLength.getProgress()));
		textViewCapLengthVal.setText(String.format(" %d%%", seekCapLength.getProgress()));
		
		seekRotate.setOnSeekBarChangeListener(this);
		seekTailWidth.setOnSeekBarChangeListener(this);
		seekTailLength.setOnSeekBarChangeListener(this);
		seekCapLength.setOnSeekBarChangeListener(this);
	}
	
	
	public void onClick(View v) {       
		  switch (v.getId()) {
		    case R.id.buttonOk:
		    	onOK(v);
		    break;  
			case R.id.buttonApply:
		    	onApply(v);
		    break;  
		    case R.id.buttonCancel:
		    	onCancel(v);
		    break;  
		    default:                
		        break;
		   }
		}   

	public void onCancel(View v) {
	  
	   	cancel();
    }
	
	public void ApplyObject() 
	{
		if (currentPath != null)
		{
			currentPath.setTailWidth((float)seekTailWidth.getProgress()/100.0f);
			currentPath.setTailLength((float)seekTailLength.getProgress()/100.0f);
			currentPath.setCapLength((float)seekCapLength.getProgress()/100.0f);
			
			currentPath.setDegree(seekRotate.getProgress());
			String xInchStr = editX.getText().toString();
			String yInchStr = editY.getText().toString();
			String widthInchStr = editWidth.getText().toString();
			String heightInchStr = editHeight.getText().toString();
			
			float xInch = Float.parseFloat(xInchStr);
			float yInch = Float.parseFloat(yInchStr);
			
			float widthInch = Float.parseFloat(widthInchStr);
			float heightInch = Float.parseFloat(heightInchStr);
			
			if (currentUnit == 1)//mm
			{
				xInch = xInch / 0.0393701f;
				yInch = yInch / 0.0393701f;
				widthInch = widthInch / 0.0393701f;
				heightInch = heightInch / 0.0393701f;
			}
			else
			if (currentUnit == 2)//cm
			{
				xInch = xInch / 0.393701f;
				yInch = yInch / 0.393701f;
				widthInch = widthInch / 0.393701f;
				heightInch = heightInch / 0.393701f;
			}
			
			RectF rect = new RectF(xInch * xdpi, yInch * ydpi, 
									(xInch + widthInch) * xdpi, 
									(yInch + heightInch) * ydpi);
			
			currentPath.setComputeBounds(rect);
		}
	}
	
	public void onApply(View v) {
		  
		ApplyObject();
		
		mFinishInterface.onToolOptionApply();
    }
	
	public void onOK(View v) {
	   	
		int nRotate = seekRotate.getProgress();
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
    	editor.putLong("Rotate", nRotate);
    	
    	editor.putFloat("ArrowTailWidth",(float)seekTailWidth.getProgress()/100.0f);
    	editor.putFloat("ArrowTailLength", (float)seekTailLength.getProgress()/100.0f);
    	editor.putFloat("ArrowCapLength", (float)seekCapLength.getProgress()/100.0f);
    	
		editor.commit();
		
		ApplyObject();
		
		mFinishInterface.onToolOptionFinish();
	   	 
		dismiss();
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		if (seekRotate == seekBar)
		{
			textRotateVal.setText(String.format(" %d�", seekRotate.getProgress()));
		}
		else
			if (seekBar == seekTailWidth)
			{
				textViewTailWidthVal.setText(String.format(" %d%%", seekTailWidth.getProgress()));
			}
			else
			if (seekBar == seekTailLength)
			{
				textViewTailLengthVal.setText(String.format(" %d%%", seekTailLength.getProgress()));
			}
			else
			if (seekBar == seekCapLength)
			{
				textViewCapLengthVal.setText(String.format(" %d%%", seekCapLength.getProgress()));
			}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
