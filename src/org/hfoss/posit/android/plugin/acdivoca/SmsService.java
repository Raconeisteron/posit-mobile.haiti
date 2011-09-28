/*
 * File: SmsService.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.j256.ormlite.android.apptools.OrmLiteBaseService;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsService extends OrmLiteBaseService<AcdiVocaDbHelper> {
	public static final String TAG = "AcdiVocaSmsManager";

	private BroadcastReceiver mReceiver;
	private int nMsgsSent = 0;
	private int nMsgsPending = 0;
	private int mBroadcastsOutstanding = 0;
	private String mErrorMsg = ""; // Set to last error by BroadcastReceiver,
									// not currently used

	private SendMessagesTask sendMessagesTask;

	private ArrayList<String> mMessages;
	private String mPhoneNumber;

	/**
	 * This service is not accepting bindings.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	/**
	 * Starts the service. Creates a AsyncTask to perform the actual sending of
	 * messages in a background task.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mMessages = intent.getStringArrayListExtra("messages");
			mPhoneNumber = intent.getStringExtra("phonenumber");
			Log.i(TAG, "Started background service, phone = " + mPhoneNumber
					+ " nMessages = " + mMessages.size());

			sendMessagesTask = new SendMessagesTask();
			sendMessagesTask.execute(this);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * A broadcast receiver receives an intent about a sent message and reports
	 * whether it was sent successfully or failed.
	 * 
	 * @param receiver
	 *            that was created for an individual message
	 * @param resultCode
	 *            whether the message succeeded in being sent or not
	 * @param intent
	 *            the msgid is the ACTION of the intent
	 */
	private synchronized void handleSentMessage(BroadcastReceiver receiver,
			int resultCode, Intent intent, String smsMsg) {
		String avIdStr = intent.getAction(); // arg1.getStringExtra("msgid")
		int avIdInt = Integer.parseInt(avIdStr);

		Log.i(TAG, "Rcvd broadcast for msg: " + smsMsg);
		AcdiVocaMessage avMsg = new AcdiVocaMessage(smsMsg);
		// AcdiVocaDbHelper db = new AcdiVocaDbHelper(this);
		switch (resultCode) {
		case Activity.RESULT_OK:
			Log.d(TAG,
					"Received OK, avId = " + avIdStr + " msg:"
							+ avMsg.getSmsMessage());
			if (avIdInt < 0)
				this.getHelper().updateMessageStatusForBulkMsg(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_SENT);
			else
				this.getHelper().updateMessageStatusForNonBulkMessage(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_SENT);
			++nMsgsSent;
			mErrorMsg = "Sent OK";
			break;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			Log.e(TAG, "Received  generic failure, avId =  " + avIdStr
					+ " msg:" + avMsg.getSmsMessage());
			if (avIdInt < 0)
				this.getHelper().updateMessageStatusForBulkMsg(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			else
				this.getHelper().updateMessageStatusForNonBulkMessage(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			++nMsgsPending;
			mErrorMsg = "Generic Failure";
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			Log.e(TAG, "Received  No service, avId =  " + avIdStr + " msg:"
					+ avMsg.getSmsMessage());
			if (avIdInt < 0)
				this.getHelper().updateMessageStatusForBulkMsg(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			else
				this.getHelper().updateMessageStatusForNonBulkMessage(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			++nMsgsPending;
			mErrorMsg = "No cellular service";
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			Log.e(TAG, "Received Null PDU, avId =  " + avIdStr + " msg:"
					+ avMsg.getSmsMessage());
			if (avIdInt < 0)
				this.getHelper().updateMessageStatusForBulkMsg(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			else
				this.getHelper().updateMessageStatusForNonBulkMessage(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			++nMsgsPending;
			mErrorMsg = "Null PDU error";
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			Log.e(TAG, "Received  Radio off, avId =  " + avIdStr + " msg:"
					+ avMsg.getSmsMessage());
			if (avIdInt < 0)
				this.getHelper().updateMessageStatusForBulkMsg(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			else
				this.getHelper().updateMessageStatusForNonBulkMessage(avMsg,
						AcdiVocaDbHelper.MESSAGE_STATUS_PENDING);
			++nMsgsPending;
			mErrorMsg = "Texting is off";
			break;
		}
		logMessage(smsMsg + ", " + mErrorMsg);
	}

	/**
	 * Appends Sms Messages to a text file on the SD card.
	 * 
	 * @param sFileName
	 * @param msg
	 */
	public void logMessages(ArrayList<String> msgs) {
		try {
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/" + AcdiVocaAdminActivity.DEFAULT_LOG_DIRECTORY + "/"
					+ AcdiVocaAdminActivity.SMS_LOG_FILE);

			// FileWriter writer = new FileWriter(file);
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(file, true)));

			Iterator<String> it = msgs.iterator();
			while (it.hasNext()) {
				String msg = it.next();
				writer.println(new Date() + ": " + msg);
				Log.i(TAG, "Wrote to file: " + msg);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception writing to Log " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void logMessage(String msg) {
		try {
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/" + AcdiVocaAdminActivity.DEFAULT_LOG_DIRECTORY + "/"
					+ AcdiVocaAdminActivity.SMS_LOG_FILE);

			// FileWriter writer = new FileWriter(file);
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(file, true)));
			writer.println(new Date() + ": " + msg);
			Log.i(TAG, "Wrote to file: " + msg);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception writing to Log " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Separately threaded task to send messages.
	 * 
	 */
	class SendMessagesTask extends AsyncTask<Context, Integer, String> {
		public static final String TAG = "AsyncTask";

		private Context context;

		@Override
		protected String doInBackground(Context... contexts) {
			Log.i(TAG, "doInBackground");
			this.context = contexts[0];
			logMessages(mMessages);
			transmitMessages(context, mMessages);
			return null;
		}

		protected void transmitMessages(Context context,
				ArrayList<String> messages) {
			Iterator<String> it = messages.iterator();
			mBroadcastsOutstanding = messages.size();
			while (it.hasNext()) {
				final String message = it.next();
				String[] msgparts = message
						.split(AttributeManager.PAIRS_SEPARATOR);
				String[] firstPair = msgparts[0]
						.split(AttributeManager.ATTR_VAL_SEPARATOR);
				String msgid = firstPair[1];

				Intent sendIntent = new Intent(msgid);
				IntentFilter intentFilter = new IntentFilter(msgid);
				PendingIntent sentIntent = PendingIntent.getBroadcast(context,
						0, sendIntent, 0);

				// Not really used -- we are not processing DELIVERED
				// broadcasts.
				Intent delivered = new Intent("SMS DELIVERED");
				PendingIntent deliveryIntent = PendingIntent.getBroadcast(
						context, 0, delivered, 0);

				// This receiver will be sent a result from the radio device as
				// to whether the message was sent
				mReceiver = new BroadcastReceiver() {
					@Override
					public synchronized void onReceive(Context arg0, Intent arg1) {
						try {
							handleSentMessage(this, getResultCode(), arg1,
									message);
							--mBroadcastsOutstanding;

							unregisterReceiver(this);
							Log.i(TAG, "Broadcasts outstanding  = "
									+ mBroadcastsOutstanding);
						} catch (Exception e) {
							Log.e("BroadcastReceiver",
									"Error in onReceive for msgId "
											+ arg1.getAction());
							Log.e("BroadcastReceiver", e.getMessage());
							e.printStackTrace();
						}
					}
				};
				context.registerReceiver(mReceiver, intentFilter);

				// The length array contains 4 result:
				// length[0] the number of Sms messages required
				// length[1] the number of 7-bit code units used
				// length[2] the number of 7-bit code units remaining
				// length[3] an indicator of the encoding code unit size
				int[] length = null;
				length = SmsMessage.calculateLength(message, true);
				Log.i(TAG, "Length - 7 bit encoding = " + length[0] + " "
						+ length[1] + " " + length[2] + " " + length[3]);
				length = SmsMessage.calculateLength(message, false);
				Log.i(TAG, "Length - 16 bit encoding = " + length[0] + " "
						+ length[1] + " " + length[2] + " " + length[3]);

				// TODO: Add code to break the message into 2 or more.
				SmsManager smsMgr = SmsManager.getDefault();

				if (length[0] == 1) {
					try {
						smsMgr.sendTextMessage(mPhoneNumber, null, message,
								sentIntent, deliveryIntent);
						Log.i(TAG, "SMS Sent: " + msgid + " msg :" + message
								+ " phone= " + mPhoneNumber);
					} catch (IllegalArgumentException e) {
						Log.e(TAG,
								"IllegalArgumentException, probably phone number = "
										+ mPhoneNumber);
						mErrorMsg = e.getMessage();
						e.printStackTrace();
						return;
					} catch (Exception e) {
						Log.e(TAG, "Exception " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					int nMessagesNeeded = length[0];
					ArrayList<String> msgList = smsMgr.divideMessage(message);
					ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
					// Iterator<String> msgIt = msgList.iterator();
					for (int k = 0; k < msgList.size(); k++) {
						sentIntents.add(sentIntent);
					}

					smsMgr.sendMultipartTextMessage(mPhoneNumber, null,
							msgList, sentIntents, null);
					// smsMgr.sendTextMessage(mPhoneNumber, null, messageFrag,
					// sentIntent, deliveryIntent);
					Log.i(TAG, "SMS Sent multipart message: " + msgid
							+ " msg :" + msgList.toString() + " phone= "
							+ mPhoneNumber);
				}
			}
		}

		@Override
		protected void onCancelled() {
			Log.i(TAG, "onCancelled");
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute, broadcasts outstanding = "
					+ mBroadcastsOutstanding);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute");
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.i(TAG, "onProgressUpdate");
			super.onProgressUpdate(values);
		}
	}
}
