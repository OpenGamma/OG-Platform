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
import com.opengamma.analytics.financial.credit.hazardratemodel.CalibrateHazardRateCurve;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of CS01 for a vanilla Legacy CDS
 */
public class CS01LegacyCreditDefaultSwap {

  // -------------------------------------------------------------------------------------------------

  private final double _tolerance = 1e-15;

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments e.g. tenors in increasing order

  // -------------------------------------------------------------------------------------------------

  public double getCS01ParallelShiftCreditDefaultSwap(
      LegacyCreditDefaultSwapDefinition cds,
      ISDACurve yieldCurve,
      ZonedDateTime[] marketTenors,
      double[] marketSpreads,
      double spreadBump,
      SpreadBumpType spreadBumpType) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null

    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    // Check that the number of input tenors matches the number of input spreads
    ArgumentChecker.isTrue(marketTenors.length == marketSpreads.length, "Number of tenors and number of spreads should be equal");

    // Check the efficacy of the input market data
    for (int m = 0; m < marketTenors.length; m++) {

      ArgumentChecker.isTrue(marketTenors[m].isAfter(cds.getValuationDate()), "Calibration instrument of tenor {} is before the valuation date {}", marketTenors[m], cds.getValuationDate());

      if (marketTenors.length > 1 && m > 0) {
        ArgumentChecker.isTrue(marketTenors[m].isAfter(marketTenors[m - 1]), "Tenors not in ascending order");
      }

      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + marketTenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + marketTenors[m]);
    }

    // -------------------------------------------------------------

    double[] bumpedMarketSpreads = new double[marketSpreads.length];

    double[] times = new double[marketTenors.length];

    // -------------------------------------------------------------

    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT365.getDayCountFraction(cds.getValuationDate(), marketTenors[m]);
    }

    // Calculate the bumped spreads
    for (int m = 0; m < marketTenors.length; m++) {
      if (spreadBumpType == SpreadBumpType.ADDITIVE_PARALLEL) {
        bumpedMarketSpreads[m] = marketSpreads[m] + spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_PARALLEL) {
        bumpedMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
      }
    }

    // -------------------------------------------------------------

    // Create a CDS for calibration 
    LegacyCreditDefaultSwapDefinition calibrationCDS = cds;

    // Create a CDS for valuation
    LegacyCreditDefaultSwapDefinition valuationCDS = cds;

    // -------------------------------------------------------------

    // Call the constructor to create a CDS present value object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurve hazardRateCurve = new CalibrateHazardRateCurve();

    // -------------------------------------------------------------

    //   Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(calibrationCDS, marketTenors, marketSpreads, yieldCurve);

    // Calibrate a hazard rate curve to the bumped market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    double[] bumpedCalibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(calibrationCDS, marketTenors, bumpedMarketSpreads, yieldCurve);

    // -------------------------------------------------------------

    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(times, calibratedHazardRates, 0.0);

    final HazardRateCurve bumpedCalibratedHazardRateCurve = new HazardRateCurve(times, bumpedCalibratedHazardRates, 0.0);

    // -------------------------------------------------------------

    // Calculate the unbumped CDS PV using the just calibrated hazard rate term structure
    double presentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(valuationCDS, yieldCurve, calibratedHazardRateCurve);

    // Calculate the bumped CDS PV using the just calibrated bumped hazard rate term structure
    double bumpedPresentValue = creditDefaultSwap.getPresentValueCreditDefaultSwap(valuationCDS, yieldCurve, bumpedCalibratedHazardRateCurve);

    // -------------------------------------------------------------

    // Calculate the parallel CS01
    double parallelCS01 = (bumpedPresentValue - presentValue) / spreadBump;

    return parallelCS01;
  }

  // -------------------------------------------------------------------------------------------------

  public double[] getCS01BucketedCfreditDefaultSwap(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null

    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");

    // -------------------------------------------------------------

    double[] bucketedCS01 = new double[10];

    return bucketedCS01;
  }

  // -------------------------------------------------------------------------------------------------

}
