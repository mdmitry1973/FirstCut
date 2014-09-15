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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsDialog extends Dialog  {
	
	SettingsDialog dialog;
	Context contextDialog;
	
	private Spinner spinnerUnit;
	private int unit;

	public SettingsDialog(Context context) {
		super(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		
		setContentView(R.layout.settings);
		setCanceledOnTouchOutside(false);
		
		dialog = this;
		contextDialog = context;
		
		spinnerUnit = (Spinner) findViewById(R.id.spinnerUnit);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		unit = sharedPrefs.getInt("unit", 0);
		
		spinnerUnit.setSelection(unit);
		
		Button saveButton = (Button) findViewById(R.id.buttonSave);
		
		saveButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(contextDialog);
	        	
	        	unit = spinnerUnit.getSelectedItemPosition();
	        	
	        	SharedPreferences.Editor editor = sharedPrefs.edit();
	        	editor.putInt("unit", unit);
	    		editor.commit();
	        	
	        	dialog.dismiss();
	        }
		});
		
		
		Button cancelButton = (Button) findViewById(R.id.buttonCancel);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	dialog.dismiss();
	        }
		});
	}

}
