/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositCounterpartDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an ...
 */
public final class PresentValueIssuerCalculator extends AbstractInstrumentDerivativeVisitor<IssuerProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueIssuerCalculator INSTANCE = new PresentValueIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueIssuerCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final DepositCounterpartDiscountingMethod METHOD_DEPO_CTPY = DepositCounterpartDiscountingMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final IssuerProviderInterface issuercurves) {
    try {
      return derivative.accept(this, issuercurves);
    } catch (Exception e) {
      return derivative.accept(PVDC, issuercurves.getMulticurveProvider());
    }
  }

  @Override
  public MultipleCurrencyAmount visitDepositCounterpart(final DepositCounterpart deposit, final IssuerProviderInterface issuercurves) {
    return METHOD_DEPO_CTPY.presentValue(deposit, issuercurves);
  }

}
