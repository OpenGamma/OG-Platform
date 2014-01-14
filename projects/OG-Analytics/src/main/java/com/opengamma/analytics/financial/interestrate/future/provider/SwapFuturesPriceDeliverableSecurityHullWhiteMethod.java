/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price for an deliverable swap futures with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Deliverable Interest Rate Swap Futures: pricing in Gaussian HJM model, September 2012.
 */
public final class SwapFuturesPriceDeliverableSecurityHullWhiteMethod extends FuturesSecurityHullWhiteMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod INSTANCE = new SwapFuturesPriceDeliverableSecurityHullWhiteMethod();

  /**
   * Constructor.
   */
  private SwapFuturesPriceDeliverableSecurityHullWhiteMethod() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwapFuturesPriceDeliverableSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The present value calculator by discounting.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  /**
   * Returns the convexity adjustment, i.e. the difference between the adjusted price and the present value of the underlying swap.
   * @param futures The swap futures.
   * @param hwMulticurves The multi-curve and parameters provider.
   * @return The adjustment.
   */
  public double convexityAdjustment(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(futures, "swap futures");
    ArgumentChecker.notNull(hwMulticurves, "parameter provider");
    MultipleCurrencyAmount pv = futures.getUnderlyingSwap().accept(PVDC, hwMulticurves.getMulticurveProvider());
    double price = price(futures, hwMulticurves);
    return price - (1.0d + pv.getAmount(futures.getCurrency()));
  }

}
