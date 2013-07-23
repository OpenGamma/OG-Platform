/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import java.util.Arrays;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ParSpread;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFront;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.QuotedSpread;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.SpreadSensitivityCalculator;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantParallelCS01CDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private SpreadSensitivityCalculator _pricer = new SpreadSensitivityCalculator();

  public ISDACompliantParallelCS01CDSFunction() {
    super(ValueRequirementNames.PARALLEL_CS01);
  }

  @Override
  protected Object compute(final ZonedDateTime maturity, final double parSpread, final double notional, final BuySellProtection buySellProtection,
      final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, final CDSAnalytic[] curveAnalytics, final
      CDSQuoteConvention[] quotes, final ZonedDateTime[] bucketDates) {
    CDSQuoteConvention convention = quotes[Math.abs(Arrays.binarySearch(bucketDates, maturity))];
    double cs01 = 0.0;
    if (convention instanceof PointsUpFront) {
      cs01 = _pricer.parallelCS01FromPUF(analytic,
                                         parSpread * getTenminus4(),
                                         yieldCurve,
                                         ((PointsUpFront) convention).getPointsUpFront(),
                                         getTenminus4());
    } else if (convention instanceof QuotedSpread) {
      cs01 = _pricer.parallelCS01FromQuotedSpread(analytic, parSpread * getTenminus4(), yieldCurve, ((QuotedSpread) convention).getQuotedSpread(),
                                                  getTenminus4(), BumpType.ADDITIVE);

    } else if (convention instanceof ParSpread) {
      cs01 = _pricer.bucketedCS01FromParSpreads(analytic, parSpread * getTenminus4(), yieldCurve, curveAnalytics,
                                                new double[] {((QuotedSpread) convention).getQuotedSpread()},
                                                getTenminus4(), BumpType.ADDITIVE)[0];
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + convention);
    }
    // SELL protection reverses directions of legs
    return Double.valueOf(cs01 * notional * getTenminus4());
  }

}
