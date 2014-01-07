/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the bond futures transaction results with the price computed as the cheapest forward.
 */
public final class BondFuturesTransactionHullWhiteMethod extends BondFuturesTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesTransactionHullWhiteMethod INSTANCE = new BondFuturesTransactionHullWhiteMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesTransactionHullWhiteMethod() {
  }

  /**
   * The method to compute bond security figures.
   */
  private static final BondFuturesSecurityHullWhiteMethod METHOD_FUTURES_SEC = BondFuturesSecurityHullWhiteMethod.getInstance();

  /**
   * Computes the present value of future from the curves using the Hull-White model and valuation the futures convexity and delivery option.
   * @param futures The bond futures.
   * @param hwIssuerMulticurves The curve and Hull-White parameters.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondFuturesTransaction futures, final HullWhiteIssuerProviderInterface hwIssuerMulticurves) {
    return presentValueFromPrice(futures, METHOD_FUTURES_SEC.price(futures.getUnderlyingFuture(), hwIssuerMulticurves));
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param futures The future.
   * @param hwIssuerMulticurves The curve and Hull-White parameters.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondFuturesTransaction futures, final HullWhiteIssuerProviderInterface hwIssuerMulticurves) {
    Currency ccy = futures.getUnderlyingFuture().getCurrency();
    final MulticurveSensitivity priceSensitivity = METHOD_FUTURES_SEC.priceCurveSensitivity(futures.getUnderlyingFuture(), hwIssuerMulticurves);
    final MultipleCurrencyMulticurveSensitivity transactionSensitivity = MultipleCurrencyMulticurveSensitivity.of(ccy,
        priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getNotional() * futures.getQuantity()));
    return transactionSensitivity;
  }

}
