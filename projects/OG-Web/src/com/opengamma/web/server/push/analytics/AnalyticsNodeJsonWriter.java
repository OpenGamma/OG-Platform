/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Writes an {@link AnalyticsNode} into very compact JSON. The nodes are represented as nested arrays. There is
 * always a single root node.
 * <pre>
 *   [startRow,endRow,[childNode1,childNode2,...]]
 * </pre>
 */
public class AnalyticsNodeJsonWriter {

  public static String getJson(AnalyticsNode root) {
    Object[] rootArray = createNodeArray(root);
    try {
      return new JSONArray(rootArray).toString();
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON for node " + root, e);
    }
  }

  private static Object[] createNodeArray(AnalyticsNode node) {
    Object[] nodeArray = new Object[3];
    nodeArray[0] = node.getStart();
    nodeArray[1] = node.getEnd();

    List<AnalyticsNode> children = node.getChildren();
    Object[] childArray = new Object[children.size()];
    for (int i = 0; i < childArray.length; i++) {
      childArray[i] = createNodeArray(children.get(i));
    }
    nodeArray[2] = childArray;
    return nodeArray;
  }
}
