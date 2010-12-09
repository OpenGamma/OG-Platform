/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * 
 */
public class BondFutureDeliverableBasketDataBundleTest {
  private static final List<Double> DELIVERY_DATES = Arrays.asList(0.1, 0.1, 0.1, 0.1);
  private static final List<Double> CLEAN_PRICES = Arrays.asList(101., 102., 103., 104.);
  private static final List<Double> ACCRUED_INTEREST = Arrays.asList(0.02, 0.05, 0.05, 0.08);
  private static final List<Double> REPO_RATES = Arrays.asList(0.03, 0.04, 0.04, 0.01);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeliveryDates() {
    new BondFutureDeliverableBasketDataBundle(null, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCleanPrices() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, null, ACCRUED_INTEREST, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccruedInterest() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, null, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRepoRates() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeliveryDateWithNull() {
    new BondFutureDeliverableBasketDataBundle(Arrays.asList(0.1, null, 0.1, 0.11), CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCleanPricesWithNull() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, Arrays.asList(100., null, 103., 106.), ACCRUED_INTEREST, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAccruedInterestWithNull() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, Arrays.asList(0.3, null, 0., 0.), REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRepoRatesWithNull() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, Arrays.asList(0.02, 0.01, null, 0.04));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadDeliveryDates() {
    new BondFutureDeliverableBasketDataBundle(Arrays.asList(0.1, 0.1), CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadCleanPrices() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, Arrays.asList(100., 103.), ACCRUED_INTEREST, REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadAccruedInterest() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, Arrays.asList(0.3, 0.), REPO_RATES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadRepoRates() {
    new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, Arrays.asList(0.02, 0.01));
  }

  @Test
  public void testGetters() {
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
    assertEquals(basketData.getAccruedInterest(), ACCRUED_INTEREST);
    assertEquals(basketData.getCleanPrices(), CLEAN_PRICES);
    assertEquals(basketData.getDeliveryDates(), DELIVERY_DATES);
    assertEquals(basketData.getSize(), ACCRUED_INTEREST.size());
    assertEquals(basketData.getRepoRates(), REPO_RATES);
  }

  @Test
  public void testHashCodeAndEquals() {
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
    BondFutureDeliverableBasketDataBundle other = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
    assertEquals(basketData, other);
    assertEquals(basketData.hashCode(), other.hashCode());
    other = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, CLEAN_PRICES, ACCRUED_INTEREST, REPO_RATES);
    assertFalse(other.equals(basketData));
    other = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, DELIVERY_DATES, ACCRUED_INTEREST, REPO_RATES);
    assertFalse(other.equals(basketData));
    other = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, DELIVERY_DATES, REPO_RATES);
    assertFalse(other.equals(basketData));
    other = new BondFutureDeliverableBasketDataBundle(DELIVERY_DATES, CLEAN_PRICES, ACCRUED_INTEREST, DELIVERY_DATES);
    assertFalse(other.equals(basketData));
  }
}
