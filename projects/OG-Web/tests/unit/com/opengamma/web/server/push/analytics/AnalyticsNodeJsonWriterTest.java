/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests that {@link AnalyticsNodeJsonWriter} creates the expected JSON.
 */
public class AnalyticsNodeJsonWriterTest {

  @Test
  public void emptyPortfolio() throws JSONException {
    String json = AnalyticsNodeJsonWriter.getJson(AnalyticsNode.emptyRoot());
    assertTrue(JsonTestUtils.equal(new JSONArray("[0,0,[]]"), new JSONArray(json)));
  }

  @Test
  public void flatPortfolio() throws JSONException {
    /*
    0 root
    1  |_pos
    2  |_pos
    3  |_pos
    */
    AnalyticsNode root = new AnalyticsNode(0, 3, Collections.<AnalyticsNode>emptyList());
    String json = AnalyticsNodeJsonWriter.getJson(root);
    assertTrue(JsonTestUtils.equal(new JSONArray("[0,3,[]]"), new JSONArray(json)));
  }

  @Test
  public void portfolioWithSubNodes() throws JSONException {
    /*
    0 root
    1  |_child1
    2  |  |_pos
    3  |_child2
    4  |  |_pos
    4  |_pos
    */
    AnalyticsNode child1 = new AnalyticsNode(1, 2, Collections.<AnalyticsNode>emptyList());
    AnalyticsNode child2 = new AnalyticsNode(3, 4, Collections.<AnalyticsNode>emptyList());
    AnalyticsNode root = new AnalyticsNode(0, 5, ImmutableList.of(child1, child2));
    String json = AnalyticsNodeJsonWriter.getJson(root);
    assertTrue(JsonTestUtils.equal(new JSONArray("[0,5,[[1,2,[]],[3,4,[]]]]"), new JSONArray(json)));
  }

  @Test
  public void nestedPortfolio() throws JSONException {
    /*
    0 root
    1  |_child1
    2     |_child2
    3        |_pos
    4        |_pos
    */
    AnalyticsNode child2 = new AnalyticsNode(2, 4, Collections.<AnalyticsNode>emptyList());
    AnalyticsNode child1 = new AnalyticsNode(1, 4, ImmutableList.of(child2));
    AnalyticsNode root = new AnalyticsNode(0, 4, ImmutableList.of(child1));
    String json = AnalyticsNodeJsonWriter.getJson(root);
    assertTrue(JsonTestUtils.equal(new JSONArray("[0,4,[[1,4,[[2,4,[]]]]]]"), new JSONArray(json)));
  }
}
