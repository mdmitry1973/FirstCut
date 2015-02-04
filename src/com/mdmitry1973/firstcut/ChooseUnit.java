package com.mdmitry1973.firstcut;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ChooseUnit extends Dialog implements OnItemSelectedListener  {
	
	private Spinner spinnerUnit;
	private int unit;
	private boolean unitInit = false;
	private ChooseUnitInterface deligate = null;
	
	public void SetChooseUnitInterface(ChooseUnitInterface deligate)
	{
		this.deligate = deligate;
	}

	public ChooseUnit(Context context) {
		super(context);
		
		setContentView(R.layout.choose_unit);
		
		spinnerUnit = (Spinner) findViewById(R.id.spinnerUnit);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		unit = sharedPrefs.getInt("unit", 0);
		
		spinnerUnit.setSelection(unit);
		spinnerUnit.setOnItemSelectedListener(this);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
	{
		if (unitInit == true)
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	    	
	    	SharedPreferences.Editor editor = sharedPrefs.edit();
	    	editor.putInt("unit", pos);
			editor.commit();
			
			if (deligate != null)
			{
				deligate.UnitChanged();
			}
			 
			dismiss();
		}
		
		unitInit = true;
    }

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

}
