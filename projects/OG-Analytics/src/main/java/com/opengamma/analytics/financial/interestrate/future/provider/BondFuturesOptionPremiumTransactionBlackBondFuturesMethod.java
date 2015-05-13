/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of bond future options transactions with up-front premium. 
 * The pricing is done with a Black approach.
 * The future prices are computed without convexity adjustments and without delivery option.
 */
public final class BondFuturesOptionPremiumTransactionBlackBondFuturesMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesOptionPremiumTransactionBlackBondFuturesMethod INSTANCE = 
      new BondFuturesOptionPremiumTransactionBlackBondFuturesMethod();

  /**
   * Constructor.
   */
  private BondFuturesOptionPremiumTransactionBlackBondFuturesMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesOptionPremiumTransactionBlackBondFuturesMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The methods and calculators used.
   */
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_SECURITY = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = 
      PaymentFixedDiscountingMethod.getInstance();

  /**
   * Compute the present value of a bond future option transaction from the quoted option price.
   * @param option The future option, not null
   * @param multicurves The multi-curves provider.
   * @param price The quoted price of the option on futures security.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromOptionPrice(final BondFuturesOptionPremiumTransaction option, 
      MulticurveProviderInterface multicurves, final double price) {
    ArgumentChecker.notNull(option, "option");
    final Currency ccy = option.getCurrency();
    final MultipleCurrencyAmount premiumPV = METHOD_PAY_FIXED.presentValue(option.getPremium(), multicurves);
    final double optionPV = price * option.getQuantity() * option.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return premiumPV.plus(MultipleCurrencyAmount.of(ccy, optionPV));
  }

  /**
   * Computes the present value of a bond future option. The bond futures price is computed from the curves.
   * @param transaction The option transaction.
   * @param black The curve and Black volatility data, not null
   * @param price  the price of the underlying future
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromUnderlyingPrice(final BondFuturesOptionPremiumTransaction transaction, 
      BlackBondFuturesProviderInterface black, double price) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(black, "Black parameters and futures price");
    double optionPrice = METHOD_SECURITY.priceFromUnderlyingPrice(transaction.getUnderlyingOption(), black, price);
    MultipleCurrencyAmount pvTransaction = 
        presentValueFromOptionPrice(transaction, black.getMulticurveProvider(), optionPrice);
    return pvTransaction;
  }

  /**
   * Computes the present value of a bond future option. The bond futures price is computed from the curves.
   * @param transaction The option transaction.
   * @param black The curve and Black volatility data, not null
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondFuturesOptionPremiumTransaction transaction, 
      BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(black, "Black parameters and futures price");
    double priceSecurity = METHOD_SECURITY.price(transaction.getUnderlyingOption(), black);
    return presentValueFromOptionPrice(transaction, black.getMulticurveProvider(), priceSecurity);
  }

  /**
   * Computes the present value curve sensitivity of a transaction. The bond futures price is computed from the curves.
   * @param transaction The option transaction.
   * @param black The curve and Black volatility data, not null
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(
      BondFuturesOptionPremiumTransaction transaction, 
      BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(black, "Black parameters");
    MultipleCurrencyMulticurveSensitivity premiumSensitivity = 
        METHOD_PAY_FIXED.presentValueCurveSensitivity(transaction.getPremium(), black.getMulticurveProvider());
    MulticurveSensitivity securitySensitivity = 
        METHOD_SECURITY.priceCurveSensitivity(transaction.getUnderlyingOption(), black);
    return premiumSensitivity.plus(MultipleCurrencyMulticurveSensitivity.of(transaction.getCurrency(),
        securitySensitivity.multipliedBy(transaction.getQuantity() * 
            transaction.getUnderlyingOption().getUnderlyingFuture().getNotional())));
  }

  /**
   * Computes the present value delta of a transaction (i.e. the first order derivative of the present value with 
   * respect to the bond futures price).
   * @param transaction The option transaction.
   * @param black The curve and Black volatility data, not null
   * @return The delta.
   */
  public double presentValueDelta(
      BondFuturesOptionPremiumTransaction transaction, 
      BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(black, "Black parameters");
    final double securityDelta = METHOD_SECURITY.delta(transaction.getUnderlyingOption(), black);
    final double txnDelta = securityDelta * transaction.getQuantity() * 
        transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnDelta;
  }

  /**
   * Computes the present value gamma of a transaction (i.e. the second order derivative of the present value with 
   * respect to the bond futures price).
   * The bond futures price is computed from the curves.
   * @param transaction The option transaction.
   * @param black The curve and Black volatility data, not null
   * @return The gamma.
   */
  public double presentValueGamma(
      BondFuturesOptionPremiumTransaction transaction, 
      BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(black, "Black parameters");
    final double securityGamma = METHOD_SECURITY.gamma(transaction.getUnderlyingOption(), black);
    final double txnGamma = securityGamma * transaction.getQuantity() * 
        transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnGamma;
  }

  /**
   * Computes the present value vega of a transaction (i.e. the first order derivative of the present value 
   * with respect to the volatility used).
   * The bond futures price is computed from the curves.
   * @param transaction The option transaction.
   * @param black The curve and Black volatility data, not null
   * @return The delta.
   */
  public double presentValueVega(
      BondFuturesOptionPremiumTransaction transaction, 
      BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(transaction, "transaction");
    ArgumentChecker.notNull(black, "Black parameters");
    final double securityVega = METHOD_SECURITY.vega(transaction.getUnderlyingOption(), black);
    final double txnVega = securityVega * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional();
    return txnVega;
  }

}
