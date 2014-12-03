/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class SwapCleanDiscountingCalculatorTest {
  private static final ZonedDateTime[] VALUATION_DATE_SET = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 1, 22),
      DateUtils.getUTCDate(2014, 4, 22) };
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster
      .getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator(
      "USD6MLIBOR3M", NYC);

  private static final double NOTIONAL = 100000000; //100 m
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10), DateUtils.getUTCDate(2013, 12, 12),
              DateUtils.getUTCDate(2014, 3, 10) },
          new double[] {0.0024185, 0.0024285, 0.0025175, });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M };

  /* Instrument description: Swap Fixed vs Libor3M already started */
  private static final ZonedDateTime TRADE_DATE_3M_S = DateUtils.getUTCDate(2013, 9, 10);
  private static final Period TENOR_SWAP_3M_S = Period.ofYears(7);
  private static final double FIXED_RATE_3M_S = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M_S = new GeneratorAttributeIR(TENOR_SWAP_3M_S);
  private static final SwapFixedIborDefinition SWAP_FIXED_3M_S_DEFINITION =
      USD6MLIBOR3M.generateInstrument(TRADE_DATE_3M_S, FIXED_RATE_3M_S, NOTIONAL, ATTRIBUTE_3M_S);

  private static final MulticurveProviderDiscount[] MULTICURVE_SET;
  static {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_OIS_PAIR =
      StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
    MulticurveProviderDiscount MULTICURVE_OIS = MULTICURVE_OIS_PAIR.getFirst();
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FF_PAIR =
      StandardDataSetsMulticurveUSD.getCurvesUSDOisFFL1L3L6();
    MulticurveProviderDiscount MULTICURVE_FFS = MULTICURVE_FF_PAIR.getFirst();
    MULTICURVE_SET = new MulticurveProviderDiscount[] {MULTICURVE_OIS, MULTICURVE_FFS };
  }

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final SwapCleanDiscountingCalculator CPRC = new SwapCleanDiscountingCalculator();

  private static final double TOL = 1.0e-12;

  /**
   * Example test
   */
  @Test
  public void E2ETest() {
    ZonedDateTime valuationDate = VALUATION_DATE_SET[0]; // 2014, 1, 22

    MulticurveProviderDiscount multicurve = MULTICURVE_SET[0]; // MULTICURVE_OIS
    double parRate = CPRC.parRate(SWAP_FIXED_3M_S_DEFINITION, USD6MLIBOR3M.getFixedLegDayCount(), USD6MLIBOR3M
        .getIborIndex().getDayCount(), USD6MLIBOR3M.getCalendar(), valuationDate, TS_USDLIBOR3M, multicurve);
    assertRelative("consistencyTest", 0.021442847215148938, parRate, TOL);

    multicurve = MULTICURVE_SET[1]; // MULTICURVE_FFS
    parRate = CPRC.parRate(SWAP_FIXED_3M_S_DEFINITION, USD6MLIBOR3M.getFixedLegDayCount(), USD6MLIBOR3M
        .getIborIndex().getDayCount(), USD6MLIBOR3M.getCalendar(), valuationDate, TS_USDLIBOR3M, multicurve);
    assertRelative("consistencyTest", 0.0208613981377012, parRate, TOL);
  }

  /**
   * Test consistency between accrued interest and par rate
   */
  @Test
  public void consistencyTest() {
    for (ZonedDateTime valuationDate : VALUATION_DATE_SET) {
      for (MulticurveProviderDiscount multicurve : MULTICURVE_SET) {

        /* Compute "clean" par rate manually */
        AnnuityCouponIborDefinition floatingLeg = (AnnuityCouponIborDefinition) SWAP_FIXED_3M_S_DEFINITION
            .getSecondLeg();
        IborIndex index = floatingLeg.getIborIndex();
        DayCount dayCountFloating = index.getDayCount();
        Calendar calendarFloating = SWAP_FIXED_3M_S_DEFINITION.getFirstLeg().getCalendar();
        CouponIborDefinition[] paymentsFloating = floatingLeg.getPayments();
        final List<CouponIborDefinition> listFloating = new ArrayList<>();
        for (final CouponIborDefinition payment : paymentsFloating) {
          if (!payment.getPaymentDate().isBefore(valuationDate)) {
            listFloating.add(payment);
          }
        }
        AnnuityCouponIborDefinition trimedFloatingLeg = new AnnuityCouponIborDefinition(
            listFloating.toArray(new CouponIborDefinition[listFloating.size()]), index, calendarFloating);
        Annuity<? extends Coupon> floatingLegDerivative = trimedFloatingLeg.toDerivative(valuationDate, TS_USDLIBOR3M);
        double accruedYearFractionFloating = dayCountFloating.getDayCountFraction(trimedFloatingLeg.getNthPayment(0)
            .getAccrualStartDate(), valuationDate, calendarFloating);
        double dirtyFloatingPV = floatingLegDerivative.accept(PVDC, multicurve).getAmount(
            floatingLegDerivative.getCurrency()) * Math.signum(floatingLegDerivative.getNthPayment(0).getNotional());
        CouponFixed firstCoupon = (CouponFixed) floatingLegDerivative.getNthPayment(0);
        double accruedInterestFloating = firstCoupon.getFixedRate() * accruedYearFractionFloating *
            Math.abs(firstCoupon.getNotional());
        double cleanFloatingPV = dirtyFloatingPV - accruedInterestFloating;
        AnnuityCouponFixedDefinition fixedLeg = (AnnuityCouponFixedDefinition) SWAP_FIXED_3M_S_DEFINITION.getFirstLeg();
        DayCount dayCountFixed = USD6MLIBOR3M.getFixedLegDayCount();
        Calendar calendarFixed = USD6MLIBOR3M.getCalendar();
        CouponFixedDefinition[] paymentsFixed = fixedLeg.getPayments();
        final List<CouponFixedDefinition> listFixed = new ArrayList<>();
        for (final CouponFixedDefinition payment : paymentsFixed) {
          if (!payment.getPaymentDate().isBefore(valuationDate)) {
            listFixed.add(payment);
          }
        }
        AnnuityCouponFixedDefinition trimedFixedLeg = new AnnuityCouponFixedDefinition(
            listFixed.toArray(new CouponFixedDefinition[listFixed.size()]), calendarFixed);
        double accruedYearFractionFixed = dayCountFixed.getDayCountFraction(trimedFixedLeg.getNthPayment(0)
            .getAccrualStartDate(), valuationDate, calendarFixed);
        AnnuityCouponFixed fixedLegDerivative = trimedFixedLeg.toDerivative(valuationDate);
        SwapFixedCoupon<?> fixedCouponSwap = new SwapFixedCoupon<>(fixedLegDerivative, floatingLegDerivative);
        double dirtyAnnuity = METHOD_SWAP.presentValueBasisPoint(fixedCouponSwap, multicurve);
        double accruedFixed = accruedYearFractionFixed * Math.abs(trimedFixedLeg.getNthPayment(0).getNotional());
        double cleanAnnuity = dirtyAnnuity - accruedFixed;
        double refParRate = cleanFloatingPV / cleanAnnuity;
        double parRate = CPRC.parRate(SWAP_FIXED_3M_S_DEFINITION, USD6MLIBOR3M.getFixedLegDayCount(), USD6MLIBOR3M
            .getIborIndex().getDayCount(), USD6MLIBOR3M.getCalendar(), valuationDate, TS_USDLIBOR3M, multicurve);
        assertRelative("consistencyTest", refParRate, parRate, TOL);

        /* Check the par rate produces zero clean PV */
        SwapFixedIborDefinition swapWithParRateDfn = USD6MLIBOR3M.generateInstrument(TRADE_DATE_3M_S,
            parRate, NOTIONAL, ATTRIBUTE_3M_S);
        Swap<? extends Payment, ? extends Payment> swapWithParRate = swapWithParRateDfn.toDerivative(valuationDate,
            TS_ARRAY_USDLIBOR3M);
        double accruedInterest = CPRC.accruedInterest(swapWithParRateDfn, USD6MLIBOR3M.getFixedLegDayCount(),
            USD6MLIBOR3M.getIborIndex().getDayCount(), USD6MLIBOR3M.getCalendar(), valuationDate, TS_USDLIBOR3M,
            multicurve).getAmount(Currency.USD);
        double dirtyPV = swapWithParRate.accept(PVDC, multicurve).getAmount(Currency.USD);
        assertRelative("consistencyTest", 0.0, dirtyPV - accruedInterest, Math.abs(dirtyPV) * TOL);
      }
    }
  }

  /**
   * first coupon is not yet paid, but second coupon period started
   */
  @Test
  public void paidAfterPeriodEndTest() {
    MulticurveProviderDiscount multicurve = MULTICURVE_SET[0];
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2014, 3, 17);
    ZonedDateTimeDoubleTimeSeries timeSeries =
        ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
            new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 12), DateUtils.getUTCDate(2013, 12, 12),
                DateUtils.getUTCDate(2014, 3, 13) }, new double[] {0.0024185, 0.0025285, 0.0024175, });
    ZonedDateTimeDoubleTimeSeries[] timeSeriesArray = new ZonedDateTimeDoubleTimeSeries[] {timeSeries };
    Currency usd = Currency.USD;
    /* Ibor leg */
    ZonedDateTime[] paymentDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 16),
        DateUtils.getUTCDate(2014, 3, 17), DateUtils.getUTCDate(2014, 6, 16), DateUtils.getUTCDate(2014, 9, 16) };
    ZonedDateTime[] accStartDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 16),
        DateUtils.getUTCDate(2013, 12, 16), DateUtils.getUTCDate(2014, 3, 16), DateUtils.getUTCDate(2014, 6, 16) };
    ZonedDateTime[] accEndDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 16),
        DateUtils.getUTCDate(2014, 3, 16), DateUtils.getUTCDate(2014, 6, 16), DateUtils.getUTCDate(2014, 9, 16) };
    ZonedDateTime[] FixingDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 12),
        DateUtils.getUTCDate(2013, 12, 12), DateUtils.getUTCDate(2014, 3, 13), DateUtils.getUTCDate(2014, 6, 12) };
    int nPaymentsIbor = paymentDatesIbor.length;
    IborIndex index = USD6MLIBOR3M.getIborIndex(); // USDLIBOR3M
    Calendar calendar = USD6MLIBOR3M.getCalendar();
    DayCount dcIbor = index.getDayCount();
    CouponIborDefinition[] ibor = new CouponIborDefinition[nPaymentsIbor];
    for (int i = 0; i < nPaymentsIbor; ++i) {
      double paymentYearFraction = dcIbor.getDayCountFraction(accStartDatesIbor[i], accEndDatesIbor[i], NYC);
      ibor[i] = new CouponIborDefinition(usd, paymentDatesIbor[i], accStartDatesIbor[i], accEndDatesIbor[i],
          paymentYearFraction, -NOTIONAL, FixingDatesIbor[i], index, NYC);
    }
    AnnuityCouponIborDefinition iborLeg = new AnnuityCouponIborDefinition(ibor, index, calendar);
    /* Fixed leg */
    ZonedDateTime[] paymentDatesFixed = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 17),
        DateUtils.getUTCDate(2014, 9, 16) };
    ZonedDateTime[] accStartDatesFixed = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 16),
        DateUtils.getUTCDate(2014, 3, 16) };
    ZonedDateTime[] accEndDatesFixed = new ZonedDateTime[] {DateUtils.getUTCDate(2014, 3, 16),
        DateUtils.getUTCDate(2014, 9, 16) };
    int nPaymentsFixed = paymentDatesFixed.length;
    CouponFixedDefinition[] fixed = new CouponFixedDefinition[nPaymentsFixed];
    DayCount dcFixed = USD6MLIBOR3M.getFixedLegDayCount();
    for (int i = 0; i < nPaymentsFixed; ++i) {
      double paymentYearFraction = dcFixed.getDayCountFraction(accStartDatesFixed[i], accEndDatesFixed[i], calendar);
      fixed[i] = new CouponFixedDefinition(usd, paymentDatesFixed[i], accStartDatesFixed[i], accEndDatesFixed[i],
          paymentYearFraction, NOTIONAL, FIXED_RATE_3M_S);
    }
    AnnuityCouponFixedDefinition fixedLeg = new AnnuityCouponFixedDefinition(fixed, calendar);
    /* Swap */
    SwapFixedIborDefinition swapDefinition = new SwapFixedIborDefinition(fixedLeg, iborLeg);
    SwapFixedCoupon<Coupon> fixedCouponSwap = swapDefinition.toDerivative(valuationDate, timeSeriesArray);

    /* Result */
    double parRate = CPRC.parRate(swapDefinition, dcFixed, dcIbor, calendar, valuationDate, timeSeries,
        multicurve);
    double accruedInterest = CPRC.accruedInterest(swapDefinition, dcFixed, dcIbor, calendar, valuationDate,
        timeSeries, multicurve).getAmount(usd);

    /* Compute accrued amounts manually */
    int pIbor = 1;
    double accYFIbor = dcIbor.getDayCountFraction(accStartDatesIbor[pIbor], valuationDate, calendar);
    double indexRate = timeSeries.getValue(ibor[pIbor].getFixingDate());
    double notionalIbor = ibor[pIbor].getNotional();
    double accIbor = accYFIbor * indexRate * notionalIbor;
    ++pIbor;
    accYFIbor = dcIbor.getDayCountFraction(accStartDatesIbor[pIbor], valuationDate, calendar);
    indexRate = timeSeries.getValue(ibor[pIbor].getFixingDate());
    notionalIbor = ibor[pIbor].getNotional();
    accIbor += accYFIbor * indexRate * notionalIbor;
    int pFixed = 0;
    double accYFFixed = dcFixed.getDayCountFraction(accStartDatesFixed[pFixed], valuationDate, calendar);
    double notionalFixed = fixed[pFixed].getNotional();
    double accFixed = accYFFixed * notionalFixed;
    ++pFixed;
    accYFFixed = dcFixed.getDayCountFraction(accStartDatesFixed[pFixed], valuationDate, calendar);
    notionalFixed = fixed[pFixed].getNotional();
    accFixed += accYFFixed * notionalFixed;
    double refAccInterest = accIbor + FIXED_RATE_3M_S * accFixed;
    assertRelative("paidAfterPeriodEndTest", refAccInterest, accruedInterest, TOL);
    double pvIborLeg = iborLeg.toDerivative(valuationDate, timeSeries).accept(PVDC, multicurve).getAmount(usd);
    double annuity = METHOD_SWAP.presentValueBasisPoint(fixedCouponSwap, multicurve);
    double refParRate = Math.abs((pvIborLeg - accIbor) / (annuity - accFixed));
    assertRelative("paidAfterPeriodEndTest", refParRate, parRate, TOL);

  }

  /**
   * No accrued for a newly issued swap
   */
  @Test
  public void notAccruedTest() {
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2013, 8, 10); // before start date
    SwapFixedCoupon<Coupon> fixedCouponSwap = SWAP_FIXED_3M_S_DEFINITION.toDerivative(valuationDate,
        TS_ARRAY_USDLIBOR3M);
    MulticurveProviderDiscount multicurve = MULTICURVE_SET[1];
    Calendar calendar = USD6MLIBOR3M.getCalendar();
    DayCount dcIbor = USD6MLIBOR3M.getIborIndex().getDayCount();
    DayCount dcFixed = USD6MLIBOR3M.getFixedLegDayCount();

    double parRate = CPRC.parRate(SWAP_FIXED_3M_S_DEFINITION, dcFixed, dcIbor, calendar, valuationDate, TS_USDLIBOR3M,
        multicurve);
    double accruedInterest = CPRC.accruedInterest(SWAP_FIXED_3M_S_DEFINITION, dcFixed, dcIbor, calendar, valuationDate,
        TS_USDLIBOR3M, multicurve).getAmount(Currency.USD);
    double refParRate = fixedCouponSwap.accept(PRDC, multicurve);
    assertRelative("", refParRate, parRate, TOL);
    assertRelative("", 0.0, accruedInterest, TOL);
  }

  /**
   * With 30/360 accrued days are 0 between 30th and 31st
   */
  @Test
  public void afterStartNotAccruedTest() {
    MulticurveProviderDiscount multicurve = MULTICURVE_SET[0];
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2013, 12, 31);
    ZonedDateTimeDoubleTimeSeries timeSeries =
        ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
            new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 26), DateUtils.getUTCDate(2013, 9, 26),
                DateUtils.getUTCDate(2013, 12, 26) }, new double[] {0.0024185, 0.0025285, 0.0024175, });
    ZonedDateTimeDoubleTimeSeries[] timeSeriesArray = new ZonedDateTimeDoubleTimeSeries[] {timeSeries };
    Currency usd = Currency.USD;
    /* Ibor leg */
    ZonedDateTime[] paymentDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 30),
        DateUtils.getUTCDate(2013, 12, 30), DateUtils.getUTCDate(2014, 3, 31), DateUtils.getUTCDate(2014, 6, 30) };
    ZonedDateTime[] accStartDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 30),
        DateUtils.getUTCDate(2013, 9, 30), DateUtils.getUTCDate(2013, 12, 30), DateUtils.getUTCDate(2014, 3, 30) };
    ZonedDateTime[] accEndDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 30),
        DateUtils.getUTCDate(2013, 12, 30), DateUtils.getUTCDate(2014, 3, 30), DateUtils.getUTCDate(2014, 6, 30) };
    ZonedDateTime[] FixingDatesIbor = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 26),
        DateUtils.getUTCDate(2013, 9, 26), DateUtils.getUTCDate(2013, 12, 26), DateUtils.getUTCDate(2014, 3, 26) };
    int nPaymentsIbor = paymentDatesIbor.length;
    IborIndex index = USD6MLIBOR3M.getIborIndex(); // USDLIBOR3M
    Calendar calendar = USD6MLIBOR3M.getCalendar(); // modified s.t.30U/360 is used for both legs
    DayCount dcIbor = USD6MLIBOR3M.getFixedLegDayCount();
    CouponIborDefinition[] ibor = new CouponIborDefinition[nPaymentsIbor];
    for (int i = 0; i < nPaymentsIbor; ++i) {
      double paymentYearFraction = dcIbor.getDayCountFraction(accStartDatesIbor[i], accEndDatesIbor[i], NYC);
      ibor[i] = new CouponIborDefinition(usd, paymentDatesIbor[i], accStartDatesIbor[i], accEndDatesIbor[i],
          paymentYearFraction, -NOTIONAL, FixingDatesIbor[i], index, NYC);
    }
    AnnuityCouponIborDefinition iborLeg = new AnnuityCouponIborDefinition(ibor, index, calendar);
    /* Fixed leg */
    ZonedDateTime[] paymentDatesFixed = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 30),
        DateUtils.getUTCDate(2014, 6, 30) };
    ZonedDateTime[] accStartDatesFixed = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 30),
        DateUtils.getUTCDate(2013, 12, 30) };
    ZonedDateTime[] accEndDatesFixed = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 30),
        DateUtils.getUTCDate(2014, 6, 30) };
    int nPaymentsFixed = paymentDatesFixed.length;
    CouponFixedDefinition[] fixed = new CouponFixedDefinition[nPaymentsFixed];
    DayCount dcFixed = USD6MLIBOR3M.getFixedLegDayCount();
    for (int i = 0; i < nPaymentsFixed; ++i) {
      double paymentYearFraction = dcFixed.getDayCountFraction(accStartDatesFixed[i], accEndDatesFixed[i], calendar);
      fixed[i] = new CouponFixedDefinition(usd, paymentDatesFixed[i], accStartDatesFixed[i], accEndDatesFixed[i],
          paymentYearFraction, NOTIONAL, FIXED_RATE_3M_S);
    }
    AnnuityCouponFixedDefinition fixedLeg = new AnnuityCouponFixedDefinition(fixed, calendar);
    /* Swap */
    SwapFixedIborDefinition swapDefinition = new SwapFixedIborDefinition(fixedLeg, iborLeg);
    SwapFixedCoupon<Coupon> fixedCouponSwap = swapDefinition.toDerivative(valuationDate, timeSeriesArray);

    double parRate = CPRC.parRate(swapDefinition, dcFixed, dcIbor, calendar, valuationDate, timeSeries,
        multicurve);
    double accruedInterest = CPRC.accruedInterest(swapDefinition, dcFixed, dcIbor, calendar, valuationDate,
        timeSeries, multicurve).getAmount(usd);
    double refParRate = fixedCouponSwap.accept(PRDC, multicurve);
    assertRelative("", refParRate, parRate, TOL);
    assertRelative("", 0.0, accruedInterest, TOL);
  }

  private void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
