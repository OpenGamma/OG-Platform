/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/**
 * Tests that {@link AnalyticsNodeJsonWriter} creates the expected JSON.
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticsNodeJsonWriterTest {

  @Test
  public void emptyPortfolio() throws JSONException {
    String json = getJson(null);
    assertTrue(JsonTestUtils.equal(new JSONArray("[]"), new JSONArray(json)));
  }

  @Test
  public void flatPortfolio() throws JSONException {
    /*
    0 root
    1  |_pos
    2  |_pos
    3  |_pos
    */
    AnalyticsNode root = new AnalyticsNode(0, 3, Collections.<AnalyticsNode>emptyList(), false);
    String json = getJson(root);
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
    AnalyticsNode child1 = new AnalyticsNode(1, 2, Collections.<AnalyticsNode>emptyList(), false);
    AnalyticsNode child2 = new AnalyticsNode(3, 4, Collections.<AnalyticsNode>emptyList(), false);
    AnalyticsNode root = new AnalyticsNode(0, 5, ImmutableList.of(child1, child2), false);
    String json = getJson(root);
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
    AnalyticsNode child2 = new AnalyticsNode(2, 4, Collections.<AnalyticsNode>emptyList(), false);
    AnalyticsNode child1 = new AnalyticsNode(1, 4, ImmutableList.of(child2), false);
    AnalyticsNode root = new AnalyticsNode(0, 4, ImmutableList.of(child1), false);
    String json = getJson(root);
    assertTrue(JsonTestUtils.equal(new JSONArray("[0,4,[[1,4,[[2,4,[]]]]]]"), new JSONArray(json)));
  }

  private static String getJson(AnalyticsNode node) {
    try {
      return new JSONArray(AnalyticsNodeJsonWriter.getJsonStructure(node)).toString();
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON for node " + node, e);
    }
  }
}
