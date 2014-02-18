/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
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

  private final String _scenario1Name = "scenario1Name";
  private final String _scenario2Name = "scenario2Name";
  private final String _valuationTime1 = "2014-02-17T12:00Z";
  private final String _valuationTime2 = "2013-02-17T12:00Z";
  private final String _id1 = "Tst~123";
  private final String _id2 = "Tst~234";
  private final String _param1Value1 = "param1Value1";
  private final String _param1Value2 = "param1Value2";
  private final String _param2Value1 = "param2Value1";
  private final String _param2Value2 = "param2Value2";
  private final String _res1Name = "Result1";
  private final String _res2Name = "Result2";
  private final String _param1Name = "param1Name";
  private final String _param2Name = "param2Name";

  @Test
  public void shortFormat() throws IOException {
    List<String> expectedList =
        ImmutableList.of(
            // header -----
            row("ScenarioName", "ValuationTime", "TargetId", "ParamName1", "ParamValue1", "ParamName2", "ParamValue2", _res1Name, _res2Name),
            // scenario 1 trade 1 -----
            row(_scenario1Name, _valuationTime1, _id1, _param1Name, _param1Value1, _param2Name, _param2Value1, 1, 2),
            // scenario 1 trade 2 -----
            row(_scenario1Name, _valuationTime1, _id2, _param1Name, _param1Value1, _param2Name, _param2Value1, 3, 4),
            // scenario 2 trade 1 -----
            row(_scenario2Name, _valuationTime2, _id1, _param1Name, _param1Value2, _param2Name, _param2Value2, 5, 6),
            // scenario 2 trade 2 -----
            row(_scenario2Name, _valuationTime2, _id2, _param1Name, _param1Value2, _param2Name, _param2Value2, 7, 8));
    String expected = StringUtils.join(expectedList, "\n") + "\n";

    StringBuilder builder = new StringBuilder();
    ScenarioResultsWriter.writeShortFormat(scenarioResults(), builder);
    assertEquals(expected, builder.toString());
  }

  @Test
  public void longFormat() throws IOException {
    List<String> expectedList =
        ImmutableList.of(
            // header -----
            row("ScenarioName", "ValuationTime", "TargetId", "ParamName1", "ParamValue1", "ParamName2", "ParamValue2", "ResultName", "ResultValue"),
            // scenario 1 trade 1 result 1-----
            row(_scenario1Name, _valuationTime1, _id1, _param1Name, _param1Value1, _param2Name, _param2Value1, _res1Name, 1),
            // scenario 1 trade 1 result 2 -----
            row(_scenario1Name, _valuationTime1, _id1, _param1Name, _param1Value1, _param2Name, _param2Value1, _res2Name, 2),
            // scenario 1 trade 2 result 1 -----
            row(_scenario1Name, _valuationTime1, _id2, _param1Name, _param1Value1, _param2Name, _param2Value1, _res1Name, 3),
            // scenario 1 trade 2 result 2 -----
            row(_scenario1Name, _valuationTime1, _id2, _param1Name, _param1Value1, _param2Name, _param2Value1, _res2Name, 4),
            // scenario 2 trade 1 result 1 -----
            row(_scenario2Name, _valuationTime2, _id1, _param1Name, _param1Value2, _param2Name, _param2Value2, _res1Name, 5),
            // scenario 2 trade 1 result 2-----
            row(_scenario2Name, _valuationTime2, _id1, _param1Name, _param1Value2, _param2Name, _param2Value2, _res2Name, 6),
            // scenario 2 trade 2 result 1 -----
            row(_scenario2Name, _valuationTime2, _id2, _param1Name, _param1Value2, _param2Name, _param2Value2, _res1Name, 7),
            // scenario 2 trade 2 result 2 -----
            row(_scenario2Name, _valuationTime2, _id2, _param1Name, _param1Value2, _param2Name, _param2Value2, _res2Name, 8));
    String expected = StringUtils.join(expectedList, "\n") + "\n";

    StringBuilder builder = new StringBuilder();
    ScenarioResultsWriter.writeLongFormat(scenarioResults(), builder);
    assertEquals(expected, builder.toString());
  }

  private List<ScenarioResultModel> scenarioResults() {
    List<UniqueIdentifiable> ids = ImmutableList.<UniqueIdentifiable>of(UniqueId.parse(_id1), UniqueId.parse(_id2));
    List<String> columnNames = ImmutableList.of(_res1Name, _res2Name);

    Table<Integer, Integer, Object> table1 = TreeBasedTable.create();
    table1.put(0, 0, compuatedValue(1));
    table1.put(0, 1, compuatedValue(2));
    table1.put(1, 0, compuatedValue(3));
    table1.put(1, 1, compuatedValue(4));
    ViewCycleExecutionOptions executionOptions1 =
        ViewCycleExecutionOptions.builder()
            .setValuationTime(Instant.parse(_valuationTime1))
            .setName(_scenario1Name)
            .create();
    SimpleResultModel simpleResultModel1 = new SimpleResultModel(ids, columnNames, table1, executionOptions1);
    Map<String, Object> scenarioParams1 = ImmutableMap.<String, Object>of(_param1Name, _param1Value1,
                                                                          _param2Name, _param2Value1);
    ScenarioResultModel scenarioResultModel1 = new ScenarioResultModel(simpleResultModel1, scenarioParams1);

    Table<Integer, Integer, Object> table2 = TreeBasedTable.create();
    table2.put(0, 0, compuatedValue(5));
    table2.put(0, 1, compuatedValue(6));
    table2.put(1, 0, compuatedValue(7));
    table2.put(1, 1, compuatedValue(8));
    ViewCycleExecutionOptions executionOptions2 =
        ViewCycleExecutionOptions.builder()
            .setValuationTime(Instant.parse(_valuationTime2))
            .setName(_scenario2Name)
            .create();
    SimpleResultModel simpleResultModel2 = new SimpleResultModel(ids, columnNames, table2, executionOptions2);
    Map<String, Object> scenarioParams2 = ImmutableMap.<String, Object>of(_param1Name, _param1Value2,
                                                                          _param2Name, _param2Value2);
    ScenarioResultModel scenarioResultModel2 = new ScenarioResultModel(simpleResultModel2, scenarioParams2);

    return ImmutableList.of(scenarioResultModel1, scenarioResultModel2);
  }

  private static Object compuatedValue(int value) {
    return new ComputedValueResult(VALUE_SPEC, value, EmptyAggregatedExecutionLog.INSTANCE);
  }

  private static String row(Object... values) {
    return StringUtils.join(values, ScenarioResultsWriter.DELIMITER);
  }
}
