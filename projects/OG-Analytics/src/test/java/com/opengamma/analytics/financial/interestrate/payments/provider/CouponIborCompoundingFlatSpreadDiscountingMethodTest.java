/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Ibor compounded coupon in the discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborCompoundingFlatSpreadDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveCad();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveCad();
  private static final IborIndex CADCDOR3M = IBOR_INDEXES[0];
  private static final Currency CAD = CADCDOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getCADCalendar();

  private static final Period M6 = Period.ofMonths(6);
  private static final double NOTIONAL = 123000000;
  private static final double SPREAD = 0.0010;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final ZonedDateTime END_DATE = START_DATE.plus(M6);
  private static final CouponIborCompoundingFlatSpreadDefinition CPN_DEFINITION = CouponIborCompoundingFlatSpreadDefinition.from(NOTIONAL, START_DATE, END_DATE, CADCDOR3M, SPREAD,
      StubType.SHORT_START, CADCDOR3M.getBusinessDayConvention(), CADCDOR3M.isEndOfMonth(), CALENDAR);

  private static final CouponIborCompoundingFlatSpreadDiscountingMethod METHOD_COMPOUNDED = CouponIborCompoundingFlatSpreadDiscountingMethod.getInstance();

  private static final ZonedDateTime REFERENCE_DATE_BEFORE = DateUtils.getUTCDate(2012, 8, 7);
  private static final CouponIborCompoundingFlatSpread CPN_BEFORE = CPN_DEFINITION.toDerivative(REFERENCE_DATE_BEFORE); // Reference date before the coupon start date.

  private static final double[] FIXING_RATES = new double[] {0.0010, 0.0011, 0.0012 };
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 23), DateUtils.getUTCDate(2012, 8, 24),
          DateUtils.getUTCDate(2012, 9, 20) }, FIXING_RATES);
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 8, 28);
  private static final CouponIborCompoundingFlatSpread CPN_1 = (CouponIborCompoundingFlatSpread) CPN_DEFINITION.toDerivative(REFERENCE_DATE_1, FIXING_TS);

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSDC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();

  private static final double SHIFT = 1.0E-6;

  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PS_PV_C = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  public void presentValueBeforeFirstFixing() {
    final MultipleCurrencyAmount pvComputed = METHOD_COMPOUNDED.presentValue(CPN_BEFORE,
        MULTICURVES);
    final int nbSub = CPN_BEFORE.getFixingTimes().length;
    double[] forward = new double[nbSub];
    for (int loopsub = 0; loopsub < nbSub; loopsub++) {
      forward[loopsub] = MULTICURVES.getSimplyCompoundForwardRate(CADCDOR3M, CPN_BEFORE.getFixingPeriodStartTimes()[loopsub],
          CPN_BEFORE.getFixingPeriodEndTimes()[loopsub], CPN_BEFORE.getFixingPeriodAccrualFactors()[loopsub]);
    }
    final double cpa = cpa(CPN_BEFORE.getCompoundingPeriodAmountAccumulated(), forward, CPN_BEFORE.getSubperiodsAccrualFactors(), SPREAD);
    final double dfPayment = MULTICURVES.getDiscountFactor(CAD, CPN_BEFORE.getPaymentTime());
    final double pvExpected = NOTIONAL * cpa * dfPayment;
    assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value", pvExpected, pvComputed.getAmount(CAD), TOLERANCE_PV);
  }

  public double cpa(double startValue, double[] rates, double[] accrualFactors, double spread) {
    double cpa = startValue;
    for (int loopsub = 0; loopsub < rates.length; loopsub++) {
      cpa += cpa * rates[loopsub] * accrualFactors[loopsub]; // Additional Compounding Period Amount
      cpa += (rates[loopsub] + spread) * accrualFactors[loopsub]; // Basic Compounding Period Amount
    }
    return cpa;
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_COMPOUNDED.presentValue(CPN_BEFORE, MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CPN_BEFORE.accept(PVDC, MULTICURVES);
    assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value", pvMethod.getAmount(CAD), pvCalculator.getAmount(CADCDOR3M.getCurrency()), TOLERANCE_PV);

  }

  @Test
  public void presentValueAfter1Fixing() {
    final MultipleCurrencyAmount pvComputed = METHOD_COMPOUNDED.presentValue(CPN_1, MULTICURVES);
    final int nbSub = CPN_1.getFixingTimes().length;
    double[] forward = new double[nbSub];
    for (int loopsub = 0; loopsub < nbSub; loopsub++) {
      forward[loopsub] = MULTICURVES.getSimplyCompoundForwardRate(CADCDOR3M, CPN_1.getFixingPeriodStartTimes()[loopsub],
          CPN_1.getFixingPeriodEndTimes()[loopsub], CPN_1.getFixingPeriodAccrualFactors()[loopsub]);
    }
    final double cpa = cpa(CPN_1.getCompoundingPeriodAmountAccumulated() / NOTIONAL, forward, CPN_1.getSubperiodsAccrualFactors(), SPREAD);
    final double dfPayment = MULTICURVES.getDiscountFactor(CAD, CPN_1.getPaymentTime());
    final double pvExpected = NOTIONAL * cpa * dfPayment;
    assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value", pvExpected, pvComputed.getAmount(CAD), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(CPN_BEFORE, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(CPN_BEFORE, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_COMPOUNDED.presentValueCurveSensitivity(CPN_BEFORE, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_BEFORE.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the sensitivity of the present value to the change of the market quote (i.e. the spread).
   */
  public void presentValueMarketQuoteSensitivity() {
    final CouponIborCompoundingFlatSpreadDefinition cpnShiftedPDefinition = CouponIborCompoundingFlatSpreadDefinition.from(NOTIONAL, START_DATE, END_DATE, CADCDOR3M, SPREAD + SHIFT,
        StubType.SHORT_START, CADCDOR3M.getBusinessDayConvention(), CADCDOR3M.isEndOfMonth(), CALENDAR);
    final CouponIborCompoundingFlatSpreadDefinition cpnShiftedMDefinition = CouponIborCompoundingFlatSpreadDefinition.from(NOTIONAL, START_DATE, END_DATE, CADCDOR3M, SPREAD - SHIFT,
        StubType.SHORT_START, CADCDOR3M.getBusinessDayConvention(), CADCDOR3M.isEndOfMonth(), CALENDAR);
    final CouponIborCompoundingFlatSpread cpnShiftedP = cpnShiftedPDefinition.toDerivative(REFERENCE_DATE_BEFORE);
    final CouponIborCompoundingFlatSpread cpnShiftedM = cpnShiftedMDefinition.toDerivative(REFERENCE_DATE_BEFORE);
    final MultipleCurrencyAmount pvP = cpnShiftedP.accept(PVDC, MULTICURVES);
    final MultipleCurrencyAmount pvM = cpnShiftedM.accept(PVDC, MULTICURVES);
    final double mqsExpected = (pvP.getAmount(CAD) - pvM.getAmount(CAD)) / (2 * SHIFT);
    final double mqsCalculator = CPN_BEFORE.accept(PVMQSDC, MULTICURVES);
    final double mqsMethod = METHOD_COMPOUNDED.presentValueSpreadSensitivity(CPN_BEFORE, MULTICURVES);
    assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value market quote sensitivity", mqsExpected, mqsCalculator, TOLERANCE_PV_DELTA);
    assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value market quote sensitivity", mqsCalculator, mqsMethod, TOLERANCE_PV_DELTA);
  }

}
