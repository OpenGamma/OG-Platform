/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.SpreadSensitivityCalculator;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantBucketedCS01CDSFunction extends ISDACompliantCDSFunction {

  private SpreadSensitivityCalculator _pricer = new SpreadSensitivityCalculator();

  public ISDACompliantBucketedCS01CDSFunction() {
    super(ValueRequirementNames.BUCKETED_CS01);
  }

  @Override
  protected Object compute(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDACompliantCreditCurve creditCurve,
                           final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, final CDSAnalytic[] curveAnalytics, final double[] spreads) {
    final double[] cs01 = _pricer.bucketedCS01FromParSpreads(analytic,
                                                     cds.getParSpread() * s_tenminus4,
                                                     yieldCurve,
                                                     curveAnalytics,
                                                     spreads,
                                                     s_tenminus4,
                                                     BumpType.ADDITIVE);
    for (int i = 0; i < cs01.length; i++) {
      cs01[i] *= cds.getNotional() * s_tenminus4;
    }
    return cs01;
  }

}
