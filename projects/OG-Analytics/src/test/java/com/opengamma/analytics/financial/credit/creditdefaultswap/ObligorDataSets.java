/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

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
public class ObligorDataSets {
  private static final String TICKER1 = "OG1";
  private static final String SHORT_NAME1 = "OpenGamma1";
  private static final String RED_CODE1 = "ABC123";
  private static final CreditRating COMPOSITE_RATING1 = CreditRating.AA;
  private static final CreditRating IMPLIED_RATING1 = CreditRating.A;
  private static final CreditRatingMoodys MOODYS_RATING1 = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors STANDARD_AND_POORS_RATING1 = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch FITCH_RATING1 = CreditRatingFitch.AA;
  private static final boolean HAS_DEFAULTED1 = false;
  private static final Sector SECTOR1 = Sector.INDUSTRIALS;
  private static final Region REGION1 = Region.NORTHAMERICA;
  private static final String COUNTRY1 = "United States";
  private static final Obligor OBLIGOR1 = new Obligor(TICKER1, SHORT_NAME1, RED_CODE1, COMPOSITE_RATING1, IMPLIED_RATING1,
      MOODYS_RATING1, STANDARD_AND_POORS_RATING1, FITCH_RATING1, HAS_DEFAULTED1, SECTOR1, REGION1, COUNTRY1);

  private static final String TICKER2 = "OG2";
  private static final String SHORT_NAME2 = "OpenGamma2";
  private static final String RED_CODE2 = "XYZ321";
  private static final CreditRating COMPOSITE_RATING2 = CreditRating.BB;
  private static final CreditRating IMPLIED_RATING2 = CreditRating.B;
  private static final CreditRatingMoodys MOODYS_RATING2 = CreditRatingMoodys.BB;
  private static final CreditRatingStandardAndPoors STANDARD_AND_POORS_RATING2 = CreditRatingStandardAndPoors.B;
  private static final CreditRatingFitch FITCH_RATING2 = CreditRatingFitch.BB;
  private static final boolean HAS_DEFAULTED2 = false;
  private static final Sector SECTOR2 = Sector.INDUSTRIALS;
  private static final Region REGION2 = Region.NORTHAMERICA;
  private static final String COUNTRY2 = "United States";
  private static final Obligor OBLIGOR2 = new Obligor(TICKER2, SHORT_NAME2, RED_CODE2, COMPOSITE_RATING2, IMPLIED_RATING2,
      MOODYS_RATING2, STANDARD_AND_POORS_RATING2, FITCH_RATING2, HAS_DEFAULTED2, SECTOR2, REGION2, COUNTRY2);

  private static final String TICKER3 = "OG3";
  private static final String SHORT_NAME3 = "OpenGamma3";
  private static final String RED_CODE3 = "123ABC";
  private static final CreditRating COMPOSITE_RATING3 = CreditRating.C;
  private static final CreditRating IMPLIED_RATING3 = CreditRating.C;
  private static final CreditRatingMoodys MOODYS_RATING3 = CreditRatingMoodys.C;
  private static final CreditRatingStandardAndPoors STANDARD_AND_POORS_RATING3 = CreditRatingStandardAndPoors.C;
  private static final CreditRatingFitch FITCH_RATING3 = CreditRatingFitch.C;
  private static final boolean HAS_DEFAULTED3 = false;
  private static final Sector SECTOR3 = Sector.INDUSTRIALS;
  private static final Region REGION3 = Region.EUROPE;
  private static final String COUNTRY3 = "United Kingdom";
  private static final Obligor OBLIGOR3 = new Obligor(TICKER3, SHORT_NAME3, RED_CODE3, COMPOSITE_RATING3, IMPLIED_RATING3,
      MOODYS_RATING3, STANDARD_AND_POORS_RATING3, FITCH_RATING3, HAS_DEFAULTED3, SECTOR3, REGION3, COUNTRY3);

  public static Obligor getObligor1() {
    return OBLIGOR1;
  }

  public static Obligor getObligor2() {
    return OBLIGOR2;
  }

  public static Obligor getObligor3() {
    return OBLIGOR3;
  }
}
