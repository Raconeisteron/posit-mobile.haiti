package org.hfoss.posit.android.api;

import android.content.Context;

/**
 * Represents a specific find for a project, with a unique identifier
 * 
 */
public abstract class Find implements FindInterface {
	
	protected Find(){}
	
	/**
	 * This constructor is used for a new Find
	 * @param context is the Activity
	 */
	public Find (Context context){}
	
	/**
	 * This constructor is used for an existing Find.
	 * @param context is the Activity
	 * @param id is the Find's _id in the Sqlite DB
	 */
	public Find (Context context, long id){}
	
	/**
	 * This constructor is used for an existing Find.
	 * @param context is the Activity
	 * @param guid is a globally unique identifier, used by the server
	 *   and other devices
	 */
	public Find (Context context, String guid){}
}