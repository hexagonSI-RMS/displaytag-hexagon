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

import org.apache.commons.lang.StringUtils;
import org.displaytag.tags.ColumnTag;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
public class CustomColumnData implements Comparable<CustomColumnData> {
	private Long recnum;
	private String cotsTitle;
	private String customTitle;
	private int displayOrder;
	private boolean hidden = false;
	private String propertyName;
	private boolean sortable = false;
	private String sortOnProperty;
	private int maxLength = 0;
	private boolean isAdded = false;
	private boolean nonConfigurable = false;
	private String sortDirection;
	
	
	public CustomColumnData(JsonObject jsonObject) {
		this.recnum = getLongValue(jsonObject, "colrecnum");
		this.cotsTitle = getStringValue(jsonObject, "cotsTitle");
		this.customTitle = getStringValue(jsonObject, "customTitle");
		this.displayOrder = getIntValue(jsonObject, "displayOrder");
		this.hidden = getBooleanValue(jsonObject, "hidden");
		this.propertyName = getStringValue(jsonObject, "propertyName");
		this.sortable = getBooleanValue(jsonObject, "sortable");
		this.sortOnProperty = getStringValue(jsonObject, "sortProperty");
		this.maxLength = getIntValue(jsonObject, "maxLength");
		this.isAdded = getBooleanValue(jsonObject, "isAdded");
		this.nonConfigurable = getBooleanValue(jsonObject, "nonConfigurable");
		this.sortDirection = getStringValue(jsonObject, "sortDirection");
	}
	
	
	public void initColumn(ColumnTag columnTag) {
		columnTag.setProperty(propertyName);
		columnTag.setTitle(getTitle());
		columnTag.setSortable(sortable);
		columnTag.setSortProperty(sortOnProperty);
		columnTag.setMaxLength(maxLength);
		columnTag.setNonConfigurable(nonConfigurable);
	}
	
	public String getTitle() {
		return StringUtils.isNotBlank(customTitle) ? customTitle : cotsTitle;
	}
	
    @Override
	public int compareTo(CustomColumnData o) {
		return new Integer(this.displayOrder).compareTo(new Integer(o.getDisplayOrder()));
	}


	private String getStringValue(JsonObject jsonObj, String name) {
    	if (jsonObj.has(name)) {
    		JsonElement e = jsonObj.get(name);
    		return e != null ? e.getAsString() : null;
    	}
    	return null;
    }
    
    private Long getLongValue(JsonObject jsonObj, String name) {
    	if (jsonObj.has(name)) {
    		JsonElement e = jsonObj.get(name);
    		return e != null ? e.getAsLong() : null;
    	}
    	return 0L;
    }
    
    private int getIntValue(JsonObject jsonObj, String name) {
    	if (jsonObj.has(name)) {
    		JsonElement e = jsonObj.get(name);
    		return e != null ? e.getAsInt() : 0;
    	}
    	return 0;
    }
    
    private Boolean getBooleanValue(JsonObject jsonObj, String name) {
    	if (jsonObj.has(name)) {
    		JsonElement e = jsonObj.get(name);
    		return Boolean.TRUE.equals(e.getAsBoolean());
    	}
    	return Boolean.FALSE;
    }
    
	String toStringTemplate = "cotsTitle[%s], displayOrder[%s]";
	public String toString() {
		return String.format(toStringTemplate, cotsTitle, displayOrder);
	}


	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public boolean isHidden() {
		return hidden;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public boolean getSortable() {
		return sortable;
	}

	public String getSortOnProperty() {
		return sortOnProperty;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public String getCotsTitle() {
		return cotsTitle;
	}

	public String getCustomTitle() {
		return customTitle;
	}

	public boolean getIsAdded() {
		return isAdded;
	}


	public Long getRecnum() {
		return recnum;
	}


	public void setSortOnProperty(String sortOnProperty) {
		this.sortOnProperty = sortOnProperty;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public boolean isNonConfigurable() {
		return nonConfigurable;
	}

	public void setNonConfigurable(boolean nonConfigurable) {
		this.nonConfigurable = nonConfigurable;
	}


	public String getSortDirection() {
		return sortDirection;
	}


	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}
}