/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * YieldCurvebundle with FX rate.
 */
public class YieldCurveWithFXBundle extends YieldCurveBundle {
  // TODO: Ad hoc data bundle for some FX products. Should be remove and changed to a generic bundle.

  /**
   * The forex exchange rate at the valuation date.
   */
  private final double _spot;

  /**
   * Constructor.
   * @param spot The forex exchange rate at the valuation date.
   * @param bundle The yield curve bundle.
   */
  public YieldCurveWithFXBundle(final double spot, final YieldCurveBundle bundle) {
    super(bundle);
    _spot = spot;
  }

  /**
   * Gets the forex exchange rate at the valuation date.
   * @return The rate.
   */
  public double getSpot() {
    return _spot;
  }

}
