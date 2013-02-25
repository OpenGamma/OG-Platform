/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RateFutureStrip {
  private final int _futureNumber;
  private final Period _startTenor;
  private final Period _futurePeriod;
  private final Period _underlyingPeriod;
  private final String _futureConvention;
  private final String _underlyingConvention;
  private final String _curveSpecificationName;

  /**
   * 
   */
  public RateFutureStrip(final int futureNumber, final Period startTenor, final Period futurePeriod, final Period underlyingPeriod, final String futureConvention,
      final String underlyingConvention, final String curveSpecificationName) {
    ArgumentChecker.notNegativeOrZero(futureNumber, "future number");
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(futurePeriod, "future period");
    ArgumentChecker.notNull(underlyingPeriod, "underlying period");
    ArgumentChecker.notNull(futureConvention, "future convention");
    ArgumentChecker.notNull(underlyingConvention, "underlying convention");
    ArgumentChecker.notNull(curveSpecificationName, "curve specification name");
    _futureNumber = futureNumber;
    _startTenor = startTenor;
    _futurePeriod = futurePeriod;
    _underlyingPeriod = underlyingPeriod;
    _futureConvention = futureConvention;
    _underlyingConvention = underlyingConvention;
    _curveSpecificationName = curveSpecificationName;
  }

  public int getFutureNumber() {
    return _futureNumber;
  }

  public Period getStartTenor() {
    return _startTenor;
  }

  public Period getFuturePeriod() {
    return _futurePeriod;
  }

  public Period getUnderlyingPeriod() {
    return _underlyingPeriod;
  }

  public String getFutureConvention() {
    return _futureConvention;
  }

  public String getUnderlyingConvention() {
    return _underlyingConvention;
  }

  public String getCurveSpecificationName() {
    return _curveSpecificationName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveSpecificationName.hashCode();
    result = prime * result + _futureConvention.hashCode();
    result = prime * result + _futureNumber;
    result = prime * result + _futurePeriod.hashCode();
    result = prime * result + _startTenor.hashCode();
    result = prime * result + _underlyingConvention.hashCode();
    result = prime * result + _underlyingPeriod.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RateFutureStrip)) {
      return false;
    }
    final RateFutureStrip other = (RateFutureStrip) obj;
    if (_futureNumber != other._futureNumber) {
      return false;
    }
    if (!ObjectUtils.equals(_futureConvention, other._futureConvention)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingConvention, other._underlyingConvention)) {
      return false;
    }
    if (!ObjectUtils.equals(_futurePeriod, other._futurePeriod)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingPeriod, other._underlyingPeriod)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveSpecificationName, other._curveSpecificationName)) {
      return false;
    }
    return true;
  }

}
