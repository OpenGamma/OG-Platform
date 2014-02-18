/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Instant;

import com.google.common.collect.Table;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Writes the results of running scenarios to a tab delimited text file.
 * There are two possible formats:
 * <ul>
 *   <li>Short format. All values calculated for a position / scenario are in the same row.</li>
 *   <li>Long format. There is a separate row for every calculated value. i.e. If there are n values calculated
 *   in the view there will be n rows per position / scenario.</li>
 * </ul>
 */
public class ScenarioResultsWriter {

  // TODO make this configurable?
  /* package */ static final String DELIMITER = "\t";

  private ScenarioResultsWriter() {
  }

  /*public static void writeLongFormat(List<ScenarioResultModel> allScenarioResults, String fileName) throws IOException {
    try (Writer writer = new BufferedWriter(new FileWriter(fileName))) {
      writeLongFormat(allScenarioResults, writer);
    }
  }

  public static void writeLongFormat(List<ScenarioResultModel> allScenarioResults, Appendable appendable) {
  }*/

  public static void writeShortFormat(List<ScenarioResultModel> allScenarioResults, String fileName) throws IOException {
    try (Writer writer = new BufferedWriter(new FileWriter(fileName))) {
      writeShortFormat(allScenarioResults, writer);
    }
  }

  public static void writeShortFormat(List<ScenarioResultModel> allScenarioResults, Appendable appendable) throws IOException {
    if (allScenarioResults.isEmpty()) {
      appendable.append("NO RESULTS");
      return;
    }
    ScenarioResultModel firstResults = allScenarioResults.get(0);

    // write the header ----------------------------------------

    // this assumes all scenarios have the same number of parameters which should always be true
    StringBuilder builder = new StringBuilder(metadataHeader(firstResults.getParameters().size()));
    List<String> columnNames = firstResults.getResults().getColumnNames();

    // there is one column of calculated data for each column in the results table
    for (String columnName : columnNames) {
      builder.append(DELIMITER).append(columnName);
      //headerBuilder.append(DELIMITER).append('\'').append(columnName).append('\'');
    }
    builder.append('\n');
    appendable.append(builder.toString());

    // write the row results ----------------------------------------

    for (ScenarioResultModel scenarioResults : allScenarioResults) {
      SimpleResultModel simpleResults = scenarioResults.getResults();
      Table<Integer, Integer, Object> resultsTable = simpleResults.getResults();

      for (Map.Entry<Integer, Map<Integer, Object>> entry : resultsTable.rowMap().entrySet()) {
        int rowIndex = entry.getKey();
        Map<Integer, Object> rowValues = entry.getValue();
        builder.setLength(0);
        builder.append(metadataColumns(scenarioResults, rowIndex));

        for (Object value : rowValues.values()) {
          builder.append(DELIMITER).append(((ComputedValueResult) value).getValue());
        }
        builder.append('\n');
        appendable.append(builder.toString());
      }
    }
  }

  private static String metadataHeader(int paramCount) {
    StringBuilder builder = new StringBuilder();
    builder.append("ScenarioName").append(DELIMITER).append("ValuationTime").append(DELIMITER).append("TargetId");
    for (int i = 1; i <= paramCount; i++) {
      builder.append(DELIMITER).append("ParamName").append(i).append(DELIMITER).append("ParamValue").append(i);
    }
    return builder.toString();
  }

  /**
   * @return Formats the data in the specified row up to but not including the calculated results
   */
  private static String metadataColumns(ScenarioResultModel scenarioResults, int rowIndex) {
    String scenarioName = scenarioResults.getScenarioName();
    SimpleResultModel simpleResults = scenarioResults.getResults();
    Instant valuationTime = simpleResults.getValuationTime();
    UniqueIdentifiable target = simpleResults.getTargets().get(rowIndex);
    StringBuilder builder = new StringBuilder();
    builder
        .append(scenarioName)
        .append(DELIMITER)
        .append(valuationTime)
        .append(DELIMITER)
        .append(target.getUniqueId());

    for (Map.Entry<String, Object> entry : scenarioResults.getParameters().entrySet()) {
      String paramName = entry.getKey();
      Object paramValue = entry.getValue();

      builder.append(DELIMITER).append(paramName).append(DELIMITER).append(paramValue);
    }
    return builder.toString();
  }
}
