/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Class describing the data required to price interest rate derivatives with LMM displaced diffusion (curves and parameters).
 */
public class LiborMarketModelDisplacedDiffusionDataBundle extends YieldCurveBundle {

  /**
   * The LMM parameters.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _parameters;

  /**
   * Constructor from LMM parameters and curve bundle.
   * @param lmmParameters The LMM model parameters.
   * @param curves Curve bundle.
   */
  public LiborMarketModelDisplacedDiffusionDataBundle(final LiborMarketModelDisplacedDiffusionParameters lmmParameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(lmmParameters, "LMM parameters");
    _parameters = lmmParameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same LiborMarketModelDisplacedDiffusionParameters is used.
   * @return The bundle.
   */
  public LiborMarketModelDisplacedDiffusionDataBundle copy() {
    return new LiborMarketModelDisplacedDiffusionDataBundle(_parameters, this);
  }

  /**
   * Gets the G2++ parameters.
   * @return The parameters.
   */
  public LiborMarketModelDisplacedDiffusionParameters getLmmParameter() {
    return _parameters;
  }

}
