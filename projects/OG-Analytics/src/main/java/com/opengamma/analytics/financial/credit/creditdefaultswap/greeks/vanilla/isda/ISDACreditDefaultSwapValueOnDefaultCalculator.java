/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.ISDAHazardRateCurveCalibrationCalculator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDACreditDefaultSwapPVCalculator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.util.CreditMarketDataUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACreditDefaultSwapValueOnDefaultCalculator {
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final ISDACreditDefaultSwapPVCalculator CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final ISDAHazardRateCurveCalibrationCalculator HAZARD_RATE_CALCULATOR = new ISDAHazardRateCurveCalibrationCalculator();

  public double getValueOnDefaultCreditDefaultSwap(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors, final double[] marketSpreads, final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    CreditMarketDataUtils.checkSpreadData(valuationDate, marketTenors, marketSpreads);
    final double[] times = new double[marketTenors.length];
    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m]);
    }
    final double[] calibratedHazardRates = HAZARD_RATE_CALCULATOR.getCalibratedHazardRateTermStructure(valuationDate, cds, marketTenors,
        marketSpreads, yieldCurve, priceType);
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, calibratedHazardRates, 0.0);
    final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, calibratedHazardRateCurve);
    final double presentValue = CALCULATOR.getPresentValue(cds, curves, valuationDate, priceType);
    final double lossGivenDefault = cds.getNotional() * (1 - cds.getRecoveryRate());
    double valueOnDefault = 0.0;
    if (cds.getBuySellProtection() == BuySellProtection.BUY) {
      valueOnDefault = -presentValue + lossGivenDefault;
    } else {
      valueOnDefault = -presentValue - lossGivenDefault;
    }
    return valueOnDefault;
  }

}
