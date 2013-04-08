/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.isda.GenerateCreditDefaultSwapContingentLegIntegrationScheduleNew;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * 
 */
public class ISDACompliantContingentLegCalculator {
  private static final GenerateCreditDefaultSwapContingentLegIntegrationScheduleNew SCHEDULE_CALCULATOR = new GenerateCreditDefaultSwapContingentLegIntegrationScheduleNew();
  private static final int spotDays = 3;
  private static final boolean businessDayAdjustCashSettlementDate = true;
  private static final BusinessDayConvention cashSettlementDateBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  public double calculateContingentLeg(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {
    double presentValueContingentLeg = 0.0;
    final int offset = cds.getProtectionStart() ? 1 : 0;
    ZonedDateTime startDate;
    final ZonedDateTime clStartDate = cds.getProtectionStart() ? valuationDate.minusDays(1) : valuationDate;
    final ZonedDateTime clEndDate = cds.getMaturityDate();
    if (valuationDate.isAfter(clEndDate)) {
      presentValueContingentLeg = 0.0;
      return presentValueContingentLeg;
    }
    final ZonedDateTime stepinDate = cds.getEffectiveDate();
    if (clStartDate.isAfter(stepinDate.minusDays(offset))) {
      startDate = clStartDate;
    } else {
      startDate = stepinDate.minusDays(offset);
    }
    if (!startDate.isAfter(valuationDate.minusDays(1))) {
      startDate = valuationDate.minusDays(1);
    }
    final Double[] contingentLegIntegrationSchedule = SCHEDULE_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(valuationDate, startDate, clEndDate, cds, yieldCurve,
        hazardRateCurve);
    // Get the survival probability at the first point in the integration schedule
    double survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[0]);
    // Get the discount factor at the first point in the integration schedule
    double discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[0]);
    final double loss = (1 - cds.getRecoveryRate());
    for (int i = 1; i < contingentLegIntegrationSchedule.length; ++i) {
      // Calculate the time between adjacent points in the integration schedule
      final double deltat = contingentLegIntegrationSchedule[i] - contingentLegIntegrationSchedule[i - 1];
      // Set the probability of survival up to the previous point in the integration schedule
      final double survivalProbabilityPrevious = survivalProbability;
      // Set the discount factor up to the previous point in the integration schedule
      final double discountFactorPrevious = discountFactor;
      // Get the survival probability at this point in the integration schedule
      survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[i]);
      // Get the discount factor at this point in the integration schedule
      discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[i]);
      // Calculate the forward hazard rate over the interval deltat (assumes the hazard rate is constant over this period)
      final double hazardRate = Math.log(survivalProbabilityPrevious / survivalProbability) / deltat;
      // Calculate the forward interest rate over the interval deltat (assumes the interest rate is constant over this period)
      final double interestRate = Math.log(discountFactorPrevious / discountFactor) / deltat;
      // Calculate the contribution of the interval deltat to the overall contingent leg integral
      presentValueContingentLeg += loss * (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * deltat)) * survivalProbabilityPrevious * discountFactorPrevious;
    }
    // TODO : Check this calculation - maybe move it out of this routine and into the PV calculation routine?
    // TODO : Note the cash settlement date is hardcoded at 3 days
    //final int spotDays = 5;
    //final ZonedDateTime cashSettleDate = valuationDate.plusDays(spotDays);
    //final double t = TimeCalculator.getTimeBetween(valuationDate, cashSettleDate, ACT_365);
    ZonedDateTime bdaCashSettlementDate = valuationDate.plusDays(spotDays);
    if (businessDayAdjustCashSettlementDate) {
      bdaCashSettlementDate = cashSettlementDateBusinessDayConvention.adjustDate(cds.getCalendar(), valuationDate.plusDays(spotDays));
    }
    final double t = TimeCalculator.getTimeBetween(valuationDate, bdaCashSettlementDate, ACT_365);
    final double valueDatePV = yieldCurve.getDiscountFactor(t);
    return cds.getNotional() * presentValueContingentLeg / valueDatePV;
  }
}
