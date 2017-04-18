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
 * Per the conditions of the Artistic License,
 * Hexagon Safety & Infrastructure states that it has
 * made the following changes to this source file:
 *
 *  4 August 2015 - Modified Excel export to support Excel 2007+ format
 *  
 */
 
package org.displaytag.export;

import org.apache.commons.lang.StringUtils;


/**
 * Export view for excel 2007+ exporting.
 */
public class XlsxView extends BaseExportView
{

    /**
     * @see org.displaytag.export.ExportView#getMimeType()
     * @return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
     */
    public String getMimeType()
    {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; //$NON-NLS-1$
    }

    /**
     * @see org.displaytag.export.BaseExportView#getRowEnd()
     */
    protected String getRowEnd()
    {
        return "\n"; //$NON-NLS-1$
    }

    /**
     * @see org.displaytag.export.BaseExportView#getCellEnd()
     */
    protected String getCellEnd()
    {
        return "\t"; //$NON-NLS-1$
    }

    /**
     * @see org.displaytag.export.BaseExportView#getAlwaysAppendCellEnd()
     * @return false
     */
    protected boolean getAlwaysAppendCellEnd()
    {
        return false;
    }

    /**
     * @see org.displaytag.export.BaseExportView#getAlwaysAppendRowEnd()
     * @return false
     */
    protected boolean getAlwaysAppendRowEnd()
    {
        return false;
    }

    /**
     * Escaping for excel format.
     * <ul>
     * <li>Quotes inside quoted strings are escaped with a double quote</li>
     * <li>Fields are surrounded by " (should be optional, but sometimes you get a "Sylk error" without those)</li>
     * </ul>
     * @see org.displaytag.export.BaseExportView#escapeColumnValue(java.lang.Object)
     */
    protected String escapeColumnValue(Object value)
    {
        if (value != null)
        {
            // quotes around fields are needed to avoid occasional "Sylk format invalid" messages from excel
            return "\"" //$NON-NLS-1$
                + StringUtils.replace(StringUtils.trim(value.toString()), "\"", "\"\"") //$NON-NLS-1$ //$NON-NLS-2$ 
                + "\""; //$NON-NLS-1$ 
        }

        return null;
    }

}
