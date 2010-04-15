/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Base class for the beans that are identifiable. 
 * 
 * @author Andrew Griffin
 */
public abstract class DateIdentifiableBean {

  private Long _id;
  private String _identifier;
  private Date _startDate;
  private Date _endDate;
  
  protected DateIdentifiableBean () {
  }
  
  protected DateIdentifiableBean (final DateIdentifiableBean other) {
    setIdentifier (other.getIdentifier ());
    setStartDate (other.getStartDate ());
    setEndDate (other.getEndDate ());
  }

  /**
   * @return the id
   */
  public Long getId() {
    return _id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    _id = id;
  }
  
  public String getIdentifier () {
    return _identifier;
  }
  
  public void setIdentifier (final String identifier) {
    _identifier = identifier;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return _startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(Date startDate) {
    _startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public Date getEndDate() {
    return _endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(Date endDate) {
    _endDate = endDate;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!getClass ().isAssignableFrom (o.getClass ())) return false;
    final DateIdentifiableBean other = (DateIdentifiableBean)o;
    return ObjectUtils.equals (getId (), other.getId ()) && ObjectUtils.equals (getIdentifier (), other.getIdentifier ()) && ObjectUtils.equals (getStartDate (), other.getStartDate ()) && ObjectUtils.equals (getEndDate (), other.getEndDate ());
  }
  
  @Override
  public int hashCode () {
    int hc = 1;
    hc = hc * 17 + ObjectUtils.hashCode (getId ());
    hc = hc * 17 + ObjectUtils.hashCode (getIdentifier ());
    hc = hc * 17 + ObjectUtils.hashCode (getStartDate ());
    hc = hc * 17 + ObjectUtils.hashCode (getEndDate ());
    return hc;
  }

}