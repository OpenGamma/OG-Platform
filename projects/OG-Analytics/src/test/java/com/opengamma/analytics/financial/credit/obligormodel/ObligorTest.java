/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligormodel;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;

/**
 * Class to test the implementation of the obligor class 
 */
public class ObligorTest {

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Make sure that the tests cover all the input fields to the Obligor constructor

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  private static final String obligorTicker = "MSFT";
  private static final String obligorShortName = "Microsoft";
  private static final String obligorREDCode = "ABC123";

  private static final CreditRating obligorCompositeRating = CreditRating.AA;
  private static final CreditRating obligorImpliedRating = CreditRating.A;

  private static final CreditRatingMoodys obligorCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors obligorCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch obligorCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean obligorHasDefaulted = false;

  private static final Sector obligorSector = Sector.INDUSTRIALS;
  private static final Region obligorRegion = Region.NORTHAMERICA;
  private static final String obligorCountry = "United States";

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor ticker
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorTickerField() {

    new Obligor(null, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'empty' obligor ticker
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorTickerField() {

    new Obligor("", obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor short name
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorShortNameField() {

    new Obligor(obligorTicker, null, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'empty' obligor short name
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorShortNameField() {

    new Obligor(obligorTicker, "", obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor RED code
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorREDCodeField() {

    new Obligor(obligorTicker, obligorShortName, null, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'empty' obligor RED code
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorShortREDCodeField() {

    new Obligor(obligorTicker, obligorShortName, "", obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor composite rating
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCompositeRatingField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, null, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor implied rating
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorImpliedRatingField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, null, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor Moodys credit rating
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCreditRatingMoodysField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, null, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor S&P credit rating
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCreditRatingStandardAndPoorsField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, null,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor Fitch credit rating
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCreditRatingFitchField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        null, obligorHasDefaulted, obligorSector, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor sector
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorSectorField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, null, obligorRegion, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor region
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorRegionField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, null, obligorCountry);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'null' obligor country
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligorCountryField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Test for 'empty' obligor country
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyObligorCountryField() {

    new Obligor(obligorTicker, obligorShortName, obligorREDCode, obligorCompositeRating, obligorImpliedRating, obligorCreditRatingMoodys, obligorCreditRatingStandardAndPoors,
        obligorCreditRatingFitch, obligorHasDefaulted, obligorSector, obligorRegion, "");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
