/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.analytics.financial.credit.schedulegeneration.isda.ISDAAccruedLegIntegrationScheduleGenerator;
import com.opengamma.analytics.financial.credit.schedulegeneration.isda.ISDAPremiumLegScheduleGenerator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantPremiumLegCalculator extends ISDACompliantLegCalculator {
  private static final int SPOT_DAYS = 3;
  private static final boolean ADJUST_CASH_SETTLEMENT_DATE = true;
  private static final BusinessDayConvention BDA = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ISDAPremiumLegScheduleGenerator PREMIUM_LEG_SCHEDULE = new ISDAPremiumLegScheduleGenerator();
  private static final ISDAAccruedLegIntegrationScheduleGenerator ACRRUED_INTEGRATION_SCHEDULE = new ISDAAccruedLegIntegrationScheduleGenerator();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)

  // The code for the accrued calc has just been lifted from RiskCare's implementation for now because it exactly reproduces
  // the ISDA model - will replace with a better model in due course

  @Override
  public double calculateLeg(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider curves,
      final PriceType priceType) {
    double presentValuePremiumLeg = 0.0;
    final ZonedDateTime[] premiumLegSchedule = PREMIUM_LEG_SCHEDULE.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime[] accruedLegIntegrationSchedule = ACRRUED_INTEGRATION_SCHEDULE.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, curves);
    final ZonedDateTime adjustedMaturityDate = PREMIUM_LEG_SCHEDULE.getAdjustedMaturityDate(cds);
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);
    final ISDADateCurve yieldCurve = curves.getYieldCurve();
    final HazardRateCurve hazardRateCurve = curves.getHazardRateCurve();
    // TODO : Check the effective date calc here
    // If the valuation date is exactly the adjusted maturity date then simply return zero
    /*
        if (valuationDate.equals(adjustedMaturityDate) || cds.getEffectiveDate().equals(adjustedMaturityDate)) {
          return 0.0;
        }
     */
    final ZonedDateTime today = valuationDate;
    final ZonedDateTime stepinDate = cds.getEffectiveDate(); //TODO this relies on the person that's set up the CDS to know that effective date = 1 day after valuation date by convention
    // The value date is when cash settlement is made
    // TODO : Add the extra logic for this calculation
    final ZonedDateTime matDate = cds.getMaturityDate();
    // TODO : Check valueDate >= today and stepinDate >= today
    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {
      return 0;
    }
    double thisPV = 0.0;
    for (int i = 1; i < premiumLegSchedule.length; i++) {
      final int obsOffset = cds.getProtectionStart() ? -1 : 0;
      final ZonedDateTime accrualStartDate = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEndDate = premiumLegSchedule[i];
      final ZonedDateTime discountDate = accrualEndDate;
      // The last coupon date has an extra day of accrued
      if (i == premiumLegSchedule.length - 1) {
        accrualEndDate = accrualEndDate.plusDays(1);
      }
      final double delta = accrualEndDate.isAfter(stepinDate) ? 1 : 0;
      final double accTime = ACT_360.getDayCountFraction(accrualStartDate, accrualEndDate);
      final ZonedDateTime offsetAccrual = accrualEndDate.plusDays(obsOffset);
      double tObsOffset = today.isAfter(offsetAccrual) ? -ACT_365.getDayCountFraction(offsetAccrual, today) : ACT_365.getDayCountFraction(today, offsetAccrual);
      if (Double.compare(tObsOffset, -0.0) == 0) {
        tObsOffset = 0;
      }
      double t = today.isAfter(discountDate) ? -ACT_365.getDayCountFraction(discountDate, today) : ACT_365.getDayCountFraction(today, discountDate);
      final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);
      final double discount = yieldCurve.getDiscountFactor(t);
      thisPV += delta * accTime * discount * survival;
      double myPV = 0.0;
      if (cds.getIncludeAccruedPremium()) {
        final ZonedDateTime offsetStepinDate = stepinDate.plusDays(obsOffset);            // stepinDate
        final ZonedDateTime offsetAccStartDate = accrualStartDate.plusDays(obsOffset);    // startDate
        final ZonedDateTime offsetAccEndDate = accrualEndDate.plusDays(obsOffset);        // endDate
        // TODO : Check endDate > startDate
        final ZonedDateTime[] truncatedDateList = ScheduleUtils.getTruncatedTimeLine(accruedLegIntegrationSchedule, offsetAccStartDate, offsetAccEndDate, true);
        ZonedDateTime subStartDate = offsetStepinDate.isAfter(offsetAccStartDate) ? offsetStepinDate : offsetAccStartDate;
        final double tAcc = ACT_365.getDayCountFraction(offsetAccStartDate, offsetAccEndDate);
        final double accRate = accTime / tAcc;
        if (today.equals(subStartDate)) {
          t = 0;
        } else {
          t = today.isBefore(subStartDate) ? ACT_365.getDayCountFraction(today, subStartDate) : -ACT_365.getDayCountFraction(subStartDate, today);
        }
        if (Double.compare(t, -0.0) == 0) {
          t = 0;
        }
        double s0 = hazardRateCurve.getSurvivalProbability(t);
        double df0 = yieldCurve.getDiscountFactor(t);
        for (int j = 1; j < truncatedDateList.length; ++j) {
          double thisAccPV = 0.0;
          final ZonedDateTime date = truncatedDateList[j];
          if (date.isAfter(offsetStepinDate)) {
            t = today.isBefore(date) ? ACT_365.getDayCountFraction(today, date) : -ACT_365.getDayCountFraction(date, today); //TimeCalculator.getTimeBetween(today, truncatedDateList[j], ACT_365);
            if (Double.compare(t, -0.0) == 0) {
              t = 0;
            }
            final double s1 = hazardRateCurve.getSurvivalProbability(t);
            final double df1 = yieldCurve.getDiscountFactor(t);
            final double t0 = (offsetAccStartDate.isBefore(subStartDate) ? ACT_365.getDayCountFraction(offsetAccStartDate, subStartDate)
                : ACT_365.getDayCountFraction(subStartDate, offsetAccStartDate)) + 0.5 / 365.0;
            final double t1 = (offsetAccStartDate.isBefore(date) ? ACT_365.getDayCountFraction(offsetAccStartDate, date)
                : ACT_365.getDayCountFraction(date, offsetAccStartDate)) + 0.5 / 365.0;
            t = t1 - t0;
            final double lambda = Math.log(s0 / s1) / t;
            final double fwdRate = Math.log(df0 / df1) / t;
            final double lambdafwdRate = lambda + fwdRate + 1.0e-50;
            thisAccPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
            myPV += thisAccPV;
            s0 = s1;
            df0 = df1;
            subStartDate = date;
          }
        }
      }
      thisPV += myPV;
    }
    presentValuePremiumLeg = thisPV;
    // TODO : Check this calculation - maybe move it out of this routine and into the PV calculation routine?
    // TODO : Note the cash settlement date is hardcoded at 3 days
    ZonedDateTime bdaCashSettlementDate = valuationDate.plusDays(SPOT_DAYS);
    if (ADJUST_CASH_SETTLEMENT_DATE) {
      bdaCashSettlementDate = BDA.adjustDate(cds.getCalendar(), bdaCashSettlementDate);
    }
    final double tSett = ACT_ACT.getDayCountFraction(valuationDate, bdaCashSettlementDate);
    final double valueDatePV = yieldCurve.getDiscountFactor(tSett);
    presentValuePremiumLeg /= valueDatePV;
    if (priceType == PriceType.CLEAN) {
      //TODO not looked at this yet
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
