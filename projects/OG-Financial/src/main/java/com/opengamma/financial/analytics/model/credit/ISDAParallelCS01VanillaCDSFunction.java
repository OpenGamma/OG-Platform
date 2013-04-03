/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;


import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.CS01CreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Function to calculate the parallel CS01 for a given CDS instrument.
 */
public class ISDAParallelCS01VanillaCDSFunction extends ISDAVanillaCDSFunction {

  private static CS01CreditDefaultSwap CALCULATOR = new CS01CreditDefaultSwap();

  public ISDAParallelCS01VanillaCDSFunction() {
    super(ValueRequirementNames.PARALLEL_CS01);
  }

  @Override
  protected Object compute(final ZonedDateTime now, LegacyVanillaCreditDefaultSwapDefinition cds, final double[] spreads, final ISDADateCurve isdaCurve, final ZonedDateTime[] bucketDates) {
    double cs01 = CALCULATOR.getCS01ParallelShiftCreditDefaultSwap(now, cds, isdaCurve, bucketDates, spreads, 1.0, SpreadBumpType.ADDITIVE_PARALLEL,
        PriceType.CLEAN); // take values from requirements
    return cs01;
  }

}
