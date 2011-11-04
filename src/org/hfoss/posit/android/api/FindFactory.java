package org.hfoss.posit.android.api;

public abstract class FindFactory implements FindProviderInterface {
	protected FindFactory(){}
	
	public static void initIntance()throws Exception{
		throw new Exception("This method must be overwritten in subclass.");
	}
	
	public static FindFactory getInstance() throws Exception{
		throw new Exception("This method must be overwritten in subclass.");
	}
}
