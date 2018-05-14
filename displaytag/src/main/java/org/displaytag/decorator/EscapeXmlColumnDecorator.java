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
 *  27 April 2018 - Ability to customize the set of columns displayed
 *        and their order  
 */
package org.displaytag.decorator;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.MediaTypeEnum;


/**
 * This takes the string that is passed in, and escapes html tags and entities. Only operates on "html" or "xml" media.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class EscapeXmlColumnDecorator implements DisplaytagColumnDecorator
{

    /**
     * Instance used for the "escapeXml" tag attribute.
     */
    public static final DisplaytagColumnDecorator INSTANCE = new EscapeXmlColumnDecorator();

    /**
     * @see org.displaytag.decorator.DisplaytagColumnDecorator#decorate(Object, PageContext, MediaTypeEnum)
     */
    public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media)
    {
    	boolean isCustomLink = columnValue != null && columnValue.toString().indexOf("<a class='custom_grid' href='") != -1;
    	boolean isCustomProperty = columnValue != null && columnValue.toString().indexOf("<div class='custom_grid'>") != -1;
    	if (isCustomLink || isCustomProperty) {
    		if (media.equals(MediaTypeEnum.HTML))
    			return columnValue;
    		else
    			return removeHtmlTags(StringUtils.trimToNull(columnValue.toString()));
    	}

        if (columnValue == null || (!media.equals(MediaTypeEnum.HTML) && !media.equals(MediaTypeEnum.XML)))
        {
            return columnValue;
        }

        return StringEscapeUtils.escapeXml(columnValue.toString());
    }

    
    private String removeHtmlTags(String html) {
    	if (html == null)
    		return html;
    	
		List<String> entry = new ArrayList<>();
		StringBuilder builder = null;
		
		int startPos = -1;
		for (int i = 0; i < html.length(); i++) {
			char ch = html.charAt(i);

			if (ch == '>') {
				startPos = i;
				builder = new StringBuilder();
			}
			else if (ch == '<' && i > 0) {
				startPos = -1;
				if (builder.length() > 0) 
					entry.add(builder.toString());
			} else if (startPos != -1 && i > startPos) {
				builder.append(ch);
			}
		}
	
		return StringUtils.join(entry, ", ");
    		
    }

}
