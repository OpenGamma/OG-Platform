/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 *
 * @author Andrew Griffin
 */
public class FutureBundleBean {
  
  private Long _id;
  private FutureSecurityBean _futureSecurity;
  private Date _startDate;
  private Date _endDate;
  private double _conversionFactor;
  private Set<FutureBundleIdentifierBean> _identifiers;

  public FutureBundleBean() {
  }
  
  public Long getId() {
    return _id;
  }
  
  public void setId(Long id) {
    _id = id;
  }
  
  public FutureSecurityBean getFutureSecurity() {
    return _futureSecurity;
  }
  
  public void setFutureSecurity(FutureSecurityBean futureSecurity) {
    _futureSecurity = futureSecurity;
  }
  
  public Date getStartDate () {
    return _startDate;
  }
  
  public void setStartDate (final Date startDate) {
    _startDate = startDate;
  }
  
  public Date getEndDate () {
    return _endDate;
  }
  
  public void setEndDate (final Date endDate) {
    _endDate = endDate;
  }
  
  public double getConversionFactor () {
    return _conversionFactor;
  }
  
  public void setConversionFactor (final double conversionFactor) {
    _conversionFactor = conversionFactor;
  }
  
  public Set<FutureBundleIdentifierBean> getIdentifiers () {
    return _identifiers;
  }
  
  public void setIdentifiers (final Set<FutureBundleIdentifierBean> identifiers) {
    _identifiers = identifiers;
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof FutureBundleBean)) return false;
    final FutureBundleBean other = (FutureBundleBean)o;
    return ObjectUtils.equals (getId (), other.getId ())
        && ObjectUtils.equals (getFutureSecurity (), other.getFutureSecurity ())
        && ObjectUtils.equals (getStartDate (), other.getStartDate ())
        && ObjectUtils.equals (getEndDate (), other.getEndDate ())
        && ObjectUtils.equals (getConversionFactor (), other.getConversionFactor ());
  }
  
  @Override
  public int hashCode () {
    int hc = 1;
    hc = hc * 17 + ObjectUtils.hashCode (getId ());
    hc = hc * 17 + ObjectUtils.hashCode (getFutureSecurity ());
    hc = hc * 17 + ObjectUtils.hashCode (getStartDate ());
    hc = hc * 17 + ObjectUtils.hashCode (getEndDate ());
    hc = hc * 17 + ObjectUtils.hashCode (getConversionFactor ());
    return hc;
  }
  
}
