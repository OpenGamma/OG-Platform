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

import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FixedIncomeStripWithIdentifier implements Comparable<FixedIncomeStripWithIdentifier> {
  private final FixedIncomeStrip _strip;
  private final ExternalId _security;

  /**
   * Gets the fixed income strip.
   * @return The fixed income strip
   */
  public FixedIncomeStrip getStrip() {
    return _strip;
  }
  /**
   * Gets the strip instrument type.
   * @return the strip instrument type
   */
  public StripInstrumentType getInstrumentType() {
    return _strip.getInstrumentType();
  }

  /**
   * Gets the strip maturity.
   * @return the strip maturity
   */
  public Tenor getMaturity() {
    return _strip.getCurveNodePointTime();
  }

  /**
   * Gets the security field.
   * @return the security
   */
  public ExternalId getSecurity() {
    return _security;
  }

  /**
   * Get the number of the quarterly IR futures after the tenor to choose.
   * NOTE: THIS DOESN'T REFER TO A GENERIC FUTURE
   * @return number of futures after the tenor
   */
  public int getNumberOfFuturesAfterTenor() {
    return _strip.getNumberOfFuturesAfterTenor();
  }

  /**
   * Get the periods per year of a periodic zero deposit security
   * 
   * @return the number of periods per year
   * @throws IllegalStateException if called on a non-periodic zero deposit strip
   */
  public int getPeriodsPerYear() {
    return _strip.getPeriodsPerYear();
  }

  public FixedIncomeStripWithIdentifier(final FixedIncomeStrip strip, final ExternalId security) {
    Validate.notNull(strip);
    Validate.notNull(security);
    _strip = strip;
    _security = security;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithIdentifier) {
      final FixedIncomeStripWithIdentifier other = (FixedIncomeStripWithIdentifier) obj;
      return ObjectUtils.equals(_strip, other._strip) &&
          ObjectUtils.equals(_security, other._security);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _strip.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int compareTo(final FixedIncomeStripWithIdentifier o) {
    int result = getStrip().compareTo(o.getStrip());
    if (result != 0) {
      return result;
    }
    result = getSecurity().getValue().compareTo(o.getSecurity().getValue());
    return result;
  }

}
