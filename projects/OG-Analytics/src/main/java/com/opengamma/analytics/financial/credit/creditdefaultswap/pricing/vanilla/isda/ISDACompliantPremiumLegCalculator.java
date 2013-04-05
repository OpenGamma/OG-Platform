/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.isda.GenerateCreditDefaultSwapAccruedLegIntegrationScheduleNew;
import com.opengamma.analytics.financial.credit.schedulegeneration.isda.GenerateCreditDefaultSwapPremiumLegScheduleNew;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantPremiumLegCalculator {
  private static final int spotDays = 3;
  private static final boolean businessDayAdjustCashSettlementDate = true;
  private static final BusinessDayConvention cashSettlementDateBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)

  // The code for the accrued calc has just been lifted from RiskCare's implementation for now because it exactly reproduces
  // the ISDA model - will replace with a better model in due course

  public double calculatePremiumLeg(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {
    double presentValuePremiumLeg = 0.0;
    // Construct a cashflow schedule object for the premium leg
    final GenerateCreditDefaultSwapPremiumLegScheduleNew cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegScheduleNew();
    // Build the premium leg cashflow schedule from the contract specification
    //final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    // Construct a schedule object for the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    final GenerateCreditDefaultSwapAccruedLegIntegrationScheduleNew accruedSchedule = new GenerateCreditDefaultSwapAccruedLegIntegrationScheduleNew();
    // Build the integration schedule for the calculation of the accrued leg
    final ZonedDateTime[] accruedLegIntegrationSchedule = accruedSchedule.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, false);
    // Get the (adjusted) maturity date of the trade
    final ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);
    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);
    // TODO : Check the effective date calc here
    // If the valuation date is exactly the adjusted maturity date then simply return zero
    /*
    if (valuationDate.equals(adjustedMaturityDate) || cds.getEffectiveDate().equals(adjustedMaturityDate)) {
      return 0.0;
    }
     */
    // Determine where in the cashflow schedule the valuationDate is
    //final int startCashflowIndex = getCashflowIndex(valuationDate, premiumLegSchedule, 1, 1);
    final ZonedDateTime today = valuationDate;
    final ZonedDateTime stepinDate = cds.getEffectiveDate(); //TODO this relies on the person that's set up the CDS to know that effective date = 1 day after valuation date by convention
    // The value date is when cash settlement is made
    // TODO : Add the extra logic for this calculation
    final ZonedDateTime matDate = cds.getMaturityDate();
    // TODO : Check valueDate >= today and stepinDate >= today
    // TODO : Check when today > matDate || stepinDate > matDate
    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {
      presentValuePremiumLeg = 0.0;
      return presentValuePremiumLeg;
    }
    double thisPV = 0.0;
    for (int i = 1; i < premiumLegSchedule.length; i++) {
      int obsOffset = 0;
      if (cds.getProtectionStart()) {
        obsOffset = -1;
      }
      final ZonedDateTime accrualStartDate = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEndDate = premiumLegSchedule[i];
      // The last coupon date has an extra day of accrued
      if (i == premiumLegSchedule.length - 1) {
        accrualEndDate = accrualEndDate.plusDays(1);
      }
      double delta = 1.0;
      final boolean temp = accrualEndDate.isAfter(stepinDate);
      // TODO : Check accEndDate <= stepinDate
      if (temp == false) {
        delta = 0.0;
      }
      final double accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate, ACT_360);
      ZonedDateTime discountDate = accrualEndDate;
      if (i == premiumLegSchedule.length - 1) {
        //obsOffset = 0;
        discountDate = accrualEndDate.minusDays(1);
        //accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate.plusDays(1), ACT_360);
      }
      double tObsOffset = TimeCalculator.getTimeBetween(today, accrualEndDate.plusDays(obsOffset), ACT_365);
      if (Double.compare(tObsOffset, -0.0) == 0) {
        tObsOffset = 0;
      }
      //double t = TimeCalculator.getTimeBetween(today, accrualEndDate, ACT_365);
      double t = TimeCalculator.getTimeBetween(today, discountDate, ACT_365);
      final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);
      final double discount = yieldCurve.getDiscountFactor(t);
      //final double discount = yieldCurve.getDiscountFactor(today, accrualEndDate);
      thisPV += delta * accTime * discount * survival;
      double myPV = 0.0;
      if (cds.getIncludeAccruedPremium()) {
        final double accrual = 0.0;
        final ZonedDateTime offsetStepinDate = stepinDate.plusDays(obsOffset);            // stepinDate
        final ZonedDateTime offsetAccStartDate = accrualStartDate.plusDays(obsOffset);    // startDate
        final ZonedDateTime offsetAccEndDate = accrualEndDate.plusDays(obsOffset);        // endDate
        // TODO : Check endDate > startDate
        final ZonedDateTime[] truncatedDateList = accruedSchedule.getTruncatedTimeLine(accruedLegIntegrationSchedule, offsetAccStartDate, offsetAccEndDate, true);
        ZonedDateTime subStartDate;
        if (offsetStepinDate.isAfter(offsetAccStartDate)) {
          subStartDate = offsetStepinDate;
        } else {
          subStartDate = offsetAccStartDate;
        }
        final double tAcc = TimeCalculator.getTimeBetween(offsetAccStartDate, offsetAccEndDate, ACT_365);
        final double accRate = accTime / tAcc;
        t = TimeCalculator.getTimeBetween(today, subStartDate, ACT_365);
        if (Double.compare(t, -0.0) == 0) {
          t = 0;
        }
        double s0 = hazardRateCurve.getSurvivalProbability(t);
        double df0 = yieldCurve.getDiscountFactor(t);
        //double df0 = yieldCurve.getDiscountFactor(today, subStartDate);
        for (int j = 1; j < truncatedDateList.length; ++j) {
          double thisAccPV = 0.0;
          if (!truncatedDateList[j].isAfter(offsetStepinDate)) {
          } else {
            t = TimeCalculator.getTimeBetween(today, truncatedDateList[j], ACT_365);
            final double s1 = hazardRateCurve.getSurvivalProbability(t);
            final double df1 = yieldCurve.getDiscountFactor(t);
            //double df1 = yieldCurve.getDiscountFactor(today, truncatedDateList[j]);
            final double t0 = TimeCalculator.getTimeBetween(offsetAccStartDate, subStartDate, ACT_365) + 0.5 / 365.0;
            final double t1 = TimeCalculator.getTimeBetween(offsetAccStartDate, truncatedDateList[j], ACT_365) + 0.5 / 365.0;
            t = t1 - t0;
            final double lambda = Math.log(s0 / s1) / t;
            final double fwdRate = Math.log(df0 / df1) / t;
            final double lambdafwdRate = lambda + fwdRate + 1.0e-50;
            thisAccPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
            myPV += thisAccPV;
            s0 = s1;
            df0 = df1;
            subStartDate = truncatedDateList[j];
          }
        }
      }
      thisPV += myPV;
    }
    presentValuePremiumLeg = thisPV;
    // TODO : Check this calculation - maybe move it out of this routine and into the PV calculation routine?
    // TODO : Note the cash settlement date is hardcoded at 3 days
    ZonedDateTime bdaCashSettlementDate = valuationDate.plusDays(spotDays);
    if (businessDayAdjustCashSettlementDate) {
      bdaCashSettlementDate = cashSettlementDateBusinessDayConvention.adjustDate(cds.getCalendar(), valuationDate.plusDays(spotDays));
    }
    final double tSett = TimeCalculator.getTimeBetween(valuationDate, bdaCashSettlementDate);
    final double valueDatePV = yieldCurve.getDiscountFactor(tSett);
    presentValuePremiumLeg /= valueDatePV;
    if (priceType == PriceType.CLEAN) {
      // pass in stepinDate as 'today' 31/1/2013
      double ai = 0.0;
      // TODO : Maybe check if stepinDate is in range [startDate, maturityDate] - probably not necessary
      //final int startCashflowIndex = getCashflowIndex(stepinDate, premiumLegSchedule, 0, 1);
      int startCashflowIndex = 0;
      ZonedDateTime rollingDate = premiumLegSchedule[0].minusDays(1);
      while (rollingDate.isBefore(premiumLegSchedule[startCashflowIndex])) {
        startCashflowIndex++;
        rollingDate = premiumLegSchedule[startCashflowIndex];
      }
      // Get the date of the last coupon before the current valuation date
      final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1];
      // Compute the amount of time between previousPeriod and stepinDate
      final double dcf = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);
      // Calculate the accrued interest gained in this period of time
      ai = dcf;
      presentValuePremiumLeg -= ai;
    }
    return cds.getNotional() * presentValuePremiumLeg;
  }
}
