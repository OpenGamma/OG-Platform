/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteProviderMethod;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueHullWhiteProviderCalculator extends AbstractInstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueHullWhiteProviderCalculator INSTANCE = new PresentValueHullWhiteProviderCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueHullWhiteProviderCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueHullWhiteProviderCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final PresentValueDiscountingProviderCalculator PVDC = PresentValueDiscountingProviderCalculator.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    try {
      return derivative.accept(this, multicurves);
    } catch (Exception e) {
      return derivative.accept(PVDC, multicurves.getMulticurveProvider());
    }
  }

  //  @Override
  //  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final HullWhiteOneFactorProviderInterface multicurves) {
  //    Validate.notNull(annuity);
  //    MultipleCurrencyAmount pv = MultipleCurrencyAmount.of(annuity.getCurrency(), 0.0);
  //    for (final Payment p : annuity.getPayments()) {
  //      pv = pv.plus(visit(p, multicurves));
  //    }
  //    return pv;
  //  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFuture(final InterestRateFuture future, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_IRFUT_HW.presentValue(future, multicurves);
  }

}
