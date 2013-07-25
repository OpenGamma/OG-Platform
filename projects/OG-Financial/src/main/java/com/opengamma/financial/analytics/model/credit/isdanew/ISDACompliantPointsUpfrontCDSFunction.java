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
  protected Object compute(final ZonedDateTime maturiy, final double parSpread, final double notional, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, CDSAnalytic[] creditAnalytics, CDSQuoteConvention[] quotes, final ZonedDateTime[] bucketDates) {
    CDSQuoteConvention convention = quotes[Math.abs(Arrays.binarySearch(bucketDates, maturiy))];
    double puf = 0.0;
    //TODO: Move to analytics
    if (convention instanceof PointsUpFront) {
      puf = ((PointsUpFront) convention).getPointsUpFront();
    } else if (convention instanceof QuotedSpread) {
      puf = 100.0 * _puf.quotedSpreadToPUF(analytic,
                                                        parSpread * getTenminus4(),
                                                        yieldCurve,
                                                        ((QuotedSpread) convention).getQuotedSpread());
      // SELL protection reverses directions of legs
      return Double.valueOf(buySellProtection == BuySellProtection.SELL ? -puf : puf);
    } else if (convention instanceof ParSpread) {
      puf = 100.0 * _puf.parSpreadsToPUF(new CDSAnalytic[] {analytic},
                                                        parSpread * getTenminus4(),
                                                        yieldCurve,
                                                        new double[] {((ParSpread) convention).getCoupon()})[0];
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + convention);
    }
    return puf;
  }

}
