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
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.SpreadSensitivityCalculator;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantBucketedCS01CDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private SpreadSensitivityCalculator _pricer = new SpreadSensitivityCalculator();
  private final PointsUpFrontConverter _puf = new PointsUpFrontConverter();

  public ISDACompliantBucketedCS01CDSFunction() {
    super(ValueRequirementNames.BUCKETED_CS01);
  }

  @Override
  protected Object compute(final ZonedDateTime maturity, final double parSpread, final double notional, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, final CDSAnalytic[] curveAnalytics, final CDSQuoteConvention[] quotes, final ZonedDateTime[] bucketDates) {
    CDSQuoteConvention convention = quotes[Math.abs(Arrays.binarySearch(bucketDates, maturity))];
    double[] cs01;
    if (convention instanceof PointsUpFront) {
      //TODO: This should be in analytics..
      final double[] quoteValues = new double[quotes.length];
      for (int i = 0; i < quotes.length; i++) {
        quoteValues[i] = _puf.pufToQuotedSpread(analytic, parSpread * getTenminus4(), yieldCurve,
            ((PointsUpFront) convention).getPointsUpFront());
      }
      cs01 = _pricer.bucketedCS01FromQuotedSpreads(analytic, parSpread * getTenminus4(), yieldCurve, curveAnalytics,
                                                   quoteValues, getTenminus4(), BumpType.ADDITIVE);
    } else if (convention instanceof QuotedSpread) {
      final double[] quoteValues = new double[quotes.length];
      for (int i = 0; i < quotes.length; i++) {
        quoteValues[i] = ((QuotedSpread) quotes[i]).getQuotedSpread();
      }
      cs01 = _pricer.bucketedCS01FromQuotedSpreads(analytic, parSpread * getTenminus4(), yieldCurve, curveAnalytics,
                                                   quoteValues, getTenminus4(), BumpType.ADDITIVE);
    } else if (convention instanceof ParSpread) {
      final double[] quoteValues = new double[quotes.length];
      for (int i = 0; i < quotes.length; i++) {
        quoteValues[i] = ((ParSpread) quotes[i]).getCoupon();
      }
      cs01 = _pricer.bucketedCS01FromParSpreads(analytic, parSpread * getTenminus4(), yieldCurve, curveAnalytics,
                                                quoteValues, getTenminus4(), BumpType.ADDITIVE);
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + convention);
    }
    for (int i = 0; i < cs01.length; i++) {
      cs01[i] *= notional * getTenminus4();
    }
    return cs01;
  }

}
