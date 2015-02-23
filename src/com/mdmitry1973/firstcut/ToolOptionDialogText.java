package com.mdmitry1973.firstcut;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.TrueTypeFont;

import com.mdmitry1973.firstcut.PortManagerDialog.PortManagerInterface;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ToolOptionDialogText extends Dialog 
									implements OnClickListener, SeekBar.OnSeekBarChangeListener{
	
	private EditText editX;
	private EditText editY;
	private EditText editWidth;
	private EditText editHeight;
	private EditText editSpace;
	private EditText editTextEnter;
	private SeekBar seekRotate;
	private CutObjectText currentPath;
	private int currentUnit;
	private float xdpi;
	private float ydpi;
	
	private TextView textRotateVal;
	private TextView textViewSpaceUnit;
	private Spinner spinnerFontList;
	private CheckBox checkBoxBold;
	private CheckBox checkBoxItalic;
	
	private String currentLine = "";
	
	private MainActivity activity;
	
	List<String> arrFontNames = null;
	List<String> arrFonts = null;
	
	
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
	
	public ToolOptionDialogText(Context context, CutObject currentPath) {
		super(context);
		
		setContentView(R.layout.tool_options_text);
		setTitle(R.string.ToolOptionTitle);
		setCanceledOnTouchOutside(false);
		
		activity = (MainActivity)context;
		
		arrFontNames = new ArrayList<String>();
		arrFonts = new ArrayList<String>();
		
		this.currentPath = (CutObjectText)currentPath;
		
		editX = ((EditText)findViewById(R.id.editTextX));
		editY = ((EditText)findViewById(R.id.editTextY));
		editWidth = ((EditText)findViewById(R.id.editTextWidth));
		editHeight = ((EditText)findViewById(R.id.editTextHeight));
		
		editTextEnter = ((EditText)findViewById(R.id.editTextEnter));
		
		editSpace = ((EditText)findViewById(R.id.editTextSpace));
		
		seekRotate = ((SeekBar)findViewById(R.id.seekRotate));
		
		textRotateVal = ((TextView)findViewById(R.id.textViewRotateVal));
		textViewSpaceUnit= ((TextView)findViewById(R.id.textViewSpaceUnit));
		
		spinnerFontList = ((Spinner)findViewById(R.id.spinnerFontList));
		checkBoxBold = ((CheckBox)findViewById(R.id.checkBoxBold));
		checkBoxItalic = ((CheckBox)findViewById(R.id.checkBoxItalic));
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		xdpi = metrics.xdpi;
		ydpi = metrics.ydpi;
		
		currentUnit = sharedPrefs.getInt("unit", 0);
	
		getWindow().getDecorView().getBackground().setAlpha(sharedPrefs.getInt("TransparentToolOption", 50));
		
		float fSpace = 0;
		
		if (currentPath == null)//default settings
		{
			editX.setEnabled(false);
			editY.setEnabled(false);
			editWidth.setEnabled(false);
			editHeight.setEnabled(false);
			editTextEnter.setEnabled(false);
			
			seekRotate.setMax(360);
			seekRotate.setProgress((int) sharedPrefs.getLong("Rotate", 0));
			((Button)findViewById(R.id.buttonApply)).setEnabled(false);
			
			fSpace = sharedPrefs.getFloat("TextSpace", 0);
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
			
			fSpace = this.currentPath.getGlyphSpace();
			
			editX.setText(String.format("%.2f", xInch));
			editY.setText(String.format("%.2f", yInch));
			editWidth.setText(String.format("%.2f", widthInch));
			editHeight.setText(String.format("%.2f", heightInch));
			
			seekRotate.setMax(360);
			seekRotate.setProgress((int)currentPath.getDegree());
			
			editTextEnter.setText(this.currentPath.getText());
		}
		
		if (currentUnit == 1)//mm
		{
			fSpace = fSpace * 0.0393701f;
			textViewSpaceUnit.setText(R.string.mm);
		}
		else
		if (currentUnit == 2)//cm
		{
			fSpace = fSpace * 0.393701f;
			textViewSpaceUnit.setText(R.string.cm);
		}
		
		editSpace.setText(String.format("%.2f", fSpace));
		
		((Button)findViewById(R.id.buttonOk)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonCancel)).setOnClickListener(this);
		((Button)findViewById(R.id.buttonApply)).setOnClickListener(this);
		
		textRotateVal.setText(String.format(" %d°", seekRotate.getProgress()));
		seekRotate.setOnSeekBarChangeListener(this);
		
		setFontList();
		setFontProperties();
		
		int bBold = sharedPrefs.getInt("currentFontBold", 0);
		int bItalic = sharedPrefs.getInt("currentFontItalic", 0);
		
		checkBoxBold.setChecked(bBold == 1);
   		checkBoxItalic.setChecked(bItalic == 1);
   		
   		spinnerFontList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				setFontProperties();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	    });
	}
	
	public void setFontList()
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SpinnerAdapter adp = spinnerFontList.getAdapter();
		String currentName = "";
		int sel = spinnerFontList.getSelectedItemPosition();
		
		if (adp == null || sel == -1)
		{
			currentName = sharedPrefs.getString("currentFont", "droid_sans");
		}
		else
		{
			if (adp != null && sel != -1)
			{
				currentName = (String)adp.getItem(sel);
			}
		}
		
		String[] fontdirs = { "/system/fonts", "/system/font", "/data/fonts" };
		
		for(int j = 0; j < fontdirs.length; j++)
		{
			File fontDir = new File(fontdirs[j]);        
			File fileFonts[] = fontDir.listFiles();
			
			if (fileFonts != null)
			{
				for (int i=0; i < fileFonts.length; i++)
				{
					String fileName = fileFonts[i].getName();
					String fileNameWithOutExt = fileName;
					
					if (fileName.contains(".")) 
					{
						fileNameWithOutExt = fileName.substring(0, fileName.lastIndexOf("."));
					}
					
					File fontFile = new File(fontDir, fileName); 
					boolean found = false;
					String fileNameWithoutStyles = fileNameWithOutExt;
					
					if (fileNameWithoutStyles.contains("_")) 
					{
						fileNameWithoutStyles = fileNameWithoutStyles.substring(0, fileNameWithoutStyles.indexOf('_'));
					}
					
					if (fileNameWithoutStyles.contains("-")) 
					{
						fileNameWithoutStyles = fileNameWithoutStyles.substring(0, fileNameWithoutStyles.indexOf('-'));
					}
					
					for (int n = 0; n < arrFonts.size(); n++)
					{
						if (arrFonts.get(n).startsWith(fileNameWithoutStyles))
						{
							arrFonts.set(n, arrFonts.get(n) + "," + fileName);
							found = true;
							break;
						}
					}
					
					if (found == false)
					{
						arrFonts.add(fileNameWithoutStyles + "|" + fileName);
						arrFontNames.add(fileNameWithoutStyles);
						
						if (currentName.startsWith(fileNameWithoutStyles))
		   				{
		   					sel = arrFonts.size() - 1;
		   				}
					}
					
				    Log.d("Files", "FileName:" + fileFonts[i].getName());
				}
			}
		}
		
		if (arrFonts.size() == 0)
		{
			List<String> myArrayFontHardcodeList = Arrays.asList("Droid Sans|droid_sans,droid_sans_b\n" +
														"Arimo|arimo,arimo_b,arimo_i,arimo_bi\n" +
														"Anonymous Pro|anonymous_pro,anonymous_pro_b,anonymous_pro_i,anonymous_pro_bi\n");
			arrFonts = new ArrayList<String>(myArrayFontHardcodeList);
			
			for(int i = 0; i < arrFonts.size(); i++)
		   	{
		   		String []fontArrProperties = arrFonts.get(i).split("\\|");
		   		
		   		if (fontArrProperties.length > 1)
		   		{
		   			arrFontNames.add(fontArrProperties[0]);
		   			
		   			if (currentName.isEmpty() == false)
		   			{
			   			String []fontArrTypes = fontArrProperties[1].split(",");
			   			
			   			for(int n = 0; n < fontArrTypes.length; n++)
			   		   	{
			   				if (fontArrTypes[n].compareTo(currentName) == 0)
			   				{
			   					sel = arrFontNames.size() - 1;
			   					break;
			   				}
			   		   	}
		   			}
		   		}
		   	}
		}
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrFontNames);
        spinnerFontList.setAdapter(adapter);
        
        if (sel == -1 || sel > arrFonts.size())
        {
        	sel = 0;
        }
        
        spinnerFontList.setSelection(sel);
	}
	
	public void setFontProperties()
	{
		int pos = spinnerFontList.getSelectedItemPosition();
		
		if (pos < arrFonts.size())
		{
			currentLine = arrFonts.get(pos);
		}
		
		if (!currentLine.isEmpty())
		{
			boolean bSupporBold = false;
			boolean bSupporItalic = false;
			String []fontArrProperties = currentLine.split("\\|");
	   		
	   		if (fontArrProperties.length > 1)
	   		{
	   			String []fontArrTypes = fontArrProperties[1].split(",");
	   			
	   			for(int n = 0; n < fontArrTypes.length; n++)
	   		   	{
	   				if (fontArrTypes[n].endsWith("_b") == true)
	   				{
	   					bSupporBold = true;
	   				}
	   				
	   				if (fontArrTypes[n].endsWith("_i") == true)
	   				{
	   					bSupporItalic = true;
	   				}
	   				
	   				if (fontArrTypes[n].endsWith("_bi") == true)
	   				{
	   					bSupporBold = true;
	   					bSupporItalic = true;
	   				}
	   				
	   				String fileNameWithoutStyles = fontArrTypes[n];
					
					//if (fileNameWithoutStyles.contains("_")) 
					//{
					//	fileNameWithoutStyles = fileNameWithoutStyles.substring(0, fileNameWithoutStyles.indexOf('_'));
					//}
					
					//if (fileNameWithoutStyles.contains("-")) 
					//{
					//	fileNameWithoutStyles = fileNameWithoutStyles.substring(0, fileNameWithoutStyles.indexOf('-'));
					//}
	   				
	   				if (fileNameWithoutStyles.contains("Bold") == true)
	   				{
	   					bSupporBold = true;
	   				}
	   				
	   				if (fileNameWithoutStyles.contains("Italic") == true)
	   				{
	   					bSupporItalic = true;
	   				}
	   		   	}
	   		}
	   		
	   		checkBoxBold.setEnabled(bSupporBold);
	   		checkBoxItalic.setEnabled(bSupporItalic);
		}
	}
	
	@Override
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
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPrefs.edit();
		float fSpace = Float.parseFloat(editSpace.getText().toString());
		
		if (currentUnit == 1)//mm
		{
			fSpace = fSpace / 0.0393701f;
		}
		else
		if (currentUnit == 2)//cm
		{
			fSpace = fSpace / 0.393701f;
		}
		
		if (currentPath != null)
		{
			currentPath.setDegree(seekRotate.getProgress());
			String xInchStr = editX.getText().toString();
			String yInchStr = editY.getText().toString();
			String widthInchStr = editWidth.getText().toString();
			String heightInchStr = editHeight.getText().toString();
			
			currentPath.setText(editTextEnter.getText().toString());
			
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
			
			currentPath.setGlyphSpace(fSpace);
		}
		
		editor.putFloat("TextSpace", fSpace);
		editor.commit();
	}
	
	public void ApplySettings() 
	{
		int nRotate = seekRotate.getProgress();
		int bBold = checkBoxBold.isChecked() == true ? 1 : 0;
		int bItalic = checkBoxItalic.isChecked() == true ? 1 : 0;
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
    	editor.putLong("Rotate", nRotate);
    	editor.putInt("currentFontBold", bBold);
    	editor.putInt("currentFontItalic", bItalic);
    	
    	if (currentLine.isEmpty() == false)
    	{
    		String currentFontName = "";
    		
    		String []fontArrProperties = currentLine.split("\\|");
    		
    		if (fontArrProperties.length > 1)
    		{
    			String []fontArrTypes = fontArrProperties[1].split(",");
    			
    			if (fontArrTypes != null && fontArrTypes.length > 0)
        		{
    				currentFontName = fontArrTypes[0];
    				
					for(int m = 0; m < fontArrTypes.length; m++)
					{
						boolean hasBold = fontArrTypes[m].contains("Bold");
						boolean hasItalic = fontArrTypes[m].contains("Italic");
						boolean hasRegular = fontArrTypes[m].contains("Regular");
						
						if ((bBold > 0) == hasBold && (bItalic > 0) == hasItalic)
						{
							currentFontName = fontArrTypes[m];
							break;
						}
						
						if (bBold == 0 && bItalic == 0 && hasRegular)
						{
							currentFontName = fontArrTypes[m];
							break;
						}
					}
        		}
    		}
    			
    		editor.putString("currentFont", currentFontName);
    	}
    	
		editor.commit();
	}
	
	public void onApply(View v) {
		  
		ApplySettings();
		ApplyObject();
		
		mFinishInterface.onToolOptionApply();
    }
	
	public void onOK(View v) {
	   	
		ApplySettings();
		ApplyObject();
		
		mFinishInterface.onToolOptionFinish();
	   	 
		dismiss();
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		if (seekRotate == seekBar)
		{
			textRotateVal.setText(String.format(" %d°", seekRotate.getProgress()));
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
