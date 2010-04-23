/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.Identifier;

/**
 * 
 * @author Andrew Griffin
 */
public class IdentifierAssociationBean extends DateIdentifiableBean {
  
  private PositionBean _position;
  private String _scheme;
  
  public IdentifierAssociationBean () {
  }
  
  public IdentifierAssociationBean (final IdentifierAssociationBean other) {
    super (other);
    setPosition (other.getPosition ());
    setDomain (other.getScheme ());
  }

  /**
   * @return the position
   */
  public PositionBean getPosition() {
    return _position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(PositionBean position) {
    _position = position;
  }

  /**
   * @return the domain
   */
  public String getScheme() {
    return _scheme;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(String domain) {
    _scheme = domain;
  }

  public Identifier getDomainSpecificIdentifier () {
    return new Identifier (getScheme (), getIdentifier ());
  }
  
  public void setDomainSpecificIdentifier (final Identifier identifier) {
    setDomain (identifier.getScheme ().getName ());
    setIdentifier (identifier.getValue ());
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!super.equals (o)) return false;
    final IdentifierAssociationBean other = (IdentifierAssociationBean)o;
    return ObjectUtils.equals (getPosition (), other.getPosition ()) && ObjectUtils.equals (getScheme (), other.getScheme ());
  }
  
  @Override
  public int hashCode () {
    int hc = super.hashCode ();
    hc = hc * 17 + ObjectUtils.hashCode (getPosition ());
    hc = hc * 17 + ObjectUtils.hashCode (getScheme ());
    return hc;
  }
  
}