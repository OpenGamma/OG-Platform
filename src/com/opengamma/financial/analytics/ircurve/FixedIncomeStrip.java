/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A fixed income strip.
 */
public class FixedIncomeStrip implements Comparable<FixedIncomeStrip>, Serializable {

  private final double _numYears;
  private final UniqueIdentifier _marketDataKey;

  /**
   * Creates the strip.
   * @param numYears  the number of years, not negative
   * @param marketDataKey  the market data key, not null
   */
  public FixedIncomeStrip(double numYears, UniqueIdentifier marketDataKey) {
    if (numYears < 0) {
      throw new IllegalArgumentException("Fixed income strips cannot be in the future.");
    }
    ArgumentChecker.notNull(marketDataKey, "Market data key");
    _numYears = numYears;
    _marketDataKey = marketDataKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of years.
   * @return the number of years
   */
  public double getNumYears() {
    return _numYears;
  }

  /**
   * Gets the market data identifier.
   * @return the market data key, not null
   */
  public UniqueIdentifier getMarketDataKey() {
    return _marketDataKey;
  }

  /**
   * Gets the specification for the market data key.
   * @return the specification, not null
   */
  public ComputationTargetSpecification getMarketDataSpecification() {
    // REVIEW kirk 2009-12-30 -- We might want to cache this on construction if it's called a lot.
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, getMarketDataKey());
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(FixedIncomeStrip other) {
    if (getNumYears() < other.getNumYears()) {
      return -1;
    } else if (getNumYears() > other.getNumYears()) {
      return 1;
    }
    return getMarketDataKey().compareTo(other.getMarketDataKey());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStrip) {
      FixedIncomeStrip other = (FixedIncomeStrip) obj;
      return CompareUtils.closeEquals(_numYears, other._numYears) &&
        ObjectUtils.equals(_marketDataKey, other._marketDataKey);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    long numYearsBits = Double.doubleToLongBits(getNumYears());
    result = (result * prime) + ((int) (numYearsBits ^ (numYearsBits >>> 32)));
    result = (result * prime) + getMarketDataKey().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
