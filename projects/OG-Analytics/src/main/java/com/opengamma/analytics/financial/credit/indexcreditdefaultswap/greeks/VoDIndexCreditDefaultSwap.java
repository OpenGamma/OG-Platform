/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligor.DefaultState;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of Value-on-Default for an index CDS
 */
public class VoDIndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Implement the bump and re-price code

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the Value-on-Default for each of the obligors in the index - VoD is calculated individually to all obligors one by one

  public double[] getVoDIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] valueOnDefault = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      valueOnDefault[i] = 0.0;
    }

    return valueOnDefault;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the Value-on-Default for a user specified subset of obligors in the index e.g. what is the VoD if all financials defaulted

  public double getVoDIndexScenarioCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final DefaultState[] defaultScenario) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(defaultScenario, "Default scenario");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double runningVOD = 0.0;

    for (int i = 0; i < numberOfObligors; i++) {
      if (defaultScenario.equals(DefaultState.DEFAULTED)) {
        runningVOD += 0.0;
      }
    }

    return runningVOD;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
