/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of RecoveryRate01 for a vanilla Legacy CDS
 */
public class RecRate01CreditDefaultSwap {

  //------------------------------------------------------------------------------------------------------------------------------------------

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments
  // TODO : Need to consider more sophisticated sensitivity calculations e.g. algorithmic differentiation

  // TODO : Need to move the recovery rate bumper function into the recovery rate bumpers class

  // NOTE : The recovery rate is computed according to the following procedure
  // NOTE : 1. Calibrate the hazard rate term structure to the market input data
  // NOTE : 2. Calculate the CDS PV
  // NOTE : 3. Bump the recovery rate and recalculate the CDS PV keeping the hazard rates the same as in step 2 (this is why we do the calibration in this routine)

  // NOTE : recoveryrateBump is input as a percentage e.g. recoveryRate = 0.4 (40%), recoveryrateBump = 0.10 (10%)
  // NOTE : ADDITIVE bump - bumpedRecoveryRate = 0.4 + 0.1 = 0.5 (50%)
  // NOTE : MULTIPLICATIVE bump - bumpedRecoveryRate = 0.4 * (1 + 0.1) = 0.44 (44%)

  // NOTE : We do not enforce recoveryRateBump > 0, therefore the bumped recovery rate can be less than 0% (but this will be caught by the CDS ctor)

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecoveryRate01

  public double getRecoveryRate01CreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double recoveryRateBump,
      final RecoveryRateBumpType recoveryRateBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[marketTenors.length];

    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT365.getDayCountFraction(valuationDate, marketTenors[m]);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the bumped recovery rate
    final double bumpedRecoveryRate = getBumpedRecoveryRate(cds, recoveryRateBump, recoveryRateBumpType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Build a hazard rate curve object based on the input market data
    //final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(times, calibratedHazardRates, 0.0);
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, calibratedHazardRates, 0.0);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a new CDS identical to the input CDS except for the recovery rate
    final LegacyVanillaCreditDefaultSwapDefinition bumpedCDS = cds.withRecoveryRate(bumpedRecoveryRate);

    /*
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap bumpedHazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();
    final double[] bumpedCalibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, bumpedCDS, marketTenors, marketSpreads, yieldCurve, priceType);
    final HazardRateCurve bumpedCalibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, bumpedCalibratedHazardRates, 0.0);
    */

    // Create a CDS PV calculator
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the CDS PV using the just calibrated hazard rate term structure
    final double presentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, cds, yieldCurve, calibratedHazardRateCurve, priceType);

    // Calculate the bumped CDS PV
    final double bumpedPresentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, bumpedCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    //final double bumpedPresentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, bumpedCDS, yieldCurve, bumpedCalibratedHazardRateCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the RecoveryRate01
    final double recoveryRate01 = (bumpedPresentValue - presentValue);

    return recoveryRate01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  private double getBumpedRecoveryRate(final LegacyVanillaCreditDefaultSwapDefinition cds, final double recoveryRateBump, final RecoveryRateBumpType recoveryRateBumpType) {

    double bumpedRecoveryRate = 0.0;

    if (recoveryRateBumpType == RecoveryRateBumpType.ADDITIVE) {
      bumpedRecoveryRate = cds.getRecoveryRate() + recoveryRateBump;
    }

    if (recoveryRateBumpType == RecoveryRateBumpType.MULTIPLICATIVE) {
      bumpedRecoveryRate = cds.getRecoveryRate() * (1 + recoveryRateBump);
    }

    return bumpedRecoveryRate;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
