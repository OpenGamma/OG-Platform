/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ParSpread;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.SpreadSensitivityCalculator;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.engine.value.ValueRequirementNames;

import ch.qos.logback.classic.net.SyslogAppender;

/**
 *
 */
public class ISDACompliantParallelCS01CDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private SpreadSensitivityCalculator _pricer = new SpreadSensitivityCalculator();

  public ISDACompliantParallelCS01CDSFunction() {
    super(ValueRequirementNames.PARALLEL_CS01);
  }

  @Override

  protected Object compute(final ZonedDateTime maturity, CDSQuoteConvention quote, final double notional, final BuySellProtection buySellProtection,
      final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, final CDSAnalytic[] curveAnalytics, final
      CDSQuoteConvention[] quotes, final ZonedDateTime[] bucketDates) {

    double cs01;
    if (quote instanceof ParSpread) {
      // use a slightly different methodology on non IMM dates
      final double spreads[] = new double[quotes.length];
      for (int i = 0; i < spreads.length; i++) {
        spreads [i] = quotes[i].getCoupon();
      }
      cs01 = _pricer.parallelCS01FromParSpreads(analytic, quote.getCoupon(), yieldCurve, curveAnalytics, spreads, getTenminus4(), BumpType.ADDITIVE);
    } else {
      cs01 = _pricer.parallelCS01(analytic, quote, yieldCurve, getTenminus4());
    }

    // SELL protection reverses directions of legs
    return Double.valueOf(cs01 * notional * getTenminus4());
  }
}