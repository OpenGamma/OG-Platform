/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.MissingOutput;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.analytics.formatting.ResultsFormatter;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ViewportResultsJsonCsvWriterTest {

  private static final Duration DURATION = Duration.ofMillis(1234);
  
  private static final Instant CALC_TIME = Instant.now();

  private final ViewportDefinition _viewportDefinition = ViewportDefinition.create(0,
                                                                                   ImmutableList.of(0),
                                                                                   ImmutableList.of(0),
                                                                                   ImmutableList.<GridCell>of(),
                                                                                   TypeFormatter.Format.CELL,
                                                                                   false);
  private final ValueRequirement _valueReq =
      new ValueRequirement("valueName", ComputationTargetType.POSITION, UniqueId.of("foo", "bar"));
  private final ComputationTargetSpecification _target =
      new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("foo", "bar"));
  private final ValueSpecification _valueSpec =
      new ValueSpecification(_valueReq.getValueName(), _target,
                             ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "fnName").get());
  private final ViewportResultsJsonCsvWriter _writer = new ViewportResultsJsonCsvWriter(new ResultsFormatter());

  private static GridColumnGroups createColumns(Class<?> type) {
    GridColumn.CellRenderer renderer = new TestCellRenderer();
    GridColumn column = new GridColumn("header", "desc", type, renderer);
    return new GridColumnGroups(new GridColumnGroup("grp", ImmutableList.of(column), false));
  }

  private List<ResultsCell> createResults(Object value, List<Object> history, Class<?> columnType) {
    return ImmutableList.of(ResultsCell.forCalculatedValue(value, _valueSpec, history, null, false, columnType, TypeFormatter.Format.CELL));
  }

  @Test
  public void valueWithNoHistory() throws JSONException {
    List<ResultsCell> results = createResults("val", null, String.class);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(String.class), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"val\"}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void valueWithHistory() throws JSONException {
    List<ResultsCell> results = createResults(3d, ImmutableList.<Object>of(1d, 2d, 3d), Double.class);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(Double.class), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"h\":[1,2,3]}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void valueWithUnknownType() throws JSONException {
    List<ResultsCell> results = createResults(3d, null, null);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(null), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"t\":\"DOUBLE\"}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void nullValueWithUnknownType() throws JSONException {
    List<ResultsCell> results = createResults(null, null, null);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(null), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"\",\"t\":\"STRING\"}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void valueWithUnknownTypeAndHistory() throws JSONException {
    List<ResultsCell> results = createResults(3d, ImmutableList.<Object>of(1d, 2d, 3d), null);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(null), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"t\":\"DOUBLE\",\"h\":[1,2,3]}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void errorValueNoHistory() throws JSONException {
    List<ResultsCell> results = createResults(MissingOutput.EVALUATION_ERROR, null, String.class);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(String.class), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"Evaluation error\", \"error\":true}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void errorValueWithHistory() throws JSONException {
    ImmutableList<Object> history = ImmutableList.<Object>of(1d, 2d, MissingOutput.EVALUATION_ERROR);
    List<ResultsCell> results = createResults(MissingOutput.EVALUATION_ERROR, history, Double.class);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(Double.class), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"Evaluation error\", \"h\":[1,2,null], \"error\":true}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void errorValueInHistory() throws JSONException {
    ImmutableList<Object> history = ImmutableList.<Object>of(1d, MissingOutput.EVALUATION_ERROR, 3d);
    List<ResultsCell> results = createResults(3d, history, Double.class);
    ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(Double.class), DURATION, CALC_TIME);
    String json = _writer.getJson(viewportResults);
    String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"h\":[1,null,3]}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  private static class TestCellRenderer implements GridColumn.CellRenderer {

    @Override
    public ResultsCell getResults(int rowIndex,
                                  TypeFormatter.Format format,
                                  ResultsCache cache,
                                  Class<?> columnType,
                                  Object inlineKey) {
      return null;
    }
  }

  // TODO tests for log output
}
