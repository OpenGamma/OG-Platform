/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;

/**
 * 
 */
public class BondFutureImpliedRepoRateCalculatorTest {
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondFutureImpliedRepoRateCalculator IRR_CALCULATOR = BondFutureImpliedRepoRateCalculator.getInstance();
  private static final String NAME = "A";
  private static final double[] COUPONS = new double[] {0.03, 0.04, 0.05};
  private static final Bond[] DELIVERABLES = new Bond[] {new Bond(new double[] {1, 2, 3}, COUPONS[0], NAME), new Bond(new double[] {1, 2, 3, 4, 5, 6}, COUPONS[1], NAME),
      new Bond(new double[] {1, 2, 3, 4, 5}, COUPONS[2], NAME)};
  private static final double[] CONVERSION_FACTORS = new double[] {1, 1, 1};
  private static final BondFuture FUTURE = new BondFuture(DELIVERABLES, CONVERSION_FACTORS);
  private static final List<Double> DELIVERY_DATES = Arrays.asList(1.5, 1.5, 1.5);
  private static final List<Double> CLEAN_PRICES = Arrays.asList(97., 98., 99.);
  private static final List<Double> ACCRUED_INTEREST = Arrays.asList(0., 0.04, 1.);
  private static final List<Double> REPO_RATES = Arrays.asList(0.03, 0.02, 0.03);
  private static final BondFutureDeliverableBasketDataBundle BASKET_DATA = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
  private static final double FUTURE_PRICE = 105;

  @Test(expected = IllegalArgumentException.class)
  public void testNullBondFuture() {
    IRR_CALCULATOR.calculate(null, BASKET_DATA, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBasketData() {
    IRR_CALCULATOR.calculate(FUTURE, null, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongBasketSize() {
    IRR_CALCULATOR.calculate(new BondFuture(new Bond[] {DELIVERABLES[0]}, new double[] {0.78}), BASKET_DATA, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeFuturePrice() {
    IRR_CALCULATOR.calculate(FUTURE, BASKET_DATA, -FUTURE_PRICE);
  }

  @Test
  public void test() {
    final int n = 3;
    final double[] result = new double[n];
    final double[] dirtyPrice = new double[n];
    final double[] invoicePrice = new double[n];
    for (int i = 0; i < 3; i++) {
      dirtyPrice[i] = DIRTY_PRICE_CALCULATOR.calculate(DELIVERABLES[i], CLEAN_PRICES.get(i));
      invoicePrice[i] = FUTURE_PRICE * CONVERSION_FACTORS[i] + ACCRUED_INTEREST.get(i);
      result[i] = (invoicePrice[i] - dirtyPrice[i] + COUPONS[i]) / (dirtyPrice[i] * DELIVERY_DATES.get(i) - COUPONS[i] / 2);
    }
    final double[] irr = IRR_CALCULATOR.calculate(FUTURE, BASKET_DATA, FUTURE_PRICE);
    assertArrayEquals(irr, result, 1e-15);
  }

}
