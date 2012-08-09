/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class CDSApproxISDAMethod implements PricingMethod {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CDSApproxISDAMethod.class);
  
  private static DayCount s_act365 = new ActualThreeSixtyFive();

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    throw new RuntimeException("please pass in the pricing date");
  }
  
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves, ZonedDateTime pricingDate) {
    CDSDerivative cds = (CDSDerivative) instrument;
    YieldAndDiscountCurve discountCurve = curves.getCurve(cds.getDiscountCurveName());
    YieldAndDiscountCurve spreadCurve = curves.getCurve(cds.getSpreadCurveName());

    return CurrencyAmount.of(cds.getPremium().getCurrency(), calculateUpfrontCharge(cds, discountCurve, spreadCurve, pricingDate));
  }

  /**
   * 
   * 
   * @param cds
   * @param discountCurve
   * @param spreadCurve
   * @param pricingDate
   * @return
   */
  public double calculateUpfrontCharge(CDSDerivative cds, YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, ZonedDateTime pricingDate) {
    
    // TODO: fix the step in date
    ZonedDateTime stepinDate = cds.getPremium().getNthPayment(0).getAccrualStartDate().plusDays(1);
    
    if (stepinDate.isBefore(pricingDate))
      stepinDate = pricingDate.plusDays(1);
    
    final double defaultLeg = valueDefaultLeg(cds, stepinDate, discountCurve, spreadCurve, pricingDate);
    final double premiumLeg = valuePremiumLeg(cds, stepinDate, discountCurve, spreadCurve, pricingDate);
    
    System.out.println("Default=" + defaultLeg + ", premium=" + premiumLeg);
    
    return (defaultLeg - premiumLeg) * cds.getNotional();
  }

  /**
   * 
   * 
   * @param cds
   * @param discountCurve
   * @param spreadCurve
   * @param pricingDate
   * @return
   */
  private double valuePremiumLeg(CDSDerivative cds, ZonedDateTime stepinDate, YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, ZonedDateTime pricingDate) {
    
    final CouponFixed[] premiumPayments = cds.getPremium().getPayments();
    final int offset = cds.isProtectStart() ? -1 : 0;
    
    // TODO: encode this in CDS class or check protectStart (which causes the extra day accrual on last period)
    double maturity = s_act365.getDayCountFraction(pricingDate, premiumPayments[premiumPayments.length - 1].getAccrualEndDate());
    
    System.out.println( maturity );
    
    NavigableSet<Double> timeline = buildTimeLine(discountCurve, spreadCurve, cds.getStartTime(), maturity);
    
    CouponFixed payment;
    ZonedDateTime accrualPeriodEnd;
    
    double ammount, survival, discount;
    double result = 0.0;
    
    for (int i = 0; i < premiumPayments.length; i++) {
      
      payment = premiumPayments[i];
      accrualPeriodEnd = payment.getAccrualEndDate();

      // TODO: Spread and discount curve must be continuous/act365
      ammount = payment.getFixedRate() * payment.getPaymentYearFraction(); // unit notional
      survival = spreadCurve.getDiscountFactor(s_act365.getDayCountFraction(pricingDate, accrualPeriodEnd.plusDays(offset)));
      discount = discountCurve.getDiscountFactor(s_act365.getDayCountFraction(pricingDate, accrualPeriodEnd.plusDays(offset))); // TODO: Use pay date
      
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("i=" + i + ", accrualEnd=" + accrualPeriodEnd + ", accrualTime=" + payment.getPaymentYearFraction());
        s_logger.debug("ammount=" + ammount + ", survival=" + survival + ", discount=" + discount + ", pv=" + ammount * survival * discount);
      }
      
      result += ammount * survival * discount;
      
      if (cds.isAccrualOnDefault()) {
        result += valuePremiumAccrualOnDefault(ammount, pricingDate, stepinDate.plusDays(offset),
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
  private double valuePremiumAccrualOnDefault(final double ammount, ZonedDateTime pricingDate, ZonedDateTime stepinDate, ZonedDateTime startDate, ZonedDateTime endDate,
    YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, NavigableSet<Double> fullTimeline) {
    
    final double today = 0.0;
    final double startTime = startDate.isAfter(pricingDate) ? s_act365.getDayCountFraction(pricingDate, startDate) : -s_act365.getDayCountFraction(startDate, pricingDate);
    final double endTime = s_act365.getDayCountFraction(pricingDate, endDate);
    final double stepinTime = s_act365.getDayCountFraction(pricingDate, stepinDate);
    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    
    // TODO: Handle startDate == endDate (divide by zero)
    final double accrualRate = ammount / s_act365.getDayCountFraction(startDate, endDate);
    
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

  private double valueDefaultLeg(CDSDerivative cds, ZonedDateTime stepinDate, YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, ZonedDateTime pricingDate) {

    // TODO: Offset uses observationStartOfDay, which is true precisely when protectStart is set on the CDS
    final int offset = cds.isProtectStart() ? -1 : 0;
    
    // TODO: Get CDS maturity
    final ZonedDateTime cdsStartDate = cds.getPremium().getNthPayment(0).getAccrualStartDate();
    final ZonedDateTime cdsEndDate = cds.getPremium().getNthPayment(cds.getPremium().getNumberOfPayments()-1).getAccrualEndDate().minusDays(1); //ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC);
    
    // Start date is the latest of CDS start date, step-in date and pricing date
    final ZonedDateTime startDate =
      stepinDate.isAfter(cdsStartDate)
      ? stepinDate.isAfter(pricingDate)
        ? stepinDate
        : pricingDate
      : cdsStartDate.isAfter(pricingDate)
        ? cdsStartDate
        : pricingDate;
    
    final double value = cds.isPayOnDefault()
      ? valueDefaultLegPayOnDefault(cds, pricingDate, startDate.plusDays(offset), cdsEndDate, discountCurve, spreadCurve)
      : valueDefaultLegPayOnMaturity();
    
    final double discount = 1.0; // TODO: verify assumptions about pricing date  -- cdsCcyCurve.getDiscountFactor(valueDate);

    return value / discount;
  }

  private double valueDefaultLegPayOnDefault(final CDSDerivative cds, ZonedDateTime pricingDate, ZonedDateTime startDate, ZonedDateTime maturity,
    YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve) {

    // CDS has already matured
    if (pricingDate.isAfter(maturity)) {
      return 0.0;
    }
    
    final double startTime = s_act365.getDayCountFraction(pricingDate, startDate); // cds.getStartTime(); //
    final double endTime = s_act365.getDayCountFraction(pricingDate, maturity); // cds.getMaturity(); // 
    
    if (startTime != s_act365.getDayCountFraction(pricingDate, startDate) || endTime != s_act365.getDayCountFraction(pricingDate, maturity)) {
      s_logger.debug("startTime: in cds = " + startTime + ", using act365 = " + s_act365.getDayCountFraction(pricingDate, startDate) + " (pricingDate=" + pricingDate + ")");
      s_logger.debug("endTime: in cds = " + endTime + ", using act365 = " + s_act365.getDayCountFraction(pricingDate, maturity) + " (pricingDate=" + pricingDate + ")");
    }
    
    // TODO: start and end times
    NavigableSet<Double> fullTimeline = buildTimeLine(discountCurve, spreadCurve, startTime, endTime);
    final Double[] timeline = truncateTimeLine(fullTimeline, startTime, endTime);

    final double loss = 1.0 - cds.getRecoveryRate();

    double dt, spread0, spread1, discount0, discount1;
    double lambda, fwdRate, valueForTimeStep, value;
    
    // For t < 0, assume discount factor = 1 (MAT: I think this is how ISDA works, TODO: confirm)
    spread1 = startTime > 0.0 ? spreadCurve.getDiscountFactor(startTime) : 1.0;
    discount1 = startTime > 0.0 ? discountCurve.getDiscountFactor(startTime) : 1.0;

    value = 0.0;

    for (int i = 1; i < timeline.length; ++i) {

      dt = timeline[i] - timeline[i - 1];

      spread0 = spread1;
      spread1 = spreadCurve.getDiscountFactor(timeline[i]);

      discount0 = discount1;
      discount1 = discountCurve.getDiscountFactor(timeline[i]);

      lambda = Math.log(spread0 / spread1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      valueForTimeStep = ((loss * lambda) / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * dt)) * spread0 * discount0;

      value += valueForTimeStep;
    }

    return value;
  }
  
  private double valueDefaultLegPayOnMaturity() {
    
    // TODO: implement me
    return 0.0;
  }

  /**
   * Build a set of time points corresponding to data points on the discount and spread curve
   * 
   * @param discountCurve The discount curve
   * @param spreadCurve The spread curve
   * @param startTime The start time is included as an extra data point on the curve
   * @param endTime The end time is included as an extra data point on the curve
   * 
   * @return A set of time points
   */
  public static NavigableSet<Double> buildTimeLine(YieldAndDiscountCurve discountCurve, YieldAndDiscountCurve spreadCurve, double startTime, double endTime) {

    if (!(discountCurve instanceof YieldCurve) || !(spreadCurve instanceof YieldCurve)) {
      throw new OpenGammaRuntimeException("ISDA CDS model requires YieldCurve objects for discount and spread curves");
    }

    final Double[] cdsCcyCurveTimes = ((YieldCurve) discountCurve).getCurve().getXData();
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
