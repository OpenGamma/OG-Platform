/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the currency exposure by discounting with issuer specific curves.
 */
public final class EqyTrsCurrencyExposureCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final EqyTrsCurrencyExposureCalculator INSTANCE = new EqyTrsCurrencyExposureCalculator();

  private static final EquityTotalReturnSwapDiscountingMethod PV_CAL = EquityTotalReturnSwapDiscountingMethod.getInstance();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static EqyTrsCurrencyExposureCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private EqyTrsCurrencyExposureCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitEquityTotalReturnSwap(final EquityTotalReturnSwap trs, final EquityTrsDataBundle multicurve) {
    MultipleCurrencyAmount pv = PV_CAL.presentValue(trs, multicurve);

    FXMatrix fxMatrix = multicurve.getCurves().getFxRates();
    MultipleCurrencyAmount pvEquity = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(), multicurve.getSpotEquity() * trs.getEquity().getNumberOfShares());
    return pv.plus(fxMatrix.convert(pvEquity, trs.getEquity().getCurrency()));
  }
}
