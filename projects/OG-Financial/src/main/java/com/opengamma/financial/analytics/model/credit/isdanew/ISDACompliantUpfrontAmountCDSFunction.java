/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import java.util.Arrays;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ParSpread;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFront;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.QuotedSpread;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantUpfrontAmountCDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private AnalyticCDSPricer _pricer = new AnalyticCDSPricer();
  private PointsUpFrontConverter _pufConverter = new PointsUpFrontConverter();

  public ISDACompliantUpfrontAmountCDSFunction() {
    super(ValueRequirementNames.UPFRONT_AMOUNT);
  }

  @Override
  protected Object compute(final ZonedDateTime maturity, final double parSpread, final double notional, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, CDSAnalytic[] creditAnalytics, final CDSQuoteConvention[] quotes, final ZonedDateTime[] bucketDates) {
    // upfront amount is defined as PV at first tenor point
    CDSQuoteConvention convention = quotes[Math.abs(Arrays.binarySearch(bucketDates, maturity))];
    double cash;
    if (convention instanceof PointsUpFront) {
      cash = ((PointsUpFront) convention).getPointsUpFront() - analytic.getAccruedPremium(parSpread * getTenminus4()) * notional;
    } else if (convention instanceof QuotedSpread) {
      final PointsUpFront puf = _pufConverter.toPointsUpFront(analytic, (QuotedSpread) convention, yieldCurve);
      cash = puf.getPointsUpFront() - analytic.getAccruedPremium(parSpread * getTenminus4()) * notional;
    } else if (convention instanceof ParSpread) {
      final double puf = _pufConverter.parSpreadsToPUF(new CDSAnalytic[] {analytic}, ((ParSpread) convention).getCoupon() * getTenminus4(),
                                                              yieldCurve, new double[] {parSpread * getTenminus4()})[0];
      cash = puf - analytic.getAccruedPremium(parSpread * getTenminus4()) * notional;
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + convention);
    }
    // SELL protection reverses directions of legs
    return Double.valueOf(buySellProtection == BuySellProtection.SELL ? -cash : cash);
  }

}
