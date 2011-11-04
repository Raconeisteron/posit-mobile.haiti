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

import org.hfoss.posit.android.R;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaUser.UserType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Allows the user either to create messages for different new beneficiaries
 * or for updated beneficiaries and no-shows following a distribution or displays
 * various category of message (sent, ack, pending, all) 
 * 
 */
public class SearchFilterActivity extends Activity implements OnClickListener {
	public static final String TAG = "AcdiVocaLookupActivity";
	public static final int ACTION_SELECT = 0;
	
	// NOTE: Activity_RESULT_CANCELED = 1
	public static final int RESULT_SELECT_ALL = 2;
	public static final int RESULT_SELECT_NEW = 3;
	public static final int RESULT_SELECT_UPDATE = 4;
	public static final int RESULT_BULK_UPDATE = 8;
	public static final int RESULT_SELECT_PENDING = 5;
	public static final int RESULT_SELECT_SENT = 6;
	public static final int RESULT_SELECT_ACKNOWLEDGED = 7;
	public static final String[] MESSAGE_STATUS_STRINGS = {"","","","NEW", "UPDATE", "PENDING", "SENT","ACKNOWLEDGED"};
	
	private String mUserMode;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 Log.i(TAG, "onCreate");	
		 mUserMode = (String) this.getIntent().getExtras().getString("user_mode");
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

		setContentView(R.layout.acdivoca_search_filter);  // Should be done after locale configuration
		
		Log.i(TAG, "User mode = " + mUserMode);
		if (AppControlManager.getUserType().equals(UserType.AGRON)){
			 ((RadioButton)findViewById(R.id.update_messages)).setVisibility(View.GONE);
			 ((RadioButton)findViewById(R.id.update_bulk_messages)).setVisibility(View.GONE);
		}
		
		((Button)findViewById(R.id.search_filter_select_button)).setOnClickListener(this);
		((Button)findViewById(R.id.cancel_select_filter_button)).setOnClickListener(this);

		// Listen for clicks on radio buttons
		 ((RadioButton)findViewById(R.id.new_messages)).setOnClickListener(this);
		 ((RadioButton)findViewById(R.id.update_messages)).setOnClickListener(this);
		 ((RadioButton)findViewById(R.id.update_bulk_messages)).setOnClickListener(this);
		 
		 if (!mUserMode.equals("USER")) {
			 ((RadioButton)findViewById(R.id.pending_messages)).setOnClickListener(this);
			 ((RadioButton)findViewById(R.id.sent_messages)).setOnClickListener(this);
			 ((RadioButton)findViewById(R.id.acknowledged_messages)).setOnClickListener(this);
			 ((RadioButton)findViewById(R.id.all_messages)).setOnClickListener(this);
		 }
		 else {
			 ((RadioButton)findViewById(R.id.pending_messages)).setVisibility(View.INVISIBLE);
			 ((RadioButton)findViewById(R.id.sent_messages)).setVisibility(View.INVISIBLE);
			 ((RadioButton)findViewById(R.id.acknowledged_messages)).setVisibility(View.INVISIBLE);
			 ((RadioButton)findViewById(R.id.all_messages)).setVisibility(View.INVISIBLE);

		 }

	}

	/**
	 * Required as part of OnClickListener interface. Handles button clicks.
	 */
	public void onClick(View v) {
		Log.i(TAG, "onClick");
	    Intent returnIntent = new Intent();
	    
		try {
			if (v.getClass().equals(Class.forName("android.widget.RadioButton"))) {
					//Toast.makeText(this, "RadioClicked", Toast.LENGTH_SHORT).show();
				((Button)findViewById(R.id.search_filter_select_button)).setEnabled(true);
				return;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		if (v.getId() == R.id.search_filter_select_button) {
			int result = selectRadioResult();
			setResult(result,returnIntent);
		} else {
			setResult(Activity.RESULT_CANCELED, returnIntent);
		}
	    finish();
	}
	
	/**
	 * Returns the user's selection.
	 * @return
	 */
	private int selectRadioResult() {
		int result = Activity.RESULT_OK;
		RadioButton rb = (RadioButton)findViewById(R.id.all_messages);
		if (rb.isChecked()) 
			result = RESULT_SELECT_ALL;
		rb = (RadioButton)findViewById(R.id.new_messages);
		if (rb.isChecked())
			result = RESULT_SELECT_NEW;
		rb = (RadioButton)findViewById(R.id.update_messages);
		if (rb.isChecked())
			result = RESULT_SELECT_UPDATE;
		rb = (RadioButton)findViewById(R.id.update_bulk_messages);
		if (rb.isChecked())
			result = RESULT_BULK_UPDATE;		
		rb = (RadioButton)findViewById(R.id.sent_messages);
		if (rb.isChecked())
			result = RESULT_SELECT_SENT;
		rb = (RadioButton)findViewById(R.id.pending_messages);
		if (rb.isChecked()) 
			result = RESULT_SELECT_PENDING;
		rb = (RadioButton)findViewById(R.id.acknowledged_messages);
		if (rb.isChecked())
			result = RESULT_SELECT_ACKNOWLEDGED;
		return result;

	}
}