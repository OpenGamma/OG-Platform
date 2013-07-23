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
public class ISDACompliantAccruedPremiumCDSFunction extends AbstractISDACompliantCDSFunction {

  public ISDACompliantAccruedPremiumCDSFunction() {
    super(ValueRequirementNames.ACCRUED_PREMIUM);
  }

  @Override
  protected Object compute(final double parSpread, final double notional, final CDSAnalytic analytic) {
    return Double.valueOf(analytic.getAccruedPremium(parSpread * s_tenminus4) * notional);
  }

}
