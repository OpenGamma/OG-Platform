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
 * Returns {@link ReportGenerator}s for different report formats.
 */
public class ReportGeneratorFactory {

  /** Generators keyed by format name */
  private final Map<String, ReportGenerator> _generators;

  /**
   * @param generators {@link ReportGenerator}s keyed by format name
   */
  public ReportGeneratorFactory(Map<String, ReportGenerator> generators) {
    _generators = new HashMap<String, ReportGenerator>(generators);
  }

  /**
   * @param format The name of the report format
   * @return A generator of reports in the specified format
   * @throws DataNotFoundException If no generator can be found for the specified format
   */
  public ReportGenerator getReportGenerator(String format) {
    ReportGenerator generator = _generators.get(format);
    if (generator == null) {
      throw new DataNotFoundException("No report generator available for format " + format);
    }
    return generator;
  }
}
