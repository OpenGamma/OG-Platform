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
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
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
  /** Whether this node represents a position in a fungible security, i.e. it has child nodes which are trades. */
  private final boolean _collapsed;

  /* package */ AnalyticsNode(int startRow, int endRow, List<AnalyticsNode> children, boolean collapsed) {
    ArgumentChecker.notNull(children, "children");
    _collapsed = collapsed;
    _startRow = startRow;
    _endRow = endRow;
    _children = children;
  }

  /**
   * Factory method that creates a node structure from a portfolio and returns the root node.
   * @param portfolio The portfolio
   * @return The root node of the portfolio's node structure, null if the portfolio is null
   */
  /* package */ static AnalyticsNode portfolioRoot(Portfolio portfolio) {
    if (portfolio == null) {
      return null;
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

  /**
   * @return Whether this node is collapsed
   */
  public boolean isCollapsed() {
    return _collapsed;
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
      _root = createPortfolioNode(root, 0);
      _lastRow = 0;
    }

    private AnalyticsNode createPortfolioNode(PortfolioNode node, int level) {
      int nodeStart = _lastRow;
      List<AnalyticsNode> nodes = Lists.newArrayList();
      for (Position position : node.getPositions()) {
        ++_lastRow;
        if (position.getTrades().size() > 0 && isFungible(position.getSecurity())) {
          nodes.add(createFungiblePositionNode(position));
        }
      }
      for (PortfolioNode child : node.getChildNodes()) {
        ++_lastRow;
        nodes.add(createPortfolioNode(child, level + 1));
      }
      // leave root and first-level children expanded
      boolean collapse = level >= 2;
      return new AnalyticsNode(nodeStart, _lastRow, Collections.unmodifiableList(nodes), collapse);
    }

    private AnalyticsNode createFungiblePositionNode(Position position) {
      int nodeStart = _lastRow;
      _lastRow += position.getTrades().size();
      return new AnalyticsNode(nodeStart, _lastRow, Collections.<AnalyticsNode>emptyList(), true);
    }

    /**
     * @param security A security
     * @return true if the security is fungible, false if OTC
     */
    private static boolean isFungible(Security security) {
      if (security instanceof FinancialSecurity) {
        Boolean isOTC = ((FinancialSecurity) security).accept(new OtcSecurityVisitor());
        if (isOTC == null) {
          return false;
        }
        return !isOTC;
      } else {
        return false;
      }
    }

    /* package */ AnalyticsNode getRoot() {
      return _root;
    }
  }
}
