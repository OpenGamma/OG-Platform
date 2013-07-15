/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
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
public class ISDACreditDefaultSwapRR01Calculator {
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final ISDACreditDefaultSwapPVCalculator CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final ISDAHazardRateCurveCalibrationCalculator HAZARD_RATE_CALCULATOR = new ISDAHazardRateCurveCalibrationCalculator();

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments
  // TODO : Need to consider more sophisticated sensitivity calculations e.g. algorithmic differentiation

  // TODO : Need to move the recovery rate bumper function into the recovery rate bumpers class

  // NOTE : The recovery rate is computed according to the following procedure
  // NOTE : 1. Calibrate the hazard rate term structure to the market input data
  // NOTE : 2. Calculate the CDS PV
  // NOTE : 3. Bump the recovery rate and recalculate the CDS PV keeping the hazard rates the same as in step 2 (this is why we do the calibration in this routine)

  // NOTE : recoveryrateBump is input as a percentage e.g. recoveryRate = 0.4 (40%), recoveryrateBump = 0.10 (10%)
  // NOTE : ADDITIVE bump - bumpedRecoveryRate = 0.4 + 0.1 = 0.5 (50%)
  // NOTE : MULTIPLICATIVE bump - bumpedRecoveryRate = 0.4 * (1 + 0.1) = 0.44 (44%)

  // NOTE : We do not enforce recoveryRateBump > 0, therefore the bumped recovery rate can be less than 0% (but this will be caught by the CDS ctor)

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecoveryRate01

  public double getRecoveryRate01CreditDefaultSwap(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors, final double[] marketSpreads, final double recoveryRateBump, final RecoveryRateBumpType recoveryRateBumpType,
      final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(priceType, "price type");
    CreditMarketDataUtils.checkSpreadData(valuationDate, marketTenors, marketSpreads);
    final double[] times = new double[marketTenors.length];
    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m]);
    }
    final double[] calibratedHazardRates = HAZARD_RATE_CALCULATOR.getCalibratedHazardRateTermStructure(valuationDate, cds, marketTenors,
        marketSpreads, yieldCurve, priceType);
    final double bumpedRecoveryRate = getBumpedRecoveryRate(cds, recoveryRateBump, recoveryRateBumpType);
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, calibratedHazardRates, 0.0);
    final CreditDefaultSwapDefinition bumpedCDS = cds.withRecoveryRate(bumpedRecoveryRate);
    final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, calibratedHazardRateCurve);
    final double presentValue = CALCULATOR.getPresentValue(cds, curves, valuationDate, priceType);
    final double bumpedPresentValue = CALCULATOR.getPresentValue(bumpedCDS, curves, valuationDate, priceType);
    return bumpedPresentValue - presentValue;
  }

  private double getBumpedRecoveryRate(final CreditDefaultSwapDefinition cds, final double recoveryRateBump, final RecoveryRateBumpType recoveryRateBumpType) {
    switch(recoveryRateBumpType) {
      case ADDITIVE:
        return cds.getRecoveryRate() + recoveryRateBump;
      case MULTIPLICATIVE:
        return cds.getRecoveryRate() * (1 + recoveryRateBump);
      default:
        throw new IllegalArgumentException("Cannot handle bump type " + recoveryRateBumpType);
    }
  }
}
