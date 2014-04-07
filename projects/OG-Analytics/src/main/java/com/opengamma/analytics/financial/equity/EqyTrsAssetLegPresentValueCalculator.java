/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of the asset leg of an equity total return swap.
 */
public final class EqyTrsAssetLegPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyAmount> {
  /** The singleton instance */
  private static final EqyTrsAssetLegPresentValueCalculator INSTANCE = new EqyTrsAssetLegPresentValueCalculator();

  /**
   * Gets the instance.
   * @return The instance
   */
  public static EqyTrsAssetLegPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private EqyTrsAssetLegPresentValueCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitEquityTotalReturnSwap(final EquityTotalReturnSwap equityTrs, final EquityTrsDataBundle data) {
    ArgumentChecker.notNull(equityTrs, "equityTrs");
    ArgumentChecker.notNull(data, "data");
    return MultipleCurrencyAmount.of(CurrencyAmount.of(equityTrs.getNotionalCurrency(), data.getSpotEquity() * equityTrs.getEquity().getNumberOfShares()));
  }
}
