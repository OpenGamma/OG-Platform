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
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantPrincipalCDSFunction extends ISDACompliantCDSFunction {

  private PointsUpFrontConverter _pricer = new PointsUpFrontConverter();

  public ISDACompliantPrincipalCDSFunction() {
    super(ValueRequirementNames.PRINCIPAL);
  }

  @Override
  protected Object compute(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDACompliantCreditCurve creditCurve,
                           final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, CDSAnalytic[] creditAnalytics, final double[] spreads) {
    return _pricer.principle(cds.getNotional(), analytic, yieldCurve, creditCurve, cds.getParSpread() * s_tenminus4);
  }

}
