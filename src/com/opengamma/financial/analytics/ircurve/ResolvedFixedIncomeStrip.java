/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.security.Security;

/**
 * 
 */
public class ResolvedFixedIncomeStrip {
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

  public ResolvedFixedIncomeStrip(StripInstrumentType instrumentType, ZonedDateTime maturity, Security security) {
    _instrumentType = instrumentType;
    _maturity = maturity;
    _security = security;
  }
}
