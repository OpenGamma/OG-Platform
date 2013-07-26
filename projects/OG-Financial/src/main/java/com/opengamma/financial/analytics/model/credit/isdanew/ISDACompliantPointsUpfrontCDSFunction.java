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
public class ISDACompliantPointsUpfrontCDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private final PointsUpFrontConverter _puf = new PointsUpFrontConverter();

  public ISDACompliantPointsUpfrontCDSFunction() {
    super(ValueRequirementNames.POINTS_UPFRONT);
  }

  @Override
  protected Object compute(final ZonedDateTime maturiy, final CDSQuoteConvention quote, final double notional, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, CDSAnalytic[] creditAnalytics, CDSQuoteConvention[] quotes, final ZonedDateTime[] bucketDates) {
    double puf = 0.0;
    //TODO: Move to analytics
    if (quote instanceof PointsUpFront) {
      puf = ((PointsUpFront) quote).getPointsUpFront();
    } else if (quote instanceof QuotedSpread) {
      puf = 100.0 * _puf.quotedSpreadToPUF(analytic,
                                                        quote.getCoupon(),
                                                        yieldCurve,
                                                        ((QuotedSpread) quote).getQuotedSpread());
      // SELL protection reverses directions of legs
      return Double.valueOf(buySellProtection == BuySellProtection.SELL ? -puf : puf);
    } else if (quote instanceof ParSpread) {
      puf = 100.0 * _puf.parSpreadsToPUF(new CDSAnalytic[] {analytic},
                                                        quote.getCoupon(),
                                                        yieldCurve,
                                                        new double[] {((ParSpread) quote).getCoupon()})[0];
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + quote);
    }
    return puf;
  }

}
