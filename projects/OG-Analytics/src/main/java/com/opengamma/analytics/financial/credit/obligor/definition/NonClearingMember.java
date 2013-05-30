/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor.definition;

import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;

/**
 * Class to define a Non Clearing Member (an extension of the Obligor class) i.e. an Obligor who does not clear trades through a CCP
 */
public class NonClearingMember extends Obligor {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public NonClearingMember(
      final String obligorTicker,
      final String obligorShortName,
      final String obligorREDCode,
      final CreditRating compositeRating,
      final CreditRating impliedRating,
      final CreditRatingMoodys moodysCreditRating,
      final CreditRatingStandardAndPoors standardAndPoorsCreditRating,
      final CreditRatingFitch fitchCreditRating,
      final boolean hasDefaulted,
      final Sector sector,
      final Region region,
      final String country) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    super(obligorTicker, obligorShortName, obligorREDCode, compositeRating, impliedRating, moodysCreditRating, standardAndPoorsCreditRating, fitchCreditRating, hasDefaulted, sector, region, country);

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
