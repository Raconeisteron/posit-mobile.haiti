/*
 * File: AttributeManager.java
 * 
 * Copyright (C) 2011 The Humanitarian FOSS Project (http://hfoss.org).
 * 
 * This file is part of POSIT-Haiti Server.
 *
 * POSIT-Haiti Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not visit http://www.gnu.org/licenses/gpl.html.
 *
 */

package org.hfoss.posit.android.plugin.acdivoca;   // Mobile side package
//package haiti.server.datamodel;                  // Server side package

import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

/**
 * This class manages all attributes and abbreviations
 * on both the client (mobile) and server side.  
 * 
 * On the mobile side, it defines the mappings between
 * Db column names, such as 'firstname' and he abbreviation
 * used for that attribute in the SMS messasge ('f').  
 * 
 * On the server side it defines the reverse mapping, from
 * 'f' to 'firstname'.   
 * 
 */
public class AttributeManager {
	public static final String TAG = "AttributeManager";
	
	private static AttributeManager mInstance = null; 
	private static HashMap<String,String> mappings;

	public enum BeneficiaryType {
		UNKNOWN(-1), MCHN(0), AGRI(1), BOTH(2);

		private int code;

		private BeneficiaryType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	public enum Sex {
		U, M, F
	};

	public enum YnQuestion {
		U, Y, N
	};

	public enum BeneficiaryCategory {
		UNKNOWN(-1), AGRI(0), EXPECTING(1), NURSING(2), PREVENTION(3), MALNOURISHED(
				4);

		private int code;

		private BeneficiaryCategory(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	public enum Abbreviated {
		TRUE, FALSE
	};

	public enum MessageStatus {
		UNKNOWN(-1), NEW(0), UPDATED(1), PENDING(2), PROCESSED(3), ALL(4);
		private int code;

		private MessageStatus(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	};

	public enum MessageType {
		UNKNOWN(-1), REGISTRATION(0), UPDATE(1), ATTENDANCE(2), ALL(3);

		private int code;

		private MessageType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}	
	
	public static final String ATTR_VAL_SEPARATOR = "=";
	public static final String PAIRS_SEPARATOR = ",";
	public static final String LIST_SEPARATOR = "&"; // Don't use '/' and don't use '|'
	public static final String NUMBER_SLASH_SIZE_SEPARATOR = ":";
	public static final String NOT_FOUND = " NOT FOUND";
	public static final String WHERE = " where ";
	public static final String SELECT_FROM = "select * from ";
	public static final String DATE_SEPARATOR = ":";
	public static final String LINE_ENDER = ";";
	public static final String CONJUNCTION = " and ";
	public static final String DOES_NOT_EXIST = " does not exist!";
	public static final String MATCH_NOT_FOUND = "MATCH NOT FOUND!";
	public static final String UPDATE = "UPDATE ";
	public static final String SET = " SET ";
	public static final String SINGLE_QUOTE = "'";
	public static final String OPEN_PAREN = "(";
	public static final String CLOSE_PAREN = ")";
	public static final String DB_HOST = "org.sqlite.JDBC";
	public static final String DB_NAME = "jdbc:sqlite:";
	public static final String INSERT = "INSERT INTO ";
	public static final String VALUES = "VALUES ";
	public static final String USER_DIRECTORY = "user.dir";
	public static final String DATABASE_PATHNAME = "/db/";
	public static final int ACK_MESSAGES_AT = 5;
	public static final String MSG_NUMBER_SEPARATOR = ":";
	
	public static final String NEW_USER = "Insert into user (username, password, role) values('";
	public static final int NEW_USER_ROLE = 1;

	public static final String OUTER_DELIM = PAIRS_SEPARATOR;
	public static final String INNER_DELIM = ATTR_VAL_SEPARATOR;
	
	public static final String URL_OUTER_DELIM = "%2C";
	public static final String URL_INNER_DELIM = "%3D";
	public static final String URL_PLUS = "%2B";
	public static final String PLUS = "+";
	public static final String FORM_BENEFICIARY = null;
	
	
	// Server side widget, button, and label names used
	//  in the DataEntry forms.
	public static final String FORM_FIRST_NAME="FirstName";
	public static final String FORM_LAST_NAME="LastName";
	public static final String FORM_COMMUNE="Commune";
	public static final String FORM_SECTION="Section";
	public static final String FORM_ADDRESS="Address";
	public static final String FORM_AGE="Age";
	public static final String FORM_SEX="Sex";

	public static final String FORM_BENEFICIARY_CATEGORY="BeneficiaryCategory";
	public static final String FORM_NUMBER_IN_HOUSE= "NumberInHome"; // "Number of persons in the house:";
	public static final String FORM_HEALTH_CENTER="HealthCenter";
	public static final String FORM_DISTRIBUTION_POST="DistributionPost";
	public static final String FORM_NAME_CHILD= "NameIfChild"; // "Responsible name (if child):";
	public static final String FORM_NAME_WOMAN= "NameIfWoman";  // "Responsible name (if pregnant woman):";
	public static final String FORM_HUSBAND= "Husband"; // "Husband name (if woman):";
	public static final String FORM_FATHER= "Father";  // "Father's name (if child):";
	public static final String FORM_MOTHER_LEADER = "MotherLeader";  // "Are you a mother leader?:";
	public static final String FORM_VISIT_MOTHER = "VisitMotherLeader";   // "Do you visit a mother leader?:";
	public static final String FORM_AGRICULTURE_1 = "Agr1";  // "Is someone in your family in the";
	public static final String FORM_AGRICULTURE_2 = "Agr2";  // Agriculture Program of ACDI/VOCA?:";
	public static final String FORM_GIVE_NAME= "GiveName";  // "If yes, give the name:";

	
	public static final String FORM_HEALTH = "Health";

	
	public static final String FORM_DOSSIER="Dossier";
	public static final String FORM_DOB= "DateOfBirth";
	public static final String FORM_MONTHS= "MonthsRemaining";
	public static final String FORM_PRESENT= "Present";  // "Was the beneficiary present?";
	public static final String FORM_TRANSFERRED= "Transferred";  // "Should this beneficiary be transferred to a new category?";
	public static final String FORM_MODIFICATIONS= "Modifications";  // "Are modifications needed in the beneficiary's record?";
	public static final String FORM_SUSPEND= "Suspend";  // "Should the beneficiary be suspended?";
	public static final String FORM_WHY= "Why"; //"If so, why?:";
	public static final String FORM_AGRICULTURE_CATEGORY = "AgricultureCategory";
	public static final String FORM_LAND = "AmountOfLand";
	public static final String FORM_SEED_TYPE = "SeedTypes";
	public static final String FORM_SEED_QUANTITY = "QuantityOfSeeds";
	public static final String FORM_MEASUREMENT = "UnitOfMeasurement";
	public static final String FORM_TOOLS = "Tools";
	public static final String FORM_ORGANIZATIONS = "Organizations";	
	public static final String FORM_AGRI = "Agriculture";	
	
	
	public static final String FINDS_DOSSIER =  "dossier";
	public static final String FINDS_TYPE =  "type";
	public static final String FINDS_BENEFICIARY_TYPE = "type";     //Newly added
	public static final String FINDS_MESSAGE_TYPE = "message_type"; //Newly added
	public static final String FINDS_STATUS =  "status";
	public static final String MESSAGE_TEXT =  "message";
	public static final String FINDS_MESSAGE_ID = "message_id";
	public static final String FINDS_MESSAGE_STATUS =  "message_status";
	public static final String FINDS_FIRSTNAME =  "firstname";
	public static final String FINDS_LASTNAME =  "lastname";
	public static final String FINDS_ADDRESS =  "address";
	public static final String FINDS_DOB =  "dob";
	public static final String FINDS_HOUSEHOLD_SIZE =  "household_size";
	public static final String FINDS_BENEFICIARY_CATEGORY =  "beneficiary_category";
	public static final String FINDS_SEX =  "sex";
	public static final String FINDS_HEALTH_CENTER =  "health_center"; //Re-added
	public static final String FINDS_DISTRIBUTION_POST =  "distribution_post";
	public static final String MESSAGE_BENEFICIARY_ID =  "beneficiary_id";
	public static final String MESSAGE_CREATED_AT =  "created_time";
	public static final String MESSAGE_SENT_AT =  "sent_time";
	public static final String MESSAGE_ACK_AT =  "acknowledged_time";
	
	public static final String FINDS_Q_MOTHER_LEADER = "mother_leader";
	public static final String FINDS_Q_VISIT_MOTHER_LEADER = "visit_mother_leader";
	public static final String FINDS_Q_PARTICIPATING_AGRI = "participating_agri";
	public static final String FINDS_Q_RELATIVE_AGRI = "same_person_participating_agri";
	public static final String FINDS_Q_PARTICIPATING_BENE = "participating_bene";
	public static final String FINDS_Q_RELATIVE_BENE = "same_person_participating_bene";
	

	public static final String FINDS_NAME_AGRI_PARTICIPANT = "name_agri_participant";
	public static final String FINDS_LAND_AMOUNT = "amount_of_land";
	public static final String FINDS_RELATIVE_1 = "relative_1";
	public static final String FINDS_RELATIVE_2 = "relative_2";
	public static final String FINDS_MONTHS_REMAINING = "MonthsRemaining";
	
	
	
	// Abbreviated names of fields and attributes that make
	// up the Attribute side of SMS messages.
	// For example, 's=0', would represent 'status=new'.
	public static final String ABBREV_DOSSIER = "i";
	public static final String ABBREV_MESSAGE_TEXT = "tx";
	public static final String ABBREV_MESSAGE_STATUS = "ms";
	public static final String ABBREV_CREATED_AT = "t1";
	public static final String ABBREV_SENT_AT = "t2";
	public static final String ABBREV_ACK_AT = "t3";
	public static final String ABBREV_MESSAGE_ID = "mi";
	
	public static final String ABBREV_STATUS = "s"; 
	public static final String ABBREV_ID = "id";    
	public static final String ABBREV_AV = "AV";
	public static final String ABBREV_MSG_NUMBER_SLASH_SIZE = "mn";
	public static final String ABBREV_TYPE = "t";
	
	public static final String ABBREV_BENE_DOSSIER = "0";
	public static final String ABBREV_AGRI_DOSSIER = "1";
	public static final String ABBREV_BOTH_DOSSIER = "2";
	
	
	
	public static final String ABBREV_FIRST = "f";     
	public static final String ABBREV_LAST = "l";      
	public static final String ABBREV_COMMUNE = "cm";
	public static final String ABBREV_COMMUNE_SECTION = "cs";
	public static final String ABBREV_LOCALITY = "ad";    
	public static final String ABBREV_DOB = "b";        
	public static final String ABBREV_SEX = "g";         
	public static final String ABBREV_CATEGORY = "c";
	public static final String ABBREV_IS_MOTHERLEADER = "ml";
	public static final String ABBREV_VISIT_MOTHERLEADER = "mv";
	public static final String ABBREV_IS_AGRI = "ag";
	public static final String ABBREV_RELATIVE_AGRI = "ar";
	public static final String ABBREV_LAND_AMT = "la";
	public static final String ABBREV_PARTICIPATING_BENE = "bn";
	public static final String ABBREV_RELATIVE_BENE = "rb";
	
	public static final String ABBREV_NUMBER_IN_HOME = "n";     
	public static final String ABBREV_HEALTH_CENTER = "h";      
	public static final String ABBREV_DISTRIBUTION_POST = "d"; 
	
	public static final String ABBREV_RELATIVE_1 = "r1";
	public static final String ABBREV_RELATIVE_2 = "r2";
	public static final String ABBREV_NAME_CHILD = "nc";
	public static final String ABBREV_NAME_WOMAN = "nw";
	public static final String ABBREV_HUSBAND = "h";
	public static final String ABBREV_FATHER = "f";

	public static final String ABBREV_SEED_QUANTITY = "sq";
	public static final String ABBREV_MEASUREMENT_UNIT = "mu";
	public static final String ABBREV_MONTHS = "mo";  //Added to compliment the attribute-value pair
	
	// Constants for Y/N questions on the agri form
	
	public static final String FORM_FARMER = "Farmer";
	public static final String FORM_MUSO = "Muso";
	public static final String FORM_CATTLE_RANCHER = "CattleRancher";
	public static final String FORM_STORE_OWNER = "StoreOwner";
	public static final String FORM_FISHERMAN = "Fisherman";
	public static final String FORM_OTHER = "Other";
	public static final String FORM_ARTISAN = "Artisan";
	
	public static final String FINDS_IS_FARMER = "is_farmer";
	public static final String FINDS_IS_FISHER = "is_fisher";
	public static final String FINDS_IS_MUSO = "is_MUSO";
	public static final String FINDS_IS_RANCHER = "is_rancher";
	public static final String FINDS_IS_STOREOWN = "is_store_owner";
	public static final String FINDS_IS_OTHER = "is_other";
	public static final String FINDS_IS_ARTISAN = "is_artisan";
	
	public static final String ABBREV_IS_FARMER = "fa";
	public static final String ABBREV_IS_FISHER = "fi";
	public static final String ABBREV_IS_MUSO = "mu";
	public static final String ABBREV_IS_RANCHER = "ra";
	public static final String ABBREV_IS_STOREOWNER = "st";
	public static final String ABBREV_IS_OTHER = "ot";
	public static final String ABBREV_IS_ARTISAN = "at";
	
	public static final String FORM_FAO = "FAO";
	public static final String FORM_SAVE_ORG = "SAVEOrg";
	public static final String FORM_CROSE = "CROSE";
	public static final String FORM_PLAN = "PLAN";
	public static final String FORM_MARDNR = "MARDNR";
	public static final String FORM_OTHER_ORG = "OtherOrg";
	
	public static final String FINDS_PARTNER_FAO = "partner_fao";
	public static final String FINDS_PARTNER_SAVE = "partner_save";
	public static final String FINDS_PARTNER_CROSE = "partner_crose";
	public static final String FINDS_PARTNER_PLAN = "partner_plan";
	public static final String FINDS_PARTNER_MARDNR = "partner_mardnr";
	public static final String FINDS_PARTNER_OTHER = "partner_other";
	
	public static final String ABBREV_PARTNER_FAO = "fo";
	public static final String ABBREV_PARTNER_SAVE = "sv";
	public static final String ABBREV_PARTNER_CROSE = "cr";
	public static final String ABBREV_PARTNER_PLAN = "pl";
	public static final String ABBREV_PARTNER_MARDNR = "md";
	public static final String ABBREV_PARTNER_OTHER = "pt";

	
	// For Update Messages
	public static final String ABBREV_Q_CHANGE = "cq";   // Added to incorporated changes to beneficiary type
	public static final String ABBREV_CHANGE_TYPE = "ch";
	public static final String ABBREV_Q_PRESENT = "pr";
	public static final String ABBREV_Q_TRANSFER = "tr";
	public static final String ABBREV_Q_TRANSFER_LACTATE = "tl";
	public static final String ABBREV_Q_TRANSFER_PREVENTION = "tp";
	public static final String ABBREV_Q_TRANSFER_LOCATION = "to";
	public static final String ABBREV_Q_TRANSFER_ABORTION = "ta";
	public static final String ABBREV_Q_MODIFICATIONS = "ms";	
	public static final String ABBREV_Q_DECEASED = "dc";
	public static final String ABBREV_Q_FRAUD = "fr";
	public static final String ABBREV_Q_COMPLETED_PROGRAM = "cp";
	
	// This array of abbreviations is used to encode
	//  multiple fields of Y/N data -- such as 'is the 
	//  beneficiary a farmer' -- into a single integer.
	//  For example, the binary integer 'is=9' or, in binary,
	//  'is=1001' would represent 'fa and ra" or 'farmer and rancher'.
	//  Methods are available to perform the encoding.
	public static final String[] isAFields = {ABBREV_IS_FARMER, ABBREV_IS_FISHER, ABBREV_IS_MUSO,
		ABBREV_IS_RANCHER, ABBREV_IS_STOREOWNER, ABBREV_IS_OTHER, ABBREV_IS_ARTISAN, 
		ABBREV_PARTNER_FAO, ABBREV_PARTNER_SAVE, ABBREV_PARTNER_CROSE, ABBREV_PARTNER_PLAN,
		ABBREV_PARTNER_MARDNR, ABBREV_PARTNER_OTHER, ABBREV_IS_AGRI, ABBREV_RELATIVE_AGRI,
		ABBREV_PARTICIPATING_BENE, ABBREV_RELATIVE_BENE};
	public static final String ABBREV_ISA = "is";

	
	
	
	
	// More Y/N questions for the agri form
	
	public static final String FORM_VEGETABLES = "Vegetables";
	public static final String FORM_CEREAL = "Cereal";
	public static final String FORM_TUBERS = "Tubers";
	public static final String FORM_TREE = "Tree";
	public static final String FORM_LIVRE = "Livre";
	public static final String FORM_MARMITES = "Marmites";
	public static final String FORM_POTE = "Pote";
	public static final String FORM_KG = "Kg";
	public static final String FORM_PLANTULES = "Plantules";
	public static final String FORM_BOUTURES = "Boutures";
	public static final String FORM_HOE = "Hoe";
	public static final String FORM_PICKAXE = "Pickaxe";
	public static final String FORM_WHEELBARROW = "Wheelbarrow";
	public static final String FORM_MACHETTE = "Machette";
	public static final String FORM_PRUNING_KNIFE = "PruningKnife";
	public static final String FORM_SHOVEL = "Shovel";
	public static final String FORM_CROWBAR = "Crowbar";
	
	public static final String FINDS_HAVE_BARREAMINES = "have_crowbar";
	public static final String FINDS_HAVE_BROUETTE = "have_wheelbarrow";
	public static final String FINDS_HAVE_CEREAL = "have_cereal";
	public static final String FINDS_HAVE_HOUE = "have_hoe";
	public static final String FINDS_HAVE_MACHETTE = "have_machete";
	public static final String FINDS_HAVE_PELLE = "have_shovel";
	public static final String FINDS_HAVE_PIOCHE = "have_pick";
	public static final String FINDS_HAVE_SERPETTE = "have_pruning_knife";
	public static final String FINDS_HAVE_TREE = "have_tree";
	public static final String FINDS_HAVE_GRAFTING = "have_grafting";
	public static final String FINDS_HAVE_TUBER = "have_tuber";
	public static final String FINDS_HAVE_VEGE = "have_vege";
	public static final String FINDS_HAVE_COFFEE = "have_coffee";
	
	public static final String ABBREV_HAVE_BARREMINES = "ba";
	public static final String ABBREV_HAVE_BROUTTE = "br";
	public static final String ABBREV_HAVE_CEREAL = "ce";
	public static final String ABBREV_HAVE_HOE = "ho";
	public static final String ABBREV_HAVE_MACHETE = "ma";
	public static final String ABBREV_HAVE_PELLE = "pe";
	public static final String ABBREV_HAVE_PIOCHE = "pi";
	public static final String ABBREV_HAVE_SERPETTE = "se";
	public static final String ABBREV_HAVE_TREE = "tr";
	public static final String ABBREV_HAVE_GRAFTING = "gr";
	public static final String ABBREV_HAVE_VEG = "ve";
	public static final String ABBREV_HAVE_TUBER = "tu";
	public static final String ABBREV_HAVE_COFFEE = "co";
	
	// This pair of constants is used to encode/decode Y/N
	// questions regarding plant, seeds, and tools.
	public static final String ABBREV_HASA = "hs";
	public static final String[] hasAFields = {ABBREV_HAVE_BARREMINES, ABBREV_HAVE_BROUTTE,
		ABBREV_HAVE_CEREAL, ABBREV_HAVE_HOE, ABBREV_HAVE_MACHETE, ABBREV_HAVE_PELLE, 
		ABBREV_HAVE_PIOCHE, ABBREV_HAVE_SERPETTE, ABBREV_HAVE_TREE, ABBREV_HAVE_VEG, 
		ABBREV_HAVE_TUBER, ABBREV_HAVE_COFFEE, ABBREV_HAVE_GRAFTING};

	// -------------- DATA VALUES
	// These correspond to data values represented as Enums
	// public static final String FORM_MALE="MALE";
	// public static final String FORM_FEMALE="FEMALE";
	public static final String FORM_INFANT_MAL = "InfantMal";
	public static final String FORM_INFANT_PREVENTION = "InfantPrevention";
	public static final String FORM_MOTHER_EXPECTING = "MotherExpecting";
	public static final String FORM_MOTHER_NURSING = "MotherNursing";
	
	public static final String BUTTON_INFANT_MAL="Enfant mal nourri";
	public static final String BUTTON_INFANT_PREVENTION="Enfant en prevention";
	public static final String BUTTON_MOTHER_EXPECTING="Femme enceinte";
	public static final String BUTTON_MOTHER_NURSING="Femme allaitante";
	
	
	public static final String FINDS_MALNOURISHED = "MALNOURISHED";
	public static final String FINDS_EXPECTING = "EXPECTING";
	public static final String FINDS_NURSING = "NURSING";
	public static final String FINDS_PREVENTION = "PREVENTION";
	
//	public static final String FINDS_MALNOURISHED_HA = "Enfant Mal"; //Note: different spelling
	public static final String FINDS_MALNOURISHED_HA = "Enfant Mal Nouri";
	public static final String FINDS_EXPECTING_HA = "Femme Enceinte";
	public static final String FINDS_NURSING_HA = "Femme Allaitante";
	public static final String FINDS_PREVENTION_HA = "Enfant Prevention";
	
	public static final String ABBREV_MALNOURISHED = "M";
	public static final String ABBREV_EXPECTING = "E";
	public static final String ABBREV_NURSING = "N";
	public static final String ABBREV_PREVENTION = "P";

	public static final String FORM_FEMALE="Female";
	public static final String FORM_MALE="Male";
	public static final String FORM_NO = "No";	
	public static final String FORM_YES = "Yes";
	
	public static final String FINDS_FEMALE = "FEMALE";
	public static final String FINDS_MALE = "MALE";
	public static final String FINDS_NO = "NO"; 
	public static final String FINDS_YES = "YES";
	public static final String FINDS_TRUE = "TRUE";
	public static final String FINDS_FALSE = "FALSE";
	
	public static final String BUTTON_FEMALE="FEMALE";
	public static final String BUTTON_MALE="MALE";
	public static final String BUTTON_NO="No";
	public static final String BUTTON_YES="Yes";
	
	public static final String ABBREV_FEMALE= "F";
	public static final String ABBREV_MALE= "M";
	public static final String ABBREV_NO= "N";	
	public static final String ABBREV_YES= "Y";

	
	public static final String ABBREV_TRUE = "T";
	public static final String ABBREV_FALSE = "F";


	
	public static final String FINDS_Q_CHANGE = "ChangeInStatus";   // Added to incorporated changes to beneficiary type
	public static final String FINDS_CHANGE_TYPE = "ChangeType";
	public static final String FINDS_Q_PRESENT = "Present";
	public static final String FINDS_Q_TRANSFER = "Transfer";
	public static final String FINDS_Q_MODIFICATIONS = "Modifications";
	
	public static final String FINDS_Q_TRANSFER_NEW_CATEGORY = "Transfer to new category";
	public static final String FINDS_Q_TRANSFER_LACTATE = "Transfer from pregnant to lactating";
	public static final String FINDS_Q_TRANSFER_PREVENTION = "Transfer from lactating to prevention";
	public static final String FINDS_Q_TRANSFER_LOCATION = "Transfer due to location change";
	public static final String FINDS_Q_TRANSFER_ABORTION = "Transfer due to abortion";
	public static final String FINDS_Q_CHANGED_BENEFICIARY_DATA = "Changed beneficiary data"; //Added
	public static final String FINDS_Q_DECEASED= "Deceased";
	public static final String FINDS_Q_FRAUD = "Fraud";
	public static final String FINDS_Q_COMPLETED_PROGRAM = "Completed program";
	public static final String FINDS_Q_OTHER = "Other"; //Added
	
	//NEEDS TO TRANSLATE
//	public static final String FINDS_Q_TRANSFER_NEW_CATEGORY_HA = "Transfere nan yon lòt kategori";
//	public static final String FINDS_Q_TRANSFER_LACTATE_HA = "Transfert de: Enceinte à Allaitante";
//	public static final String FINDS_Q_TRANSFER_PREVENTION_HA = "Transfert de: Allaitante à Prevention";
//	public static final String FINDS_Q_TRANSFER_LOCATION_HA = "Transfert du au changement de lieu";
//	public static final String FINDS_Q_TRANSFER_ABORTION_HA = "Transfert du a cause de l’avortement";
//	public static final String FINDS_Q_DECEASED_HA = "Decede";
//	public static final String FINDS_Q_FRAUD_HA = "Fraud";
//	public static final String FINDS_Q_COMPLETED_PROGRAM_HA = "Cycle complet";

	
	public static final String FINDS_RELATIVE_AGRI = "relative_having_agrAid";
	public static final String FINDS_RELATIVE_BENE = "relative_having_beneAid";
	// ---------------- LONG COLUMNS NAMES FROM PHONE"S DB------
	// Mobile side column names used in the phone's Db. Mostly 
	//  used by DbHelper to construct the SMS messages. A raw
	//  message pulled from the phone's DB would take the form:
	//  'firstname=joe,lastname=smith,...,sex=M'
	//  Using the data defined here, it would be encoded as
	// 'f=joe,l=smith,...,g=M'
	public static final String FINDS_BENE_DOSSIER = "Mchn";
	public static final String FINDS_AGRI_DOSSIER = "Agri";
	public static final String FINDS_BOTH_DOSSIER = "Both";
	

//  These don't seem to be necessary any more, but keep as commented out
//	public static final String ABBREV_AGRICULTURE_1 = "a1";
//	public static final String ABBREV_AGRICULTURE_2 = "a2";
//	public static final String ABBREV_GIVE_NAME = "gn";
//	public static final String ABBREV_YES = "y";
//	public static final String ABBREV_NO = "n";
//	public static final String ABBREV_MALE = "M"; //"m";
//	public static final String ABBREV_FEMALE = "F"; //"f";
//	public static final String ABBREV_INFANT_CATEGORY = "ic";
//	public static final String ABBREV_INFANT_MAL = "ia";
//	public static final String ABBREV_INFANT_PREVENTION = "P"; //"ip";
//	public static final String ABBREV_MOTHER_CATEGORY = "mc";
//	public static final String ABBREV_MOTHER_EXPECTING = "E"; //"me";
//	public static final String ABBREV_MOTHER_NURSING = "N"; //"mn";
//	public static final String ABBREV_DATA = "d";
//	public static final String ABBREV_GENERAL_INFORMATION = "gi";
//	public static final String ABBREV_MCHN_INFORMATION = "mchn";
//	public static final String ABBREV_CONTROLS = "ctrl";

	
	// ------------- LONG NAMES FOR SERVER SIDE FORMS
	// Don't know whether all of these are necessary?
	// TODO:  Clean up this list
//	public static final String LONG_FIRST = "firstName";
//	public static final String LONG_LAST = "lastName";
	public static final String LONG_MESSAGE_NUMBER = "messageNumber"; //added
//	public static final String LONG_COMMUNE = "commune";
	public static final String LONG_COMMUNE_SECTION = "commune_section";
//	public static final String LONG_ADDRESS = "address";
//	public static final String LONG_AGE = "age";
//	public static final String LONG_SEX = "sex";
//	public static final String LONG_BENEFICIARY = "beneficiary";
	public static final String LONG_NUMBER_IN_HOME = "NumberInHome";
//	public static final String LONG_HEALTH_CENTER = "HealthCenter"; //re-added
//	public static final String LONG_DISTRIBUTION_POST = "DistributionPost";
//	public static final String LONG_RELATIVE_1 = "Relative1"; //added
//	public static final String LONG_RELATIVE_2 = "Relative2"; //added
//	public static final String LONG_NAME_CHILD = "nameChild";
//	public static final String LONG_NAME_WOMAN = "nameWoman";
//	public static final String LONG_HUSBAND = "husband";
//	public static final String LONG_FATHER = "father";
//	public static final String LONG_MOTHER_LEADER = "motherLeader";
//	public static final String LONG_VISIT_MOTHER = "visitMotherLeader";
//	public static final String LONG_AGRICULTURE_1 = "agriculture1";
//	public static final String LONG_AGRICULTURE_2 = "agriculture2";
//	public static final String LONG_GIVE_NAME = "giveName";
//	public static final String LONG_YES = "yes";
//	public static final String LONG_NO = "no";
//	public static final String LONG_MALE = "male";
//	public static final String LONG_FEMALE = "female";
//	public static final String LONG_INFANT_CATEGORY = "InfantCategory";
	public static final String LONG_INFANT_MAL = "InfantMal";
	public static final String LONG_INFANT_PREVENTION = "InfantPrevention";
//	public static final String LONG_MOTHER_CATEGORY = "MotherCategory";
	public static final String LONG_MOTHER_EXPECTING = "MotherExpecting";
	public static final String LONG_MOTHER_NURSING = "MotherNursing";
//	public static final String LONG_DATA = "data";
//	public static final String LONG_GENERAL_INFORMATION = "generalInformation";
//	public static final String LONG_MCHN_INFORMATION = "mchnInformation";
//	public static final String LONG_CONTROLS = "controls";
	public static final String LONG_MESSAGE_STATUS = "messageStatus"; //added
	public static final String LONG_STATUS = "status";
	public static final String LONG_ID = "id";
	public static final String LONG_AV = "AV";
	public static final String LONG_TYPE = "type";
	public static final String LONG_MESSAGE_TYPE = "messageType";  //added
//	public static final String LONG_BENEFICIARY_TYPE = "beneficiaryType"; //added
	
//	public static final String LONG_DOSSIER = "dossier";
	
	
	
// Distribution points

		
	
//	public static final String CENTRE_PLATON_CEDRE = "Centre Platon Cedre";
//	public static final String POINT_FIXE_KA_TOUSEN = "Point Fixe Ka Tousen";
//	public static final String ANSE_A_PITRES = "Anse a Pitres";
//	public static final String DISPENSAIRE_BANANE = "Dispensaire Banane";
//	public static final String PT_FIXE_CALUMETTE = "Pt fixe Calumette";
//	public static final String CENTRE_BELLE_ANCE = "Centre Belle-Ance";
//	public static final String DISPENSAIRE_MAPOU = "Dispensaire Mapou";
//	public static final String PT_FIXE_BAIE_D_ORANGE = "Pt fixe Baie d_orange";
//	public static final String DISPENSAIRE_MARBRIOLE = "Dispensaire marbriole";
//	public static final String PT_FIXE_CORAIL_LAMOTHE = "Pt fixe Corail Lamothe";
//	public static final String PT_FIXE_PICHON = "Pt fixe Pichon";
//	public static final String PT_FIXE_BEL_AIR = "Pt Fixe Bel-air";
//	public static final String LABICHE = "Labiche";
//	public static final String CENTRE_ST_JOSPEH = "Centre St Joseph";
//	public static final String DISPENSAIRE_STE_ROSE_DE_LIMA = "Dispensaire Ste rose de lima";
//	public static final String DISPENSAIRE_BOUCAN_BELIER = "Dispensaire Boucan Belier";
//	public static final String DISPENSAIRE_RICOT = "Dispensaire Ricot";
//	public static final String PT_FIXE_MACIEUX = "Pt Fixe Macieux";
//	public static final String POINT_FIXE_DE_MAYETTE = "Point Fixe de Mayette";
//	public static final String POINT_FIXE_DE_AMAZONE = "Point Fixe de Amazone";
//	public static final String DISPENSAIRE_GRAND_GOSIER = "Dispensaire Grand Gosier";
//	public static final String DISPENSAIRE_BODARIE = "Dispensaire Bodarie";
//	public static final String PT_FIXE_BOULAY = "Pt fixe Boulay";
//	public static final String CENTRE_SACRE_COUER = "Centre Sacre Coeur";
//	public static final String DISPENSAIRE_DE_SAVANE_ZOMBI = "Dispensaire de Savane Zombi";
//	public static final String DISPENSAIRE_DE_BLACK_MAR_MIRANDE = "Dispensaire de Bleck/ Mar Mirande";
//	
	
	public static final String COMMUNE_SECTION_0 = "11eme La vallee";
	public static final String COMMUNE_SECTION_1 = "1ere Musac";
	public static final String COMMUNE_SECTION_2 = "3 eme La vallee";
	public static final String COMMUNE_SECTION_3 = "3eme Bras de gauche";
	public static final String COMMUNE_SECTION_4 = "4eme Haut Gandou";
	public static final String COMMUNE_SECTION_5 = "5eme Bas Gandou";
	public static final String COMMUNE_SECTION_6 = "6eme Bas de la croix";
	public static final String COMMUNE_SECTION_7 = "6ème Jamais-Vu";
	public static final String COMMUNE_SECTION_8 = "8eme orangers";
	public static final String COMMUNE_SECTION_9 = "9eme Bas des gris gris";
	public static final String COMMUNE_SECTION_10 = "Amazone";
	public static final String COMMUNE_SECTION_11 = "Anse a Pitres";
	public static final String COMMUNE_SECTION_12 = "Baie d_Orange";
	public static final String COMMUNE_SECTION_13 = "Bas Grandou";
	public static final String COMMUNE_SECTION_14 = "BelAir";
	public static final String COMMUNE_SECTION_15 = "Belle Anse";
	public static final String COMMUNE_SECTION_16 = "Bodarie";
	public static final String COMMUNE_SECTION_17 = "Bois d_Orme";
	public static final String COMMUNE_SECTION_18 = "Boucan Belier";
	public static final String COMMUNE_SECTION_19 = "BoucanGuillaume";
	public static final String COMMUNE_SECTION_20 = "Bras de gauche";
	public static final String COMMUNE_SECTION_21 = "Bresilienne";
	public static final String COMMUNE_SECTION_22 = "Callumette";
	public static final String COMMUNE_SECTION_23 = "Centre de Santé Anse a Pitres";
	public static final String COMMUNE_SECTION_24 = "CollinedesChaines";
	public static final String COMMUNE_SECTION_25 = "CorailLamothe";
	public static final String COMMUNE_SECTION_26 = "Cote de fer";
	public static final String COMMUNE_SECTION_27 = "Grand Gosier";
	public static final String COMMUNE_SECTION_28 = "Gris-Gris";
	public static final String COMMUNE_SECTION_29 = "Haut Grandou";
	public static final String COMMUNE_SECTION_30 = "Jamais vus";
	public static final String COMMUNE_SECTION_31 = "Labich";
	public static final String COMMUNE_SECTION_32 = "Mabriole";
	public static final String COMMUNE_SECTION_33 = "Mapou";
	public static final String COMMUNE_SECTION_34 = "Morne a Brule";
	public static final String COMMUNE_SECTION_35 = "Pichon";
	public static final String COMMUNE_SECTION_36 = "Platon Cedre";
	public static final String COMMUNE_SECTION_37 = "Pot de Chambre/2eMareMirande";
	public static final String COMMUNE_SECTION_38 = "Thiotte  1ereColombier";
	public static final String COMMUNE_SECTION_39 = "Tou mahot";
	
	public static final String ABBREV_C0 = "0";
	public static final String ABBREV_C1 = "1";
	public static final String ABBREV_C2 = "2";
	public static final String ABBREV_C3 = "3";
	public static final String ABBREV_C4 = "4";
	public static final String ABBREV_C5 = "5";
	public static final String ABBREV_C6 = "6";
	public static final String ABBREV_C7 = "7";
	public static final String ABBREV_C8 = "8";
	public static final String ABBREV_C9 = "9";
	public static final String ABBREV_C10 = "10";
	public static final String ABBREV_C11 = "11";
	public static final String ABBREV_C12 = "12";
	public static final String ABBREV_C13 = "13";
	public static final String ABBREV_C14 = "14";
	public static final String ABBREV_C15 = "15";
	public static final String ABBREV_C16 = "16";
	public static final String ABBREV_C17 = "17";
	public static final String ABBREV_C18 = "18";
	public static final String ABBREV_C19 = "19";
	public static final String ABBREV_C20 = "20";
	public static final String ABBREV_C21 = "21";
	public static final String ABBREV_C22 = "22";
	public static final String ABBREV_C23 = "23";
	public static final String ABBREV_C24 = "24";
	public static final String ABBREV_C25 = "25";
	public static final String ABBREV_C26 = "26";
	public static final String ABBREV_C27 = "27";
	public static final String ABBREV_C28 = "28";
	public static final String ABBREV_C29 = "29";
	public static final String ABBREV_C30 = "30";
	public static final String ABBREV_C31 = "31";
	public static final String ABBREV_C32 = "32";
	public static final String ABBREV_C33 = "33";
	public static final String ABBREV_C34 = "34";
	public static final String ABBREV_C35 = "35";
	public static final String ABBREV_C36 = "36";
	public static final String ABBREV_C37 = "37";
	public static final String ABBREV_C38 = "38";
	public static final String ABBREV_C39 = "39";
	public static final String ABBREV_C40 = "40";

	public static final String DISTRIBUTION_POINT_1 = "Centre Platon Cedre";
	public static final String DISTRIBUTION_POINT_2 = "Point Fixe Ka Tousen";
	public static final String DISTRIBUTION_POINT_3 = "Anse a Pitres";
	public static final String DISTRIBUTION_POINT_4 = "Dispensaire Banane";
	public static final String DISTRIBUTION_POINT_5 = "Pt fixe Calumette";
	public static final String DISTRIBUTION_POINT_6 = "Centre Belle-Ance";
	public static final String DISTRIBUTION_POINT_7 = "Dispensaire Mapou";
	public static final String DISTRIBUTION_POINT_8 = "Pt fixe Baie d_orange";
	public static final String DISTRIBUTION_POINT_9 = "Dispensaire marbriole";
	public static final String DISTRIBUTION_POINT_10 = "Pt fixe Corail Lamothe";
	public static final String DISTRIBUTION_POINT_11 = "Pt fixe Pichon";
	public static final String DISTRIBUTION_POINT_12 = "Pt Fixe Bel-air";
	public static final String DISTRIBUTION_POINT_13 = "Labiche";
	public static final String DISTRIBUTION_POINT_14 = "Centre St Joseph";
	public static final String DISTRIBUTION_POINT_15 = "Dispensaire Ste rose de lima";
	public static final String DISTRIBUTION_POINT_16 = "Dispensaire Boucan Belier";
	public static final String DISTRIBUTION_POINT_17 = "Dispensaire Ricot";
	public static final String DISTRIBUTION_POINT_18 = "Pt Fixe Macieux";
	public static final String DISTRIBUTION_POINT_19 = "Point Fixe de Mayette";
	public static final String DISTRIBUTION_POINT_20 = "Point Fixe de Amazone";
	public static final String DISTRIBUTION_POINT_21 = "Dispensaire Grand Gosier";
	public static final String DISTRIBUTION_POINT_22 = "Dispensaire Bodarie";
	public static final String DISTRIBUTION_POINT_23 = "Pt fixe Boulay";
	public static final String DISTRIBUTION_POINT_24 = "Centre Sacre Coeur";
	public static final String DISTRIBUTION_POINT_25 = "Dispensaire de Savane Zombi";
	public static final String DISTRIBUTION_POINT_26 = "Dispensaire de Bleck/ Mar Mirande";
	
	
	public static final String ABBREV_D1 = "HEAP-014";
	public static final String ABBREV_D2 = "HEAP-017";
	public static final String ABBREV_D3 = "HEAP-020";
	public static final String ABBREV_D4 = "HEAP-021";
	public static final String ABBREV_D5 = "HEBA-016";
	public static final String ABBREV_D6 = "HEBA-017";
	public static final String ABBREV_D7 = "HEBA006";
	public static final String ABBREV_D8 = "HEBA007";
	public static final String ABBREV_D9 = "HEBA008";	
	public static final String ABBREV_D10 = "HEBA009";
	public static final String ABBREV_D11 = "HEBA010";	
	public static final String ABBREV_D12 = "HEBA015";	
	public static final String ABBREV_D13 = "HECF-005";	
	public static final String ABBREV_D14 = "HECF001";	
	public static final String ABBREV_D15 = "HECF002";	
	public static final String ABBREV_D16 = "HECF003";	
	public static final String ABBREV_D17 = "HECF004";	
	public static final String ABBREV_D18 = "HECF023";	
	public static final String ABBREV_D19 = "HECF024";	
	public static final String ABBREV_D20 = "HECF025";
	public static final String ABBREV_D21 = "HEGG-018";
	public static final String ABBREV_D22 = "HEGG-019";
	public static final String ABBREV_D23 = "HEGG-020";
	public static final String ABBREV_D24 = "HETH-024";
	public static final String ABBREV_D25 = "HETH026";
	public static final String ABBREV_D26 = "HETH027";
	
	
	
	
	/**
	 * Private constructor means it can't be instantiated.
	 * @param activity
	 */
	private AttributeManager(){
	}
	
	public static AttributeManager getInstance(){
		mInstance = new AttributeManager();
		mInstance.init();
		//assert(mInstance != null);
		return mInstance;
	}
	
	/**
	 * Default constructor, inserts all the attributes into a HashMap
	 */
//	public AttributeManager() {
	public static void init() {
		mappings = new HashMap<String, String>();
//		mappings.put(ABBREV_ATTRIBUTE, LONG_ATTRIBUTE);
//		mappings.put(ABBREV_FIRST, LONG_FIRST);
//		mappings.put(ABBREV_LAST, LONG_LAST);
//		mappings.put(ABBREV_COMMUNE, LONG_COMMUNE);
		mappings.put(ABBREV_COMMUNE_SECTION, LONG_COMMUNE_SECTION);
//		mappings.put(ABBREV_LOCALITY, LONG_ADDRESS);
//		mappings.put(ABBREV_DOB, LONG_AGE);
//		mappings.put(ABBREV_SEX, LONG_SEX);
//		
//		mappings.put(ABBREV_CATEGORY, LONG_BENEFICIARY);
//		mappings.put(ABBREV_NUMBER_IN_HOME, LONG_NUMBER_IN_HOME);
//		mappings.put(ABBREV_HEALTH_CENTER, LONG_HEALTH_CENTER); //Re-added
//		mappings.put(ABBREV_DISTRIBUTION_POST, LONG_DISTRIBUTION_POST);
//		mappings.put(ABBREV_NAME_CHILD, LONG_NAME_CHILD);
//		mappings.put(ABBREV_NAME_WOMAN, LONG_NAME_WOMAN);
//		mappings.put(ABBREV_HUSBAND, LONG_HUSBAND);
//		mappings.put(ABBREV_FATHER, LONG_FATHER);
//		mappings.put(ABBREV_RELATIVE_1, LONG_RELATIVE_1); //added
//		mappings.put(ABBREV_RELATIVE_2, LONG_RELATIVE_2); //added

		
		mappings.put(ABBREV_MESSAGE_STATUS, LONG_MESSAGE_STATUS); //added
		mappings.put(ABBREV_STATUS,LONG_STATUS);
		mappings.put(ABBREV_ID,LONG_ID);
		mappings.put(ABBREV_AV, LONG_AV);
		mappings.put(ABBREV_TYPE, LONG_TYPE);
		
		
		// ----------- DATA MAPPINGS -------------------------
		mappings.put(FINDS_FEMALE, ABBREV_FEMALE);
		mappings.put(FINDS_MALE,ABBREV_MALE);
		mappings.put(FINDS_NO, ABBREV_NO);
		mappings.put(FINDS_YES, ABBREV_YES);
		
		mappings.put(FINDS_TRUE, ABBREV_TRUE);
		mappings.put(FINDS_FALSE, ABBREV_FALSE);

		mappings.put(FINDS_EXPECTING, ABBREV_EXPECTING);
		mappings.put(FINDS_EXPECTING_HA, ABBREV_EXPECTING);
		mappings.put(FINDS_NURSING, ABBREV_NURSING);
		mappings.put(FINDS_NURSING_HA, ABBREV_NURSING);	
		mappings.put(FINDS_PREVENTION, ABBREV_PREVENTION);
		mappings.put(FINDS_PREVENTION_HA, ABBREV_PREVENTION);		
		mappings.put(FINDS_MALNOURISHED, ABBREV_MALNOURISHED);
		mappings.put(FINDS_MALNOURISHED_HA, ABBREV_MALNOURISHED);	
		
		//  Server needs abbrev --> long for these Enums
		mappings.put(ABBREV_EXPECTING, FINDS_EXPECTING);
		mappings.put(ABBREV_NURSING, FINDS_NURSING);
		mappings.put(ABBREV_PREVENTION, FINDS_PREVENTION);
		mappings.put(ABBREV_MALNOURISHED, FINDS_MALNOURISHED);
		
		//  Not sure whether these are needed on server side?
//		mappings.put("F", "FEMALE");
//		mappings.put("M", "MALE");
//		mappings.put("E", "EXPECTING");
//		mappings.put("N", "NURSING");
//		mappings.put("P", "PREVENTION");
//		mappings.put("MA", "MALNOURISHED");
				
		// ---------- MOBILE SIDE MAPPINGS TO ABBREVIATIONS
		// This group maps Db column names in the on-phone Db to SMS abbreviations
		
		mappings.put(FINDS_BENE_DOSSIER, ABBREV_BENE_DOSSIER);
		mappings.put(FINDS_AGRI_DOSSIER, ABBREV_AGRI_DOSSIER);
		mappings.put(FINDS_BOTH_DOSSIER, ABBREV_BOTH_DOSSIER);
		mappings.put(FINDS_DOSSIER, ABBREV_DOSSIER);
		mappings.put(FINDS_TYPE, ABBREV_TYPE);
		mappings.put(FINDS_STATUS, ABBREV_STATUS);
		mappings.put(FINDS_BENEFICIARY_TYPE, ABBREV_TYPE); //added
		mappings.put(FINDS_MESSAGE_TYPE, ABBREV_STATUS); //added
		mappings.put(MESSAGE_TEXT, ABBREV_MESSAGE_TEXT);
		mappings.put(FINDS_MESSAGE_STATUS, ABBREV_MESSAGE_STATUS);
		mappings.put(FINDS_MESSAGE_ID, ABBREV_MESSAGE_ID);
		mappings.put(FINDS_FIRSTNAME, ABBREV_FIRST);
		mappings.put(FINDS_LASTNAME, ABBREV_LAST);
		mappings.put(FINDS_ADDRESS, ABBREV_LOCALITY);
		mappings.put(FINDS_DOB, ABBREV_DOB);
		mappings.put(FINDS_HOUSEHOLD_SIZE, ABBREV_NUMBER_IN_HOME);
		mappings.put(FINDS_BENEFICIARY_CATEGORY, ABBREV_CATEGORY );
		mappings.put(FINDS_SEX, ABBREV_SEX);
		mappings.put(FINDS_HEALTH_CENTER, ABBREV_HEALTH_CENTER);//re-added
		mappings.put(LONG_COMMUNE_SECTION, ABBREV_COMMUNE_SECTION);
		mappings.put(FINDS_DISTRIBUTION_POST,ABBREV_DISTRIBUTION_POST);
		mappings.put(MESSAGE_BENEFICIARY_ID, ABBREV_ID);
		mappings.put(MESSAGE_CREATED_AT, ABBREV_CREATED_AT);
		mappings.put(MESSAGE_SENT_AT, ABBREV_SENT_AT);
		mappings.put(MESSAGE_ACK_AT, ABBREV_ACK_AT);
		
		mappings.put(FINDS_Q_MOTHER_LEADER, ABBREV_IS_MOTHERLEADER);
		mappings.put(FINDS_Q_VISIT_MOTHER_LEADER, ABBREV_VISIT_MOTHERLEADER);
		mappings.put(FINDS_Q_PARTICIPATING_AGRI, ABBREV_IS_AGRI);
		mappings.put(FINDS_Q_RELATIVE_AGRI, ABBREV_RELATIVE_AGRI);
		mappings.put(FINDS_Q_PARTICIPATING_BENE, ABBREV_PARTICIPATING_BENE);
		mappings.put(FINDS_Q_RELATIVE_BENE, ABBREV_RELATIVE_BENE);
		mappings.put(FINDS_LAND_AMOUNT, ABBREV_LAND_AMT);
		mappings.put(FINDS_RELATIVE_1, ABBREV_RELATIVE_1);
		mappings.put(FINDS_RELATIVE_2, ABBREV_RELATIVE_2);
		mappings.put(FINDS_MONTHS_REMAINING, ABBREV_MONTHS);
		mappings.put(FINDS_IS_FARMER, ABBREV_IS_FARMER);
		mappings.put(FINDS_IS_FISHER, ABBREV_IS_FISHER);
		mappings.put(FINDS_IS_MUSO, ABBREV_IS_MUSO);
		mappings.put(FINDS_IS_RANCHER, ABBREV_IS_RANCHER);
		mappings.put(FINDS_IS_STOREOWN, ABBREV_IS_STOREOWNER);
		mappings.put(FINDS_IS_ARTISAN, ABBREV_IS_ARTISAN);
		mappings.put(FINDS_IS_OTHER, ABBREV_IS_OTHER);
		mappings.put(FINDS_HAVE_BARREAMINES, ABBREV_HAVE_BARREMINES);
		mappings.put(FINDS_HAVE_BROUETTE, ABBREV_HAVE_BROUTTE);
		mappings.put(FINDS_HAVE_CEREAL, ABBREV_HAVE_CEREAL);
		mappings.put(FINDS_HAVE_HOUE, ABBREV_HAVE_HOE);
		mappings.put(FINDS_HAVE_MACHETTE, ABBREV_HAVE_MACHETE);
		mappings.put(FINDS_HAVE_PELLE, ABBREV_HAVE_PELLE);
		mappings.put(FINDS_HAVE_PIOCHE, ABBREV_HAVE_PIOCHE);
		mappings.put(FINDS_HAVE_SERPETTE, ABBREV_HAVE_SERPETTE);
		mappings.put(FINDS_HAVE_TREE, ABBREV_HAVE_TREE);
		mappings.put(FINDS_HAVE_GRAFTING, ABBREV_HAVE_GRAFTING);
		mappings.put(FINDS_HAVE_TUBER, ABBREV_HAVE_TUBER);
		mappings.put(FINDS_HAVE_VEGE, ABBREV_HAVE_VEG);
		mappings.put(FINDS_HAVE_COFFEE, ABBREV_HAVE_COFFEE);
		
		mappings.put(FINDS_PARTNER_FAO, ABBREV_PARTNER_FAO);
		mappings.put(FINDS_PARTNER_SAVE, ABBREV_PARTNER_SAVE);
		mappings.put(FINDS_PARTNER_CROSE, ABBREV_PARTNER_CROSE);
		mappings.put(FINDS_PARTNER_PLAN, ABBREV_PARTNER_PLAN);
		mappings.put(FINDS_PARTNER_MARDNR, ABBREV_PARTNER_MARDNR);
		mappings.put(FINDS_PARTNER_OTHER, ABBREV_PARTNER_OTHER);
		
		
		
		// These are for beneficiary update messages
		mappings.put(FINDS_Q_CHANGE, ABBREV_Q_CHANGE);  
		mappings.put(FINDS_CHANGE_TYPE, ABBREV_CHANGE_TYPE);  
		mappings.put(FINDS_Q_PRESENT, ABBREV_Q_PRESENT);
		mappings.put(FINDS_Q_TRANSFER, ABBREV_Q_TRANSFER);
		mappings.put(FINDS_Q_MODIFICATIONS, ABBREV_Q_MODIFICATIONS);
		mappings.put(FINDS_Q_TRANSFER_NEW_CATEGORY, ABBREV_Q_TRANSFER);
		mappings.put(FINDS_Q_TRANSFER_LACTATE, ABBREV_Q_TRANSFER_LACTATE);
		mappings.put(FINDS_Q_TRANSFER_PREVENTION, ABBREV_Q_TRANSFER_PREVENTION);
		mappings.put(FINDS_Q_TRANSFER_LOCATION, ABBREV_Q_TRANSFER_LOCATION);
		mappings.put(FINDS_Q_TRANSFER_ABORTION, ABBREV_Q_TRANSFER_ABORTION);
		mappings.put(FINDS_Q_DECEASED, ABBREV_Q_DECEASED);
		mappings.put(FINDS_Q_FRAUD, ABBREV_Q_FRAUD);
		mappings.put(FINDS_Q_COMPLETED_PROGRAM, ABBREV_Q_COMPLETED_PROGRAM);
//		mappings.put(FINDS_Q_TRANSFER_NEW_CATEGORY_HA, ABBREV_Q_TRANSFER);
//		mappings.put(FINDS_Q_TRANSFER_LACTATE_HA, ABBREV_Q_TRANSFER_LACTATE);
//		mappings.put(FINDS_Q_TRANSFER_PREVENTION_HA, ABBREV_Q_TRANSFER_PREVENTION);
//		mappings.put(FINDS_Q_TRANSFER_LOCATION_HA, ABBREV_Q_TRANSFER_LOCATION);
//		mappings.put(FINDS_Q_TRANSFER_ABORTION_HA, ABBREV_Q_TRANSFER_ABORTION);
//		mappings.put(FINDS_Q_DECEASED_HA, ABBREV_Q_DECEASED);
//		mappings.put(FINDS_Q_FRAUD_HA, ABBREV_Q_FRAUD);
//		mappings.put(FINDS_Q_COMPLETED_PROGRAM_HA, ABBREV_Q_COMPLETED_PROGRAM);
		

		
		// ---------- ACDIV/VOCA DATA -----------------
		// There should be mappings for all fixed data
		// used on the phone.
//		<string-array name="distribution_point_names"> 
		mappings.put(DISTRIBUTION_POINT_1,  ABBREV_D1 );
		mappings.put(DISTRIBUTION_POINT_2,  ABBREV_D2 );
		mappings.put(DISTRIBUTION_POINT_3,  ABBREV_D3 );
		mappings.put(DISTRIBUTION_POINT_4,  ABBREV_D4 );
		mappings.put(DISTRIBUTION_POINT_5,  ABBREV_D5 );
		mappings.put(DISTRIBUTION_POINT_6,  ABBREV_D6 );
		mappings.put(DISTRIBUTION_POINT_7, ABBREV_D7  );
		mappings.put(DISTRIBUTION_POINT_8, ABBREV_D8  );
		mappings.put(DISTRIBUTION_POINT_9, ABBREV_D9  );	
		mappings.put(DISTRIBUTION_POINT_10, ABBREV_D10  );
		mappings.put(DISTRIBUTION_POINT_11,   ABBREV_D11 );	
		mappings.put(DISTRIBUTION_POINT_12,  ABBREV_D12  );	
		mappings.put(DISTRIBUTION_POINT_13,   ABBREV_D13 );	
		mappings.put(DISTRIBUTION_POINT_14,   ABBREV_D14 );	
		mappings.put(DISTRIBUTION_POINT_15,  ABBREV_D15  );	
		mappings.put(DISTRIBUTION_POINT_16, ABBREV_D16   );	
		mappings.put(DISTRIBUTION_POINT_17,   ABBREV_D17 );	
		mappings.put(DISTRIBUTION_POINT_18,  ABBREV_D18  );	
		mappings.put(DISTRIBUTION_POINT_19,   ABBREV_D19 );	
		mappings.put(DISTRIBUTION_POINT_20,   ABBREV_D20 );
		mappings.put(DISTRIBUTION_POINT_21, ABBREV_D21   );
		mappings.put(DISTRIBUTION_POINT_22,   ABBREV_D22 );
		mappings.put(DISTRIBUTION_POINT_23, ABBREV_D23  );
		mappings.put(DISTRIBUTION_POINT_24,   ABBREV_D24 );
		mappings.put(DISTRIBUTION_POINT_25,  ABBREV_D25  );
		mappings.put(DISTRIBUTION_POINT_26,   ABBREV_D26 );
		
		mappings.put(ABBREV_D1, DISTRIBUTION_POINT_1);
		mappings.put(ABBREV_D2, DISTRIBUTION_POINT_2);
		mappings.put(ABBREV_D3, DISTRIBUTION_POINT_3);
		mappings.put(ABBREV_D4, DISTRIBUTION_POINT_4);
		mappings.put(ABBREV_D5, DISTRIBUTION_POINT_5);
		mappings.put(ABBREV_D6, DISTRIBUTION_POINT_6);
		mappings.put(ABBREV_D7, DISTRIBUTION_POINT_7);
		mappings.put(ABBREV_D8, DISTRIBUTION_POINT_8);
		mappings.put(ABBREV_D9, DISTRIBUTION_POINT_9);
		mappings.put(ABBREV_D10, DISTRIBUTION_POINT_10);
		mappings.put(ABBREV_D11, DISTRIBUTION_POINT_11);
		mappings.put(ABBREV_D12, DISTRIBUTION_POINT_12);
		mappings.put(ABBREV_D13, DISTRIBUTION_POINT_13);
		mappings.put(ABBREV_D14, DISTRIBUTION_POINT_14);
		mappings.put(ABBREV_D15, DISTRIBUTION_POINT_15);
		mappings.put(ABBREV_D16, DISTRIBUTION_POINT_16);
		mappings.put(ABBREV_D17, DISTRIBUTION_POINT_17);
		mappings.put(ABBREV_D18, DISTRIBUTION_POINT_18);
		mappings.put(ABBREV_D19, DISTRIBUTION_POINT_19);
		mappings.put(ABBREV_D20, DISTRIBUTION_POINT_20);
		mappings.put(ABBREV_D21, DISTRIBUTION_POINT_21);
		mappings.put(ABBREV_D22, DISTRIBUTION_POINT_22);
		mappings.put(ABBREV_D23, DISTRIBUTION_POINT_23);
		mappings.put(ABBREV_D24, DISTRIBUTION_POINT_24);
		mappings.put(ABBREV_D25, DISTRIBUTION_POINT_25);
		mappings.put(ABBREV_D26, DISTRIBUTION_POINT_26);
		
//		<string-array name="health_center_names"> 
//		mappings.put("Centre de sant� une", "h1" );
//		mappings.put("Centre de sant� deux", "h2"  );
//		mappings.put("Centre de sant� trois", "h3" );
//		mappings.put("Centre de sant� quatre", "h4" );
//		mappings.put("Centre de sant� cinq", "h5" );
//		
//		mappings.put("h1", "Centre de sant� un");
//		mappings.put("h2", "Centre de sant� deux");
//		mappings.put("h3", "Centre de sant� trois");
//		mappings.put("h4", "Centre de sant� quatre");
//		mappings.put("h5", "Centre de sant� cinq");
		
	}
	
	
	/* *******************
	 * 
	 * 
	 * 
Centre Platon Cedre	HEAP-014
Point Fixe Ka Tousen	HEAP-017
Anse a Pitres	HEAP-020
dispensaire Banane	HEAP-021
	
Pt fixe Calumette	HEBA-016
Centre Belle-Ance	HEBA-017
Dispensaire Mapou	HEBA006
Pt fixe Baie d_orange	HEBA007
Dispensaire marbriole	HEBA008
Pt fixe Corail Lamothe	HEBA009
Pt fixe Pichon	HEBA010
Pt Fixe Bel-air	HEBA015
	
Labiche	HECF-005
Centre St Joseph	HECF001
Dispensaire Ste rose de lima	HECF002
Dispensaire Boucan Belier	HECF003
Dispensaire Ricot	HECF004
Pt Fixe Macieux	HECF023
Point Fixe de Mayette	HECF024
Point Fixe de Amazone	HECF025
	
Dispensaire Grand Gosier	HEGG-018
Dispensaire Bodarie	HEGG-019
Pt fixe Boulay	HEGG-020
	
Centre Sacre Coeur	HETH-024
Dispensaire de Savane Zombi	HETH026
Dispensaire de Bleck/ Mar Mirande	HETH027
***********************************************	 */
	
	/**
	 * Server side method -- Converted to remove reference to server side enum
	 * Maps a short form attribute or value name to a long form
	 * @param abbreviatedAttributes TRUE if abbreviated, FALSE if not
	 * @param s the String to be mapped to long
	 * @return the long form of the String
	 */
//	public static String mapToLong(Beneficiary.Abbreviated abbreviatedAttributes, String s){
	public static String mapToLong(boolean abbreviatedAttributes, String s){
		if (abbreviatedAttributes) {
			String str = mappings.get(s);
			if (str != null)
				return str;
			else 
				return "";
		}
		else
			return s;
	}
	
	/**
	 * Utility mapping from long attribute or value String to short.
	 * @param s
	 * @return
	 */
	public static String mapToShort(String s){
		String str = mappings.get(s);
		if (str != null)
			return str;
		else 
			return s;
	}
	
	/**
	 * Utility mapping from boolean to T/F.
	 * @param b
	 * @return
	 */
	public static String mapToShort(boolean b){
		if (b)
			return "T";
		else 
			return "F";
	}
	
	/**
	 * Mobile side method. 
	 * Converts attribute-value pairs to abbreviated attribute-value pairs.
	 * @param attr  a long attribute name, e.g. 'firstname'
	 * @param val   a data value, e.g. 'Joseph'
	 * @return  a string of the form a1=v1,a2=b2, ..., aN=vN
	 */
	public static String convertAttrValPairToAbbrev(String attr, String val) {
		String attrAbbrev = attr;
		attrAbbrev =	getMapping(attr);
		String valAbbrev = val;
		if ( !isUnmappedValue(attr) )
			valAbbrev = getMapping(val);
		
		return attrAbbrev + ATTR_VAL_SEPARATOR + valAbbrev;
	}
	
	/**
	 * Helper method to exclude some values from being mapped -- e.g., names,
	 * locations.
	 * @param attr
	 * @return
	 */
	private static boolean isUnmappedValue (String attr) {
		if (attr.equals(FINDS_FIRSTNAME)
				|| attr.equals(FINDS_LASTNAME)
				|| attr.equals(FINDS_ADDRESS))
			return true;
		
		return false;
	}
	

	/**
	 * Mobile side. 
	 * Convert a value to an abbreviated value. This is mostly
	 * used for the names of health centers and distribution posts.
	 * @param val a String of the form "Name of Some Health Center"
	 * @return a String of the form "h1" representing that health center
	 */
	public static String getMapping(String val) {
		//System.out.println("val = " + val);
		String result = mappings.get(val);
		if (result != null) {
				return result;
		}
		else 
			return val;
	}
	
	/**
	 * A simple test method. 
	 */
	public void testAllAttributes() {
		Iterator<String> it = mappings.keySet().iterator();
		//Iterator<String> it = mappings.values().iterator();
		while (it.hasNext()) { 
			String s = it.next();
			
			//.println(s +  " = " + mappings.get(s));
		}
	}

	/**
	 * Replaces some attribute/value Y/N pairs with an encoded base-2 number.
	 * Example:  'fa=1,ra=1' would be converted to 9 decimal, which 1001 in 
	 * binary or 9 = 1001 = 2^0 + 2^3 where 'fa' and 'ra' are contained in the
	 * array  ["fa", "fi", "mu", "ra", "st", "ot"] .  
	 * @param smsMsg the message to be encoded
	 * @param attributes an array of attribute names: 
	 * @param newAttr a String giving the name for the int attribute in the message
	 *  -- e.g., 'is' or 'hs'
	 * @return  an SMS where an attr/val pair such as 'is=9' replaces 'fa=1,ra=1'.  
	 */
	public static String encodeBinaryFields(String smsMsg, String[] attributes, String newAttr) {
		int sum = 0;
		String result = "";
		String pairs[] = smsMsg.split(PAIRS_SEPARATOR);  
		
		// Split each attr/value pair in the Sms message into its attr and its val
		for (int k = 0; k < pairs.length; k++) {
			String attrval[] = pairs[k].split(ATTR_VAL_SEPARATOR);
			String attr = "", val = "";
			if (attrval.length == 2) {
				attr = attrval[0];
				val = attrval[1];
			} else if (attrval.length == 1) {
				attr = attrval[0];
			}
			System.out.println(TAG + "attr = " + attr + " val = " + val);

			// If the attribute (eg 'fa') is contained in the attributes array
			// its value represents 2^x where x is its index in the array. Add
			// that value to the running total.  If the attribute is not in the
			// array, just put it back in the result string.
			int x = getIndex(attributes, attr);
			int valInt = 0;
			if (x != -1) {
				try {
					if (val.equals("true"))
						valInt = 1;
					else if (val.equals("false"))
						valInt = 0;
					else 
						valInt = Integer.parseInt(val);   // Shouldn't happen
				} catch (NumberFormatException e) {
					Log.e(TAG, "NumberFormatException on " + val);
				}
				if (valInt == 1)
					sum += Math.pow(2, x);              // Compute a running total
			} else {
				if (attr.length() != 0)
					result += attr + ATTR_VAL_SEPARATOR + val + PAIRS_SEPARATOR;
			}
		}
		result += newAttr + ATTR_VAL_SEPARATOR + sum;	
		
		// To test the encoding, print out the decoding
		//System.out.println(TAG +  "Decoded sum = " + decodeBinaryFieldsInt(sum, attributes));
		//System.out.println(TAG +  "Result= " + result);

		return result;
	}
	
	/**
	 * This is the reverse mapping of encodeBinaryFields.  It takes a
	 * decimal (base-10) number and converts it to binary where 1 bits
	 * correspond to the attributes in the attributes array.  Those are
	 * returns as a String of attr=val pairs.  For example the codedInt
	 * 9 would translate to 1001 or 2^0 + 2^3 or 'fa=1,ra=1' using the
	 * attributes array ["fa", "fi", "mu", "ra", "st", "ot"]
	 * @param codedInt is a base-10 number
	 * @param attributes is an array of attribute abbreviations
	 * @return  a string of attr=val pairs
	 */
	public static String decodeBinaryFieldsInt(int codedInt, String[] attributes) {
		String result = "";
		int len = attributes.length;    
		Log.i(TAG,"AAAAAAAAAAAAAAAAAAAAAABBBBBBBBBBBREEEEEEEEEEVVVVVVVVVVVV");
		Log.i(TAG,attributes.toString());
		// Moving right to left in the attributes array, use the array index
		// as the exponent in 2^k, subtracting 2^k from the codedInt on each 
		// iteration. If 2^k can be subtracted from codedInt that means that
		// attributes[k] contains an attribute whose value should be 1. So
		// insert that attr=1 into the result string.
		for (int k = len; k >= 0; k--)  {
			int pow2 = (int) Math.pow(2, k);
			if (codedInt >= pow2) {
				codedInt -= pow2;     // Subtract that power of 2
				result += attributes[k] + ATTR_VAL_SEPARATOR + "1" + PAIRS_SEPARATOR;
			}
		}
		//System.out.println(TAG + "Result = " + result);
		return result;	
	}
	

	/**
	 * Helper method to find the location of the String s in a String array.
	 * @param arr
	 * @param s
	 * @return returns the index of s in arr or -1 if it is not contained therein
	 */
	private static int getIndex(String []arr, String s)  {
		for (int k = 0; k < arr.length; k++) 
			if (s.equals(arr[k])) 
				return k;
		return -1;
	}
	
	
	/**
	 * TODO:  This method should thoroughly test this class. For example, 
	 * print out all mappings of short to long.
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello Attribute Manager");
		AttributeManager am = AttributeManager.getInstance(); // new AttributeManager();
		//System.out.print(am.mapToLong(false, "f"));
		//am.testAllAttributes();
		//System.out.println("Str= " + am.mappings.get("M"));
		String ids[] = "1/2".split("/");
		for (int k = 0; k < ids.length; k++)
			System.out.println(ids[k]);
	}

}