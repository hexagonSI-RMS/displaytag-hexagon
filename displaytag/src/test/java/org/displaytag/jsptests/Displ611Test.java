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
 * 5 January 2017
 * Fix DISPL-611 - Column text should not be abbreviated in pdf/excel export
 *          when maxLength is set
 */ 
package org.displaytag.jsptests;

import java.io.InputStream;

import org.displaytag.export.ExportViewFactory;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.test.DisplaytagCase;
import org.displaytag.util.ParamEncoder;

import com.lowagie.text.pdf.PdfReader;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**
 * Tests for content in column body.
 * @author Fabrizio Giustina
 * @version $Revision: 1081 $ ($Author: fgiust $)
 */
public class Displ611Test extends DisplaytagCase
{

    /**
     * @see org.displaytag.test.DisplaytagCase#getJspName()
     */
    public String getJspName()
    {
        return "DISPL-611.jsp";
    }

    /**
     * Check content in generated table.
     * @param jspName jsp name, with full path
     * @throws Exception any axception thrown during test.
     */
    public void doTest(String jspName) throws Exception
    {
        ParamEncoder encoder = new ParamEncoder("table");
        String mediaParameter = encoder.encodeParameterName(TableTagParameters.PARAMETER_EXPORTTYPE);
        WebRequest request = new GetMethodWebRequest(jspName);

        // this will force media type initialization
        ExportViewFactory.getInstance();
        MediaTypeEnum pdfMedia = MediaTypeEnum.fromName("pdf");
        assertNotNull("Pdf export view not correctly registered.", pdfMedia);
        request.setParameter(mediaParameter, Integer.toString(pdfMedia.getCode()));

        WebResponse response = runner.getResponse(request);

        // we are really testing a pdf output?
        assertEquals("Expected a different content type.", "application/pdf", response.getContentType());

        InputStream stream = response.getInputStream();
        byte[] result = new byte[3000];
        stream.read(result);

        PdfReader reader = new PdfReader(result);
        assertEquals("Expected a valid pdf file with a single page", 1, reader.getNumberOfPages());

        // simple way to verify the content..
        String content = new String(reader.getPageContent(1));
        assertTrue("Expected full text exported to the pdf file", content.contains("(camel)"));
    }
}