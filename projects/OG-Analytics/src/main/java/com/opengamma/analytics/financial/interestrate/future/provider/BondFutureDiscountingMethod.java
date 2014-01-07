/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price of bond future as the cheapest forward.
 * @deprecated Use the {@link BondFuturesTransactionDiscountingMethod}.
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
  /**
   * Method used to compute the minimum of an array.
   */
  private static final Min MIN_FUNCTION = new Min();

  /**
   * Computes the future price from the curves used to price the underlying bonds.
   * @param future The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The future price.
   */
  public double price(final BondFuture future, final IssuerProviderInterface issuerMulticurves) {
    return priceFromNetBasis(future, issuerMulticurves, 0.0);
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and the net basis.
   * @param future The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param netBasis The net basis associated to the future.
   * @return The future price.
   */
  public double priceFromNetBasis(final BondFuture future, final IssuerProviderInterface issuerMulticurves, final double netBasis) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[future.getDeliveryBasket().length];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], issuerMulticurves) - netBasis) / future.getConversionFactor()[loopbasket];
    }
    final double priceFuture = MIN_FUNCTION.evaluate(priceFromBond);
    return priceFuture;
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondFuture future, final IssuerProviderInterface issuerMulticurves) {
    return presentValueFromPrice(future, price(future, issuerMulticurves));
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param netBasis The net basis associated to the future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromNetBasis(final BondFuture future, final IssuerProviderInterface issuerMulticurves, final double netBasis) {
    return presentValueFromPrice(future, priceFromNetBasis(future, issuerMulticurves, netBasis));
  }

  /**
   * Computes the future price curve sensitivity.
   * @param future The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFuture future, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[future.getDeliveryBasket().length];
    int indexCTD = 0;
    double priceMin = 2.0;
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], issuerMulticurves)) / future.getConversionFactor()[loopbasket];
      if (priceFromBond[loopbasket] < priceMin) {
        priceMin = priceFromBond[loopbasket];
        indexCTD = loopbasket;
      }
    }
    MulticurveSensitivity result = BOND_METHOD.dirtyPriceCurveSensitivity(future.getDeliveryBasket()[indexCTD], issuerMulticurves);
    return result.multipliedBy(1.0 / future.getConversionFactor()[indexCTD]);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondFuture future, final IssuerProviderInterface issuerMulticurves) {
    Currency ccy = future.getCurrency();
    final MulticurveSensitivity priceSensitivity = priceCurveSensitivity(future, issuerMulticurves);
    final MultipleCurrencyMulticurveSensitivity transactionSensitivity = MultipleCurrencyMulticurveSensitivity.of(ccy, priceSensitivity.multipliedBy(future.getNotional()));
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
    ArgumentChecker.notNull(future, "Future");
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
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param futurePrice The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromCurves(final BondFuture future, final IssuerProviderInterface issuerMulticurves, final double futurePrice) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] grossBasis = new double[nbBasket];
    final double[] cleanPrices = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      cleanPrices[loopbasket] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], issuerMulticurves);
      grossBasis[loopbasket] = cleanPrices[loopbasket] - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return grossBasis;

  }

  /**
   * Computes the net basis of all the bonds in the underlying basket from the curves and the future price.
   * @param future The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param futurePrice The future price.
   * @return The net basis for each bond in the basket.
   */
  public double[] netBasisAllBonds(final BondFuture future, final IssuerProviderInterface issuerMulticurves, final double futurePrice) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      netBasis[loopbasket] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], issuerMulticurves) - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return netBasis;
  }

  /**
   * Computes the net basis of associated to the cheapest to deliver bonds in the underlying basket from the curves and the future price.
   * @param future The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param futurePrice The future price.
   * @return The net basis.
   */
  public double netBasisCheapest(final BondFuture future, final IssuerProviderInterface issuerMulticurves, final double futurePrice) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = future.getDeliveryBasket().length;
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < future.getDeliveryBasket().length; loopbasket++) {
      netBasis[loopbasket] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasket()[loopbasket], issuerMulticurves) - futurePrice * future.getConversionFactor()[loopbasket];
    }
    return MIN_FUNCTION.evaluate(netBasis);
  }

}
