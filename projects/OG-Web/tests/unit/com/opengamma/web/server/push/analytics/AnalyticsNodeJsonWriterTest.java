/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests that {@link AnalyticsNodeJsonWriter} creates the expected JSON.
 */
public class AnalyticsNodeJsonWriterTest {

  @Test
  public void emptyPortfolio() {
    String json = AnalyticsNodeJsonWriter.getJson(AnalyticsNode.empty());
    assertEquals("[0,0,[]]", json);
  }

  @Test
  public void flatPortfolio() {
    /*
    0 root
    1  |_pos
    2  |_pos
    3  |_pos
    */
    AnalyticsNode root = new AnalyticsNode(0, 3, Collections.<AnalyticsNode>emptyList());
    String json = AnalyticsNodeJsonWriter.getJson(root);
    assertEquals("[0,3,[]]", json);
  }

  @Test
  public void portfolioWithSubNodes() {
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
    assertEquals("[0,5,[[1,2,[]],[3,4,[]]]]", json);
  }

  @Test
  public void nestedPortfolio() {
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
    assertEquals("[0,4,[[1,4,[[2,4,[]]]]]]", json);
  }
}
