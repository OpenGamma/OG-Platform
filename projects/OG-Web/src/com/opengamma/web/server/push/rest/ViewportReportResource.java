/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.reports.Report;
import com.opengamma.web.server.push.reports.ReportGenerator;
import com.opengamma.web.server.push.reports.ReportGeneratorFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * REST resource for generating {@link Report}s from {@link Viewport}s.
 * @see ReportGeneratorFactory
 */
public class ViewportReportResource {

  private final Viewport _viewport;
  private final ReportGeneratorFactory _reportGeneratorFactory;

  public ViewportReportResource(Viewport viewport, ReportGeneratorFactory reportGeneratorFactory) {
    _viewport = viewport;
    _reportGeneratorFactory = reportGeneratorFactory;
  }

  /**
   * @param format The format of the report
   * @return A report containing the lastest snapshot of the viewport's data
   */
  @GET
  @Path("{format}")
  public Response getReport(@PathParam("format") String format) {
    ReportGenerator generator = _reportGeneratorFactory.getReportGenerator(format);
    Report report = generator.generateReport(_viewport.getRawData());
    return Response.ok(report, generator.getMediaType()).header("Content-Disposition", "attachment; filename=" + report.getFilename()).build();
  }
}
