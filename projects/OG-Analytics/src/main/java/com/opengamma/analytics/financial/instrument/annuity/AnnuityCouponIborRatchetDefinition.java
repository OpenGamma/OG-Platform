/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for a AnnuityDefinition containing mainly CouponIborRatchetDefinition. The first coupon should be a CouponFixedDefinition or a CouponIborGearingDefinition.
 */
public class AnnuityCouponIborRatchetDefinition extends AnnuityCouponDefinition<CouponDefinition> {

  /**
   * Array used for the list conversion.
   */
  protected static final Coupon[] EMPTY_ARRAY_CPN = new Coupon[0];

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   * @param calendar The calendar
   */
  public AnnuityCouponIborRatchetDefinition(final CouponDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
    ArgumentChecker.isTrue((payments[0] instanceof CouponFixedDefinition) || (payments[0] instanceof CouponIborGearingDefinition),
        "First coupon should be CouponFixedDefinition or a CouponIborGearingDefinition");
    for (int looppay = 1; looppay < payments.length; looppay++) {
      ArgumentChecker.isTrue((payments[looppay] instanceof CouponIborRatchetDefinition), "Next coupons should be CouponIborRatchetDefinition");
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
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   * @param floorCoefficients The coefficients of the floor. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   * @param capCoefficients The coefficients of the cap. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The annuity.
   */
  public static AnnuityCouponIborRatchetDefinition withFirstCouponFixed(final ZonedDateTime settlementDate, final Period annuityTenor, final double notional, final IborIndex index,
      final boolean isPayer, final double firstCouponFixedRate, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients,
      final Calendar calendar) {
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, annuityTenor, index.getTenor(), index.getBusinessDayConvention(), calendar,
        index.isEndOfMonth());
    final CouponDefinition[] coupons = new CouponDefinition[paymentDates.length];
    final double notionalSign = notional * (isPayer ? -1.0 : 1.0);
    coupons[0] = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0],
        index.getDayCount().getDayCountFraction(settlementDate, paymentDates[0], calendar), notionalSign, firstCouponFixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponIborRatchetDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          index.getDayCount().getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), notionalSign,
          ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar), index, mainCoefficients, floorCoefficients,
          capCoefficients, calendar);
    }
    return new AnnuityCouponIborRatchetDefinition(coupons, calendar);
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
   * @param calendar The holiday calendar for the ibor leg.
   * @return The annuity.
   */
  public static AnnuityCouponIborRatchetDefinition withFirstCouponIborGearing(final ZonedDateTime settlementDate, final Period annuityTenor, final double notional, final IborIndex index,
      final boolean isPayer, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients, final Calendar calendar) {
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, annuityTenor, index.getTenor(), index.getBusinessDayConvention(), calendar,
        index.isEndOfMonth());
    final CouponDefinition[] coupons = new CouponDefinition[paymentDates.length];
    final double notionalSign = notional * (isPayer ? -1.0 : 1.0);
    coupons[0] = CouponIborGearingDefinition.from(settlementDate, paymentDates[0], index.getDayCount().getDayCountFraction(settlementDate, paymentDates[0], calendar),
        notionalSign, index, mainCoefficients[2], mainCoefficients[1], calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponIborRatchetDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          index.getDayCount().getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), notionalSign,
          ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar), index, mainCoefficients, floorCoefficients, capCoefficients, calendar);
    }
    return new AnnuityCouponIborRatchetDefinition(coupons, calendar);
  }

  @Override
  public AnnuityCouponIborRatchet toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgumentChecker.notNull(date, "date");
    final List<Coupon> cpnList = new ArrayList<>();
    final List<Double> fixedCpn = new ArrayList<>();
    final boolean[] isFixed = new boolean[getNumberOfPayments()];

    // List of fixing
    if (getPayments()[0] instanceof CouponFixedDefinition) { // CouponFixedDefinition
      isFixed[0] = true;
    } else { // CouponIborGearingDefinition
      isFixed[0] = (indexFixingTS.getValue(((CouponIborGearingDefinition) getPayments()[0]).getFixingDate())) != null;
    }
    for (int loopcpn = 1; loopcpn < getNumberOfPayments(); loopcpn++) {
      isFixed[loopcpn] = (indexFixingTS.getValue(((CouponIborRatchetDefinition) getPayments()[loopcpn]).getFixingDate())) != null;
    }
    // Already fixed coupons
    if (getPayments()[0] instanceof CouponFixedDefinition) { // CouponFixedDefinition
      fixedCpn.add(((CouponFixedDefinition) getNthPayment(0)).getRate());
    } else { // CouponIborDefinition
      if (isFixed[0]) {
        final CouponIborGearingDefinition cpnIbor = (CouponIborGearingDefinition) getPayments()[0];
        fixedCpn.add(indexFixingTS.getValue(cpnIbor.getFixingDate()) * cpnIbor.getFactor() + cpnIbor.getSpread());
      }
    }
    for (int loopcpn = 1; loopcpn < getNumberOfPayments(); loopcpn++) {
      if (isFixed[loopcpn]) {
        final CouponIborRatchetDefinition cpnRatchet = (CouponIborRatchetDefinition) getPayments()[loopcpn];
        final double ibor = indexFixingTS.getValue(cpnRatchet.getFixingDate());
        final double cpnMain = cpnRatchet.getMainCoefficients()[0] * fixedCpn.get(loopcpn - 1) + cpnRatchet.getMainCoefficients()[1] * ibor + cpnRatchet.getMainCoefficients()[2];
        final double cpnFloor = cpnRatchet.getFloorCoefficients()[0] * fixedCpn.get(loopcpn - 1) + cpnRatchet.getFloorCoefficients()[1] * ibor + cpnRatchet.getFloorCoefficients()[2];
        final double cpnCap = cpnRatchet.getCapCoefficients()[0] * fixedCpn.get(loopcpn - 1) + cpnRatchet.getCapCoefficients()[1] * ibor + cpnRatchet.getCapCoefficients()[2];
        final double cpnActual = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
        fixedCpn.add(cpnActual);
      }
    }
    // Derivatives
    final CouponDefinition cpn0 = getNthPayment(0);
    if (!date.isAfter(cpn0.getPaymentDate())) {
      if (isFixed[0]) {
        cpnList.add(new CouponFixed(getCurrency(), TimeCalculator.getTimeBetween(date, cpn0.getPaymentDate()), cpn0.getPaymentYearFraction(), cpn0.getNotional(), fixedCpn.get(0),
            cpn0.getAccrualStartDate(), cpn0.getAccrualEndDate()));
      } else { // CouponIborGearingDefinition
        cpnList.add(((CouponIborGearingDefinition) cpn0).toDerivative(date));
      }
    }
    for (int loopcpn = 1; loopcpn < getNumberOfPayments(); loopcpn++) {
      final CouponDefinition cpn = getNthPayment(loopcpn);
      if (!date.isAfter(getNthPayment(loopcpn).getPaymentDate())) {
        if (isFixed[loopcpn]) {
          cpnList.add(new CouponFixed(getCurrency(), TimeCalculator.getTimeBetween(date, cpn.getPaymentDate()), cpn.getPaymentYearFraction(), cpn.getNotional(), fixedCpn
              .get(loopcpn), cpn.getAccrualStartDate(), cpn.getAccrualEndDate()));
        } else {
          cpnList.add(((CouponIborRatchetDefinition) cpn).toDerivative(date));
        }
      }
    }
    return new AnnuityCouponIborRatchet(cpnList.toArray(EMPTY_ARRAY_CPN));
  }
}
