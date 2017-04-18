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
 * 4 August 2015
 * Modifed Excel export to support Excel 2007+ format
 */
package org.displaytag.export.excel;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.jsp.JspException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.displaytag.Messages;
import org.displaytag.exception.BaseNestableJspTagException;
import org.displaytag.exception.SeverityEnum;
import org.displaytag.export.BinaryExportView;
import org.displaytag.model.TableModel;
import org.displaytag.render.XssfTableWriter;

/**
 * Excel 2007+/xlsx exporter using POI.
 */
public class DefaultXssfExportView implements BinaryExportView
{
    /**
     * TableModel to render.
     */
    private TableModel model;

    public void doExport(OutputStream out) throws IOException, JspException {
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            new XssfTableWriter(wb).writeTable(this.model, "-1");
            wb.write(out);

        } catch (Exception e) {
            throw new XssfGenerationException(e);
        }
    }

    /** 
     * @see org.displaytag.export.ExportView#setParameters(org.displaytag.model.TableModel, boolean, boolean, boolean)
     */
    public void setParameters(TableModel model, boolean exportFullList,
            boolean includeHeader, boolean decorateValues)
    {
        this.model = model;
    }

    /**
     * @see org.displaytag.export.BaseExportView#getMimeType()
     * @return "application/vnd.ms-excel"
     */
    public String getMimeType()
    {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; //$NON-NLS-1$
    }
    
    /**
     * Wraps POI-generated exceptions.
     */
    static class XssfGenerationException extends BaseNestableJspTagException
    {
		/**
         * D1597A17A6.
		 */
		private static final long serialVersionUID = 4790185917169309667L;

        /**
         * Instantiate a new PdfGenerationException with a fixed message and the given cause.
         * @param cause Previous exception
         */
        public XssfGenerationException(Throwable cause)
        {
            super(DefaultXssfExportView.class, Messages.getString("DefaultXssfExportView.errorexporting"), cause); //$NON-NLS-1$
        }

        /**
         * @see org.displaytag.exception.BaseNestableJspTagException#getSeverity()
         */
        public SeverityEnum getSeverity()
        {
            return SeverityEnum.ERROR;
        }
    }
}
