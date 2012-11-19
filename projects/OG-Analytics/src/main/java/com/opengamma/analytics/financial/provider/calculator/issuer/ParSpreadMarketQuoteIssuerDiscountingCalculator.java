/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParSpreadMarketQuoteIssuerDiscountingCalculator extends AbstractInstrumentDerivativeVisitor<IssuerProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator INSTANCE = new ParSpreadMarketQuoteIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteIssuerDiscountingCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final ParSpreadMarketQuoteDiscountingCalculator PVDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final IssuerProviderInterface issuercurves) {
    try {
      return derivative.accept(this, issuercurves);
    } catch (Exception e) {
      return derivative.accept(PVDC, issuercurves.getMulticurveProvider());
    }
  }

  //     -----     Deposit     -----

  @Override
  public Double visitDepositCounterpart(final DepositCounterpart deposit, final IssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.parSpread(deposit, issuercurves);
  }

}
