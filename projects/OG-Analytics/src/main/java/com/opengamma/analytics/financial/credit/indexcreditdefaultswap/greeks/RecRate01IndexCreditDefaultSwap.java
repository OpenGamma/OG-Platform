/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of RecoveryRate01 for an index CDS
 */
public class RecRate01IndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Implement the bump and re-price code

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecRate by a bump to the recovery rates of each of the obligors in the index - bump is applied individually to all obligors one by one

  public double[] getRecRate01IndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDACurve[] yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double recoveryRateBump,
      final RecoveryRateBumpType recoveryRateBumpType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(recoveryRateBumpType, "Recovery rate bump type");

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] recRate01 = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      recRate01[i] = 0.0;
    }

    return recRate01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
