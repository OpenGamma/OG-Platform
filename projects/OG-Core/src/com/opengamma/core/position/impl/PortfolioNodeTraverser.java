/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-01-02 -- One reason this class exists is so that you can parallel-apply
// many of the operations if they happen to be expensive. This isn't done yet, but the
// more code that depends on this class the easier it is to port to that system.

/**
 * A manager for traversing the tree of nodes.
 */
public class PortfolioNodeTraverser {

  /**
   * Enumeration of traversal styles.
   */
  public enum TraversalStyle {
    /** Depth first. */
    DFS,
    /** Breadth first. */
    BFS,
  };

  /**
   * The traversal style.
   */
  private final TraversalStyle _traversalStyle;
  /**
   * The callback.
   */
  private final PortfolioNodeTraversalCallback _callback;

  /**
   * Creates a traverser.
   * @param callback  the callback to invoke, not null
   */
  public PortfolioNodeTraverser(PortfolioNodeTraversalCallback callback) {
    this(TraversalStyle.DFS, callback);
  }

  /**
   * Creates a traverser.
   * @param traversalStyle  the style of traversal, not null
   * @param callback  the callback to invoke, not null
   */
  public PortfolioNodeTraverser(TraversalStyle traversalStyle, PortfolioNodeTraversalCallback callback) {
    ArgumentChecker.notNull(traversalStyle, "Traversal Style");
    ArgumentChecker.notNull(callback, "Traversal Callback");
    _traversalStyle = traversalStyle;
    _callback = callback;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the traversal style to be used.
   * @return the traversal style, not null
   */
  public TraversalStyle getTraversalStyle() {
    return _traversalStyle;
  }

  /**
   * Gets the callback to be used.
   * @return the callback, not null
   */
  public PortfolioNodeTraversalCallback getCallback() {
    return _callback;
  }

  //-------------------------------------------------------------------------
  /**
   * Traverse the nodes.
   * @param portfolioNode  the node to start from, null does nothing
   */
  public void traverse(PortfolioNode portfolioNode) {
    if (portfolioNode == null) {
      return;
    }
    traverse(portfolioNode, true);
  }

  /**
   * Traverse the nodes.
   * @param portfolioNode  the node to start from, not null
   * @param firstPass  true if first pass
   */
  protected void traverse(PortfolioNode portfolioNode, boolean firstPass) {
    if (firstPass) {
      getCallback().preOrderOperation(portfolioNode);
      for (Position position : portfolioNode.getPositions()) {
        getCallback().preOrderOperation(position);
      }
    }
    
    switch (getTraversalStyle()) {
      case DFS:
        assert firstPass == true;
        for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
          traverse(subNode, true);
        }
        break;
      case BFS:
        if (!firstPass) {
          for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
            traverse(subNode, true);
          }
          for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
            traverse(subNode, false);
          }
        }
        break;
      default:
        throw new IllegalStateException("Only BFS and DFS currently supported");
    }
    
    if (firstPass) {
      for (Position position : portfolioNode.getPositions()) {
        getCallback().postOrderOperation(position);
      }
      getCallback().postOrderOperation(portfolioNode);
    }
  }
}
