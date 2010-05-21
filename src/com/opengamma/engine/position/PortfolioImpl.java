/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Portfolio}.
 */
public class PortfolioImpl implements Portfolio, Serializable {

  /**
   * The identifier.
   */
  private UniqueIdentifier _identifier;
  /**
   * The name.
   */
  private String _name;
  /**
   * The root node.
   */
  private PortfolioNodeImpl _rootNode;

  /**
   * Creates a portfolio with the specified identifier.
   * @param identifier  the portfolio identifier, not null
   * @param name  the name to use, not null
   */
  public PortfolioImpl(UniqueIdentifier identifier, String name) {
    this(identifier, name, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified identifier in the format {@code <SCHEME>::<VALUE>}.
   * @param identifierStr  the portfolio identifier, not null
   */
  public PortfolioImpl(String identifierStr) {
    this(UniqueIdentifier.parse(identifierStr), identifierStr, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified identifier and root node.
   * @param identifier  the portfolio identifier, not null
   * @param name  the name to use, not null
   * @param rootNode  the root node, not null
   */
  public PortfolioImpl(UniqueIdentifier identifier, String name, PortfolioNodeImpl rootNode) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "root node");
    _identifier = identifier;
    _name = name;
    _rootNode = rootNode;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the portfolio.
   * @return the identifier, not null
   */
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _identifier;
  }

  /**
   * Sets the unique identifier of the portfolio.
   * @param identifier  the new identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the portfolio intended for display purposes.
   * @return the name, not null
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the portfolio intended for display purposes.
   * @param name  the name, not null
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the root node in the portfolio.
   * @return the root node of the tree structure, not null
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
   * Finds a specific node from this portfolio by identifier.
   * @param identifier  the identifier, null returns null
   * @return the node, null if not found
   */
  @Override
  public PortfolioNode getNode(UniqueIdentifier identifier) {
    return _rootNode.getNode(identifier);
  }

  /**
   * Finds a specific position from this portfolio by identifier.
   * @param identifier  the identifier, null returns null
   * @return the position, null if not found
   */
  @Override
  public Position getPosition(UniqueIdentifier identifier) {
    return _rootNode.getPosition(identifier);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return new StrBuilder()
      .append("Portfolio[")
      .append(getUniqueIdentifier())
      .append("]")
      .toString();
  }
  
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof PortfolioImpl)) {
      return false;
    }
    final PortfolioImpl other = (PortfolioImpl) o;
    return ObjectUtils.equals(getUniqueIdentifier(), other.getUniqueIdentifier())
        && ObjectUtils.equals(getName(), other.getName())
        && ObjectUtils.equals(getRootNode(), other.getRootNode());
  }

  @Override
  public int hashCode() {
    int result = 0;
    int prime = 31;
    if (getUniqueIdentifier() != null) {
      result = result * prime + getUniqueIdentifier().hashCode();
    }
    if (getName() != null) {
      result = result * prime + getName().hashCode(); 
    }
    // Intentionally skip the root node; no need for it.
    return result;
  }
  
  

}
