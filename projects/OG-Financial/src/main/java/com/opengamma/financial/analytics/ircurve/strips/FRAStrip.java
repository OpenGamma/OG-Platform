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
public class FRAStrip {
  /** The fixing start tenor */
  private final Period _fixingStart;
  /** The fixing end tenor */
  private final Period _fixingEnd;
  /** The convention name */
  private final String _conventionName;
  /** The curve specification name */
  private final String _curveSpecificationName;

  /**
   * 
   */
  public FRAStrip(final Period fixingStart, final Period fixingEnd, final String conventionName, final String curveSpecificationName) {
    ArgumentChecker.notNull(fixingStart, "fixing start");
    ArgumentChecker.notNull(fixingEnd, "fixing end");
    ArgumentChecker.notNull(conventionName, "convention name");
    ArgumentChecker.notNull(curveSpecificationName, "curve specification name");
    _fixingStart = fixingStart;
    _fixingEnd = fixingEnd;
    _conventionName = conventionName;
    _curveSpecificationName = curveSpecificationName;
  }

  public Period getFixingPeriodStart() {
    return _fixingStart;
  }

  public Period getFixingPeriodEnd() {
    return _fixingEnd;
  }

  public String getConventionName() {
    return _conventionName;
  }

  public String getCurveSpecificationName() {
    return _curveSpecificationName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _conventionName.hashCode();
    result = prime * result + _curveSpecificationName.hashCode();
    result = prime * result + _fixingEnd.hashCode();
    result = prime * result + _fixingStart.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FRAStrip)) {
      return false;
    }
    final FRAStrip other = (FRAStrip) obj;
    if (!ObjectUtils.equals(_fixingStart, other._fixingStart)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingEnd, other._fixingEnd)) {
      return false;
    }
    if (!ObjectUtils.equals(_conventionName, other._conventionName)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveSpecificationName, other._curveSpecificationName)) {
      return false;
    }
    return true;
  }


}
