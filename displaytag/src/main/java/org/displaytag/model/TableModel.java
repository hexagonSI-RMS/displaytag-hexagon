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
 *  18 July 2013 - Solve issue DISPL-242 - Added support for "grouped"
 *        table headers by adding two new attributes: groupTitle 
 *        and groupTitleKey
 *
 *  27 April 2018 - Add ability to customize set of columns and their order  
 */
package org.displaytag.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.decorator.EscapeXmlColumnDecorator;
import org.displaytag.decorator.TableDecorator;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.properties.TableProperties;
import org.displaytag.tags.TableTag;
import org.displaytag.util.HtmlAttributeMap;


/**
 * Table Model. Holds table data for presentation.
 * @author Fabrizio Giustina
 * @version $Revision: 1125 $ ($Author: fgiust $)
 */
@SuppressWarnings("all")
public class TableModel
{
	private boolean customizationDisabled;
	CustomTableData applicationCustomTableData;
	private boolean isInUICustomizationMode; // carry this so we write out customizationControls only in UICustomizationMode
	private String defaultTableSortProperty;
	private String paginatedListSortProperty;

    /**
     * logger.
     */
    private static Log log = LogFactory.getLog(TableModel.class);

    /**
     * list of HeaderCell.
     */
    private List headerCellList;

    /**
     * full list (contains Row objects).
     */
    private List rowListFull;

    /**
     * list of data to be displayed in page.
     */
    private List rowListPage;

    /**
     * Name of the column currently sorted (only used when sort=external).
     */
    private String sortedColumnName;

    /**
     * sort order = ascending?
     */
    private boolean sortOrderAscending = true;

    /**
     * sort full List? (false sort only displayed page).
     */
    private boolean sortFullTable = true;

    /**
     * index of the sorted column (-1 if the table is not sorted).
     */
    private int sortedColumn = -1;

    /**
     * Table decorator.
     */
    private TableDecorator tableDecorator;

    /**
     * id inherited from the TableTag (needed only for logging).
     */
    private String id;

    /**
     * configurable table properties.
     */
    private TableProperties properties;

    /**
     * Starting offset for elements in the viewable list.
     */
    private int pageOffset;

    /**
     * Response encoding.
     */
    private String encoding;

    /**
     * Are we sorting locally? (Default True)
     */
    private boolean localSort = true;

    /**
     * Table caption.
     */
    private String caption;

    /**
     * Table footer.
     */
    private String footer;

    /**
     * Jsp page context.
     */
    private PageContext pageContext;

    /**
     * Current media.
     */
    private MediaTypeEnum media;

    /**
     * Uses post for links.
     */
    private String form;

    private List subHeaderCellList;

    /**
     * DISPL-242: List of sub group HeaderCells.
     */
    private List mainHeaderCellList;

    /**
     * Constructor for TableModel.
     * @param tableProperties table properties
     * @param charEncoding response encoding
     */
    public TableModel(TableProperties tableProperties, String charEncoding, PageContext pageContext)
    {
        this.rowListFull = new ArrayList(20);
        this.headerCellList = new ArrayList(20);
        this.properties = tableProperties;
        this.encoding = charEncoding;
        this.pageContext = pageContext;
    }

    private boolean isConfiguredDefaultTableSortColumn(HeaderCell headerCell) {
    	String sortedProperty = this.paginatedListSortProperty != null ? this.paginatedListSortProperty : this.defaultTableSortProperty;
    	
    	if (sortedProperty != null ) {
			if (headerCell.getSortProperty() != null)
				return StringUtils.equalsIgnoreCase(headerCell.getSortProperty(), sortedProperty);
			else
				return StringUtils.equalsIgnoreCase(headerCell.getBeanPropertyName(), sortedProperty);
    	}
    	return false;
    }
    
    public void applyApplicationCustomizations(Comparator defaultColumnComparator) {
    	if (this.applicationCustomTableData == null) return;
    	
    	// reorder headers:
    	
    	Map<String, HeaderCell> headerCellByCotsTitle = sortHeaderCellsByTitle();
		List<HeaderCell> newHeaderCellList = new ArrayList<>();
		
		for (CustomColumnData colConfig : applicationCustomTableData.getVisibleColumnList()) {
			HeaderCell headerCell = colConfig.getIsAdded() 
									? createHeaderCell(colConfig, defaultColumnComparator)
									: headerCellByCotsTitle.get(colConfig.getCotsTitle());
									
			// column is not available for the media type (as an improvement, can set a ref to the unavailable columns at ColumnTag.doEndTag)
			if (headerCell == null) 
				continue;
			
			// change COTS header cell titles if there's a customized one:
			if (!colConfig.getIsAdded()
				&& StringUtils.isNotBlank(colConfig.getCustomTitle()) 
				&& !StringUtils.equals(colConfig.getCotsTitle(), colConfig.getCustomTitle())) {
				
				headerCell.setTitle(colConfig.getCustomTitle());
			}
			
			headerCell.setCustomColumnData(colConfig);
			
			newHeaderCellList.add(headerCell);
		}
		
		// set default table sort, this just marks the table header cell with the sorted style for display:
		for (int i = 0; i < newHeaderCellList.size(); i++) {
			HeaderCell headerCell = newHeaderCellList.get(i);
			if (isLocalSort()) {
				if (sortedColumn == i) 
					headerCell.setAlreadySorted();
				else
					headerCell.clearAlreadySorted();
			}
			else { // paginated sort:
				if (isConfiguredDefaultTableSortColumn(headerCell))
					headerCell.setAlreadySorted();
				else
					headerCell.clearAlreadySorted();
			}
		}
		
		this.headerCellList = newHeaderCellList;
		
		// reorder columns:
		for (int rowIndex = 0; rowIndex < rowListFull.size(); rowIndex++) {
			 Row currentRow = (Row) rowListFull.get(rowIndex);
			 // System.out.println("reordering row: "  + currentRow.getRowNumber());
		 
			 List<Cell> newCellList = new ArrayList<Cell>(headerCellList.size());
			 
			 for (int columnNumber = 0; columnNumber < headerCellList.size(); columnNumber++) {
				 HeaderCell header = (HeaderCell) headerCellList.get(columnNumber);
				 header.setColumnNumber(columnNumber);
				 // System.out.println("columnNumber: " + columnNumber, header.getBeanPropertyName: " + header.getBeanPropertyName());
			    
				 if (!header.getCustomColumnData().getIsAdded()) { 
					 // COTS column, cell data is already evaluated if there's body content (JSP), so copy it over
					 Cell cell = header.getCellAtRow(rowIndex); 
					 newCellList.add(cell);
				 }
				 else {
				 	// custom column, data fetched on writing out cell content
		        	newCellList.add(getEmptyCell());
				 }
			 }
			   
			 currentRow.getCellList().clear();
			 currentRow.getCellList().addAll(newCellList);
		}
    }
    
    private Cell getEmptyCell() {
    	Cell cell = new Cell(null);
    	HtmlAttributeMap attributes = new HtmlAttributeMap();
    	attributes.put("class", "result");
    	cell.setPerRowAttributes(attributes);
    	return cell;
    }
    
    
    private HeaderCell createHeaderCell(CustomColumnData colConfig, Comparator defaultColumnComparator) {
        HeaderCell headerCell = new HeaderCell();
        headerCell.setHeaderAttributes(new HtmlAttributeMap());
        headerCell.setHtmlAttributes(new HtmlAttributeMap());
        headerCell.setTitle(colConfig.getTitle());
        headerCell.setSortable(colConfig.getSortable());
        headerCell.setBeanPropertyName(colConfig.getPropertyName());
        headerCell.setMaxLength(colConfig.getMaxLength());
        headerCell.setSortProperty(colConfig.getSortOnProperty());
        headerCell.setColumnDecorators(new DisplaytagColumnDecorator[] {EscapeXmlColumnDecorator.INSTANCE});
    	headerCell.setComparator(defaultColumnComparator);
        return headerCell;
    }
    
	private Map<String, HeaderCell> sortHeaderCellsByTitle() {
		Map<String, HeaderCell> headerCellsByTitle = new HashMap<>();
    	for (Object header : headerCellList) {
    		HeaderCell headerCell = (HeaderCell) header;
    		headerCellsByTitle.put(headerCell.getTitle(), headerCell);
    	}
    	return headerCellsByTitle;
    }
	
//    private boolean isSelectBox(String title) {
//    	return StringUtils.indexOf(title, "<div>Select</div>") != -1;
//    }
    
    
    /**
     * Returns the jsp page context.
     * @return page context
     */
    protected PageContext getPageContext()
    {
        return this.pageContext;
    }

    /**
     * Gets the current media type.
     * @return current media (html, pdf ...)
     */
    public MediaTypeEnum getMedia()
    {
        return this.media;
    }

    /**
     * sets the current media type.
     * @param media current media (html, pdf ...)
     */
    public void setMedia(MediaTypeEnum media)
    {
        this.media = media;
    }

    /**
     * Sets whether the table performs local in memory sorting of the data.
     * @param localSort
     */
    public void setLocalSort(boolean localSort)
    {
        this.localSort = localSort;
    }

    /**
     * @return sorting in local memory
     */
    public boolean isLocalSort()
    {
        return localSort;
    }

    /**
     * Getter for <code>form</code>.
     * @return Returns the form.
     */
    public String getForm()
    {
        return this.form;
    }

    /**
     * Setter for <code>form</code>.
     * @param form The form to set.
     */
    public void setForm(String form)
    {
        this.form = form;
    }

    /**
     * Sets the starting offset for elements in the viewable list.
     * @param offset The page offset to set.
     */
    public void setPageOffset(int offset)
    {
        this.pageOffset = offset;
    }

    /**
     * Setter for the tablemodel id.
     * @param tableId same id of table tag, needed for logging
     */
    public void setId(String tableId)
    {
        this.id = tableId;
    }

    /**
     * get the table id.
     * @return table id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * get the full list.
     * @return the full list containing Row objects
     */
    public List getRowListFull()
    {
        return this.rowListFull;
    }

    /**
     * gets the partial (paginated) list.
     * @return the partial list to display in page (contains Row objects)
     */
    public List getRowListPage()
    {
        return this.rowListPage;
    }

    /**
     * adds a Row object to the table.
     * @param row Row
     */
    public void addRow(Row row)
    {
        row.setParentTable(this);

        if (log.isDebugEnabled())
        {
            log.debug("[" + this.id + "] adding row " + row);
        }
        this.rowListFull.add(row);
    }

    /**
     * sets the name of the currently sorted column
     * @param sortedColumnName
     */
    public void setSortedColumnName(String sortedColumnName)
    {
        this.sortedColumnName = sortedColumnName;
    }

    /**
     * sets the sort full table property. If true the full list is sorted, if false sorting is applied only to the
     * displayed sublist.
     * @param sortFull boolean
     */
    public void setSortFullTable(boolean sortFull)
    {
        this.sortFullTable = sortFull;
    }

    /**
     * return the sort full table property.
     * @return boolean true if sorting is applied to the full list
     */
    public boolean isSortFullTable()
    {
        return this.sortFullTable;
    }

    /**
     * return the sort order of the page.
     * @return true if sort order is ascending
     */
    public boolean isSortOrderAscending()
    {
        return this.sortOrderAscending;

    }

    /**
     * set the sort order of the list.
     * @param isSortOrderAscending true to sort in ascending order
     */
    public void setSortOrderAscending(boolean isSortOrderAscending)
    {
        this.sortOrderAscending = isSortOrderAscending;
    }

    /**
     * @param rowList - the new value for this.rowListPage
     */
    public void setRowListPage(List rowList)
    {
        this.rowListPage = rowList;
    }

    /**
     * getter for the Table Decorator.
     * @return TableDecorator
     */
    public TableDecorator getTableDecorator()
    {
        return this.tableDecorator;
    }

    /**
     * setter for the table decorator.
     * @param decorator - the TableDecorator object
     */
    public void setTableDecorator(TableDecorator decorator)
    {
        this.tableDecorator = decorator;
    }

    /**
     * returns true if the table is sorted.
     * @return boolean true if the table is sorted
     */
    public boolean isSorted()
    {
        return this.sortedColumn != -1;
    }

    /**
     * returns the HeaderCell for the sorted column.
     * @return HeaderCell
     */
    public HeaderCell getSortedColumnHeader()
    {
    	if (this.localSort) {
    		// this applies application customization of the default table sort property:
    		if (sortedColumn == -1) { // this is when the user never clicked on any table header to sort it yet
    			if (this.defaultTableSortProperty != null) {
			    	for (int i = 0; i < headerCellList.size(); i++) {
			    		HeaderCell headerCell = (HeaderCell) headerCellList.get(i);
						if (isConfiguredDefaultTableSortColumn(headerCell)) {
							setSortedColumnNumber(i);
							return headerCell;
						}
					}
			    	return null;
    			}
    		}
        if (this.sortedColumn < 0 || (this.sortedColumn > (this.headerCellList.size() - 1)))
        {
            return null;
        }
        return (HeaderCell) this.headerCellList.get(this.sortedColumn);
    	} else {
    		return null;
    	}
    }

    /**
     * return the number of columns in the table.
     * @return int number of columns
     */
    public int getNumberOfColumns()
    {
        return this.headerCellList.size();
    }

    /**
     * return true is the table has no columns.
     * @return boolean
     */
    public boolean isEmpty()
    {
        return this.headerCellList.size() == 0;
    }

    /**
     * return the index of the sorted column.
     * @return index of the sorted column or -1 if the table is not sorted
     */
    public int getSortedColumnNumber()
    {
        return this.sortedColumn;
    }

    /**
     * set the sorted column index.
     * @param sortIndex - the index of the sorted column
     */
    public void setSortedColumnNumber(int sortIndex)
    {
        this.sortedColumn = sortIndex;
    }

    /**
     * Adds a column header (HeaderCell object).
     * @param headerCell HeaderCell
     */
    public void addColumnHeader(HeaderCell headerCell)
    {
        if (this.sortedColumnName == null)
        {
            if (this.sortedColumn == this.headerCellList.size())
            {
                headerCell.setAlreadySorted();
            }
        }
        else
        {
            // the sorted parameter was a string so try and find that column name and set it as sorted
            if (this.sortedColumnName.equals(headerCell.getSortName()))
            {
                headerCell.setAlreadySorted();
            }
        }
        headerCell.setColumnNumber(this.headerCellList.size());

        this.headerCellList.add(headerCell);
    }

    /**
     * List containing headerCell objects.
     * @return List containing headerCell objects
     */
    public List getHeaderCellList()
    {
        return this.headerCellList;
    }

    /**
     * returns a RowIterator on the requested (full|page) list.
     * @return RowIterator
     * @param full if <code>true</code> returns an iterator on te full list, if <code>false</code> only on the
     * viewable part.
     * @see org.displaytag.model.RowIterator
     */
    public RowIterator getRowIterator(boolean full)
    {
        RowIterator iterator = new RowIterator(
            full ? this.rowListFull : this.rowListPage,
            this.headerCellList,
            this.tableDecorator,
            this.pageOffset);
        // copy id for logging
        iterator.setId(this.id);
        return iterator;
    }

    /**
     * sorts the given list of Rows. The method is called internally by sortFullList() and sortPageList().
     * @param list List
     */
    private void sortRowList(List list)
    {
        if (isSorted())
        {
            HeaderCell sortedHeaderCell = getSortedColumnHeader();

            if (sortedHeaderCell != null)
            {
                // If it is an explicit value, then sort by that, otherwise sort by the property...
                if (sortedHeaderCell.getBeanPropertyName() != null
                    || (this.sortedColumn != -1 && this.sortedColumn < this.headerCellList.size()))
                {

                    String sorted = (sortedHeaderCell.getSortProperty() != null)
                        ? sortedHeaderCell.getSortProperty()
                        : sortedHeaderCell.getBeanPropertyName();

                    Collections.sort(list, new RowSorter(
                        this.sortedColumn,
                        sorted,
                        getTableDecorator(),
                        this.sortOrderAscending,
                        sortedHeaderCell.getComparator()));
                }
            }

        }

    }

    /**
     * sort the list displayed in page.
     */
    public void sortPageList()
    {
        if (log.isDebugEnabled())
        {
            log.debug("[" + this.id + "] sorting page list");
        }
        sortRowList(this.rowListPage);

    }

    /**
     * sort the full list of data.
     */
    public void sortFullList()
    {
        if (log.isDebugEnabled())
        {
            log.debug("[" + this.id + "] sorting full data");
        }
        sortRowList(this.rowListFull);
    }

    /**
     * Returns the table properties.
     * @return the configured table properties.
     */
    public TableProperties getProperties()
    {
        return this.properties;
    }

    /**
     * Getter for character encoding.
     * @return Returns the encoding used for response.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Obtain this table's caption.
     * @return This table's caption.
     */
    public String getCaption()
    {
        return this.caption;
    }

    /**
     * Set this table's caption.
     * @param caption This table's caption.
     */
    public void setCaption(String caption)
    {
        this.caption = caption;
    }

    /**
     * Obtain this table's footer.
     * @return This table's footer.
     */
    public String getFooter()
    {
        return this.footer;
    }

    /**
     * Set this table's footer.
     * @param footer This table's footer.
     */
    public void setFooter(String footer)
    {
        this.footer = footer;
    }

    /**
     * DISPL-242: new grouped title attribute 
     */
    public List getSubHeaderCellList() {
		return subHeaderCellList;
	}

	public void setSubHeaderCellList(List subHeaderCellList) {
		this.subHeaderCellList = subHeaderCellList;
	}
      
    public List getMainHeaderCellList() {
		return mainHeaderCellList;
	}

	public void setMainHeaderCellList(List mainHeaderCellList) {
		this.mainHeaderCellList = mainHeaderCellList;
	}

	public boolean isGroupedTableHeader()
	{
    	boolean isGroupHeader = false;

    	List extHeaderderList = this.headerCellList;
    	List newHeaderList = new ArrayList();

    	if ((extHeaderderList != null)&&(extHeaderderList.size()>0)){
    		//arrange tables to have a grouped header
    		Iterator iterator = extHeaderderList.iterator();
    		String prevHeader = "";
    		int colCount = 0; // colspan count
    		subHeaderCellList = new ArrayList();

    		 while (iterator.hasNext()){
    			 // get the header cell
    	            HeaderCell headerCell = (HeaderCell) iterator.next();
    	            //check its a column with grouped header 
    	            String groupedHeader = headerCell.getGroupHeader();
    	            if ((groupedHeader!=null)&&(groupedHeader.trim().length()>0)){
    	            	isGroupHeader = true;
    	            	if (prevHeader.equals(groupedHeader)){ 
    	            		subHeaderCellList.add(headerCell); 
    	            		colCount++; // increase column count
    	            		//dont add new header cell
    	            	}else {// new group header 
    	            		//(contious group headers) 
    	            		if (prevHeader.trim().length()>0){
    	            			//add new header cell for grouped header
    	            			newHeaderList.add(getGroupedHeader(prevHeader,colCount));
    	            		}
//    	            		reset group header related variables
    	            		colCount = 1;
    	            		prevHeader = groupedHeader;
    	            		subHeaderCellList.add(headerCell);
    	            	}
    	            	
    	            } else {// its not a group header 
    	            	//check there is already a group header
    	            	if (prevHeader.trim().length()>0){
    	            		//add new header cell for grouped header
    	            		newHeaderList.add(getGroupedHeader(prevHeader,colCount));
    	            		//reset group header related variables
    	            		colCount = 0;
    	            		prevHeader = "";
    	            	}
    	            	//add un grouped cell
    	            	newHeaderList.add(headerCell);
    	            }
    		 }
    		 //in case grouping at the end
    		 if (prevHeader.trim().length()>0){
         		//add new header cell for grouped header
         		newHeaderList.add(getGroupedHeader(prevHeader,colCount));
         	}

    		 // the header cell list needs to be changed only if it contains grouped header
    		if (isGroupHeader){
    			this.mainHeaderCellList = newHeaderList;
    		}
    	}

    	return isGroupHeader;
    }

    private HeaderCell getGroupedHeader(String groupedHeaderTitle, int colCount)
    {
		HeaderCell groupedHeaderCell = new HeaderCell();
 		// this must provided with i18n key
 		groupedHeaderCell.setTitle(groupedHeaderTitle);
 		groupedHeaderCell.setIsGroupedHeader(true);
 		groupedHeaderCell.addColSpan(colCount);
 		//this is to center the group header
 		groupedHeaderCell.addHeaderClass("groupedHead");
 		groupedHeaderCell.setSortable(false);

 		return groupedHeaderCell;
	}

    /****  DISPL-242  ********/

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
            .append("rowListFull", this.rowListFull) //$NON-NLS-1$
            .append("rowListPage", this.rowListPage) //$NON-NLS-1$
            .append("properties", this.properties) //$NON-NLS-1$
            .append("empty", this.isEmpty()) //$NON-NLS-1$
            .append("encoding", this.encoding) //$NON-NLS-1$
            .append("numberOfColumns", this.getNumberOfColumns()) //$NON-NLS-1$
            .append("headerCellList", this.headerCellList) //$NON-NLS-1$
            .append("sortFullTable", this.sortFullTable) //$NON-NLS-1$
            .append("sortedColumnNumber", this.getSortedColumnNumber()) //$NON-NLS-1$
            .append("sortOrderAscending", this.sortOrderAscending) //$NON-NLS-1$
            .append("sortedColumnHeader", this.getSortedColumnHeader()) //$NON-NLS-1$
            .append("sorted", this.isSorted()) //$NON-NLS-1$
            .append("tableDecorator", this.tableDecorator) //$NON-NLS-1$
            .append("caption", this.caption) //$NON-NLS-1
            .append("footer", this.footer) //$NON-NLS-1
            .append("media", this.media) //$NON-NLS-1
            .toString();
    }

	
    public CustomTableData getApplicationCustomTableData() {
		return applicationCustomTableData;
	}

	public void setApplicationCustomTableData(CustomTableData applicationCustomTableData) {
		this.applicationCustomTableData = applicationCustomTableData;
	}

    public boolean isInUICustomizationMode() {
		return isInUICustomizationMode;
	}

	public void setInUICustomizationMode(boolean isInUICustomizationMode) {
		this.isInUICustomizationMode = isInUICustomizationMode;
	}

	public String getDefaultTableSortProperty() {
		return defaultTableSortProperty;
	}

	public void setDefaultTableSortProperty(String defaultTableSortProperty) {
		this.defaultTableSortProperty = defaultTableSortProperty;
	}

	public Integer getDefaultTableSortColumnIndex() {
		if (this.applicationCustomTableData == null) return null;
		return applicationCustomTableData.getDefaultTableSortColumnIndex();
	}

	public String getPaginatedListSortProperty() {
		return paginatedListSortProperty;
	}

	public void setPaginatedListSortProperty(String paginatedListSortProperty) {
		this.paginatedListSortProperty = paginatedListSortProperty;
	}

	public boolean isCustomizationDisabled() {
		return customizationDisabled;
	}

	public void setCustomizationDisabled(boolean customizationDisabled) {
		this.customizationDisabled = customizationDisabled;
	}
}