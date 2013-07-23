/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class ISDACompliantDirtyPresentValueCDSFunction extends AbstractISDACompliantWithCreditCurveCDSFunction {

  private AnalyticCDSPricer _pricer = new AnalyticCDSPricer();

  public ISDACompliantDirtyPresentValueCDSFunction() {
    super(ValueRequirementNames.DIRTY_PRESENT_VALUE);
  }

  @Override
  protected Object compute(final double parSpread, final double notional, final BuySellProtection buySellProtection, final ISDACompliantCreditCurve creditCurve,
      final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic) {
    final double pv = notional * _pricer.pv(analytic, yieldCurve, creditCurve, parSpread * getTenminus4(), PriceType.DIRTY);
    // SELL protection reverses directions of legs
    return Double.valueOf(buySellProtection == BuySellProtection.SELL ? -pv : pv);
  }

}
