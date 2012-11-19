/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
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
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDACurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double[] spreadBumps,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // -------------------------------------------------------------

    final double[] bucketedCS01 = new double[10];

    final double[] bumpedMarketSpreads = new double[marketSpreads.length];

    // -------------------------------------------------------------

    final PresentValueLegacyCreditDefaultSwap temp = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = temp.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

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
      final double bumpedPresentValue = temp.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

      bucketedCS01[m] = (bumpedPresentValue - presentValue) / spreadBumps[m];
    }

    // -------------------------------------------------------------

    return bucketedCS01;
  }
  // -------------------------------------------------------------------------------------------------

}
