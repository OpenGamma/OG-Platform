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
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;

/**
 * 
 */
public class BondFutureNetBasisCalculatorTest {
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondForwardDirtyPriceCalculator FORWARD_DIRTY_PRICE_CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final BondFutureNetBasisCalculator NET_BASIS_CALCULATOR = BondFutureNetBasisCalculator.getInstance();
  private static final String NAME = "A";
  private static final Bond[] DELIVERABLES = new Bond[] {new Bond(new double[] {1, 2, 3}, 0.05, NAME), new Bond(new double[] {1, 2, 3, 4, 5, 6}, 0.06, NAME),
      new Bond(new double[] {1, 2, 3, 4, 5}, 0.045, NAME)};
  private static final double[] CONVERSION_FACTORS = new double[] {0.123, 0.456, 0.789};
  private static final BondFuture FUTURE = new BondFuture(DELIVERABLES, CONVERSION_FACTORS);
  private static final List<Double> DELIVERY_DATES = Arrays.asList(0.1, 0.1, 0.2);
  private static final List<Double> CLEAN_PRICES = Arrays.asList(97., 98., 99.);
  private static final List<Double> ACCRUED_INTEREST = Arrays.asList(0., 0.04, 1.);
  private static final List<Double> REPO_RATES = Arrays.asList(0.03, 0.02, 0.03);
  private static final double FUTURE_PRICE = 105;

  @Test(expected = IllegalArgumentException.class)
  public void testNullBondFuture() {
    NET_BASIS_CALCULATOR.calculate(null, DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeliveryDates() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, null, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCleanPrices() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, null, ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccruedInterest() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, null, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRepoRates() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, null, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeliveryDateWithNull() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, Arrays.asList(0.1, null, 0.1), CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCleanPricesWithNull() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, Arrays.asList(100., null, 103.), ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAccruedInterestWithNull() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, Arrays.asList(0.3, null, 0.), REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRepoRatesWithNull() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, Arrays.asList(0.02, 0.01, null), FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeFuturePrice() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES, -FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadDeliveryDates() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, Arrays.asList(0.1, 0.1), CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadCleanPrices() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, Arrays.asList(100., 103.), ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadAccruedInterest() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, Arrays.asList(0.3, 0.), REPO_RATES, FUTURE_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadRepoRates() {
    NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, Arrays.asList(0.02, 0.01), FUTURE_PRICE);
  }

  @Test
  public void test() {
    final int n = 3;
    final double[] deliverableDirtyPrices = new double[n];
    final double[] forwardDirtyPrices = new double[n];
    final double[] invoicePrices = new double[n];
    final double[] netBasis = new double[n];
    for (int i = 0; i < n; i++) {
      invoicePrices[i] = FUTURE_PRICE * CONVERSION_FACTORS[i] + ACCRUED_INTEREST.get(i);
      deliverableDirtyPrices[i] = DIRTY_PRICE_CALCULATOR.calculate(DELIVERABLES[i], CLEAN_PRICES.get(i));
      forwardDirtyPrices[i] = FORWARD_DIRTY_PRICE_CALCULATOR.calculate(new BondForward(DELIVERABLES[i], DELIVERY_DATES.get(i)), deliverableDirtyPrices[i], REPO_RATES.get(i));
      netBasis[i] = forwardDirtyPrices[i] - invoicePrices[i];
    }
    final double[] result = NET_BASIS_CALCULATOR.calculate(FUTURE, DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES, FUTURE_PRICE);
    assertArrayEquals(result, netBasis, 1e-15);
  }
}
