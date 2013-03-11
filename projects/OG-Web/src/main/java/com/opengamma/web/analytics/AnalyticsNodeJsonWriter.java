/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

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

  /**
   * @param node The node
   * @return Nested JSON array of the node structure
   */
  public static String getJson(AnalyticsNode node) {
    Object[] rootArray = createNodeArray(node);
    try {
      return new JSONArray(rootArray).toString();
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON for node " + node, e);
    }
  }

  /**
   * Creates an array containing the contents of {@code node}. Recursively creates arrays for child nodes.
   * {@code isFungiblePosition} is optional, it has a value of 1 for fungible positions and is omitted for all
   * other node types.
   * <pre>
   *   [startRow,endRow,[childNode1,childNode2,...],isFungiblePosition]
   * </pre>
   * @param node The grid node
   * @return <pre>[startRow,endRow,[childNode1,childNode2,...],isFungiblePosition]</pre>
   */
  private static Object[] createNodeArray(AnalyticsNode node) {
    if (node == null) {
      return new Object[0];
    }
    Object[] nodeArray;
    if (node.isFungiblePosition()) {
      nodeArray = new Object[4];
      nodeArray[3] = 1;
    } else {
      nodeArray = new Object[3];
    }
    nodeArray[0] = node.getStartRow();
    nodeArray[1] = node.getEndRow();

    List<AnalyticsNode> children = node.getChildren();
    Object[] childArray = new Object[children.size()];
    for (int i = 0; i < childArray.length; i++) {
      childArray[i] = createNodeArray(children.get(i));
    }
    nodeArray[2] = childArray;
    return nodeArray;
  }
}
