package org.hfoss.posit.android.api;

import android.app.Activity;

/**
* Convenience class to quickly get right find activity object without having to do lots of chained calls  
*/
public class FindActivityProvider {
	private FindActivityProvider(){} // don't instantiate this class
	
	public static Class<FindActivity> getFindActivityClass(){
		return FindPluginManager.getInstance().getFindActivityClass();
	}

	public static Class<ListFindsActivity> getListFindsActivityClass(){
		return FindPluginManager.getInstance().getListFindsActivityClass();
	}
	
	public static Class<Activity> getLoginActivityClass(){
		return FindPluginManager.getInstance().getLoginActivityClass();
	}

	public static Class<Activity> getExtraActivityClass(){
		return FindPluginManager.getInstance().getExtraActivityClass();
	}
	
	public static Class<Activity> getExtraActivityClass2(){
		return FindPluginManager.getInstance().getExtraActivityClass2();
	}
}
