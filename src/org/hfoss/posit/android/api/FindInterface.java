package org.hfoss.posit.android.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public interface FindInterface {

	public void setGuid(String guid);
	
	/**
	 * 	getContent() returns the Find's <attr:value> pairs in a ContentValues array. 
	 *  This method assumes the Find object has been instantiated with a context
	 *  and an id.
	 *  @return A ContentValues with <key, value> pairs
	 */
	public ContentValues getContent();
		
	public Uri getFindDataUriByPosition(long findId, int position);
	
	
//	/**
//	 * Deprecated
//	 * Returns a hashmap of string key and value
//	 * this is used primarily for sending and receiving over Http, which is why it 
//	 * somewhat works as everything is sent as string eventually. 
//	 * @return
//	 */
//	public HashMap<String,String> getContentMap();
//	
//	/**
//	 * Returns a hashmap of key/value pairs represented as Strings.
//	 * This is used primarily for sending and receiving over Http.
//	 * @return
//	 */
//	public HashMap<String,String> getContentMapGuid();

	
	/**
	 * Inserts find data entries for this find
	 * @param find_data_entries
	 * @return
	 */
	public boolean insertFindDataEntriesToDB(List<ContentValues> find_data_entries);
	
	/**
	 * deletes the Find object form the DB, not including its data
	 * Call deleteFindDataEntry() to delete its data.
	 * @return whether the DB operation was successful
	 */
	public boolean delete();
	
	/**
	 * deletes the data associated with this find.
	 * @return
	 */
	public boolean deleteFindDataEntry();

	/**
	 * @return the mId
	 */
	public long getId();
	
	/**
	 * @return the guId
	 */
	public String getguId();
	
	
//	/**
//	 * NOTE: This may cause a leak because the Cursor is not closed
//	 * Get all find data entries attached to this find
//	 * @return the cursor that points to the find data
//	 */
//	public Cursor getFindDataEntriesCursor();
//	
//	public ArrayList<ContentValues> getFindDataEntriesList();

	
//	/**
//	 * @return whether or not there is find data attached to this find
//	 */
//	public boolean hasDataEntries();
//
//	public boolean deleteFindDataEntriesByPosition(int position);
//	

	/**
	 * Directly sets the Find as either Synced or not.
	 * @param status
	 */
	public void setSyncStatus(boolean status);
	
	// TODO: Test that this method works with GUIDs
	public int getRevision();
	
	/**
	 * Used for adhoc finds.  Tests whether find already exists
	 * @param guid
	 * @return
	 */
	public boolean exists(String guid);
	
	/**
	 * Tests whether the Find is synced.  This method should work with
	 * either GUIDs (Find from a server) or row iDs (Finds from the phone).
	 * If neither is set, it returns false.
	 * @return
	 */
	public boolean isSynced();

}
