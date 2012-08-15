/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * A curve that behaves according to the ISDA standard for CDS pricing.
 * 
 * This curve is intended for use with {@link CDSApproxISDAMethod} in order
 * to produce numbers that match the ISDA standard pricing model for CDS. It
 * may be useful in other situations where ISDA standard discount factors
 * are assumed.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDACurve {
  
  private final String _name;
  
  private final double _offset;
  
  private final DoublesCurve _curve;
  
  private final double[] _shiftedTimePoints;
  
  public ISDACurve(final String name, final double[] xData, final double[] yData, final double offset) {
    
    _name = name;
    _offset = offset;
    
    _curve = InterpolatedDoublesCurve.fromSorted(xData, yData,
      new CombinedInterpolatorExtrapolator(
        new LinearInterpolator1D(),
        new FlatExtrapolator1D(),
        new FlatExtrapolator1D()));
    
    _shiftedTimePoints = new double[xData.length];
    
    for (int i = 0; i < xData.length; ++i) {
      _shiftedTimePoints[i] = xData[i] + _offset;
    }
  }

  public String getName() {
    return _name;
  }
  
  public double getInterestRate(final Double t) {
    return _curve.getYValue(t - _offset);
  }
  
  public double getDiscountFactor(final double t_) {
    
    double t = t_;// + 1.0/365.0;
    
    //System.out.println(_name + "(" + t + "): offset=" + _offset + ", t-offset=" + (t - _offset) + ", rate/pay=" + getInterestRate(t) + ", df/pay= " + Math.exp((_offset-t) * getInterestRate(t)) + ", rate/today=" + getInterestRate(0.0) + ", df/today=" + Math.exp(_offset * getInterestRate(0.0)));
    
    return Math.exp((_offset-t) * getInterestRate(t)) / Math.exp(_offset * getInterestRate(0.0));
  }
  
  public double[] getTimePoints() {
    return _shiftedTimePoints;
  }
}
