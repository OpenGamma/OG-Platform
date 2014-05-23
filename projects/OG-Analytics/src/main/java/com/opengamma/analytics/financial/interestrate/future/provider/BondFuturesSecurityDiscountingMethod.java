/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Method to compute the bond futures security results with the price computed as the cheapest forward.
 */
public final class BondFuturesSecurityDiscountingMethod extends FuturesSecurityIssuerMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesSecurityDiscountingMethod INSTANCE = new BondFuturesSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesSecurityDiscountingMethod() {
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
   * Computes the futures price from the curves used to price the underlying bonds and the net basis.
   * @param futures The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param netBasis The net basis associated to the future.
   * @return The future price.
   */
  public double priceFromNetBasis(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves, final double netBasis) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[futures.getDeliveryBasketAtDeliveryDate().length];
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      priceFromBond[loopbasket] = (BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbasket], issuerMulticurves) - netBasis) / futures.getConversionFactor()[loopbasket];
    }
    final double priceFuture = MIN_FUNCTION.evaluate(priceFromBond);
    return priceFuture;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from their clean prices.
   * @param futures The future security.
   * @param cleanPrices The clean prices (at standard bond market spot date) of the bond in the basket.
   * @param futurePrice The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromPrices(final BondFuturesSecurity futures, final double[] cleanPrices, final double futurePrice) {
    ArgumentChecker.notNull(futures, "futures");
    ArgumentChecker.notNull(cleanPrices, "cleanPrices");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    ArgumentChecker.isTrue(cleanPrices.length == nbBasket, "Number of clean prices");
    final double[] grossBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      grossBasis[loopbasket] = cleanPrices[loopbasket] - futurePrice * futures.getConversionFactor()[loopbasket];
    }
    return grossBasis;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from the curves.
   * @param futures The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param futurePrice The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromCurves(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves, final double futurePrice) {
    ArgumentChecker.notNull(futures, "future");
    ArgumentChecker.notNull(issuerMulticurves, "issuerMulticurves");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    final double[] grossBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      final double cleanPrice = BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtSpotDate()[loopbasket], issuerMulticurves);
      grossBasis[loopbasket] = cleanPrice - futurePrice * futures.getConversionFactor()[loopbasket];
    }
    return grossBasis;
  }

  /**
   * Computes the net basis of all the bonds in the underlying basket from the curves and the future price.
   * @param futures The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param futurePrice The future price.
   * @return The net basis for each bond in the basket.
   */
  public double[] netBasisAllBonds(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves, final double futurePrice) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      final double cleanPrice = BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbasket], issuerMulticurves);
      netBasis[loopbasket] = cleanPrice - futurePrice * futures.getConversionFactor()[loopbasket];
    }
    return netBasis;
  }

  /**
   * Computes the net basis of associated to the cheapest to deliver bonds in the underlying basket from the curves and the future price.
   * @param futures The future security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param futurePrice The future price.
   * @return The net basis.
   */
  public double netBasisCheapest(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves, final double futurePrice) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    final double[] netBasis = new double[nbBasket];
    for (int loopbasket = 0; loopbasket < futures.getDeliveryBasketAtDeliveryDate().length; loopbasket++) {
      netBasis[loopbasket] = BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbasket], issuerMulticurves) - futurePrice * futures.getConversionFactor()[loopbasket];
    }
    return MIN_FUNCTION.evaluate(netBasis);
  }

}
