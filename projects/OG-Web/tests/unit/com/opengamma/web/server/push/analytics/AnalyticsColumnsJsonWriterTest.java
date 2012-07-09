/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 *
 */
public class AnalyticsColumnsJsonWriterTest {

  @Test
  public void getJson() throws JSONException {
    AnalyticsColumn col1 = new AnalyticsColumn("col1", "col1 desc");
    AnalyticsColumn col2 = new AnalyticsColumn("col2", "col2 desc");
    AnalyticsColumn col3 = new AnalyticsColumn("col3", "col3 desc");
    AnalyticsColumn col4 = new AnalyticsColumn("col4", "col4 desc");
    AnalyticsColumnGroup group1 = new AnalyticsColumnGroup("group1", ImmutableList.of(col1, col2));
    AnalyticsColumnGroup group2 = new AnalyticsColumnGroup("group2", ImmutableList.of(col3, col4));
    ImmutableList<AnalyticsColumnGroup> groups = ImmutableList.of(group1, group2);
    AnalyticsColumnsJsonWriter writer = new AnalyticsColumnsJsonWriter(mock(ResultConverterCache.class));
    String json = writer.getJson(groups);
    String expectedJson =
        "[{\"name\":\"group1\",\"columns\":[" +
            "{\"header\":\"col1\",\"description\":\"col1 desc\"}," +
            "{\"header\":\"col2\",\"description\":\"col2 desc\"}]}," +
        "{\"name\":\"group2\",\"columns\":[" +
            "{\"header\":\"col3\",\"description\":\"col3 desc\"}," +
            "{\"header\":\"col4\",\"description\":\"col4 desc\"}]}]";
    assertTrue(JsonTestUtils.equal(new JSONArray(expectedJson), new JSONArray(json)));
  }

  @Test
  public void getJsonWithType() throws JSONException {
    AnalyticsColumn col = new AnalyticsColumn("col1", "col1 desc");
    AnalyticsColumnGroup group = new AnalyticsColumnGroup("group1", Collections.singletonList(col));
    col.setType(Double.class);
    ResultConverterCache converters = new ResultConverterCache(FudgeContext.GLOBAL_DEFAULT);
    AnalyticsColumnsJsonWriter writer = new AnalyticsColumnsJsonWriter(converters);
    String json = writer.getJson(Collections.singletonList(group));
    String expectedJson =
        "[{\"name\":\"group1\",\"columns\":[" +
            "{\"header\":\"col1\",\"description\":\"col1 desc\",\"type\":\"DOUBLE\"}]}]";
    assertTrue(JsonTestUtils.equal(new JSONArray(expectedJson), new JSONArray(json)));
  }
}
