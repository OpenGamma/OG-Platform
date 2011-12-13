/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A wrapper class for a AnnuityDefinition containing mainly CouponIborRatchetDefinition. The first coupon should be a CouponFixedDefinition or a CouponIborGearingDefinition.
 */
public class AnnuityCouponIborRatchetDefinition extends AnnuityDefinition<CouponDefinition> {

  /**
   * Array used for the list conversion.
   */
  protected static final Coupon[] EMPTY_ARRAY_CPN = new Coupon[0];

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   */
  public AnnuityCouponIborRatchetDefinition(final CouponDefinition[] payments) {
    super(payments);
    Validate.isTrue((payments[0] instanceof CouponFixedDefinition) || (payments[0] instanceof CouponIborGearingDefinition),
        "First coupon should be CouponFixedDefinition or a CouponIborGearingDefinition");
    for (int looppay = 1; looppay < payments.length; looppay++) {
      Validate.isTrue((payments[looppay] instanceof CouponIborRatchetDefinition), "Next coupons should be CouponIborRatchetDefinition");
    }
  }

  /**
   * Build a Ratchet Ibor annuity with fixed first coupon. All the ratchet coupons have the same coefficients.
   * @param settlementDate The annuity settlement date.
   * @param annuityTenor The annuity tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer (true) / receiver (false) flag.
   * @param firstCouponFixedRate The rate of the first coupon.
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3.
   * @param floorCoefficients The coefficients of the floor. Array of length 3.
   * @param capCoefficients The coefficients of the cap. Array of length 3.
   * @return The annuity.
   */
  public static AnnuityCouponIborRatchetDefinition withFirstCouponFixed(final ZonedDateTime settlementDate, final Period annuityTenor, final double notional, final IborIndex index,
      final boolean isPayer, final double firstCouponFixedRate, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients) {
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, annuityTenor, index.getTenor(), index.getBusinessDayConvention(), index.getCalendar(),
        index.isEndOfMonth());
    CouponDefinition[] coupons = new CouponDefinition[paymentDates.length];
    double notionalSign = notional * (isPayer ? -1.0 : 1.0);
    coupons[0] = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0], index.getDayCount().getDayCountFraction(settlementDate, paymentDates[0]),
        notionalSign, firstCouponFixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponIborRatchetDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], index.getDayCount().getDayCountFraction(
          paymentDates[loopcpn - 1], paymentDates[loopcpn]), notionalSign, ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], index.getCalendar(), -index.getSpotLag()), index,
          mainCoefficients, floorCoefficients, capCoefficients);
    }
    return new AnnuityCouponIborRatchetDefinition(coupons);
  }

  /**
   * Build a Ratchet Ibor annuity with Ibor gearing first coupon. The factor and spread of the first coupon are the one of the main part of the Ratchet.
   * All the Ratchet coupons have the same coefficients.
   * @param settlementDate The annuity settlement date.
   * @param annuityTenor The annuity tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer (true) / receiver (false) flag.
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3.
   * @param floorCoefficients The coefficients of the floor. Array of length 3.
   * @param capCoefficients The coefficients of the cap. Array of length 3.
   * @return The annuity.
   */
  public static AnnuityCouponIborRatchetDefinition withFirstCouponIborGearing(final ZonedDateTime settlementDate, final Period annuityTenor, final double notional, final IborIndex index,
      final boolean isPayer, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients) {
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, annuityTenor, index.getTenor(), index.getBusinessDayConvention(), index.getCalendar(),
        index.isEndOfMonth());
    CouponDefinition[] coupons = new CouponDefinition[paymentDates.length];
    double notionalSign = notional * (isPayer ? -1.0 : 1.0);
    coupons[0] = CouponIborGearingDefinition.from(settlementDate, paymentDates[0], index.getDayCount().getDayCountFraction(settlementDate, paymentDates[0]), notionalSign, index, mainCoefficients[2],
        mainCoefficients[1]);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponIborRatchetDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], index.getDayCount().getDayCountFraction(
          paymentDates[loopcpn - 1], paymentDates[loopcpn]), notionalSign, ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], index.getCalendar(), -index.getSpotLag()), index,
          mainCoefficients, floorCoefficients, capCoefficients);
    }
    return new AnnuityCouponIborRatchetDefinition(coupons);
  }

  @Override
  public AnnuityCouponIborRatchet toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final List<Coupon> cpnList = new ArrayList<Coupon>();
    final List<Double> fixedCpn = new ArrayList<Double>();
    boolean[] isFixed = new boolean[getNumberOfPayments()];

    // List of fixing
    if (getPayments()[0] instanceof CouponFixedDefinition) { // CouponFixedDefinition
      isFixed[0] = true;
    } else { // CouponIborGearingDefinition
      isFixed[0] = (indexFixingTS.getValue(((CouponIborDefinition) getPayments()[0]).getFixingDate())) != null;
    }
    for (int loopcpn = 1; loopcpn < getNumberOfPayments(); loopcpn++) {
      isFixed[loopcpn] = (indexFixingTS.getValue(((CouponIborRatchetDefinition) getPayments()[loopcpn]).getFixingDate())) != null;
    }
    // Already fixed coupons
    if (getPayments()[0] instanceof CouponFixedDefinition) { // CouponFixedDefinition
      fixedCpn.add(((CouponFixedDefinition) getNthPayment(0)).getRate());
    } else { // CouponIborDefinition
      if (isFixed[0]) {
        CouponIborGearingDefinition cpnIbor = (CouponIborGearingDefinition) getPayments()[0];
        fixedCpn.add(indexFixingTS.getValue(cpnIbor.getFixingDate()) * cpnIbor.getFactor() + cpnIbor.getSpread());
      }
    }
    for (int loopcpn = 1; loopcpn < getNumberOfPayments(); loopcpn++) {
      if (isFixed[loopcpn]) {
        CouponIborRatchetDefinition cpnRatchet = (CouponIborRatchetDefinition) getPayments()[loopcpn];
        double ibor = indexFixingTS.getValue(cpnRatchet.getFixingDate());
        double cpnMain = cpnRatchet.getMainCoefficients()[0] * fixedCpn.get(loopcpn - 1) + cpnRatchet.getMainCoefficients()[1] * ibor + cpnRatchet.getMainCoefficients()[2];
        double cpnFloor = cpnRatchet.getFloorCoefficients()[0] * fixedCpn.get(loopcpn - 1) + cpnRatchet.getFloorCoefficients()[1] * ibor + cpnRatchet.getFloorCoefficients()[2];
        double cpnCap = cpnRatchet.getCapCoefficients()[0] * fixedCpn.get(loopcpn - 1) + cpnRatchet.getCapCoefficients()[1] * ibor + cpnRatchet.getCapCoefficients()[2];
        double cpnActual = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
        fixedCpn.add(cpnActual);
      }
    }
    // Derivatives 
    CouponDefinition cpn0 = getNthPayment(0);
    if (!date.isAfter(cpn0.getPaymentDate())) {
      if (isFixed[0]) {
        cpnList.add(new CouponFixed(getCurrency(), TimeCalculator.getTimeBetween(date, cpn0.getPaymentDate()), yieldCurveNames[0], cpn0.getPaymentYearFraction(), cpn0.getNotional(), fixedCpn.get(0),
            cpn0.getAccrualStartDate(), cpn0.getAccrualEndDate()));
      } else { // CouponIborGearingDefinition
        cpnList.add(((CouponIborGearingDefinition) cpn0).toDerivative(date, yieldCurveNames));
      }
    }
    for (int loopcpn = 1; loopcpn < getNumberOfPayments(); loopcpn++) {
      CouponDefinition cpn = getNthPayment(loopcpn);
      if (!date.isAfter(getNthPayment(loopcpn).getPaymentDate())) {
        if (isFixed[loopcpn]) {
          cpnList.add(new CouponFixed(getCurrency(), TimeCalculator.getTimeBetween(date, cpn.getPaymentDate()), yieldCurveNames[0], cpn.getPaymentYearFraction(), cpn.getNotional(), fixedCpn
              .get(loopcpn), cpn.getAccrualStartDate(), cpn.getAccrualEndDate()));
        } else {
          cpnList.add(((CouponIborRatchetDefinition) cpn).toDerivative(date, yieldCurveNames));
        }
      }
    }
    return new AnnuityCouponIborRatchet(cpnList.toArray(EMPTY_ARRAY_CPN));
  }
}
