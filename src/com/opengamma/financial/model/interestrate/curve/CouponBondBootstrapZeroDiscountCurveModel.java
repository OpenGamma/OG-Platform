/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.definition.FixedInterestRateInstrumentDefinition;
import com.opengamma.math.interpolation.Interpolator1D;

/**
 * @author emcleod
 *
 */
public class CouponBondBootstrapZeroDiscountCurveModel implements DiscountCurveModel<FixedInterestRateInstrumentDefinition> {
  private final Map<Double, Interpolator1D> _interpolators;

  public CouponBondBootstrapZeroDiscountCurveModel(final Interpolator1D interpolator) {
    this(Collections.<Double, Interpolator1D> singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  public CouponBondBootstrapZeroDiscountCurveModel(final Map<Double, Interpolator1D> interpolators) {
    _interpolators = interpolators;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.model.interestrate.curve.DiscountCurveModel#getCurve(java.util.Set,
   * javax.time.calendar.ZonedDateTime)
   */
  @Override
  public DiscountCurve getCurve(final Set<FixedInterestRateInstrumentDefinition> data, final ZonedDateTime date) {
    // TODO Auto-generated method stub
    return null;
  }

}
