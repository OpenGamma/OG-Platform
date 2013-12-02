/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the obligor object.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityTest {
  /** The ticker */
  static final String TICKER = "ABC";
  /** The short name */
  static final String SHORT_NAME = "DEF";
  /** The credit ratings */
  static final Set<CreditRating> CREDIT_RATINGS = Sets.newHashSet(CreditRating.of("A", "Prime", "S&P", false), CreditRating.of("B", "Investment Grade", "Moody's", false));
  /** The sector */
  static final Sector SECTOR;
  /** The region */
  static final Region REGION = Region.of("NA", Sets.newHashSet(Country.US, Country.CA), Sets.newHashSet(Currency.USD, Currency.CAD));
  /** The country */
  static final Country COUNTRY = Country.US;
  /** The RED code */
  static final String RED_CODE = "WER";
  /** An obligor */
  static final LegalEntity LEGAL_ENTITY;
  /** An obligor with RED code */
  static final LegalEntityWithREDCode LEGAL_ENTITY_RED_CODE;

  static {
    final FlexiBean industryClassifications = new FlexiBean();
    industryClassifications.put("GICS", GICSCode.of("10203040"));
    industryClassifications.put("ICB", ICBCode.of("1020"));
    SECTOR = Sector.of("INDUSTRY", industryClassifications);
    LEGAL_ENTITY = new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION);
    LEGAL_ENTITY_RED_CODE = new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, RED_CODE);
  }

  /**
   * Tests failure for a null short name.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShortName1() {
    new LegalEntity(TICKER, null, CREDIT_RATINGS, SECTOR, REGION);
  }

  /**
   * Tests failure for a null short name.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShortName2() {
    new LegalEntityWithREDCode(TICKER, null, CREDIT_RATINGS, SECTOR, REGION, RED_CODE);
  }

  /**
   * Tests failure for null RED code.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullREDCode() {
    new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, null);
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject1() {
    final LegalEntity legalEntity = new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION);
    assertEquals(TICKER, legalEntity.getTicker());
    assertEquals(SHORT_NAME, legalEntity.getShortName());
    assertEquals(CREDIT_RATINGS, legalEntity.getCreditRatings());
    assertEquals(SECTOR, legalEntity.getSector());
    assertEquals(REGION, legalEntity.getRegion());
    LegalEntity other = new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION);
    assertEquals(legalEntity, other);
    assertEquals(legalEntity.hashCode(), other.hashCode());
    other = new LegalEntity(SHORT_NAME, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION);
    assertFalse(legalEntity.equals(other));
    other = new LegalEntity(TICKER, TICKER, CREDIT_RATINGS, SECTOR, REGION);
    assertFalse(legalEntity.equals(other));
    other = new LegalEntity(TICKER, SHORT_NAME, Collections.singleton(CreditRating.of("AAA", "ASD", false)), SECTOR, REGION);
    assertFalse(legalEntity.equals(other));
    other = new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, Sector.of("OIJ"), REGION);
    assertFalse(legalEntity.equals(other));
    other = new LegalEntity(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, Region.of("OIH"));
    assertFalse(legalEntity.equals(other));
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject2() {
    final LegalEntityWithREDCode obligor = new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, RED_CODE);
    assertEquals(TICKER, obligor.getTicker());
    assertEquals(SHORT_NAME, obligor.getShortName());
    assertEquals(CREDIT_RATINGS, obligor.getCreditRatings());
    assertEquals(SECTOR, obligor.getSector());
    assertEquals(REGION, obligor.getRegion());
    assertEquals(RED_CODE, obligor.getRedCode());
    LegalEntityWithREDCode other = new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, RED_CODE);
    assertEquals(obligor, other);
    assertEquals(obligor.hashCode(), other.hashCode());
    other = new LegalEntityWithREDCode(SHORT_NAME, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new LegalEntityWithREDCode(TICKER, TICKER, CREDIT_RATINGS, SECTOR, REGION, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new LegalEntityWithREDCode(TICKER, SHORT_NAME, Collections.singleton(CreditRating.of("AAA", "ASD", false)), SECTOR, REGION, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, Sector.of("OIJ"), REGION, RED_CODE);
    assertFalse(obligor.equals(other));
    other = new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, Region.of("OIH"), RED_CODE);
    assertFalse(obligor.equals(other));
    other = new LegalEntityWithREDCode(TICKER, SHORT_NAME, CREDIT_RATINGS, SECTOR, REGION, "OIJOI");
    assertFalse(obligor.equals(other));
  }
}
