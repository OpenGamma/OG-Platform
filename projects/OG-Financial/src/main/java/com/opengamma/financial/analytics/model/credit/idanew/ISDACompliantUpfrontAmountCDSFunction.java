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
public class ISDACompliantUpfrontAmountCDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private AnalyticCDSPricer _pricer = new AnalyticCDSPricer();

  public ISDACompliantUpfrontAmountCDSFunction() {
    super(ValueRequirementNames.UPFRONT_AMOUNT);
  }

  @Override
  protected Object compute(final double parSpread, final double notional, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, CDSAnalytic[] creditAnalytics, final double[] spreads) {
    // upfront amount is defined as PV at first tenor point
    final FastCreditCurveBuilder creditCurveBuilder = new FastCreditCurveBuilder();
    final ISDACompliantCreditCurve singleTenorCurve = creditCurveBuilder.calibrateCreditCurve(creditAnalytics[0], spreads[0], yieldCurve);
    final double pv = notional * _pricer.pv(analytic, yieldCurve, singleTenorCurve, parSpread * s_tenminus4,  PriceType.DIRTY);
    // SELL protection reverses directions of legs
    return Double.valueOf(buySellProtection == BuySellProtection.SELL ? -pv : pv);
  }

}
