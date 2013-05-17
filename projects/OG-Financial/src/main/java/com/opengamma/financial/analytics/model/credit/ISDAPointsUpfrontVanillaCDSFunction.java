/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.standard.PresentValueStandardCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Function to calculate the points upfront for a given CDS instrument.
 */
public class ISDAPointsUpfrontVanillaCDSFunction extends ISDAVanillaCDSFunction {

  //private static PresentValueCreditDefaultSwap CALCULATOR = new PresentValueCreditDefaultSwap();
  private static PresentValueStandardCreditDefaultSwap CALCULATOR = new PresentValueStandardCreditDefaultSwap();

  public ISDAPointsUpfrontVanillaCDSFunction() {
    super(ValueRequirementNames.POINTS_UPFRONT);
  }

  @Override
  protected Object compute(final ZonedDateTime now, LegacyVanillaCreditDefaultSwapDefinition cds, final double[] spreads, final ISDADateCurve isdaCurve, final ZonedDateTime[] bucketDates) {
    final ZonedDateTime[] singleCalibrationTenor = {cds.getMaturityDate() };
    final double[] singleSpreadTermStructure = {spreads[0] };
    double points = CALCULATOR.calculateUpfrontFlat(now, cds, singleCalibrationTenor, singleSpreadTermStructure, isdaCurve, PriceType.CLEAN); // take values from requirements
    return 100.0 * points;
  }

}
