/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 *
 * @author Andrew Griffin
 */
public class FutureBundleIdentifierBean {
  
  private Long _id;
  private FutureBundleBean _futureBundle;
  private DomainSpecificIdentifierBean _identifier;

  public FutureBundleIdentifierBean() {
  }
  
  public Long getId() {
    return _id;
  }
  
  public void setId(Long id) {
    _id = id;
  }
  
  public FutureBundleBean getFutureBundle() {
    return _futureBundle;
  }
  
  public void setFutureBundle(FutureBundleBean futureBundle) {
    _futureBundle = futureBundle;
  }
  
  public DomainSpecificIdentifierBean getIdentifier () {
    return _identifier;
  }
  
  public void setIdentifier (final DomainSpecificIdentifierBean identifier) {
    _identifier = identifier;
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof FutureBundleIdentifierBean)) return false;
    final FutureBundleIdentifierBean other = (FutureBundleIdentifierBean)o;
    return ObjectUtils.equals (getId (), other.getId ())
        && ObjectUtils.equals (getFutureBundle (), other.getFutureBundle ())
        && ObjectUtils.equals (getIdentifier (), other.getIdentifier ());
  }
  
  @Override
  public int hashCode () {
    int hc = 1;
    hc = hc * 17 + ObjectUtils.hashCode (getId ());
    hc = hc * 17 + ObjectUtils.hashCode (getFutureBundle ());
    hc = hc * 17 + ObjectUtils.hashCode (getIdentifier ());
    return hc;
  }
  
}
