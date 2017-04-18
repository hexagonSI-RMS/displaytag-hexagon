# displaytag-hexagon
Fork of the the "DisplayTag 1.2" project with additional fixes and enhancement.

The following files within the distribution have been modified.

File | Changes
--- | ---
/displaytag/src/main/java/org/displaytag/render/SecurityService.java | New file; implements support for "securing" rows of data so unauthorized users cannot see/export them
/displaytag/src/main/java/org/displaytag/tags/TableTag.java | DISPL-409: wrong sorting column in export; DISPL-439: additional wildcard featrures; change to support "securing" rows of data so unauthorized users cannot see/export them
/displaytag/src/main/java/org/displaytag/render/TableWriterTemplate.java | DISPL-611: Column text should not be abbreviated in PDF/Excel export when maxLength is set; change to support "securing" rows of data so unauthorized users cannot see/export them
/displaytag/src/main/java/org/displaytag/util/LookupUtil.java | DISPL-611: Column text should not be abbreviated in PDF/Excel export when maxLength is set
/displaytag/src/test/java/org/displaytag/jsptests/Displ611Test.java | DISPL-611: Column text should not be abbreviated in PDF/Excel export when maxLength is set
/displaytag-portlet/src/test/java/org/displaytag/portlet/PortletHrefTest.java |  DISPL-611: Column text should not be abbreviated in PDF/Excel export when maxLength is set
/displaytag/src/main/java/org/displaytag/filter/ExportDelegate.java | Set Cache-control values so exported data are not cached.
/displaytag/src/main/java/org/displaytag/filter/BufferedResponseWrapper.java | Prevent DisplayTag exports from stripping no-cache headers on response
/displaytag/src/main/java/org/displaytag/ColumnTag.java | Implement support for "securing" rows of data so unauthorized users cannot see/export them
/displaytag/src/main/java/org/displaytag/export/XlsxView.java | Modified Excel export to support Excel 2007+ format
/displaytag/src/test/java/org/displaytag/render/SampleObject.java | Facilitate testing of secured table row features; Modified Excel export to support Excel 2007+ format
/displaytag/src/test/java/org/displaytag/render/SecurityServiceTest.java | Facilitate testing of secured table row features; Modified Excel export to support Excel 2007+ format
/displaytag-export-poi/src/main/java/org/displaytag/decorator/xssf/DecoratesXssf.java | New file; Modified Excel export to support Excel 2007+ format
/displaytag-export-poi/src/main/java/org/displaytag/export/DefaultXssfExportView.java | New file; Modified Excel export to support Excel 2007+ format
/displaytag-export-poi/src/main/java/org/displaytag/render/XssfTableWriter.java | Modifed Excel export to support Excel 2007+ format
/displaytag/src/main/java/org/displaytag/model/Column.java | Enforce security settings in display/export of data
/displaytag/src/main/java/org/displaytag/model/Row.java | Enforce security settings in display/export of data
/displaytag/src/main/java/org/displaytag/properties/TableProperties.java | Added a property for secured row label
/displaytag/src/test/java/org/displaytag/render/SampleObjectWithoutRecnum.java | Facilitate unit testing of the secured table row features.
/displaytag/src/main/java/org/displaytag/model/HeaderCell.java | DISPL-242: Add support for "grouped" table headers
/displaytag/src/main/java/org/displaytag/model/TableModel.java | DISPL-242: Add support for "grouped" table headers
/displaytag/src/main/java/org/displaytag/render/HtmlTableWriter.java | DISPL-242: Add support for "grouped" table headers
/displaytag/src/main/java/org/displaytag/tags/ColumnTagBeanInfo.java | DISPL-242: Add support for "grouped" table headers
/displaytag/src/main/java/org/displaytag/util/TagConstants.java | DISPL-242: Add support for "grouped" table headers
