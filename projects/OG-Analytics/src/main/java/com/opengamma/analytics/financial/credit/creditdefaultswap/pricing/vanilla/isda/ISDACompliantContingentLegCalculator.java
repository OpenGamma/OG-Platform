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
import com.opengamma.analytics.financial.credit.schedulegeneration.isda.ISDAContingentLegIntegrationScheduleGenerator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantContingentLegCalculator extends ISDACompliantLegCalculator {
  private static final ISDAContingentLegIntegrationScheduleGenerator SCHEDULE_CALCULATOR = new ISDAContingentLegIntegrationScheduleGenerator();
  private static final int SPOT_DAYS = 3;
  private static final boolean ADJUST_CASH_SETTLEMENT_DATE = true;
  private static final BusinessDayConvention BDA = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  @Override
  public double calculateLeg(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider curves,
      final PriceType priceType) {
    ArgumentChecker.notNull(valuationDate, "valuation date");
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(priceType, "price type");
    double presentValueContingentLeg = 0.0;
    final int offset = cds.getProtectionStart() ? 1 : 0;
    ZonedDateTime startDate;
    final ZonedDateTime valuationDateM1 = valuationDate.minusDays(1);
    final ZonedDateTime clStartDate = cds.getProtectionStart() ? valuationDateM1 : valuationDate;
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
    if (!startDate.isAfter(valuationDateM1)) {
      startDate = valuationDateM1;
    }
    final double[] contingentLegIntegrationSchedule = SCHEDULE_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(valuationDate, startDate, clEndDate, cds, curves);
    // Get the survival probability at the first point in the integration schedule
    final HazardRateCurve hazardRateCurve = curves.getHazardRateCurve();
    double survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[0]);
    // Get the discount factor at the first point in the integration schedule
    final ISDADateCurve yieldCurve = curves.getYieldCurve();
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
    ZonedDateTime bdaCashSettlementDate = valuationDate.plusDays(SPOT_DAYS);
    if (ADJUST_CASH_SETTLEMENT_DATE) {
      bdaCashSettlementDate = BDA.adjustDate(cds.getCalendar(), valuationDate.plusDays(SPOT_DAYS));
    }
    final double t = TimeCalculator.getTimeBetween(valuationDate, bdaCashSettlementDate, ACT_365);
    final double valueDatePV = yieldCurve.getDiscountFactor(t);
    return cds.getNotional() * presentValueContingentLeg / valueDatePV;
  }

}
