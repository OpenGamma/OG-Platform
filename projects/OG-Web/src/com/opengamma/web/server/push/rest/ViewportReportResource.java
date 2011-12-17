/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.reports.Report;
import com.opengamma.web.server.push.reports.ReportFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * REST resource for generating {@link Report}s from {@link Viewport}s.  Support for different report formats is
 * configured in {@link ReportFactory}
 * @see ReportFactory
 */
public class ViewportReportResource {

  private final Viewport _viewport;
  private final ReportFactory _reportFactory;

  public ViewportReportResource(Viewport viewport, ReportFactory reportFactory) {
    _viewport = viewport;
    _reportFactory = reportFactory;
  }

  /**
   * Returns a report containing the viewport's current data.  The {@code format} part of the URL is configured
   * by the keys supplied to {@link ReportFactory#ReportFactory(java.util.Map)}.
   * @param format The format of the report
   * @return A report containing the viewport's current data
   */
  @GET
  @Path("{format}")
  public Response getReport(@PathParam("format") String format) {
    Report report = _reportFactory.generateReport(format, _viewport.getRawData());
    return Response.ok(report, report.getMediaType()).header("Content-Disposition", "attachment; filename=" + report.getFilename()).build();
  }
}
