/*
 * File: AcdiVocaFindActivity.java
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

import java.util.Calendar;
import org.hfoss.posit.android.R;
import org.hfoss.posit.android.api.FindActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker.OnDateChangedListener;

/**
 * Handles Finds for AcdiVoca Mobile App.
 * 
 */
public class AcdiVocaUpdateFindActivity extends FindActivity implements OnDateChangedListener, 
TextWatcher, OnItemSelectedListener { //, OnKeyListener {
	public static final String TAG = "AcdiVocaUpdateActivity";

	private static final int CONFIRM_EXIT = 0;
	private static final int WRONG_ID = 1;

	private static final int ACTION_ID = 0;

	private String mBeneficiaryId = "unknown";

	private boolean isProbablyEdited = false;   // Set to true if user edits a datum
	private String mAction = "";
	private int mFindId = 0;
	//private AcdiVocaDbHelper mDbHelper;
	private ContentValues mContentValues;
	private boolean inEditableMode = false;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Barcode scanner may pass in the beneficiary's Id
		Intent intent = getIntent();
		String id = intent.getStringExtra("Id");
		if (id != "unknown") {
			mBeneficiaryId = id;
		}	
		Log.i(TAG, "onCreate");
		isProbablyEdited = false;
	}

	/**
	 * Inflates the Apps menus from a resource file.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acdivoca_menu_add, menu);
		return true;
	}

	/**
	 * Localizes already created menu items.
	 */
	@Override	
	public boolean onPrepareOptionsMenu(Menu menu) {

		// Re-inflate to force localization.
		Log.i(TAG, "onPrepareOptionsMenu");
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acdivoca_menu_add, menu);

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Implements the requested action when user selects a menu item.
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(TAG, "onMenuItemSelected");
		return true;
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
		Log.i(TAG, "onResume beneficiary id = " + mBeneficiaryId);

		AcdiVocaLocaleManager.setDefaultLocale(this);  // Locale Manager should be in API

		Log.i(TAG, "Before edited = " + isProbablyEdited);

		if (mBeneficiaryId == "unknown") {
			Log.i(TAG, "UNKNOWN ID");
			Intent lookupIntent = new Intent();
			lookupIntent.setClass(this, AcdiVocaLookupActivity.class);
			this.startActivityForResult(lookupIntent, ACTION_ID);
		} else {

			//AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
			AcdiVocaFind avFind = this.getHelper().fetchBeneficiaryByDossier(mBeneficiaryId, null);
			if (avFind != null) {
				mContentValues = avFind.toContentValues();
				if (mContentValues == null) {
					Toast.makeText(this, getString(R.string.toast_no_beneficiary) + mBeneficiaryId, Toast.LENGTH_SHORT).show();
				} else {
					Log.i(TAG,mContentValues.toString());
					setContentView(R.layout.acdivoca_update_noedit);  // Should be done after locale configuration
					((Button)findViewById(R.id.update_to_db_button)).setOnClickListener(this);
					((Button)findViewById(R.id.update_edit_button)).setOnClickListener(this);

					displayContentUneditable(mContentValues);
					mFindId = mContentValues.getAsInteger(AcdiVocaDbHelper.FINDS_ORMLIST_ID);
				}
			} else {
				showDialog(WRONG_ID);
				//Toast.makeText(this, getString(R.string.toast_no_beneficiary) + mBeneficiaryId,  Toast.LENGTH_LONG).show();
				//finish();
			}
		}
	}

	/**
	 * Displays the content as uneditable labels -- default view.
	 * @param values
	 */
	private void displayContentUneditable(ContentValues values) {
		TextView tv = ((TextView) findViewById(R.id.dossier_label));
		tv.setText(getString(R.string.beneficiary_dossier) + " " + mBeneficiaryId);
		tv = ((TextView) findViewById(R.id.firstnameLabel));
		tv.setText(getString(R.string.firstname) + ": " 
				+  values.getAsString(AcdiVocaDbHelper.FINDS_FIRSTNAME));
		tv = ((TextView) findViewById(R.id.lastnameLabel));
		tv.setText(getString(R.string.lastname) + ": " 
				+  values.getAsString(AcdiVocaDbHelper.FINDS_LASTNAME));  	
		tv = ((TextView) findViewById(R.id.dobLabel));
		tv.setText(getString(R.string.dob) + ": " 
				+  values.getAsString(AcdiVocaDbHelper.FINDS_DOB));  
		tv = ((TextView) findViewById(R.id.sexLabel));
		tv.setText(getString(R.string.sex) + ": " 
				+  values.getAsString(AcdiVocaDbHelper.FINDS_SEX));  	
		tv = ((TextView) findViewById(R.id.categoryLabel));
		tv.setText(getString(R.string.Beneficiary_Category) + ": " 
				+  values.getAsString(AcdiVocaDbHelper.FINDS_BENEFICIARY_CATEGORY)); 

		displayUpdateQuestions(values);
	}

	/**
	 * The update questions are displayed regardless of whether the form is
	 * in editable or uneditable mode.
	 * @param contentValues
	 */
	private void displayUpdateQuestions(ContentValues contentValues) {
		((RadioButton) findViewById(R.id.radio_present_yes)).setOnClickListener(this);
		((RadioButton) findViewById(R.id.radio_present_no)).setOnClickListener(this);
		((RadioButton) findViewById(R.id.radio_change_in_status_yes)).setOnClickListener(this);
		((RadioButton) findViewById(R.id.radio_change_in_status_no)).setOnClickListener(this);

		RadioButton aRb = (RadioButton) findViewById(R.id.radio_present_yes);

		if (contentValues.getAsBoolean(AcdiVocaDbHelper.FINDS_Q_PRESENT) != null) {
			if (contentValues.getAsBoolean(AcdiVocaDbHelper.FINDS_Q_PRESENT))
				aRb.setChecked(true);
			aRb = (RadioButton) findViewById(R.id.radio_present_no);
			if (!contentValues.getAsBoolean(AcdiVocaDbHelper.FINDS_Q_PRESENT))
				aRb.setChecked(true);
		}
		//    	if (contentValues.getAsString(AcdiVocaDbHelper.FINDS_Q_PRESENT) != null) {
		//    		if (contentValues.getAsString(AcdiVocaDbHelper.FINDS_Q_PRESENT).equals(AcdiVocaDbHelper.FINDS_TRUE.toString()))
		//    			aRb.setChecked(true);
		//    		aRb = (RadioButton) findViewById(R.id.radio_present_no);
		//    		if (contentValues.getAsString(AcdiVocaDbHelper.FINDS_Q_PRESENT).equals(AcdiVocaDbHelper.FINDS_FALSE.toString()))
		//    			aRb.setChecked(true);
		//    	}

		//  New button - 6/17/11          

		Spinner spinner = (Spinner)findViewById(R.id.statuschangeSpinner);
		((Spinner)findViewById(R.id.statuschangeSpinner)).setOnItemSelectedListener(this);
		//    	AcdiVocaFindActivity.setSpinner(spinner, contentValues, AcdiVocaDbHelper.FINDS_CHANGE_TYPE);
		String selected = contentValues.getAsString(AcdiVocaDbHelper.FINDS_CHANGE_TYPE); 
		int k = 0;  //I was unable to use the spinner function here.
		if(selected != null){
			while (k < spinner.getCount()-1 && !(k == Integer.parseInt(selected))) {
				++k;			
			}
			if (k < spinner.getCount())
				spinner.setSelection(k);
			else
				spinner.setSelection(0);
		}
		else{
			spinner.setSelection(0);
		}

		aRb = (RadioButton) findViewById(R.id.radio_change_in_status_yes);

		if (contentValues.getAsString(AcdiVocaDbHelper.FINDS_Q_CHANGE) != null){

			if (contentValues.getAsBoolean(AcdiVocaDbHelper.FINDS_Q_CHANGE)){
				findViewById(R.id.statuschange).setVisibility(View.VISIBLE);
				findViewById(R.id.statuschangeSpinner).setVisibility(View.VISIBLE);
				aRb.setChecked(true);
			}

			aRb = (RadioButton) findViewById(R.id.radio_change_in_status_no);

			if (!contentValues.getAsBoolean(AcdiVocaDbHelper.FINDS_Q_CHANGE)){
				findViewById(R.id.statuschange).setVisibility(View.GONE);
				findViewById(R.id.statuschangeSpinner).setVisibility(View.GONE);
				aRb.setChecked(true);
			}
		}
	}

	/**
	 * Called when the "edit" button is clicked to change the view to 
	 * an editable view. 
	 */
	private void setUpEditableView() {
		setContentView(R.layout.acdivoca_update);  // Should be done after locale configuration
		displayContentInView(mContentValues);    	
		((Button)findViewById(R.id.update_to_db_button)).setOnClickListener(this);
		((Button)findViewById(R.id.update_to_db_button)).setOnClickListener(this);

		TextView tv = ((TextView) findViewById(R.id.dossier_label));
		tv.setText(getString(R.string.beneficiary_dossier) + mBeneficiaryId);

		// Listen for text changes in edit texts and set the isEdited flag
		((EditText)findViewById(R.id.firstnameEdit)).addTextChangedListener(this);
		((EditText)findViewById(R.id.lastnameEdit)).addTextChangedListener(this);
		//    	((EditText)findViewById(R.id.monthsInProgramEdit)).addTextChangedListener(this);

		// Initialize the DatePicker and listen for changes
		Calendar calendar = Calendar.getInstance();

		((DatePicker)findViewById(R.id.datepicker)).init(
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), 
				calendar.get(Calendar.DAY_OF_MONTH), this);

		// Listen for clicks on radio buttons
		((RadioButton)findViewById(R.id.femaleRadio)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.maleRadio)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.malnourishedRadio)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.inpreventionRadio)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.expectingRadio)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.nursingRadio)).setOnClickListener(this);

		////New Listeners - 6/17/11
		//      
		((RadioButton)findViewById(R.id.radio_present_yes)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.radio_present_no)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.radio_change_in_status_yes)).setOnClickListener(this);
		((RadioButton)findViewById(R.id.radio_change_in_status_no)).setOnClickListener(this);
		((Spinner)findViewById(R.id.statuschangeSpinner)).setOnItemSelectedListener(this);

		final Intent intent = getIntent();
		mAction = intent.getAction();
		Log.i(TAG, "mAction = " + mAction);
		if (this.inEditableMode ||  mAction.equals(Intent.ACTION_EDIT)) {
			doEditAction();
			isProbablyEdited = false; // In EDIT mode, initialize after filling in data
		}

	}


	/**
	 * Returns the result of the Lookup Activity, which gets the beneficiary Id.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult");
		switch(requestCode) {
		case ACTION_ID:
			if (resultCode == RESULT_OK) {
				mBeneficiaryId = data.getStringExtra("Id");
				Log.i(TAG, "Set beneficiaryId = " + mBeneficiaryId);
				// Toast.makeText(this, "Beneficiary Id = " + " " + beneficiaryId, Toast.LENGTH_LONG).show();
				break;
			}
			else {
				Log.i(TAG, "Result NOT Ok, finishing");
				finish();
			}
		}   
	}


	/**
	 * Allows editing of editable data for existing finds.  For existing finds, 
	 * we retrieve the Find's data from the DB and display it in a TextView. The
	 * Find's location and time stamp are not updated.
	 */    
	private void doEditAction() {
		Log.i(TAG, "doEditAction");
		//AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
		AcdiVocaFind avFind = this.getHelper().fetchBeneficiaryByDossier(mBeneficiaryId, null);
		ContentValues values = avFind.toContentValues();

		Log.i(TAG, "################  Value of FindsID " + values.getAsString(AcdiVocaDbHelper.FINDS_ID));
		//        mFindId = (int) getIntent().getLongExtra(values.getAsString(AcdiVocaDbHelper.FINDS_ID), 0);   
		mFindId = values.getAsInteger(AcdiVocaDbHelper.FINDS_ORMLIST_ID);   
		//        mFindId = Integer.parseInt(values.getAsString(AcdiVocaDbHelper.FINDS_ID));

		Log.i(TAG, "################  If the id is 0 something is wrong: " + mFindId);
		//        Log.i(TAG,"FINDS_ID = " + AcdiVocaDbHelper.FINDS_ID);
		//        mFindId = (int) getIntent().getLongExtra(AcdiVocaDbHelper.FINDS_ID, 0); //Getting default from here  
		//        Integer.parseInt(db.AcdiVocaDbHelper.FINDS_ID);
		//        mFindId = ben;
		//        mFindId = Integer.parseInt(AcdiVocaDbHelper.FINDS_ID.substring(AcdiVocaDbHelper.FINDS_ID.indexOf(","))); //WARNING: USED TO BE LONG CAST INTO INT ,AcdiVocaDbHelper.FINDS_ID.lastIndexOf(" ")
		//        Log.i(TAG,"Find id = " + mFindId);
		//        AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
		//        Log.i(TAG, db.fetchFindDataById(mFindId, null).toString()); //Need correct ID.
		//        ContentValues values = db.fetchFindDataById(mFindId, null);

		//        ContentValues values = (AcdiVocaFindDataManager.getInstance()).fetchFindDataById(this, mFindId, null);
		Log.i(TAG,"ContentValues has become");
		Log.i(TAG,values.toString());
		displayContentInView(values);                        
	}

	/**
	 * Retrieves values from the View fields and stores them as <key,value> pairs in a ContentValues.
	 * This method is invoked from the Save menu item.  It also marks the find 'unsynced'
	 * so it will be updated to the server.
	 * @return The ContentValues hash table.
	 */
	private ContentValues retrieveContentFromView() {
		Log.i(TAG, "retrieveContentFromView");
		ContentValues result = new ContentValues();

		if (inEditableMode) {
			EditText eText = (EditText) findViewById(R.id.lastnameEdit);


			String value = eText.getText().toString();
			result.put(AcdiVocaDbHelper.FINDS_LASTNAME, value);
			Log.i(TAG, "retrieve LAST NAME = " + value);

			eText = (EditText)findViewById(R.id.firstnameEdit);
			value = eText.getText().toString();
			result.put(AcdiVocaDbHelper.FINDS_FIRSTNAME, value);

			//    		eText = (EditText)findViewById(R.id.monthsInProgramEdit);
			//    		value = eText.getText().toString();
			//    		result.put(AcdiVocaDbHelper.FINDS_MONTHS_REMAINING, value);

			//value = mMonth + "/" + mDay + "/" + mYear;
			DatePicker picker = ((DatePicker)findViewById(R.id.datepicker));
			value = picker.getYear() + "/" + picker.getMonth() + "/" + picker.getDayOfMonth();
			Log.i(TAG, "Date = " + value);
			result.put(AcdiVocaDbHelper.FINDS_DOB, value);

			RadioButton sexRB = (RadioButton)findViewById(R.id.femaleRadio);
			String sex = "";
			if (sexRB.isChecked()) 
				sex = AcdiVocaDbHelper.FINDS_FEMALE;
			sexRB = (RadioButton)findViewById(R.id.maleRadio);
			if (sexRB.isChecked()) 
				sex = AcdiVocaDbHelper.FINDS_MALE;
			result.put(AcdiVocaDbHelper.FINDS_SEX, sex);     

			// Set the Beneficiary's category (4 exclusive radio buttons)
			String category = "";
			RadioButton rb = (RadioButton)findViewById(R.id.malnourishedRadio);
			if (rb.isChecked()) 
				category = AcdiVocaDbHelper.FINDS_MALNOURISHED;
			rb = (RadioButton)findViewById(R.id.inpreventionRadio);
			if (rb.isChecked())
				category = AcdiVocaDbHelper.FINDS_PREVENTION;

			rb = (RadioButton)findViewById(R.id.expectingRadio);
			if (rb.isChecked()) 
				category = AcdiVocaDbHelper.FINDS_EXPECTING;
			rb = (RadioButton)findViewById(R.id.nursingRadio);
			if (rb.isChecked())
				category = AcdiVocaDbHelper.FINDS_NURSING;
			result.put(AcdiVocaDbHelper.FINDS_BENEFICIARY_CATEGORY, category);   

		}

		RadioButton presentRB = (RadioButton)findViewById(R.id.radio_present_yes);
		String present = "";
		if (presentRB.isChecked()) 
			present = AcdiVocaDbHelper.FINDS_TRUE;
		presentRB = (RadioButton)findViewById(R.id.radio_present_no);
		if (presentRB.isChecked()) 
			present = AcdiVocaDbHelper.FINDS_FALSE;
		result.put(AcdiVocaDbHelper.FINDS_Q_PRESENT, present); 

		RadioButton changeRB = (RadioButton)findViewById(R.id.radio_change_in_status_yes);
		String change = "";
		if (changeRB.isChecked()) 
			change = AcdiVocaDbHelper.FINDS_TRUE;
		changeRB = (RadioButton)findViewById(R.id.radio_change_in_status_no);
		if (changeRB.isChecked()) 
			change = AcdiVocaDbHelper.FINDS_FALSE;
		result.put(AcdiVocaDbHelper.FINDS_Q_CHANGE, change); 


		int spinnerInt;
		Spinner spinner = (Spinner)findViewById(R.id.statuschangeSpinner);


		if (spinner != null) {
			spinnerInt = spinner.getSelectedItemPosition();
			result.put(AcdiVocaDbHelper.FINDS_CHANGE_TYPE, String.valueOf(spinnerInt));
		}

		// Retrieving COMMUNE SECTION from Spinner    
		if (result.getAsString(AcdiVocaDbHelper.FINDS_CHANGE_TYPE).equals(
				AttributeManager.TRANSFER_TYPES.get(AttributeManager.ABBREV_Q_TRANSFER_LOCATION).toString())) {
			String communeId = "";
			spinner = (Spinner) findViewById(R.id.communeSectionSpinner);
			if (spinner != null) {
				communeId = Integer.toString(spinner.getSelectedItemPosition());
				result.put(AcdiVocaDbHelper.FINDS_COMMUNE_SECTION, communeId);
			}
			EditText addressEditText = (EditText) findViewById(R.id.addressEdit);
			if (addressEditText != null) {
				if (!addressEditText.getText().toString().equals(""))
					result.put(AcdiVocaDbHelper.FINDS_ADDRESS, addressEditText.getText().toString());
			}
			//		} else {
			//			result.put(AcdiVocaDbHelper.FINDS_COMMUNE_SECTION, AttributeManager.COMMUNE_SECTION_NOT_CHANGED);
		}

		EditText otherEditText = (EditText) findViewById(R.id.otherReason);
		if (otherEditText != null) {
			result.put(AcdiVocaDbHelper.FINDS_OTHER_TRANSFER, otherEditText.getText().toString());
		}
		if (result.getAsString(AcdiVocaDbHelper.FINDS_CHANGE_TYPE).equals(
				AttributeManager.TRANSFER_TYPES.get(AttributeManager.ABBREV_Q_TRANSFER_LACTATE).toString())) {
			EditText babyFirstName = (EditText) findViewById(R.id.babyFirstName);
			if (babyFirstName != null) {
				if (!babyFirstName.getText().toString().equals("")) {
					result.put(AcdiVocaDbHelper.FINDS_FIRSTNAME, babyFirstName.getText().toString());
				}
			}
			EditText babyLastName = (EditText) findViewById(R.id.babyLastName);
			if (babyLastName != null) {
				if (!babyLastName.getText().toString().equals(""))
					result.put(AcdiVocaDbHelper.FINDS_LASTNAME, babyLastName.getText().toString());
			}
		}

		return result;
	}

	/**
	 * Displays values from a ContentValues in the View.
	 * @param contentValues stores <key, value> pairs
	 */
	private void displayContentInView(ContentValues contentValues) {
		Log.i(TAG, "displayContentInView");
		EditText eText = (EditText) findViewById(R.id.lastnameEdit);
		eText.setText(contentValues.getAsString(AcdiVocaDbHelper.FINDS_LASTNAME));

		eText = (EditText) findViewById(R.id.firstnameEdit);
		eText.setText(contentValues.getAsString(AcdiVocaDbHelper.FINDS_FIRSTNAME));
		Log.i(TAG,"display First Name = " + contentValues.getAsString(AcdiVocaDbHelper.FINDS_FIRSTNAME));

		DatePicker dp = (DatePicker) findViewById(R.id.datepicker);
		String date = contentValues.getAsString(AcdiVocaDbHelper.FINDS_DOB);
		int yr=0, mon=0, day=0;
		day = Integer.parseInt(date.substring(date.lastIndexOf("/")+1));
		yr = Integer.parseInt(date.substring(0,date.indexOf("/")));
		mon = Integer.parseInt(date.substring(date.indexOf("/")+1,date.lastIndexOf("/")));

		try {
			if (date != null) {
				Log.i(TAG,"display DOB = " + date);
				dp.init(yr, mon, day, this);
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Illegal Argument, probably month == 12 in " + date);
			e.printStackTrace();
		}

		//        eText = (EditText)findViewById(R.id.monthsInProgramEdit);
		//        eText.setText(contentValues.getAsString(AcdiVocaDbHelper.FINDS_MONTHS_REMAINING));


		// Chris - 6/9/11 - Filling the form            

		RadioButton sexRB = (RadioButton)findViewById(R.id.maleRadio);
		Log.i(TAG, "sex=" + contentValues.getAsString(AcdiVocaDbHelper.FINDS_SEX));
		if (contentValues.getAsString(AcdiVocaDbHelper.FINDS_SEX).equals(AcdiVocaDbHelper.FINDS_MALE.toString()))
			sexRB.setChecked(true);
		sexRB = (RadioButton)findViewById(R.id.femaleRadio);
		if (contentValues.getAsString(AcdiVocaDbHelper.FINDS_SEX).equals(AcdiVocaDbHelper.FINDS_FEMALE.toString())){
			sexRB.setChecked(true);
		}

		RadioButton motherRB = (RadioButton) findViewById(R.id.expectingRadio);
		String cat = contentValues.getAsString(AcdiVocaDbHelper.FINDS_BENEFICIARY_CATEGORY);
		if (cat != null){
			if (cat.equals(AcdiVocaDbHelper.FINDS_EXPECTING.toString()))
				motherRB.setChecked(true);
			else
				motherRB.setChecked(false);
			motherRB = (RadioButton)findViewById(R.id.nursingRadio);
			if (cat.equals(AcdiVocaDbHelper.FINDS_NURSING.toString()))
				motherRB.setChecked(true);
			else
				motherRB.setChecked(false);
			RadioButton infantRB = (RadioButton) findViewById(R.id.malnourishedRadio);
			if (cat.equals(AcdiVocaDbHelper.FINDS_MALNOURISHED.toString()))
				infantRB.setChecked(true);
			else
				motherRB.setChecked(false);
			infantRB = (RadioButton)findViewById(R.id.inpreventionRadio);
			if (cat.equals(AcdiVocaDbHelper.FINDS_PREVENTION.toString()))
				infantRB.setChecked(true);
			else
				motherRB.setChecked(false);
		}
		else{
			motherRB.setChecked(false);
			motherRB = (RadioButton)findViewById(R.id.nursingRadio);
			motherRB.setChecked(false);
			motherRB = (RadioButton)findViewById(R.id.malnourishedRadio);
			motherRB.setChecked(false);
			motherRB = (RadioButton)findViewById(R.id.inpreventionRadio);
			motherRB.setChecked(false);
		}

		displayUpdateQuestions(contentValues);
	}

	/**
	 * Required as part of OnClickListener interface. Handles button clicks.
	 */
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		// If a RadioButton was clicked, mark the form as edited.
		//Toast.makeText(this, "Clicked on a " + v.getClass().toString(), Toast.LENGTH_SHORT).show();
		try {
			if (v.getClass().equals(Class.forName("android.widget.RadioButton"))) {
				//Toast.makeText(this, "RadioClicked", Toast.LENGTH_SHORT).show();
				isProbablyEdited = true;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int id = v.getId();
		if (id == R.id.datepicker) 
			isProbablyEdited = true;

		//New code for the spinner - 9/17/11        

		if (id == R.id.radio_change_in_status_yes){
			findViewById(R.id.statuschange).setVisibility(View.VISIBLE);
			findViewById(R.id.statuschangeSpinner).setVisibility(View.VISIBLE);	
		}

		if (id == R.id.radio_change_in_status_no){
			findViewById(R.id.statuschange).setVisibility(View.GONE);
			findViewById(R.id.statuschangeSpinner).setVisibility(View.GONE);	
		}

		if (v.getId() == R.id.update_edit_button) {
			inEditableMode = true;
			setUpEditableView();
		}

		if(v.getId()==R.id.update_to_db_button) {
			boolean result = false;
			//           Toast.makeText(this, "Saving update to DB", Toast.LENGTH_SHORT).show();

			ContentValues data = this.retrieveContentFromView(); 
			Log.i(TAG, "Retrieved = " + data.toString());

			//AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);

			AcdiVocaFind avFind = this.getHelper().fetchBeneficiaryByDossier(mBeneficiaryId, null);
			avFind.update(data);
			result = this.getHelper().updateBeneficiary(avFind);

			//    		result = AcdiVocaFindDataManager.getInstance().updateFind(this, mFindId, data);
			Log.i(TAG, "Update to Db is " + result);
			if (result){
				//   			Toast.makeText(this, "Find saved to Db " + data.toString(), Toast.LENGTH_SHORT).show();
			}

			else 
				Toast.makeText(this, getString(R.string.toast_db_error), Toast.LENGTH_SHORT).show();

			finish();
			//return;
		}
	}


	/**
	 * Intercepts the back key (KEYCODE_BACK) and displays a confirmation dialog
	 * when the user tries to exit POSIT.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onKeyDown keyCode = " + keyCode);
		if(keyCode==KeyEvent.KEYCODE_BACK && isProbablyEdited){  // 
			Toast.makeText(this, getString(R.string.toast_Backkey_isEdited) +  isProbablyEdited, Toast.LENGTH_SHORT).show();




			showDialog(CONFIRM_EXIT);
			return true;
		}
		Log.i("code", keyCode+"");
		return super.onKeyDown(keyCode, event);
	}


	/**
	 * Creates a dialog to confirm that the user wants to exit POSIT.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i(TAG, "onCreateDialog");
		switch (id) {
		case CONFIRM_EXIT:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(R.string.acdivoca_exit_findactivity)
					.setPositiveButton(R.string.Yes,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							finish();
						}
					}).setNegativeButton(R.string.acdivoca_cancel,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							/* User clicked Cancel so do nothing */
						}
					}).create();
		case WRONG_ID:
			String title = getString(R.string.toast_no_beneficiary)+ mBeneficiaryId;
			return new AlertDialog.Builder(this).setTitle(title)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					}).create();	

		default:
			return null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		AlertDialog d = (AlertDialog) dialog;
		Button needsabutton;
		switch (id) {
		case CONFIRM_EXIT:
			d.setTitle(R.string.acdivoca_exit_findactivity);

			needsabutton = d.getButton(DialogInterface.BUTTON_POSITIVE);
			needsabutton.setText(R.string.Yes);
			needsabutton.invalidate();

			needsabutton = d.getButton(DialogInterface.BUTTON_NEGATIVE);
			needsabutton.setText(R.string.acdivoca_cancel);
			needsabutton.invalidate();

			break;
		}
	}


	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Log.i(TAG, "onDateChanged");
		isProbablyEdited = true;
	}

	//  The remaining methods are part of unused interfaces inherited from the super class.
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { }
	public void onLocationChanged(Location location) {    }
	public void onProviderDisabled(String provider) {    }
	public void onProviderEnabled(String provider) {    }
	public void onStatusChanged(String provider, int status, Bundle extras) {    }


	/**
	 * Sets the 'edited' flag if text has been changed in an EditText
	 */
	public void afterTextChanged(Editable arg0) {
		Log.i(TAG, "afterTextChanged " + arg0.toString());
		isProbablyEdited = true;
		// TODO Auto-generated method stub

	}

	// Unused
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	// Unused
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		Log.i(TAG, "onTextChanged " + arg0.toString());    
	}



	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(TAG, "onItemSelected = " + position);
		if (findViewById(R.id.otherReason) != null)
			findViewById(R.id.otherReason).setVisibility(View.GONE);
		if (findViewById(R.id.otherReason) != null)
			findViewById(R.id.babyFirstName).setVisibility(View.GONE);
		if (findViewById(R.id.otherReason) != null)
			findViewById(R.id.babyLastName).setVisibility(View.GONE);
		if (findViewById(R.id.otherReason) != null)
			findViewById(R.id.communeSectionSpinner).setVisibility(View.GONE);
		if (findViewById(R.id.otherReason) != null)
			findViewById(R.id.addressEdit).setVisibility(View.GONE);

		switch (position) {
		case 0: // Transfer pregnant to lactating
			findViewById(R.id.babyFirstName).setVisibility(View.VISIBLE);
			findViewById(R.id.babyLastName).setVisibility(View.VISIBLE);
			break;
		case 2: // Change of location
			findViewById(R.id.communeSectionSpinner).setVisibility(View.VISIBLE);
			findViewById(R.id.addressEdit).setVisibility(View.VISIBLE);
			break;
		case 8:  // "Other" selected
			findViewById(R.id.otherReason).setVisibility(View.VISIBLE);
			break;
		}

		//isProbablyEdited = true;
	}

	// Unused
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onNothingSelected = " + arg0);

	}


	/**
	 * Handles the back key
	 * Had to write this function because, two layoutViews are using the same screen
	 */
	@Override
	public void onBackPressed() {
		if(inEditableMode == false){
			finish();
		}
		else{
			AcdiVocaFind avFind = this.getHelper().fetchBeneficiaryByDossier(mBeneficiaryId, null);
			if (avFind != null) {
				mContentValues = avFind.toContentValues();
				if (mContentValues == null) {
					Toast.makeText(this, getString(R.string.toast_no_beneficiary) + mBeneficiaryId, Toast.LENGTH_SHORT).show();
				} else {
					Log.i(TAG,mContentValues.toString());
					setContentView(R.layout.acdivoca_update_noedit);  // Should be done after locale configuration
					((Button)findViewById(R.id.update_to_db_button)).setOnClickListener(this);
					((Button)findViewById(R.id.update_edit_button)).setOnClickListener(this);
					displayContentUneditable(mContentValues);
					mFindId = mContentValues.getAsInteger(AcdiVocaDbHelper.FINDS_ORMLIST_ID);
					inEditableMode = false;

				}
			}

		}
	}

}
