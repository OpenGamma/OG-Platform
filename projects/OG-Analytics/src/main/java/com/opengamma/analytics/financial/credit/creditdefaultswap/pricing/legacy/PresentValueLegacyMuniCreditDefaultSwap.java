/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the valuation of a Legacy Muni CDS
 */
public class PresentValueLegacyMuniCreditDefaultSwap {

  //----------------------------------------------------------------------------------------------------------------------------------------

  private static final PresentValueCreditDefaultSwap presentValueCreditDefaultSwap = new PresentValueCreditDefaultSwap();

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a hazard rate curve calibrated to market observed data)

  public double getPresentValueLegacyMuniCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyMuniCreditDefaultSwapDefinition muniCDS,
      final ISDACurve yieldCurve,
      final HazardRateCurve muniHazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(muniCDS, "LegacyMuniCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(muniHazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    final double presentValuePremiumLeg = presentValueCreditDefaultSwap.calculatePremiumLeg(valuationDate, muniCDS, yieldCurve, muniHazardRateCurve);

    // Calculate the value of the contingent leg
    final double presentValueContingentLeg = presentValueCreditDefaultSwap.calculateContingentLeg(valuationDate, muniCDS, yieldCurve, muniHazardRateCurve);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -(muniCDS.getParSpread() / 10000.0) * presentValuePremiumLeg + presentValueContingentLeg;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // If we require the clean price, then calculate the accrued interest and add this to the PV
    if (priceType == PriceType.CLEAN) {
      presentValue += (muniCDS.getParSpread() / 10000.0) * presentValueCreditDefaultSwap.calculateAccruedInterest(valuationDate, muniCDS);
    }

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (muniCDS.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method to calculate the par spread of a CDS at contract inception (with a hazard rate curve calibrated to market observed data)

  public double getParSpreadLegacyMuniCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyMuniCreditDefaultSwapDefinition muniCDS,
      final ISDACurve yieldCurve,
      final HazardRateCurve muniHazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(muniCDS, "LegacyMuniCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(muniHazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double parSpread = 0.0;
    double accruedInterest = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Check if the valuationDate equals the adjusted effective date (have to do this after the schedule is constructed)
    ArgumentChecker.isTrue(valuationDate.equals(cashflowSchedule.getAdjustedEffectiveDate(muniCDS)), "Valuation Date should equal the adjusted effective date when computing par spreads");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg
    final double presentValuePremiumLeg = 100; //presentValueCreditDefaultSwap.calculatePremiumLeg(valuationDate, muniCDS, yieldCurve, muniHazardRateCurve);

    // Calculate the value of the contingent leg
    final double presentValueContingentLeg = 100; //presentValueCreditDefaultSwap.calculateContingentLeg(valuationDate, muniCDS, yieldCurve, muniHazardRateCurve);

    // If we require the clean price, then calculate the accrued interest and add this to the PV of the premium leg
    if (priceType == PriceType.CLEAN) {
      accruedInterest = presentValueCreditDefaultSwap.calculateAccruedInterest(valuationDate, muniCDS);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the par spread (NOTE : Returned value is in bps)
    if (Double.doubleToLongBits(presentValuePremiumLeg) == 0.0) {
      throw new IllegalStateException("Warning : The premium leg has a PV of zero - par spread cannot be computed");
    } else {
      parSpread = 10000.0 * presentValueContingentLeg / (presentValuePremiumLeg + accruedInterest);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return parSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
