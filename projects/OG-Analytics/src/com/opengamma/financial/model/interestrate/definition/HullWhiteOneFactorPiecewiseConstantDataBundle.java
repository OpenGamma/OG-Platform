/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Class describing the data required to price interest rate derivatives with Hull-White one factor model (curves and parameters).
 */
public class HullWhiteOneFactorPiecewiseConstantDataBundle extends YieldCurveBundle {

  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;

  /**
   * Constructor from SABR parameters and curve bundle.
   * @param hullWhiteParameters The Hull-White model parameters.
   * @param curves Curve bundle.
   */
  public HullWhiteOneFactorPiecewiseConstantDataBundle(final HullWhiteOneFactorPiecewiseConstantParameters hullWhiteParameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(hullWhiteParameters, "SABR parameters");
    _parameters = hullWhiteParameters;
  }

  /**
   * Gets the _sabrParameter field.
   * @return The SABR parameters.
   */
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameter() {
    return _parameters;
  }

}
