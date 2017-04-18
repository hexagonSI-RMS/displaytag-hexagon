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
 * extension to the Standard Package for the following purposes:
 * 
 * 25 July 2014
 * Facilitate unit testing of the secured table row features.
 *
 * 4 August 2015
 * Modifed Excel export to support Excel 2007+ format
 */
package org.displaytag.render;

import junit.framework.TestCase;

public class SecurityServiceTest extends TestCase {
	
	public void testUnsecuredObject() throws Exception {
		SecurityService service = new SecurityService(SampleObject.class);
		boolean rowSecured = service.isRowSecured(new SampleObject(false, "test1"));
		assertFalse(rowSecured);
	}
	
	public void testSecuredObject() throws Exception {
		SecurityService service = new SecurityService(SampleObject.class);
		boolean rowSecured = service.isRowSecured(new SampleObject(true, "test1"));
		assertTrue(rowSecured);
	}
}
