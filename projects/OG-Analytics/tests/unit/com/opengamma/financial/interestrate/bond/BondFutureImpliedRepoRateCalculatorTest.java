/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertArrayEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
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
  private static final double[] DELIVERY_TIMES = new double[] {1.5, 1.5, 1.5};
  private static final double[] CLEAN_PRICES = new double[] {97., 98., 99.};
  private static final double[] ACCRUED_INTEREST = new double[] {0, 0.04, 1};
  private static final double[] ACCRUED_INTEREST_AT_DELIVERY = new double[] {0.1, 0.23, 0.4};
  private static final double[] REPO_RATES = new double[] {0.03, 0.02, 0.03};
  private static final BondFutureDeliverableBasketDataBundle BASKET_DATA = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, REPO_RATES);
  private static final BondForward[] DELIVERABLES = new BondForward[] {
      new BondForward(new Bond(new double[] {1, 2, 3}, COUPONS[0], NAME), DELIVERY_TIMES[0], ACCRUED_INTEREST[0], ACCRUED_INTEREST_AT_DELIVERY[0]),
      new BondForward(new Bond(new double[] {1, 2, 3, 4, 5, 6}, COUPONS[1], NAME), DELIVERY_TIMES[1], ACCRUED_INTEREST[1], ACCRUED_INTEREST_AT_DELIVERY[1]),
      new BondForward(new Bond(new double[] {1, 2, 3, 4, 5}, COUPONS[2], NAME), DELIVERY_TIMES[2], ACCRUED_INTEREST[2], ACCRUED_INTEREST_AT_DELIVERY[2])};
  private static final double[] CONVERSION_FACTORS = new double[] {1, 1, 1};
  private static final double FUTURE_PRICE = 105;
  private static final BondFuture FUTURE = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, FUTURE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondFuture() {
    IRR_CALCULATOR.calculate(null, BASKET_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBasketData() {
    IRR_CALCULATOR.calculate(FUTURE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongBasketSize() {
    IRR_CALCULATOR.calculate(new BondFuture(new BondForward[] {DELIVERABLES[0]}, new double[] {0.78}, FUTURE_PRICE), BASKET_DATA);
  }

  @Test
  public void test() {
    final int n = 3;
    final double[] result = new double[n];
    final double[] dirtyPrice = new double[n];
    final double[] invoicePrice = new double[n];
    for (int i = 0; i < 3; i++) {
      dirtyPrice[i] = DIRTY_PRICE_CALCULATOR.calculate(DELIVERABLES[i].getBond(), CLEAN_PRICES[i]);
      invoicePrice[i] = FUTURE_PRICE * CONVERSION_FACTORS[i] + ACCRUED_INTEREST_AT_DELIVERY[i];
      result[i] = (invoicePrice[i] - dirtyPrice[i] + COUPONS[i]) / (dirtyPrice[i] * DELIVERY_TIMES[i] - COUPONS[i] / 2);
    }
    final double[] irr = IRR_CALCULATOR.calculate(FUTURE, BASKET_DATA);
    assertArrayEquals(irr, result, 1e-15);
  }

}
