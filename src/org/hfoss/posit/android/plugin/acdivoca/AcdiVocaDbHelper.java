/*
 * File: AcdiVocaDbHelper.java
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import org.hfoss.posit.android.R;
import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaUser.UserType;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * The class is the interface with the Database. 
 *  It controls all Db access 
 *  and directly handles all Db queries.
 */
public class AcdiVocaDbHelper extends OrmLiteSqliteOpenHelper  {

	private static final String TAG = "DbHelper";

	private static final boolean DBG = false;
	private static final String DATABASE_NAME ="posit";
	public static final int DATABASE_VERSION = 2;

	// the DAO objects we use to access the Db tables
	private Dao<AcdiVocaUser, Integer> avUserDao = null;
	private Dao<AcdiVocaFind, Integer> acdiVocaFindDao = null;
	private Dao<AcdiVocaMessage, Integer> acdiVocaMessageDao = null;

	private static Context mContext;   // The Activity
	private SQLiteDatabase mDb;  // Pointer to the DB	
	
	/**
	 * Constructor just saves and opens the Db. The Db
	 * is closed in the public methods.
	 * @param context
	 */
	public AcdiVocaDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext= context;
//		OpenHelper openHelper = new OpenHelper(this.mContext);
//		mDb = openHelper.getWritableDatabase();
	}
	
	/**
	 * Invoked automatically if the Database does not exist.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(TAG, "onCreate");
			Log.i(TAG, "Creating Tables in onCreate: ");

			AcdiVocaUser.init(connectionSource, getAvUserDao());
			
			// Beneficiary Table
			TableUtils.createTable(connectionSource, AcdiVocaFind.class);
			AcdiVocaFind find = new AcdiVocaFind();
//			find.init(AcdiVocaFind.TEST_FIND);
//			Log.i(TAG, "find = " + find.toString());
			
			// Message Table
			TableUtils.createTable(connectionSource, AcdiVocaMessage.class);
			
		} catch (SQLException e) {
			Log.e(TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(TAG, "onUpgrade");
			TableUtils.dropTable(connectionSource, AcdiVocaUser.class, true);
			TableUtils.dropTable(connectionSource, AcdiVocaFind.class, true);
			TableUtils.dropTable(connectionSource, AcdiVocaMessage.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(TAG, "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
		
	
	/**
	 * Returns the Database Access Object (DAO) for the AcdiVocaUser class. 
	 * It will create it or just give the cached value.
	 */
	public Dao<AcdiVocaUser, Integer> getAvUserDao() throws SQLException {
		if (avUserDao == null) {
			avUserDao = getDao(AcdiVocaUser.class);
		}
		return avUserDao;
	}
	
//	public int authenicateUser(String username, String password, UserType userType) {
//		
//		Dao<AcdiVocaUser, Integer> avUserDao = null;
//		try {
//			avUserDao = getAvUserDao();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		if (avUserDao != null)
//			return AcdiVocaUser.authenicateUser(avUserDao, username, password, userType);
//		else 
//			return -1;
//	}

	
	
	/**
	 * Returns the Database Access Object (DAO) for the AcdiVocaFind class. 
	 * It will create it or just give the cached value.
	 */
	public Dao<AcdiVocaFind, Integer> getAcdiVocaFindDao() throws SQLException {
		if (acdiVocaFindDao == null) {
			acdiVocaFindDao = getDao(AcdiVocaFind.class);
			//acdiVocaFindDao = DaoManager.createDao(getConnectionSource(), AcdiVocaFind.class);
		}
		return acdiVocaFindDao;
	}
	
	/**
	 * Returns the Database Access Object (DAO) for the AcdiVocaFind class. 
	 * It will create it or just give the cached value.
	 */
	public Dao<AcdiVocaMessage, Integer> getAcdiVocaMessageDao() throws SQLException {
		if (acdiVocaMessageDao == null) {
			acdiVocaMessageDao = getDao(AcdiVocaMessage.class);
		}
		return acdiVocaMessageDao;
	}
	


	/**
	 * For the message table.
	 */
	public static final String MESSAGE_TABLE = "sms_message_log";
	public static final String MESSAGE_ID = "_id";
	public static final int UNKNOWN_ID = -9999;
	public static final String MESSAGE_BENEFICIARY_ID = AttributeManager.MESSAGE_BENEFICIARY_ID;  // Row Id in Beneficiary table
	public static final String MESSAGE_TEXT = AttributeManager.MESSAGE_TEXT;
	public static final String MESSAGE_STATUS = AttributeManager.FINDS_MESSAGE_STATUS; 
	public static final String[] MESSAGE_STATUS_STRINGS = {"Unsent", "Pending", "Sent", "Ack", "Deleted"};
	public static final int MESSAGE_STATUS_UNSENT = 0;
	public static final int MESSAGE_STATUS_PENDING = 1;
	public static final int MESSAGE_STATUS_SENT = 2;
	public static final int MESSAGE_STATUS_ACK = 3;
	public static final int MESSAGE_STATUS_DEL = 4;
	public static final String MESSAGE_CREATED_AT = AttributeManager.MESSAGE_CREATED_AT;
	public static final String MESSAGE_SENT_AT = AttributeManager.MESSAGE_SENT_AT;
	public static final String MESSAGE_ACK_AT = AttributeManager.MESSAGE_ACK_AT;
	
	/**
	 *  For the Beneficiary table
	 */
	public static final String FINDS_TABLE = "acdi_voca_finds";
	public static final String FINDS_ID = "_id";
	public static final String FINDS_ORMLIST_ID = "id";
	public static final String FINDS_DOSSIER = AttributeManager.FINDS_DOSSIER;
	public static final String FINDS_PROJECT_ID = "project_id";
	public static final String FINDS_NAME = "name";
	
	public static final String FINDS_TYPE = AttributeManager.FINDS_TYPE;    
	public static final int FINDS_TYPE_MCHN = 0;
	public static final int FINDS_TYPE_AGRI = 1;
	public static final int FINDS_TYPE_BOTH = 2;
	public static final String[] FIND_TYPE_STRINGS = {"MCHN", "AGRI", "BOTH"};  // For display purpose

	public static final String FINDS_STATUS = AttributeManager.FINDS_STATUS;
	public static final int FINDS_STATUS_NEW = 0;      // New registration, no Dossier ID
	public static final int FINDS_STATUS_UPDATE = 1;   // Update, imported from TBS, with Dossier ID
	public static final int FINDS_STATUS_DONTCARE = -1;  
	public static final String[] FIND_STATUS_STRINGS = {"New", "Update"};  // For display purpose

	public static final String FINDS_MESSAGE_ID = AttributeManager.FINDS_MESSAGE_ID;
	public static final String FINDS_MESSAGE_STATUS = MESSAGE_STATUS;

	public static final String FINDS_FIRSTNAME = AttributeManager.FINDS_FIRSTNAME;
	public static final String FINDS_LASTNAME = AttributeManager.FINDS_LASTNAME;

	public static final String FINDS_ADDRESS = AttributeManager.FINDS_ADDRESS;
	public static final String FINDS_DOB = AttributeManager.FINDS_DOB;
	public static final String FINDS_SEX = AttributeManager.FINDS_SEX;
	public static final String FINDS_AGE = "age";

	public static final String FINDS_BENEFICIARY_CATEGORY = AttributeManager.FINDS_BENEFICIARY_CATEGORY;
	public static final String FINDS_HOUSEHOLD_SIZE = AttributeManager.FINDS_HOUSEHOLD_SIZE;

	public static final String FINDS_DISTRIBUTION_POST = AttributeManager.FINDS_DISTRIBUTION_POST;
	public static final String FINDS_Q_MOTHER_LEADER = AttributeManager.FINDS_Q_MOTHER_LEADER; // "mother_leader";
	public static final String FINDS_Q_VISIT_MOTHER_LEADER = AttributeManager.FINDS_Q_VISIT_MOTHER_LEADER; // "visit_mother_leader";
	public static final String FINDS_Q_PARTICIPATING_AGRI = AttributeManager.FINDS_Q_PARTICIPATING_AGRI; // "pariticipating_agri";
	public static final String FINDS_Q_RELATIVE_AGRI = AttributeManager.FINDS_Q_RELATIVE_AGRI; // "pariticipating_agri";
	public static final String FINDS_Q_PARTICIPATING_BENE = AttributeManager.FINDS_Q_PARTICIPATING_BENE; // "pariticipating_agri";
	public static final String FINDS_Q_RELATIVE_BENE = AttributeManager.FINDS_Q_RELATIVE_BENE; // "pariticipating_agri";
	
	public static final String FINDS_NAME_AGRI_PARTICIPANT = AttributeManager.FINDS_NAME_AGRI_PARTICIPANT; // "name_agri_paricipant";

	
	public static final String FINDS_GUID = "guid";    // Globally unique ID


	public static final String FINDS_ZERO = "0";
	public static final String FINDS_ONE = "1";

	// For the agriculture registration form
	public static final String FINDS_LAND_AMOUNT = AttributeManager.FINDS_LAND_AMOUNT; // "amount_of_land";	
	public static final String FINDS_IS_FARMER = AttributeManager.FINDS_IS_FARMER; //  "is_farmer";
	public static final String FINDS_IS_MUSO = AttributeManager.FINDS_IS_MUSO;  // "is_MUSO";
	public static final String FINDS_IS_RANCHER = AttributeManager.FINDS_IS_RANCHER;  //  "is_rancher";
	public static final String FINDS_IS_STOREOWN = AttributeManager.FINDS_IS_STOREOWN; //  "is_store_owner";
	public static final String FINDS_IS_FISHER = AttributeManager.FINDS_IS_FISHER;  // "is_fisher";
	public static final String FINDS_IS_OTHER = AttributeManager.FINDS_IS_OTHER;  // "is_other";
	public static final String FINDS_IS_ARTISAN = AttributeManager.FINDS_IS_ARTISAN; // "is_artisan";
	
	public static final String FINDS_HAVE_VEGE = AttributeManager.FINDS_HAVE_VEGE; //  "have_vege";
	public static final String FINDS_HAVE_CEREAL = AttributeManager.FINDS_HAVE_CEREAL;  //  "have_cereal";
	public static final String FINDS_HAVE_TUBER = AttributeManager.FINDS_HAVE_TUBER;  // "have_tuber";
	public static final String FINDS_HAVE_TREE = AttributeManager.FINDS_HAVE_TREE; // "have_tree";
	public static final String FINDS_HAVE_GRAFTING = AttributeManager.FINDS_HAVE_GRAFTING; // "have_grafting";
	public static final String FINDS_HAVE_HOUE = AttributeManager.FINDS_HAVE_HOUE;  //  "have_houe";
	public static final String FINDS_HAVE_PIOCHE = AttributeManager.FINDS_HAVE_PIOCHE;  // "have_pioche";
	public static final String FINDS_HAVE_BROUETTE = AttributeManager.FINDS_HAVE_BROUETTE; // "have_brouette";
	public static final String FINDS_HAVE_MACHETTE = AttributeManager.FINDS_HAVE_MACHETTE; //  "have_machette";
	public static final String FINDS_HAVE_SERPETTE = AttributeManager.FINDS_HAVE_SERPETTE;  // "have_serpette";
	public static final String FINDS_HAVE_PELLE = AttributeManager.FINDS_HAVE_PELLE;  // "have_pelle";
	public static final String FINDS_HAVE_BARREAMINES = AttributeManager.FINDS_HAVE_BARREAMINES; // "have_barreamines";
	public static final String FINDS_RELATIVE_1 = AttributeManager.FINDS_RELATIVE_1;  // "relative_1";
	public static final String FINDS_RELATIVE_2 = AttributeManager.FINDS_RELATIVE_2;  // "relative_2";
	public static final String FINDS_HAVE_COFFEE = AttributeManager.FINDS_HAVE_COFFEE; //  "have_vege";

	public static final String FINDS_IS_FAO = AttributeManager.FINDS_IS_FAO;// "partner_fao";
	public static final String FINDS_IS_SAVE = AttributeManager.FINDS_IS_SAVE;// "partner_save";
	public static final String FINDS_IS_CROSE = AttributeManager.FINDS_IS_CROSE;// "partner_crose";
	public static final String FINDS_IS_PLAN = AttributeManager.FINDS_IS_PLAN;// "partner_plan";
	public static final String FINDS_IS_MARDNR = AttributeManager.FINDS_IS_MARDNR;// "partner_mardnr";
	public static final String FINDS_IS_OTHER_ORG = AttributeManager.FINDS_IS_OTHER_ORG;// "partner_other";
	
	public static final String FINDS_MALNOURISHED = AttributeManager.FINDS_MALNOURISHED;  // "MALNOURISHED";
	public static final String FINDS_PREVENTION = AttributeManager.FINDS_PREVENTION;     // "PREVENTION";
	public static final String FINDS_EXPECTING = AttributeManager.FINDS_EXPECTING;   // "EXPECTING";
	public static final String FINDS_NURSING = AttributeManager.FINDS_NURSING;      // "NURSING";
	
    public static final String FINDS_MALE = AttributeManager.FINDS_MALE;          // "MALE";
    public static final String FINDS_FEMALE = AttributeManager.FINDS_FEMALE;        // "FEMALE";
    public static final String FINDS_YES = AttributeManager.FINDS_YES;           // "YES";
    public static final String FINDS_NO = AttributeManager.FINDS_NO;            // "NO";
    public static final String FINDS_TRUE = AttributeManager.FINDS_TRUE;       // "TRUE";
    public static final String FINDS_FALSE = AttributeManager.FINDS_FALSE;      // "FALSE";
    public static final String FINDS_COMMUNE_SECTION = AttributeManager.LONG_COMMUNE_SECTION;
    
	public static final String FINDS_Q_PRESENT = AttributeManager.FINDS_Q_PRESENT;   // "Present";
	public static final String FINDS_Q_TRANSFER = AttributeManager.FINDS_Q_TRANSFER;   //"Transfer";
	public static final String FINDS_Q_MODIFICATION = AttributeManager.FINDS_Q_MODIFICATIONS;  // "Modifications";
	public static final String FINDS_MONTHS_REMAINING = "MonthsRemaining";
	public static final String FINDS_Q_CHANGE = AttributeManager.FINDS_Q_CHANGE;   //"ChangeInStatus";   // Added to incorporated changes to beneficiary type
	public static final String FINDS_CHANGE_TYPE = AttributeManager.FINDS_CHANGE_TYPE;   //"ChangeType";
	
	public static final String FINDS_DESCRIPTION = "description";
	public static final String FINDS_LATITUDE = "latitude";
	public static final String FINDS_LONGITUDE = "longitude";
	public static final String FINDS_TIME = "timestamp";
	public static final String FINDS_MODIFY_TIME = "modify_time";
	public static final String FINDS_SYNCED = "synced";
	public static final String FINDS_REVISION = "revision";
	public static final String FINDS_IS_ADHOC = "is_adhoc";
	public static final String FINDS_ACTION = "action";
	public static final String FINDS_DELETED = "deleted";
	public static final int FIND_IS_SYNCED = 1;
	public static final int FIND_NOT_SYNCED = 0;

	public static final int DELETE_FIND = 1;
	public static final int UNDELETE_FIND = 0;
	public static final String WHERE_NOT_DELETED = " " + FINDS_DELETED + " != " + DELETE_FIND + " ";
	public static final String DATETIME_NOW = "`datetime('now')`";

	public static final String FINDS_HISTORY_TABLE = "acdi_voca_finds_history";
	public static final String HISTORY_ID = "_id" ;

	// Fields for reading the beneficiaries.txt file. The numbers correspond to
	// the columns.  These might need to be changed.
	// Here's the file header line (line 1)
	//	*No dossier,Nom,Prenom,Section Communale,Localite beneficiaire,Date entree,Date naissance,Sexe,Categorie,Poste distribution,
	//	068MP-FAT, Balthazar,Denisana,Mapou,Saint Michel,2010/08/03,1947/12/31, F,Enfant Prevention,Dispensaire Mapou,
	private static final int FIELD_DOSSIER = 0;
	private static final int FIELD_LASTNAME = 1;
	private static final int FIELD_FIRSTNAME = 2;
	private static final int FIELD_SECTION = 3;
	private static final int FIELD_LOCALITY = 4;
	private static final int FIELD_ENTRY_DATE = 5;
	private static final int FIELD_BIRTH_DATE = 6;
	private static final int FIELD_SEX = 7;
	private static final int FIELD_CATEGORY = 8;
	private static final int FIELD_DISTRIBUTION_POST = 9;
	private static final String COMMA= ",";

	private static final int AGRI_FIELD_DOSSIER = 0;
	private static final int AGRI_FIELD_LASTNAME = 1;
	private static final int AGRI_FIELD_FIRSTNAME = 2;
	private static final int AGRI_FIELD_COMMUNE = 3;
	private static final int AGRI_FIELD_SECTION = 4;
	private static final int AGRI_FIELD_LOCALITY = 5;
	private static final int AGRI_FIELD_ENTRY_DATE = 6;
	private static final int AGRI_FIELD_BIRTH_DATE = 7;
	private static final int AGRI_FIELD_SEX = 8;
	private static final int AGRI_FIELD_CATEGORY = 9;
	private static final int AGRI_FIELD_NUM_PERSONS  = 10;


	
	

	/**
	 * Inserts an array of beneficiaries input from AcdiVoca data file.
	 * NOTE:  The Android date picker stores months as 0..11, so
	 *  we have to adjust dates.
	 * TODO: Refactor, should this move to Find class?
	 * @param beneficiaries
	 * @return
	 */
	public int addAgriBeneficiaries(String[] beneficiaries) {
		Log.i(TAG, "Adding " + beneficiaries.length + " AGRI beneficiaries");
		String fields[] = null;
		int count = 0;
		int result = 0;
		
		AcdiVocaFind avFind = null;
		for (int k = 0; k < beneficiaries.length; k++) {
			avFind = new AcdiVocaFind();

			fields = beneficiaries[k].split(COMMA);
			avFind.type =   AcdiVocaDbHelper.FINDS_TYPE_AGRI;
			avFind.status = AcdiVocaDbHelper.FINDS_STATUS_UPDATE;
			avFind.dossier = fields[AGRI_FIELD_DOSSIER];
			avFind.lastname = fields[AGRI_FIELD_LASTNAME];
			avFind.firstname =  fields[AGRI_FIELD_FIRSTNAME];
			avFind.address = fields[AGRI_FIELD_LOCALITY];
			String adjustedDate = translateDateForDatePicker(fields[AGRI_FIELD_BIRTH_DATE]);
			avFind.dob = adjustedDate;
			String adjustedSex = translateSexData(fields[AGRI_FIELD_SEX]);
			avFind.sex = adjustedSex;
			String adjustedCategory = translateCategoryData(fields[AGRI_FIELD_CATEGORY]);
			avFind.beneficiary_category = adjustedCategory;
			avFind.household_size = fields[AGRI_FIELD_NUM_PERSONS];
			
			Dao<AcdiVocaFind, Integer> acdiVocaFindDao;
			try {
				acdiVocaFindDao = getAcdiVocaFindDao();
				result = acdiVocaFindDao.create(avFind);
				if (result == 1) 
					++count;
				else 
					Log.e(TAG, "Error creating beneficiary entry " + avFind.toString());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.i(TAG, "Inserted to Db " + count + " Beneficiaries");
		return count;
	}
	
	/**
	 * Inserts an array of beneficiaries input from AcdiVoca data file.
	 * NOTE:  The Android date picker stores months as 0..11, so
	 *  we have to adjust dates.
	 * TODO: Refactor, should this move to Find class?
	 * @param beneficiaries
	 * @return
	 */
	public int addUpdateBeneficiaries(String[] beneficiaries) {
		Log.i(TAG, "Adding " + beneficiaries.length + " MCHN beneficiaries");
		String fields[] = null;
		int count = 0;
		int result = 0;

		AcdiVocaFind avFind = null;
		for (int k = 0; k < beneficiaries.length; k++) {
			avFind = new AcdiVocaFind();
			
			fields = beneficiaries[k].split(COMMA);
			avFind.type =  AcdiVocaDbHelper.FINDS_TYPE_MCHN;
			avFind.status = AcdiVocaDbHelper.FINDS_STATUS_UPDATE;
			avFind.dossier = fields[FIELD_DOSSIER];
			avFind.lastname = fields[FIELD_LASTNAME];
			avFind.firstname =  fields[FIELD_FIRSTNAME];
			avFind.commune_section = AttributeManager.getMapping(fields[FIELD_SECTION]);
			avFind.address = fields[FIELD_LOCALITY];
			String adjustedDate = translateDateForDatePicker(fields[FIELD_BIRTH_DATE]);
			avFind.dob = adjustedDate;
			String adjustedSex = translateSexData(fields[FIELD_SEX]);
			avFind.sex = adjustedSex;
			String adjustedCategory = translateCategoryData(fields[FIELD_CATEGORY]);
			avFind.beneficiary_category = adjustedCategory;
			avFind.distribution_post = fields[FIELD_DISTRIBUTION_POST];

			Dao<AcdiVocaFind, Integer> acdiVocaFindDao;
			try {
				acdiVocaFindDao = getAcdiVocaFindDao();
				result = acdiVocaFindDao.create(avFind);
				if (result == 1) 
					++count;
				else 
					Log.e(TAG, "Error creating beneficiary entry " + avFind.toString());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.i(TAG, "Inserted to Db " + count + " Beneficiaries");
		return count;
	}

	/**
	 * Translates attribute name from Haitian to English.  Beneficiaries.txt 
	 * 	data file represents categories in Haitian.  
	 * @param date
	 * @return
	 */
	private String translateCategoryData(String category) {
		if (category.equals(AttributeManager.FINDS_MALNOURISHED_HA))
			return AttributeManager.FINDS_MALNOURISHED;
		else if (category.equals(AttributeManager.FINDS_EXPECTING_HA))
			return AttributeManager.FINDS_EXPECTING;
		else if (category.equals(AttributeManager.FINDS_NURSING_HA))
			return AttributeManager.FINDS_NURSING;		
		else if (category.equals(AttributeManager.FINDS_PREVENTION_HA))
			return AttributeManager.FINDS_PREVENTION;	
		else return category;
	}
	
	/**
	 * Beneficiaries.txt represents sex as 'M' or 'F'.  We represent them as
	 * 'FEMALE' or 'MALE'
	 * @param date
	 * @return
	 */
	private String translateSexData(String sex) {
		if (sex.equals(AttributeManager.ABBREV_FEMALE))
			return AttributeManager.FINDS_FEMALE;
		else if (sex.equals(AttributeManager.ABBREV_MALE))
			return AttributeManager.FINDS_MALE;
		else return sex;
	}
	
	/**
	 * The Android date picker stores dates as 0..11. Weird.
	 * So we have to adjust dates input from data file.
	 * @param date
	 * @return
	 */
	@SuppressWarnings("finally")
	private String translateDateForDatePicker(String date) {
		try {
			String[] yrmonday = date.split("/");
			date =  yrmonday[0] + "/" + (Integer.parseInt(yrmonday[1]) - 1) + "/" + yrmonday[2];
		} catch (Exception e) {
			Log.i(TAG, "Bad date = " + date + " " + e.getMessage());
			e.printStackTrace();
		} finally {
			return date;
		}
	}
	
	/**
	 * This function changes the date format from 0...11 to 1..12 format for the SMS Reader.
	 * @param date
	 * @return
	 */
	@SuppressWarnings("finally")
	public static String adjustDateForSmsReader(String date) {
		try { 
			String[] yrmonday = date.split("/");
			date =  yrmonday[0] + "/" + (Integer.parseInt(yrmonday[1]) + 1) + "/" + yrmonday[2];	
		} catch (Exception e) {
			Log.i(TAG, "Bad date = " + date + " " + e.getMessage());
			e.printStackTrace();
		} finally {
			return date;
		}
	}


	/**
	 * Helper method to process ACKs for bulk update messages. Bulk updates
	 * are messages of the form AV=msgid,d1&d2&...&dN,  where d's are dossier numbers.
	 * Bulk update messages are stored in the message table. 
	 * 
	 * Uses the the message id to look up the bulk update message in the message table
	 * and updates the FINDS table with STATUS = ACK for each dossier number in the 
	 * bulk message. Finally, updates the MESSAGE table with STATUS=ACK.
	 * 
	 * @param acdiVocaMsg the message constructed by SmsManager.
	 * @return
	 */
	private boolean recordAcknowledgedBulkMessage(AcdiVocaMessage acdiVocaMsg) {
		int msg_id = acdiVocaMsg.getMessageId();
		int beneId = acdiVocaMsg.getBeneficiaryId();
		int rows = 0;
		int count = 0;
		Log.i(TAG, "Recording Bulk ACK, msg_id = " + msg_id);
		
		// Update the message status
		boolean result = updateMessageStatus(beneId, msg_id,MESSAGE_STATUS_ACK);
		
		Dao<AcdiVocaMessage, Integer> avMsgDao = null;
		AcdiVocaMessage avMsg = null;
		try {
			avMsgDao = getAcdiVocaMessageDao();
			avMsg = avMsgDao.queryForId(msg_id);  // Retrieve the message
			if (avMsg != null) {

				// Update each beneficiary
				Dao<AcdiVocaFind, Integer> avFindDao = getAcdiVocaFindDao();
				AcdiVocaFind avFind = null;
				String msg = avMsg.smsMessage;
				String dossiers[] = msg.split(AttributeManager.LIST_SEPARATOR);
				for (int k = 0; k < dossiers.length; k++) {
					avFind = fetchBeneficiaryByDossier(dossiers[k], null);
					avFind.message_status = MESSAGE_STATUS_ACK;
					rows = avFindDao.update(avFind);
					if (rows == 1) 
						++count;
				}
			} else {
				Log.e(TAG, "Error, unable to retrieve message id = " + msg_id);
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return count > 0;
	}

	
	/**
	 * Record an incoming ACK from the modem. For normal messages, the modem 
	 * sends back the beneficiary id for the sent message.  This method looks 
	 * up the message id and updates the MESSAGE and FIND tables. 
	 * 
	 * NOTE: This method also receives ACKs for bulk messages, which it passes
	 * to a helper method. 
	 * 
	 * @param acdiVocaMsg the message constructed by SmsManager.
	 * @return
	 */
	public boolean recordAcknowledgedMessage(AcdiVocaMessage acdiVocaMsg) {
		int beneficiary_id = acdiVocaMsg.getBeneficiaryId();
		int msg_id = acdiVocaMsg.messageId;
		Log.i(TAG, "Recording ACK, bene_id = " + acdiVocaMsg.getBeneficiaryId() + " msg_id " + msg_id);

		AcdiVocaFind avFind = null; 
		boolean result = false;

		// Update the Beneficiary table for this message
		if (beneficiary_id < 0) {  // This is an ACK for a bulk message, msg_id is -beneficiary_id
			return recordAcknowledgedBulkMessage(acdiVocaMsg);
		} else {  // In this case, beneficiary id is known and message Id must be looked up
			avFind = fetchFindById(beneficiary_id, null);
			if (avFind != null) {
				msg_id = avFind.message_id;
				result = updateBeneficiaryMessageStatus(beneficiary_id, msg_id,MESSAGE_STATUS_ACK);
				if (result) {
					Log.d(TAG, "Updated ACK, for beneficiary_id = " +  beneficiary_id);
					result = updateMessageStatus(beneficiary_id, msg_id, MESSAGE_STATUS_ACK);
					if (result) {
						Log.d(TAG, "Updated ACK, for msg_id = " +  msg_id);
					} else {
						Log.e(TAG, "Unable to process ACK, for msg_id = " +  msg_id);	
					}
				} else {
					Log.e(TAG, "Unable to process ACK, for beneficiary_id = " +  beneficiary_id);
				}
			} else {
				Log.e(TAG, "Error retrieving beneficiary_id = " +  beneficiary_id);
			}
		}
		return result;
	}

	
	/**
	 * Updates beneficiary table for bulk dossier numbers -- i.e. n1&n2&...&nM.
	 * Bulk messages are sent to record absentees at AcdiVoca distribution events.
	 * @param acdiVocaMsg
	 * @return
	 */
	private synchronized int updateBeneficiaryTableForBulkIds(AcdiVocaMessage acdiVocaMsg, int msgId, int status) {
		String msg = acdiVocaMsg.getSmsMessage();
		Log.i(TAG, "updateBeneficiary Table, sms = " + msg);
		boolean result = false;
		int rows = 0;
		String dossiers[] = msg.split(AttributeManager.LIST_SEPARATOR);
	
		for (int k = 0; k < dossiers.length; k++) {
			
			AcdiVocaFind avFind = fetchBeneficiaryByDossier(dossiers[k], null);
			if (avFind != null) {
				result = updateBeneficiaryMessageStatus(avFind.id, msgId, status);
				if (result) {
					Log.d(TAG, "Updated beneficiary id = " + avFind.id + " to status = " + status);
					++rows;
				}
				else
					Log.e(TAG, "Unable to update beneficiary id = " + avFind.id + " to status = " + status);

			} 
		}
		return rows;
	}

	/**
	 * Handles updating the message table and beneficiary table for bulk messages. 
	 * In this case the msg_id is known. So we directly update the message table then
	 * update the beneficiary table for each of the DOSIER numbers. 
	 * @param acdiVocaMsg
	 * @param status
	 * @param huh
	 * @return
	 */
	public boolean updateMessageStatusForBulkMsg(AcdiVocaMessage acdiVocaMsg, int status) {
		Log.i(TAG, "Updating, msg_id " + acdiVocaMsg.getMessageId() + 
				" bene_id = " +  " to status = " + status);
		
		int beneficiary_id = acdiVocaMsg.getBeneficiaryId();
		int msg_id = acdiVocaMsg.getMessageId();
		int rows = 0;
		boolean result = updateMessageStatus(beneficiary_id, msg_id, status );
		if (result) {
			rows = updateBeneficiaryTableForBulkIds(acdiVocaMsg, msg_id, status);
		}
		return result;
	}
	
	/**
	 * Updates the message status in the message table and Finds table for
	 * distribution update messages.  In this case the msg_id is UNKNOWN and
	 * the beneficiary is KNOWN.
	 * @param message the SMS message
	 * @param status the new status
	 * @return
	 */
	public boolean updateMessageStatusForNonBulkMessage(AcdiVocaMessage acdiVocaMsg, int status) {
		Log.i(TAG, "Updating, msg_id " + acdiVocaMsg.getMessageId() + 
				" bene_id = " + acdiVocaMsg.getBeneficiaryId() + " to status = " + status);
		
		int beneficiary_id = acdiVocaMsg.getBeneficiaryId();
		int msg_id = acdiVocaMsg.getMessageId();
		
		boolean result = updateBeneficiaryMessageStatus(beneficiary_id, msg_id, status);
		if (result) {
			result = updateMessageStatus(beneficiary_id, msg_id, status);
		}
		return result;
	}

	/**
	 * Updates Beneficiary table for processed message.
	 * @param beneficiary_id
	 * @param msg_id
	 * @param msgStatus
	 * @return
	 */
	public boolean updateBeneficiaryMessageStatus(int beneficiary_id, int msg_id, int msgStatus) {
		Log.i(TAG, "Updating beneficiary = " + beneficiary_id + " for message " + msg_id + " status=" + msgStatus);

		Dao<AcdiVocaFind, Integer> avFindDao = null;
		AcdiVocaFind avFind = null;
		int result = 0;
		try {
			avFindDao = getAcdiVocaFindDao();
			avFind = avFindDao.queryForId(beneficiary_id);  // Retrieve the beneficiary
			if (avFind != null) {
				avFind.message_status = msgStatus;
				avFind.message_id = msg_id;
				result = avFindDao.update(avFind);
			} else {
				Log.e(TAG, "Unable to retrieve beneficiary id = " + beneficiary_id ); 
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if (result == 1) 
			Log.d(TAG, "Updated beneficiary id = " + beneficiary_id + " for message " + msg_id + " status=" + msgStatus); 
		return result == 1;
	}
	
	
	/**
	 * Creates a new entry in the message table and update the FINDS
	 * table to point to the message.
	 * @return
	 */
	public long createNewMessageTableEntry(AcdiVocaMessage acdiVocaMsg, int beneficiary_id, int msgStatus) {
		Log.i(TAG, "createNewMessage for beneficiary = " + beneficiary_id + " status= " + msgStatus);

		Date now = new Date(System.currentTimeMillis());

		Dao<AcdiVocaMessage, Integer> avAvMsgDao = null;
		int result = 0;
		try {
			avAvMsgDao = getAcdiVocaMessageDao();
			acdiVocaMsg.message_created_at = now;
			result = avAvMsgDao.create(acdiVocaMsg);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if (result != 1) {
			Log.i(TAG, "Unable to insert NEW message for beneficiary id= " + beneficiary_id);
		} else {
			Log.i(TAG, "Inserted NEW message, id= " + acdiVocaMsg.id + " bene_id=" + beneficiary_id); 
			if (!updateBeneficiaryMessageStatus(beneficiary_id, acdiVocaMsg.id, msgStatus))
				Log.e(TAG, "Unable to update beneficiary id = " + beneficiary_id + " for message " + acdiVocaMsg.id);
		} 
		return acdiVocaMsg.id;  // Return the message Id.
	}

	/**
	 * Updates the message table for an existing message.
	 * @return
	 */
	public boolean updateMessageStatus(int beneficiary_id, int msg_id, int status ) {
		Log.i(TAG, "Updating message, bene_id = " + beneficiary_id + " message = " + msg_id + " status=" + status);

		int result = 0;
		Date now = new Date(System.currentTimeMillis());
//		String now = new Date(System.currentTimeMillis()).toString()
//		+ " " + new Time(System.currentTimeMillis()).toString();
		Log.i(TAG, "Time now = " + now);
		
		Dao<AcdiVocaMessage, Integer> avMsgDao = null;
		AcdiVocaMessage avMsg = null;
		try {
			avMsgDao = getAcdiVocaMessageDao();
			avMsg = avMsgDao.queryForId(msg_id);  // Retrieve the message
			if (avMsg != null) {
				avMsg.setMsgStatus(status);
				avMsg.setBeneficiaryId(beneficiary_id);
				if (status == MESSAGE_STATUS_SENT) {
					avMsg.message_sent_at = now;
				} else if (status == MESSAGE_STATUS_ACK) 
					avMsg.message_ack_at = now;
				result = avMsgDao.update(avMsg);
			} else {
				Log.e(TAG, "Unable to retrieve message id = " + msg_id ); 	
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if (result == 1) 
			Log.d(TAG, "Updated message id = " + msg_id + " for message " + msg_id + " status=" + status);
		return result == 1;
	}
	
	/**
	 * Updates the Beneficiary's row in the Beneficiary Table.
	 * @param avFind
	 * @return
	 */
	public boolean updateBeneficiary(AcdiVocaFind avFind) {
		Log.i(TAG, "Updating beneficiary " + avFind.id);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		int result = 0;
		try {
			avFindDao = getAcdiVocaFindDao();
			result = avFindDao.update(avFind);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return result == 1;
	}
	
	/**
	 * Inserts a new Beneficiary's row in the Beneficiary Table.
	 * @param avFind
	 * @return
	 */
	public boolean insertBeneficiary(AcdiVocaFind avFind) {
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		int result = 0;
		try {
			avFindDao = getAcdiVocaFindDao();
			result = avFindDao.create(avFind);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return result == 1;
	}

	/**
	 * Retrieves a Find object by its dossier number.
	 * @param dossier the Find's dossier number
	 * @param columns an array of column names, can be left null
	 * @return
	 */
	public AcdiVocaFind fetchBeneficiaryByDossier(String dossier, String[] columns) {		
		Log.i(TAG, "Fetching beneficiary, dossier = " + dossier);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		AcdiVocaFind avFind = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.eq(FINDS_DOSSIER, dossier);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			avFind = avFindDao.queryForFirst(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return avFind;
	}
	
	/**
	 * Retrieves a Find object by its last name.
	 * @param lastname 
	 * @return
	 */
	public List<AcdiVocaFind> fetchBeneficiariesByLastName(String lastName) {		
		Log.i(TAG, "Fetching beneficiary, lastname = " + lastName);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.like(FINDS_LASTNAME, lastName);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Retrieves a Find object by its last name.
	 * @param lastname 
	 * @return
	 */
	public List<AcdiVocaFind> fetchBeneficiariesByFirstName(String firstName) {		
		Log.i(TAG, "Fetching beneficiary, lastname = " + firstName);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.like(FINDS_FIRSTNAME, firstName);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Retrieves a Find object by its last name.
	 * @param lastname 
	 * @return
	 */
	public AcdiVocaFind fetchBeneficiaryByLastName(String lastName) {		
		Log.i(TAG, "Fetching beneficiary, lastname = " + lastName);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		AcdiVocaFind avFind = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.like(FINDS_LASTNAME, lastName);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			avFind = avFindDao.queryForFirst(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return avFind;
	}
	
//	7/25/11
	public AcdiVocaFind fetchBeneficiaryByLastAndFirstName(String lastName, String firstName) {		
		Log.i(TAG, "Fetching beneficiary, lastname = " + lastName);
		Log.i(TAG, "Fetching beneficiary, firstname = " + firstName);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		AcdiVocaFind avFind = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.like(FINDS_LASTNAME, lastName);
			where.and();
			where.like(FINDS_FIRSTNAME, firstName);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			avFind = avFindDao.queryForFirst(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return avFind;
	}
	
//	7/25/11
	public List<AcdiVocaFind> fetchBeneficiariesByLastAndFirstName(String lastName, String firstName) {		
		Log.i(TAG, "Fetching beneficiary, lastname = " + lastName);
		Log.i(TAG, "Fetching beneficiary, firstname = " + firstName);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.like(FINDS_LASTNAME, lastName);
			where.and();
			where.like(FINDS_FIRSTNAME, firstName);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	
	/**
	 * Helper method to retrieve selected messages from message table. 
	 * @param filter
	 * @param order_by
	 * @return
	 */
	private List<AcdiVocaMessage> lookupMessages(int filter, int bene_status, String order_by) {
		int msg_status = 0;
		// Map the select result to the message status
		switch (filter) {
		case SearchFilterActivity.RESULT_SELECT_PENDING:
			msg_status = MESSAGE_STATUS_PENDING;
			break;
		case SearchFilterActivity.RESULT_SELECT_SENT:
			msg_status = MESSAGE_STATUS_SENT;
			break;
		case SearchFilterActivity.RESULT_SELECT_ACKNOWLEDGED:
			msg_status = MESSAGE_STATUS_ACK;
			break;
		default:
			break;
		}
		
		Dao<AcdiVocaMessage, Integer> avMessageDao = null;
		List<AcdiVocaMessage> list = null;
		AcdiVocaMessage avMsg = null;
		try {
			avMessageDao = getAcdiVocaMessageDao();
			QueryBuilder<AcdiVocaMessage, Integer> queryBuilder =
				avMessageDao.queryBuilder();
			if (msg_status != 0) {
				Where<AcdiVocaMessage, Integer> where = queryBuilder.where();
				where.eq("msgStatus", msg_status);
			}
			PreparedQuery<AcdiVocaMessage> preparedQuery = queryBuilder.prepare();
			list = avMessageDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

		
	/**
	 * Helper method to retrieve selected beneficiarys from table. Data are
	 * pulled from the FINDS_TABLE.  This method is called by createMessages()
	 * retrieve those beneficiaries for whom SMS messages will be sent.  For
	 * NEW beneficiaries all beneficiaries whose messages are UNSENT are returned.
	 * For UPDATE beneficiaries only those for whom  there's a change in STATUS
	 * are returned.
	 * @param filter
	 * @param order_by
	 * @return
	 */
	private List<AcdiVocaFind> lookupBeneficiaryRecords(int filter, String order_by, String distrCtr) {
		Log.i(TAG, "lookupBeneficiaryRecords filter = " + filter);
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			if (filter == SearchFilterActivity.RESULT_SELECT_NEW) {
				where.eq(FINDS_STATUS, FINDS_STATUS_NEW);
				where.and();
				where.eq( FINDS_MESSAGE_STATUS, MESSAGE_STATUS_UNSENT);
			} else if (filter == SearchFilterActivity.RESULT_SELECT_UPDATE) {
				where.eq(FINDS_STATUS, FINDS_STATUS_UPDATE);
				where.and();
				where.eq(FINDS_MESSAGE_STATUS, MESSAGE_STATUS_UNSENT);
				where.and();
				where.eq(FINDS_DISTRIBUTION_POST, distrCtr);
				where.and();
				where.eq(FINDS_Q_CHANGE, true);
			}
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Returns an array of AcdiVocaMessages for new or updated beneficiaries. 
	 * Fetches the beneficiary records from the Db and converts the column names
	 * and their respective values to abbreviated attribute-value pairs.
	 * @param filter
	 * @param order_by
	 * @return
	 */
	public ArrayList<AcdiVocaMessage> createMessagesForBeneficiaries(int filter, String order_by, String distrCtr) {
		List<AcdiVocaFind> list = lookupBeneficiaryRecords(filter, order_by, distrCtr);
		ArrayList<AcdiVocaMessage> acdiVocaMsgs = new ArrayList<AcdiVocaMessage>();
		if (list != null) {
			Log.i(TAG,"createMessagesForBeneficiaries " +  " count=" + list.size() + " filter= " + filter);
		
		// Construct the messages and return as a String array
			Iterator<AcdiVocaFind> it = list.iterator();

			while (it.hasNext()) {
				AcdiVocaFind avFind = it.next();   // Process the next beneficiary
				AcdiVocaMessage avMessage = avFind.toSmsMessage();
				acdiVocaMsgs.add(avMessage);
			}
		}
		return acdiVocaMsgs;		
	}

	/**
	 * Deletes all rows from the Beneficiary Table.
	 * @return
	 */
	public int clearBeneficiaryTable() {
		Log.i(TAG, "Clearing Beneficiary Table");
		int count = 0;
		Dao<AcdiVocaFind, Integer> avFind = null;
		List<AcdiVocaFind> list = null;
		try {
			avFind = getAcdiVocaFindDao();
			DeleteBuilder<AcdiVocaFind, Integer> deleteBuilder =
				avFind.deleteBuilder();
			// Delete all rows -- no where clause
			count = avFind.delete(deleteBuilder.prepare());
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * Deletes all rows from the Beneficiary Table.
	 * @return
	 */
	public int clearMessageTable() {
		Log.i(TAG, "Clearing Message Table");
		int count = 0;
		Dao<AcdiVocaMessage, Integer> avMsg = null;
		List<AcdiVocaMessage> list = null;
		try {
			avMsg = getAcdiVocaMessageDao();
			DeleteBuilder<AcdiVocaMessage, Integer> deleteBuilder =
				avMsg.deleteBuilder();
			// Delete all rows -- no where clause
			count = avMsg.delete(deleteBuilder.prepare());
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * Returns a list of all beneficiaries in the Db.
	 * @return
	 */
	public List<AcdiVocaFind> fetchAllBeneficiaries() {
		Log.i(TAG, "Fetching all beneficiaries");
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			list = avFindDao.queryForAll();
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Returns a list of all beneficiaries in the Db by type (MCHN, AGRI)
	 * @return
	 */
	public List<AcdiVocaFind> fetchAllBeneficiaries(int beneficiary_type) {
		Log.i(TAG, "Fetching all beneficiaries of type " + beneficiary_type);
		
		if (beneficiary_type == FINDS_TYPE_BOTH)
			return fetchAllBeneficiaries();
		
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.eq(FINDS_TYPE, beneficiary_type);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Creates an array list of messages each of which consists of a list of the
	 * dossier numbers of beneficiaries who did not show up at the distribution event.
	 * Beneficiaries who did show up and who have changes are updated with individual
	 * messages.  Beneficiaries who showed up but there was no change are not processed.
	 * Their status is deduced on the server by process of elimination. 
	 * @param distrCtr
	 * @return
	 */
	public ArrayList<AcdiVocaMessage> createBulkUpdateMessages(String distrCtr) {
		Log.i(TAG, "Creating bulk update messages distribution center = " + distrCtr);
		
		ArrayList<AcdiVocaMessage> acdiVocaMsgs = new ArrayList<AcdiVocaMessage>();

		Dao<AcdiVocaFind, Integer> avFindDao = null;
		AcdiVocaFind avFind = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.eq(FINDS_STATUS,  FINDS_STATUS_UPDATE);
			where.and();
			where.eq(FINDS_MESSAGE_STATUS, MESSAGE_STATUS_UNSENT);
			where.and();
			where.eq(FINDS_DISTRIBUTION_POST, distrCtr);
			where.and();
			where.eq(FINDS_Q_PRESENT, false);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}

		Log.i(TAG,"fetchBulkUpdateMessages " +  " count=" + list.size() + " distrPost " + distrCtr);
		
		if (list.size() != 0) {
			Iterator<AcdiVocaFind> it = list.iterator();
			String smsMessage = "";
			String msgHeader = "";
			while (it.hasNext()) {
				avFind = it.next();
				smsMessage += avFind.dossier + AttributeManager.LIST_SEPARATOR;
				
				if (smsMessage.length() > 120) {
					// Add a header (length and status) to message
					msgHeader = "MsgId: bulk, Len:" + smsMessage.length();

					acdiVocaMsgs.add(new AcdiVocaMessage(UNKNOWN_ID, 
							UNKNOWN_ID, 
							MESSAGE_STATUS_UNSENT,
							"", smsMessage, msgHeader, 
							!AcdiVocaMessage.EXISTING));
					smsMessage = "";
				}
			}
			if (!smsMessage.equals("")) {
				msgHeader = "MsgId: bulk, Len:" + smsMessage.length();
				acdiVocaMsgs.add(new AcdiVocaMessage(UNKNOWN_ID, 
							UNKNOWN_ID, 
							MESSAGE_STATUS_UNSENT,
							"", smsMessage, msgHeader, 
							!AcdiVocaMessage.EXISTING));
			}
		}
		return acdiVocaMsgs;
	}
	
	/** 
	 * Returns an array of Strings where each String represents an SMS
	 * message for a Beneficiary.
	 * @param filter a int that selects messages by status
	 * @return an array of SMS strings
	 */
	public ArrayList<AcdiVocaMessage> fetchSmsMessages(int filter, int bene_status, String order_by) {
		List<AcdiVocaMessage> list = lookupMessages(filter, bene_status, order_by);

		ArrayList<AcdiVocaMessage> acdiVocaMsgs = new ArrayList<AcdiVocaMessage>();

		Iterator<AcdiVocaMessage> it = list.iterator();
		while (it.hasNext()) {
			AcdiVocaMessage msg = it.next();
			String header = "MsgId:" + msg.id + " Stat:" + msg.msgStatus + " Len:" 
				+ msg.smsMessage.length() + " Bid = " + msg.beneficiaryId;
			msg.setMsgHeader(header);
			acdiVocaMsgs.add(msg);
		}
		
		return acdiVocaMsgs;
	}
	
	/**
	 * Returns the number of babies in prevention or malnouri processed.
	 * @return
	 */
	public int queryNDistributionChildrenProcessed(String distrSite) {
		Log.i(TAG,"Querying number of children processed");
		
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.and(where.eq(FINDS_STATUS, FINDS_STATUS_UPDATE),
					where.eq(FINDS_DISTRIBUTION_POST, distrSite),
					where.eq(FINDS_Q_PRESENT, true),
					where.or(where.eq(FINDS_BENEFICIARY_CATEGORY, FINDS_PREVENTION),
							where.eq(FINDS_BENEFICIARY_CATEGORY, FINDS_MALNOURISHED)));
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if (list != null)
			return list.size();
		return 0;	
	}

	/**
	 * Returns the number of expectant or lactating mothers processed.
	 * @return
	 */
	public int queryNDistributionWomenProcessed(String distrSite) {
		Log.i(TAG,"Querying number of women processed");
		
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.and(where.eq(FINDS_STATUS, FINDS_STATUS_UPDATE),
					where.eq(FINDS_DISTRIBUTION_POST, distrSite),
					where.eq(FINDS_Q_PRESENT, true),
					where.or(where.eq(FINDS_BENEFICIARY_CATEGORY, FINDS_EXPECTING),
							where.eq(FINDS_BENEFICIARY_CATEGORY, FINDS_NURSING)));
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if (list != null)
			return list.size();
		return 0;
	}

	
	/**
	 * Returns the number of beneficiaries for whom messages are not sent.
	 * @return
	 */
	public boolean queryUnsentBeneficiaries() {
		Log.i(TAG,"Querying number of unsent beneficiaries");
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(FINDS_MESSAGE_STATUS, MESSAGE_STATUS_UNSENT);
		
		// Query for the username in the user table
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		AcdiVocaFind result = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.or(where.eq(AcdiVocaDbHelper.FINDS_MESSAGE_STATUS, AcdiVocaDbHelper.MESSAGE_STATUS_UNSENT),
					where.eq(AcdiVocaDbHelper.FINDS_MESSAGE_STATUS, AcdiVocaDbHelper.MESSAGE_STATUS_PENDING));
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			result = avFindDao.queryForFirst(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		return result != null;
	}
	
	/**
	 * Returns the number of beneficiaries who were absent at
	 * the end of the distribution event.
	 * @return
	 */
	public int queryNDistributionAbsentees(String distrSite) {
		Log.i(TAG,"Querying number of absentees");
		
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.eq(FINDS_STATUS, FINDS_STATUS_UPDATE);
			where.and();
			where.eq(FINDS_DISTRIBUTION_POST, distrSite);
			where.and();
			where.eq(FINDS_Q_PRESENT, false);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if (list != null)
			return list.size();
		return 0;
	}
	
	/** 
	 * Returns an array of dossier numbers for all beneficiaries.
	 * @distribSite the Distribution Site of the beneficiaries
	 * @return an array of N strings or null if no beneficiaries are found
	 */
	public String[] fetchAllBeneficiaryIdsByDistributionSite(String distribSite) {
		Log.i(TAG, "fetchBeneficiaries DistributionSite = " + distribSite);
		
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		List<AcdiVocaFind> list = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			QueryBuilder<AcdiVocaFind, Integer> queryBuilder =
				avFindDao.queryBuilder();
			Where<AcdiVocaFind, Integer> where = queryBuilder.where();
			where.eq(FINDS_DISTRIBUTION_POST, distribSite);
			PreparedQuery<AcdiVocaFind> preparedQuery = queryBuilder.prepare();
			list = avFindDao.query(preparedQuery);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		if(list == null) {
			return null;
		}
		else if(list.size() == 0){
			return null;
		}		
		String dossiers[] = new String[list.size()];
		Iterator<AcdiVocaFind> it = list.iterator();
		int k = 0;
		while (it.hasNext()) {
			dossiers[k] = it.next().dossier;
			Log.i(TAG, "dossier = " + dossiers[k]);
			++k;
		}
		return dossiers;
	}


	/**
	 * Returns selected columns for a find by id.
	 * @param id the Find's id
	 * @param columns an array of column names, can be left null
	 * @return
	 */
	public AcdiVocaFind fetchFindById(int id, String[] columns) {
		Log.i(TAG, "Fetch find = " + id);
		
		Dao<AcdiVocaFind, Integer> avFindDao = null;
		AcdiVocaFind avFind = null;
		try {
			avFindDao = getAcdiVocaFindDao();
			avFind = avFindDao.queryForId(id);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}

		return avFind;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		acdiVocaFindDao = null;
		avUserDao = null;
		acdiVocaMessageDao = null;
		
	}

}
