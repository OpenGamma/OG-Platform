/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-01-02 -- One reason this class exists is so that you can parallel-apply
// many of the operations if they happen to be expensive. This isn't done yet, but the
// more code that depends on this class the easier it is to port to that system.

/**
 * 
 *
 * @author kirk
 */
public class PortfolioNodeTraverser {
  public enum TraversalStyle { DFS, BFS };
  private final TraversalStyle _traversalStyle;
  private final PortfolioNodeTraversalCallback _callback;
  
  public PortfolioNodeTraverser(PortfolioNodeTraversalCallback callback) {
    this(TraversalStyle.DFS, callback);
  }
  
  public PortfolioNodeTraverser(TraversalStyle traversalStyle, PortfolioNodeTraversalCallback callback) {
    ArgumentChecker.notNull(traversalStyle, "Traversal Style");
    ArgumentChecker.notNull(callback, "Traversal Callback");
    _traversalStyle = traversalStyle;
    _callback = callback;
  }

  /**
   * @return the traversalStyle
   */
  public TraversalStyle getTraversalStyle() {
    return _traversalStyle;
  }

  /**
   * @return the callback
   */
  public PortfolioNodeTraversalCallback getCallback() {
    return _callback;
  }

  public void traverse(PortfolioNode portfolioNode) {
    traverse(portfolioNode, true);
  }
  
  protected void traverse(PortfolioNode portfolioNode, boolean firstPass) {
    if(portfolioNode == null) {
      return;
    }
    if(firstPass) {
      getCallback().preOrderOperation(portfolioNode);
      
      for(Position position : portfolioNode.getPositions()) {
        getCallback().preOrderOperation(position);
      }
    }
    
    if(getTraversalStyle() == TraversalStyle.DFS) {
      assert firstPass == true;
      for(PortfolioNode subNode : portfolioNode.getSubNodes()) {
        traverse(subNode, true);
      }
    } else if(getTraversalStyle() == TraversalStyle.BFS) {
      if(!firstPass) {
        for(PortfolioNode subNode : portfolioNode.getSubNodes()) {
          traverse(subNode, true);
        }
        for(PortfolioNode subNode : portfolioNode.getSubNodes()) {
          traverse(subNode, false);
        }
      }
    } else {
      throw new IllegalStateException("Only BFS and DFS currently supported.");
    }

    if(firstPass) {
      for(Position position : portfolioNode.getPositions()) {
        getCallback().postOrderOperation(position);
      }
      
      getCallback().postOrderOperation(portfolioNode);
    }
  }
}
