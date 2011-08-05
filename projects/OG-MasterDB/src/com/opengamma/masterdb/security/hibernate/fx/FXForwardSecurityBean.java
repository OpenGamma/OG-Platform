/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link FXForwardSecurity}.
 */
public class FXForwardSecurityBean extends SecurityBean {
  private ZonedDateTimeBean _forwardDate;
  private ExternalIdBean _region;
  private ExternalIdBean _underlying;
  
  /**
   * Gets the forwardDate.
   * @return the forwardDate
   */
  public ZonedDateTimeBean getForwardDate() {
    return _forwardDate;
  }

  /**
   * Sets the forwardDate.
   * @param forwardDate  the forwardDate
   */
  public void setForwardDate(ZonedDateTimeBean forwardDate) {
    _forwardDate = forwardDate;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  public ExternalIdBean getUnderlying() {
    return _underlying;
  }

  /**
   * Sets the underlying.
   * @param underlying  the underlying
   */
  public void setUnderlying(ExternalIdBean underlying) {
    _underlying = underlying;
  }

  /**
   * Gets the region.
   * @return the region
   */
  public ExternalIdBean getRegion() {
    return _region;
  }

  /**
   * Sets the region.
   * @param region  the region
   */
  public void setRegion(ExternalIdBean region) {
    _region = region;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof FXForwardSecurityBean)) {
      return false;
    }
    FXForwardSecurityBean fxForward = (FXForwardSecurityBean) other;
    return new EqualsBuilder()
      .append(getId(), fxForward.getId())
      .append(getForwardDate(), fxForward.getForwardDate())
      .append(getUnderlying(), fxForward.getUnderlying())
      .append(getRegion(), fxForward.getRegion())
      .isEquals();
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getForwardDate())
      .append(getUnderlying())
      .append(getRegion())
      .toHashCode();
  }

}
