/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FixedIncomeStripWithIdentifier {
  private StripInstrumentType _instrumentType;
  private Tenor _maturity;
  private Identifier _security;

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
  public Tenor getMaturity() {
    return _maturity;
  }

  /**
   * Gets the security field.
   * @return the security
   */
  public Identifier getSecurity() {
    return _security;
  }

  public FixedIncomeStripWithIdentifier(StripInstrumentType instrumentType, Tenor maturity, Identifier security) {
    _instrumentType = instrumentType;
    _maturity = maturity;
    _security = security;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithIdentifier) {
      FixedIncomeStripWithIdentifier other = (FixedIncomeStripWithIdentifier) obj;
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

  public int compareTo(FixedIncomeStripWithIdentifier o) {
    int result = (int) getMaturity().getPeriod().toPeriodFields().toEstimatedDuration().compareTo(o.getMaturity().getPeriod().toPeriodFields().toEstimatedDuration());
    if (result != 0) {
      return result;
    }
    result = getInstrumentType().ordinal() - o.getInstrumentType().ordinal(); 
    if (result != 0) {
      return result;
    }
    result = getSecurity().getValue().compareTo(o.getSecurity().getValue());
    return result;
  }
  
}
