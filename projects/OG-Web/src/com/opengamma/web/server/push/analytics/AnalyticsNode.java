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
 *
 */
public class AnalyticsNode {

  private final int _start;
  private final int _end;
  private final List<AnalyticsNode> children;

  /* package */ AnalyticsNode(int start, int end, List<AnalyticsNode> children) {
    ArgumentChecker.notNull(children, "children");
    _start = start;
    _end = end;
    this.children = children;
  }

  public static AnalyticsNode empty() {
    return new AnalyticsNode(0, 0, Collections.<AnalyticsNode>emptyList());
  }

  public static AnalyticsNode create(CompiledViewDefinition compiledViewDef) {
    Portfolio portfolio = compiledViewDef.getPortfolio();
    PortfolioNode root = portfolio.getRootNode();
    return new Builder(root).getRoot();
  }

  public int getStart() {
    return _start;
  }

  public int getEnd() {
    return _end;
  }

  public List<AnalyticsNode> getChildren() {
    return children;
  }

  /* package */ static class Builder {

    private final AnalyticsNode _root;

    private int _lastRow = 0;

    /* package */ Builder(PortfolioNode root) {
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
