/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of a YieldAndDiscountCurve where the curve is stored as simple rates. 
 * The discount factors are computed as <i>DF = 1 / (1 + r * t)</i> where <i>t</i> is the time and <i>r</i> is the 
 * simple rate.
 */
public class YieldSimpleCurve extends YieldAndDiscountCurve {

  /** The curve storing the required data in the simple rate convention. */
  private final DoublesCurve _curve;
  
  /**
   * Constructor.
   * @param name The curve name.
   * @param curve The doubles curve providing the rate in the simple rate convention.
   */
  public YieldSimpleCurve(String name, DoublesCurve curve) {
    super(name);
    ArgumentChecker.notNull(curve, "Curve");
    _curve = curve;
  }
  
  @Override
  public double getDiscountFactor(final double t) {
    return 1.0d / (1.0d + t * _curve.getYValue(t));
  }

  @Override
  public double getInterestRate(Double x) {
    if (Math.abs(x) < 1.0E-10) {
      return _curve.getYValue(x); // First order approximation for very short times.
    }
    return -Math.log(getDiscountFactor(x)) / x;
  }

  @Override
  public double getForwardRate(double t) {
    throw new NotImplementedException("Forward instantaneous rate not implemented for simple rate curves");
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double time) {
    double df = getDiscountFactor(time);
    double rsBar = df;
    Double[] drsdp = _curve.getYValueParameterSensitivity(time);
    final double[] pBar = new double[drsdp.length];
    for (int loopp = 0; loopp < drsdp.length; loopp++) {
      pBar[loopp] = drsdp[loopp] * rsBar;
    }
    return pBar;
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.size();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final YieldSimpleCurve other = (YieldSimpleCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
