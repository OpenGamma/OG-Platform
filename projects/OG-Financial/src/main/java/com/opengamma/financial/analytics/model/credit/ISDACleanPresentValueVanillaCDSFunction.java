/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACleanPresentValueVanillaCDSFunction extends ISDAVanillaCDSFunction {

  private static PresentValueCreditDefaultSwap CALCULATOR = new PresentValueCreditDefaultSwap();

  public ISDACleanPresentValueVanillaCDSFunction() {
    super(ValueRequirementNames.CLEAN_PRESENT_VALUE);
  }

  @Override
  protected Object compute(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final double[] spreadCurve, final ISDADateCurve isdaCurve,
      final ZonedDateTime[] bucketDates) {
    return CALCULATOR.calibrateAndGetPresentValue(valuationDate, cds, bucketDates, spreadCurve, isdaCurve, PriceType.CLEAN);
  }

}
