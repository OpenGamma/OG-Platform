/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * 
 *
 * @author kirk
 */
public class FixedIncomeStrip implements Serializable, Comparable<FixedIncomeStrip> {
  
  private final double _numYears;
  private final Identifier _marketDataKey;
  
  public FixedIncomeStrip(double numYears, Identifier marketDataKey) {
    if(numYears < 0) {
      throw new IllegalArgumentException("Fixed income strips cannot be in the future.");
    }
    ArgumentChecker.checkNotNull(marketDataKey, "Market data key");
    _numYears = numYears;
    _marketDataKey = marketDataKey;
  }

  /**
   * @return the numYears
   */
  public double getNumYears() {
    return _numYears;
  }

  /**
   * @return the marketDataKey
   */
  public Identifier getMarketDataKey() {
    return _marketDataKey;
  }
  
  public ComputationTargetSpecification getMarketDataSpecification() {
    // REVIEW kirk 2009-12-30 -- We might want to cache this on construction if it's called a lot.
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, getMarketDataKey());
  }

  @Override
  public int compareTo(FixedIncomeStrip o) {
    if(getNumYears() < o.getNumYears()) {
      return -1;
    } else if (getNumYears() > o.getNumYears()) {
      return 1;
    }
    return getMarketDataKey().compareTo(o.getMarketDataKey());
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof FixedIncomeStrip)) {
      return false;
    }
    FixedIncomeStrip other = (FixedIncomeStrip) obj;
    if(!CompareUtils.closeEquals(_numYears, other._numYears)) {
      return false;
    }
    if(!ObjectUtils.equals(_marketDataKey, other._marketDataKey)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    long numYearsBits = Double.doubleToLongBits(getNumYears());
    result = (result * prime) + ((int)(numYearsBits ^ (numYearsBits >>> 32)));
    result = (result * prime) + getMarketDataKey().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
