/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.security.Security;

/**
 *  
 */
public class FixedIncomeStripWithSecurity {
  private StripInstrumentType _instrumentType;
  private ZonedDateTime _maturity;
  private Security _security;

  /**
   * Gets the instrumentType field.
   * @return the instrumentType
   */
  public StripInstrumentType getInstrumentType() {
    return _instrumentType;
  }

  /**
   * Gets the years field.
   * @return the years
   */
  public ZonedDateTime getMaturity() {
    return _maturity;
  }

  /**
   * Gets the security field.
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  public FixedIncomeStripWithSecurity(StripInstrumentType instrumentType, ZonedDateTime maturity, Security security) {
    _instrumentType = instrumentType;
    _maturity = maturity;
    _security = security;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithSecurity) {
      FixedIncomeStripWithSecurity other = (FixedIncomeStripWithSecurity) obj;
      return ObjectUtils.equals(_maturity, other._maturity) &&
             ObjectUtils.equals(_security, other._security) &&
             _instrumentType == other._instrumentType;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _maturity.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public int compareTo(FixedIncomeStripWithSecurity o) {
    int result = (int) getMaturity().compareTo(o.getMaturity());
    if (result != 0) {
      return result;
    }
    result = getInstrumentType().ordinal() - o.getInstrumentType().ordinal(); 
    if (result != 0) {
      return result;
    }
    result = getSecurity().getUniqueIdentifier().getValue().compareTo(o.getSecurity().getUniqueIdentifier().getValue());
    return result;
  }
}
