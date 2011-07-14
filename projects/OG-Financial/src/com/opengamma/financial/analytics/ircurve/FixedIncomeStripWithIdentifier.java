/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FixedIncomeStripWithIdentifier implements Comparable<FixedIncomeStripWithIdentifier> {
  private final StripInstrumentType _instrumentType;
  private final Tenor _maturity;
  private int _nthFutureFromTenor;
  private final Identifier _security;

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

  /**
   * Get the number of the quarterly IR futures after the tenor to choose.  
   * NOTE: THIS DOESN'T REFER TO A GENERIC FUTURE
   * @return number of futures after the tenor
   * @throws IllegalStateException if called on a non-future strip
   */
  public int getNumberOfFuturesAfterTenor() {
    if (_instrumentType != StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot get number of futures after tenor for a non future node " + toString());
    }
    return _nthFutureFromTenor;
  }

  public FixedIncomeStripWithIdentifier(final StripInstrumentType instrumentType, final Tenor maturity, final int nthFutureFromTenor, final Identifier security) {
    _instrumentType = instrumentType;
    if (_instrumentType != StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot set number of futures after tenor for a non future node, type=" + instrumentType + " maturity=" + maturity + " security=" + security);
    }
    _nthFutureFromTenor = nthFutureFromTenor;
    Validate.notNull(maturity);
    _maturity = maturity;
    Validate.notNull(security);
    _security = security;
  }

  public FixedIncomeStripWithIdentifier(final StripInstrumentType instrumentType, final Tenor maturity, final Identifier security) {
    _instrumentType = instrumentType;
    if (_instrumentType == StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot create future node type without a nthFutureFromTenor parameter, type=" + instrumentType + " maturity=" + maturity + " security=" + security);
    }
    Validate.notNull(maturity);
    _maturity = maturity;
    Validate.notNull(security);
    _security = security;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithIdentifier) {
      final FixedIncomeStripWithIdentifier other = (FixedIncomeStripWithIdentifier) obj;
      return ObjectUtils.equals(_maturity, other._maturity) &&
             _nthFutureFromTenor == other._nthFutureFromTenor &&
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

  @Override
  public int compareTo(final FixedIncomeStripWithIdentifier o) {
    int result = getMaturity().getPeriod().toPeriodFields().toEstimatedDuration().compareTo(o.getMaturity().getPeriod().toPeriodFields().toEstimatedDuration());
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
