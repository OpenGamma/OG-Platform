/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantCleanPriceCDSFunction extends AbstractISDACompliantWithCreditCurveCDSFunction {

  private PointsUpFrontConverter _pricer = new PointsUpFrontConverter();

  public ISDACompliantCleanPriceCDSFunction() {
    super(ValueRequirementNames.CLEAN_PRICE);
  }

  @Override
  protected Object compute(final double parSpread, final double notional, final BuySellProtection buySellProtection, final ISDACompliantCreditCurve creditCurve,
      final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic) {
    return Double.valueOf(100.0 * _pricer.cleanPrice(analytic, yieldCurve, creditCurve, parSpread * getTenminus4()));
  }

}
