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

public class SettingsDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {
	
	SettingsDialog dialog;
	Context contextDialog;
	
	private Spinner spinnerUnit;
	private SeekBar seekBarTransparent;
	private TextView textTransparentVal;
	private int unit;

	public SettingsDialog(Context context) {
		super(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		
		setContentView(R.layout.settings);
		setCanceledOnTouchOutside(false);
		
		dialog = this;
		contextDialog = context;
		
		spinnerUnit = (Spinner) findViewById(R.id.spinnerUnit);
		seekBarTransparent = (SeekBar) findViewById(R.id.seekBarTransparent);
		textTransparentVal = (TextView) findViewById(R.id.textViewTranVal);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		unit = sharedPrefs.getInt("unit", 0);
		
		spinnerUnit.setSelection(unit);
		seekBarTransparent.setMax(100);
		seekBarTransparent.setProgress(sharedPrefs.getInt("TransparentToolOption", 50));
		textTransparentVal.setText(String.format(" %d %%", seekBarTransparent.getProgress()));
		
		seekBarTransparent.setOnSeekBarChangeListener(this);
		
		Button saveButton = (Button) findViewById(R.id.buttonSave);
		
		saveButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	
	        	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(contextDialog);
	        	
	        	unit = spinnerUnit.getSelectedItemPosition();
	        	
	        	SharedPreferences.Editor editor = sharedPrefs.edit();
	        	editor.putInt("unit", unit);
	        	editor.putInt("TransparentToolOption", seekBarTransparent.getProgress());
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

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		textTransparentVal.setText(String.format(" %d %%", seekBarTransparent.getProgress()));
		
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
