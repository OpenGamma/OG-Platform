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
 * Tests the obligor object.
 */
@Test(groups = TestGroup.UNIT)
public class ObligorTest {
  /** The ticker */
  private static final String TICKER = "ABC";
  /** The short name */
  private static final String SHORT_NAME = "DEF";
  /** The credit ratings */
  private static final Set<CreditRating> CREDIT_RATINGS = Sets.newHashSet(CreditRating.of("A", "S&P", false), CreditRating.of("A", "Moody's", false));
  /** The sector */
  private static final Sector SECTOR = Sector.of("CD");
  /** The region */
  private static final Region REGION = Region.of("US", Country.US, Currency.USD);
  /** The country */
  private static final Country COUNTRY = Country.US;
  /** The RED code */
  private static final String RED_CODE = "WER";

  /**
   * Tests failure for a null ticker.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTicker1() {
    new Obligor(null, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null ticker.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTicker2() {
    new ObligorWithREDCode(null, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, RED_CODE);
  }

  /**
   * Tests failure for a null short name.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShortName1() {
    new Obligor(TICKER, null, CREDIT_RATINGS, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null short name.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShortName2() {
    new ObligorWithREDCode(TICKER, null, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, RED_CODE);
  }

  /**
   * Tests failure for null credit ratings.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCreditRatings1() {
    new Obligor(TICKER, SHORT_NAME, null, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for null credit ratings.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCreditRatings2() {
    new ObligorWithREDCode(TICKER, SHORT_NAME, null, SECTOR, REGION, COUNTRY, RED_CODE);
  }

  /**
   * Tests failure for null sector.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSector1() {
    new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, null, REGION, COUNTRY);
  }

  /**
   * Tests failure for null sector.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSector2() {
    new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, null, REGION, COUNTRY, RED_CODE);
  }

  /**
   * Tests failure for null region.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegion1() {
    new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, null, COUNTRY);
  }

  /**
   * Tests failure for null region.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegion2() {
    new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, null, COUNTRY, RED_CODE);
  }

  /**
   * Tests failure for null country.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCountry1() {
    new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, null);
  }

  /**
   * Tests failure for null country.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCountry2() {
    new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, null, RED_CODE);
  }

  /**
   * Tests failure for null RED code.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullREDCode() {
    new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, null);
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject1() {
    final Obligor obligor = new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY);
    assertEquals(TICKER, obligor.getTicker());
    assertEquals(SHORT_NAME, obligor.getShortName());
    assertEquals(CREDIT_RATINGS, obligor.getCreditRatings());
    assertEquals(SECTOR, obligor.getSector());
    assertEquals(REGION, obligor.getRegion());
    assertEquals(COUNTRY, obligor.getCountry());
    Obligor other = new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY);
    assertEquals(obligor, other);
    assertEquals(obligor.hashCode(), other.hashCode());
    other = new Obligor(SHORT_NAME, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, TICKER, CREDIT_RATINGS, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, Collections.singleton(CreditRating.of("AAA", "ASD", false)), SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, Sector.of("OIJ"), REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, Region.of("OIH"), COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, Country.AR);
    assertFalse(obligor.equals(other));
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject2() {
    final ObligorWithREDCode obligor = new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, RED_CODE);
    assertEquals(TICKER, obligor.getTicker());
    assertEquals(SHORT_NAME, obligor.getShortName());
    assertEquals(CREDIT_RATINGS, obligor.getCreditRatings());
    assertEquals(SECTOR, obligor.getSector());
    assertEquals(REGION, obligor.getRegion());
    assertEquals(COUNTRY, obligor.getCountry());
    assertEquals(RED_CODE, obligor.getRedCode());
    ObligorWithREDCode other = new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, RED_CODE);
    assertEquals(obligor, other);
    assertEquals(obligor.hashCode(), other.hashCode());
    other = new ObligorWithREDCode(SHORT_NAME, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new ObligorWithREDCode(TICKER, TICKER, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new ObligorWithREDCode(TICKER, SHORT_NAME, Collections.singleton(CreditRating.of("AAA", "ASD", false)), SECTOR, REGION, COUNTRY, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, Sector.of("OIJ"), REGION, COUNTRY, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, Region.of("OIH"), COUNTRY, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, Country.AR, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new ObligorWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, COUNTRY, "OIJOI");
    assertFalse(obligor.equals(other));
  }
}
