/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import org.apache.commons.lang.ObjectUtils;

/**
 * A Hibernate bean for portfolio nodes.
 */
public class PortfolioNodeBean extends DateIdentifiableBean {

  private String _name;
  private PortfolioNodeBean _ancestor;
  private Long _ancestorId;

  /* Only one or other of ancestor/ancestorId is ever set. The ID is
   * used by Hibernate or it will go recursive to load beans from the
   * database. The holding of a bean is to allow a graph of beans
   * to be created before any are written to the database.
   */

  /**
   * Creates an instance.
   */
  public PortfolioNodeBean() {
  }

  /**
   * Creates an instance based on another.
   * @param other  the instance to copy, not null
   */
  public PortfolioNodeBean(final PortfolioNodeBean other) {
    super(other);
    setName(other.getName());
    setAncestorId(other.getAncestorId());
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
   * @param name  the name to set
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Gets the ancestor id.
   * @return the ancestor
   */
  public Long getAncestorId() {
    if (_ancestorId != null) {
      return _ancestorId;
    } else {
      if (_ancestor != null) {
        return _ancestor.getId();
      } else {
        return null;
      }
    }
  }

  /**
   * Sets the ancestor id.
   * This sets the ancestor object to null.
   * @param ancestorId  the ancestor to set
   */
  public void setAncestorId(final Long ancestorId) {
    _ancestorId = ancestorId;
    _ancestor = null;
  }

  /**
   * Gets the ancestor object.
   * @return the ancestor
   */
  public PortfolioNodeBean getAncestor() {
    return _ancestor;
  }

  /**
   * Sets the ancestor object.
   * This sets the id to null.
   * @param ancestor  the ancestor to set
   */
  public void setAncestor(final PortfolioNodeBean ancestor) {
    _ancestor = ancestor;
    _ancestorId = null;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (super.equals(obj)) {
      final PortfolioNodeBean other = (PortfolioNodeBean) obj;
      return ObjectUtils.equals(getName(), other.getName()) && ObjectUtils.equals(getAncestorId(), other.getAncestorId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hc = super.hashCode();
    hc = hc * 17 + ObjectUtils.hashCode(getName());
    hc = hc * 17 + ObjectUtils.hashCode(getAncestorId());
    return hc;
  }

}
