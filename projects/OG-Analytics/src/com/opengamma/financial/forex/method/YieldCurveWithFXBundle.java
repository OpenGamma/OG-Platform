/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;

/**
 * YieldCurvebundle with FX rate.
 */
public class YieldCurveWithFXBundle extends YieldCurveBundle {

  /**
   * The forex exchange rates at the valuation date.
   */
  private final FXMatrix _fxRates;

  /**
   * Constructor.
   * @param fxRates The FXMatrix with the FX exchange rates.
   * @param bundle The yield curve bundle.
   */
  public YieldCurveWithFXBundle(final FXMatrix fxRates, final YieldCurveBundle bundle) {
    super(bundle);
    _fxRates = fxRates;
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

  // TODO: forward rate

  // public double getForwardFxRate(final Currency ccy1, final Currency ccy2, final double time){

}
