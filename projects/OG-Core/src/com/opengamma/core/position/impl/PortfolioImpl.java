/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.Portfolio;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Portfolio}.
 */
public class PortfolioImpl implements Portfolio, MutableUniqueIdentifiable, Serializable {

  /** Serialization. */
  private static final long serialVersionUID = 1L;

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
   * Creates a portfolio with the specified name.
   * @param name  the name to use, not null
   */
  public PortfolioImpl(String name) {
    this(name, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified name and root node.
   * @param name  the name to use, not null
   * @param rootNode  the root node, not null
   */
  public PortfolioImpl(String name, PortfolioNodeImpl rootNode) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "root node");
    _name = name;
    _rootNode = rootNode;
  }

  /**
   * Creates a portfolio with the specified identifier.
   * @param identifier  the portfolio identifier, not null
   * @param name  the name to use, not null
   */
  public PortfolioImpl(UniqueIdentifier identifier, String name) {
    this(identifier, name, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified identifier, name and root node.
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
   * @return the identifier, null if not from a position source
   */
  @Override
  public UniqueIdentifier getUniqueId() {
    return _identifier;
  }

  /**
   * Sets the unique identifier of the portfolio.
   * @param identifier  the new identifier, not null
   */
  public void setUniqueId(UniqueIdentifier identifier) {
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
   * Gets a long string describing the object.
   * @return the long format string, not null
   */
  public String toLongString() {
    return new StrBuilder()
      .append("Portfolio[")
      .append("uniqueIdentifier=")
      .append(getUniqueId())
      .append(",rootNode=")
      .append(getRootNode().toLongString())
      .append("]")
      .toString();
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("Portfolio[")
      .append(getUniqueId())
      .append("]")
      .toString();
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PortfolioImpl) {
      final PortfolioImpl other = (PortfolioImpl) obj;
      return ObjectUtils.equals(getUniqueId(), other.getUniqueId())
          && ObjectUtils.equals(getName(), other.getName())
          && ObjectUtils.equals(getRootNode(), other.getRootNode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 0;
    int prime = 31;
    if (getUniqueId() != null) {
      result = result * prime + getUniqueId().hashCode();
    }
    if (getName() != null) {
      result = result * prime + getName().hashCode(); 
    }
    // Intentionally skip the root node; no need for it.
    return result;
  }

}
