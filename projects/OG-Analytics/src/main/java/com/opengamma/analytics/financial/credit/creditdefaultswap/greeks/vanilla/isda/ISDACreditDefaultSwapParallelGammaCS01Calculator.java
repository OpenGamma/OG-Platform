/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndSpreadsProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.CreditSpreadBumpersNew;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.ISDAHazardRateCurveCalculator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDACreditDefaultSwapPVCalculator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACreditDefaultSwapParallelGammaCS01Calculator {
  private static final CreditSpreadBumpersNew SPREAD_BUMPER = new CreditSpreadBumpersNew();
  private static final ISDAHazardRateCurveCalculator HAZARD_RATE_CALCULATOR = new ISDAHazardRateCurveCalculator();
  private static final ISDACreditDefaultSwapPVCalculator PV_CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final SpreadTermStructureDataChecker DATA_CHECKER = new SpreadTermStructureDataChecker();

  public double getGammaCS01ParallelShiftCreditDefaultSwap(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors, final double[] marketSpreads, final double spreadBump, final SpreadBumpType spreadBumpType, final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "price type");
    ArgumentChecker.notNegative(spreadBump, "Spread bump");
    DATA_CHECKER.checkSpreadData(valuationDate, marketTenors, marketSpreads);
    final double[] bumpedUpMarketSpreads = SPREAD_BUMPER.getBumpedCreditSpreads(marketSpreads, spreadBump, spreadBumpType);
    final double[] bumpedDownMarketSpreads = SPREAD_BUMPER.getBumpedCreditSpreads(marketSpreads, -spreadBump, spreadBumpType);
    ISDAYieldCurveAndSpreadsProvider calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, marketSpreads, yieldCurve);
    HazardRateCurve hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
    ISDAYieldCurveAndHazardRateCurveProvider curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
    final double presentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
    calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, bumpedUpMarketSpreads, yieldCurve);
    hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
    curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
    final double bumpedUpPresentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
    calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, bumpedDownMarketSpreads, yieldCurve);
    hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
    curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
    final double bumpedDownPresentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
    return (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);
  }

}
