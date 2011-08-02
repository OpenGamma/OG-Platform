/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the portfolio.
   */
  private UniqueIdentifier _uniqueId;
  /**
   * The display name of the portfolio.
   */
  private String _name;
  /**
   * The root node.
   */
  private PortfolioNodeImpl _rootNode;

  /**
   * Creates a portfolio with the specified name.
   * 
   * @param name  the name to use, not null
   */
  public PortfolioImpl(String name) {
    this(name, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified name and root node.
   * 
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
   * Creates a portfolio with the specified unique identifier and name.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param name  the name to use, not null
   */
  public PortfolioImpl(UniqueIdentifier uniqueId, String name) {
    this(uniqueId, name, new PortfolioNodeImpl());
  }

  /**
   * Creates a portfolio with the specified unique identifier, name and root node.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param name  the name to use, not null
   * @param rootNode  the root node, not null
   */
  public PortfolioImpl(UniqueIdentifier uniqueId, String name, PortfolioNodeImpl rootNode) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "rootNode");
    _uniqueId = uniqueId;
    _name = name;
    _rootNode = rootNode;
  }

  /**
   * Creates a deep copy of the specified portfolio.
   * 
   * @param copyFrom  the portfolio to copy from, not null
   */
  public PortfolioImpl(Portfolio copyFrom) {
    ArgumentChecker.notNull(copyFrom, "portfolio");
    _uniqueId = copyFrom.getUniqueId();
    _name = copyFrom.getName();
    _rootNode = new PortfolioNodeImpl(copyFrom.getRootNode());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the portfolio.
   * 
   * @return the identifier, null if not from a position source
   */
  @Override
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the portfolio.
   * 
   * @param uniqueId  the new unique identifier, not null
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    _uniqueId = uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the portfolio intended for display purposes.
   * 
   * @return the name, not null
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the portfolio intended for display purposes.
   * 
   * @param name  the name, not null
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the root node in the portfolio.
   * 
   * @return the root node of the tree structure, not null
   */
  @Override
  public PortfolioNodeImpl getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the root node in the portfolio.
   * 
   * @param rootNode  the root node of the tree structure, not null
   */
  public void setRootNode(PortfolioNodeImpl rootNode) {
    ArgumentChecker.notNull(rootNode, "root node");
    _rootNode = rootNode;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a full-detail string containing all child nodes and positions.
   * 
   * @return the full-detail string, not null
   */
  public String toLongString() {
    return new StrBuilder(1024)
        .append("Portfolio[")
        .append("uniqueId" + "=")
        .append(getUniqueId())
        .append(",rootNode=")
        .append(getRootNode().toLongString())
        .append("]")
        .toString();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PortfolioImpl) {
      final PortfolioImpl other = (PortfolioImpl) obj;
      return ObjectUtils.equals(getUniqueId(), other.getUniqueId()) &&
          ObjectUtils.equals(getName(), other.getName()) &&
          ObjectUtils.equals(getRootNode(), other.getRootNode());
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
    // intentionally skip the root node
    return result;
  }

  @Override
  public String toString() {
    return new StrBuilder(128)
        .append("Portfolio[")
        .append(getUniqueId())
        .append("]")
        .toString();
  }

}
