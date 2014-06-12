/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the value delta (i.e. delta w.r.t the equity) of an equity total return swap.
 */
public final class EqyTrsValueDeltaCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyAmount> {
  /** The singleton instance */
  private static final EqyTrsValueDeltaCalculator INSTANCE = new EqyTrsValueDeltaCalculator();

  /**
   * Gets the instance.
   * @return The instance
   */
  public static EqyTrsValueDeltaCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private EqyTrsValueDeltaCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitEquityTotalReturnSwap(final EquityTotalReturnSwap equityTrs, final EquityTrsDataBundle data) {
    ArgumentChecker.notNull(equityTrs, "equityTrs");
    ArgumentChecker.notNull(data, "data");
    final CurrencyAmount equityValueDelta = CurrencyAmount.of(equityTrs.getNotionalCurrency(), -(data.getSpotEquity() * equityTrs.getEquity().getNumberOfShares()));
    return MultipleCurrencyAmount.of(equityValueDelta);
  }
}
