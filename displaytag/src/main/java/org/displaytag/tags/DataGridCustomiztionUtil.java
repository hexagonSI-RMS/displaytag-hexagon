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

package org.displaytag.tags;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.model.Cell;
import org.displaytag.model.Column;
import org.displaytag.model.CustomColumnData;
import org.displaytag.model.CustomTableData;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.Row;
import org.displaytag.model.TableModel;
import org.displaytag.util.HtmlAttributeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@SuppressWarnings("all")
public class DataGridCustomiztionUtil {
	 private static Log log = LogFactory.getLog(DataGridCustomiztionUtil.class);
	/**
	 * custom display tag configuration, populated in the HttpServletRequest as an attribute
	 */
	public static final String custom_config_attribute_key = "custom_grid_config";
	public static final String UI_CUSTOMIZATION_MODE_KEY = "show_customization_icon";
	
	static Method customFieldGetterMethod = null;
	static String customFieldGetterMethodName = "findCustomFieldValue";
	
	static Method linkedTableGetterMethod = null;
	static String linkedTableGetterMethodName = "findLinkedFieldValue";
	static String listTableName_attribute_format = "displaytag_listTableName_%s";
	static String disable_customization_attribute_format = "displaytag_uncustomizable_%s";
	
	static Object customGridQueryService;

	public static CustomTableData readCustomizations(PageContext pageContext, TableTag tableTag) {
		String tableTagId = tableTag.getUid();
		
		// for screens with multiple tables, we need to know the db tableName:
		String listTableKey = String.format(listTableName_attribute_format, tableTagId);
		String listTable = (String) pageContext.getRequest().getAttribute(listTableKey);
		if (listTable != null) {
			tableTag.setListTableName(listTable);
		}
		
		String disableCustomizationKey = String.format(disable_customization_attribute_format, tableTagId);
		Boolean disableCustomization = (Boolean) pageContext.getRequest().getAttribute(disableCustomizationKey);
		if (Boolean.TRUE.equals(disableCustomization)) {
			tableTag.getTableModel().setCustomizationDisabled(true);
		}
		
		customGridQueryService = pageContext.getRequest().getAttribute("customGridQueryService");
		if (customGridQueryService != null && (customFieldGetterMethod == null || linkedTableGetterMethod == null)) 
			findGetterMethods(customGridQueryService.getClass());
		
		if (pageContext != null) {
			JsonObject customizations = (JsonObject) pageContext.getRequest().getAttribute(custom_config_attribute_key);
			
			if (customizations != null) {
				for (Map.Entry<String, JsonElement> entry : customizations.entrySet()) {
					
					// key is tableIdAttribute-customGridRecnum, set in CustomGridQueryServiceImpl->findCustomGridConfigurations
					String[] ids = entry.getKey().split("-");
					if (ids.length == 2 && ids[0] != null && ids[1] != null 
						&& StringUtils.equals(tableTagId, ids[0]) && NumberUtils.toLong(ids[1]) != -1) {
						
						if (entry.getValue() != null) {
							return new CustomTableData(NumberUtils.toLong(ids[1]), entry.getValue().getAsJsonObject());
						}
					}
				}
			}
		}
		return null;
	}
	
	public static Object findCustomFieldValue(Object object, String customField) {
		if (customFieldGetterMethod != null) {
			try {
				return customFieldGetterMethod.invoke(customGridQueryService, object, customField);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}
	
	public static Object findLinkedTableFieldValue(Object object, String linkedField) {
		if (linkedTableGetterMethod != null) {
			try {
				Object result = linkedTableGetterMethod.invoke(customGridQueryService, object, linkedField);
				if (result != null) {
					if (result instanceof List) {
						StringBuilder builder = new StringBuilder();
						List resultList = (List) result;
						for (Object entry : resultList) {
							builder.append("<div class='custom_grid'>").append(entry).append("</div>");
						}
						return builder.toString();
					} else 
						return result;
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}
	
	static void findGetterMethods(Class clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(customFieldGetterMethodName)) {
				// check params:
				Class<?>[]  paramTypes = method.getParameterTypes();
				if (paramTypes.length == 2 && paramTypes[0].equals(Object.class) && paramTypes[1].equals(String.class))
					customFieldGetterMethod = method;
			} else if (method.getName().equals(linkedTableGetterMethodName)) {
				// check params:
				Class<?>[]  paramTypes = method.getParameterTypes();
				if (paramTypes.length == 2 && paramTypes[0].equals(Object.class) && paramTypes[1].equals(String.class))
				linkedTableGetterMethod = method;
			}
		}
	}
	
	private static JsonObject findConfigurationsForTable(JsonObject customizations, String tableUID) {
		for (Map.Entry<String, JsonElement> entry : customizations.entrySet()) {
			String[] ids = entry.getKey().split("-");
			if (StringUtils.equals(tableUID, ids[0]))
					return entry.getValue().getAsJsonObject();
		}
		return null;
	}
	
    public static CustomColumnData getCustomColumnDataByCotsTitle(CustomTableData customTableData, String cotsTitle) {
    	if (customTableData == null) 
    		return null;
    	
    	if (cotsTitle != null) 
    		return customTableData.getColumnByCotsTitle(cotsTitle);
    	
		return null;
    }
    
    static String column_display_order = "<input type='hidden' class='columnOrder' name='columnOrder' value='%s'/>";
    public static String getDisplayOrder(HeaderCell headerCell) {
    	if (isSelectBox(headerCell.getTitle()))
    		return "";
    	return String.format(column_display_order, headerCell.getTitle());
    }
    
    static String editCtrl_cust_col = "<div class='coldata' "
    		   + "data-colrecnum='%s' " 
			   + "data-title='%s' " 
			   + "data-property='%s' " 
			   + "data-hidden='%s' "
			   + "data-sortable='%s' "
			   + "data-sortproperty='%s' " 
			   + "data-maxlength='%s' "
			   + "data-iscustomcolumn='%s'></div>";
	public static String getEditControl(HeaderCell headerCell) {
		String title = headerCell.getTitle();
		if (title == null || isSelectBox(title)) return "";
		
		CustomColumnData customColumnData = headerCell.getCustomColumnData();
		if (customColumnData != null) {
			return String.format(editCtrl_cust_col,
								customColumnData.getRecnum(),
								customColumnData.getTitle(),
								customColumnData.getPropertyName(),
								customColumnData.isHidden(),
								customColumnData.getSortable(), // original: headerCell.getBeanPropertyName() != null ? headerCell.getSortable() : customColumnData.getSortable(), 
								customColumnData.getSortOnProperty(),
								customColumnData.getMaxLength(),
								customColumnData.getIsAdded()
								);
		} else {
			return String.format(editCtrl_cust_col,
					"",
					headerCell.getTitle(),
					headerCell.getBeanPropertyName(),
					"false",
					headerCell.getSortable(),
					headerCell.getSortProperty() != null ? headerCell.getSortProperty() : headerCell.getBeanPropertyName(),
					headerCell.getMaxLength() == 0 ? "" : headerCell.getMaxLength(),
					"false"
					);
		}
	}
    
	public static String getEditControlsForHiddenColumns(CustomTableData customTableData) {
		if( customTableData == null || customTableData.getHiddenColumnList() == null) 
			return "";
		
    	 List<String> result = new ArrayList<String>();
    	 
         for (CustomColumnData columnData : customTableData.getHiddenColumnList()) {
        	String data =  String.format(editCtrl_cust_col,
        					columnData.getRecnum(),
		 					columnData.getTitle(),
		 					columnData.getPropertyName(),
		 					"true", //hidden
		 					columnData.getSortable(),
		 					columnData.getSortOnProperty(),
		 					columnData.getMaxLength(),
		 					columnData.getIsAdded() // iscustom-added column
 					);
        	 result.add(data);
         }
         return StringUtils.join(result, "");
	}
	private static HeaderCell findHeaderCellByTitle(CustomColumnData columnConfig, List headerCellList) {
    	for (int i = 0; i < headerCellList.size(); i++) {
    		HeaderCell cell = (HeaderCell) headerCellList.get(i);
    		String headerCellTitle = cell.getTitle();
    		String configuredTitle = columnConfig.getTitle();
    		if (isSelectBox(configuredTitle) && isSelectBox(headerCellTitle)) {
    			return cell;
    		}
    		else if (StringUtils.equalsIgnoreCase(configuredTitle, headerCellTitle))
    			return cell;
    	}
    	return null;
    }
	
    private static int getHeaderCellIndex(String title, List headerCellList) {
    	for (int i = 0; i < headerCellList.size(); i++) {
    		HeaderCell cell = (HeaderCell) headerCellList.get(i);
    		String headerCellTitle = cell.getTitle();
    		if (isSelectBox(title) && isSelectBox(headerCellTitle)) {
    			return i;
    		}
    		else if (StringUtils.equalsIgnoreCase(title, headerCellTitle))
    			return i;
    	}
    	return -1;
    }
    
    private static boolean isSelectBox(String title) {
    	return StringUtils.indexOf(title, "<div>Select</div>") != -1;
    }
}
