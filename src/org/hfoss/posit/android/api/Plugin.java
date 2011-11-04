package org.hfoss.posit.android.api;

import android.app.Activity;

public class Plugin {

	private static final String TAG = "Plugin";
	
	private Activity mMainActivity = null;
	
	private FindFactory mFindFactory = null;
	private FindDataManager mFindDataManager = null;
	private Class<FindActivity> mFindActivityClass = null;
	private Class<ListFindsActivity> mListFindsActivityClass = null;
	
	private Class<SettingsActivity> mSettingsActivityClass = null;

	public static String mPreferences = null;
	public static String mMainIcon = null;

	public Activity getmMainActivity() {
		return mMainActivity;
	}
	public void setmMainActivity(Activity mMainActivity) {
		this.mMainActivity = mMainActivity;
	}
	public FindFactory getmFindFactory() {
		return mFindFactory;
	}
	public void setmFindFactory(FindFactory mFindFactory) {
		this.mFindFactory = mFindFactory;
	}
	public FindDataManager getmFindDataManager() {
		return mFindDataManager;
	}
	public void setmFindDataManager(FindDataManager mFindDataManager) {
		this.mFindDataManager = mFindDataManager;
	}
	public Class<FindActivity> getmFindActivityClass() {
		return mFindActivityClass;
	}
	public void setmFindActivityClass(Class<FindActivity> mFindActivityClass) {
		this.mFindActivityClass = mFindActivityClass;
	}
	public Class<ListFindsActivity> getmListFindsActivityClass() {
		return mListFindsActivityClass;
	}
	public void setmListFindsActivityClass(
			Class<ListFindsActivity> mListFindsActivityClass) {
		this.mListFindsActivityClass = mListFindsActivityClass;
	}
	public Class<SettingsActivity> getmSettingsActivityClass() {
		return mSettingsActivityClass;
	}
	public void setmSettingsActivityClass(
			Class<SettingsActivity> mSettingsActivityClass) {
		this.mSettingsActivityClass = mSettingsActivityClass;
	}
	public static String getmPreferences() {
		return mPreferences;
	}
	public static void setmPreferences(String mPreferences) {
		Plugin.mPreferences = mPreferences;
	}
	
	
	
}
