/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.reports;

import au.com.bytecode.opencsv.CSVWriter;
import com.opengamma.web.server.WebGridCell;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * Generates CSV files containing a single snapshot of a viewport's data.
 */
public class CsvReportGenerator implements ReportGenerator {

  /** The media type of the generated files */
  private static final MediaType TEXT_CSV = MediaType.valueOf("text/csv");

  @Override
  public Report generateReport(ViewportData viewportData) {
    StringBuilder builder = new StringBuilder();
    builder.append(viewportData.getViewClientId()).append("\n");
    builder.append("Valuation time: ").append(viewportData.getValuationTime()).append("\n\n");

    builder.append("Portfolio\n");
    builder.append(generateGridCsv(viewportData.getPortfolioData()));
    builder.append("\n");
    for (DependencyGraphGridData gridData : viewportData.getPortfolioDependencyGraphData()) {
      WebGridCell cell = gridData.getCell();
      builder.append("Portfolio dependency graph [").append(cell.getRowId()).append(", ").append(cell.getColumnId()).append("]\n");
      builder.append(generateGridCsv(gridData));
      builder.append("\n");
    }
    
    builder.append("Primitives\n");
    builder.append(generateGridCsv(viewportData.getPrimitivesData()));
    builder.append("\n");
    for (DependencyGraphGridData gridData : viewportData.getPrimitivesDependencyGraphData()) {
      WebGridCell cell = gridData.getCell();
      builder.append("Primitives dependency graph [").append(cell.getRowId()).append(", ").append(cell.getColumnId()).append("]\n");
      builder.append(generateGridCsv(gridData));
      builder.append("\n");
    }
    String filename = viewportData.getViewClientId() + "-" + viewportData.getValuationTime() + ".csv";
    ByteArrayInputStream reportStream = new ByteArrayInputStream(builder.toString().getBytes());
    return new Report(filename, reportStream, TEXT_CSV);
  }

  private String generateGridCsv(GridData gridData) {
    StringWriter stringWriter = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);
    String[][] columnHeaders = gridData.getHeaders();
    if (columnHeaders != null) {
      for (String[] header : columnHeaders) {
        csvWriter.writeNext(header);
      }
    }
    String[][] rows = gridData.getRows();
    if (rows != null) {
      for (String[] row : rows) {
        csvWriter.writeNext(row);
      }
    }
    return stringWriter.toString();
  }
}
