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
 * Base class for the Hibernate beans that have an identifier and date range.
 * Subclasses include portfolio, portfolio node and position.
 */
public abstract class DateIdentifiableBean {

  private Long _id;
  private String _identifier;
  private Date _startDate;
  private Date _endDate;

  /**
   * Creates an instance.
   */
  protected DateIdentifiableBean() {
  }

  /**
   * Creates an instance.
   * @param other  the bean to copy, not null
   */
  protected DateIdentifiableBean(final DateIdentifiableBean other) {
    setIdentifier(other.getIdentifier());
    setStartDate(other.getStartDate());
    setEndDate(other.getEndDate());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the id
   */
  public Long getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the id to set
   */
  public void setId(Long id) {
    _id = id;
  }

  /**
   * Gets the identifier.
   * @return the start date
   */
  public String getIdentifier() {
    return _identifier;
  }

  /**
   * Sets the identifier.
   * @param identifier  the identifier to set
   */
  public void setIdentifier(final String identifier) {
    _identifier = identifier;
  }

  /**
   * Gets the start date.
   * @return the start date
   */
  public Date getStartDate() {
    return _startDate;
  }

  /**
   * Sets the start date.
   * @param startDate the start date to set
   */
  public void setStartDate(Date startDate) {
    _startDate = startDate;
  }

  /**
   * Gets the end date.
   * @return the end date
   */
  public Date getEndDate() {
    return _endDate;
  }

  /**
   * Sets the end date.
   * @param endDate  the end date to set
   */
  public void setEndDate(Date endDate) {
    _endDate = endDate;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (getClass() == obj.getClass()) {
      final DateIdentifiableBean other = (DateIdentifiableBean) obj;
      return ObjectUtils.equals(getId(), other.getId()) && ObjectUtils.equals(getIdentifier(), other.getIdentifier())
          && ObjectUtils.equals(getStartDate(), other.getStartDate())
          && ObjectUtils.equals(getEndDate(), other.getEndDate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc = hc * 17 + ObjectUtils.hashCode(getId());
    hc = hc * 17 + ObjectUtils.hashCode(getIdentifier());
    hc = hc * 17 + ObjectUtils.hashCode(getStartDate());
    hc = hc * 17 + ObjectUtils.hashCode(getEndDate());
    return hc;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
