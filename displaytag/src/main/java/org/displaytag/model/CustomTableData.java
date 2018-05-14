/**
 * Licensed under the Artistic License; you may not use this file
 * except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://displaytag.sourceforge.net/license.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * Copyright 2014-2018 Intergraph Corporation d/b/a Hexagon Safety & Infrastruture ("Hexagon")
 * Hexagon is part of Hexagon AB.
 * All rights reserved.
 *
 * Per the conditions of the Artistic License,
 * Hexagon Safety & Infrastructure states that it has
 * made the following changes to this source file:
 *
 *
 *  27 April 2018 - Ability to customize the set of columns displayed
 *        and their order  
 */

package org.displaytag.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;


public class CustomTableData {
	private Long recnum;
	private String defaultTableSortProperty;
	private Integer defaultTableSortColumnIndex; // need this for local sorts
	
	private Map<String, CustomColumnData> columnConfigByCotsTitle = new LinkedHashMap<>(0);
	
	private List<CustomColumnData> visibleColumnList = new ArrayList<CustomColumnData>(0);
	private List<CustomColumnData> hiddenColumnList = new ArrayList<CustomColumnData>(0);
	private List<CustomColumnData> addedColumnList = new ArrayList<CustomColumnData>(0);

	public CustomTableData(Long recnum, JsonObject tableCustomization) {
		this.recnum = recnum;
		
		JsonArray columnCnfigurations = tableCustomization.getAsJsonArray("columnConfigurations").getAsJsonArray();
		
		for (Iterator<JsonElement> it = columnCnfigurations.iterator(); it.hasNext(); ) {
			
			JsonObject colConfig = it.next().getAsJsonObject();
			
			CustomColumnData columnData = new CustomColumnData(colConfig);
			if (!columnData.getIsAdded())
				columnConfigByCotsTitle.put(columnData.getCotsTitle(), columnData);
			
			if (columnData.isHidden())
				hiddenColumnList.add(columnData);
			else
				visibleColumnList.add(columnData);
			
			if (columnData.getIsAdded())
				addedColumnList.add(columnData);
		}
		
		// sort visible columns by display order:
		Collections.sort(visibleColumnList);
		
		if (!(tableCustomization.get("defaultTableSortProperty") instanceof JsonNull)) {
			defaultTableSortProperty = tableCustomization.get("defaultTableSortProperty").getAsString();
			for (int i = 0; i < visibleColumnList.size(); i++) {
				if (StringUtils.equals(defaultTableSortProperty, visibleColumnList.get(i).getSortOnProperty())) {
					defaultTableSortColumnIndex = i;
					break;
				}
			}
		}
	}
	
	public CustomColumnData getColumnByCotsTitle(String cotsTitle) {
		return columnConfigByCotsTitle.containsKey(cotsTitle) ? columnConfigByCotsTitle.get(cotsTitle) : null;
	}
	
	public Long getRecnum() {
		return recnum;
	}

	public List<CustomColumnData> getHiddenColumnList() {
		return hiddenColumnList;
	}


	public List<CustomColumnData> getAddedColumnList() {
		return addedColumnList;
	}

	public List<CustomColumnData> getVisibleColumnList() {
		return visibleColumnList;
	}

	public String getDefaultTableSortProperty() {
		return defaultTableSortProperty;
	}

	public Integer getDefaultTableSortColumnIndex() {
		return defaultTableSortColumnIndex;
	}
}