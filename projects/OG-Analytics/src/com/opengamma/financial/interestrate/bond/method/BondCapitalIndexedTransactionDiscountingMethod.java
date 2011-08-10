/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.method.PricingMarketMethod;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method for inflation bond transaction. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedTransactionDiscountingMethod implements PricingMarketMethod {

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param market The market.
   * @return The present value.
   */
  public CurrencyAmount presentValue(BondCapitalIndexedTransaction<?> bond, MarketBundle market) {
    final CurrencyAmount pvBond = PVIC.visit(bond.getBondTransaction(), market);
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getSettlementAmount(), "Not used");
    final CurrencyAmount pvSettlement = PVIC.visit(settlement, market);
    return pvBond.multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, MarketBundle market) {
    Validate.isTrue(instrument instanceof BondCapitalIndexedTransaction<?>, "Capital inflation indexed bond.");
    return presentValue((BondCapitalIndexedTransaction<?>) instrument, market);
  }

  // TODO: curve sensitivity
  // TODO: price index sensitivity

}
