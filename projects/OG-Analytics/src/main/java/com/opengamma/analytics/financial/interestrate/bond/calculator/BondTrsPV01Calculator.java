/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the PV01s for a bond total return swap. 
 */
public final class BondTrsPV01Calculator extends InstrumentDerivativeVisitorAdapter<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> {
  /** A singleton instance */
  private static final BondTrsPV01Calculator INSTANCE = new BondTrsPV01Calculator();
  /** The PV01 calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityIssuerCalculator.getInstance());

  /**
   * Gets the singleton instance.
   * @return The singleton instance
   */
  public static BondTrsPV01Calculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private BondTrsPV01Calculator() {
  }

  @Override
  public ReferenceAmount<Pair<String, Currency>> visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final ParameterIssuerProviderInterface data) {
    ArgumentChecker.notNull(bondTrs, "bondTrs");
    ArgumentChecker.notNull(data, "data");
    final ReferenceAmount<Pair<String, Currency>> fundingLegPV01 = bondTrs.getFundingLeg().accept(CALCULATOR, data);
    final ReferenceAmount<Pair<String, Currency>> bondPV01 = bondTrs.getAsset().accept(CALCULATOR, data);
    return fundingLegPV01.plus(bondPV01);
  }
}
