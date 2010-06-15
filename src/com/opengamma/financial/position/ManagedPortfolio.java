/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueIdentifier;

/**
 * A portfolio that can be managed.
 */
public final class ManagedPortfolio {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The node name.
   */
  private String _name;
  /**
   * The root node.
   */
  private ManagedPortfolioNode _rootNode;

  /**
   * Creates an instance.
   */
  public ManagedPortfolio() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the unique identifier
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the unique identifier of the position.
   * @param uid  the unique identifier
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the name
   */
  public void setName(String name) {
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the root node.
   * @return the root node
   */
  public ManagedPortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the root node.
   * @param rootNode  the root node, not null
   */
  public void setRootNode(ManagedPortfolioNode rootNode) {
    _rootNode = rootNode;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
