/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPriceCurveSensitivityIssuerCalculator extends InstrumentDerivativeVisitorAdapter<ParameterIssuerProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceCurveSensitivityIssuerCalculator INSTANCE = new FuturesPriceCurveSensitivityIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityIssuerCalculator() {
  }

  /** Method used to compute bond yield **/
  private static final BondSecurityDiscountingMethod METHOD_BND = BondSecurityDiscountingMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitBondFuturesSecurity(final BondFuturesSecurity futures, final ParameterIssuerProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(multicurve, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[futures.getDeliveryBasketAtDeliveryDate().length];
    int indexCTD = 0;
    double priceMin = 2.0;
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      priceFromBond[loopbasket] = (METHOD_BND.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbasket], multicurve.getIssuerProvider())) / futures.getConversionFactor()[loopbasket];
      if (priceFromBond[loopbasket] < priceMin) {
        priceMin = priceFromBond[loopbasket];
        indexCTD = loopbasket;
      }
    }
    final MulticurveSensitivity result = METHOD_BND.dirtyPriceCurveSensitivity(futures.getDeliveryBasketAtDeliveryDate()[indexCTD], multicurve.getIssuerProvider());
    return result.multipliedBy(1.0 / futures.getConversionFactor()[indexCTD]);
  }

}
