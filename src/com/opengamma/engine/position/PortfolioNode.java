/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.List;

import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;

/**
 * A node within a portfolio tree.
 * <p>
 * A portfolio holds all positions within a flexible tree structure.
 * This interface represents a node in the tree holding a list of child nodes and positions.
 * Positions are the leaves in the tree and do not implement this interface.
 */
public interface PortfolioNode extends Identifiable {

  /**
   * The key to be used to refer to a node in identifiers.
   */
  public static final IdentificationScheme PORTFOLIO_NODE_IDENTITY_KEY_SCHEME = new IdentificationScheme("PortfolioNodeIdentityKey");   

  /**
   * Gets the identity key of the node.
   * @return the identity key, null if not uniquely identified
   */
  @Override
  Identifier getIdentityKey();

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
   * @return the child nodes, unmodifiable, never null
   */
  List<PortfolioNode> getChildNodes();

  /**
   * Gets the positions immediate children of this node.
   * @return the positions, unmodifiable, never null
   */
  List<Position> getPositions();

  /**
   * Recursively finds a specific node from this node by identity key.
   * If this node matches it is returned.
   * @param identityKey  the identity key, null returns null
   * @return the node, null if not found
   */
  PortfolioNode getNode(Identifier identityKey);

  /**
   * Recursively finds a specific position from this node by identity key.
   * @param identityKey  the identity key, null returns null
   * @return the position, null if not found
   */
  Position getPosition(Identifier identityKey);

}
