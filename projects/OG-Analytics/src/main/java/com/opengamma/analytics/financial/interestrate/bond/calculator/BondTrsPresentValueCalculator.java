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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public final class BondTrsPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, MultipleCurrencyAmount> {
  private static final BondTrsPresentValueCalculator INSTANCE = new BondTrsPresentValueCalculator();

  public static BondTrsPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private BondTrsPresentValueCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final IssuerProviderInterface data) {
    ArgumentChecker.notNull(bondTrs, "equityTrs");
    ArgumentChecker.notNull(data, "data");
    final MultipleCurrencyAmount fundingLegPV = bondTrs.getFundingLeg().accept(PresentValueIssuerCalculator.getInstance(), data);
    final MultipleCurrencyAmount bondPV = bondTrs.getAsset().accept(PresentValueIssuerCalculator.getInstance(), data);
    return fundingLegPV.plus(bondPV.multipliedBy(-1));
  }
}
