/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;

/**
 *  
 */
public class FixedIncomeStripWithSecurity {
  private StripInstrumentType _instrumentType;
  private ZonedDateTime _maturity;
  private Identifier _securityIdentifier;
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
   * Gets the identifier that was used to resolve the security
   * This is available, just so the same identifier can be conveniently used to retrieve requested market data.
   * @return The security identifier
   */
  public Identifier getSecurityIdentifier() {
    return _securityIdentifier;
  }

  /**
   * Gets the security field.
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  public FixedIncomeStripWithSecurity(StripInstrumentType instrumentType, ZonedDateTime maturity, Identifier securityIdentifier, Security security) {
    _instrumentType = instrumentType;
    _maturity = maturity;
    _securityIdentifier = securityIdentifier;
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
    result = getSecurity().getUniqueId().getValue().compareTo(o.getSecurity().getUniqueId().getValue());
    return result;
  }
}
