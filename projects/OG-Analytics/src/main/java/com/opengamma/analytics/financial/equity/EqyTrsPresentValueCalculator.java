/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.util.ArgumentChecker;
<<<<<<< HEAD
import com.opengamma.util.money.Currency;
=======
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an equity total return swap.
 */
public final class EqyTrsPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyAmount> {
  /** The singleton instance */
  private static final EqyTrsPresentValueCalculator INSTANCE = new EqyTrsPresentValueCalculator();

  /**
   * Gets the instance.
   * @return The instance
   */
  public static EqyTrsPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private EqyTrsPresentValueCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitEquityTotalReturnSwap(final EquityTotalReturnSwap equityTrs, final EquityTrsDataBundle data) {
    ArgumentChecker.notNull(equityTrs, "equityTrs");
    ArgumentChecker.notNull(data, "data");
    final MultipleCurrencyAmount fundingLegPV = equityTrs.getFundingLeg().accept(PresentValueDiscountingCalculator.getInstance(), data.getCurves());
<<<<<<< HEAD
<<<<<<< HEAD
    final Currency fundingCurrency = equityTrs.getFundingLeg().getCurrency();
    final CurrencyAmount equityPV = CurrencyAmount.of(equityTrs.getNotionalCurrency(), data.getSpotEquity() * equityTrs.getEquity().getNumberOfShares());
    final Currency equityCurrency = equityTrs.getEquity().getCurrency();
    final double fxRate = data.getCurves().getFxRate(equityCurrency, fundingCurrency);
    return MultipleCurrencyAmount.of(equityPV.plus(CurrencyAmount.of(equityCurrency, -fundingLegPV.getAmount(fundingCurrency) * fxRate)));
=======
    final CurrencyAmount equityPV = CurrencyAmount.of(equityTrs.getNotionalCurrency(), data.getSpotEquity() * equityTrs.getEquity().getNumberOfShares());
    return fundingLegPV.plus(equityPV);
>>>>>>> a8c2f08... Revert "Revert "[PLAT-5345] Adding bond TRS analytics to examples-simulated""
=======
    final CurrencyAmount equityPV = CurrencyAmount.of(equityTrs.getNotionalCurrency(), -(data.getSpotEquity() * equityTrs.getEquity().getNumberOfShares()));
    return fundingLegPV.plus(equityPV);
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
  }
}
