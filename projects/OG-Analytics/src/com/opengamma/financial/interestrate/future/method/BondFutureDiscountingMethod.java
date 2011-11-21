/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.util.money.CurrencyAmount;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.stat.descriptive.rank.Min;

/**
 * Method to compute the price of bond future as the cheapest forward.
 */
public final class BondFutureDiscountingMethod extends BondFutureMethod {

  /**
   * The method to compute bond security figures.
   */
  private static final BondSecurityDiscountingMethod BOND_METHOD = BondSecurityDiscountingMethod.getInstance();

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureDiscountingMethod INSTANCE = new BondFutureDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFutureDiscountingMethod() {
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds.
   * @param future The future security.
   * @param curves The curves.
   * @return The future price.
   */
  public double price(final BondFuture future, final YieldCurveBundle curves) {
    return priceNetBasis(future, curves, 0.0);
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and the net basis.
   * @param future The future security.
   * @param curves The curves.
   * @param netBasis The net basis associated to the future.
   * @return The future price.
   */
  public double priceNetBasis(final BondFuture future, final YieldCurveBundle curves, final double netBasis) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final double[] priceFromBond = new double[future.getDeliveryBasket().length];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves) - netBasis) / future.getConversionFactor()[loopbasket];
    }
    final Min minFunction = new Min();
    final double priceFuture = minFunction.evaluate(priceFromBond);
    return priceFuture;
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated with the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    final double pv = presentValueFromPrice(future, price(future, curves));
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BondFuture, "Bond future transaction");
    return presentValue((BondFuture) instrument, curves);
  }

  /**
   * Computes the future price curve sensitivity.
   * @param future The future security.
   * @param curves The curves.
   * @return The curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final double[] priceFromBond = new double[future.getDeliveryBasket().length];
    int indexCTD = 0;
    double priceMin = 2.0;
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves)) / future.getConversionFactor()[loopbasket];
      if (priceFromBond[loopbasket] < priceMin) {
        priceMin = priceFromBond[loopbasket];
        indexCTD = loopbasket;
      }
    }
    InterestRateCurveSensitivity result = BOND_METHOD.dirtyPriceCurveSensitivity(future.getDeliveryBasket()[indexCTD], curves);
    result = result.multiply(1.0 / future.getConversionFactor()[indexCTD]);
    return result;
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated. 
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BondFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateCurveSensitivity priceSensitivity = priceCurveSensitivity(future, curves);
    final InterestRateCurveSensitivity transactionSensitivity = priceSensitivity.multiply(future.getNotional());
    return transactionSensitivity;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from their clean prices.
   * @param future The future security.
   * @param cleanPrices The clean prices (at standard bond market spot date) of the bond in the basket.
   * @param futurePrice The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromPrices(final BondFuture future, final double[] cleanPrices, final double futurePrice) {
    final int nbBasket = future.getDeliveryBasket().length;
    Validate.isTrue(cleanPrices.length == nbBasket, "Number of clean prices");
    final double[] grossBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      grossBasis[loopbasket] = cleanPrices[loopbasket] - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return grossBasis;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from the curves.
   * @param future The future security.
   * @param curves The curves.
   * @param futurePrice The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromCurves(final BondFuture future, final YieldCurveBundle curves, final double futurePrice) {
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] grossBasis = new double[nbBasket];
    final double[] cleanPrices = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      cleanPrices[loopbasket] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves);
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
  public double[] netBasisFromCurves(final BondFuture future, final YieldCurveBundle curves, final double futurePrice) {
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] bondDirtyPrice = new double[nbBasket];
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      bondDirtyPrice[loopbasket] = BOND_METHOD.dirtyPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves);
      netBasis[loopbasket] = bondDirtyPrice[loopbasket] - (futurePrice * future.getConversionFactor()[loopbasket] + future.getDeliveryBasket()[loopbasket].getAccruedInterest());
    }
    return netBasis;
  }

}
