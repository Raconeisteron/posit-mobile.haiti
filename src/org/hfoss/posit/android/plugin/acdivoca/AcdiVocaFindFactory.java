/*
 * File: AcdiVocaFindFactory.java
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

import org.hfoss.posit.android.api.Find;
import org.hfoss.posit.android.api.FindFactory;

import android.content.Context;

public class AcdiVocaFindFactory extends FindFactory {
	private static AcdiVocaFindFactory sInstance = null;
	
	public static AcdiVocaFindFactory getInstance(){
		if(sInstance == null){
			initInstance();
		}
		
		return sInstance;
	}
	
	public static void initInstance(){
		assert(sInstance == null);
		
		sInstance = new AcdiVocaFindFactory();
	}
	
	public Find createNewFind(Context context){
		return null;
//		return new AcdiVocaFind(context);
	}
	
	public Find createNewFind(Context context, long id){
		return null;
//		return new AcdiVocaFind(context, id);
	}
	
	public Find createNewFind(Context context, String guid){
		return null;
//		return new AcdiVocaFind(context, guid);
	}
}
