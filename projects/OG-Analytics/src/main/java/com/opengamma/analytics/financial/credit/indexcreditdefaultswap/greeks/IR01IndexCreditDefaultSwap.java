/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdayieldcurve.InterestRateBumpType;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of IR01 for an index CDS (parallel and bucketed bumps)
 */
public class IR01IndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Implement the bump and re-price code

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the IR01 by a parallel bump of each point on the yield curve - bump is applied simultaneously to all obligors

  public double getIR01ParallelShiftAllObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double interestRateBump,
      final InterestRateBumpType interestRateBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(interestRateBumpType, "Interest rate bump type");

    ArgumentChecker.notNegative(interestRateBump, "Interest rate bump");

    double parallelIR01 = 0.0;

    return parallelIR01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the IR01 by bumping each point on the yield curve individually by interestRateBump (bump is same for all tenors) - bump is applied simultaneously to all obligors

  public double[] getIR01BucketedAllObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double interestRateBump,
      final InterestRateBumpType interestRateBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(interestRateBumpType, "Interest rate bump type");

    ArgumentChecker.notNegative(interestRateBump, "Interest rate bump");

    int numberOfYieldCurveTenors = 1; //indexCDS.getUnderlyingPool().getNumberOfCreditSpreadTenors();

    double[] bucketedIR01 = new double[numberOfYieldCurveTenors];

    for (int m = 0; m < numberOfYieldCurveTenors; m++) {
      bucketedIR01[m] = 0.0;
    }

    return bucketedIR01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the IR01 by a parallel bump of each point on the yield curve - bump is applied individually to all obligors one by one

  public double[] getIR01ParallelShiftIndividualObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double interestRateBump,
      final InterestRateBumpType interestRateBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(interestRateBumpType, "Interest rate bump type");

    ArgumentChecker.notNegative(interestRateBump, "Interest rate bump");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] parallelIR01 = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      parallelIR01[i] = 0.0;
    }

    return parallelIR01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the IR01 by bumping each point on the yield curve individually by interestRateBump (bump is same for all tenors) - bump is applied individually to all obligors one by one

  public double[][] getIR01BucketedIndividualObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double interestRateBump,
      final InterestRateBumpType interestRateBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(interestRateBumpType, "Interest rate bump type");

    ArgumentChecker.notNegative(interestRateBump, "Interest rate bump");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();
    int numberOfYieldCurveTenors = marketTenors.length;

    double[][] ir01 = new double[numberOfObligors][numberOfYieldCurveTenors];

    for (int i = 0; i < numberOfObligors; i++) {
      for (int m = 0; m < numberOfYieldCurveTenors; m++) {
        ir01[i][m] = 0.0;
      }
    }

    return ir01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
