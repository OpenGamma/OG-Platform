/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.List;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * A node within a portfolio tree.
 * <p>
 * A portfolio holds all positions within a flexible tree structure.
 * This interface represents a node in the tree holding a list of child nodes and positions.
 * Positions are the leaves in the tree and do not implement this interface.
 */
public interface PortfolioNode extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the node.
   * @return the identifier, not null
   */
  UniqueIdentifier getUniqueIdentifier();

  /**
   * Gets the name of the node intended for display purposes.
   * @return the name, not null
   */
  String getName();

  /**
   * Gets the total size of the children, including nodes and positions.
   * @return the size of the nodes and position
   */
  int size();

  /**
   * Gets the nodes which are immediate children of this node.
   * @return the child nodes, unmodifiable, not null
   */
  List<PortfolioNode> getChildNodes();

  /**
   * Gets the positions immediate children of this node.
   * @return the positions, unmodifiable, not null
   */
  List<Position> getPositions();

}
