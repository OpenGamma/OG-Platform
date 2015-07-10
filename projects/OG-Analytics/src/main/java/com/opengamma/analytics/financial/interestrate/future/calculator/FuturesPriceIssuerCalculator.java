/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
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
  /** Function to compute average of arrays **/
  private static final Min MIN_FUNCTION = new Min();

  //     -----     Futures     -----

  @Override
  public Double visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity futures, final ParameterIssuerProviderInterface multicurve) {
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

  @Override
  public Double visitBondFuturesSecurity(final BondFuturesSecurity futures, final ParameterIssuerProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(multicurve, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[futures.getDeliveryBasketAtDeliveryDate().length];
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      priceFromBond[loopbasket] = METHOD_BND.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbasket], multicurve.getIssuerProvider())
          / futures.getConversionFactor()[loopbasket];
    }
    final double priceFuture = MIN_FUNCTION.evaluate(priceFromBond);
    return priceFuture;
  }

}
