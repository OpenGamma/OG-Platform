/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.yc;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.util.CompareUtils;

/**
 * Represents a particular strip used to bootstrap a discount curve.
 *
 * @author kirk
 */
public class FixedIncomeStrip implements Serializable {
  private final double _numYears;
  private final AnalyticValueDefinition<?> _stripValueDefinition;
  
  public FixedIncomeStrip(double numYears, AnalyticValueDefinition<?> stripValueDefinition) {
    _numYears = numYears;
    _stripValueDefinition = stripValueDefinition;
  }

  /**
   * @return the numYears
   */
  public double getNumYears() {
    return _numYears;
  }

  /**
   * @return the stripValueDefinition
   */
  public AnalyticValueDefinition<?> getStripValueDefinition() {
    return _stripValueDefinition;
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
    if(!CompareUtils.closeEquals(getNumYears(), other.getNumYears())) {
      return false;
    }
    if(!ObjectUtils.equals(getStripValueDefinition(), other.getStripValueDefinition())) {
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
    result = (result * prime) + getStripValueDefinition().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
