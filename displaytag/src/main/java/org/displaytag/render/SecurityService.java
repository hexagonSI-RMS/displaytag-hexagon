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
 * Copyright 2014-2017 Intergraph Corporation d/b/a Hexagon Safety & Infrastruture ("Hexagon")
 * Hexagon is part of Hexagon AB.
 * All rights reserved.
 *
 * Per the conditions of the Artistic License,
 * Hexagon Safety & Infrastructure states that it has created this file as an
 * extension to the Standard Package for the following purpose:
 *
 * 25 July 2014
 * To allow "security" access control to be applied to individual rows of a table recordset.
 *
 * 29 July 2014
 * Refactored to use a "securedForUser" property rather than interpreting a specific
 * record number as meaning "secured".
 */
 
package org.displaytag.render;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SecurityService {

	private static Log log = LogFactory.getLog(SecurityService.class);
	
	private Method recnumMethod;
	private HashMap<Class<?>, Method> recnumMethods = new HashMap<Class<?>, Method>();
	
	public SecurityService(Class<?> cl) {
		recnumMethod = getRecnumMethod(cl);
		if (log.isDebugEnabled()) {
			log.debug("creating security service for class:" + cl.toString());
			log.debug("recnumMethod = " + recnumMethod);
		}
	}
	
	public boolean classIsSecurable() {
		return recnumMethod != null;
	}
	
	public boolean isRowSecured(Object rowObject) {
		if (rowObject != null && recnumMethod != null) {
			try {
				Boolean securedForuser = (Boolean) getRecnumMethod(rowObject.getClass()).invoke(rowObject);
	            if (securedForuser != null && securedForuser.booleanValue() == true) {
	            	return true;
	            }
            } catch (Exception e) {
				log.warn("Exception calling getSecuredForUser method on " + rowObject.getClass());
            	return true;
            }
		}
		
		return false;
	}
	
	private Method getRecnumMethod(Class<?> c) {
		if (recnumMethods.containsKey(c)) {
			return recnumMethods.get(c);
		}
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
        	String methodName = methods[i].getName();
        	if ("getSecuredForUser".equals(methodName)) {
        		recnumMethods.put(c, methods[i]);
        		return methods[i];
        	}
        }
        return null;
    }

}
