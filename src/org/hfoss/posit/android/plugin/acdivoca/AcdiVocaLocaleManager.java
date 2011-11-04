package org.hfoss.posit.android.plugin.acdivoca;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

public class AcdiVocaLocaleManager {
	
	public static final String TAG = "LocaleManager";
	
	public static void setDefaultLocale(Activity activity) {
		String localePref = PreferenceManager.getDefaultSharedPreferences(activity).getString("locale", "");
		Log.i(TAG, "Locale = " + localePref);
		Locale locale = new Locale(localePref); 
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
		activity.getBaseContext().getResources().updateConfiguration(config, null);
	}

}
