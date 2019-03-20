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
 * Copyright 2014-2019 Intergraph Corporation d/b/a Hexagon Safety & Infrastruture ("Hexagon")
 * Hexagon is part of Hexagon AB.
 * All rights reserved.
 * 
 * Per the conditions of the Artistic License,
 * Hexagon Safety & Infrastructure states that it has
 * made the following changes to this source file:
 *
 *  19 March 2019 - Ability to operate in multi-lingual environments
 */
 
package org.displaytag.util;

import java.lang.reflect.Method;

import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.tags.TableTag;

/**
 * Utility methods for handling project-specific display (e.g. multi-lingual support).
 */
public class HxgnDisplayUtil
{
	private static Log log = LogFactory.getLog(HxgnDisplayUtil.class);

	static Method translateMethod = null;
	static String TRANSLATE_METHOD_NAME = "translateNameIfNecessary";

	static Method translateSortingPropertyMethod = null;
	static String TRANSLATE_SORTING_PROPERTY_METHOD_NAME = "translateSortingPropertyIfNecessary";

	static Object hxgnDisplayService;

	public static void initialize(PageContext pageContext) {
		hxgnDisplayService = pageContext.getRequest().getAttribute("hxgnDisplayService");
		if (hxgnDisplayService != null && (translateMethod == null || translateSortingPropertyMethod == null)) {
			for (Method method : hxgnDisplayService.getClass().getMethods()) {
				String methodName = method.getName();

				if (TRANSLATE_METHOD_NAME.equals(methodName)) {
					// check params:
					Class<?>[]  paramTypes = method.getParameterTypes();
					if (paramTypes.length == 2 && paramTypes[0].equals(Object.class) && paramTypes[1].equals(String.class)) {
						translateMethod = method;
					}
				} else if (TRANSLATE_SORTING_PROPERTY_METHOD_NAME.equals(methodName)) {
					// check params:
					Class<?>[]  paramTypes = method.getParameterTypes();
					if (paramTypes.length == 2 && paramTypes[0].equals(Object.class) && paramTypes[1].equals(String.class)) {
						translateSortingPropertyMethod = method;
					}
				}
			}
		}

		if (log.isDebugEnabled()) {
			if (hxgnDisplayService == null) {
				log.debug("HxgnDisplayService is NOT available.. Use default behavior..");
			} else {
				log.debug("Use HxgnDisplayService to lookup property value and sorting..");
			}
		}
	}

	/**
	 * Evaluates property value and translates to proper language, if necessary.
	 * @param evalBean Bean whose property is to be extracted
	 * @param evalName Name of the property to be extracted
	 * @return Object Property value; null if any exception occurs
	 */
	public static Object findFieldValue(Object evalBean, String evalName) {
		try {
			// try to "translate" name for localized entity first..
			String value = null;
			if (translateMethod != null) {
				value = (String) translateMethod.invoke(hxgnDisplayService, evalBean, evalName);
			}

			// if "translated" name is not found (e.g. not defined, evaluated object is not localized), return property value normally..
			return (value != null) ?
					value : PropertyUtils.getSimpleProperty(evalBean, evalName);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return null;
	}

	/**
	 * Translates sort name if sorting on a code property. Otherwise use the regular sortProperty.
	 * @param tableTag the current tableTag object
	 * @param property Name of the property
	 * @param sortProperty Name of the sortProperty
	 * @return String proper sort property name
	 */
    public static String translateSortingPropertyIfNecessary(TableTag tableTag, String property, String sortProperty) {
    	// sorting in memory.. sortProperty is ignored anyway..
    	if (tableTag.getTableModel().isLocalSort())
    		return sortProperty;

    	// respect manually-specified sortProperty..
    	if (StringUtils.isNotBlank(sortProperty))
    		return sortProperty;

    	// no result found.. sortProperty is useless anyway..
    	if (tableTag.getCurrentRow() == null)
    		return sortProperty;

    	// no "external" service found so no "translation"..
		if (translateSortingPropertyMethod == null)
    		return sortProperty;
			
		try {
			return (String) translateSortingPropertyMethod.invoke(hxgnDisplayService, tableTag.getCurrentRow().getObject(), property);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return sortProperty;
    }
}