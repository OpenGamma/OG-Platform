/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.util.List;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A node within a portfolio tree.
 * <p>
 * A portfolio holds all positions within a flexible tree structure.
 * This interface represents a node in the tree holding a list of child nodes and positions.
 * Positions are the leaves in the tree and do not implement this interface.
 */
@PublicSPI
public interface PortfolioNode extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the node.
   * 
   * @return the unique identifier, not null
   */
  UniqueIdentifier getUniqueId();

  /**
   * Gets the unique identifier of the parent node, or {@code null} if this is a root node.
   * 
   * @return the unique identifier, null if root node
   */
  UniqueIdentifier getParentNodeId();

  /**
   * Gets the name of the node intended for display purposes.
   * 
   * @return the display name, not null
   */
  String getName();

  /**
   * Gets the total size of the children, including nodes and positions.
   * 
   * @return the size of the nodes and position
   */
  int size();

  /**
   * Gets the nodes that are immediate children of this node.
   * 
   * @return the child nodes, unmodifiable, not null
   */
  List<PortfolioNode> getChildNodes();

  /**
   * Gets the positions that are immediate children of this node.
   * 
   * @return the positions, unmodifiable, not null
   */
  List<Position> getPositions();

}
