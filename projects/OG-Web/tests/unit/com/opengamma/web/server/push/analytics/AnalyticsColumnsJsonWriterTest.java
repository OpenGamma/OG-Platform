/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.testng.AssertJUnit.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.web.server.push.analytics.formatting.ResultsFormatter;

/**
 *
 */
public class AnalyticsColumnsJsonWriterTest {

  @Test
  public void getJson() throws JSONException {
    AnalyticsColumn col1 = new AnalyticsColumn("col1", "col1 desc", Double.class);
    AnalyticsColumn col2 = new AnalyticsColumn("col2", "col2 desc", String.class);
    AnalyticsColumn col3 = new AnalyticsColumn("col3", "col3 desc", Double.class);
    AnalyticsColumn col4 = new AnalyticsColumn("col4", "col4 desc", String.class);
    AnalyticsColumnGroup group1 = new AnalyticsColumnGroup("group1", ImmutableList.of(col1, col2));
    AnalyticsColumnGroup group2 = new AnalyticsColumnGroup("group2", ImmutableList.of(col3, col4));
    ImmutableList<AnalyticsColumnGroup> groups = ImmutableList.of(group1, group2);
    AnalyticsColumnsJsonWriter writer = new AnalyticsColumnsJsonWriter(new ResultsFormatter());
    String json = writer.getJson(groups);
    String expectedJson =
        "[{\"name\":\"group1\",\"columns\":[" +
            "{\"header\":\"col1\",\"description\":\"col1 desc\",\"type\":\"DOUBLE\"}," +
            "{\"header\":\"col2\",\"description\":\"col2 desc\",\"type\":\"PRIMITIVE\"}]}," +
        "{\"name\":\"group2\",\"columns\":[" +
            "{\"header\":\"col3\",\"description\":\"col3 desc\",\"type\":\"DOUBLE\"}," +
            "{\"header\":\"col4\",\"description\":\"col4 desc\",\"type\":\"PRIMITIVE\"}]}]";
    assertTrue(JsonTestUtils.equal(new JSONArray(expectedJson), new JSONArray(json)));
  }
}
