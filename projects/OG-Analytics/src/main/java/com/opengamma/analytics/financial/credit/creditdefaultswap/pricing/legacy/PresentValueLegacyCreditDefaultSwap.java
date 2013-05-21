/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods for the valuation of a vanilla Legacy CDS (this valuation methodology is common to most legacy CDS's)
 */
public class PresentValueLegacyCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : 

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Create a PV calculator for a CDS object
  private static final PresentValueCreditDefaultSwap presentValueCreditDefaultSwap = new PresentValueCreditDefaultSwap();

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a hazard rate curve calibrated to market observed data)

  public double getPresentValueLegacyCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    final double presentValuePremiumLeg = presentValueCreditDefaultSwap.calculatePremiumLeg(valuationDate, cds, yieldCurve, hazardRateCurve, priceType);

    // Calculate the value of the contingent leg
    // TODO : Remember this check in the ISDA code 'if (MAX(stepinDate, startDate) <= endDate)' 
    final double presentValueContingentLeg = presentValueCreditDefaultSwap.calculateContingentLeg(valuationDate, cds, yieldCurve, hazardRateCurve);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -(cds.getParSpread() / 10000.0) * presentValuePremiumLeg + presentValueContingentLeg;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method to calculate the par spread of a CDS at contract inception (with a hazard rate curve calibrated to market observed data)

  public double getParSpreadLegacyCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double parSpread = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg
    final double presentValuePremiumLeg = presentValueCreditDefaultSwap.calculatePremiumLeg(valuationDate, cds, yieldCurve, hazardRateCurve, priceType);

    // Calculate the value of the contingent leg
    final double presentValueContingentLeg = presentValueCreditDefaultSwap.calculateContingentLeg(valuationDate, cds, yieldCurve, hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the par spread (NOTE : Returned value is in bps)
    if (Double.doubleToLongBits(presentValuePremiumLeg) == 0.0) {
      throw new IllegalStateException("Warning : The premium leg has a PV of zero - par spread cannot be computed");
    } else {
      parSpread = 10000.0 * presentValueContingentLeg / presentValuePremiumLeg;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return parSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
