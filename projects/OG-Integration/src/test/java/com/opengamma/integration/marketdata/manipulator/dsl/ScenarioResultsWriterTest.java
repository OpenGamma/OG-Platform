/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.EmptyAggregatedExecutionLog;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ScenarioResultsWriterTest {

  private static final ValueSpecification VALUE_SPEC =
      new ValueSpecification("foo",
                             ComputationTargetSpecification.NULL,
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "bar").get());

  @Test
  public void shortFormat() throws IOException {
    String scenario1Name = "scenario1Name";
    String scenario2Name = "scenario2Name";
    String valuationTime1 = "2014-02-17T12:00Z";
    String valuationTime2 = "2013-02-17T12:00Z";
    String id1 = "Tst~123";
    String id2 = "Tst~234";
    String param1Value1 = "param1Value1";
    String param1Value2 = "param1Value2";
    String param2Value1 = "param2Value1";
    String param2Value2 = "param2Value2";
    String col1Name = "Col1";
    String col2Name = "Col2";
    String param1Name = "param1Name";
    String param2Name = "param2Name";
    List<String> expectedList =
        ImmutableList.of(
            // header row -----
            row("ScenarioName", "ValuationTime", "TargetId", "ParamName1", "ParamValue1", "ParamName2", "ParamValue2", col1Name, col2Name),
            // scenario 1 trade 1 -----
            row(scenario1Name, valuationTime1, id1, param1Name, param1Value1, param2Name, param2Value1, value(0, 0), value(0, 1)),
            // scenario 1 trade 2 -----
            row(scenario1Name, valuationTime1, id2, param1Name, param1Value1, param2Name, param2Value1, value(1, 0), value(1, 1)),
            // scenario 2 trade 1 -----
            row(scenario2Name, valuationTime2, id1, param1Name, param1Value2, param2Name, param2Value2, value(2, 0), value(2, 1)),
            // scenario 2 trade 2 -----
            row(scenario2Name, valuationTime2, id2, param1Name, param1Value2, param2Name, param2Value2, value(3, 0), value(3, 1)));
    String expected = StringUtils.join(expectedList, "\n") + "\n";

    List<UniqueIdentifiable> ids = ImmutableList.<UniqueIdentifiable>of(UniqueId.parse(id1), UniqueId.parse(id2));
    List<String> columnNames = ImmutableList.of(col1Name, col2Name);

    Table<Integer, Integer, Object> table1 = TreeBasedTable.create();
    table1.put(0, 0, compuatedValue(0, 0));
    table1.put(0, 1, compuatedValue(0, 1));
    table1.put(1, 0, compuatedValue(1, 0));
    table1.put(1, 1, compuatedValue(1, 1));
    ViewCycleExecutionOptions executionOptions1 =
        ViewCycleExecutionOptions.builder()
            .setValuationTime(Instant.parse(valuationTime1))
            .setName(scenario1Name)
            .create();
    SimpleResultModel simpleResultModel1 = new SimpleResultModel(ids, columnNames, table1, executionOptions1);
    Map<String, Object> scenarioParams1 = ImmutableMap.<String, Object>of(param1Name, param1Value1, param2Name, param2Value1);
    ScenarioResultModel scenarioResultModel1 = new ScenarioResultModel(simpleResultModel1, scenarioParams1);

    Table<Integer, Integer, Object> table2 = TreeBasedTable.create();
    table2.put(0, 0, compuatedValue(2, 0));
    table2.put(0, 1, compuatedValue(2, 1));
    table2.put(1, 0, compuatedValue(3, 0));
    table2.put(1, 1, compuatedValue(3, 1));
    ViewCycleExecutionOptions executionOptions2 =
        ViewCycleExecutionOptions.builder()
            .setValuationTime(Instant.parse(valuationTime2))
            .setName(scenario2Name)
            .create();
    SimpleResultModel simpleResultModel2 = new SimpleResultModel(ids, columnNames, table2, executionOptions2);
    Map<String, Object> scenarioParams2 = ImmutableMap.<String, Object>of(param1Name, param1Value2, param2Name, param2Value2);
    ScenarioResultModel scenarioResultModel2 = new ScenarioResultModel(simpleResultModel2, scenarioParams2);

    StringBuilder builder = new StringBuilder();
    ScenarioResultsWriter.writeShortFormat(ImmutableList.of(scenarioResultModel1, scenarioResultModel2), builder);

    assertEquals(expected, builder.toString());
  }

  private static String value(int row, int col) {
    return "cellValue" + row + col;
  }

  private static Object compuatedValue(int row, int col) {
    return new ComputedValueResult(VALUE_SPEC, value(row, col), EmptyAggregatedExecutionLog.INSTANCE);
  }

  private static String row(String... values) {
    return StringUtils.join(values, ScenarioResultsWriter.DELIMITER);
  }

  @Test
  public void longFormat() {


  }
}
