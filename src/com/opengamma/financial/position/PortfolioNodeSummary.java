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
 * A summary of a node.
 */
public final class PortfolioNodeSummary {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The node name.
   */
  private String _name;
  /**
   * The total number of positions at any depth.
   */
  private int _totalPositions;

  /**
   * Creates an instance.
   */
  public PortfolioNodeSummary() {
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
   * Gets the total number of positions at any depth.
   * @return the total number of positions
   */
  public int getTotalPositions() {
    return _totalPositions;
  }

  /**
   * Sets the total number of positions at any depth.
   * @param totalPositions  the total number of positions
   */
  public void setTotalPositions(int totalPositions) {
    _totalPositions = totalPositions;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
