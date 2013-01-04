/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for inflation bond transaction. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedTransactionDiscountingMethod implements PricingMarketMethod {

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();
  /**
   * The method used for security computation.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_SECURITY = new BondCapitalIndexedSecurityDiscountingMethod();

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param market The market.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondCapitalIndexedTransaction<?> bond, final MarketDiscountBundle market) {
    final MultipleCurrencyAmount pvBond = bond.getBondTransaction().accept(PVIC, market);
    final MultipleCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, market).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final IMarketBundle market) {
    Validate.isTrue(instrument instanceof BondCapitalIndexedTransaction<?>, "Capital inflation indexed bond.");
    return presentValue((BondCapitalIndexedTransaction<?>) instrument, (MarketDiscountBundle) market);
  }

  /**
   * Computes the security present value from a quoted clean real price.
   * @param bond The bond transaction.
   * @param market The market.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedTransaction<Coupon> bond, final MarketDiscountBundle market, final double cleanPriceReal) {
    Validate.notNull(bond, "Coupon");
    Validate.notNull(market, "Market");
    final MultipleCurrencyAmount pvBond = METHOD_SECURITY.presentValueFromCleanPriceReal(bond.getBondTransaction(), market, cleanPriceReal);
    final MultipleCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, market).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.plus(pvSettlement);
  }

  // TODO: curve sensitivity
  // TODO: price index sensitivity

}
