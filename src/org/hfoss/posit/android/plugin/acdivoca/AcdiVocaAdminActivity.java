/*
 * File: TutorialActivity.java
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

package org.hfoss.posit.android.plugin.acdivoca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

//import org.hfoss.posit.android.Log;
import org.hfoss.posit.android.PositMain;
import org.hfoss.posit.android.R;
import org.hfoss.posit.android.api.FilePickerActivity;
import org.hfoss.posit.android.api.FindActivityProvider;
import org.hfoss.posit.android.api.SettingsActivity;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Button;
import android.widget.Toast;


/**
 * This class designed to make the user more comfortable with
 * posit the first time posit is opened. The user navigates through the tutorial
 * using previous, next, skip, and finish buttons.
 */

interface SmsCallBack {
	public void smsMgrCallBack(String s);
}

public class AcdiVocaAdminActivity extends OrmLiteBaseActivity<AcdiVocaDbHelper> {

	public static String TAG = "AdminActivity";
	public static final int MAX_BENEFICIARIES = 20000;  // Max readable
	public static final int MAX_SMS = 85;
	public static final String DEFAULT_DIRECTORY = "acdivoca";
	public static final String DEFAULT_MCHN_DIRECTORY = "acdivoca/mchn";
	public static final String DEFAULT_AGRI_DIRECTORY = "acdivoca/agri";
	public static final String DEFAULT_LOG_DIRECTORY = "acdivoca/super";
	public static final String SMS_LOG_FILE = "smslog.txt";
	public static final String DEFAULT_BENEFICIARY_FILE = "Beneficiare.csv";
	public static final String DEFAULT_LIVELIHOOD_FILE = "Livelihood.csv";
	
	public static final String COMMA= ",";
	public static final int DONE = 0;
	public static final int ERROR = -1;

	public static final int IO_EXCEPTION = 1;
	public static final int STRING_EXCEPTION = 2;
	public static final int SEND_DIST_REP = 3;
	public static final int SMS_REPORT = 4;
	public static final int SUMMARY_OF_IMPORT = 5;
	public static final int ZERO_BENEFICIARIES_READ = 6;
	public static final int INVALID_PHONE_NUMBER = 7;
	public static final int DISTRIBUTION_SUMMARY = 8;
	public static final int NO_SIGNAL = 9;
	public static final int NO_SIM = 10;
	public static final int SMStooMuch = 11;
	public static final int contactAdmin = 12;


	private String mBeneficiaries[] = null;
	private ProgressDialog mProgressDialog;
	private String mDistrCtr;	

	protected static Context mContext;
	private String mSmsReport;
	private String mImportDataReport;
	private String mDistributionSummaryReport;
	private String[] mSummaryItems;

	// Added stuffs to monitor the SMS sending progress
	protected static boolean sms_service_done = false;
	protected static boolean totallyDone=false;

	//Split the SMS if they go over max limit. Now supporting up to 340 messages
	private ArrayList<AcdiVocaMessage> mAcdiVocaMsgs;
	protected static ArrayList<AcdiVocaMessage> msgDiv1=new ArrayList<AcdiVocaMessage>();
	protected static ArrayList<AcdiVocaMessage> msgDiv2=new ArrayList<AcdiVocaMessage>();
	protected static ArrayList<AcdiVocaMessage> msgDiv3=new ArrayList<AcdiVocaMessage>();
	protected static ArrayList<AcdiVocaMessage> msgDiv4=new ArrayList<AcdiVocaMessage>();
	protected static ArrayList<AcdiVocaMessage> msgDiv5=new ArrayList<AcdiVocaMessage>();
	protected static ArrayList<AcdiVocaMessage> msgDiv6=new ArrayList<AcdiVocaMessage>();
	protected static int batch = 0;
	
	/**
	 * Callback method used by SmsManager to report how
	 * many messages were sent. 
	 * @param smsReport the report from SmsManager
	 */
	public void smsMgrCallBack(String smsReport) {
		mSmsReport = smsReport;
		showDialog(SMS_REPORT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();

		AcdiVocaLocaleManager.setDefaultLocale(this);  // Locale Manager should be in API
		setContentView(R.layout.acdivoca_admin);
	}	

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();
	}

	/**
	 * Creates the menu options.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acdi_voca_admin_menu, menu);
		mContext = this;
		return true;
	}


	/**
	 * The Admin menu needs to be carefully prepared to control the management
	 * of distribution events through the various stages:  select distribution point,
	 * import beneficiary data, start, stop, send distribution report.  
	 * Here's where the various values are set:
	 * 
	 * 1) Select distribution point -- selected in settings.
	 * 2) Import beneficiary file -- set in SettingsActivity
	 * 3) Start -- set in onMenuItemSelected
	 * 4) Stop -- set in onMenuItemSelected
	 * 5) Send report -- set in SendSms
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acdi_voca_admin_menu, menu);

		MenuItem loadAgriItem = menu.findItem(R.id.load_agri_data);
		MenuItem loadMchnItem = menu.findItem(R.id.load_beneficiary_data);
		MenuItem listFindsItem = menu.findItem(R.id.admin_list_beneficiaries);
		MenuItem startStopDistr = menu.findItem(R.id.start_stop_distribution);
		MenuItem sendDistrReport = menu.findItem(R.id.send_distribution_report);

		// Is the app in Distribution mode?
		Log.i(TAG, "Distribution Stage = " + AppControlManager.displayDistributionStage(this));

		// What type of user is logged in?
		Log.i(TAG, "UserType = " + AppControlManager.getUserType());

		// Only SUPER user sees the ListFinds menu
		if (!AppControlManager.isSuperUser()) {
			listFindsItem.setVisible(false);
		} else {
			listFindsItem.setVisible(true);
		}

		// Only SUPER and AGRON users can import livelihood data
		if ( !AppControlManager.isDuringDistributionEvent() 
				&& (AppControlManager.isSuperUser() || AppControlManager.isAgronUser())) {
			loadAgriItem.setVisible(true);
			loadAgriItem.setEnabled(true);
		} else {
			loadAgriItem.setVisible(false);
			loadAgriItem.setEnabled(false);
		}

		// AGRON users don't see distribution event menus
		if (AppControlManager.isAgronUser()) {
			loadMchnItem.setVisible(false);
			startStopDistr.setVisible(false);
			sendDistrReport.setVisible(false);
		}

		Log.i(TAG, "onPrepareMenuOptions, " 
				+ " distribution stage = " + AppControlManager.displayDistributionStage(this));

		// For each menu item set its visibility based on Distribution event stage
		MenuItem item = null;
		for (int k = 0; k < menu.size(); k++) {
			item = menu.getItem(k);
			switch (item.getItemId()) {
			case R.id.load_beneficiary_data:
				if (AppControlManager.isImportDataStage()) 
					item.setEnabled(true);
				else 
					item.setEnabled(false);
				break;
			case R.id.start_stop_distribution:
				SubMenu sub = item.getSubMenu();
				if (AppControlManager.isStartDistributionStage()) {
					item.setEnabled(true);
					sub.getItem(0).setEnabled(true);
					sub.getItem(1).setEnabled(false);
				} else if (AppControlManager.isStopDistributionStage()) {
					item.setEnabled(true);
					sub.getItem(0).setEnabled(false);
					sub.getItem(1).setEnabled(true);		
				} else {
					item.setEnabled(false);
				}
				break;
			case R.id.send_distribution_report:
				if (AppControlManager.isSendDistributionReportStage()) 
					item.setEnabled(true);
				else 
					item.setEnabled(false);
				break;
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Manages the selection of menu items.
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(TAG, "Menu Item = " + item.getTitle());
		Intent intent = new Intent();

		switch (item.getItemId()) {
		case R.id.settings_menu_item:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.admin_list_beneficiaries:
			startActivity(new Intent(this, AcdiVocaListFindsActivity.class));
			break;
		case R.id.load_beneficiary_data:
			Class<Activity> loginActivity = FindActivityProvider.getLoginActivityClass();
			if (loginActivity != null) {
				intent.setClass(this, loginActivity);
				intent.putExtra(AcdiVocaUser.USER_TYPE_STRING, AcdiVocaUser.UserType.ADMIN.ordinal());
				intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE, AcdiVocaDbHelper.FINDS_TYPE_MCHN);
				this.startActivityForResult(intent, LoginActivity.ACTION_LOGIN);

				Toast.makeText(this, getString(R.string.toast_admin_login_required), Toast.LENGTH_LONG).show();	
			}
			break;
		case R.id.load_agri_data:
			loginActivity = FindActivityProvider.getLoginActivityClass();
			if (loginActivity != null) {
				intent.setClass(this, loginActivity);
				intent.putExtra(AcdiVocaUser.USER_TYPE_STRING, AcdiVocaUser.UserType.AGRON.ordinal());
				intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE, AcdiVocaDbHelper.FINDS_TYPE_AGRI);
				this.startActivityForResult(intent, LoginActivity.ACTION_LOGIN);

				Toast.makeText(this, getString(R.string.toast_admin_login_required), Toast.LENGTH_LONG).show();	
			}
			break;
		case R.id.start_distribution:
			Log.i(TAG, "Start distribution event");
			item.setEnabled(false);
			AppControlManager.moveToNextDistributionStage(this);
			break;
		case R.id.stop_distribution:
			Log.i(TAG, "Start distribution event");
			item.setEnabled(false);
			AppControlManager.moveToNextDistributionStage(this);
			displayDistributionSummary();
			break;
		case R.id.send_distribution_report:
			sendDistributionReport();
			break;
		}
		return true;
	}

	/**
	 * Provides a summary of the distribution event and displays it
	 * as an Alert Dialog. 
	 */
	private void displayDistributionSummary() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String distrKey = this.getResources().getString(R.string.distribution_point_key);
		String distributionCtr = sharedPrefs.getString(distrKey, "");
		Log.i(TAG, distrKey +"="+ distributionCtr);
		int nAbsentees = this.getHelper().queryNDistributionAbsentees(distributionCtr);
		int nWomen = this.getHelper().queryNDistributionWomenProcessed(distributionCtr);
		int nChildren = this.getHelper().queryNDistributionChildrenProcessed(distributionCtr);
		mSummaryItems = new String[3];
		mDistributionSummaryReport = getString(R.string.distribution_summary);
		mSummaryItems[0] = getString(R.string.nabsentees) + " " + nAbsentees;
		mSummaryItems[1] = getString(R.string.nmothers) + " "  + nWomen;
		mSummaryItems[2] = getString(R.string.nchildren) + " "  + nChildren;
		showDialog(DISTRIBUTION_SUMMARY);
	}

	/**
	 * Sends both update messages and bulk messages.
	 */
	private void sendDistributionReport() {

		AcdiVocaSmsManager mgr = AcdiVocaSmsManager.getInstance(this);
		if (!mgr.isPhoneNumberSet(this)) {
			showDialog(INVALID_PHONE_NUMBER);
			return;
		}

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String distrKey = this.getResources().getString(R.string.distribution_point_key);
		String distributionCtr = sharedPrefs.getString(distrKey, "");
		Log.i(TAG, distrKey +"="+ distributionCtr);

		// First create update reports -- i.e., those beneficiaries who were present and had changes.
		mAcdiVocaMsgs = 
				this.getHelper().createMessagesForBeneficiaries(SearchFilterActivity.RESULT_SELECT_UPDATE, null, distributionCtr);

		// Now create bulk absences messages
		mAcdiVocaMsgs.addAll(this.getHelper().createBulkUpdateMessages(distributionCtr));
		Log.i(TAG, "nMsgs to send " + mAcdiVocaMsgs.size());
		// Prompt the user
		showDialog(SEND_DIST_REP);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG,"onActivityResult = " + resultCode);
		int beneficiaryType = 0;

		switch (requestCode) {

		case FilePickerActivity.ACTION_CHOOSER:
			if (resultCode == FilePickerActivity.RESULT_OK) {
				String filename = data.getStringExtra(Intent.ACTION_CHOOSER);

				// Are we loading beneficiary update data, all of type MCHN or AGRI data
				beneficiaryType = data.getIntExtra(AcdiVocaDbHelper.FINDS_TYPE, -1);

				Log.i(TAG, "File picker file = " + filename + " Beneficiary type = " + beneficiaryType);

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				if (beneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_MCHN) {

					// Get this phone's Distribution Center
					//					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					mDistrCtr = prefs.getString(this.getResources().getString(R.string.distribution_point_key), null);


					if (mDistrCtr == null) {	
						Log.i(TAG, "Aborting loadBeneficiaryData, No distribution post selected");
						Toast.makeText(this, getString(R.string.toast_distribution_post), Toast.LENGTH_SHORT);
						return;

					}
				}
				if (beneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_AGRI) {

					// Get this phone's commune section
					mDistrCtr = prefs.getString(this.getResources().getString(R.string.commune_section_key), null);


					if (mDistrCtr == null) {	
						Log.i(TAG, "Aborting loadBeneficiaryData for Livelihood data, No commune section selected");
						//						Toast.makeText(this, getString(R.string.toast_distribution_post), Toast.LENGTH_SHORT);
						return;
					}
				}

				mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_data),
						getString(R.string.please_wait), true, true);

				ImportDataThread thread = new ImportDataThread(this, filename, beneficiaryType, new ImportThreadHandler());
				thread.start();				
			}
			break;

		case LoginActivity.ACTION_LOGIN:
			if (resultCode == RESULT_OK) {

				Intent intent = new Intent();
				beneficiaryType = data.getIntExtra(AcdiVocaDbHelper.FINDS_TYPE, -1);
				Log.i(TAG, "Logged in, beneficiary type = " + beneficiaryType);
				intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE, beneficiaryType);
				intent.setClass(this, FilePickerActivity.class);

				this.startActivityForResult(intent, FilePickerActivity.ACTION_CHOOSER);

				break;
			} else {
				Toast.makeText(this, getString(R.string.toast_incorrect), Toast.LENGTH_LONG).show();
				//finish();
			} 

		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}


	/**
	 * Reads data from a text file into the Db.
	 */
	private int importBeneficiaryDataToDb(String filename, int beneficiaryType) {		
		ContentValues values = new ContentValues();
		Log.i(TAG, "Reading beneficiaries from " + filename);
		mBeneficiaries = loadBeneficiaryData(filename, mDistrCtr, beneficiaryType);
		if (mBeneficiaries.length == 0) {
			mImportDataReport = getString(R.string.beneficiaries_imported2) + " " + mBeneficiaries.length;
			return ZERO_BENEFICIARIES_READ;
		} else if (mBeneficiaries.length == 1) {
			if (Integer.parseInt(mBeneficiaries[0]) == IO_EXCEPTION)
				return IO_EXCEPTION;
			else if (Integer.parseInt(mBeneficiaries[0]) == STRING_EXCEPTION)
				return STRING_EXCEPTION;
		}
		int rows = this.getHelper().clearBeneficiaryTable();
		Log.i(TAG, "Deleted rows in beneficiary table = " + rows);
		rows = this.getHelper().clearMessageTable();
		Log.i(TAG, "Deleted rows in message table = " + rows);

		long nImports = 0;
		Log.i(TAG, "Beneficiary type to be loaded = " + beneficiaryType);
		if (beneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_MCHN) {
			//db = new AcdiVocaDbHelper(this);
			nImports = this.getHelper().addUpdateBeneficiaries(mBeneficiaries);

		} else  {
			//db = new AcdiVocaDbHelper(this);
			nImports = this.getHelper().addAgriBeneficiaries(mBeneficiaries);
		}

		mImportDataReport = getString(R.string.beneficiaries_imported2) + " " + nImports;
		Log.i(TAG, "Inserted to database " + nImports + " Beneficiaries");	

		// Move to the next stage of the distribution event process
		if(beneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_MCHN) {
			AppControlManager.moveToNextDistributionStage(this);
		}
		return DONE;
	}

	/**
	 * Reads beneficiary data from a text file.  Currently the
	 * file name is hard coded as "beneficiaries.txt" and it is
	 * stored in the /assets folder.
	 * @return  Returns an array of Strings, each of which represents
	 * a Beneficiary record.
	 */
	private String[] loadBeneficiaryData(String filename, String distrCtr, int beneficiaryType) {
		String[] data = null;

		File file = null;
		if (beneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_MCHN)
			file = new File(Environment.getExternalStorageDirectory() 
					+ "/" + DEFAULT_MCHN_DIRECTORY + "/" 
					+ filename);
		else 
			file = new File(Environment.getExternalStorageDirectory() 
					+ "/" + DEFAULT_AGRI_DIRECTORY + "/" 
					+ filename);


		BufferedReader br = null;
		String line = null;
		int k = 0;

		try {
			//InputStream iStream = this.getAssets().open("beneficiaries.txt");
			FileInputStream iStream = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(iStream));
			data = new String[MAX_BENEFICIARIES];
			line = br.readLine();
			//while (line != null && k < 1000)  {
			if (beneficiaryType == AcdiVocaDbHelper.FINDS_TYPE_MCHN) {

				// Reading from Beneficiare.csv and filter by distrCtr
				while (line != null)  {
					//				Log.i(TAG, line);
					if (line.length() > 0 && line.charAt(0) != '*')  {
						if (line.contains(distrCtr)) {
							data[k] = line;
							k++;
						}
					}
					line = br.readLine();
				}
			} else {

				// Reading from Livelihood.csv and no filter
				while (line != null)  {
					//				Log.i(TAG, line);
					if (line.length() > 0 && line.charAt(0) != '*')  {
						//						if (!line.contains("No_dossier")) {
						if (line.contains(distrCtr)) {						
							data[k] = line;
							k++;
						}
					}
					line = br.readLine();
				}	
			}
		} catch (IOException e) {
			Log.e(TAG, "IO Exception,  file =   " + file);
			e.printStackTrace();
			String[] error = {"" + IO_EXCEPTION};
			return error;
		} catch (StringIndexOutOfBoundsException e) {
			Log.e(TAG, "Bad line?  " + line);
			//			showDialog(STRING_EXCEPTION);
			e.printStackTrace();
			String[] error = {"" + STRING_EXCEPTION};
			return error;
		}
		String[] dossiers = new String[k];  // Create the actual size of array
		for (int i= 0; i < k; i++)
			dossiers[i] = data[i];
		return dossiers;
	}

	/**
	 * timer method to wait for time out while waiting for the SMS limit
	 * @param msgList1,msgList2,msgList3 are ArrayLists containing messages
	 * @param done is a boolean checking if all messeges are handled 
	 */
	protected static void startTimer(final ArrayList<AcdiVocaMessage> msgList1,final ArrayList<AcdiVocaMessage> msgList2,
			final ArrayList<AcdiVocaMessage> msgList3, final ArrayList<AcdiVocaMessage> msgList4,
			final ArrayList<AcdiVocaMessage> msgList5, final boolean done){
		
		if(msgList1!=null){
			CountDownTimer ctimer = new CountDownTimer(60000, 1000) 
			{
				@Override
				public void onTick(long l){
					Log.i(TAG, "TIMER HAS STARTED!");
					if(sms_service_done==true){						
						PositMain.mSMSdialog.setTitle(R.string.smsWaiting);
						int remain = (int)l/1000;
						int hr = (int) (remain/3600);
						int min= (int) ((remain%3600)/60);
						int sec = (int)((remain%3600)%60);
						String timeRemaining = Integer.toString(hr)+":"+Integer.toString(min)+":"+Integer.toString(sec);
						PositMain.mSMSdialog.setMessage(timeRemaining);
					}
				}
				@Override
				public void onFinish(){
					if(done==true){
						totallyDone=true;						
					}
					batch++;
					AcdiVocaSmsManager mgr = AcdiVocaSmsManager.getInstance((Activity) mContext);
					sms_service_done=false;
					mgr.sendMessages((OrmLiteBaseActivity<AcdiVocaDbHelper>)mContext, msgList1);
					Log.i(TAG, "NOW START THE NEXT BATCH");
					if(msgList2!=null){
						if(msgList3!=null)
							startTimer(msgList2,msgList3,msgList4,msgList5,null,false);
						else
							startTimer(msgList2,msgList3,msgList4,msgList5,null,true);
					}
				};
			}.start();

		}		
	}

	/**
	 * available method to check if the given list is less than max sms limit
	 * @param msgList is an ArrayList containing messages
	 * @return true if the list fulfills the requirement
	 */
	protected static boolean available(ArrayList<AcdiVocaMessage> msgList){
		return msgList.size() < MAX_SMS;
	}
	
	/**
	 * resetArrays method 
	 * reset the arrays that are used to split the message
	 */
	protected static void resetArrays(){
		msgDiv1.clear();
		msgDiv2.clear();
		msgDiv3.clear();
		msgDiv4.clear();
		msgDiv5.clear();
		msgDiv6.clear();
	}
	
	/**
	 * Divides the long sms list into multiple small lists
	 * @param msgList is an ArrayList containing messages
	 * @param div is an integer that defines how many divisions to maker 
	 */
	protected static void smsSplitter(ArrayList<AcdiVocaMessage> msgList, int div){
		resetArrays();
		if(msgList!=null){
			for(int i=0; i<msgList.size(); i++){
				if(available(msgDiv1)){
					Log.i(TAG, "#####DIV1 ADDED IDEX: "+ i);
					msgDiv1.add(msgList.get(i));
					continue;
				}
				if(!available(msgDiv1)&&(available(msgDiv2))){
					Log.i(TAG, "#####DIV2 ADDED IDEX: "+ i);
					msgDiv2.add(msgList.get(i));
					continue;
				}
				if(!available(msgDiv1) && (!available(msgDiv2)) && (available(msgDiv3)) ){
					Log.i(TAG, "#####DIV3 ADDED IDEX: "+ i);
					msgDiv3.add(msgList.get(i));
					continue;
				}
				if(!available(msgDiv1) && (!available(msgDiv2)) && (!available(msgDiv3)) && (available(msgDiv4))){
					Log.i(TAG, "#####DIV4 ADDED IDEX: "+ i);
					msgDiv4.add(msgList.get(i));
					continue;
				}
				if(!available(msgDiv1) && (!available(msgDiv2)) && (!available(msgDiv3)) && (!available(msgDiv4)) && (available(msgDiv5))){
					Log.i(TAG, "#####DIV5 ADDED IDEX: "+ i);
					msgDiv5.add(msgList.get(i));
					continue;
				}
				if(!available(msgDiv1) && (!available(msgDiv2)) && (!available(msgDiv3)) && (!available(msgDiv4)) && (!available(msgDiv5)) && (available(msgDiv6))){
					Log.i(TAG, "#####DIV6 ADDED IDEX: "+ i);
					msgDiv6.add(msgList.get(i));
					continue;
				}
			}
			Log.i(TAG,"TOTAL: "+msgList.size());
			Log.i(TAG,"DIVIDED: "+ div );
			Log.i(TAG,"THE FIRST ONE: "+msgDiv1.size());
			Log.i(TAG,"THE SECOND ONE: "+msgDiv2.size());
			Log.i(TAG,"THE THIRD ONE: "+msgDiv3.size());
			Log.i(TAG,"THE FOURTH ONE: "+msgDiv4.size());
			Log.i(TAG,"THE FIFTH ONE: "+msgDiv5.size());
			Log.i(TAG,"THE SIXTH ONE: "+msgDiv6.size());
			batch=1;
			totallyDone=false;
			AcdiVocaSmsManager mgr = AcdiVocaSmsManager.getInstance((Activity) mContext);
			sms_service_done=false;
			mgr.sendMessages((OrmLiteBaseActivity<AcdiVocaDbHelper>)mContext, msgDiv1);
			
			if(div==2){
				startTimer(msgDiv2,null,null,null,null,true);
			}
			if(div==3){
				startTimer(msgDiv2,msgDiv3,null,null,null,false);
			}
			if(div==4){
				startTimer(msgDiv2,msgDiv3,msgDiv4,null,null,false);
			}
			if(div==5){
				startTimer(msgDiv2,msgDiv3,msgDiv4,msgDiv5,null,false);
			}
			if(div==6){
				startTimer(msgDiv2,msgDiv3,msgDiv4,msgDiv5,msgDiv6,false);
			}	
		}
	}




	/**
	 * Creates a dialog to handle various alerts and announcements to the user.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i(TAG, "onCreateDialog, id= " + id);
		switch (id) {
		case ZERO_BENEFICIARIES_READ:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(mImportDataReport)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							finish();
						}
					}).create();		
		case DISTRIBUTION_SUMMARY:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(mDistributionSummaryReport)
					.setItems(mSummaryItems, null)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							//finish();
						}
					}).create();
		case SUMMARY_OF_IMPORT:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(mImportDataReport)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							//finish();
						}
					}).create();
		case IO_EXCEPTION:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(getString(R.string.io_exc) + DEFAULT_BENEFICIARY_FILE)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							finish();
						}
					}).create();
		case STRING_EXCEPTION:
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(getString(R.string.string_exc))
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// User clicked OK so do some stuff
							finish();
						}
					}).create();
		case SEND_DIST_REP:
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			final String phoneNumber = sp.getString(mContext.getString(R.string.smsPhoneKey),"");
			Log.i(TAG, "phonenumber = " + phoneNumber); 
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.about2).setTitle(
							mAcdiVocaMsgs.size() + " " + getString(R.string.send_dist_rep2)
							+ " #: " + phoneNumber)
							.setPositiveButton(R.string.alert_dialog_ok,
									new DialogInterface.OnClickListener() {								
								public void onClick(DialogInterface dialog,
										int which) {
									TelephonyManager tm = (TelephonyManager)getSystemService(android.content.Context.TELEPHONY_SERVICE);
									if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT){
										ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
										NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
										if(!mobile.isAvailable()){
											showDialog(NO_SIGNAL);
										}
										else{
											AcdiVocaSmsManager mgr = AcdiVocaSmsManager.getInstance((Activity) mContext);
											if(mAcdiVocaMsgs.size()>MAX_SMS){
												showDialog(SMStooMuch);

											}
											else{
												totallyDone = true;
												batch = 0;
												mgr.sendMessages((OrmLiteBaseActivity<AcdiVocaDbHelper>)mContext, mAcdiVocaMsgs);
												AppControlManager.moveToNextDistributionStage(mContext);
												mSmsReport = "#: " + phoneNumber + "\n" + mAcdiVocaMsgs.size();
												finish();
											}
										}
									}
									else{
										showDialog(NO_SIM);
									}
								}
							}).setNegativeButton(R.string.alert_dialog_cancel,
									new DialogInterface.OnClickListener() {										
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create();

		case NO_SIM:
			return new AlertDialog.Builder(this).setTitle("WARNING!")
					.setIcon(R.drawable.warning)
					.setMessage(R.string.no_sim)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					}).create();

		case NO_SIGNAL:
			return new AlertDialog.Builder(this).setTitle("WARNING!")
					.setIcon(R.drawable.warning)
					.setMessage(R.string.no_signal)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					}).create();
		case SMStooMuch:
			int div = 0;
			if(mAcdiVocaMsgs.size()>MAX_SMS && mAcdiVocaMsgs.size()<=(2*MAX_SMS)){
				div = 2;
			}
			if(mAcdiVocaMsgs.size()>(2*MAX_SMS) && mAcdiVocaMsgs.size()<=(3*MAX_SMS)){
				div = 3;	
			}
			if(mAcdiVocaMsgs.size()>(3*MAX_SMS) && mAcdiVocaMsgs.size()<=(4*MAX_SMS)){
				div = 4;
			}
			if(mAcdiVocaMsgs.size()>(4*MAX_SMS) && mAcdiVocaMsgs.size()<=(5*MAX_SMS)){
				div = 5;
			}
			if(mAcdiVocaMsgs.size()>(5*MAX_SMS) && mAcdiVocaMsgs.size()<=(6*MAX_SMS)){
				div = 6;
			}
			String message = mAcdiVocaMsgs.size() + " " +getString(R.string.smsLimit1) + " "+
							div+" "+getString(R.string.smsLimit2)+ " " +(div-1)+ " " + getString(R.string.smsLimit3); 
			return new AlertDialog.Builder(this).setTitle(R.string.smsSending)
					.setIcon(R.drawable.alert_dialog_icon)
					.setMessage(message)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if(mAcdiVocaMsgs.size()>MAX_SMS && mAcdiVocaMsgs.size()<=(2*MAX_SMS)){
								smsSplitter(mAcdiVocaMsgs,2);
								finish();
							}
							if(mAcdiVocaMsgs.size()>(2*MAX_SMS) && mAcdiVocaMsgs.size()<=(3*MAX_SMS)){
								smsSplitter(mAcdiVocaMsgs,3);	
								finish();
							}
							if(mAcdiVocaMsgs.size()>(3*MAX_SMS) && mAcdiVocaMsgs.size()<=(4*MAX_SMS)){
								smsSplitter(mAcdiVocaMsgs,4);
								finish();
							}
							if(mAcdiVocaMsgs.size()>(4*MAX_SMS) && mAcdiVocaMsgs.size()<=(5*MAX_SMS)){
								smsSplitter(mAcdiVocaMsgs,5);
								finish();
							}
							if(mAcdiVocaMsgs.size()>(5*MAX_SMS) && mAcdiVocaMsgs.size()<=(6*MAX_SMS)){
								smsSplitter(mAcdiVocaMsgs,6);
								finish();
							}
							else{
								showDialog(contactAdmin);
							}
							
						}	
					}).create();
		case contactAdmin:
			return new AlertDialog.Builder(this).setTitle(R.string.smsSending)
					.setIcon(R.drawable.alert_dialog_icon)
					.setMessage(R.string.overLimit)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}	
					}).create();
		default:
			return null;
		}
	}


	/**
	 * Called just before the dialog is shown. Need to change title
	 * to reflect the current phone number. 
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String phoneNumber = sp.getString(mContext.getString(R.string.smsPhoneKey),"");
		Button needsabutton;
		AlertDialog d = (AlertDialog) dialog;
		Log.i(TAG, "phonenumber = " + phoneNumber); 
		switch (id) {
		case SEND_DIST_REP:
			Log.i(TAG, "onPrepareDialog id= " + id);
			d.setTitle(mAcdiVocaMsgs.size() + " " + getString(R.string.send_dist_rep2)
					+ " #: " + phoneNumber);
			needsabutton = d.getButton(DialogInterface.BUTTON_POSITIVE);
			needsabutton.setText(R.string.alert_dialog_ok);
			needsabutton.invalidate();
			needsabutton = d.getButton(DialogInterface.BUTTON_NEGATIVE);
			needsabutton.setText(R.string.alert_dialog_cancel);
			needsabutton.invalidate();
			break;
		case SMS_REPORT:
			d.setTitle( mAcdiVocaMsgs.size() + " " + getString(R.string.being_sent_to) +
					" # : " + phoneNumber);
			needsabutton = d.getButton(DialogInterface.BUTTON_POSITIVE);
			needsabutton.setText(R.string.alert_dialog_ok);
			needsabutton.invalidate();
			break;

		}
	}

	/**
	 * Class for handling data import.
	 */
	class ImportThreadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == DONE) {
				mProgressDialog.dismiss();
				showDialog(SUMMARY_OF_IMPORT);
			} else if (msg.what == ZERO_BENEFICIARIES_READ) {
				mProgressDialog.dismiss();
				showDialog(ZERO_BENEFICIARIES_READ);
			}
			else if (msg.what == IO_EXCEPTION) {
				mProgressDialog.dismiss();
				showDialog(IO_EXCEPTION);
			}
			else if (msg.what == STRING_EXCEPTION) {
				mProgressDialog.dismiss();
				showDialog(STRING_EXCEPTION);
			}
		}
	}

	/**
	 * Thread to handle import of data from external file. 
	 *
	 */
	class ImportDataThread extends Thread {
		private Context mContext;
		private Handler mHandler;
		private String mFilename;
		private int mBeneficiaryType;

		public ImportDataThread(Context context, String filename, int beneficiaryType, Handler handler) {
			mHandler = handler;
			mContext = context;
			mFilename = filename;
			mBeneficiaryType = beneficiaryType;
		}

		@Override
		public void run() {
			int result = 0;

			result = importBeneficiaryDataToDb(mFilename, mBeneficiaryType);
			if (result == DONE)
				mHandler.sendEmptyMessage(AcdiVocaAdminActivity.DONE);
			else if (result == IO_EXCEPTION)
				mHandler.sendEmptyMessage(AcdiVocaAdminActivity.IO_EXCEPTION);
			else if (result == STRING_EXCEPTION)
				mHandler.sendEmptyMessage(AcdiVocaAdminActivity.STRING_EXCEPTION);
			else if (result == ZERO_BENEFICIARIES_READ) 
				mHandler.sendEmptyMessage(AcdiVocaAdminActivity.ZERO_BENEFICIARIES_READ);

		}
	}


}

