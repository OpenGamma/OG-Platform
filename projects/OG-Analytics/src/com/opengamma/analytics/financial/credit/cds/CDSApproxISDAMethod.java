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
import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.SingleRootFinder;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualThreeSixty;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * An approximation to the calculation method for the ISDA CDS model
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class CDSApproxISDAMethod {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CDSApproxISDAMethod.class);
  
  private static DayCount s_act365 = new ActualThreeSixtyFive();
  
  private static class Timeline {
    public final double[] timePoints;
    public final double[] discountFactors;
    
    public Timeline(final double[] timePoints, final double[] discountFactors) {
      this.timePoints = timePoints;
      this.discountFactors = discountFactors;
    }
  }

  /**
   * Calculate the up-front charge of for a CDS. This is what would change hands if the contract were traded.
   * and is effectively its PV.
   * 
   * Step-in date is the date when ownership of protection is assumed and is normally taken as T+1, so a CDS traded
   * on a particular day becomes effective the next day at 00:01. To value an new CDS contract with start date
   * T >= 0, then step-in date = T+1. To value an existing contract at some point T, step-in date = T+1 again
   * but now start date < 0.
   * 
   * @param cds The CDS to be valued
   * @param discountCurve The discount curve
   * @param hazardRateCurve The credit spread curve
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate the settlement date
   * @param cleanPrice Whether the price is clean (true) or dirty (false)
   * @return PV of the CDS contract
   */
  public double calculateUpfrontCharge(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve,
    ZonedDateTime pricingDate, ZonedDateTime stepinDate, ZonedDateTime settlementDate, boolean cleanPrice) {
    
    final CouponFixed[] premiums = cds.getPremium().getPayments();
    final int offset = cds.isProtectStart() ? 1 : 0;
    final ZonedDateTime startDate = premiums[0].getAccrualStartDate();
    final ZonedDateTime maturityDate = premiums[premiums.length - 1].getAccrualEndDate();
    final ZonedDateTime offsetMaturityDate = maturityDate.plusDays(offset);
    final double startTime = getTimeBetween(pricingDate, startDate);
    final double maturityTime = getTimeBetween(pricingDate, maturityDate);
    final double offsetMaturityTime = getTimeBetween(pricingDate, offsetMaturityDate);
    
    final ZonedDateTime offsetStepinDate = stepinDate.minusDays(offset);
    final ZonedDateTime offsetPricingDate = pricingDate.minusDays(offset);
    
    final ZonedDateTime protectionStartDate =
      startDate.isAfter(offsetStepinDate)
      ? startDate.isAfter(offsetPricingDate)
        ? startDate
        : offsetPricingDate
      : offsetStepinDate.isAfter(offsetPricingDate)
        ? offsetStepinDate
        : offsetPricingDate;
    
    final double protectionStartTime = getTimeBetween(pricingDate, protectionStartDate);
    
    final Timeline feeTimeline = buildTimeline(cds, discountCurve, hazardRateCurve, pricingDate, startTime, offsetMaturityTime, true);
    final Timeline contingentTimeline = buildTimeline(cds, discountCurve, hazardRateCurve, pricingDate, protectionStartTime, maturityTime, false);
    final Timeline paymentTimeline = buildPaymentTimeline(cds, discountCurve);
    
    return valueCDS(cds, discountCurve, hazardRateCurve, paymentTimeline, feeTimeline, contingentTimeline, pricingDate, stepinDate, settlementDate, cleanPrice);
  }

  public double calculateUpfrontCharge(final CDSDerivative cds, final ISDACurve discountCurve, double flatSpread,
      final ZonedDateTime pricingDate, final ZonedDateTime stepinDate, final ZonedDateTime settlementDate, boolean cleanPrice) {
    
    final CouponFixed[] premiums = cds.getPremium().getPayments();
    final int offset = cds.isProtectStart() ? 1 : 0;
    final ZonedDateTime startDate = premiums[0].getAccrualStartDate();
    final ZonedDateTime maturityDate = premiums[premiums.length - 1].getAccrualEndDate();
    final ZonedDateTime offsetMaturityDate = maturityDate.plusDays(offset);
    final double startTime = getTimeBetween(pricingDate, startDate);
    final double maturityTime = getTimeBetween(pricingDate, maturityDate);
    final double offsetMaturityTime = getTimeBetween(pricingDate, offsetMaturityDate);
    
    final ZonedDateTime offsetStepinDate = stepinDate.minusDays(offset);
    final ZonedDateTime offsetPricingDate = pricingDate.minusDays(offset);
    
    final ZonedDateTime protectionStartDate =
      startDate.isAfter(offsetStepinDate)
      ? startDate.isAfter(offsetPricingDate)
        ? startDate
        : offsetPricingDate
      : offsetStepinDate.isAfter(offsetPricingDate)
        ? offsetStepinDate
        : offsetPricingDate;
    
    final double protectionStartTime = getTimeBetween(pricingDate, protectionStartDate);
    
    final Timeline feeTimeline = buildTimeline(cds, discountCurve, null, pricingDate, startTime, offsetMaturityTime, true);
    final Timeline contingentTimeline = buildTimeline(cds, discountCurve, null, pricingDate, protectionStartTime, maturityTime, false);
    final Timeline paymentTimeline = buildPaymentTimeline(cds, discountCurve);
    
    final double[] timePoints = {cds.getMaturity()};
    final double[] dataPoints = {flatSpread};

    final CDSDefinition zeroCDSDefinition = makeZeroCDSDefintion(startDate, maturityDate, dataPoints[0], cds.getRecoveryRate());
    final CDSDerivative zeroCDS = zeroCDSDefinition.toDerivative(pricingDate, "IR_CURVE", "TEMP_CURVE");
    
    SingleRootFinder<Double, Double> rootFinder = new BrentSingleRootFinder(1E-17);
    
    double guess = dataPoints[0] / (1 + cds.getRecoveryRate());
    
    dataPoints[0] = rootFinder.getRoot(
      new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double x) {
          //System.out.println("Evaluating " + x);
          dataPoints[0] = x;   
          ISDACurve tempCurve = new ISDACurve("TEMP_CURVE", timePoints, dataPoints, 0.0);
          return valueCDS(zeroCDS, discountCurve, tempCurve, paymentTimeline, feeTimeline, contingentTimeline, pricingDate, stepinDate, settlementDate, true);
        }  
      },
      0.0, 100.0
    );
    
    final ISDACurve hazardRateCurve = new ISDACurve("HAZARD_RATE_CURVE", timePoints, dataPoints, 0.0);
      
    return valueCDS(cds, discountCurve, hazardRateCurve, paymentTimeline, feeTimeline, contingentTimeline, pricingDate, stepinDate, settlementDate, cleanPrice);
  }
  
  private CDSDefinition makeZeroCDSDefintion(ZonedDateTime startDate, ZonedDateTime maturity, final double spread, final double recoveryRate) {
    
    final double notional = 1.0;
    
    // TODO: source these values from somewhere
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final DayCount dayCount = new ActualThreeSixty();
    final BusinessDayConvention convention = new FollowingBusinessDayConvention();
    
    final CDSPremiumDefinition premiumDefinition = CDSPremiumDefinition.fromISDA(
        Currency.USD, startDate, maturity,
        couponFrequency, calendar, dayCount, convention,
        notional, spread,
        /* protect start */ true);
    
    return new CDSDefinition(premiumDefinition, null, startDate, maturity, notional, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, dayCount);
  }
  
  private double valueCDS(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve, Timeline paymentTimeline, Timeline feeTimeline, Timeline contingentTimeline,
  ZonedDateTime pricingDate, ZonedDateTime stepinDate, ZonedDateTime settlementDate, boolean cleanPrice) {
    
    if (stepinDate.isBefore(pricingDate)) {
      throw new OpenGammaRuntimeException("Cannot value a CDS with step-in date before pricing date");
    }
    
    final double contingentLeg = valueContingentLeg(cds, discountCurve, hazardRateCurve, contingentTimeline, pricingDate, stepinDate, settlementDate);
    final double feeLeg = valueFeeLeg(cds, discountCurve, hazardRateCurve, paymentTimeline, feeTimeline, pricingDate, stepinDate, settlementDate);
    final double dirtyPrice = (contingentLeg - feeLeg) * cds.getNotional();
    
    return cleanPrice ? dirtyPrice + cds.getAccruedInterest() : dirtyPrice;
  }

  /**
   * Value the premium leg of a CDS taken from the step-in date.
   * 
   * @param cds The CDS contract being priced
   * @param discountCurve The discount curve
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate the settlement date
   * @return PV of the CDS premium leg
   */
  private double valueFeeLeg(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve, Timeline paymentTimeline, Timeline accrualTimeline,
    ZonedDateTime pricingDate, ZonedDateTime stepinDate, ZonedDateTime settlementDate) {
    
    // If the "protect start" flag is set, then the start date of the CDS is protected and observations are made at the start
    // of the day rather than the end. This is modelled by shifting all period start/end dates one day forward,
    // and adding one extra day's protection to the final period
    
    final CouponFixed[] premiums = cds.getPremium().getPayments();
    final int maturityIndex = premiums.length - 1;
    final int offset = cds.isProtectStart() ? 1 : 0;
    
    final ZonedDateTime maturityDate = premiums[premiums.length - 1].getAccrualEndDate().plusDays(offset);
    final double offsetStepinTime = getTimeBetween(pricingDate, stepinDate.minusDays(offset));
    final double settlementTime = getTimeBetween(pricingDate, settlementDate);

    CouponFixed payment;
    ZonedDateTime accrualPeriodStart, accrualPeriodEnd;
    double periodStart, periodEnd, paymentTime;
    double amount, survival, discount;
    double result = 0.0;
    
    for (int i = 0; i < premiums.length; i++) {
      
      payment = premiums[i];
      accrualPeriodEnd = i < maturityIndex ? payment.getAccrualEndDate() : maturityDate;
      periodEnd = getTimeBetween(pricingDate, accrualPeriodEnd.minusDays(offset));

      amount = payment.getFixedRate() * payment.getPaymentYearFraction();
      survival = hazardRateCurve.getDiscountFactor(periodEnd);
      discount = paymentTimeline.discountFactors[i];
      result += amount * survival * discount;
      
      if (cds.isAccrualOnDefault()) {
        accrualPeriodStart = payment.getAccrualStartDate();
        periodStart = getTimeBetween(pricingDate, accrualPeriodStart.minusDays(offset));
        result += valueFeeLegAccrualOnDefault(amount, periodStart, periodEnd, offsetStepinTime, discountCurve, hazardRateCurve, accrualTimeline);
      }
    }
    
    return result / discountCurve.getDiscountFactor(settlementTime);
  }

  /**
   * Calculate the accrual-on-default portion of the PV for a specified accrual period.
   * 
   * @param amount Amount of premium that would be accrued over the entire period (in the case of no default)
   * @param startTime Accrual period start time
   * @param endTime Accrual period end time
   * @param stepinTime Step-in time for the CDS contract
   * @param discountCurve The discount curve
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param fullTimeline List of time points corresponding to real data points on the discount and spread curves
   * @return Accrual-on-default portion of PV for the accrual period
   */
  private double valueFeeLegAccrualOnDefault(final double amount, final double startTime, final double endTime, final double stepinTime,
    ISDACurve discountCurve, ISDACurve hazardRateCurve, Timeline timeline) {
    
    final double today = 0.0;
    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    final double accrualRate = amount / (endTime - startTime); // TODO: Handle startTime == endTime

    double t0, t1, dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, lambdaFwdRate, valueForTimeStep, value;
    
    t0 = subStartTime - startTime + (0.5 / 365.0);
    survival0 = hazardRateCurve.getDiscountFactor(subStartTime);
    discount0 = discountCurve.getDiscountFactor(Math.max(today, subStartTime));
    
    value = 0.0;
    
    int i = 0;
    
    while (timeline.timePoints[i] < startTime) {
      ++i;
    }

    for (i += 1; i < timeline.timePoints.length; ++i) {
      
      if (timeline.timePoints[i] <= stepinTime) {
        continue;
      }
      
      if (timeline.timePoints[i] > endTime) {
        break;
      }

      t1 = timeline.timePoints[i] - startTime + (0.5 / 365.0);
      dt = t1 - t0;
      
      survival1 = hazardRateCurve.getDiscountFactor(timeline.timePoints[i]);
      discount1 = timeline.discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdaFwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accrualRate * survival0 * discount0
        * (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * survival1 / survival0 * discount1 / discount0);

      value += valueForTimeStep;

      t0 = t1;
      survival0 = survival1;
      discount0 = discount1;
    }

    return value;
  }

  /**
   * Value the contingent leg of a CDS taken from the step-in date.
   * 
   * @param cds The derivative object representing the CDS
   * @param discountCurve The discount curve
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate the settlement date
   * @return PV of the CDS default leg
   */
  private double valueContingentLeg(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve, Timeline contingentTimeline,
      ZonedDateTime pricingDate, ZonedDateTime stepinDate, ZonedDateTime settlementDate) {

    final CouponFixed[] premiums = cds.getPremium().getPayments();
    final ZonedDateTime startDate = premiums[0].getAccrualStartDate();
    final ZonedDateTime maturityDate = premiums[premiums.length - 1].getAccrualEndDate();
    
    final int offset = cds.isProtectStart() ? 1 : 0;
    final ZonedDateTime offsetStepinDate = stepinDate.minusDays(offset);
    final ZonedDateTime offsetPricingDate = pricingDate.minusDays(offset);
    
    final ZonedDateTime valuationStartDate =
      startDate.isAfter(offsetStepinDate)
      ? startDate.isAfter(offsetPricingDate)
        ? startDate
        : offsetPricingDate
      : offsetStepinDate.isAfter(offsetPricingDate)
        ? offsetStepinDate
        : offsetPricingDate;
    
    final double valuationStartTime = getTimeBetween(pricingDate, valuationStartDate);
    final double maturity = getTimeBetween(pricingDate, maturityDate);
    final double settlementTime = getTimeBetween(pricingDate, settlementDate);
    final double recoveryRate = cds.getRecoveryRate();
    
    final double value = cds.isPayOnDefault()
      ? valueContingentLegPayOnDefault(recoveryRate, valuationStartTime, maturity, discountCurve, hazardRateCurve, contingentTimeline)
      : valueContingentLegPayOnMaturity(recoveryRate, valuationStartTime, maturity, discountCurve, hazardRateCurve);

    return value / discountCurve.getDiscountFactor(settlementTime);
  }

  /**
   * Value the default leg, assuming any possible payout is received at the time of default.
   * 
   * @param recoveryRate Recovery rate of the CDS underlying asset
   * @param startTime protection start time
   * @param maturity CDS maturity
   * @param discountCurve The discount curve
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV for the default leg
   */
  private double valueContingentLegPayOnDefault(final double recoveryRate, final double startTime, final double maturity,
    ISDACurve discountCurve, ISDACurve hazardRateCurve, Timeline timeline) {
    
    if (maturity < 0.0) {
      return 0.0;
    }
    
    final double loss = 1.0 - recoveryRate;

    double dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, valueForTimeStep, value;
    
    // TODO: handle startTime == -1 day
    survival1 = hazardRateCurve.getDiscountFactor(startTime);
    discount1 = startTime > 0.0 ? discountCurve.getDiscountFactor(startTime) : 1.0;
    value = 0.0;

    for (int i = 1; i < timeline.timePoints.length; ++i) {
      
      dt = timeline.timePoints[i] - timeline.timePoints[i - 1];

      survival0 = survival1;
      discount0 = discount1;
      survival1 = hazardRateCurve.getDiscountFactor(timeline.timePoints[i]);
      discount1 = timeline.discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      valueForTimeStep = ((loss * lambda) / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * dt)) * survival0 * discount0;

      value += valueForTimeStep;
    }

    return value;
  }
  
  /**
   * Value the default leg, assuming any possible payout is received at maturity.
   * 
   * @param recoveryRate Recovery rate of the CDS underlying asset
   * @param startTime protection start time
   * @param maturity CDS maturity
   * @param discountCurve The discount curve
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV for the default leg
   */
  private double valueContingentLegPayOnMaturity(final double recoveryRate, final double startTime, final double maturity,
    ISDACurve discountCurve, ISDACurve hazardRateCurve) {
    
    if (maturity < 0.0) {
      return 0.0;
    }
    
    // TODO: handle startTime == -1 day
    final double loss = 1.0 - recoveryRate;
    final double survival0 = hazardRateCurve.getDiscountFactor(startTime);
    final double survival1 = hazardRateCurve.getDiscountFactor(maturity);
    final double discount = discountCurve.getDiscountFactor(maturity);
    
    return (survival0 - survival1) * discount * loss;
  }
  
  
  private Timeline buildTimeline(CDSDerivative cds, ISDACurve discountCurve, ISDACurve hazardRateCurve, ZonedDateTime pricingDate, double startTime, double endTime, boolean includeSchedule) {
    
    NavigableSet<Double> timePoints = new TreeSet<Double>();
    
    final double[] discountCurveTimePoints = discountCurve.getTimePoints();
    for (int i = 0; i < discountCurveTimePoints.length; ++i) {
      timePoints.add(new Double(discountCurveTimePoints[i]));
    }
    
    if (hazardRateCurve != null) {
      final double[] hazardRateCurveTimePoints = hazardRateCurve.getTimePoints();
      for (int i = 0; i < hazardRateCurveTimePoints.length; ++i) {
        timePoints.add(new Double(hazardRateCurveTimePoints[i]));
      }
    } else {
      timePoints.add(cds.getMaturity());
    }
    
    timePoints.add(new Double(startTime));
    timePoints.add(new Double(endTime));
    
    if (includeSchedule) {
      
      final CouponFixed[] premiums = cds.getPremium().getPayments();
      final int maturityIndex = premiums.length - 1;
      final int offset = cds.isProtectStart() ? 1 : 0;
      final ZonedDateTime offsetMaturityDate = premiums[premiums.length - 1].getAccrualEndDate().plusDays(offset);
    
      CouponFixed payment;
      ZonedDateTime periodEndDate;
      double periodEndTime;
      
      for (int i = 0; i < premiums.length; i++) {
        
        payment = premiums[i];
        periodEndDate = i < maturityIndex ? payment.getAccrualEndDate() : offsetMaturityDate;
        periodEndTime = getTimeBetween(pricingDate, periodEndDate.minusDays(offset));
        timePoints.add(new Double(periodEndTime));
      }
    }
    
    final Set<Double> timePointsInRange = timePoints.subSet(startTime, true, endTime, true);
    
    Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);
    
    double[] timePoints_ = new double[boxed.length];
    double[] discountFactors_ = new double[boxed.length];
    
    for (int i = 0; i < boxed.length; ++i) {
      timePoints_[i] = boxed[i].doubleValue();
      discountFactors_[i] = discountCurve.getDiscountFactor(timePoints_[i]);
    }
    
    return new Timeline(timePoints_, discountFactors_);
  }
  
  private Timeline buildPaymentTimeline(CDSDerivative cds, ISDACurve discountCurve) {
    
    final CouponFixed[] payments = cds.getPremium().getPayments();
    
    double[] timePoints = new double[payments.length];
    double[] discountFactors = new double[payments.length];
    
    for (int i = 0; i < payments.length; ++i) {
      timePoints[i] = payments[i].getPaymentTime();
      discountFactors[i] = discountCurve.getDiscountFactor(timePoints[i]);
    }
    
    return new Timeline(timePoints, discountFactors);
  }
  
  /**
   * Return the fraction of a year between two dates according the the ACT365 convention.
   * If date2 < date1 the result will be negative.
   * 
   * @param date1 The first date
   * @param date2 The second date
   * @return Real time value in years
   */
  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {

    final ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());
    final boolean timeIsNegative = date1.isAfter(rebasedDate2);

    if (!timeIsNegative) {
      return s_act365.getDayCountFraction(date1, rebasedDate2);
    } else {
      return -1.0 * s_act365.getDayCountFraction(rebasedDate2, date1);
    }
  }
}
