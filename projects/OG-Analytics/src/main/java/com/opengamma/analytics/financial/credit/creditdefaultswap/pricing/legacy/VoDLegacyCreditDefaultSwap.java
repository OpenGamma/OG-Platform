/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.CalibrateHazardRateCurve;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of Value-on-Default (VoD or jump-to-default risk) for a vanilla Legacy CDS
 */
public class VoDLegacyCreditDefaultSwap {

  //------------------------------------------------------------------------------------------------------------------------------------------

  private final double _tolerance = 1e-15;

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments
  // TODO : Need to consider more sophisticated sensitivity calculations e.g. algorithmic differentiation 

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the VoD

  public double getValueOnDefaultCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDACurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[marketTenors.length];

    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT365.getDayCountFraction(valuationDate, marketTenors[m]);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurve hazardRateCurve = new CalibrateHazardRateCurve();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(times, calibratedHazardRates, 0.0);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the CDS PV using the just calibrated hazard rate term structure
    final double presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(valuationDate, cds, yieldCurve, calibratedHazardRateCurve, priceType);

    // Calculate the Loss Given Default (LGD) amount
    double lossGivenDefault = cds.getNotional() * (1 - cds.getRecoveryRate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the VoD

    double valueOnDefault = 0.0;

    if (cds.getBuySellProtection() == BuySellProtection.BUY) {
      valueOnDefault = -Math.abs(presentValue) + lossGivenDefault;
    } else {
      valueOnDefault = -Math.abs(presentValue) - lossGivenDefault;
    }

    return valueOnDefault;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

}
