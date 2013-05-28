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
 * Class to define a General Clearing Member of a CCP (an extension of the Obligor class)
 */
public class GeneralClearingMember extends Obligor {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add a list of clients (which are NCM's) and give them IM and VM accounts
  // TODO : Add a boolean to specify if IM and VM accounts  are segregated

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The contribution of this GCM to the CCP's reserve fund
  private final double _reserveFundContribution;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public GeneralClearingMember(
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
      final String country,
      final double reserveFundContribution) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    super(obligorTicker, obligorShortName, obligorREDCode, compositeRating, impliedRating, moodysCreditRating, standardAndPoorsCreditRating, fitchCreditRating, hasDefaulted, sector, region, country);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _reserveFundContribution = reserveFundContribution;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getReserveFundContribution() {
    return _reserveFundContribution;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
