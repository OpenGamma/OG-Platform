/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;

import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Portfolio}.
 */
public class PortfolioImpl implements Portfolio, Serializable {

  /**
   * The id.
   */
  private final Identifier _id;
  /**
   * The name.
   */
  private final String _name;
  /**
   * The root node.
   */
  private PortfolioNodeImpl _rootNode;

  /**
   * Creates a portfolio with the specified identifier.
   * @param id  the portfolio identifier, not null
   * @param name  the name to use, not null
   */
  public PortfolioImpl(Identifier id, String name) {
    this(id, name, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified identifier which is also used for the name.
   * @param id  the portfolio identifier, not null
   */
  public PortfolioImpl(String identifier) {
    this(new Identifier("Basic", identifier), identifier, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified identifier and root node.
   * @param id  the portfolio identifier, not null
   * @param name  the name to use, not null
   * @param rootNode  the root node, not null
   */
  public PortfolioImpl(Identifier id, String name, PortfolioNodeImpl rootNode) {
    ArgumentChecker.notNull(id, "identifier");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "root node");
    _id = id;
    _name = name;
    _rootNode = rootNode;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identifier of the portfolio.
   * @return the identifier, never null
   */
  @Override
  public Identifier getIdentityKey() {
    return _id;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the portfolio intended for display purposes.
   * @return the name, never null
   */
  @Override
  public String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the root node in the portfolio.
   * @return the root node of the tree structure, never null
   */
  @Override
  public PortfolioNodeImpl getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the root node in the portfolio.
   * @param rootNode  the root node of the tree structure, not null
   */
  public void setRootNode(PortfolioNodeImpl rootNode) {
    ArgumentChecker.notNull(rootNode, "root node");
    _rootNode = rootNode;
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a specific node from this portfolio by identity key.
   * @param identityKey  the identity key, null returns null
   * @return the node, null if not found
   */
  public PortfolioNode getNode(Identifier identityKey) {
    return _rootNode.getNode(identityKey);
  }

  /**
   * Finds a specific position from this portfolio by identity key.
   * @param identityKey  the identity key, null returns null
   * @return the position, null if not found
   */
  public Position getPosition(Identifier identityKey) {
    return _rootNode.getPosition(identityKey);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return new StringBuilder()
      .append("Portfolio[")
      .append(getIdentityKey())
      .append("]")
      .toString();
  }

}
