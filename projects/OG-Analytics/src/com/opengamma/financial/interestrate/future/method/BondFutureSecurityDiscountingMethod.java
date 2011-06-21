/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;

/**
 * Method to compute the price of bond future as the cheapest forward.
 */
public class BondFutureSecurityDiscountingMethod {

  private static final BondSecurityDiscountingMethod BOND_METHOD = new BondSecurityDiscountingMethod();

  /**
   * Computes the future price from the curves used to price the underlying bonds.
   * @param future The future security.
   * @param curves The curves.
   * @return The future price.
   */
  public double priceFromCurves(final BondFutureSecurity future, final YieldCurveBundle curves) {
    return priceFromCurvesAndNetBasis(future, curves, 0.0);
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and the net basis.
   * @param future The future security.
   * @param curves The curves.
   * @param netBasis The net basis associated to the future.
   * @return The future price.
   */
  public double priceFromCurvesAndNetBasis(final BondFutureSecurity future, final YieldCurveBundle curves, final double netBasis) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final double[] priceFromBond = new double[future.getDeliveryBasket().length];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves) - netBasis) / future.getConversionFactor()[loopbasket];
    }
    Min minFunction = new Min();
    double priceFuture = minFunction.evaluate(priceFromBond);
    return priceFuture;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from their clean prices.
   * @param future The future security.
   * @param cleanPrices The clean prices (at standard bond market spot date) of the bond in the basket.
   * @param futurePrice The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromPrices(final BondFutureSecurity future, final double[] cleanPrices, final double futurePrice) {
    int nbBasket = future.getDeliveryBasket().length;
    Validate.isTrue(cleanPrices.length == nbBasket, "Number of clean prices");
    double[] grossBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      grossBasis[loopbasket] = cleanPrices[loopbasket] - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return grossBasis;
  }

  /**
   * Computes the net basis of the bonds in the underlying basket from the curves and the future price.
   * @param future The future security.
   * @param curves The curves.
   * @param futurePrice The future price.
   * @return The net basis for each bond in the basket.
   */
  public double[] netBasisFromCurves(final BondFutureSecurity future, final YieldCurveBundle curves, final double futurePrice) {
    int nbBasket = future.getDeliveryBasket().length;
    final double[] bondDirtyPrice = new double[nbBasket];
    double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      bondDirtyPrice[loopbasket] = BOND_METHOD.dirtyPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves);
      netBasis[loopbasket] = bondDirtyPrice[loopbasket] - (futurePrice * future.getConversionFactor()[loopbasket] + future.getDeliveryBasket()[loopbasket].getAccruedInterest());
    }
    return netBasis;
  }

}
