/*
 * File: SearchFilterActivity.java
 * 
 * Copyright (C) 2011 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of the ACDI/VOCA plugin for POSIT, Portable Open Search 
 * and Identification Tool.
 *
 * This plugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit.android.plugin.acdivoca;

import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaUser.UserType;
import org.hfoss.posit.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * Allows the user either to create messages for different new beneficiaries
 * or for updated beneficiaries and no-shows following a distribution or displays
 * various category of message (sent, ack, pending, all) 
 * 
 */
public class SearchFindsActivity extends Activity implements OnClickListener, TextWatcher {
	public static final String TAG = "AcdiVocaLookupActivity";
	public static final int ACTION_SEARCH = 1;
	
	// NOTE: Activity_RESULT_CANCELED = 1
	public static final int RESULT_SEARCH_LASTNAME = 2;
	public static final String LAST_NAME = "lastname";
	public static final String FIRST_NAME = "firstname";
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 Log.i(TAG, "onCreate");	
	}


	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
	}

	/**
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		
		AcdiVocaLocaleManager.setDefaultLocale(this);  // Locale Manager should be in API

		setContentView(R.layout.acdivoca_search_finds);  // Should be done after locale configuration
				
		((Button)findViewById(R.id.search_finds_ok_button)).setOnClickListener(this);
		((Button)findViewById(R.id.search_finds_cancel_button)).setOnClickListener(this);
		((EditText)findViewById(R.id.searchString)).addTextChangedListener(this);
	}

	/**
	 * Required as part of OnClickListener interface. Handles button clicks.
	 */
	public void onClick(View v) {
		Log.i(TAG, "onClick");
	    Intent returnIntent = new Intent();
	
		if (v.getId() == R.id.search_finds_ok_button) {
			EditText tv = (EditText)findViewById(R.id.searchString);
			String searchStr = tv.getText().toString();
			tv = (EditText)findViewById(R.id.searchString2);
			String searchStr2 = tv.getText().toString();
			if(!searchStr2.equals("")){								// 7/25/11
				returnIntent.putExtra(LAST_NAME, searchStr + "%");
				returnIntent.putExtra(FIRST_NAME, searchStr2 + "%");
				setResult(Activity.RESULT_OK,returnIntent);
			}
			else{
				returnIntent.putExtra(LAST_NAME, searchStr + "%");
				setResult(Activity.RESULT_OK,returnIntent);
			}
		} else {
			setResult(Activity.RESULT_CANCELED, returnIntent);
		}
	    finish();
	}


	public void afterTextChanged(Editable s) {
		((Button)findViewById(R.id.search_finds_ok_button)).setEnabled(true);
	}


	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}


	public void onTextChanged(CharSequence s, int start, int before, int count) {
		((Button)findViewById(R.id.search_finds_ok_button)).setEnabled(true);
	}

}