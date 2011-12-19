package com.opengamma.web.server.push.reports;

/**
 * <p>This package contains a very simple framework for generating reports from snapshots of view data.  It is
 * intended to separate the report generation from the web code and allow arbitrary report formats to be easily added.
 * Examples of possible report formats are CSV, Excel worksheets or PDF documents.  Support can be added for a
 * format by implementing a {@link ReportGenerator} and configuring {@link ReportFactory}.  The new
 * format will automatically be availble via {@link com.opengamma.web.server.push.rest.ViewportReportResource}.</p>
 * <p><em>This is almost certainly too simplistic and will have to be revisited when reporting is tackled
 * properly</em><p/>
 */
