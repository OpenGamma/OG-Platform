/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.reports;

import com.opengamma.DataNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates {@link Report}s for different report formats.
 */
public class ReportFactory {

  /** Generators keyed by format name */
  private final Map<String, ReportGenerator> _generators;

  /**
   * @param generators {@link ReportGenerator}s keyed by format name
   */
  public ReportFactory(Map<String, ReportGenerator> generators) {
    _generators = new HashMap<String, ReportGenerator>(generators);
  }

  /**
   * Creates a report from {@link ViewportData} using a specified format.
   * @param format The name of the report format
   * @param rawData The view data for the report
   * @return A report in the specified format
   * @throws DataNotFoundException If no generator can be found for the specified format
   */
  public Report generateReport(String format, ViewportData rawData) {
    ReportGenerator generator = _generators.get(format);
    if (generator == null) {
      throw new DataNotFoundException("No report generator available for format " + format);
    }
    return generator.generateReport(rawData);
  }
}
