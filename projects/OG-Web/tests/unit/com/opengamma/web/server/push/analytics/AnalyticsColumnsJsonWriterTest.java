/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class AnalyticsColumnsJsonWriterTest {

  @Test
  public void getJson() {
    AnalyticsColumn col1 = new AnalyticsColumn("col1", "col1 desc");
    AnalyticsColumn col2 = new AnalyticsColumn("col2", "col2 desc");
    AnalyticsColumn col3 = new AnalyticsColumn("col3", "col3 desc");
    AnalyticsColumn col4 = new AnalyticsColumn("col4", "col4 desc");
    AnalyticsColumnGroup group1 = new AnalyticsColumnGroup("group1", ImmutableList.of(col1, col2));
    AnalyticsColumnGroup group2 = new AnalyticsColumnGroup("group2", ImmutableList.of(col3, col4));
    ImmutableList<AnalyticsColumnGroup> groups = ImmutableList.of(group1, group2);
    String json = AnalyticsColumnsJsonWriter.getJson(groups);
    String expectedJson =
        "[{\"name\":\"group1\",\"columns\":[" +
            "{\"description\":\"col1 desc\",\"header\":\"col1\"}," +
            "{\"description\":\"col2 desc\",\"header\":\"col2\"}]}," +
        "{\"name\":\"group2\",\"columns\":[" +
            "{\"description\":\"col3 desc\",\"header\":\"col3\"}," +
            "{\"description\":\"col4 desc\",\"header\":\"col4\"}]}]";
    assertEquals(expectedJson, json);
  }
}
