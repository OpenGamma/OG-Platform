/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the price of bond future as the cheapest forward.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.BondFutureDiscountingMethod}
 */
@Deprecated
public final class BondFutureDiscountingMethod extends BondFutureMethod {

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
   * The method to compute bond security figures.
   */
  private static final BondSecurityDiscountingMethod BOND_METHOD = BondSecurityDiscountingMethod.getInstance();

  private static final Min MIN_FUNCTION = new Min();

  /**
   * Computes the future price from the curves used to price the underlying bonds.
   * @param future The future security.
   * @param curves The curves.
   * @return The future price.
   */
  public double price(final BondFuture future, final YieldCurveBundle curves) {
    return priceFromNetBasis(future, curves, 0.0);
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and the net basis.
   * @param future The future security.
   * @param curves The curves.
   * @param netBasis The net basis associated to the future.
   * @return The future price.
   */
  public double priceFromNetBasis(final BondFuture future, final YieldCurveBundle curves, final double netBasis) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(curves, "Curves");
    final double[] priceFromBond = new double[future.getDeliveryBasket().length];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves) - netBasis) / future.getConversionFactor()[loopbasket];
    }
    final double priceFuture = MIN_FUNCTION.evaluate(priceFromBond);
    return priceFuture;
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated with the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFuture future, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(future, "Future");
    return presentValueFromPrice(future, price(future, curves));
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated with the instrument.
   * @param netBasis The net basis associated to the future.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromNetBasis(final BondFuture future, final YieldCurveBundle curves, final double netBasis) {
    ArgumentChecker.notNull(future, "Future");
    return presentValueFromPrice(future, priceFromNetBasis(future, curves, netBasis));
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof BondFuture, "Bond future transaction");
    return presentValue((BondFuture) instrument, curves);
  }

  /**
   * Computes the future price curve sensitivity.
   * @param future The future security.
   * @param curves The curves.
   * @return The curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final BondFuture future, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(curves, "Curves");
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
    result = result.multipliedBy(1.0 / future.getConversionFactor()[indexCTD]);
    return result;
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated.
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BondFuture future, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(future, "Future");
    final InterestRateCurveSensitivity priceSensitivity = priceCurveSensitivity(future, curves);
    final InterestRateCurveSensitivity transactionSensitivity = priceSensitivity.multipliedBy(future.getNotional());
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
    ArgumentChecker.isTrue(cleanPrices.length == nbBasket, "Number of clean prices");
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
   * Computes the net basis of all the bonds in the underlying basket from the curves and the future price.
   * @param future The future security.
   * @param curves The curves.
   * @param futurePrice The future price.
   * @return The net basis for each bond in the basket.
   */
  public double[] netBasisAllBonds(final BondFuture future, final YieldCurveBundle curves, final double futurePrice) {
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      netBasis[loopbasket] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves) - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return netBasis;
  }

  /**
   * Computes the net basis of associated to the cheapest to deliver bonds in the underlying basket from the curves and the future price.
   * @param future The future security.
   * @param curves The curves.
   * @param futurePrice The future price.
   * @return The net basis.
   */
  public double netBasisCheapest(final BondFuture future, final YieldCurveBundle curves, final double futurePrice) {
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      netBasis[loopbasket] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], curves) - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return MIN_FUNCTION.evaluate(netBasis);
  }

}
