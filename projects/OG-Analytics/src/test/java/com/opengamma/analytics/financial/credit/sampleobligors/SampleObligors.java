/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.sampleobligors;

import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;

/**
 * 
 */
public class SampleObligors {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final String TICKER_ABC = "ABC";
  private static final String SHORT_NAME_ABC = "ABC Bank";
  private static final String RED_CODE_ABC = "ABC123";
  private static final CreditRating COMPOSITE_RATING_ABC = CreditRating.AA;
  private static final CreditRating IMPLIED_RATING_ABC = CreditRating.A;
  private static final CreditRatingMoodys MOODYS_RATING_ABC = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors STANDARD_AND_POORS_RATING_ABC = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch FITCH_RATING_ABC = CreditRatingFitch.AA;
  private static final boolean HAS_DEFAULTED_ABC = false;
  private static final Sector SECTOR_ABC = Sector.FINANCIALS;
  private static final Region REGION_ABC = Region.NORTHAMERICA;
  private static final String COUNTRY_ABC = "United States";

  private static final Obligor OBLIGOR_ABC = new Obligor(TICKER_ABC, SHORT_NAME_ABC, RED_CODE_ABC, COMPOSITE_RATING_ABC, IMPLIED_RATING_ABC,
      MOODYS_RATING_ABC, STANDARD_AND_POORS_RATING_ABC, FITCH_RATING_ABC, HAS_DEFAULTED_ABC, SECTOR_ABC, REGION_ABC, COUNTRY_ABC);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final String TICKER_XYZ = "XYZ";
  private static final String SHORT_NAME_XYZ = "XYZ Bank";
  private static final String RED_CODE_XYZ = "XYZ123";
  private static final CreditRating COMPOSITE_RATING_XYZ = CreditRating.AA;
  private static final CreditRating IMPLIED_RATING_XYZ = CreditRating.A;
  private static final CreditRatingMoodys MOODYS_RATING_XYZ = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors STANDARD_AND_POORS_RATING_XYZ = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch FITCH_RATING_XYZ = CreditRatingFitch.AA;
  private static final boolean HAS_DEFAULTED_XYZ = false;
  private static final Sector SECTOR_XYZ = Sector.FINANCIALS;
  private static final Region REGION_XYZ = Region.EUROPE;
  private static final String COUNTRY_XYZ = "Europe";

  private static final Obligor OBLIGOR_XYZ = new Obligor(TICKER_XYZ, SHORT_NAME_XYZ, RED_CODE_XYZ, COMPOSITE_RATING_XYZ, IMPLIED_RATING_XYZ,
      MOODYS_RATING_XYZ, STANDARD_AND_POORS_RATING_XYZ, FITCH_RATING_XYZ, HAS_DEFAULTED_XYZ, SECTOR_XYZ, REGION_XYZ, COUNTRY_XYZ);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final String TICKER_RefEnt = "RefEnt";
  private static final String SHORT_NAME_RefEnt = "Reference Entity";
  private static final String RED_CODE_RefEnt = "Ref123";
  private static final CreditRating COMPOSITE_RATING_RefEnt = CreditRating.AA;
  private static final CreditRating IMPLIED_RATING_RefEnt = CreditRating.A;
  private static final CreditRatingMoodys MOODYS_RATING_RefEnt = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors STANDARD_AND_POORS_RATING_RefEnt = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch FITCH_RATING_RefEnt = CreditRatingFitch.AA;
  private static final boolean HAS_DEFAULTED_RefEnt = false;
  private static final Sector SECTOR_RefEnt = Sector.ENERGY;
  private static final Region REGION_RefEnt = Region.NORTHAMERICA;
  private static final String COUNTRY_RefEnt = "United States";

  private static final Obligor OBLIGOR_RefEnt = new Obligor(TICKER_RefEnt, SHORT_NAME_RefEnt, RED_CODE_RefEnt, COMPOSITE_RATING_RefEnt, IMPLIED_RATING_RefEnt,
      MOODYS_RATING_RefEnt, STANDARD_AND_POORS_RATING_RefEnt, FITCH_RATING_RefEnt, HAS_DEFAULTED_RefEnt, SECTOR_RefEnt, REGION_RefEnt, COUNTRY_RefEnt);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public static Obligor getObligor_ABC() {
    return OBLIGOR_ABC;
  }

  public static Obligor getObligor_XYZ() {
    return OBLIGOR_XYZ;
  }

  public static Obligor getObligor_RefEnt() {
    return OBLIGOR_RefEnt;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
