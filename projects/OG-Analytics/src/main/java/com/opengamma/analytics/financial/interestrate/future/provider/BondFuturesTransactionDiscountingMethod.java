/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the bond futures transaction results with the price computed as the cheapest forward.
 */
public final class BondFuturesTransactionDiscountingMethod extends BondFuturesTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesTransactionDiscountingMethod INSTANCE = new BondFuturesTransactionDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesTransactionDiscountingMethod() {
  }

  /**
   * The method to compute bond security figures.
   */
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURES_SEC = BondFuturesSecurityDiscountingMethod.getInstance();

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param futures The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondFuturesTransaction futures, final IssuerProviderInterface issuerMulticurves) {
    return presentValueFromPrice(futures, METHOD_FUTURES_SEC.price(futures.getUnderlyingFuture(), issuerMulticurves));
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param futures The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param netBasis The net basis associated to the future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromNetBasis(final BondFuturesTransaction futures, final IssuerProviderInterface issuerMulticurves, final double netBasis) {
    return presentValueFromPrice(futures, METHOD_FUTURES_SEC.priceFromNetBasis(futures.getUnderlyingFuture(), issuerMulticurves, netBasis));
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param futures The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondFuturesTransaction futures, final IssuerProviderInterface issuerMulticurves) {
    Currency ccy = futures.getUnderlyingFuture().getCurrency();
    final MulticurveSensitivity priceSensitivity = METHOD_FUTURES_SEC.priceCurveSensitivity(futures.getUnderlyingFuture(), issuerMulticurves);
    final MultipleCurrencyMulticurveSensitivity transactionSensitivity = MultipleCurrencyMulticurveSensitivity.of(ccy,
        priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getNotional() * futures.getQuantity()));
    return transactionSensitivity;
  }

}
