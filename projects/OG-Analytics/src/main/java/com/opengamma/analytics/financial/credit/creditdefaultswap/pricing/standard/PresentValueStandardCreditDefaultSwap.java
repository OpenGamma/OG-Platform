/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.standard;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Class containing the methods for valuing a standard CDS which are common to all types of standard CDS 
 */
public class PresentValueStandardCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private static final double spreadLowerBound = 1e-10;
  private static final double spreadUpperBound = 1e10;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add argument checkers for all the functions
  // TODO : Need to check through these calculations again carefully

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Given a points upfront amount, compute the flat par spread implied by this

  public double calculateParSpreadFlat(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final double upfrontAmount,
      final ZonedDateTime[] marketTenors,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // 1 x 1 vector to hold the flat spread (term structure)
    final double[] marketSpreads = new double[1];

    final Function1D<Double, Double> function = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double parSpread) {

        // For this value of the flat spread, compute the upfront amount
        marketSpreads[0] = parSpread;
        final double pointsUpfront = calculateUpfrontFlat(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

        // Compute the difference between the calculated and input upfront amount
        final double delta = pointsUpfront - upfrontAmount;

        return delta;
      }
    };

    final double parSpreadFlat = new BisectionSingleRootFinder().getRoot(function, spreadLowerBound, spreadUpperBound);

    return parSpreadFlat;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the upfront amount given a specified spread curve level

  public double calculateUpfrontFlat(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vectors of time nodes and spreads for the hazard rate curve (note the sizes of these arrays)
    final double[] times = new double[1];
    final double[] spreads = new double[1];

    times[0] = ACT_365.getDayCountFraction(valuationDate, marketTenors[0]);
    spreads[0] = marketSpreads[0];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS for calibration
    final LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    // Create a CDS for valuation
    final LegacyVanillaCreditDefaultSwapDefinition valuationCDS = cds;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the constructor to create a CDS present value object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, marketTenors, marketSpreads, yieldCurve, PriceType.CLEAN);

    final double[] modifiedHazardRateCurve = new double[1];

    modifiedHazardRateCurve[0] = calibratedHazardRates[0];

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, modifiedHazardRateCurve/*calibratedHazardRates*/, 0.0);

    // Calculate the points upfront
    final double pointsUpfront = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, valuationCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return pointsUpfront / cds.getNotional();
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
