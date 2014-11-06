/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.VariableNotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AbstractAnnuityDefinitionBuilder.CouponStub;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegFixed;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapInstrumentsDataSet;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
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

    /*
     * Construct annuity by the builder without dates
     */
    provider = new VariableNotionalProvider(notionals);
    AnnuityDefinition<?> fixedDefinition1 = new FixedAnnuityDefinitionBuilder().
        payer(PAYER_1).currency(USD).startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).
        dayCount(USD6MLIBOR3M.getFixedLegDayCount()).accrualPeriodFrequency(period).
        rate(FIXED_RATE_1).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).notional(provider).build();
    assertEquals(fixedDefinitionBare, fixedDefinition1);
  }

  /**
   * Test consistency with constant notional
   */
  @Test
  public void variableNotionalConsistencyTest() {
    Period period = Period.ofMonths(6);
    int nDates = 22;
    double[] notionals = new double[nDates];
    Arrays.fill(notionals, NOTIONAL_1);
    NotionalProvider provider = new VariableNotionalProvider(notionals);
    AnnuityDefinition<?> fixedDefinition = new FixedAnnuityDefinitionBuilder().payer(PAYER_1).currency(USD)
        .startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).dayCount(USD6MLIBOR3M.getFixedLegDayCount())
        .accrualPeriodFrequency(period).rate(FIXED_RATE_1).accrualPeriodParameters(ADJUSTED_DATE_LIBOR)
        .exchangeInitialNotional(true).exchangeFinalNotional(true).notional(provider)
        .startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).build();

    AnnuityDefinition<?> fixedDefinitionConst = new FixedAnnuityDefinitionBuilder().
        payer(PAYER_1).currency(USD).startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1)
        .dayCount(USD6MLIBOR3M.getFixedLegDayCount()).accrualPeriodFrequency(period).rate(FIXED_RATE_1)
        .accrualPeriodParameters(ADJUSTED_DATE_LIBOR).exchangeInitialNotional(true).exchangeFinalNotional(true)
        .startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR)
        .notional(NOTIONAL_PROV_1).build();

    assertEquals(fixedDefinitionConst, fixedDefinition);
  }

  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR6M = MASTER_IBOR.getIndex("USDLIBOR6M");
  private static final AdjustedDateParameters ADJUSTED_DATE_USDLIBOR =
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final Period P3M = Period.ofMonths(3);
  private static final Period P6M = Period.ofMonths(6);
  private static final Period P1Y = Period.ofYears(1);
  /* Long start */
  private static final LocalDate START_DATE_STUB1 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB1 = LocalDate.of(2015, 9, 10);
  private static final CouponStub CPN_STUB1 = new CouponStub(StubType.LONG_START);
  private static final AnnuityDefinition<CouponFixedDefinition> LEG_STUB1 =
      (AnnuityDefinition<CouponFixedDefinition>) new FixedAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).
          accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).dayCount(USDLIBOR3M.getDayCount()).
          currency(USD).startStub(CPN_STUB1).build();
  /* Short start */
  private static final LocalDate START_DATE_STUB2 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB2 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_STUB2 = new CouponStub(StubType.SHORT_START);
  private static final AnnuityDefinition<CouponFixedDefinition> LEG_STUB2 = (AnnuityDefinition<CouponFixedDefinition>)
      new FixedAnnuityDefinitionBuilder().payer(true).notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB2)
          .endDate(END_DATE_STUB2).accrualPeriodFrequency(P6M)
          .rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
          .dayCount(USDLIBOR6M.getDayCount()).currency(USD).startStub(CPN_STUB2).build();
  /* Short end */
  private static final LocalDate START_DATE_STUB3 = LocalDate.of(2014, 3, 14);
  private static final LocalDate END_DATE_STUB3 = LocalDate.of(2015, 4, 22);
  private static final CouponStub CPN_STUB3 = new CouponStub(StubType.SHORT_END);
  private static final AnnuityDefinition<CouponFixedDefinition> LEG_STUB3 = (AnnuityDefinition<CouponFixedDefinition>)
      new FixedAnnuityDefinitionBuilder().payer(true).notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB3)
          .endDate(END_DATE_STUB3).accrualPeriodFrequency(P6M)
          .rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
          .dayCount(USDLIBOR6M.getDayCount()).currency(USD).endStub(CPN_STUB3).build();
  /* Long end */
  private static final LocalDate START_DATE_STUB4 = LocalDate.of(2013, 9, 12);
  private static final LocalDate END_DATE_STUB4 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_STUB4 = new CouponStub(StubType.LONG_END);
  private static final AnnuityDefinition<CouponFixedDefinition> LEG_STUB4 = (AnnuityDefinition<CouponFixedDefinition>)
      new FixedAnnuityDefinitionBuilder().payer(true).notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB4)
          .endDate(END_DATE_STUB4).accrualPeriodFrequency(P6M)
          .rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
          .dayCount(USDLIBOR6M.getDayCount()).currency(USD).endStub(CPN_STUB4).build();

  /**
   * short/long stub type CouponFixedDefinition
   */
  @Test
  public void stubCouponFixedTest() {
    testStub("FixedAnnuityDefinitionBuilder - Stub - long start", LEG_STUB1, true, 5,
        START_DATE_STUB1, END_DATE_STUB1.minus(P1Y));
    testStub("FixedAnnuityDefinitionBuilder - Stub - short start", LEG_STUB2, true, 3,
        START_DATE_STUB2, END_DATE_STUB2.minus(P1Y));
    testStub("FixedAnnuityDefinitionBuilder - Stub - short end", LEG_STUB3, false, 3,
        ADJUSTED_DATE_USDLIBOR.getBusinessDayConvention().adjustDate(ADJUSTED_DATE_USDLIBOR.getCalendar(),
            START_DATE_STUB3.plus(P1Y)), END_DATE_STUB3);
    testStub("FixedAnnuityDefinitionBuilder - Stub - long end", LEG_STUB4, false, 3,
        START_DATE_STUB4.plus(P1Y), END_DATE_STUB4);
  }

  private void testStub(String message, AnnuityDefinition<CouponFixedDefinition> targetAnnuity, boolean startStub,
      int expectedLength, LocalDate expectedStartDate, LocalDate expectedEndDate) {
    assertEquals(message, expectedLength, targetAnnuity.getNumberOfPayments());
    int refPosition = startStub ? 0 : expectedLength - 1;
    CouponFixedDefinition cpnL = targetAnnuity.getNthPayment(refPosition);
    assertEquals(message, cpnL.getAccrualStartDate().toLocalDate(), expectedStartDate);
    assertEquals(message, cpnL.getAccrualEndDate().toLocalDate(), expectedEndDate);
  }

  /**
   * An example showing relation to GeneratorLeg
   * See {@link SwapInstrumentsDataSet}. 
   */
  @Test
  public void ConsistencyGeneratorTest() {
    MulticurveProviderDiscount mCurvesOIS = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6().getFirst();
    PresentValueDiscountingCalculator pvCalculator = PresentValueDiscountingCalculator.getInstance();
    DayCount dc = DayCounts.THIRTY_U_360;
    StubType stubType = StubType.SHORT_START;
    BusinessDayConvention bbc = USDLIBOR3M.getBusinessDayConvention();
    int offset = 2;
    ZonedDateTime valDate = DateUtils.getUTCDate(2014, 1, 22);
    ZonedDateTime tradeDate = DateUtils.getUTCDate(2014, 9, 10);
    Period period = Period.ofMonths(22);
    double fixedRate = 0.01;
    ZonedDateTimeDoubleTimeSeries tsLibor = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
        new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10), DateUtils.getUTCDate(2013, 12, 12) },
        new double[] {0.0024185, 0.0024285 });
    ZonedDateTimeDoubleTimeSeries[] ts = new ZonedDateTimeDoubleTimeSeries[] {tsLibor, tsLibor };

    GeneratorLegFixed fixedGnr = new GeneratorLegFixed("LEG_USDFixed1Y", USD, offset,
        P6M, dc, bbc, 0, true, stubType, false, NYC);
    GeneratorLegIbor liborGnr = GeneratorLegIborMaster.getInstance().getGenerator("USDLIBOR3M", NYC);
    GeneratorAttributeIR att = new GeneratorAttributeIR(period);
    PaymentDefinition[] payments = fixedGnr.generateInstrument(tradeDate, fixedRate, NOTIONAL_1, att)
        .getPayments();
    CouponFixedDefinition[] cpnFixedDefinition = new CouponFixedDefinition[payments.length];
    for (int n = 0; n < payments.length; n++) {
      cpnFixedDefinition[n] = (CouponFixedDefinition) payments[n];
    }
    AnnuityCouponFixedDefinition fixedLegGnr = new AnnuityCouponFixedDefinition(cpnFixedDefinition, NYC);
    AnnuityDefinition<? extends CouponDefinition> liborLegGnr = (AnnuityDefinition<? extends CouponDefinition>)
        liborGnr.generateInstrument(tradeDate, 0.0, -NOTIONAL_1, att);
    SwapCouponFixedCouponDefinition swapDfnGnr = new SwapCouponFixedCouponDefinition(fixedLegGnr, liborLegGnr);
    Swap<? extends Payment, ? extends Payment> swapGnr = swapDfnGnr.toDerivative(valDate, ts);

    AdjustedDateParameters adjDatePram = new AdjustedDateParameters(NYC, USDLIBOR3M.getBusinessDayConvention());
    OffsetAdjustedDateParameters offsetAdjParam = new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, bbc);
    CouponStub stub = new CouponStub(stubType);
    // start date adjustment is absent in the annuity builder
    LocalDate spot = ScheduleCalculator.getAdjustedDate(tradeDate, offset, NYC).toLocalDate();

    AnnuityDefinition<?> liborLegBld = new FloatingAnnuityDefinitionBuilder()
        .notional(NOTIONAL_PROV_1).payer(true).startDate(spot).endDate(spot.plus(period)).index(USDLIBOR3M)
        .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(adjDatePram).accrualPeriodParameters(adjDatePram).currency(USD).spread(0.0)
        .dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(offsetAdjParam).startStub(stub).build();
    AnnuityDefinition<?> fixedLegBld = new FixedAnnuityDefinitionBuilder()
        .payer(false).notional(NOTIONAL_PROV_1).startDate(spot).endDate(spot.plusMonths(22))
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .accrualPeriodParameters(adjDatePram).dayCount(dc).currency(USD).startStub(stub).rate(fixedRate).build();
    SwapDefinition swapDfnBld = new SwapDefinition(fixedLegBld, liborLegBld);
    Swap<? extends Payment, ? extends Payment> swapBld = swapDfnBld.toDerivative(valDate, ts);
    double ref = swapGnr.accept(pvCalculator, mCurvesOIS).getAmount(USD);
    assertEquals(ref, swapBld.accept(pvCalculator, mCurvesOIS).getAmount(USD), Math.abs(ref) * 1.0e-10);
  }
}
