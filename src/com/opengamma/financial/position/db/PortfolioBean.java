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
public class PortfolioBean extends DateIdentifiableBean {

  private String _name;
  private PortfolioNodeBean _root;
  
  public PortfolioBean () {
  }
  
  public PortfolioBean (final PortfolioBean other) {
    super (other);
    setName (other.getName ());
    setRoot (other.getRoot ());
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
   * @return the root
   */
  public PortfolioNodeBean getRoot() {
    return _root;
  }

  /**
   * @param root the root to set
   */
  public void setRoot(PortfolioNodeBean root) {
    _root = root;
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!super.equals (o)) return false;
    final PortfolioBean other = (PortfolioBean)o;
    return ObjectUtils.equals (getName (), other.getName ()) && ObjectUtils.equals (getRoot (), other.getRoot ());
  }
  
  @Override
  public int hashCode () {
    int hc = super.hashCode ();
    hc = hc * 17 + ObjectUtils.hashCode (getName ());
    hc = hc * 17 + ObjectUtils.hashCode (getRoot ());
    return hc;
  }

}