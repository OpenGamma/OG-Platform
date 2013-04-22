/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.util.CreditMarketDataUtils;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of Value-on-Default (VoD or jump-to-default risk) for a vanilla Legacy CDS
 */
public class VoDCreditDefaultSwap {

  private static final DayCount ACT_365 = new ActualThreeSixtyFive();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments
  // TODO : Need to consider more sophisticated sensitivity calculations e.g. algorithmic differentiation

  // NOTE : The calibration is done with PriceType = CLEAN and the valuation is done with whatever the priceType input is

  // NOTE : The SNCDS VoD is calculated as follows.
  // NOTE : In the event of a default the CDS contract extinguishes with an effective value of 0. The change in MtM on default is
  // NOTE : therefore 0 - MtM (just prior to default). This is the same for both buy an sell protection positions (since the MtM
  // NOTE : can be either positive or negative). In addition to this amount a buy protection position will receive LGD from the 
  // NOTE : payoff of the contingent leg. A sell protection position will make a payment of LGD.

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the VoD

  public double getValueOnDefaultCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final PriceType priceType) {

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(priceType, "price type");

    CreditMarketDataUtils.checkSpreadData(valuationDate, marketTenors, marketSpreads);

    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[marketTenors.length];

    times[0] = 0.0;
    for (int m = 1; m < marketTenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m]);
    }

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, PriceType.CLEAN);

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
      valueOnDefault = -presentValue + lossGivenDefault;
    } else {
      valueOnDefault = -presentValue - lossGivenDefault;
    }

    return valueOnDefault;

  }
}
