/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of Value-on-Default for an index CDS
 */
public class VoDIndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Implement the bump and re-price code

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecRate by a bump to the recovery rates of each of the obligors in the index - bump is applied individually to all obligors one by one

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
}
