/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import org.apache.commons.lang.ObjectUtils;

/**
 * A Hibernate bean for portfolios.
 */
public class PortfolioBean extends DateIdentifiableBean {

  private String _name;
  private PortfolioNodeBean _root;

  public PortfolioBean() {
  }

  /**
   * Creates an instance based on another.
   * @param other  the instance to copy, not null
   */
  public PortfolioBean(final PortfolioBean other) {
    super(other);
    setName(other.getName());
    setRoot(other.getRoot());
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
   * Gets the root.
   * @return the root
   */
  public PortfolioNodeBean getRoot() {
    return _root;
  }

  /**
   * Sets the root.
   * @param root  the root to set
   */
  public void setRoot(PortfolioNodeBean root) {
    _root = root;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (super.equals(obj)) {
      final PortfolioBean other = (PortfolioBean) obj;
      return ObjectUtils.equals(getName(), other.getName()) && ObjectUtils.equals(getRoot(), other.getRoot());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hc = super.hashCode();
    hc = hc * 17 + ObjectUtils.hashCode(getName());
    hc = hc * 17 + ObjectUtils.hashCode(getRoot());
    return hc;
  }

}
