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
import com.opengamma.math.interpolation.Interpolator1DModel;

/**
 *
 */
public class CouponBondBootstrapZeroDiscountCurveModel implements DiscountCurveModel<FixedInterestRateInstrumentDefinition> {
  private final Map<Double, Interpolator1D<? extends Interpolator1DModel>> _interpolators;

  public CouponBondBootstrapZeroDiscountCurveModel(final Interpolator1D<? extends Interpolator1DModel> interpolator) {
    this(Collections.<Double, Interpolator1D<? extends Interpolator1DModel>>singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  public CouponBondBootstrapZeroDiscountCurveModel(final Map<Double, Interpolator1D<? extends Interpolator1DModel>> interpolators) {
    _interpolators = interpolators;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.model.interestrate.curve.DiscountCurveModel#getCurve(java.util.Set,
   * javax.time.calendar.ZonedDateTime)
   */
  @Override
  public YieldAndDiscountCurve getCurve(final Set<FixedInterestRateInstrumentDefinition> data, final ZonedDateTime date) {
    // TODO Auto-generated method stub
    return null;
  }

}
