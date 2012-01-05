/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.reports;

/**
 * Generates a {@link Report} containing a snapshot of analytics data, e.g. a CSV file, Excel workbook or PDF.
 */
public interface ReportGenerator {

  /**
   * @param rawData The snapshot of raw view data
   * @return A report containing the data
   */
  Report generateReport(ViewportData rawData);
}
