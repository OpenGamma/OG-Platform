/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Class describing the data required to price interest rate derivatives with SABR (curves and parameters).
 */
public class SABRInterestRateDataBundle extends YieldCurveBundle {

  /**
   * The surfaces of SABR parameters.
   */
  private final SABRInterestRateParameter _sabrParameter;

  /**
   * Constructor from SABR parameters and curve bundle.
   * @param sabrParameter SABR parameters.
   * @param curves Curve bundle.
   */
  public SABRInterestRateDataBundle(SABRInterestRateParameter sabrParameter, YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(sabrParameter, "SABR parameters");
    Validate.notNull(curves, "curves");
    _sabrParameter = sabrParameter;
  }

  /**
   * Gets the _sabrParameter field.
   * @return The SABR parameters.
   */
  public SABRInterestRateParameter getSABRParameter() {
    return _sabrParameter;
  }

}
