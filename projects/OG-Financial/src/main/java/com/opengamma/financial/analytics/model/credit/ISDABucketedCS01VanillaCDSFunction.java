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
 * Function to calculate the bucketed CS01 for a given CDS instrument.
 */
public class ISDABucketedCS01VanillaCDSFunction extends ISDAVanillaCDSFunction {

  private static CS01CreditDefaultSwap CALCULATOR = new CS01CreditDefaultSwap();

  public ISDABucketedCS01VanillaCDSFunction() {
    super(ValueRequirementNames.BUCKETED_CS01);
  }

  @Override
  protected Object compute(final ZonedDateTime now, LegacyVanillaCreditDefaultSwapDefinition cds, final double[] spreads, final ISDADateCurve isdaCurve, final ZonedDateTime[] bucketDates) {
    double[] bucketedCS01 = CALCULATOR.getCS01BucketedCreditDefaultSwap(now, cds, isdaCurve, bucketDates, spreads, 1.0, SpreadBumpType.ADDITIVE_BUCKETED,
        PriceType.CLEAN); // take values from requirements
    return bucketedCS01;
  }

}
