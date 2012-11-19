/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import javax.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;
import com.opengamma.id.UniqueId;
import com.opengamma.web.analytics.formatting.ResultsFormatter;
import com.opengamma.web.analytics.formatting.TypeFormatter;

public class ViewportResultsJsonWriterTest {

  private final ComputationTargetSpecification _target = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("foo", "bar"));
  private final ValueRequirement _valueReq = new ValueRequirement("valueName", _target);
  private final ValueSpecification _valueSpec = new ValueSpecification(_valueReq.getValueName(), _target, ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "fnName").get());
  private final ViewportDefinition _viewportDefinition = ViewportDefinition.create(0, ImmutableList.of(0), ImmutableList.of(0), ImmutableList.<GridCell>of(), TypeFormatter.Format.CELL);
  private static final Duration DURATION = Duration.ofMillis(1234);
  private final ViewportResultsJsonWriter _writer = new ViewportResultsJsonWriter(new ResultsFormatter());

  private static AnalyticsColumnGroups createColumns(final Class<?> type) {
    final AnalyticsColumn column = new AnalyticsColumn("header", "desc", type);
    return new AnalyticsColumnGroups(ImmutableList.of(new AnalyticsColumnGroup("grp", ImmutableList.of(column))));
  }

  private List<ViewportResults.Cell> createResults(final Object value, final List<Object> history) {
    return ImmutableList.of(ViewportResults.valueCell(value, _valueSpec, history, 0));
  }

  @Test
  public void valueWithNoHistory() throws JSONException {
    final List<ViewportResults.Cell> results = createResults("val", null);
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(String.class), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"val\"}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void valueWithHistory() throws JSONException {
    final List<ViewportResults.Cell> results = createResults(3d, ImmutableList.<Object>of(1d, 2d, 3d));
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(Double.class), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"h\":[1,2,3]}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void valueWithUnknownType() throws JSONException {
    final List<ViewportResults.Cell> results = createResults(3d, null);
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(null), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"t\":\"DOUBLE\"}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void nullValueWithUnknownType() throws JSONException {
    final List<ViewportResults.Cell> results = createResults(null, null);
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(null), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"\",\"t\":\"PRIMITIVE\"}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void valueWithUnknownTypeAndHistory() throws JSONException {
    final List<ViewportResults.Cell> results = createResults(3d, ImmutableList.<Object>of(1d, 2d, 3d));
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(null), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"t\":\"DOUBLE\",\"h\":[1,2,3]}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void errorValueNoHistory() throws JSONException {
    final List<ViewportResults.Cell> results = createResults(NotCalculatedSentinel.EVALUATION_ERROR, null);
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(String.class), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"Evaluation error\", \"error\":true}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void errorValueWithHistory() throws JSONException {
    final ImmutableList<Object> history = ImmutableList.<Object>of(1d, 2d, NotCalculatedSentinel.EVALUATION_ERROR);
    final List<ViewportResults.Cell> results = createResults(NotCalculatedSentinel.EVALUATION_ERROR, history);
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(Double.class), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"Evaluation error\", \"h\":[1,2,null], \"error\":true}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }

  @Test
  public void errorValueInHistory() throws JSONException {
    final ImmutableList<Object> history = ImmutableList.<Object>of(1d, NotCalculatedSentinel.EVALUATION_ERROR, 3d);
    final List<ViewportResults.Cell> results = createResults(3d, history);
    final ViewportResults viewportResults = new ViewportResults(results, _viewportDefinition, createColumns(Double.class), DURATION);
    final String json = _writer.getJson(viewportResults);
    final String expectedJson = "{\"version\":0, \"calculationDuration\":\"1,234\", \"data\":[{\"v\":\"3.0\",\"h\":[1,null,3]}]}";
    assertTrue(JsonTestUtils.equal(new JSONObject(expectedJson), new JSONObject(json)));
  }
}
