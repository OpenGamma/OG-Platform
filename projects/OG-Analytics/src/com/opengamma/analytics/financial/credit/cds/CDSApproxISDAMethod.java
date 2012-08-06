/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.util.money.CurrencyAmount;

/**
 * An approximation to the calculation method for the ISDA CDS model
 * 
 * @author Martin Traverse & Niels Stchedroff (Riskcare)
 */
public class CDSApproxISDAMethod implements PricingMethod {

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    CDSDerivative cds = (CDSDerivative) instrument;
    YieldAndDiscountCurve cdsCcyCurve = curves.getCurve(cds.getCdsCcyCurveName());
    YieldAndDiscountCurve spreadCurve = curves.getCurve(cds.getSpreadCurveName());

    return CurrencyAmount.of(cds.getPremium().getCurrency(), calculateUpfrontCharge(cds, cdsCcyCurve, spreadCurve));
  }

  /**
   * Do the calculation
   * 
   * @param cds
   * @param cdsCcyCurve
   * @param spreadCurve
   * @return
   */
  private double calculateUpfrontCharge(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve) {
    return valuePremiumLeg(cds, cdsCcyCurve, spreadCurve) - valueDefaultLeg(cds, cdsCcyCurve, spreadCurve);
  }

  private double valuePremiumLeg(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve) {

    double stepinDate = 0.0;
    double startDate = cds.getProtectionStartTime();
    double subStartDate = stepinDate > startDate ? stepinDate : startDate;
    
    Double[] timeline = realTimePointsForCurves(cds, cdsCcyCurve, spreadCurve); // TODO: optionally use critical dates trucated by CDS protection period

    /** the integration - we can assume flat forwards between points on
       the timeline - this is true for both curves 

       we are integrating -Zt dS/dt where Z is the discount factor and
       S is the survival probability and t is the accrual time

       assuming flat forwards on each part of the integration, this is an
       exact integral
    */

    double accRate = cds.getNotional() * cds.getSpread();

    double today = 0.0;
    double startTime = Math.max(cds.getProtectionStartTime(), 0.0);

    double dt;
    double t0 = subStartDate + 0.5 / 365.0 - startTime;
    double t1;
    double spread0 = spreadCurve.getDiscountFactor(subStartDate);
    double spread1;
    double discount0 = cdsCcyCurve.getDiscountFactor(today > subStartDate ? today : subStartDate);
    double discount1;

    double lambda;
    double fwdRate;
    double lambdafwdRate;
    double valueForTimeStep;

    double value = 0.0;

    for (int i = 1; i < timeline.length; ++i) {

      if (!(timeline[i] > stepinDate)) {
        continue;
      }

      t1 = timeline[i] + 0.5 / 365.0 - startTime; // TODO: Replace with proper day count constructs
      dt = t1 - t0;

      spread1 = spreadCurve.getDiscountFactor(timeline[i]);
      discount1 = cdsCcyCurve.getDiscountFactor(timeline[i]);

      lambda = Math.log(spread0 / spread1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdafwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accRate * spread0 * discount0
        * (((t0 + 1.0 / lambdafwdRate) / lambdafwdRate) - ((t1 + 1.0 / lambdafwdRate) / lambdafwdRate) * spread1 / spread0 * discount1 / discount0);

      value += valueForTimeStep;

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

    // CDS has already matured
    if (cds.getMaturity() < 0.0) {
      return 0.0;
    }

    final double loss = 1.0 - cds.getRecoveryRate();
    final Double[] timePoints = realTimePointsForCurves(cds, cdsCcyCurve, spreadCurve);

    /** the integration - we can assume flat forwards between points on
       the timeline - this is true for both curves 

       we are integrating -Z dS/dt where Z is the discount factor and
       S is the survival probability

       assuming flat forwards on each part of the integration, this is an
       exact integral
    */

    // TODO: consider what is meant by "today"
    double today = 0.0;

    double dt;
    double spread0;
    double spread1 = spreadCurve.getDiscountFactor(cds.getProtectionStartTime() > today ? cds.getProtectionStartTime() : today);
    double discount0;
    double discount1 = cdsCcyCurve.getDiscountFactor(cds.getProtectionStartTime() > today ? cds.getProtectionStartTime() : today);

    double lambda;
    double fwdRate;
    double valueForTimeStep;

    double value = 0.0;

    for (int i = 1; i < timePoints.length; ++i) {

      dt = timePoints[i] - timePoints[i - 1];

      spread0 = spread1;
      spread1 = spreadCurve.getDiscountFactor(timePoints[i]);

      discount0 = discount1;
      discount1 = cdsCcyCurve.getDiscountFactor(timePoints[i]);

      lambda = Math.log(spread0 / spread1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      valueForTimeStep = ((loss * lambda) / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * dt)) * spread0 * discount0;

      value += valueForTimeStep;
    }

    return value;
  }

  public static Double[] realTimePointsForCurves(CDSDerivative cds, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve spreadCurve) {

    final Double[] cdsCcyCurveTimes = ((YieldCurve) cdsCcyCurve).getCurve().getXData();
    final Double[] spreadCurveTimes = ((YieldCurve) spreadCurve).getCurve().getXData();

    NavigableSet<Double> timePoints = new TreeSet<Double>();
    for (int i = 0; i < cdsCcyCurveTimes.length; i++) {
      timePoints.add(cdsCcyCurveTimes[i]);
    }

    for (int i = 0; i < spreadCurveTimes.length; i++) {
      timePoints.add(spreadCurveTimes[i]);
    }

    final double startTime = Math.max(cds.getProtectionStartTime(), 0.0);
    timePoints.add(startTime);
    timePoints.add(cds.getMaturity());
    
    final Set<Double> timePointsInRange = timePoints.subSet(startTime, true, cds.getMaturity(), true);

    return timePointsInRange.toArray(new Double[timePointsInRange.size()]);
  }
}
