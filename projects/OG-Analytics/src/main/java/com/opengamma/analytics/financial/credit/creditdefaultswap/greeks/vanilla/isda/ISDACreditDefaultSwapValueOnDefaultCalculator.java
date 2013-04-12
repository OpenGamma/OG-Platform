/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.ISDAHazardRateCurveCalculator;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDACreditDefaultSwapPVCalculator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACreditDefaultSwapValueOnDefaultCalculator {
  private static final ISDAHazardRateCurveCalculator HAZARD_RATE_CALCULATOR = new ISDAHazardRateCurveCalculator();
  private static final ISDACreditDefaultSwapPVCalculator PV_CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final SpreadTermStructureDataChecker DATA_CHECKER = new SpreadTermStructureDataChecker();
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  public double getValueOnDefaultCreditDefaultSwap(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors, final double[] marketSpreads, final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(priceType, "price type");
    DATA_CHECKER.checkSpreadData(valuationDate, marketTenors, marketSpreads);

    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[marketTenors.length];

    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m]);
    }

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, calibratedHazardRates, 0.0);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the CDS PV using the just calibrated hazard rate term structure
    final double presentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, cds, yieldCurve, calibratedHazardRateCurve, priceType);

    // Calculate the Loss Given Default (LGD) amount
    final double lossGivenDefault = cds.getNotional() * (1 - cds.getRecoveryRate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the VoD

    double valueOnDefault = 0.0;

    if (cds.getBuySellProtection() == BuySellProtection.BUY) {
      valueOnDefault = -Math.abs(presentValue) + lossGivenDefault;
    } else {
      valueOnDefault = -Math.abs(presentValue) - lossGivenDefault;
    }

    return valueOnDefault;

  }

}
