/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of a bond total return swap. The value is returned in the currency 
 * of the asset.
 * @deprecated Use the standard issuer calculator {@link PresentValueIssuerCalculator}.
 */
@Deprecated
public final class BondTrsPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, MultipleCurrencyAmount> {
  /** A singleton instance */
  private static final BondTrsPresentValueCalculator INSTANCE = new BondTrsPresentValueCalculator();

  /**
   * Gets the singleton instance.
   * @return The singleton instance
   */
  public static BondTrsPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private BondTrsPresentValueCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final IssuerProviderInterface data) {
    ArgumentChecker.notNull(bondTrs, "bondTrs");
    ArgumentChecker.notNull(data, "data");
    final MultipleCurrencyAmount fundingLegPV = bondTrs.getFundingLeg().accept(PresentValueIssuerCalculator.getInstance(), data);
    final Currency fundingCurrency = bondTrs.getFundingLeg().getCurrency();
    final MultipleCurrencyAmount bondPV = bondTrs.getAsset().accept(PresentValueIssuerCalculator.getInstance(), data);
    final Currency bondCurrency = bondTrs.getAsset().getCurrency();
    final double fxRate = data.getMulticurveProvider().getFxRate(bondCurrency, fundingCurrency);
    return bondPV.plus(CurrencyAmount.of(bondCurrency, -fundingLegPV.getAmount(fundingCurrency) * fxRate));
  }
}
