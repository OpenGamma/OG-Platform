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
public class ISDACompliantAccruedPremiumCDSFunction extends AbstractISDACompliantCDSFunction {

  public ISDACompliantAccruedPremiumCDSFunction() {
    super(ValueRequirementNames.ACCRUED_PREMIUM);
  }

  @Override
  protected Object compute(final double parSpread, final double notional, final CDSAnalytic analytic) {
    return Double.valueOf(analytic.getAccruedPremium(parSpread * getTenminus4()) * notional);
  }

}
