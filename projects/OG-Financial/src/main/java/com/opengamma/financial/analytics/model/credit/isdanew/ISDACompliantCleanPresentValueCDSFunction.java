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
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFront;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantCleanPresentValueCDSFunction extends AbstractISDACompliantWithSpreadsCDSFunction {

  private final PointsUpFrontConverter _puf = new PointsUpFrontConverter();

  public ISDACompliantCleanPresentValueCDSFunction() {
    super(ValueRequirementNames.CLEAN_PRESENT_VALUE);
  }

  @Override
  protected Object compute(final ZonedDateTime maturiy,
                           final PointsUpFront puf,
                           final CDSQuoteConvention quote,
                           final double notional,
                           final BuySellProtection buySellProtection,
                           final ISDACompliantYieldCurve yieldCurve,
                           final CDSAnalytic analytic,
                           CDSAnalytic[] creditAnalytics,
                           CDSQuoteConvention[] quotes,
                           ISDACompliantCreditCurve creditCurve) {
    return Double.valueOf(puf.getPointsUpFront() * notional);
  }
}
