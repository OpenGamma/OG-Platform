/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.SpreadBumpType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.LegacyCreditDefaultSwapDefinition;

/**
 * Class to enable user to specify a non-homogeneous set of bumps to the credit spread term structure
 * This allows the user to specify a curve scenario (e.g. curve flips from upward to downward sloping)
 * and to then compute the impact on the PV
 */
public class CurveScenarioLegacyCreditDefaultSwap {

  // -------------------------------------------------------------

  public double[] getCS01BucketedCfreditDefaultSwap(
      LegacyCreditDefaultSwapDefinition cds,
      ISDACurve yieldCurve,
      ZonedDateTime[] marketTenors,
      double[] marketSpreads,
      double[] spreadBumps,
      SpreadBumpType spreadBumpType) {

    // -------------------------------------------------------------

    double[] bucketedCS01 = new double[10];

    double[] bumpedMarketSpreads = new double[marketSpreads.length];

    // -------------------------------------------------------------

    PresentValueLegacyCreditDefaultSwap temp = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    double presentValue = temp.calibrateAndGetPresentValue(cds, marketTenors, marketSpreads, yieldCurve);

    // -------------------------------------------------------------

    // Loop through each of the spreads at each tenor
    for (int m = 0; m < marketTenors.length; m++) {

      // Reset the bumpedMarketSpreads vector to the original marketSpreads
      for (int n = 0; n < marketTenors.length; n++) {
        bumpedMarketSpreads[n] = marketSpreads[n];
      }

      // Bump the spread at tenor m
      if (spreadBumpType == SpreadBumpType.ADDITIVE_BUCKETED) {
        bumpedMarketSpreads[m] = marketSpreads[m] + spreadBumps[m];
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_BUCKETED) {
        bumpedMarketSpreads[m] = marketSpreads[m] * (1 + spreadBumps[m]);
      }

      // Calculate the bumped CDS PV
      double bumpedPresentValue = temp.calibrateAndGetPresentValue(cds, marketTenors, bumpedMarketSpreads, yieldCurve);

      bucketedCS01[m] = (bumpedPresentValue - presentValue) / spreadBumps[m];
    }

    // -------------------------------------------------------------

    return bucketedCS01;
  }
  // -------------------------------------------------------------------------------------------------

}
