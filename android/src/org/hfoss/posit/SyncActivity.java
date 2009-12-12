/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package org.hfoss.posit;

import org.hfoss.posit.utilities.Utils;
import org.hfoss.posit.web.SyncThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SyncActivity extends Activity {

	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getAction().equals(Intent.ACTION_SYNC)) {
			{
				if (!Utils.isNetworkAvailable(this)) {
					Utils.showToast(this, "Sync Error: No Network Available");
					finish();
				} else
					syncFinds();

			}
		}
	}

	/**
	 * This method starts the synchronization thread which handles the sync
	 * action.
	 */
	private void syncFinds() {
		mProgressDialog = ProgressDialog.show(this, "Synchronizing",
				"Please wait.", true, false);
		Thread syncThread = new SyncThread(this, new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == SyncThread.DONE) {
					mProgressDialog.dismiss();
					finish();
				} else if (msg.what == SyncThread.NONETWORK) {
					Utils.showToast(mProgressDialog.getContext(),
							"Sync Error:No Network Available");
					mProgressDialog.dismiss();
					finish();
				} else if (msg.what == SyncThread.SYNCERROR) {
					Utils.showToast(mProgressDialog.getContext(),
							"Sync Error: An unknown error has occurred");
					mProgressDialog.dismiss();
					finish();
				}
			}
		});
		syncThread.start();

	}
}