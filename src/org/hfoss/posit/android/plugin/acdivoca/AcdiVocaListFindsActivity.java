/*
 * File: AcdiVocaListFindsActivity.java
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

import java.util.ArrayList;
import java.util.List;

import org.hfoss.posit.android.R;
//import org.hfoss.posit.android.Utils;
import org.hfoss.posit.android.api.ListFindsActivity;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaUser.UserType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Displays a summary of Finds on this phone in a clickable list.
 * 
 */
public class AcdiVocaListFindsActivity extends ListFindsActivity implements
		ViewBinder, SmsCallBack {

	private static final String TAG = "ListActivity";
	private Cursor mCursor; // Used for DB accesses

	private static final int confirm_exit = 1;

	private static final int CONFIRM_DELETE_DIALOG = 0;
	public static final int FIND_FROM_LIST = 0;
	public static final String MESSAGE_START_SUBSTRING = "t=";
	private static final int SMS_REPORT = AcdiVocaAdminActivity.SMS_REPORT;
	private static final int SEND_MSGS_ALERT = AcdiVocaAdminActivity.SEND_DIST_REP;
	private static final int INVALID_PHONE_NUMBER = AcdiVocaAdminActivity.INVALID_PHONE_NUMBER;
	private static final int NO_MSGS_ALERT = SMS_REPORT + 1;

	private String mAction;
	private int mStatusFilter;
	private Activity mActivity;
	private ArrayList<AcdiVocaMessage> mAcdiVocaMsgs;

	// private int project_id;
	private static final boolean DBG = false;
	// private ArrayAdapter<String> mAdapter;

	private MessageListAdapter<AcdiVocaMessage> mAdapter;
	// private ArrayAdapter<AcdiVocaMessage> mAdapter;

	private int mMessageFilter = -1; // Set in SearchFilterActivity result
	private int mNMessagesDisplayed = 0;
	private int mNFinds = 0;
	private boolean thereAreUnsentFinds = false;
	private boolean mDisplayingSearchResults = false;
	private boolean mMessageListDisplayed = false;
	private String mSmsReport;

	/**
	 * Callback method used by SmsManager to report how many messages were sent.
	 * 
	 * @param smsReport
	 *            the report from SmsManager
	 */
	public void smsMgrCallBack(String smsReport) {
		mSmsReport = smsReport;
		showDialog(SMS_REPORT);
	}

	/**
	 * Called when the Activity starts.
	 * 
	 * @param savedInstanceState
	 *            contains the Activity's previously frozen state. In this case
	 *            it is unused.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = this;
		Intent intent = getIntent();
		mAction = intent.getAction();
		if (mAction == null)
			mAction = "";
		mStatusFilter = intent.getIntExtra(AcdiVocaDbHelper.FINDS_STATUS, -1);
		Log.i(TAG, "onCreate(), action = " + mAction);

		// SharedPreferences sp =
		// PreferenceManager.getDefaultSharedPreferences(this);
		// project_id = 0; //sp.getInt("PROJECT_ID", 0);
	}

	/**
	 * Called when the activity is ready to start interacting with the user. It
	 * is at the top of the Activity stack.
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume()");
		AcdiVocaLocaleManager.setDefaultLocale(this); // Locale Manager should
														// be in API

		// SharedPreferences sp =
		// PreferenceManager.getDefaultSharedPreferences(this);
		// project_id = 0; //sp.getInt("PROJECT_ID", 0);

		// if (mAction.equals(Intent.ACTION_SEND)) {
		// displayMessageList(mStatusFilter, null); // Null distribution center
		// = all New finds
		// } else
		if (!mMessageListDisplayed && !mDisplayingSearchResults) {
			fillData(null);
			// NotificationManager nm =
			// (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			// nm.cancel(Utils.NOTIFICATION_ID);
		}
	}

	/**
	 * Called when the system is about to resume some other activity. It can be
	 * used to save state, if necessary. In this case we close the cursor to the
	 * DB to prevent memory leaks.
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		stopManagingCursor(mCursor);
		if (mCursor != null)
			mCursor.close();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mCursor != null)
			mCursor.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCursor != null)
			mCursor.close();
	}

	private void fillSearchResults(List avFinds) {
		if (avFinds.size() == 0) {
			setContentView(R.layout.acdivoca_list_beneficiaries);
			return;
		}
		Log.i(TAG, "returns: " + avFinds);
		BeneficiaryListAdapter<AcdiVocaFind> adapter = new BeneficiaryListAdapter(
				this, R.layout.acdivoca_list_row, avFinds);
		setListAdapter(adapter);

		// adapter.getView(position, convertView, parent)
		// Intent intent = new Intent(this, AcdiVocaListFindsActivity.class);
		// intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,AcdiVocaDbHelper.FINDS_TYPE_MCHN);

		// startActivityForResult(intent, FIND_FROM_LIST);
	}

	/**
	 * Puts the items from the DB table into the rows of the view. Note that
	 * once you start managing a Cursor, you cannot close the DB without causing
	 * an error.
	 */
	private void fillData(String order_by) {
		AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);

		int beneficiary_type = -1;
		UserType userType = AppControlManager.getUserType();
		if (userType.equals(UserType.ADMIN) || userType.equals(UserType.USER))
			beneficiary_type = AcdiVocaDbHelper.FINDS_TYPE_MCHN;
		else if (userType.equals(UserType.AGRON)
				|| userType.equals(UserType.AGRI))
			beneficiary_type = AcdiVocaDbHelper.FINDS_TYPE_AGRI;
		else if (userType.equals(UserType.SUPER))
			beneficiary_type = AcdiVocaDbHelper.FINDS_TYPE_BOTH;
		else
			Log.e(TAG, "Error: Unexpected user type in List Finds");

		List<AcdiVocaFind> list = db.fetchAllBeneficiaries(beneficiary_type);
		if (list.size() == 0) {
			setContentView(R.layout.acdivoca_list_beneficiaries);
			return;
		}

		thereAreUnsentFinds = db.queryUnsentBeneficiaries();

		BeneficiaryListAdapter<AcdiVocaFind> adapter = new BeneficiaryListAdapter(
				this, R.layout.acdivoca_list_row, list);
		setListAdapter(adapter);
		Log.i(TAG, "There are unsent finds = " + thereAreUnsentFinds);
	}

	/**
	 * Invoked when the user clicks on one of the Finds in the list. It starts
	 * the PhotoFindActivity in EDIT mode, which will read the Find's data from
	 * the DB.
	 * 
	 * @param l
	 *            is the ListView that was clicked on
	 * @param v
	 *            is the View within the ListView
	 * @param position
	 *            is the View's position in the ListView
	 * @param id
	 *            is the Find's RowID
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		TextView tv = (TextView) v.findViewById(R.id.row_id);
		int findId = Integer.parseInt((String) tv.getText());

		// lookup the id and check the beneficiary type
		// based on that prepare the intent
		// Intent intent = new Intent(this, AcdiVocaFindActivity.class);
		AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
		AcdiVocaFind avFind = db.fetchFindById(findId, null);
		if (avFind == null) {
			Log.e(TAG, "Unable to lookup find with id = " + findId);
			return;
		}

		startDisplayFindActivity(avFind);
		// ContentValues values = avFind.toContentValues();
		//
		// // ContentValues values = db.fetchFindDataById(id, null);
		//
		// Log.i(TAG, "###############################################");
		// Log.i(TAG, values.toString());
		// Intent intent = null;
		// if(values.getAsInteger(AcdiVocaDbHelper.FINDS_TYPE) ==
		// AcdiVocaDbHelper.FINDS_TYPE_MCHN){
		// intent = new Intent(this, AcdiVocaMchnFindActivity.class);
		// intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,AcdiVocaDbHelper.FINDS_TYPE_MCHN);
		// }
		// if(values.getAsInteger(AcdiVocaDbHelper.FINDS_TYPE) ==
		// AcdiVocaDbHelper.FINDS_TYPE_AGRI){
		// intent = new Intent(this, AcdiVocaAgriFindActivity.class);
		// intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,AcdiVocaDbHelper.FINDS_TYPE_AGRI);
		// }
		// // if(values.getAsInteger(AcdiVocaDbHelper.FINDS_TYPE) ==
		// AcdiVocaDbHelper.FINDS_TYPE_BOTH){
		// // intent = new Intent(this, AcdiVocaFindActivity.class);
		// //
		// intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,AcdiVocaDbHelper.FINDS_TYPE_BOTH);
		// // }
		//
		// intent.setAction(Intent.ACTION_EDIT);
		// if (DBG) Log.i(TAG,"id = " + id);
		// intent.putExtra(AcdiVocaDbHelper.FINDS_ID, (long) findId);
		//
		// startActivityForResult(intent, FIND_FROM_LIST);
	}

	/**
	 * Helper method that takes a Find and starts the appropriate display
	 * activity.
	 * 
	 * @param avFind
	 */
	private void startDisplayFindActivity(AcdiVocaFind avFind) {
		ContentValues values = avFind.toContentValues();
		int findId = avFind.id;

		// ContentValues values = db.fetchFindDataById(id, null);

		Log.i(TAG, "###############################################");
		Log.i(TAG, values.toString());
		Intent intent = null;
		if (values.getAsInteger(AcdiVocaDbHelper.FINDS_TYPE) == AcdiVocaDbHelper.FINDS_TYPE_MCHN) {
			intent = new Intent(this, AcdiVocaMchnFindActivity.class);
			intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,
					AcdiVocaDbHelper.FINDS_TYPE_MCHN);
		}
		if (values.getAsInteger(AcdiVocaDbHelper.FINDS_TYPE) == AcdiVocaDbHelper.FINDS_TYPE_AGRI) {
			intent = new Intent(this, AcdiVocaAgriFindActivity.class);
			intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,
					AcdiVocaDbHelper.FINDS_TYPE_AGRI);
		}
		// if(values.getAsInteger(AcdiVocaDbHelper.FINDS_TYPE) ==
		// AcdiVocaDbHelper.FINDS_TYPE_BOTH){
		// intent = new Intent(this, AcdiVocaFindActivity.class);
		// intent.putExtra(AcdiVocaDbHelper.FINDS_TYPE,AcdiVocaDbHelper.FINDS_TYPE_BOTH);
		// }

		intent.setAction(Intent.ACTION_EDIT);
		if (DBG)
			Log.i(TAG, "id = " + findId);
		intent.putExtra(AcdiVocaDbHelper.FINDS_ID, (long) findId);

		startActivityForResult(intent, FIND_FROM_LIST);

	}

	/**
	 * Creates the menus for this activity.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acdi_voca_list_finds_menu, menu);
		return true;
	}

	/**
	 * Prepares the menu options based on the message search filter. This is
	 * called just before the menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(TAG, "Prepare Menus, N messages = " + mNMessagesDisplayed);

		// Re-inflate to force localization.
		Log.i(TAG, "onPrepareOptionsMenu");
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acdi_voca_list_finds_menu, menu);

		MenuItem listItem = menu.findItem(R.id.list_messages);
		MenuItem syncItem = menu.findItem(R.id.sync_messages);
		MenuItem deleteItem = menu.findItem(R.id.delete_messages_menu);
		deleteItem.setVisible(false); // Always invisible for now
		MenuItem searchItem = menu.findItem(R.id.search_finds_menu);

		// If invoked from main view Button, rather than Admin Menu
		// (ACTION_SEND) then
		// the Finds are displayed initially so just show the USER, SEND, and
		// SEARCH menu items
		if (mAction.equals(Intent.ACTION_SEND)) {
			Log.i(TAG, "UserType = " + AppControlManager.getUserType());

			// Normal USER -- just show the Send menu
			if (AppControlManager.isRegularUser()
					|| AppControlManager.isAgriUser()) {
				listItem.setVisible(false);
				searchItem.setVisible(false);
				syncItem.setVisible(true);
				if (thereAreUnsentFinds)
					syncItem.setEnabled(true);
				else
					syncItem.setEnabled(false);
			} else { // SUPER or ADMIN USER, also show the manage messages menu
				adjustAdminMenuOptions(menu, syncItem, deleteItem, searchItem);
			}
			return super.onPrepareOptionsMenu(menu);
		} else {
			adjustAdminMenuOptions(menu, syncItem, deleteItem, searchItem);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Helper method to control menu options for Admin users.
	 * 
	 * @param menu
	 * @param syncItem
	 * @param deleteItem
	 * @param searchItem
	 */
	private void adjustAdminMenuOptions(Menu menu, MenuItem syncItem,
			MenuItem deleteItem, MenuItem searchItem) {
		// In this case the Menu also applies to a list of MESSAGES, not FINDS
		// and this should apply only to SUPER or ADMIN users
		Log.i(TAG, "Prepare Menus, nMsgs = " + mNMessagesDisplayed);

		// Case where messages are displayed
		if (mMessageListDisplayed) {
			if (mNMessagesDisplayed > 0
					&& (mMessageFilter == SearchFilterActivity.RESULT_SELECT_NEW
							|| mMessageFilter == SearchFilterActivity.RESULT_SELECT_PENDING
							|| mMessageFilter == SearchFilterActivity.RESULT_SELECT_UPDATE || mMessageFilter == SearchFilterActivity.RESULT_BULK_UPDATE)) {
				Log.i(TAG, "Prepare Menus, enabled SYNC");
				syncItem.setEnabled(true);
			} else {
				Log.i(TAG, "Prepare Menus, disabled SYNC");
				syncItem.setEnabled(false);
			}
		} else {
			// Case where Finds are displayed
			if (mAction.equals(Intent.ACTION_SEND) && thereAreUnsentFinds)
				syncItem.setEnabled(true);
			else
				syncItem.setEnabled(false);
		}

		deleteItem = menu.findItem(R.id.delete_messages_menu);
		if (mMessageFilter == SearchFilterActivity.RESULT_SELECT_ACKNOWLEDGED
				&& mNMessagesDisplayed > 0) {
			deleteItem.setEnabled(true);
		} else {
			deleteItem.setEnabled(false);
		}

		searchItem.setVisible(true);

	}

	/**
	 * Starts the appropriate Activity when a MenuItem is selected.
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.i(TAG, "Menu item selected " + item.getTitle());
		Intent intent;
		Log.i(TAG, "UserType = " + AppControlManager.getUserType());

		switch (item.getItemId()) {

		// Invoked only when Finds (not messages) are displayed
		case R.id.search_finds_menu:
			intent = new Intent();
			intent.setClass(this, SearchFindsActivity.class);
			this.startActivityForResult(intent,
					SearchFindsActivity.ACTION_SEARCH);
			break;

		// Start a SearchFilterActivity for result
		case R.id.list_messages:
			intent = new Intent();
			intent.setClass(this, SearchFilterActivity.class);
			intent.putExtra("user_mode", "ADMIN");
			this.startActivityForResult(intent,
					SearchFilterActivity.ACTION_SELECT);
			break;

		// This case sends all messages (if messages are currently displayed)
		case R.id.sync_messages:
			// For regular USER, create the messages and send

			AcdiVocaSmsManager mgr = AcdiVocaSmsManager.getInstance(this);
			if (!mgr.isPhoneNumberSet(this)) {
				showDialog(INVALID_PHONE_NUMBER);
				return true;
			}

			Log.i(TAG, "Displayed messages, n = " + mNMessagesDisplayed);
			if (mNMessagesDisplayed > 0) {
				sendDisplayedMessages();
				return true;
			}

			AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
			mAcdiVocaMsgs = db.createMessagesForBeneficiaries(
					SearchFilterActivity.RESULT_SELECT_NEW, null, null);
			Log.i(TAG, "Created messages, n = " + mAcdiVocaMsgs.size());
			if (AppControlManager.isRegularUser()
					|| AppControlManager.isAgriUser()) {
				db = new AcdiVocaDbHelper(this);
				mAcdiVocaMsgs.addAll(db.fetchSmsMessages(
						SearchFilterActivity.RESULT_SELECT_PENDING,
						AcdiVocaDbHelper.FINDS_STATUS_NEW, null));
			} else {
				db = new AcdiVocaDbHelper(this);
				mAcdiVocaMsgs.addAll(db.fetchSmsMessages(
						SearchFilterActivity.RESULT_SELECT_PENDING,
						AcdiVocaDbHelper.FINDS_STATUS_DONTCARE, null));
			}

			Log.i(TAG, "Appended pending messages, n = " + mAcdiVocaMsgs.size());

			int n = mAcdiVocaMsgs.size();
			Log.i(TAG, "onMenuSelected sending " + n
					+ " new beneficiary messages");
			if (n != 0) {
				showDialog(SEND_MSGS_ALERT);
			} else {
				showDialog(NO_MSGS_ALERT);
			}
			break;

		case R.id.delete_messages_menu:
			showDialog(CONFIRM_DELETE_DIALOG);
			break;
		}

		return true;
	}

	/**
	 * Helper method to send SMS messages when messages are already displayed.
	 */
	private void sendDisplayedMessages() {
		int nMsgs = mAdapter.getCount();
		Log.i(TAG, "Sending displayed messages, n= " + nMsgs);
		int k = 0;
		mAcdiVocaMsgs = new ArrayList<AcdiVocaMessage>();
		while (k < nMsgs) {
			AcdiVocaMessage acdiVocaMsg = mAdapter.getItem(k);
			mAcdiVocaMsgs.add(acdiVocaMsg);
			++k;
		}
		showDialog(SEND_MSGS_ALERT);
	}

	// /**
	// * Helper method to delete SMS messages.
	// */
	// private void deleteMessages() {
	// int nMsgs = mAdapter.getCount();
	// int nDels = 0;
	// int k = 0;
	// while (k < nMsgs) {
	// AcdiVocaMessage acdiVocaMsg = mAdapter.getItem(k);
	// int beneficiary_id = acdiVocaMsg.getBeneficiaryId();
	// Log.i(TAG, "To Delete: " + acdiVocaMsg.getSmsMessage());
	//
	// AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
	// if (db.updateMessageStatus(acdiVocaMsg,
	// AcdiVocaDbHelper.MESSAGE_STATUS_DEL))
	// ++nDels;
	// ++k;
	// }
	// Toast.makeText(this, getString(R.string.toast_deleted) + nDels +
	// getString(R.string.toast_messages), Toast.LENGTH_SHORT).show();
	// }

	/**
	 * Retrieves the Beneficiary Id from the Message string. TODO: Probably not
	 * the best way to handle this. A better way would be to have DbHelper
	 * return an array of Benefiiary objects (containing the Id) and display the
	 * message field of those objects in the list. Not sure how to do this with
	 * an ArrayAdapter??
	 * 
	 * @param message
	 * @return
	 */
	private int getBeneficiaryId(String message) {
		return Integer.parseInt(message.substring(message.indexOf(":") + 1,
				message.indexOf(" ")));
	}

	/**
	 * Cleans leading display data from the message as it is displayed in the
	 * list adapter. Current format should start with "t=" for Type. TODO: See
	 * the comment on the previous method.
	 * 
	 * @param msg
	 * @return
	 */
	private String cleanMessage(String msg) {
		String cleaned = "";
		cleaned = msg.substring(msg.indexOf(MESSAGE_START_SUBSTRING));
		return cleaned;
	}

	/**
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult = " + resultCode);
		switch (requestCode) {
		case SearchFilterActivity.ACTION_SELECT:
			if (resultCode == RESULT_CANCELED) {
				// Toast.makeText(this, "Cancel " + resultCode,
				// Toast.LENGTH_SHORT).show();
				break;
			} else {
				mMessageFilter = resultCode;
				// Toast.makeText(this, "Ok " + resultCode,
				// Toast.LENGTH_SHORT).show();
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(this);
				String distrKey = this.getResources().getString(
						R.string.distribution_point_key);
				String distributionCtr = sharedPrefs.getString(distrKey, "");

				displayMessageList(resultCode, distributionCtr);
			}
			break;
		case SearchFindsActivity.ACTION_SEARCH:
			if (resultCode == RESULT_CANCELED) {
				break;
			} else {
				mDisplayingSearchResults = true;
				String lastNameSearch = data
						.getStringExtra(SearchFindsActivity.LAST_NAME);
				String firstNameSearch = data
						.getStringExtra(SearchFindsActivity.FIRST_NAME);
				AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
				List<AcdiVocaFind> list = null;
				if (firstNameSearch == null)
					list = db
							.fetchBeneficiariesByLastName(lastNameSearch + "%");
				else if (lastNameSearch == null)
					list = db.fetchBeneficiariesByFirstName(firstNameSearch
							+ "%");
				else if (firstNameSearch != null && lastNameSearch != null)
					list = db.fetchBeneficiariesByLastAndFirstName(
							lastNameSearch + "%", firstNameSearch + "%");
				if (list.size() > 0)
					fillSearchResults(list);
				else {
					Log.e(TAG, "Sorry unable to find " + firstNameSearch + " "
							+ lastNameSearch);
					if (firstNameSearch == null)
						Toast.makeText(this,
								"Sorry unable to find " + lastNameSearch,
								Toast.LENGTH_SHORT).show();
					else if (lastNameSearch == null)
						Toast.makeText(this,
								"Sorry unable to find " + firstNameSearch,
								Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(
								this,
								"Sorry unable to find " + firstNameSearch + " "
										+ lastNameSearch, Toast.LENGTH_SHORT)
								.show();
				}
			}
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Displays SMS messages, filter by status and type.
	 */
	private void displayMessageList(int filter, String distributionCtr) {
		Log.i(TAG, "Display messages for filter " + filter
				+ " for distribution center " + distributionCtr);
		ArrayList<AcdiVocaMessage> acdiVocaMsgs = null;

		AcdiVocaDbHelper db = null;
		if (filter == SearchFilterActivity.RESULT_SELECT_NEW
				|| filter == SearchFilterActivity.RESULT_SELECT_UPDATE) { // Second
																			// arg
																			// is
																			// order
																			// by
			db = new AcdiVocaDbHelper(this);
			acdiVocaMsgs = db.createMessagesForBeneficiaries(filter, null,
					distributionCtr);
		} else if (filter == SearchFilterActivity.RESULT_SELECT_ALL
				|| filter == SearchFilterActivity.RESULT_SELECT_PENDING
				|| filter == SearchFilterActivity.RESULT_SELECT_SENT
				|| filter == SearchFilterActivity.RESULT_SELECT_ACKNOWLEDGED) {
			db = new AcdiVocaDbHelper(this);
			acdiVocaMsgs = db.fetchSmsMessages(filter,
					AcdiVocaDbHelper.FINDS_STATUS_DONTCARE, null);
		} else if (filter == SearchFilterActivity.RESULT_BULK_UPDATE) {
			db = new AcdiVocaDbHelper(this);
			acdiVocaMsgs = db.createBulkUpdateMessages(distributionCtr);
		} else {
			return;
		}

		if (acdiVocaMsgs.size() == 0) {
			mNMessagesDisplayed = 0;
			Log.i(TAG, "display Message List, N messages = "
					+ mNMessagesDisplayed);
			acdiVocaMsgs.add(new AcdiVocaMessage(AcdiVocaDbHelper.UNKNOWN_ID,
					AcdiVocaDbHelper.UNKNOWN_ID, -1, "",
					getString(R.string.no_messages), "",
					!AcdiVocaMessage.EXISTING));
		} else {
			mNMessagesDisplayed = acdiVocaMsgs.size();
			Log.i(TAG, "display Message List, N messages = "
					+ mNMessagesDisplayed);
			Log.i(TAG, "Fetched " + acdiVocaMsgs.size() + " messages");
		}
		setUpMessagesList(acdiVocaMsgs);

	}

	/**
	 * Helper method to set up a simple list view using an ArrayAdapter.
	 * 
	 * @param data
	 */
	private void setUpMessagesList(final ArrayList<AcdiVocaMessage> data) {
		if (data != null)
			Log.i(TAG, "setUpMessagesList, size = " + data.size());
		else
			Log.i(TAG, "setUpMessagesList, data = null");

		mMessageListDisplayed = true;

		mAdapter = new MessageListAdapter<AcdiVocaMessage>(this,
				R.layout.acdivoca_list_messsages, data);

		setListAdapter(mAdapter);
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String display = "";
				TextView tv = ((TextView) parent
						.findViewById(R.id.message_header));
				display += tv.getText();
				tv = ((TextView) parent.findViewById(R.id.message_body));
				display += "\n" + tv.getText();

				Toast.makeText(getApplicationContext(), display,
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	/**
	 * Called automatically by the SimpleCursorAdapter.
	 */
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		TextView tv = null; // = (TextView) view;
		long findIden = cursor.getLong(cursor
				.getColumnIndexOrThrow(AcdiVocaDbHelper.FINDS_ID));
		switch (view.getId()) {
		case R.id.messageStatusText:
			tv = (TextView) view;
			int msgstatus = cursor.getInt(cursor
					.getColumnIndex(AcdiVocaDbHelper.FINDS_MESSAGE_STATUS));
			String text = AcdiVocaDbHelper.MESSAGE_STATUS_STRINGS[msgstatus];
			if (text.equals("Unsent"))
				tv.setText(R.string.unsent);
			else if (text.equals("Sent"))
				tv.setText(R.string.sent);
			else if (text.equals("Pending"))
				tv.setText(R.string.pending);
			else if (text.equals("Acknowledged"))
				tv.setText(R.string.ack);
			else if (text.equals("Deleted"))
				tv.setText(R.string.deleted);
			else
				tv.setText(text);
			break;

		default:
			return false;
		}
		return true;
	}

	/**
	 * This method is invoked by showDialog() when a dialog window is created.
	 * It displays the appropriate dialog box, currently a dialog to confirm
	 * that the user wants to delete all the finds.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case SEND_MSGS_ALERT:
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(mActivity);
			final String phoneNumber = sp.getString(
					mActivity.getString(R.string.smsPhoneKey), "");
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.about2)
					.setTitle(
					// "#: " + phoneNumber
					// + "\n" + mAcdiVocaMsgs.size()
					// + " " + getString(R.string.send_dist_rep))
							mAcdiVocaMsgs.size() + " "
									+ getString(R.string.send_dist_rep2)
									+ " #: " + phoneNumber)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									AcdiVocaSmsManager mgr = AcdiVocaSmsManager
											.getInstance(mActivity);
									mgr.sendMessages(mActivity, mAcdiVocaMsgs);
									// mSmsReport = "Sending to " + phoneNumber
									// + " # : " + mAcdiVocaMsgs.size();
									mSmsReport = mAcdiVocaMsgs.size() + " "
											+ getString(R.string.being_sent_to)
											+ " # : " + phoneNumber;
									showDialog(SMS_REPORT);
									// finish();
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create();

		case NO_MSGS_ALERT:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.about2)
					.setTitle(R.string.no_messages_to_send)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create();

		case INVALID_PHONE_NUMBER:
			// String title = "Invalid phone number: " +
			// AcdiVocaSmsManager.getPhoneNumber(mActivity);
			String title = getString(R.string.invalid_phone_number) + " "
					+ AcdiVocaSmsManager.getPhoneNumber(mActivity);
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.about2)
					.setTitle(title)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create();
		case SMS_REPORT:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(mSmsReport)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									finish();
								}
							}).create();
			// case CONFIRM_DELETE_DIALOG:
			// return new AlertDialog.Builder(this)
			// .setIcon(R.drawable.alert_dialog_icon)
			// .setTitle(R.string.confirm_delete_messages)
			// .setPositiveButton(R.string.alert_dialog_ok,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog, int whichButton) {
			// if (mMessageListDisplayed) {
			// deleteMessages();
			// mMessageFilter = -1;
			// fillData(null);
			// }
			// dialog.cancel();
			// }
			// }).setNegativeButton(R.string.alert_dialog_cancel, new
			// DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog, int whichButton) {
			// /* User clicked Cancel so do nothing */
			// }
			// }).create();

		} // switch

		switch (id) {
		case confirm_exit:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.exit)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									finish();
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
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

	/**
	 * Called just before the dialog is shown. Need to change title to reflect
	 * the current phone number.
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		super.onPrepareDialog(id, dialog, args);
		AlertDialog d = (AlertDialog) dialog;
		Button needsabutton; // button added
		switch (id) {
		case SEND_MSGS_ALERT:
			Log.i(TAG, "onPrepareDialog id= " + id);
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(mActivity);
			final String phoneNumber = sp.getString(
					mActivity.getString(R.string.smsPhoneKey), "");
			Log.i(TAG, "phonenumber = " + phoneNumber);

			d.setTitle(
			// "#: " + phoneNumber
			// + "\n" + mAcdiVocaMsgs.size()
			// + " " + getString(R.string.send_dist_rep));
			mAcdiVocaMsgs.size() + " " + getString(R.string.send_dist_rep2)
					+ " #: " + phoneNumber);

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
	 * Adapter for displaying beneficiaries.
	 * 
	 * @param <AcdiVocaFind>
	 */
	private class BeneficiaryListAdapter<AcdiVocaFind> extends
			ArrayAdapter<AcdiVocaFind> {
		private List<AcdiVocaFind> items;

		public BeneficiaryListAdapter(Context context, int textViewResourceId,
				List<AcdiVocaFind> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.acdivoca_list_row, null);
			}
			AcdiVocaFind avFind = items.get(position);
			if (avFind != null) {
				TextView tv = (TextView) v.findViewById(R.id.row_id);
				tv.setText(""
						+ ((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaFind) avFind).id);
				tv = (TextView) v.findViewById(R.id.dossierText);
				tv.setText(((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaFind) avFind).dossier);
				tv = (TextView) v.findViewById(R.id.lastname_field);
				tv.setText(((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaFind) avFind).lastname);
				tv = (TextView) v.findViewById(R.id.firstname_field);
				tv.setText(((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaFind) avFind).firstname);
				tv = (TextView) v.findViewById(R.id.messageStatusText);
				int messageStatus = ((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaFind) avFind).message_status;
				String status = AcdiVocaDbHelper.MESSAGE_STATUS_STRINGS[messageStatus];
				tv.setText(status);
			}
			return v;
		}
	}

	/**
	 * Adapter for displaying messages.
	 * 
	 * @param <AcdiVocaMessage>
	 */
	private class MessageListAdapter<AcdiVocaMessage> extends
			ArrayAdapter<AcdiVocaMessage> {

		private ArrayList<AcdiVocaMessage> items;

		public MessageListAdapter(Context context, int textViewResourceId,
				ArrayList<AcdiVocaMessage> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.acdivoca_list_messages_row, null);
			}
			AcdiVocaMessage msg = items.get(position);
			if (msg != null) {
				TextView tt = (TextView) v.findViewById(R.id.message_header);
				TextView bt = (TextView) v.findViewById(R.id.message_body);

				String s = ((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaMessage) msg)
						.getSmsMessage();
				if (s.equals(getString(R.string.no_messages))) {
					bt.setTextColor(Color.WHITE);
					bt.setTextSize(24);
					bt.setText(((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaMessage) msg)
							.getSmsMessage());
				} else { // This case handles a real message
					if (tt != null) {
						tt.setTextColor(Color.WHITE);
						tt.setText(((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaMessage) msg)
								.getMsgHeader());
					}
					if (bt != null) {
						bt.setText(((org.hfoss.posit.android.plugin.acdivoca.AcdiVocaMessage) msg)
								.getSmsMessage());
					}
				}
			}
			return v;
		}
	}

}
