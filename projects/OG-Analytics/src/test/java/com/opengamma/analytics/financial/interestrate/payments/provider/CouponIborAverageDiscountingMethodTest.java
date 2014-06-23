package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborAverageDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final IborIndex EURIBOR6M = IBOR_INDEXES[1];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final double NOTIONAL = 1000000; //1m
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 5, 19);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 11, 22);

  private static final ZonedDateTime ACCRUAL_START_DATE_1 = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE_1 = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR_1 = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE_1, ACCRUAL_END_DATE_1);
  private static final CouponIborDefinition CPN_IBOR_DEFINITION_1 = CouponIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE_1, ACCRUAL_END_DATE_1, ACCRUAL_FACTOR_1, NOTIONAL, FIXING_DATE,
      EURIBOR3M, CALENDAR);

  private static final ZonedDateTime ACCRUAL_START_DATE_2 = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE_2 = DateUtils.getUTCDate(2011, 11, 22);
  private static final double ACCRUAL_FACTOR_2 = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE_2, ACCRUAL_END_DATE_2);
  private static final CouponIborDefinition CPN_IBOR_DEFINITION_2 = CouponIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE_2, ACCRUAL_END_DATE_2, ACCRUAL_FACTOR_2, NOTIONAL, FIXING_DATE,
      EURIBOR6M, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponIbor CPN_IBOR_1 = (CouponIbor) CPN_IBOR_DEFINITION_1.toDerivative(REFERENCE_DATE);
  private static final CouponIbor CPN_IBOR_2 = (CouponIbor) CPN_IBOR_DEFINITION_2.toDerivative(REFERENCE_DATE);

  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 5.0E-7;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double WEIGHT_1 = 17;
  private static final double WEIGHT_2 = -0.06;
  private static final CouponIborAverageIndexDefinition CPN_IBOR__AVERAGE_DEFINITION = CouponIborAverageIndexDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE_2, ACCRUAL_END_DATE_2, ACCRUAL_FACTOR_2, NOTIONAL,
      FIXING_DATE, EURIBOR3M, EURIBOR6M, WEIGHT_1, WEIGHT_2, CALENDAR, CALENDAR);
  private static final CouponIborAverage CPN_IBOR__AVERAGE = (CouponIborAverage) CPN_IBOR__AVERAGE_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIborAverageDiscountingMethod METHOD_CPN_IBOR__AVERAGE = CouponIborAverageDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  public void presentValueMarketDiscount() {
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR__AVERAGE.presentValue(CPN_IBOR__AVERAGE, MULTICURVES);
    final double forward1 = MULTICURVES.getSimplyCompoundForwardRate(CPN_IBOR__AVERAGE.getIndex1(), CPN_IBOR__AVERAGE.getFixingPeriodStartTime1(), CPN_IBOR__AVERAGE.getFixingPeriodEndTime1(),
        CPN_IBOR__AVERAGE.getFixingAccrualFactor1());
    final double forward2 = MULTICURVES.getSimplyCompoundForwardRate(CPN_IBOR__AVERAGE.getIndex2(), CPN_IBOR__AVERAGE.getFixingPeriodStartTime2(), CPN_IBOR__AVERAGE.getFixingPeriodEndTime2(),
        CPN_IBOR__AVERAGE.getFixingAccrualFactor2());

    final double df = MULTICURVES.getDiscountFactor(CPN_IBOR__AVERAGE.getCurrency(), CPN_IBOR_1.getPaymentTime());
    final double pvExpected = CPN_IBOR__AVERAGE.getNotional() * CPN_IBOR__AVERAGE.getPaymentYearFraction() * (WEIGHT_1 * forward1 + WEIGHT_2 * forward2) * df;
    assertEquals("CouponIborDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR__AVERAGE.presentValue(CPN_IBOR__AVERAGE, MULTICURVES);
    final MultipleCurrencyAmount pvComputed1 = METHOD_CPN_IBOR.presentValue(CPN_IBOR_1, MULTICURVES);
    final MultipleCurrencyAmount pvComputed2 = METHOD_CPN_IBOR.presentValue(CPN_IBOR_2, MULTICURVES);
    final double pvExpected = CPN_IBOR__AVERAGE.getPaymentYearFraction() *
        (WEIGHT_1 * pvComputed1.getAmount(EUR) / ACCRUAL_FACTOR_1 + WEIGHT_2 * pvComputed2.getAmount(EUR) / ACCRUAL_FACTOR_2);

    assertEquals("CouponIborDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsAnnuityExact = PSC.calculateSensitivity(CPN_IBOR__AVERAGE, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsAnnuityFD = PSC_DSC_FD.calculateSensitivity(CPN_IBOR__AVERAGE, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageDiscountingMethod: presentValueCurveSensitivity ", pvpsAnnuityExact, pvpsAnnuityFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CPN_IBOR__AVERAGE.presentValueCurveSensitivity(CPN_IBOR__AVERAGE, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_IBOR__AVERAGE.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }
}
