/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import org.apache.commons.math.stat.descriptive.moment.Mean;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.YieldAverageBondFuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPriceIssuerCalculator extends InstrumentDerivativeVisitorAdapter<ParameterIssuerProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceIssuerCalculator INSTANCE = new FuturesPriceIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceIssuerCalculator() {
  }

  /** Method used to compute bond yield **/
  private static final BondSecurityDiscountingMethod METHOD_BND = BondSecurityDiscountingMethod.getInstance();
  /** Function to compute average of arrays **/
  private static final Mean MEAN_FUNCTION = new Mean();

  //     -----     Futures     -----

  @Override
  public Double visitYieldAverageBondFuturesSecurity(final YieldAverageBondFuturesSecurity futures, final ParameterIssuerProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "futures");
    ArgumentChecker.notNull(futures, "multi-curve provider");
    final int nbBonds = futures.getDeliveryBasketAtDeliveryDate().length;
    // Yield at theoretical delivery for each bond
    final double[] yield = new double[nbBonds];
    for (int loopbond = 0; loopbond < nbBonds; loopbond++) {
      yield[loopbond] = METHOD_BND.yieldFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbond], multicurve.getIssuerProvider());
    }
    final double yieldAverage = MEAN_FUNCTION.evaluate(yield); // Average yield
    final double price = 1.0d - yieldAverage;
    return price;
  }

}
