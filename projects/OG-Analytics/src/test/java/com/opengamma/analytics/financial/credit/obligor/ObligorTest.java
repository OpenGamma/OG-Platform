/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.legalentity.LegalEntityWithREDCode;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Class to test the implementation of the obligor class.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class ObligorTest {
  /** The ticker */
  private static final String TICKER = "MSFT";
  /** The short name */
  private static final String SHORT_NAME = "Microsoft";
  /** The red code */
  private static final String RED_CODE = "ABC123";
  /** The composite rating */
  private static final CreditRating COMPOSITE_RATING = CreditRating.AA;
  /** The implied rating */
  private static final CreditRating IMPLIED_RATING = CreditRating.A;
  /** The Moody's rating */
  private static final CreditRatingMoodys MOODYS = CreditRatingMoodys.AA;
  /** The S&P rating */
  private static final CreditRatingStandardAndPoors SANDP = CreditRatingStandardAndPoors.A;
  /** The Fitch rating */
  private static final CreditRatingFitch FITCH = CreditRatingFitch.AA;
  /** Has default occurred */
  private static final boolean DEFAULTED = false;
  /** The sector */
  private static final Sector SECTOR = Sector.INDUSTRIALS;
  /** The region */
  private static final Region REGION = Region.NORTHAMERICA;
  /** The country */
  private static final String COUNTRY = "US";

  /**
   * Tests failure for a null ticker.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorTickerField() {
    new Obligor(null, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for an empty ticker
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorTickerField() {
    new Obligor("", SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   *  Tests failure for a null short name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorShortNameField() {
    new Obligor(TICKER, null, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for an empty short code
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorShortNameField() {
    new Obligor(TICKER, "", RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null RED code
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorREDCodeField() {
    new Obligor(TICKER, SHORT_NAME, null, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for an empty RED code.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorShortREDCodeField() {
    new Obligor(TICKER, SHORT_NAME, "", COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null composite rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCompositeRatingField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, null, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null implied rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorImpliedRatingField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, null, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null Moody's rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCreditRatingMoodysField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, null, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null S&P rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCreditRatingStandardAndPoorsField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, null,
        FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null Fitch rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCreditRatingFitchField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        null, DEFAULTED, SECTOR, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null sector
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorSectorField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, null, REGION, COUNTRY);
  }

  /**
   * Tests failure for a null region
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorRegionField() {

    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, null, COUNTRY);
  }

  /**
   * Tests failure for a null country
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCountryField() {

    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, null);
  }

  /**
   * Tests failure for an empty country name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorCountryField() {
    new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP,
        FITCH, DEFAULTED, SECTOR, REGION, "");
  }

  /**
   * Tests the object is constructed as expected
   */
  @Test
  public void testObject() {
    final Obligor obligor = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertEquals(TICKER, obligor.getObligorTicker());
    assertEquals(SHORT_NAME, obligor.getObligorShortName());
    assertEquals(RED_CODE, obligor.getObligorREDCode());
    assertEquals(COMPOSITE_RATING, obligor.getCompositeRating());
    assertEquals(IMPLIED_RATING, obligor.getImpliedRating());
    assertEquals(MOODYS, obligor.getMoodysCreditRating());
    assertEquals(SANDP, obligor.getStandardAdPoorsCreditRating());
    assertEquals(FITCH, obligor.getFitchCreditRating());
    assertEquals(DEFAULTED, obligor.getHasDefaulted());
    assertEquals(SECTOR, obligor.getSector());
    assertEquals(REGION, obligor.getRegion());
    assertEquals(COUNTRY, obligor.getCountry());
    Obligor other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertEquals(obligor, other);
    assertEquals(obligor.hashCode(), other.hashCode());
    other = new Obligor(TICKER + "A", SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME + "A", RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE + "A", COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, CreditRating.C, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, CreditRating.DEFAULT, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, CreditRatingMoodys.BBB, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, CreditRatingStandardAndPoors.CC, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, CreditRatingFitch.B, DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, !DEFAULTED, SECTOR, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, Sector.BASICMATERIALS, REGION, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, Region.AFRICA, COUNTRY);
    assertFalse(obligor.equals(other));
    other = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, "EU");
    assertFalse(obligor.equals(other));
  }

  /**
   * Tests creation of non-deprecated object.
   */
  @Test
  public void testDelegate() {
    final Obligor obligor = new Obligor(TICKER, SHORT_NAME, RED_CODE, COMPOSITE_RATING, IMPLIED_RATING, MOODYS, SANDP, FITCH, DEFAULTED, SECTOR, REGION, COUNTRY);
    final Set<com.opengamma.analytics.financial.legalentity.CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(COMPOSITE_RATING.toCreditRating());
    creditRatings.add(IMPLIED_RATING.toCreditRating());
    creditRatings.add(MOODYS.toCreditRating());
    creditRatings.add(SANDP.toCreditRating());
    creditRatings.add(FITCH.toCreditRating());
    final com.opengamma.analytics.financial.legalentity.LegalEntityWithREDCode expected = new LegalEntityWithREDCode(TICKER, SHORT_NAME, creditRatings, SECTOR.toSector(),
        com.opengamma.analytics.financial.legalentity.Region.of(Region.NORTHAMERICA.name(), Country.of(COUNTRY), null), RED_CODE);
    assertEquals(expected, obligor.toObligor());
  }
}
