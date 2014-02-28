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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;

/**
 * Writes the results of running scenarios to a tab delimited text file.
 * There are two possible formats:
 * <ul>
 *   <li>Short format. All values calculated for a position / scenario are in the same row.</li>
 *   <li>Long format. There is a separate row for every calculated value. i.e. If there are n values calculated
 *   in the view there will be n rows per position / scenario. This is intended to be easy to use when creating
 *   pivot tables and using filtering in Excel.</li>
 * </ul>
 * The long format is used by default.
 */
public class ScenarioResultsWriter {

  // TODO refactor to give access to List<List<String>> of the results

  // TODO make this configurable? use a CSV writing library?
  /* package */ static final String DELIMITER = "\t";

  private ScenarioResultsWriter() {
  }

  /**
   * Writes a set of scenario results in long tab delimited format.
   * There is a separate row for every calculated value. i.e. If there are n values calculated in the view there
   * will be n rows per position / scenario. This is intended to be easy to use when creating pivot tables and
   * using filtering in Excel.
   * @param allScenarioResults The results
   * @param fileName The file to write to
   * @throws IOException If the results can't be written
   */
  public static void writeLongFormat(List<ScenarioResultModel> allScenarioResults, String fileName) throws IOException {
    try (Writer writer = new BufferedWriter(new FileWriter(fileName))) {
      writeLongFormat(allScenarioResults, writer);
    }
  }

  /**
   * Writes a set of scenario results in long tab delimited format.
   * There is a separate row for every calculated value. i.e. If there are n values calculated in the view there
   * will be n rows per position / scenario. This is intended to be easy to use when creating pivot tables and
   * using filtering in Excel.
   * @param allScenarioResults The results
   * @param appendable For writing the results
   * @throws IOException If the results can't be written
   */
  public static void writeLongFormat(List<ScenarioResultModel> allScenarioResults, Appendable appendable) throws IOException {
    if (allScenarioResults.isEmpty()) {
      appendable.append("NO RESULTS");
      return;
    }
    ScenarioResultModel firstResults = allScenarioResults.get(0);
    ImmutableList.Builder<List<String>> rows = ImmutableList.builder();

    // write the header ----------------------------------------

    // this assumes all scenarios have the same number of parameters which should always be true
    ImmutableList.Builder<String> headerRow = ImmutableList.builder();
    headerRow.addAll(metadataHeader(firstResults.getParameters().size()));
    // there is one column of calculated data for all results and another column to say what the results are
    headerRow.add("ResultName").add("ResultValue");
    rows.add(headerRow.build());

    // write the row results ----------------------------------------

    for (ScenarioResultModel scenarioResults : allScenarioResults) {
      SimpleResultModel simpleResults = scenarioResults.getResults();
      Table<Integer, Integer, Object> resultsTable = simpleResults.getResults();

      for (Map.Entry<Integer, Map<Integer, Object>> entry : resultsTable.rowMap().entrySet()) {
        int rowIndex = entry.getKey();
        Map<Integer, Object> rowValues = entry.getValue();
        Iterator<String> colItr = simpleResults.getColumnNames().iterator();

        // loop over every column in the results and add a row
        for (Object value : rowValues.values()) {
          UniqueIdentifiable target = simpleResults.getTargets().get(rowIndex);

          // we're only interested in positions. nodes contain aggregate values and the whole point of this report
          // is to allow arbitrary reaggregation. so any aggregate values would have to be filtered out to avoid
          // confusing the results. and trade amounts are included in positions anyway
          if (target instanceof Position || target instanceof ManageablePosition) {
            ImmutableList.Builder<String> row = ImmutableList.builder();
            row.addAll(metadataColumns(scenarioResults, target));
            row.add(colItr.next()).add(getStringValue((ComputedValueResult) value));
            rows.add(row.build());
          }
        }
      }
    }
    // TODO use a CSV writer?
    for (List<String> row : rows.build()) {
      appendable.append(StringUtils.join(row, DELIMITER)).append("\n");
    }
  }

  /**
   * Writes a set of scenario results in short tab delimited format.
   * All values calculated for a position / scenario are in the same row. There is a column for each result in
   * the view.
   * @param allScenarioResults The results
   * @param fileName The file to write to
   * @throws IOException If the file can't be written
   */
  public static void writeShortFormat(List<ScenarioResultModel> allScenarioResults, String fileName) throws IOException {
    try (Writer writer = new BufferedWriter(new FileWriter(fileName))) {
      writeShortFormat(allScenarioResults, writer);
    }
  }

  /**
   * Writes a set of scenario results in short tab delimited format.
   * All values calculated for a position / scenario are in the same row. There is a column for each result in
   * the view.
   * @param allScenarioResults The results
   * @param appendable For writing the results
   * @throws IOException If the results can't be written
   */
  public static void writeShortFormat(List<ScenarioResultModel> allScenarioResults, Appendable appendable) throws IOException {
    if (allScenarioResults.isEmpty()) {
      appendable.append("NO RESULTS");
      return;
    }
    ScenarioResultModel firstResults = allScenarioResults.get(0);
    ImmutableList.Builder<List<String>> rows = ImmutableList.builder();

    // write the header ----------------------------------------

    // this assumes all scenarios have the same number of parameters which should always be true
    ImmutableList.Builder<String> headerRow = ImmutableList.builder();
    headerRow.addAll(metadataHeader(firstResults.getParameters().size()));
    List<String> columnNames = firstResults.getResults().getColumnNames();

    // there is one column of calculated data for each column in the results table
    for (String columnName : columnNames) {
      headerRow.add(columnName);
    }
    rows.add(headerRow.build());

    // write the row results ----------------------------------------

    for (ScenarioResultModel scenarioResults : allScenarioResults) {
      SimpleResultModel simpleResults = scenarioResults.getResults();
      Table<Integer, Integer, Object> resultsTable = simpleResults.getResults();

      for (Map.Entry<Integer, Map<Integer, Object>> entry : resultsTable.rowMap().entrySet()) {
        int rowIndex = entry.getKey();
        UniqueIdentifiable target = simpleResults.getTargets().get(rowIndex);

        // we're only interested in positions. nodes contain aggregate values and the whole point of this report
        // is to allow arbitrary reaggregation. so any aggregate values would have to be filtered out to avoid
        // confusing the results. and trade amounts are included in positions anyway
        if (target instanceof Position || target instanceof ManageablePosition) {
          ImmutableList.Builder<String> row = ImmutableList.builder();
          Map<Integer, Object> rowValues = entry.getValue();
          row.addAll(metadataColumns(scenarioResults, target));

          for (Object value : rowValues.values()) {
            row.add(getStringValue((ComputedValueResult) value));
          }
          rows.add(row.build());
        }
      }
    }
    // TODO use a CSV writer?
    for (List<String> row : rows.build()) {
      appendable.append(StringUtils.join(row, DELIMITER)).append("\n");
    }
  }

  /**
   * Returns {@link ComputedValueResult#getValue() computedValueResult.getValue()} as a string or the empty string
   * if {@code computedValueResult} or its value are null
   * @param computedValueResult A computed value
   * @return {@code computedValueResult.getValue().toString()} or the empty string if {@code computedValueResult}
   * or its value are null
   */
  private static String getStringValue(ComputedValueResult computedValueResult) {
    if (computedValueResult == null) {
      return "";
    }
    Object value = computedValueResult.getValue();

    if (value == null) {
      return "";
    }
    return value.toString();
  }

  private static List<String> metadataHeader(int paramCount) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    builder.add("ScenarioName").add("ValuationTime").add("Type").add("Description").add("PositionId");
    for (int i = 1; i <= paramCount; i++) {
      builder.add("ParamName" + i).add("ParamValue" + i);
    }
    return builder.build();
  }

  /**
   * @return Formats the data in the specified row up to but not including the calculated results
   */
  private static List<String> metadataColumns(ScenarioResultModel scenarioResults, UniqueIdentifiable target) {
    String scenarioName = scenarioResults.getScenarioName();
    SimpleResultModel simpleResults = scenarioResults.getResults();
    Instant valuationTime = simpleResults.getValuationTime();
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    builder
        .add(scenarioName)
        .add(valuationTime.toString())
        .add(getType(target))
        .add(getDescription(target))
        .add(target.getUniqueId().toString());

    for (Map.Entry<String, Object> entry : scenarioResults.getParameters().entrySet()) {
      String paramName = entry.getKey();
      Object paramValue = entry.getValue();

      builder.add(paramName).add(paramValue.toString());
    }
    return builder.build();
  }

  private static String getType(UniqueIdentifiable target) {
    if (target instanceof PortfolioNode || target instanceof ManageablePortfolioNode) {
      return "PortfolioNode";
    }
    Security security = getSecurity(target);

    if (security == null) {
      return target.getClass().getSimpleName();
    }
    String simpleName = security.getClass().getSimpleName();

    if (simpleName.endsWith("Security")) {
      return simpleName.substring(0, simpleName.length() - 8);
    }
    return simpleName;
  }

  private static String getDescription(UniqueIdentifiable target) {
    if (target instanceof PortfolioNode) {
      return ((PortfolioNode) target).getName();
    }
    if (target instanceof ManageablePortfolioNode) {
      return ((ManageablePortfolioNode) target).getName();
    }
    Security security = getSecurity(target);

    if (security != null && !StringUtils.isEmpty(security.getName())) {
      return security.getName();
    }
    if (target instanceof ManageablePosition) {
      return ((ManageablePosition) target).getName();
    }
    return "";
  }

  private static Security getSecurity(Object positionOrTrade) {
    if (positionOrTrade instanceof ManageablePosition) {
      return ((ManageablePosition) positionOrTrade).getSecurity();
    } else if (positionOrTrade instanceof SimplePosition) {
      return ((SimplePosition) positionOrTrade).getSecurity();
    } else if (positionOrTrade instanceof ManageableTrade) {
      return ((ManageableTrade) positionOrTrade).getSecurity();
    } else if (positionOrTrade instanceof SimpleTrade) {
      return ((SimpleTrade) positionOrTrade).getSecurity();
    } else {
      return null;
    }
  }
}
