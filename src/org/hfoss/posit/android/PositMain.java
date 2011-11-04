/*
 * File: PositMain.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
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
package org.hfoss.posit.android;


import java.sql.SQLException;
import java.util.List;

import org.hfoss.posit.android.api.FindActivityProvider;
import org.hfoss.posit.android.api.FindPluginManager;
import org.hfoss.posit.android.api.SettingsActivity;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaAdminActivity;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaDbHelper;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaListFindsActivity;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaLocaleManager;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaSmsManager;
import org.hfoss.posit.android.plugin.acdivoca.AttributeManager;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaUser;
import org.hfoss.posit.android.plugin.acdivoca.LoginActivity;
import org.hfoss.posit.android.plugin.acdivoca.AppControlManager;

import org.hfoss.posit.android.plugin.acdivoca.SearchFilterActivity;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Implements the main activity and the main screen for the POSIT application.
 */
public class PositMain  extends OrmLiteBaseActivity<AcdiVocaDbHelper> implements android.view.View.OnClickListener {

//extends Activity implements OnClickListener { //,RWGConstants {

	private static final String TAG = "PositMain";

	private static final int CONFIRM_EXIT = 0;

	public static final int LOGIN_CANCELED = 3;
	public static final int LOGIN_SUCCESSFUL = 4;


	private SharedPreferences mSharedPrefs;
	private Editor mSpEditor;

	
	/**
	 * Called when the activity is first created. Sets the UI layout, adds the
	 * buttons, checks whether the phone is registered with a POSIT server.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG,"Creating");

		// Initialize plugins and managers
		FindPluginManager.initInstance(this);
		AcdiVocaSmsManager.initInstance(this);
		AttributeManager.init();

		// A newly installed POSIT should have no shared prefs. Set the default phone pref if
		// it is not already set.
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			String phone = mSharedPrefs.getString(getString(R.string.smsPhoneKey),"");
			if (phone.equals("")) {
				mSpEditor = mSharedPrefs.edit();
				mSpEditor.putString(getString(R.string.smsPhoneKey), getString(R.string.default_phone));
				mSpEditor.commit();
			}
			Log.i(TAG, "Preferences= " + mSharedPrefs.getAll().toString());
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		
//		// get our dao
//		Dao<AcdiVocaUser, Integer> avUserDao = null;
//		try {
//			avUserDao = getHelper().getAvUserDao();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// Query for all of the data objects in the database
//		List<AcdiVocaUser> list = null;
//		try {
//			list = avUserDao.queryForAll();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// our string builder for building the content-view
//		for (AcdiVocaUser item : list) {
//			Log.i(TAG, "User= " +  item.toString());
//		}
		
		// Run login activity, if necessary
		
		Intent intent = new Intent();
		Class<Activity> loginActivity = FindActivityProvider.getLoginActivityClass();
		if (loginActivity != null) {
			intent.setClass(this, loginActivity);
			intent.putExtra(AcdiVocaUser.USER_TYPE_STRING, AcdiVocaUser.UserType.USER.ordinal());
			this.startActivityForResult(intent, LoginActivity.ACTION_LOGIN);
		}
	}


	/**
	 * When POSIT starts it should either display a Registration View, if the 
	 * phone is not registered with a POSIT server, or it should display the 
	 * main View (ListFinds, AddFinds).  This helper method is called in various
	 * places in the Android, including in onCreate() and onRestart(). 
	 */
	private void startPOSIT() {

			setContentView(R.layout.main);
			
			// Change visibility of buttons based on UserType

			Log.i(TAG, "POSIT Start, distrStage = " + AppControlManager.displayDistributionStage(this));
						
			if (FindPluginManager.mMainIcon != null) {
				final ImageView mainLogo = (ImageView) findViewById(R.id.Logo);
				int resID = getResources().getIdentifier(FindPluginManager.mMainIcon, "drawable", "org.hfoss.posit.android");
				mainLogo.setImageResource(resID);
			}
			
			// New Beneficiary button
			if (FindPluginManager.mAddButtonLabel != null) {
				final Button addFindButton = (Button)findViewById(R.id.addFindButton);
				int resid = this.getResources().getIdentifier(FindPluginManager.mAddButtonLabel, "string", "org.hfoss.posit.android");

				
				if (addFindButton != null) {
					addFindButton.setText(resid);
					addFindButton.setOnClickListener(this);
				}
				
				// Button is gone for AGRI and AGRON users and for USER users during distribution events
				if ( (AppControlManager.isAgriUser()  || AppControlManager.isAgronUser())
						|| (AppControlManager.isRegularUser() && AppControlManager.isDuringDistributionEvent())) {
					addFindButton.setVisibility(View.GONE);
				} else {
					addFindButton.setVisibility(View.VISIBLE);
				}
			}

			// Send messages button
			if (FindPluginManager.mListButtonLabel != null) {
				final Button listFindButton = (Button) findViewById(R.id.listFindButton);
				int resid = this.getResources().getIdentifier(FindPluginManager.mListButtonLabel, "string", "org.hfoss.posit.android");
				if (listFindButton != null) {
					listFindButton.setText(resid);
					listFindButton.setOnClickListener(this);
				}
				
				// Button is gone for USER and AGRI users during distribution events
				if (AppControlManager.isDuringDistributionEvent()  
						&& (AppControlManager.isRegularUser() 
								|| AppControlManager.isAgriUser() 
								|| AppControlManager.isAgronUser() )) {
					listFindButton.setVisibility(View.GONE);
				} else {
					listFindButton.setVisibility(View.VISIBLE);
				}
			}
			
			// Update button -- used during Distribution events
			if (FindPluginManager.mExtraButtonLabel != null) {
				final Button extraButton = (Button) findViewById(R.id.extraButton);
				int resid = this.getResources().getIdentifier(FindPluginManager.mExtraButtonLabel, "string", "org.hfoss.posit.android");
				if (extraButton != null) {
					extraButton.setOnClickListener(this);
					extraButton.setText(resid);
					extraButton.setVisibility(View.VISIBLE);
				}
				
				// Button is gone for USER and ADMIN users except during distribution events
				if (AppControlManager.isRegularUser() || AppControlManager.isAdminUser()) {
					if (AppControlManager.isDuringDistributionEvent()) 
						extraButton.setVisibility(View.VISIBLE);
					else
						extraButton.setVisibility(View.GONE);
					
					// Enable the Button only if the event is started
					if (AppControlManager.isDistributionStarted())
						extraButton.setEnabled(true);
					else
						extraButton.setEnabled(false);
				} else if (AppControlManager.isAgriUser() || AppControlManager.isAgronUser())
					extraButton.setVisibility(View.GONE);
			}

			// New agriculture beneficiary
			if (FindPluginManager.mExtraButtonLabel2 != null) {
				final Button extraButton = (Button) findViewById(R.id.extraButton2);
				int resid = this.getResources().getIdentifier(FindPluginManager.mExtraButtonLabel2, "string", "org.hfoss.posit.android");
				if (extraButton != null) {
					extraButton.setText(resid);
					extraButton.setVisibility(View.VISIBLE);
					extraButton.setOnClickListener(this);
				}
				
				// Button is gone for USER users and (AGRI users during distribution events)
				Log.i(TAG, "Distr Stage = " +  AppControlManager.displayDistributionStage(this));
				if (AppControlManager.isRegularUser() 
						|| AppControlManager.isAdminUser()
						|| (AppControlManager.isAgriUser() && AppControlManager.isDuringDistributionEvent())
						|| (AppControlManager.isAgronUser() && AppControlManager.isDuringDistributionEvent())) {
					extraButton.setVisibility(View.GONE);
				} else {
					extraButton.setVisibility(View.VISIBLE);
				}
				
				Log.i(TAG, "Extra button visibility = " + extraButton.getVisibility());
			}
			
	}

	// Lifecycle methods just generate Log entries to help debug and understand flow

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"Pausing");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"Resuming");
		
		AcdiVocaLocaleManager.setDefaultLocale(this);  // Locale Manager should be in API
		startPOSIT();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"Starting");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG,"Restarting");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"Stopping");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"Destroying");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG,"onActivityResult Result from registration = " + resultCode);
		switch (requestCode) {

		case LoginActivity.ACTION_LOGIN:
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, getString(R.string.toast_thankyou), Toast.LENGTH_SHORT).show();
				break;
			} else {
				finish();
			} 
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Handles clicks on PositMain's buttons.
	 */
	public void onClick(View view) {

			Intent intent = new Intent();

			switch (view.getId()) {
			case R.id.addFindButton:
				intent.setClass(this, FindActivityProvider.getFindActivityClass());
				intent.setAction(Intent.ACTION_INSERT);
				intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE, AcdiVocaDbHelper.FINDS_TYPE_MCHN);
				startActivity(intent);
				break;
			case R.id.listFindButton:
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				//intent.putExtra(AcdiVocaDbHelper.FINDS_STATUS, SearchFilterActivity.RESULT_SELECT_NEW);
				intent.setClass(this, FindActivityProvider.getListFindsActivityClass());
				intent.setClass(this, AcdiVocaListFindsActivity.class);
				startActivity(intent);				
				break;

			case R.id.extraButton:
				intent.setAction(Intent.ACTION_EDIT);
				intent.setClass(this, FindActivityProvider.getExtraActivityClass());
				startActivity(intent);
				break;	

			case R.id.extraButton2:
				intent.setAction(Intent.ACTION_INSERT);
				intent.setClass(this, FindActivityProvider.getExtraActivityClass2());
				intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE, AcdiVocaDbHelper.FINDS_TYPE_AGRI);
				startActivity(intent);
				break;			
			}
	}

	/**
	 * Creates the menu options for the PositMain screen. Menu items are
	 * inflated from a resource file.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.positmain_menu, menu);
		return true;
	}

	/**
	 * Shows/Hides menus based on user type, SUPER, ADMIN, USER  
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		// Re-inflate to force localization.
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.positmain_menu, menu);
		
		MenuItem  adminMenu = menu.findItem(R.id.admin_menu_item);
		
		Log.i(TAG, "UserType = " + AppControlManager.getUserType()); 
		Log.i(TAG, "distribution stage = " + AppControlManager.getDistributionStage());
		// Hide the ADMIN menu from regular users
		if (AppControlManager.isRegularUser() || AppControlManager.isAgriUser())
			adminMenu.setVisible(false);
		else 
			adminMenu.setVisible(true);
		
		return super.onPrepareOptionsMenu(menu);

	}

	/**
	 * Manages the selection of menu items.
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(TAG, "onMenuItemSelected " + item.toString());
		switch (item.getItemId()) {
		case R.id.settings_menu_item:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.admin_menu_item:
			startActivity(new Intent(this, AcdiVocaAdminActivity.class));
			break;
		case R.id.about_menu_item:
			startActivity(new Intent(this, AboutActivity.class));
			break;

		}
		
		return true;
	}

	
	/**
	 * Intercepts the back key (KEYCODE_BACK) and displays a confirmation dialog
	 * when the user tries to exit POSIT.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
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
		switch (id) {
		case CONFIRM_EXIT:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(R.string.exit)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							finish();
						}
					}).setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							/* User clicked Cancel so do nothing */
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
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		switch (id) {
		case CONFIRM_EXIT:
					d.setTitle(R.string.exit);
//					d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.alert_dialog_ok), new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog,
//								int whichButton) {
//							// User clicked OK so do some stuff
//							finish();
//						}
//					} );
//					d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog,
//								int whichButton) {
//							/* User clicked Cancel so do nothing */
//						}
//					} );
					needsabutton = d.getButton(DialogInterface.BUTTON_POSITIVE);
					needsabutton.setText(R.string.alert_dialog_ok);
					needsabutton.invalidate();
					
					needsabutton = d.getButton(DialogInterface.BUTTON_NEGATIVE);
					needsabutton.setText(R.string.alert_dialog_cancel);
					needsabutton.invalidate();
					
					break;
		}
	}



	/**
	 * Makes sure RWG is stopped before exiting the Activity
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		Log.i(TAG, "finish()");
		super.finish();
	}
}