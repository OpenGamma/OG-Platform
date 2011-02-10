/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;

/**
 * 
 */
public class BondFutureNetBasisCalculatorTest {
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondForwardDirtyPriceCalculator FORWARD_DIRTY_PRICE_CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final BondFutureNetBasisCalculator NET_BASIS_CALCULATOR = BondFutureNetBasisCalculator.getInstance();
  private static final String NAME = "A";
  private static final double[] DELIVERY_DATES = new double[] {0.1, 0.1, 0.2};
  private static final double[] CLEAN_PRICES = new double[] {97., 98., 99.};
  private static final double[] ACCRUED_INTEREST = new double[] {0., 0.04, 1.};
  private static final double[] ACCRUED_INTEREST_AT_DELIVERY = new double[] {0.2, 0.4, 0.6};
  private static final double[] REPO_RATES = new double[] {0.03, 0.02, 0.03};
  private static final BondForward[] DELIVERABLES = new BondForward[] {
      new BondForward(new Bond(new double[] {1, 2, 3}, 0.05, NAME), DELIVERY_DATES[0], ACCRUED_INTEREST[0], ACCRUED_INTEREST_AT_DELIVERY[0]),
      new BondForward(new Bond(new double[] {1, 2, 3, 4, 5, 6}, 0.06, NAME), DELIVERY_DATES[1], ACCRUED_INTEREST[1], ACCRUED_INTEREST_AT_DELIVERY[1]),
      new BondForward(new Bond(new double[] {1, 2, 3, 4, 5}, 0.045, NAME), DELIVERY_DATES[2], ACCRUED_INTEREST[2], ACCRUED_INTEREST_AT_DELIVERY[2])};
  private static final double[] CONVERSION_FACTORS = new double[] {0.123, 0.456, 0.789};
  private static final BondFutureDeliverableBasketDataBundle BASKET_DATA = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, REPO_RATES);
  private static final double FUTURE_PRICE = 105;
  private static final BondFuture FUTURE = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, FUTURE_PRICE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullBondFuture() {
    NET_BASIS_CALCULATOR.calculate(null, BASKET_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBasket() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongBasketSize() {
    NET_BASIS_CALCULATOR.calculate(new BondFuture(new BondForward[] {DELIVERABLES[0]}, new double[] {0.78}, FUTURE_PRICE), BASKET_DATA);
  }

  @Test
  public void test() {
    final int n = 3;
    final double[] deliverableDirtyPrices = new double[n];
    final double[] forwardDirtyPrices = new double[n];
    final double[] invoicePrices = new double[n];
    final double[] netBasis = new double[n];
    for (int i = 0; i < n; i++) {
      invoicePrices[i] = FUTURE_PRICE * CONVERSION_FACTORS[i] + ACCRUED_INTEREST_AT_DELIVERY[i];
      deliverableDirtyPrices[i] = DIRTY_PRICE_CALCULATOR.calculate(DELIVERABLES[i].getBond(), CLEAN_PRICES[i]);
      forwardDirtyPrices[i] = FORWARD_DIRTY_PRICE_CALCULATOR.calculate(new BondForward(DELIVERABLES[i].getBond(), DELIVERY_DATES[i], ACCRUED_INTEREST[i], ACCRUED_INTEREST_AT_DELIVERY[i]),
          deliverableDirtyPrices[i], REPO_RATES[i]);
      netBasis[i] = forwardDirtyPrices[i] - invoicePrices[i];
    }
    final double[] result = NET_BASIS_CALCULATOR.calculate(FUTURE, BASKET_DATA);
    assertArrayEquals(result, netBasis, 1e-15);
  }
}
