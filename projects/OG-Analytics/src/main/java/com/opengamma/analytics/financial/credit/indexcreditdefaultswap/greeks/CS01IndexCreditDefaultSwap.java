/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

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

  // TODO : Implement the bump and re-price code

  // TODO : Add function to bump a subset of obligors specified by the user

  // ------------------------------------------------------------------------------------------------------------------------------------------

  private static final CalibrateIndexCreditDefaultSwap calibrateHazardRateCurves = new CalibrateIndexCreditDefaultSwap();

  // Construct an index present value calculator object
  private static final PresentValueIndexCreditDefaultSwap indexPresentValue = new PresentValueIndexCreditDefaultSwap();

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

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "YieldCurves");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index
    final HazardRateCurve[] hazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, marketSpreads, yieldCurves);

    final int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] breakevenSpreads = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      breakevenSpreads[i] = marketSpreads[i][0];
    }

    // Calculate the value of the index
    final double presentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    double parallelCS01 = 0.0;

    return parallelCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors) - bump is applied simultaneously to all obligors

  public double[] getCS01BucketedAllObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    int numberOfCreditSpreadTenors = marketTenors.length;

    double[] bucketedCS01 = new double[numberOfCreditSpreadTenors];

    for (int m = 0; m < numberOfCreditSpreadTenors; m++) {
      bucketedCS01[m] = 0.0;
    }

    return bucketedCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by a parallel bump of each point on the spread curve - bump is applied individually to all obligors one by one

  public double[] getCS01ParallelShiftIndividualObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] parallelCS01 = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      parallelCS01[i] = 0.0;
    }

    return parallelCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors) - bump is applied individually to all obligors one by one

  public double[][] getCS01BucketedIndividualObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();
    int numberOfCreditSpreadTenors = marketTenors.length;

    double[][] cs01 = new double[numberOfObligors][numberOfCreditSpreadTenors];

    for (int i = 0; i < numberOfObligors; i++) {
      for (int m = 0; m < numberOfCreditSpreadTenors; m++) {
        cs01[i][m] = 0.0;
      }
    }

    return cs01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
