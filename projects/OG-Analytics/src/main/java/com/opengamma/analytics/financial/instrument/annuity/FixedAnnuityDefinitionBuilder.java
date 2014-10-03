/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Generates an annuity of fixed rate coupons.
 */
public class FixedAnnuityDefinitionBuilder extends AbstractAnnuityDefinitionBuilder<FixedAnnuityDefinitionBuilder> {

  /**
   * The fixed rate of the annuity. This will be ignored for zero coupon annuities.
   * TODO schedule
   */
  private double _rate;

  public FixedAnnuityDefinitionBuilder rate(double rate) {
    _rate = rate;
    return this;
  }

  @Override
  public AnnuityDefinition<?> build() {
    PaymentDefinition[] coupons = null;

    int exchangeNotionalCoupons = 0;
    if (isExchangeInitialNotional()) {
      exchangeNotionalCoupons++;
    }
    if (isExchangeFinalNotional()) {
      exchangeNotionalCoupons++;
    }
    Calendar accrualCalendar = null;
    if (getAccrualPeriodAdjustmentParameters() != null) {
      accrualCalendar = getAccrualPeriodAdjustmentParameters().getCalendar();
    }
    if (Period.ZERO.equals(getAccrualPeriodFrequency())) {
      coupons = generateZeroCouponFlows(exchangeNotionalCoupons, accrualCalendar, getCompoundingMethod() == CompoundingMethod.NONE);
    } else {
      coupons = generateFixedCouponFlows(exchangeNotionalCoupons, accrualCalendar);
    }

    if (isExchangeInitialNotional()) {
      coupons[0] = getExchangeInitialNotionalCoupon();
    }
    if (isExchangeFinalNotional()) {
      coupons[coupons.length - 1] = getExchangeFinalNotionalCoupon();
    }

    /*
     * This assumes that the dates are adjusted, which may not always be true. Use the payment date adjustment calendar
     * if not null, otherwise use accrual date adjustment calendar.
     */
    Calendar calendar;
    if (getPaymentDateAdjustmentParameters() != null) {
      calendar = getPaymentDateAdjustmentParameters().getCalendar();
    } else if (getAccrualPeriodAdjustmentParameters() != null) {
      calendar = accrualCalendar;
    } else {
      calendar = null;
    }
    return new AnnuityDefinition<>(coupons, calendar);
  }

  private PaymentDefinition[] generateFixedCouponFlows(int exchangeNotionalCoupons, Calendar accrualCalendar) {
    CouponDefinition[] coupons;
    ZonedDateTime[] accrualEndDates = getAccrualEndDates();
    ZonedDateTime startDate = getStartDate();
    //    startDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
    //        getAccrualPeriodAdjustmentParameters().getCalendar(), startDate);
    ZonedDateTime[] accrualStartDates = ScheduleCalculator.getStartDates(startDate, accrualEndDates);

    ZonedDateTime[] paymentDates;
    if (DateRelativeTo.START == getPaymentDateRelativeTo()) {
      paymentDates = getPaymentDates(accrualStartDates);
    } else {
      paymentDates = getPaymentDates(accrualEndDates);
    }

    coupons = new CouponFixedDefinition[exchangeNotionalCoupons + accrualEndDates.length];

    int couponOffset = isExchangeInitialNotional() ? 1 : 0;
    for (int c = 0; c < accrualEndDates.length; c++) {
      coupons[c + couponOffset] = new CouponFixedDefinition(
          getCurrency(),
          paymentDates[c],
          accrualStartDates[c],
          accrualEndDates[c],
          AnnuityDefinitionBuilder.getDayCountFraction(getAccrualPeriodFrequency(), accrualCalendar, getDayCount(),
              getStartStub() != null ? getStartStub().getStubType() : StubType.NONE,
              getEndStub() != null ? getEndStub().getStubType() : StubType.NONE,
              accrualStartDates[c], accrualEndDates[c], c == 0, c == accrualEndDates.length - 1),
          (isPayer() ? -1 : 1) * getNotional().getAmount(accrualStartDates[c].toLocalDate()),
          _rate);
    }
    return coupons;
  }

  private CouponDefinition[] generateZeroCouponFlows(int exchangeNotionalCoupons, Calendar accrualCalendar, boolean noCompounding) {
    CouponDefinition[] coupons;

    if (Math.abs(_rate) < 1e-16) {
      //        coupons = new CouponDefinition[exchangeNotionalCoupons];
      //        if (isExchangeInitialNotional()) {
      //          coupons[0] = getExchangeInitialNotionalCoupon();
      //        } else if (isExchangeFinalNotional()) {
      //          coupons[coupons.length - 1] = getExchangeFinalNotionalCoupon();
      //        }
      coupons = new CouponDefinition[1];
      coupons[0] = getExchangeFinalNotionalCoupon();
    } else {
      coupons = new CouponDefinition[exchangeNotionalCoupons + 1];
      // TODO missing support for start and end stub types
      StubType stubType = null;
      if (getStartStub() != null) {
        stubType = getStartStub().getStubType();
      } else if (getEndStub() != null) {
        stubType = getEndStub().getStubType();
      }

      if (stubType == null) {
        stubType = StubType.NONE;
      }

      final ZonedDateTime adjustedEndDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
          getAccrualPeriodAdjustmentParameters().getCalendar(), getEndDate());
      ZonedDateTime paymentDate = getPaymentDates(new ZonedDateTime[] {adjustedEndDate })[0];

      if (noCompounding) {
        coupons[0] = new CouponFixedDefinition(
            getCurrency(),
            paymentDate,
            getStartDate(),
            getEndDate(),
            AnnuityDefinitionBuilder.getDayCountFraction(getAccrualPeriodFrequency(), accrualCalendar, getDayCount(),
                getStartStub() != null ? getStartStub().getStubType() : StubType.NONE,
                getEndStub() != null ? getEndStub().getStubType() : StubType.NONE,
                getStartDate(), getEndDate(), true, true),
            (isPayer() ? -1 : 1) * getNotional().getAmount(getStartDate().toLocalDate()),
            _rate);
      } else {
        ZonedDateTime[] accrualEndDates;
        if (getAccrualPeriodAdjustmentParameters() != null) {
          accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(
              getStartDate(),
              getEndDate(),
              Period.ofYears(1), // PLAT-6810
              stubType,
              getAccrualPeriodAdjustmentParameters().getBusinessDayConvention(),
              getAccrualPeriodAdjustmentParameters().getCalendar(),
              getRollDateAdjuster());
        } else {
          accrualEndDates = ScheduleCalculator.getUnadjustedDateSchedule(
              getStartDate(),
              getEndDate(),
              Period.ofYears(1), // PLAT-6810
              stubType);
        }
        ZonedDateTime[] accrualStartDates = ScheduleCalculator.getStartDates(getStartDate(), accrualEndDates);
        double[] paymentYearFractions = new double[accrualEndDates.length];
        for (int i = 0; i < accrualEndDates.length; i++) {
          paymentYearFractions[i] = AnnuityDefinitionBuilder.getDayCountFraction(Period.ofYears(1), // PLAT-6810
              accrualCalendar, getDayCount(), stubType, stubType,
              accrualStartDates[i], accrualEndDates[i], i == 0, i == accrualEndDates.length - 1);
        }

        coupons[0] = CouponFixedCompoundingDefinition.from(
            getCurrency(),
            paymentDate, // pmt
            getStartDate(), // acc start
            adjustedEndDate, // acc end
            AnnuityDefinitionBuilder.getDayCountFraction(Period.ofYears(1), getPaymentDateAdjustmentParameters().getCalendar(), getDayCount(), stubType, stubType,
                getStartDate(), adjustedEndDate, true, true), // pmt yf
            (isPayer() ? -1 : 1) * getNotional().getAmount(getStartDate().toLocalDate()),
            _rate,
            accrualStartDates,
            accrualEndDates,
            paymentYearFractions);
      }
    }
    return coupons;
  }
}
