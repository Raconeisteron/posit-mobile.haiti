package org.hfoss.posit.android.plugin.acdivoca;



import org.hfoss.posit.android.R;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaUser.UserType;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class AppControlManager {
	public static final int NOT_DISTRIBUTION_STAGE = 0;
	public static final int SELECT_DISTRIBUTION_POINT = 1;
	public static final int IMPORT_BENEFICIARY_DATA = 2;  // Distribution Event Stages
	public static final int START_DISTRIBUTION_EVENT = 3;
	public static final int STOP_DISTRIBUTION_EVENT = 4;
	public static final int SEND_DISTRIBUTION_REPORT = 5;
	public static final int END_DISTRIBUTION_STAGE = 6;
	
//	private static final String[] stages = {"NOT Distribution", "Select distribution point", "Import beneficiary data", 
//			"Start distribution event", "Stop distribution event", "Send distribution report", "End Distribution Stage"};
	
	public static final String TAG = "AppControlManager";
	private static AppControlManager sInstance = null;

	private static int sLoggedInUserTypeOrdinal;
	private static UserType sLoggedInUserType;
	
	private static String sDistributionStageStr;
	private static int sDistributionStage;
	
	
	public static AppControlManager getInstance(){
		assert(sInstance != null);
		
		return sInstance;
	}
	
	// ----------------   UserType Control Methods ------------------
	// Static utility methods used to control menus and buttons based on user type.
	
	public static void setUserType(int userTypeOrdinal) {
		sLoggedInUserTypeOrdinal = userTypeOrdinal;
	}
	
	/**
	 * Returns the type of the currently logged in user
	 */
	public static UserType getUserType() {
		if (sLoggedInUserTypeOrdinal == UserType.SUPER.ordinal()) 
			return UserType.SUPER;
		else if (sLoggedInUserTypeOrdinal == UserType.ADMIN.ordinal())
			return UserType.ADMIN;
		else if (sLoggedInUserTypeOrdinal == UserType.AGRON.ordinal())
			return UserType.AGRON;
		else if (sLoggedInUserTypeOrdinal == UserType.AGRI.ordinal())
			return UserType.AGRI;
		else
			return UserType.USER;
	}
	
	/**
	 * Returns the user type as an ordinal (int) values
	 */
	public static int getUserTypeOrdinal() {
		return sLoggedInUserTypeOrdinal;
	}
	
	public static boolean isSuperUser() {
		return sLoggedInUserTypeOrdinal == UserType.SUPER.ordinal();
	}
	
	public static boolean isAdminUser() {
		return sLoggedInUserTypeOrdinal == UserType.ADMIN.ordinal();
	}
	
	public static boolean isRegularUser() {
		return sLoggedInUserTypeOrdinal == UserType.USER.ordinal();
	}
	
	public static boolean isAgriUser() {
		return sLoggedInUserTypeOrdinal == UserType.AGRI.ordinal();
	}
	
	public static boolean isAgronUser() {
		return sLoggedInUserTypeOrdinal == UserType.AGRON.ordinal();
	}
	
	// ----------------   Distribution Event Controls  ------------------
	// Static utility methods used to control menus and buttons based on user type.

	/**
	 * A distribution event can only be started by an Admin and progresses through
	 * five stages.
	 * 1) Select a distribution location (from preferences)
	 * 2) Import beneficiary data for that site.
	 * 3) Start the distribution event (and then give the phone to
	 *    an auxiliary nurse.
	 * 4) Stop the distribution event (when the event is finished).
	 * 5) Send the distribution report.
	 * 
	 * In terms of state, the app is in "distribution mode" when the 
	 * Admin selects a distribution location. This starts the process.
	 * The "Update" button will not appear (for Admin or User) unless
	 * the app is in "distribution mode". However, the "Update" button
	 * is not enabled unless the distribution has started -- i.e., the
	 * Admin has performed step #3.  In that state, the only thing a 
	 * regular user can do is update beneficiaries. 
	 * 
	 * This class hides the details of maintaining distribution state. 
	 * 
	 */

//	public static void setDistributionStage(String stageStr) {
//		sDistributionStageStr = stageStr;
//	}
	
	public static void initDistributionEvent(Context context) {
		sDistributionStage = IMPORT_BENEFICIARY_DATA;
		setSharedPreference(context, sDistributionStage);
	}
	
	public static void abortDistributionEvent(Context context) {
		sDistributionStage = END_DISTRIBUTION_STAGE;
		setSharedPreference(context, sDistributionStage);
	}

	public static void setDistributionStage(Context context, int stage) {
		sDistributionStage = stage;
		setSharedPreference(context, stage);
	}
	
	private static void setSharedPreference(Context context, int stage) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor ed = sp.edit();
		String prefKey = context.getString(R.string.distribution_event_key);
		switch (stage) {
		case SELECT_DISTRIBUTION_POINT:
			ed.putString(prefKey, context.getString(R.string.select_distr_point));
			break;
		case IMPORT_BENEFICIARY_DATA:
			ed.putString(prefKey, context.getString(R.string.import_beneficiary_file));
			break;
		case START_DISTRIBUTION_EVENT:
			ed.putString(prefKey, context.getString(R.string.start_distribution_event));
			break;
		case STOP_DISTRIBUTION_EVENT:
			ed.putString(prefKey, context.getString(R.string.stop_distribution_event));
			break;
		case SEND_DISTRIBUTION_REPORT:
			ed.putString(prefKey, context.getString(R.string.send_distribution_report));
			break;
		default:
			ed.putString(prefKey, context.getString(R.string.default_preference_string));
			break;
		}
		ed.commit();
	}
	
	public static String displayDistributionStage(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(context.getString(R.string.distribution_event_key), "");
	}
	
	public static boolean isImportDataStage() {
		return sDistributionStage == IMPORT_BENEFICIARY_DATA;
	}
	
	public static boolean isStartDistributionStage() {
		return sDistributionStage == START_DISTRIBUTION_EVENT;
	}
	
	public static boolean isStopDistributionStage() {
		return sDistributionStage == STOP_DISTRIBUTION_EVENT;
	}	

	public static boolean isDistributionStarted() {
		return sDistributionStage == STOP_DISTRIBUTION_EVENT;
	}
	
	public static boolean isSendDistributionReportStage() {
		return sDistributionStage == SEND_DISTRIBUTION_REPORT;
	}		
	
	public static int getDistributionStage() {
		return sDistributionStage;
	}
	
	public static void moveToNextDistributionStage(Context context) {
		sDistributionStage = (sDistributionStage + 1) % END_DISTRIBUTION_STAGE;
		setSharedPreference(context, sDistributionStage);
	}
	
	public static boolean isDuringDistributionEvent() {
		return sDistributionStage > SELECT_DISTRIBUTION_POINT && sDistributionStage < END_DISTRIBUTION_STAGE;
	}
	
}
