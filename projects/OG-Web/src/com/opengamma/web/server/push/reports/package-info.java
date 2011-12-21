/**
 * <p>This package contains a very simple framework for generating reports from snapshots of view data.  It is
 * intended to separate the report generation from the web code and allow arbitrary report formats to be easily added.
 * Only CSV reports are currently supported but others formats will be required.  Support can be added for a
 * format by implementing a {@link com.opengamma.web.server.push.reports.ReportGenerator} and configuring
 * {@link com.opengamma.web.server.push.reports.ReportFactory}.  The new format will automatically be availble via
 * {@link com.opengamma.web.server.push.rest.ViewportReportResource}.</p> <p><em>This is approach is probably too
 * simplistic and will have to be revisited when reporting is tackled</em><p/>
 */
package com.opengamma.web.server.push.reports;
