/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
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
