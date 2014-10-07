/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.VariableNotionalProvider;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Test the builder of fixed annuities.
 */
public class FixedAnnuityDefinitionBuilderTest {

  /** USD conventions */
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  /** Leg details */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2014, 7, 18);
  private static final int TENOR_YEAR_1 = 10;
  private static final LocalDate MATURITY_DATE_1 = EFFECTIVE_DATE_1.plus(Period.ofYears(TENOR_YEAR_1));
  private static final double FIXED_RATE_1 = 0.02655;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };
  /** Fixed legs */
  private static final AnnuityDefinition<?> FIXED_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_1).
      currency(USD).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).
      dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).
      rate(FIXED_RATE_1).
      accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      build();

  @Test
  public void leg1() {
    int nbFixedCpn = TENOR_YEAR_1 * 2;
    assertEquals("FixedAnnuityDefinitionBuilderTest: vanilla", FIXED_LEG_1_DEFINITION.getNumberOfPayments(), nbFixedCpn);
    assertEquals("FixedAnnuityDefinitionBuilderTest: vanilla", FIXED_LEG_1_DEFINITION.getNthPayment(nbFixedCpn - 1).getPaymentDate().toLocalDate(), MATURITY_DATE_1);
    ZonedDateTime effectiveDateTime = DateUtils.getUTCDate(EFFECTIVE_DATE_1.getYear(), EFFECTIVE_DATE_1.getMonthValue(), EFFECTIVE_DATE_1.getDayOfMonth());
    for (int loopcpn = 0; loopcpn < nbFixedCpn; loopcpn++) {
      ZonedDateTime expectedPaymentDate = ScheduleCalculator.getAdjustedDate(effectiveDateTime, USD6MLIBOR3M.getFixedLegPeriod().multipliedBy(loopcpn + 1), USDLIBOR3M, NYC);
      assertEquals("FixedAnnuityDefinitionBuilderTest: vanilla", FIXED_LEG_1_DEFINITION.getNthPayment(loopcpn).getPaymentDate().toLocalDate(),
          expectedPaymentDate.toLocalDate());
    }
  }

  /**
   * Variable notional annuity
   */
  @Test
  public void variableNotionalTest() {
    Period period = Period.ofMonths(6);

    /*
     * Construct annuity by the builder
     */
    FixedAnnuityDefinitionBuilder builder = new FixedAnnuityDefinitionBuilder().
        payer(PAYER_1).currency(USD).startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).
        dayCount(USD6MLIBOR3M.getFixedLegDayCount()).accrualPeriodFrequency(period).
        rate(FIXED_RATE_1).accrualPeriodParameters(ADJUSTED_DATE_LIBOR);
    ZonedDateTime[] accrualEndDates = builder.getAccrualEndDates();
    ZonedDateTime startDate = builder.getStartDate();
    ZonedDateTime[] accrualStartDates = ScheduleCalculator.getStartDates(startDate, accrualEndDates);
    int nDates = accrualStartDates.length; // assumes NO initial/final notional exchange
    LocalDate[] dates = new LocalDate[nDates];
    double[] notionals = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      dates[i] = accrualStartDates[i].toLocalDate(); // notional is specified by accrual start date in the builder
      notionals[i] = NOTIONAL_1 * (1.0 - 0.02 * i);
    }
    NotionalProvider provider = new VariableNotionalProvider(dates, notionals);
    AnnuityDefinition<?> fixedDefinition = builder.notional(provider).build();

    /*
     * Construct annuity from individual coupon payments
     */
    ZonedDateTime startDateBare = EFFECTIVE_DATE_1.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault());
    ZonedDateTime[] accrualEndDatesBare = ScheduleCalculator.getAdjustedDateSchedule(startDateBare,
        MATURITY_DATE_1.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()), period, StubType.NONE,
        ADJUSTED_DATE_LIBOR.getBusinessDayConvention(), ADJUSTED_DATE_LIBOR.getCalendar(), null);
    ZonedDateTime[] accrualStartDatesBare = ScheduleCalculator.getStartDates(startDateBare, accrualEndDatesBare);
    int nCoupons = accrualEndDatesBare.length;
    CouponDefinition[] coupons = new CouponFixedDefinition[nCoupons];
    for (int i = 0; i < nCoupons; ++i) {
      double yearFraction = AnnuityDefinitionBuilder.getDayCountFraction(period, ADJUSTED_DATE_LIBOR.getCalendar(),
          USD6MLIBOR3M.getFixedLegDayCount(), StubType.NONE, StubType.NONE,
          accrualStartDatesBare[i], accrualEndDatesBare[i], i == 0, i == accrualEndDates.length - 1);
      coupons[i] = new CouponFixedDefinition(USD, accrualEndDatesBare[i], accrualStartDatesBare[i],
          accrualEndDatesBare[i], yearFraction, notionals[i], FIXED_RATE_1);
    }
    AnnuityDefinition<?> fixedDefinitionBare = new AnnuityDefinition<>(coupons, ADJUSTED_DATE_LIBOR.getCalendar());

    assertEquals(fixedDefinitionBare, fixedDefinition);
  }
}
