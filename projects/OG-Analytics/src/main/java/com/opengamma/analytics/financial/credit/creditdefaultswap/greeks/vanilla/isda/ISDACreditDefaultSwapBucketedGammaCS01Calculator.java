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
public class ISDACreditDefaultSwapBucketedGammaCS01Calculator {
  private static final CreditSpreadBumpersNew SPREAD_BUMPER = new CreditSpreadBumpersNew();
  private static final ISDAHazardRateCurveCalculator HAZARD_RATE_CALCULATOR = new ISDAHazardRateCurveCalculator();
  private static final ISDACreditDefaultSwapPVCalculator PV_CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final SpreadTermStructureDataChecker DATA_CHECKER = new SpreadTermStructureDataChecker();

  public double[] getGammaCS01BucketedCreditDefaultSwap(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors, final double[] marketSpreads, final double spreadBump, final SpreadBumpType spreadBumpType, final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "price type");
    ArgumentChecker.notNegative(spreadBump, "Spread bump");
    DATA_CHECKER.checkSpreadData(valuationDate, marketTenors, marketSpreads);
    final int nSpreads = marketSpreads.length;
    final double[] bucketedGammaCS01 = new double[nSpreads];
    ISDAYieldCurveAndSpreadsProvider calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, marketSpreads, yieldCurve);
    HazardRateCurve hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
    ISDAYieldCurveAndHazardRateCurveProvider curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
    final double presentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
    // Loop through and bump each of the spreads at each tenor
    for (int m = 0; m < nSpreads; m++) {
      // Calculate the bumped spreads vector
      final double[] bumpedUpMarketSpreads = SPREAD_BUMPER.getBumpedCreditSpreads(marketSpreads, m, spreadBump, spreadBumpType);
      // Calculate the bumped CDS PV
      calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, bumpedUpMarketSpreads, yieldCurve);
      hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
      curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
      final double bumpedUpPresentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
      // Compute the gamma CS01 for this tenor
      final double[] bumpedDownMarketSpreads = SPREAD_BUMPER.getBumpedCreditSpreads(marketSpreads, m, -spreadBump, spreadBumpType);
      calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, bumpedDownMarketSpreads, yieldCurve);
      hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
      curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
      final double bumpedDownPresentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
      bucketedGammaCS01[m] = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);
    }
    return bucketedGammaCS01;
  }


}
