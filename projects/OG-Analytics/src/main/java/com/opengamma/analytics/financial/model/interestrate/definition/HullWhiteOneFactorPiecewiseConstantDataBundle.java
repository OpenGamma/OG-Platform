/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Class describing the data required to price interest rate derivatives with Hull-White one factor model (curves and model parameters).
 */
public class HullWhiteOneFactorPiecewiseConstantDataBundle extends YieldCurveBundle {

  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;

  /**
   * Constructor from Hull-White parameters and curve bundle.
   * @param hullWhiteParameters The Hull-White model parameters.
   * @param curves Curve bundle.
   */
  public HullWhiteOneFactorPiecewiseConstantDataBundle(final HullWhiteOneFactorPiecewiseConstantParameters hullWhiteParameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(hullWhiteParameters, "Hull-White parameters");
    _parameters = hullWhiteParameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same HullWhiteOneFactorPiecewiseConstantParameters is used.
   * @return The bundle.
   */
  public HullWhiteOneFactorPiecewiseConstantDataBundle copy() {
    return new HullWhiteOneFactorPiecewiseConstantDataBundle(_parameters, this);
  }

  /**
   * Gets the Hull-White one factor parameters.
   * @return The parameters.
   */
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameter() {
    return _parameters;
  }

}
