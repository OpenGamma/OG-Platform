/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.CreditSpreadBumpers;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.calibration.CalibrateIndexCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.pricing.PresentValueIndexCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of CS01 for an index CDS (parallel and bucketed bumps)
 */
public class CS01IndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add function to bump a subset of obligors specified by the user
  // TODO : Need to sort out the breakevenSpreads input to the pricer

  // ------------------------------------------------------------------------------------------------------------------------------------------

  private static final CalibrateIndexCreditDefaultSwap calibrateHazardRateCurves = new CalibrateIndexCreditDefaultSwap();

  // Construct an index present value calculator object
  private static final PresentValueIndexCreditDefaultSwap indexPresentValue = new PresentValueIndexCreditDefaultSwap();

  // Create an object to assist with the spread bumping
  private static final CreditSpreadBumpers spreadBumper = new CreditSpreadBumpers();

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by a parallel bump of each point on the spread curve - bump is applied simultaneously to all obligors

  public double getCS01ParallelShiftAllObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    // ------------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "YieldCurves");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // ------------------------------------------------------------------------------------------------------------------------------------------

    final int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();
    final int numberOfTenors = marketTenors.length;

    // Get the bumped credit spreads
    double[][] bumpedMarketSpreads = spreadBumper.getBumpedCreditSpreads(numberOfObligors, numberOfTenors, marketSpreads, spreadBump, spreadBumpType);

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the unbumped credit spreads
    final HazardRateCurve[] hazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, marketSpreads, yieldCurves);

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the unbumped credit spreads
    final HazardRateCurve[] bumpedHazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, bumpedMarketSpreads, yieldCurves);

    double[] breakevenSpreads = new double[numberOfObligors];
    double[] bumpedBreakevenSpreads = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      breakevenSpreads[i] = marketSpreads[i][0];
      bumpedBreakevenSpreads[i] = bumpedMarketSpreads[i][0];
    }

    // Calculate the unbumped value of the index
    final double presentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    // Calculate the unbumped value of the index
    final double bumpedPresentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, bumpedBreakevenSpreads, yieldCurves, bumpedHazardRateCurves);

    // Compute the parallel CS01
    double parallelCS01 = (bumpedPresentValue - presentValue) / spreadBump;

    return parallelCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by bumping each point on the spread curve simultaneously by spreadBump (bump is same for all tenors) - bump is applied one-by-one to each obligor

  public double[] getCS01BucketedByObligorIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    // ------------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // -----------------------------------------------------------------------------------------------------------------------------------------

    final int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();
    final int numberOfTenors = marketTenors.length;

    double[] bucketedByObligorCS01 = new double[numberOfObligors];

    double[] breakevenSpreads = new double[numberOfObligors];
    double[] bumpedBreakevenSpreads = new double[numberOfObligors];

    for (int j = 0; j < numberOfObligors; j++) {
      breakevenSpreads[j] = marketSpreads[j][0];
    }

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the unbumped credit spreads
    final HazardRateCurve[] hazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, marketSpreads, yieldCurves);

    // Calculate the unbumped value of the index
    final double presentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    for (int i = 0; i < numberOfObligors; i++) {

      // TODO : There is a bug here. The marketSpreads matrix is erroneously being bumped
      final double[][] bumpedMarketSpreads = spreadBumper.getBumpedCreditSpreads(numberOfObligors, numberOfTenors, i, marketSpreads, spreadBump, spreadBumpType);

      for (int j = 0; j < numberOfObligors; j++) {
        bumpedBreakevenSpreads[j] = bumpedMarketSpreads[j][0];
      }

      // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the unbumped credit spreads
      final HazardRateCurve[] bumpedHazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, bumpedMarketSpreads, yieldCurves);

      // Calculate the unbumped value of the index
      final double bumpedPresentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, bumpedBreakevenSpreads, yieldCurves, bumpedHazardRateCurves);

      // Calculate the parallel CS01 for obligor i
      bucketedByObligorCS01[i] = (bumpedPresentValue - presentValue) / spreadBump;
    }

    return bucketedByObligorCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
