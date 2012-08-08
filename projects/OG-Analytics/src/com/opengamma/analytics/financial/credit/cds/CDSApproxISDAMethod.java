/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.CurrencyAmount;

/**
 * An approximation to the calculation method for the ISDA CDS model
 * 
 * @author Martin Traverse & Niels Stchedroff (Riskcare)
 */
public class CDSApproxISDAMethod implements PricingMethod {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CDSApproxISDAMethod.class);
  
  private static DayCount s_act365 = new ActualThreeSixtyFive();

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    CDSDerivative cds = (CDSDerivative) instrument;
    YieldAndDiscountCurve cdsCcyCurve = curves.getCurve(cds.getCdsCcyCurveName());
    YieldAndDiscountCurve spreadCurve = curves.getCurve(cds.getSpreadCurveName());
    
    throw new RuntimeException( "pv not implemented please test calculateUpfrontCharge" );

    // return CurrencyAmount.of(cds.getPremium().getCurrency(), calculateUpfrontCharge(cds, cdsCcyCurve, spreadCurve, ZonedDateTime.now()));
  }
  
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves, ZonedDateTime pricingDate) {
    CDSDerivative cds = (CDSDerivative) instrument;
    YieldAndDiscountCurve cdsCcyCurve = curves.getCurve(cds.getCdsCcyCurveName());
    YieldAndDiscountCurve spreadCurve = curves.getCurve(cds.getSpreadCurveName());

    return CurrencyAmount.of(cds.getPremium().getCurrency(), calculateUpfrontCharge(cds, cdsCcyCurve, spreadCurve, pricingDate));
  }

  /**
   * Do the calculation
   * 
   * @param cds
   * @param cdsCcyCurve
   * @param spreadCurve
   * @return
   */
  public double calculateUpfrontCharge(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve, ZonedDateTime pricingDate) {
    double result = 0.0;
    result += valuePremiumLeg(cds, cdsCcyCurve, spreadCurve, pricingDate) - valueDefaultLeg(cds, cdsCcyCurve, spreadCurve);
    return result;
  }

  /**
   * 
   * @param cds
   * @param discountCurve
   * @param spreadCurve
   * @param pricingDate
   * @return
   */
  private double valuePremiumLeg(CDSDerivative cds, YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, ZonedDateTime pricingDate) {
    
    // TODO: fix the step in date
    final ZonedDateTime stepinDate = cds.getPremium().getNthPayment(0).getAccrualStartDate().plusDays(1);
    
    // TODO: Offset uses observationStartOfDay, which is true precisely when protectStart is set on the CDS
    final CouponFixed[] premiumPayments = cds.getPremium().getPayments();
    final int offset = -1;
    
    // TODO: encode this in CDS class or check protectStart (which causes the extra day accrual on last period)
    double maturity = s_act365.getDayCountFraction(pricingDate, premiumPayments[premiumPayments.length - 1].getAccrualEndDate());
    
    NavigableSet<Double> timeline = buildTimeLine(discountCurve, spreadCurve, cds.getCDSStartTime(), maturity);
    
    CouponFixed payment;
    ZonedDateTime accrualPeriodEnd;
    
    double ammount, survival, discount;
    double result = 0.0;
    
    for (int i = 0; i < premiumPayments.length; i++) {
      
      payment = premiumPayments[i];
      accrualPeriodEnd = payment.getAccrualEndDate();

      // TODO: Spread and discount curve must be continuous/act365
      ammount = payment.getAmount();      
      survival = spreadCurve.getDiscountFactor(s_act365.getDayCountFraction(pricingDate, accrualPeriodEnd.plusDays(offset)));
      discount = discountCurve.getDiscountFactor(s_act365.getDayCountFraction(pricingDate, accrualPeriodEnd.plusDays(offset))); // TODO: Use pay date
      
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("i=" + i + ", accrualEnd=" + accrualPeriodEnd + ", accrualTime=" + payment.getPaymentYearFraction());
        s_logger.debug("ammount=" + ammount + ", survival=" + survival + ", discount=" + discount + ", pv=" + ammount * survival * discount);
      }
      
      result += ammount * survival * discount;
      
      if (cds.getAccrualOnDefault()) {
        result += accrualOnDefault(payment, pricingDate, stepinDate.plusDays(offset),
          payment.getAccrualStartDate().plusDays(offset), payment.getAccrualEndDate().plusDays(offset),
          discountCurve, spreadCurve, timeline);
      }
    }
    
    return result;
  }
  

  /**
   * 
   * @param payment
   * @param pricingDate
   * @param stepinDate
   * @param startDate
   * @param endDate
   * @param discountCurve
   * @param spreadCurve
   * @param fullTimeline
   * @return
   */
  private double accrualOnDefault(CouponFixed payment, ZonedDateTime pricingDate, ZonedDateTime stepinDate, ZonedDateTime startDate, ZonedDateTime endDate,
    YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, NavigableSet<Double> fullTimeline) {
    
    final double today = 0.0;
    final double startTime = s_act365.getDayCountFraction(pricingDate, startDate);
    final double endTime = s_act365.getDayCountFraction(pricingDate, endDate);
    final double stepinTime = s_act365.getDayCountFraction(pricingDate, stepinDate);
    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    
    // TODO: Handle startDate == endDate (divide by zero)
    final double accrualRate = payment.getAmount() / s_act365.getDayCountFraction(startDate, endDate);
    
    final Double[] timeline = truncateTimeLine(fullTimeline, startTime, endTime);
    
    System.out.println("pricingDate=" + pricingDate + ", startDate=" + startDate + ", endDate=" + endDate + ", stepinDate=" + stepinDate + ", accrualRate=" + accrualRate);

    double t0, t1, dt, spread0, spread1, discount0, discount1;
    double lambda, fwdRate, lambdaFwdRate, valueForTimeStep, value;
    
    t0 = subStartTime - startTime + (0.5 / 365.0);
    spread0 = spreadCurve.getDiscountFactor(subStartTime);
    discount0 = discountCurve.getDiscountFactor(Math.max(today, subStartTime));
    
    value = 0.0;

    for (int i = 1; i < timeline.length; ++i) {

      if (timeline[i] <= stepinTime) {
        continue;
      }

      t1 = timeline[i] - startTime + (0.5 / 365.0);
      dt = t1 - t0;
      
      spread1 = spreadCurve.getDiscountFactor(timeline[i]);
      discount1 = discountCurve.getDiscountFactor(timeline[i]);

      lambda = Math.log(spread0 / spread1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdaFwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accrualRate * spread0 * discount0
        * (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * spread1 / spread0 * discount1 / discount0);

      value += valueForTimeStep;
      
      if (s_logger.isDebugEnabled()) {
        s_logger.debug(
          "accRate=" + accrualRate + ", t0=" + t0 + ", t1=" + t1 + ", dt=" + dt +
          ", spread0=" + spread0 + ", spread1=" + spread1 + ", discount0=" + discount0 + ", discount1=" + discount1 +
          ", lambda=" + lambda + ", fwdRate=" + fwdRate + ", lambdaFwdRate=" + lambdaFwdRate + ", valueForTimeStep=" + valueForTimeStep
        );
      }

      t0 = t1;
      spread0 = spread1;
      discount0 = discount1;
    }

    return value;
  }

  private double valueDefaultLeg(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve) {

    double myPv = 0.0;
    double valueDateDiscountFactor;
    double pv = 0.0;

    //ZonedDateTime startDate;
    //int offset;
    //offset = (cl.protectStart ? 1 : 0);
    //startDate = (cl.startDate.isAfter(stepinDate.minusDays(offset))) ? cl.startDate : stepinDate.minusDays(offset);
    //startDate = (startDate.isAfter(today.minusDays(offset))) ? startDate : today.minusDays(offset);

    if (true /*cds.payAtDefault() */) {
      myPv = valueDefaultLegPayOnDefault(cds, cdsCcyCurve, spreadCurve) * cds.getNotional();
    }
    //else {
    //  myPv = valueDefaultLegPayOnMaturity(cds, cdsCcyCurve, spreadCurve) * cds.getNotional();
    //}

    /** myPv has been calculated as at today - need it at valueDate */
    valueDateDiscountFactor = 1.0; // TODO: verify assumptions about pricing date  -- cdsCcyCurve.getDiscountFactor(valueDate);
    pv = myPv / valueDateDiscountFactor;
    return pv;
  }

  private static double valueDefaultLegPayOnDefault(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve) {
//
//    // CDS has already matured
//    if (cds.getMaturity() < 0.0) {
//      return 0.0;
//    }
//
//    final double loss = 1.0 - cds.getRecoveryRate();
//    final Double[] timePoints = realTimePointsForCurves(cds, cdsCcyCurve, spreadCurve);
//
//    /** the integration - we can assume flat forwards between points on
//       the timeline - this is true for both curves 
//
//       we are integrating -Z dS/dt where Z is the discount factor and
//       S is the survival probability
//
//       assuming flat forwards on each part of the integration, this is an
//       exact integral
//    */
//
//    // TODO: consider what is meant by "today"
//    double today = 0.0;
//
//    double dt;
//    double spread0;
//    double spread1 = spreadCurve.getDiscountFactor(cds.getProtectionStartTime() > today ? cds.getProtectionStartTime() : today);
//    double discount0;
//    double discount1 = cdsCcyCurve.getDiscountFactor(cds.getProtectionStartTime() > today ? cds.getProtectionStartTime() : today);
//
//    double lambda;
//    double fwdRate;
//    double valueForTimeStep;
//
//    double value = 0.0;
//
//    for (int i = 1; i < timePoints.length; ++i) {
//
//      dt = timePoints[i] - timePoints[i - 1];
//
//      spread0 = spread1;
//      spread1 = spreadCurve.getDiscountFactor(timePoints[i]);
//
//      discount0 = discount1;
//      discount1 = cdsCcyCurve.getDiscountFactor(timePoints[i]);
//
//      lambda = Math.log(spread0 / spread1) / dt;
//      fwdRate = Math.log(discount0 / discount1) / dt;
//      valueForTimeStep = ((loss * lambda) / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * dt)) * spread0 * discount0;
//
//      value += valueForTimeStep;
//    }
//
//    return value;
    
    return 0.0;
  }

  public static NavigableSet<Double> buildTimeLine(YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve, double startTime, double endTime) {

    final Double[] cdsCcyCurveTimes = ((YieldCurve) cdsCcyCurve).getCurve().getXData();
    final Double[] spreadCurveTimes = ((YieldCurve) spreadCurve).getCurve().getXData();

    NavigableSet<Double> timePoints = new TreeSet<Double>();
    
    for (int i = 0; i < cdsCcyCurveTimes.length; i++) {
      timePoints.add(cdsCcyCurveTimes[i]);
    }

    for (int i = 0; i < spreadCurveTimes.length; i++) {
      timePoints.add(spreadCurveTimes[i]);
    }

    timePoints.add(startTime);
    timePoints.add(endTime);      
    
    return timePoints;
  }
  
  public static Double[] truncateTimeLine(final NavigableSet<Double> timeLine, double startTime, double endTime) {
    
    final Set<Double> timePointsInRange = timeLine.subSet(startTime, true, endTime, true);
    return timePointsInRange.toArray(new Double[timePointsInRange.size()]);
  }
}
