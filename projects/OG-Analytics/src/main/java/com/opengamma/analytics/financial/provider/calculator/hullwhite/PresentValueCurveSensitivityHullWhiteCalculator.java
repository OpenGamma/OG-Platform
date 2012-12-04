/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborHullWhiteMethod;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivityHullWhiteCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<HullWhiteOneFactorProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityHullWhiteCalculator INSTANCE = new PresentValueCurveSensitivityHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityHullWhiteCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_IRFUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_SWPT_PHYS = SwaptionPhysicalFixedIborHullWhiteMethod.getInstance();
  private static final CapFloorIborHullWhiteMethod METHOD_CAPFLOOR_IBOR = CapFloorIborHullWhiteMethod.getInstance();

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface hullWhite) {
    return derivative.accept(this, hullWhite);
  }

  //     -----     Payment/Coupon     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCapFloorIbor(final CapFloorIbor cap, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_CAPFLOOR_IBOR.presentValueCurveSensitivity(cap, hullWhite);
  }

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitInterestRateFuture(final InterestRateFuture future, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_IRFUT.presentValueCurveSensitivity(future, hullWhite);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWPT_PHYS.presentValueCurveSensitivity(swaption, hullWhite);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
