/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.Map;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;

/**
 * YieldCurvebundle with FX rate.
 */
public class YieldCurveWithFXBundle extends YieldCurveWithCcyBundle {

  /**
   * The forex exchange rates at the valuation date.
   */
  private final FXMatrix _fxRates;

  /**
   * Constructor.
   * @param fxRates The FXMatrix with the FX exchange rates.
   * @param curveCurrency A map linking each curve in the bundle to its currency.
   * @param bundle The yield curve bundle. A new bundle with a new map and the same elements is created.
   */
  public YieldCurveWithFXBundle(final FXMatrix fxRates, final Map<String, Currency> curveCurrency, final YieldCurveBundle bundle) {
    super(curveCurrency, bundle);
    _fxRates = fxRates;
  }

  /**
   * Constructor.
   * @param fxRates The FXMatrix with the FX exchange rates.
   * @param bundle The yield curve bundle. A new bundle with a new map and the same elements is created.
   */
  public YieldCurveWithFXBundle(final FXMatrix fxRates, final YieldCurveWithCcyBundle bundle) {
    super(bundle);
    _fxRates = fxRates;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same FXMatrix is used.
   * @return The bundle.
   */
  public YieldCurveWithFXBundle copy() {
    return new YieldCurveWithFXBundle(_fxRates, this);
  }

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _fxRates.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix.
   * @return The matrix.
   */
  public FXMatrix getFxRates() {
    return _fxRates;
  }

  // TODO: forward rate

  // public double getForwardFxRate(final Currency ccy1, final Currency ccy2, final double time){

}
