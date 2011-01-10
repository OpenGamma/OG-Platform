/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
    /** Traverse the nodes depth-first. */
    DEPTH_FIRST,
    /** Traverse the nodes breadth-first. */
    BREADTH_FIRST,
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
   * Creates a traverser using depth-first searching.
   * If you don't know whether to use depth-first or breadth-first, then use depth-first.
   * 
   * @param callback  the callback to invoke, not null
   * @return the traverser, not null
   */
  public static PortfolioNodeTraverser depthFirst(PortfolioNodeTraversalCallback callback) {
    return new PortfolioNodeTraverser(TraversalStyle.DEPTH_FIRST, callback);
  }

  /**
   * Creates a traverser using breadth-first searching.
   * If you don't know whether to use depth-first or breadth-first, then use depth-first.
   * 
   * @param callback  the callback to invoke, not null
   * @return the traverser, not null
   */
  public static PortfolioNodeTraverser breadthFirst(PortfolioNodeTraversalCallback callback) {
    return new PortfolioNodeTraverser(TraversalStyle.BREADTH_FIRST, callback);
  }

  /**
   * Creates a traverser.
   * 
   * @param traversalStyle  the style of traversal, not null
   * @param callback  the callback to invoke, not null
   */
  public PortfolioNodeTraverser(TraversalStyle traversalStyle, PortfolioNodeTraversalCallback callback) {
    ArgumentChecker.notNull(traversalStyle, "traversalStyle");
    ArgumentChecker.notNull(callback, "callback");
    _traversalStyle = traversalStyle;
    _callback = callback;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the traversal style to be used.
   * 
   * @return the traversal style, not null
   */
  public TraversalStyle getTraversalStyle() {
    return _traversalStyle;
  }

  /**
   * Gets the callback to be used.
   * 
   * @return the callback, not null
   */
  public PortfolioNodeTraversalCallback getCallback() {
    return _callback;
  }

  //-------------------------------------------------------------------------
  /**
   * Traverse the nodes notifying using the callback.
   * 
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
   * 
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
      case DEPTH_FIRST:
        assert firstPass == true;
        for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
          traverse(subNode, true);
        }
        break;
      case BREADTH_FIRST:
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
        throw new IllegalStateException("Only DEPTH_FIRST and BREADTH_FIRST currently supported");
    }
    
    if (firstPass) {
      for (Position position : portfolioNode.getPositions()) {
        getCallback().postOrderOperation(position);
      }
      getCallback().postOrderOperation(portfolioNode);
    }
  }

}
