package com.mdmitry1973.firstcut;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ToolsDialog extends Dialog  {
	
	ToolsDialog dialog;
	
	private ToolsDialogInterface deligate = null;
	
	public void SetToolInterface(ToolsDialogInterface deligate)
	{
		this.deligate = deligate;
	}
	
	public ToolsDialog(Context context) {
		super(context);
		
		setContentView(R.layout.tools);
		setCanceledOnTouchOutside(false);
		
		dialog = this;
		
		ImageButton imageButtonLine = (ImageButton) findViewById(R.id.imageButtonLine);
		
		imageButtonLine.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Line);
	        	dialog.dismiss();
	        }
		});
		
		
		ImageButton imageButtonPen = (ImageButton) findViewById(R.id.imageButtonPen);
		
		imageButtonPen.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	deligate.ToolsChanged(MainActivity.ToolType.Pen);
	        	dialog.dismiss();
	        }
		});
	}

}
