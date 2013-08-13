/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFront;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantUpfrontAmountCDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  public ISDACompliantUpfrontAmountCDSFunction() {
    super(ValueRequirementNames.UPFRONT_AMOUNT);
  }

  @Override
  protected Object compute(final ZonedDateTime maturity,
                           final PointsUpFront puf,
                           CDSQuoteConvention quote,
                           final double notional,
                           final BuySellProtection buySellProtection,
                           final ISDACompliantYieldCurve yieldCurve,
                           final CDSAnalytic analytic,
                           CDSAnalytic[] creditAnalytics,
                           final CDSQuoteConvention[] quotes,
                           final ZonedDateTime[] bucketDates,
                           ISDACompliantCreditCurve creditCurve) {
    // upfront amount is defined as dirty PV
    double cash = (puf.getPointsUpFront() - analytic.getAccruedPremium(puf.getCoupon())) * notional;
    // SELL protection reverses directions of legs
    return Double.valueOf(buySellProtection == BuySellProtection.SELL ? -cash : cash);
  }

}
