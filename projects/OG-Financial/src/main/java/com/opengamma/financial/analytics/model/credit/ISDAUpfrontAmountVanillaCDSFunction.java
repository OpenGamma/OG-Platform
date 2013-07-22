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
 * Function to calculate the upfront amount for a given CDS instrument.
 */
public class ISDAUpfrontAmountVanillaCDSFunction extends ISDAVanillaCDSFunction {

  private static final PresentValueCreditDefaultSwap CALCULATOR = new PresentValueCreditDefaultSwap();

  public ISDAUpfrontAmountVanillaCDSFunction() {
    super(ValueRequirementNames.UPFRONT_AMOUNT);
  }

  @Override
  @SuppressWarnings("deprecation")
  protected Object compute(final ZonedDateTime now, LegacyVanillaCreditDefaultSwapDefinition cds, final double[] spreads, final ISDADateCurve isdaCurve, final ZonedDateTime[] bucketDates) {
    final ZonedDateTime[] singleCalibrationTenor = {cds.getMaturityDate() };
    final double[] singleSpreadTermStructure = {spreads[0] };
    return CALCULATOR.calibrateAndGetPresentValue(now, cds, singleCalibrationTenor, singleSpreadTermStructure, isdaCurve, PriceType.DIRTY); // take values from requirements
  }

}
