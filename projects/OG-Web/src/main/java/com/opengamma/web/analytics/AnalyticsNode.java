/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Minimal representation of a node in the structure of a portfolio. For use in an analytics grid. Contains a
 * list of sub-nodes and indices of the rows at which a node starts and ends.
 */
/* package */ class AnalyticsNode {

  /** Index of the row containing this node. */
  private final int _startRow;
  /** Index of the row containing this node's last child. */
  private final int _endRow;
  /** Immediate child nodes. */
  private final List<AnalyticsNode> _children;

  /* package */ AnalyticsNode(int startRow, int endRow, List<AnalyticsNode> children) {
    ArgumentChecker.notNull(children, "children");
    _startRow = startRow;
    _endRow = endRow;
    _children = children;
  }

  /**
   * @return An empty root node that starts and ends at row zero and has no children.
   */
  /* package */ static AnalyticsNode emptyRoot() {
    return new AnalyticsNode(0, 0, Collections.<AnalyticsNode>emptyList());
  }

  /**
   * Factory method that creates a node structure from a portfolio and returns the root node.
   * @param compiledViewDef A view definition
   * @return The root node of the portfolio's node structure
   */
  /* package */ static AnalyticsNode portoflioRoot(CompiledViewDefinition compiledViewDef) {
    Portfolio portfolio = compiledViewDef.getPortfolio();
    if (portfolio == null) {
      return new AnalyticsNode(0, 0, Collections.<AnalyticsNode>emptyList());
    }
    PortfolioNode root = portfolio.getRootNode();
    return new PortfolioNodeBuilder(root).getRoot();
  }

  /**
   * @return The row index (zero-based and inclusive) at which the node starts.
   */
  /* package */ int getStartRow() {
    return _startRow;
  }

  /**
   * @return The row index (zero-based and inclusive) at which the node ends. This includes all nested child nodes.
   * i.e. the end row of a node is the same as the end row of its most deeply nested child.
   */
  /* package */ int getEndRow() {
    return _endRow;
  }

  /**
   * @return The direct children of this node.
   */
  /* package */ List<AnalyticsNode> getChildren() {
    return _children;
  }

  @Override
  public String toString() {
    return "AnalyticsNode [_startRow=" + _startRow + ", _endRow=" + _endRow + ", _children=" + _children + "]";
  }

  /**
   * Mutable builder that creates the node structure for a portfolio and returns the root node. Package-scoped for
   * testing.
   */
  /* package */ static final class PortfolioNodeBuilder {

    /** The root node of the portfolio. */
    private final AnalyticsNode _root;
    /** Index of last row, updated as the structure is built. */
    private int _lastRow;

    /* package */ PortfolioNodeBuilder(PortfolioNode root) {
      _root = createNode(root);
      _lastRow = 0;
    }

    private AnalyticsNode createNode(PortfolioNode node) {
      int nodeStart = _lastRow;
      _lastRow += node.getPositions().size();
      List<AnalyticsNode> nodes = Lists.newArrayList();
      for (PortfolioNode child : node.getChildNodes()) {
        ++_lastRow;
        nodes.add(createNode(child));
      }
      return new AnalyticsNode(nodeStart, _lastRow, Collections.unmodifiableList(nodes));
    }

    /* package */ AnalyticsNode getRoot() {
      return _root;
    }
  }
}
