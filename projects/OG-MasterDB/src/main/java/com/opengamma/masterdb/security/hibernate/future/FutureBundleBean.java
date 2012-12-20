/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.masterdb.security.hibernate.ExternalIdBean;

/**
 * A Hibernate bean for storage.
 */
public class FutureBundleBean {

  private Long _id;
  private FutureSecurityBean _future;
  private Date _startDate;
  private Date _endDate;
  private double _conversionFactor;
  private Set<ExternalIdBean> _identifiers;

  public FutureBundleBean() {
  }
  
  public Long getId() {
    return _id;
  }
  
  public void setId(Long id) {
    _id = id;
  }
  
  public FutureSecurityBean getFuture() {
    return _future;
  }
  
  public void setFuture(final FutureSecurityBean future) {
    _future = future;
  }
  
  public Date getStartDate() {
    return _startDate;
  }
  
  public void setStartDate(final Date startDate) {
    _startDate = startDate;
  }
  
  public Date getEndDate() {
    return _endDate;
  }
  
  public void setEndDate(final Date endDate) {
    _endDate = endDate;
  }
  
  public double getConversionFactor() {
    return _conversionFactor;
  }
  
  public void setConversionFactor(final double conversionFactor) {
    _conversionFactor = conversionFactor;
  }
  
  public Set<ExternalIdBean> getIdentifiers() {
    return _identifiers;
  }
  
  public void setIdentifiers(final Set<ExternalIdBean> identifiers) {
    _identifiers = identifiers;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof FutureBundleBean)) {
      return false;
    }
    final FutureBundleBean other = (FutureBundleBean) o;
    return ObjectUtils.equals(getFuture().getId(), other.getFuture().getId())
        && ObjectUtils.equals(getConversionFactor(), other.getConversionFactor())
        && ObjectUtils.equals(getIdentifiers(), other.getIdentifiers());
  }
  
  @Override
  public int hashCode() {
    int hc = 1;
    hc = hc * 17 + ObjectUtils.hashCode(getConversionFactor());
    hc = hc * 17 + ObjectUtils.hashCode(getIdentifiers());
    return hc;
  }
  
}
