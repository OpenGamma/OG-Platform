/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantPointsUpfrontCDSFunction extends ISDACompliantCDSFunction {

  private AnalyticCDSPricer _pricer = new AnalyticCDSPricer();

  public ISDACompliantPointsUpfrontCDSFunction() {
    super(ValueRequirementNames.POINTS_UPFRONT);
  }

  @Override
  protected Object compute(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDACompliantCreditCurve creditCurve,
                           final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, CDSAnalytic[] creditAnalytics, final double[] spreads) {
    final FastCreditCurveBuilder creditCurveBuilder = new FastCreditCurveBuilder();
    final ISDACompliantCreditCurve singleTenorCurve = creditCurveBuilder.calibrateCreditCurve(new CDSAnalytic[] {creditAnalytics[0]},
                                                                                              new double[] {spreads[0]},
                                                                                              yieldCurve);
    final double pv = cds.getNotional() * s_tenminus4 * _pricer.pv(analytic,
                                                                   yieldCurve,
                                                                   singleTenorCurve,
                                                                   cds.getParSpread() * s_tenminus4,
                                                                   PriceType.CLEAN);
    // SELL protection reverses directions of legs
    return Double.valueOf(cds.getBuySellProtection() == BuySellProtection.SELL ? - pv : pv);
  }

}
