package org.hfoss.posit.android.api;

public abstract class FindDataManager implements FindDataManagerInterface {
	protected FindDataManager(){}
	
	public static void initIntance()throws Exception{
		throw new Exception("This method must be overwritten in subclass.");
	}
	
	public static FindDataManager getInstance() throws Exception{
		throw new Exception("This method must be overwritten in subclass.");
	}
}
