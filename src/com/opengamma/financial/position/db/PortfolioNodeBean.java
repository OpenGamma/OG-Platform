/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 * @author Andrew Griffin
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
  
  public PortfolioNodeBean () {
  }
  
  public PortfolioNodeBean (final PortfolioNodeBean other) {
    super (other);
    setName (other.getName ());
    setAncestorId (other.getAncestorId ());
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return the ancestor
   */
  public Long getAncestorId() {
    if (_ancestorId != null) {
      return _ancestorId;
    } else {
      if (_ancestor != null) {
        return _ancestor.getId ();
      } else {
        return null;
      }
    }
  }
  
  public void setAncestorId(final Long ancestorId) {
    _ancestorId = ancestorId;
    _ancestor = null;
  }
  
  /**
   * @param ancestor the ancestor to set
   */
  public void setAncestor(final PortfolioNodeBean ancestor) {
    _ancestor = ancestor;
    _ancestorId = null;
  }
  
  public PortfolioNodeBean getAncestor () {
    return _ancestor;
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!super.equals (o)) return false;
    final PortfolioNodeBean other = (PortfolioNodeBean)o;
    return ObjectUtils.equals (getName (), other.getName ()) && ObjectUtils.equals (getAncestorId (), other.getAncestorId ());
  }
  
  @Override
  public int hashCode () {
    int hc = super.hashCode ();
    hc = hc * 17 + ObjectUtils.hashCode (getName ());
    hc = hc * 17 + ObjectUtils.hashCode (getAncestorId ());
    return hc;
  }
  
}