/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantAccruedDaysCDSFunction extends AbstractISDACompliantCDSFunction {

  public ISDACompliantAccruedDaysCDSFunction() {
    super(ValueRequirementNames.ACCRUED_DAYS);
  }

  @Override
  protected Object compute(final double parSpread, final double notional, final CDSAnalytic analytic) {
    return Double.valueOf(analytic.getAccuredDays()); //TODO: Should be an int
  }

}
