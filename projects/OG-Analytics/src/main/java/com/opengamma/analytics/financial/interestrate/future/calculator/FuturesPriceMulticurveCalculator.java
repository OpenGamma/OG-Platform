/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve provider.
 */
public final class FuturesPriceMulticurveCalculator extends InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceMulticurveCalculator INSTANCE = new FuturesPriceMulticurveCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceMulticurveCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceMulticurveCalculator() {
  }

  /** Implementation note: The pricing of some futures is done by calling the PresentValueDiscountingCalculator on the underlying. 
   *    The present value calculator refers to the futures calculator, that creates a circular reference of static methods.              */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurve, "Multi-curves provider");
    final double forward = multicurve.getMulticurveProvider().getSimplyCompoundForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(),
        futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor());
    final double price = 1.0 - forward;
    return price;
  }

  @Override
  public Double visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "futures");
    ArgumentChecker.notNull(multicurve, "multi-curve provider");
    final IndexON index = futures.getIndex();
    final int nbFixing = futures.getFixingPeriodAccrualFactor().length;
    final double[] rates = new double[nbFixing];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      rates[loopfix] = multicurve.getMulticurveProvider().getSimplyCompoundForwardRate(index, futures.getFixingPeriodTime()[loopfix], futures.getFixingPeriodTime()[loopfix + 1],
          futures.getFixingPeriodAccrualFactor()[loopfix]);
    }
    double interest = futures.getAccruedInterest();
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      interest += rates[loopfix] * futures.getFixingPeriodAccrualFactor()[loopfix];
    }
    return 1.0 - interest / futures.getFixingTotalAccrualFactor();
  }

  @Override
  /**
   * The price is 1+underlying swap present value. 
   * There is no adjustment for margining and no correction for discounting between futures settlement and valuation date.
   * @param futures The futures security.
   * @param multicurve The multi-curve provider.
   * @return The price.
   */
  public Double visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "futures");
    ArgumentChecker.notNull(multicurve, "multi-curve provider");
    MultipleCurrencyAmount pv = futures.getUnderlyingSwap().accept(PVDC, multicurve.getMulticurveProvider());
    return 1.0d + pv.getAmount(futures.getCurrency());
  }

}
