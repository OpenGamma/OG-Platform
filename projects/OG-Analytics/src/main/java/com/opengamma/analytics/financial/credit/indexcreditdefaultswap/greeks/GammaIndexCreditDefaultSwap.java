/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of gamma for an index CDS (parallel and bucketed bumps)
 */
public class GammaIndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Implement the bump and re-price code

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the gamma by a parallel bump of each point on the spread curve - bump is applied simultaneously to all obligors

  public double getGammaParallelShiftAllObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
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

    double parallelGamma = 0.0;

    return parallelGamma;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the gamma by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors) - bump is applied simultaneously to all obligors

  public double[] getGammaBucketedAllObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
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

    double[] bucketedGamma = new double[numberOfCreditSpreadTenors];

    for (int m = 0; m < numberOfCreditSpreadTenors; m++) {
      bucketedGamma[m] = 0.0;
    }

    return bucketedGamma;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the gamma by a parallel bump of each point on the spread curve - bump is applied individually to all obligors one by one

  public double[] getGammaParallelShiftIndividualObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
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

    double[] parallelGamma = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      parallelGamma[i] = 0.0;
    }

    return parallelGamma;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the gamma by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors) - bump is applied individually to all obligors one by one

  public double[][] getGammaBucketedIndividualObligorsIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
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

    double[][] gamma = new double[numberOfObligors][numberOfCreditSpreadTenors];

    for (int i = 0; i < numberOfObligors; i++) {
      for (int m = 0; m < numberOfCreditSpreadTenors; m++) {
        gamma[i][m] = 0.0;
      }
    }

    return gamma;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
