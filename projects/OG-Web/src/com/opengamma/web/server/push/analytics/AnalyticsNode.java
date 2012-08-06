/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Minimal representation of a portfolio node in an analytics grid. Contains a list of sub-nodes and the grid row
 * indices at which a node starts and ends.
 */
public class AnalyticsNode {

  private final int _startRow;
  private final int _endRow;
  private final List<AnalyticsNode> children;

  /* package */ AnalyticsNode(int startRow, int endRow, List<AnalyticsNode> children) {
    ArgumentChecker.notNull(children, "children");
    _startRow = startRow;
    _endRow = endRow;
    this.children = children;
  }

  /**
   * @return An empty root node that starts and ends at row zero and has no children.
   */
  public static AnalyticsNode emptyRoot() {
    return new AnalyticsNode(0, 0, Collections.<AnalyticsNode>emptyList());
  }

  public static AnalyticsNode portoflioRoot(CompiledViewDefinition compiledViewDef) {
    Portfolio portfolio = compiledViewDef.getPortfolio();
    PortfolioNode root = portfolio.getRootNode();
    return new PortfolioNodeBuilder(root).getRoot();
  }

  public static AnalyticsNode primitivesRoot(int primitivesTargetCount) {
    return new AnalyticsNode(0, primitivesTargetCount - 1, Collections.<AnalyticsNode>emptyList());
  }

  /**
   * @return The row index (zero-based and inclusive) at which the node starts.
   */
  public int getStartRow() {
    return _startRow;
  }

  /**
   * @return The row index (zero-based and inclusive) at which the node ends. This includes all nested child nodes.
   * i.e. the end row of a node is the same as the end row of its most deeply nested child.
   */
  public int getEndRow() {
    return _endRow;
  }

  /**
   * @return The direct children of this node.
   */
  public List<AnalyticsNode> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    return "AnalyticsNode [_startRow=" + _startRow + ", _endRow=" + _endRow + ", children=" + children + "]";
  }

  /* package */ static class PortfolioNodeBuilder {

    private final AnalyticsNode _root;

    private int _lastRow = 0;

    /* package */ PortfolioNodeBuilder(PortfolioNode root) {
      _root = createNode(root);
    }

    private AnalyticsNode createNode(PortfolioNode node) {
      int nodeStart = _lastRow;
      List<AnalyticsNode> nodes = new ArrayList<AnalyticsNode>();
      for (PortfolioNode child : node.getChildNodes()) {
        ++_lastRow;
        nodes.add(createNode(child));
      }
      _lastRow += node.getPositions().size();
      return new AnalyticsNode(nodeStart, _lastRow, Collections.unmodifiableList(nodes));
    }

    public AnalyticsNode getRoot() {
      return _root;
    }
  }
}
