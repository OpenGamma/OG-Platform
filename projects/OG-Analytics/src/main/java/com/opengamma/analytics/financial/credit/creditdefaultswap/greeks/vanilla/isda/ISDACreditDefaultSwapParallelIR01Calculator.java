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
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.ISDAHazardRateCurveCalculator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDACreditDefaultSwapPVCalculator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.InterestRateBumpType;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACreditDefaultSwapParallelIR01Calculator {
  private static final ISDAHazardRateCurveCalculator HAZARD_RATE_CALCULATOR = new ISDAHazardRateCurveCalculator();
  private static final ISDACreditDefaultSwapPVCalculator PV_CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final SpreadTermStructureDataChecker DATA_CHECKER = new SpreadTermStructureDataChecker();

  public double getIR01ParallelShiftCreditDefaultSwap(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors, final double[] marketSpreads, final double yieldBump, final InterestRateBumpType yieldBumpType, final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(yieldBumpType, "yield bump type");
    ArgumentChecker.notNull(priceType, "price type");
    ArgumentChecker.notNegative(yieldBump, "yield bump");
    DATA_CHECKER.checkSpreadData(valuationDate, marketTenors, marketSpreads);
    ISDAYieldCurveAndSpreadsProvider calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, marketSpreads, yieldCurve);
    HazardRateCurve hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
    ISDAYieldCurveAndHazardRateCurveProvider curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
    final double presentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
    final Double[] yields = yieldCurve.getCurve().getYData();
    final int nYields = yields.length;
    final double[] bumpedYields = new double[nYields];
    final double bumpInBp = yieldBump / 10000;
    switch (yieldBumpType) {
      case ADDITIVE_PARALLEL:
        for (int i = 0; i < nYields; i++) {
          bumpedYields[i] = yields[i] + bumpInBp;
        }
        break;
      case MULTIPLICATIVE_PARALLEL:
        for (int i = 0; i < nYields; i++) {
          bumpedYields[i] = yields[i] * (1 + bumpInBp);
        }
        break;
      default:
        throw new IllegalArgumentException("Cannot handle bump type " + yieldBumpType);
    }
    final ISDADateCurve bumpedYieldCurve = new ISDADateCurve("Bumped", yieldCurve.getCurveDates(), yieldCurve.getTimePoints(), bumpedYields, yieldCurve.getOffset());
    calibrationData = new ISDAYieldCurveAndSpreadsProvider(marketTenors, marketSpreads, bumpedYieldCurve);
    hazardRateCurve = HAZARD_RATE_CALCULATOR.calibrateHazardRateCurve(cds, calibrationData, valuationDate);
    curveProvider = new ISDAYieldCurveAndHazardRateCurveProvider(bumpedYieldCurve, hazardRateCurve);
    final double bumpedPresentValue = PV_CALCULATOR.getPresentValue(cds, curveProvider, valuationDate, priceType);
    return (bumpedPresentValue - presentValue) / yieldBump;
  }

}
