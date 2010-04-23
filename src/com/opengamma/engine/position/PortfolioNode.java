/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;

/**
 * A node within the position master system.
 * <p>
 * The position master consists of a tree/acyclic graph of nodes that hold the
 * positions in a suitable structure.
 *
 * @author kirk
 */
public interface PortfolioNode extends Identifiable {

  /**
   * The key to be used to refer to a node in identifiers.
   */
  public static final IdentificationScheme PORTFOLIO_NODE_IDENTITY_KEY_DOMAIN = new IdentificationScheme("PortfolioNodeIdentityKey");   

  /**
   * Gets the positions which are direct children of this node.
   * @return the positions, never null
   */
  Collection<Position> getPositions();

  /**
   * Gets the child nodes which are direct children of this node.
   * @return the child nodes, never null
   */
  Collection<PortfolioNode> getSubNodes();

  /**
   * Gets the name of the node.
   * @return the name
   */
  String getName();

}
