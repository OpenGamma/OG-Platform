/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the region object.
 */
@Test(groups = TestGroup.UNIT)
public class RegionTest {
  /** The name */
  private static final String NAME = "NORTH_AMERICA";
  /** The countries */
  private static final Set<Country> COUNTRIES = Sets.newHashSet(Country.CA, Country.MX, Country.US);
  /** The currencies */
  private static final Set<Currency> CURRENCIES = Sets.newHashSet(Currency.CAD, Currency.of("MXN"), Currency.USD);

  /**
   * Tests failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    Region.of(null, COUNTRIES, CURRENCIES);
  }

  /**
   * Test failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    Region.of(null, Country.CA, Currency.CAD);
  }

  /**
   * Tests failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName3() {
    Region.of(null, CURRENCIES);
  }

  /**
   * Test failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName4() {
    Region.of(null, Currency.CAD);
  }

  /**
   * Tests failure on null currencies
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencies1() {
    Region.of(NAME, COUNTRIES, null);
  }

  /**
   * Tests failure on null currencies
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencies2() {
    Region.of(NAME, (Set<Currency>) null);
  }

  /**
   * Tests failure on null currency
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency1() {
    Region.of(NAME, Country.CA, null);
  }

  /**
   * Tests failure on null currency
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    Region.of(NAME, (Currency) null);
  }

  /**
   * Tests failure on null country
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCountry1() {
    Region.of(NAME, null, Currency.CAD);
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject() {
    final Region region = Region.of(NAME, COUNTRIES, CURRENCIES);
    assertEquals(NAME, region.getName());
    assertEquals(COUNTRIES, region.getCountries());
    assertEquals(CURRENCIES, region.getCurrencies());
    Region other = Region.of(NAME, COUNTRIES, CURRENCIES);
    assertEquals(region, other);
    assertEquals(region.hashCode(), other.hashCode());
    other = Region.of("OTHER", COUNTRIES, CURRENCIES);
    assertFalse(region.equals(other));
    other = Region.of(NAME, Collections.singleton(Country.CA), CURRENCIES);
    assertFalse(region.equals(other));
    other = Region.of(NAME, COUNTRIES, Collections.singleton(Currency.CAD));
    assertFalse(region.equals(other));
  }
}
