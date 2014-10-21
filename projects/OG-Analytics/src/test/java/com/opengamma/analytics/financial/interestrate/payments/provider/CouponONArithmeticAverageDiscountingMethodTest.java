/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscounting2Calculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Methods related to different ON Arithmetic Average coupons (standard, spread, simplified).
 * Pricing methods are full forward and approximated.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONArithmeticAverageDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IndexON FEDFUND = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final Currency USD = FEDFUND.getCurrency();
  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();

  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = 
      GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final Period TENOR_1Y = Period.ofYears(1);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0010; // 10 bps

  private static final ZonedDateTime FORWARD_DATE = 
      ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_1Y, USDLIBOR3M, NYC);
  
  /** Time series **/
  private static final ZonedDateTimeDoubleTimeSeries TS_ON = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 23), DateUtils.getUTCDate(2011, 5, 24),
            DateUtils.getUTCDate(2011, 5, 25), DateUtils.getUTCDate(2011, 5, 26), DateUtils.getUTCDate(2011, 5, 27),
            DateUtils.getUTCDate(2011, 5, 30), DateUtils.getUTCDate(2011, 5, 31), DateUtils.getUTCDate(2011, 6, 1),
            DateUtils.getUTCDate(2011, 6, 2), DateUtils.getUTCDate(2011, 6, 3), DateUtils.getUTCDate(2011, 6, 6)}, 
          new double[] {0.0500, 0.0100, 0.0100, 0.0100, 0.0100, 0.0100, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200});
  /** Coupon ON AA */
  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3M_DEF = 
      CouponONArithmeticAverageDefinition.from(FEDFUND, REFERENCE_DATE, TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, NYC);
  private static final CouponONArithmeticAverage FEDFUND_CPN_3M = FEDFUND_CPN_3M_DEF.toDerivative(REFERENCE_DATE);
  private static final CouponONArithmeticAverage FEDFUND_CPN_3M_ACCRUED = 
      (CouponONArithmeticAverage) FEDFUND_CPN_3M_DEF.toDerivative(DateUtils.getUTCDate(2011, 6, 7), TS_ON);
  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_3MFWD_DEF = 
      CouponONArithmeticAverageDefinition.from(FEDFUND, FORWARD_DATE, TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, NYC);
  private static final CouponONArithmeticAverage FEDFUND_CPN_3MFWD = FEDFUND_CPN_3MFWD_DEF.toDerivative(REFERENCE_DATE);  
  private static final CouponONArithmeticAverageDefinition FEDFUND_CPN_1Y_DEF = 
      CouponONArithmeticAverageDefinition.from(FEDFUND, REFERENCE_DATE, TENOR_1Y, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, NYC);
  private static final CouponONArithmeticAverage FEDFUND_CPN_1Y = FEDFUND_CPN_1Y_DEF.toDerivative(REFERENCE_DATE);
  /** Coupon ON AA - spread */

  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_SPREAD_3M_DEF = 
      CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, REFERENCE_DATE, TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, SPREAD, NYC);
//  private static final CouponONArithmeticAverageSpread FEDFUND_CPN_SPREAD_3M = FEDFUND_CPN_SPREAD_3M_DEF.toDerivative(REFERENCE_DATE);
  private static final CouponONArithmeticAverageSpread FEDFUND_CPN_SPREAD_3M_ACCRUED = 
      (CouponONArithmeticAverageSpread) FEDFUND_CPN_SPREAD_3M_DEF.toDerivative(DateUtils.getUTCDate(2011, 6, 7), TS_ON);
  
  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_SPREAD_3MFWD_DEF = 
      CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, FORWARD_DATE, TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, SPREAD, NYC);
  private static final CouponONArithmeticAverageSpread FEDFUND_CPN_SPREAD_3MFWD = 
      FEDFUND_CPN_SPREAD_3MFWD_DEF.toDerivative(REFERENCE_DATE);
  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_SPREAD0_3MFWD_DEF = 
      CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, FORWARD_DATE, TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, 0.0d, NYC);
  private static final CouponONArithmeticAverageSpread FEDFUND_CPN_SPREAD0_3MFWD = 
      FEDFUND_CPN_SPREAD0_3MFWD_DEF.toDerivative(REFERENCE_DATE);
  /** Coupon ON AA - spread simplified */
  private static final CouponONArithmeticAverageSpreadSimplifiedDefinition FEDFUND_CPN_3M_SIMPL0_DEFINITION = 
      CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND,
      FORWARD_DATE, FORWARD_DATE.plusMonths(3), NOTIONAL, 0, 0.0, NYC);
  private static final CouponONArithmeticAverageSpreadSimplified FEDFUND_CPN_3M_SIMPL0 = 
      FEDFUND_CPN_3M_SIMPL0_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponONArithmeticAverageSpreadSimplifiedDefinition FEDFUND_CPN_3M_SIMPL_DEFINITION = 
      CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND,
      FORWARD_DATE, FORWARD_DATE.plusMonths(3), NOTIONAL, 0, SPREAD, NYC);
  private static final CouponONArithmeticAverageSpreadSimplified FEDFUND_CPN_3M_SIMPL = 
      FEDFUND_CPN_3M_SIMPL_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponONArithmeticAverageDiscountingMethod METHOD_FF_EXACT = 
      CouponONArithmeticAverageDiscountingMethod.getInstance();
  private static final CouponONArithmeticAverageDiscountingApproxMethod METHOD_FF_APPRO = 
      CouponONArithmeticAverageDiscountingApproxMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadDiscountingMethod METHOD_FF_EXACT_SPREAD = 
      CouponONArithmeticAverageSpreadDiscountingMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadDiscountingApproxMethod METHOD_FF_APPRO_SPREAD = 
      CouponONArithmeticAverageSpreadDiscountingApproxMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod METHOD_AA_SIMPL = 
      CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = 
      PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscounting2Calculator PVCSD2C = 
      PresentValueCurveSensitivityDiscounting2Calculator.getInstance();

  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC2 = 
      new ParameterSensitivityParameterCalculator<>(PVCSD2C);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = 
      new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_REL = 1.0E-6; // 0.01 bp
  private static final double TOLERANCE_REL_DELTA = 1.0E-3;
  private static final double TOLERANCE_REL_DELTA_2 = 1.0E-4;

  @Test
  public void presentValueExactVsApprox() {
    final MultipleCurrencyAmount pv3MExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3M, MULTICURVES);
    final MultipleCurrencyAmount pv3MAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3M, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MExact.getAmount(USD), 
        pv3MAppro.getAmount(USD),
        TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3M.getFixingPeriodRemainingAccrualFactor());
    final MultipleCurrencyAmount pv1YExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_1Y, MULTICURVES);
    final MultipleCurrencyAmount pv1YAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_1Y, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv1YExact.getAmount(USD), 
        pv1YAppro.getAmount(USD),
        TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_1Y.getFixingPeriodRemainingAccrualFactor());
    final MultipleCurrencyAmount pv3MFwdExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    final MultipleCurrencyAmount pv3MFwdAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MFwdExact.getAmount(USD), 
        pv3MFwdAppro.getAmount(USD), TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3MFWD.getFixingPeriodRemainingAccrualFactor());
    final MultipleCurrencyAmount pv3MFwdSpread0Exact = 
        METHOD_FF_EXACT_SPREAD.presentValue(FEDFUND_CPN_SPREAD0_3MFWD, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MFwdSpread0Exact.getAmount(USD), 
        pv3MFwdExact.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount pv3MFwdSpread0Appro = 
        METHOD_FF_APPRO_SPREAD.presentValue(FEDFUND_CPN_SPREAD0_3MFWD, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MFwdSpread0Appro.getAmount(USD), 
        pv3MFwdAppro.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueWithAccruedExactVsApprox() {
    final MultipleCurrencyAmount pv3MExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3M_ACCRUED, MULTICURVES);
    final MultipleCurrencyAmount pv3MAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3M_ACCRUED, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MExact.getAmount(USD), 
        pv3MAppro.getAmount(USD), TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3M.getFixingPeriodRemainingAccrualFactor());
  }
  
  @Test
  public void presentValueWithAccruedWithSpreadExactVsApprox() {
    final MultipleCurrencyAmount pv3MExact = METHOD_FF_EXACT_SPREAD.presentValue(FEDFUND_CPN_SPREAD_3M_ACCRUED, MULTICURVES);
    final MultipleCurrencyAmount pv3MAppro = METHOD_FF_APPRO_SPREAD.presentValue(FEDFUND_CPN_SPREAD_3M_ACCRUED, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MExact.getAmount(USD), 
        pv3MAppro.getAmount(USD), TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3M.getFixingPeriodRemainingAccrualFactor());
  }
  

  @Test
  public void presentValueSpreadExactVsApprox() {
    final MultipleCurrencyAmount pv3MExact = METHOD_FF_EXACT_SPREAD.presentValue(FEDFUND_CPN_SPREAD_3MFWD, MULTICURVES);
    final MultipleCurrencyAmount pv3MAppro = METHOD_FF_APPRO_SPREAD.presentValue(FEDFUND_CPN_SPREAD_3MFWD, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MExact.getAmount(USD), pv3MAppro.getAmount(USD),
        TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3M.getFixingPeriodRemainingAccrualFactor());
  }

  @Test
  public void presentValueFullVsSimplified() {
    final MultipleCurrencyAmount pv3MFull = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    final MultipleCurrencyAmount pv3MSimp0 = METHOD_AA_SIMPL.presentValue(FEDFUND_CPN_3M_SIMPL0, MULTICURVES);
    assertEquals("CouponONArithmeticAverageSpreadSimpleDiscountingMethod: present value", pv3MFull.getAmount(USD), 
        pv3MSimp0.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount pv3MSimp = METHOD_AA_SIMPL.presentValue(FEDFUND_CPN_3M_SIMPL, MULTICURVES);
    final PaymentFixed spreadPayment = new PaymentFixed(USD, FEDFUND_CPN_3M_SIMPL.getPaymentTime(), 
        FEDFUND_CPN_3M_SIMPL.getSpreadAmount());
    final MultipleCurrencyAmount pvSpread = spreadPayment.accept(PVDC, MULTICURVES);
    assertEquals("CouponONArithmeticAverageSpreadSimpleDiscountingMethod: present value", 
        pv3MSimp0.plus(pvSpread).getAmount(USD), pv3MSimp.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueApproxMethodVsCalculator() {
    final MultipleCurrencyAmount pv3MMethod = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3M, MULTICURVES);
    final MultipleCurrencyAmount pv3MCalc = FEDFUND_CPN_3M.accept(PVDC, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MMethod.getAmount(USD), 
        pv3MCalc.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount pv3MMethodSpSi = METHOD_AA_SIMPL.presentValue(FEDFUND_CPN_3M_SIMPL, MULTICURVES);
    final MultipleCurrencyAmount pv3MCalcSpSi = FEDFUND_CPN_3M_SIMPL.accept(PVDC, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MMethodSpSi.getAmount(USD), 
        pv3MCalcSpSi.getAmount(USD), TOLERANCE_PV);
    final MultipleCurrencyAmount pv3MSpreadMethod = METHOD_FF_APPRO_SPREAD.presentValue(FEDFUND_CPN_SPREAD_3MFWD, MULTICURVES);
    final MultipleCurrencyAmount pv3MSpreadCalc = FEDFUND_CPN_SPREAD_3MFWD.accept(PVDC, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MSpreadMethod.getAmount(USD), 
        pv3MSpreadCalc.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityApprox() {
    final MultipleCurrencyParameterSensitivity pvpsApprox = PSC.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, 
        MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PSC_DSC_FD.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsApprox, 
        pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivitySpreadExactVsApprox() {
    final MultipleCurrencyParameterSensitivity pvcs3MExact = PSC2.calculateSensitivity(FEDFUND_CPN_SPREAD_3MFWD, MULTICURVES);
    final MultipleCurrencyParameterSensitivity pvcs3MAppro = PSC.calculateSensitivity(FEDFUND_CPN_SPREAD_3MFWD, MULTICURVES);;
    AssertSensitivityObjects.assertEquals("CouponArithmeticAverageONDiscountingMethod: present value curve sensitivity", 
        pvcs3MExact, pvcs3MAppro, TOLERANCE_REL_DELTA_2 * NOTIONAL);
  }

  @Test
  public void presentValueCurveSensitivitySimplifiedSpread() {
    final MultipleCurrencyParameterSensitivity pvpsFwd = PSC.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, MULTICURVES.getAllNames());
    final PaymentFixed spreadPayment = new PaymentFixed(USD, FEDFUND_CPN_3M_SIMPL.getPaymentTime(), FEDFUND_CPN_3M_SIMPL.getSpreadAmount());
    final MultipleCurrencyParameterSensitivity pvpsSpread = 
        PSC.calculateSensitivity(spreadPayment, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsExpected = pvpsFwd.plus(pvpsSpread);
    final MultipleCurrencyParameterSensitivity pvpsSpreadSimpl = 
        PSC.calculateSensitivity(FEDFUND_CPN_3M_SIMPL, MULTICURVES, MULTICURVES.getAllNames());
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", 
        pvpsExpected, pvpsSpreadSimpl, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityApproxMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcs3MMethod = METHOD_FF_APPRO.presentValueCurveSensitivity(FEDFUND_CPN_3M, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcs3MCalc = FEDFUND_CPN_3M.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pvcs3MMethod, pvcs3MCalc, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityExactVsApprox() {
    final MultipleCurrencyParameterSensitivity pvpsAppro = PSC.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsExact = PSC2.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, MULTICURVES.getAllNames());
    AssertSensitivityObjects.assertEquals("CouponArithmeticAverageONDiscountingMethod: present value curve sensitivity", pvpsAppro, pvpsExact,
        TOLERANCE_REL_DELTA * NOTIONAL * FEDFUND_CPN_3MFWD.getFixingPeriodRemainingAccrualFactor());
  }

  @Test(enabled = false)
  /**
   * Compare the performance of the approximated method to the exact method.
   */
  public void performance() {

    long startTime, endTime;
    final int nbTest = 10000;
    @SuppressWarnings("unused")
    MultipleCurrencyAmount pvExact = MultipleCurrencyAmount.of(USD, 0.0);
    @SuppressWarnings("unused")
    MultipleCurrencyAmount pvAppro = MultipleCurrencyAmount.of(USD, 0.0);
    @SuppressWarnings("unused")
    MultipleCurrencyMulticurveSensitivity pvcsAppro;
    @SuppressWarnings("unused")
    MultipleCurrencyMulticurveSensitivity pvcsExact;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final CouponONArithmeticAverageDefinition ffDefinition = CouponONArithmeticAverageDefinition.from(FEDFUND, 
          ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_1Y, USDLIBOR3M, NYC), TENOR_3M, NOTIONAL, 0, 
          USDLIBOR3M.getBusinessDayConvention(), true, NYC);
      final CouponONArithmeticAverage ff = ffDefinition.toDerivative(REFERENCE_DATE);
      pvExact = METHOD_FF_EXACT.presentValue(ff, MULTICURVES);
      //      pvExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CouponArithmeticAverageONDiscountingMethod: " + nbTest + " pv Arithmetic Average ON - Exact: " + (endTime - startTime) + " ms");
    // Performance note: AA ON exact pv: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 10000 coupons.
    // Performance note: AA ON exact constr. + pv: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 460 ms for 10000 coupons.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final CouponONArithmeticAverageDefinition ffDefinition = CouponONArithmeticAverageDefinition.from(FEDFUND, 
          ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_1Y, USDLIBOR3M, NYC),
          TENOR_3M,
          NOTIONAL, 0, USDLIBOR3M.getBusinessDayConvention(), true, NYC);
      final CouponONArithmeticAverage ff = ffDefinition.toDerivative(REFERENCE_DATE);
      pvAppro = METHOD_FF_APPRO.presentValue(ff, MULTICURVES);
      //      pvAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CouponArithmeticAverageONDiscountingMethod: " + nbTest + " pv Arithmetic Average ON - Approximation: " + (endTime - startTime) + " ms");
    // Performance note: AA ON approx pv: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 5 ms for 10000 coupons.
    // Performance note: AA ON approx constr. + pv: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 250 ms for 10000 coupons.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsExact = METHOD_FF_EXACT.presentValueCurveSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CouponArithmeticAverageONDiscountingMethod: " + nbTest + " pvcs Arithmetic Average ON - Exact: " + (endTime - startTime) + " ms");
    // Performance note: AA ON exact pvcs: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 220 ms for 10000 coupons.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsAppro = METHOD_FF_APPRO.presentValueCurveSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CouponArithmeticAverageONDiscountingMethod: " + nbTest + " pvcs Arithmetic Average ON - Approximation: " + (endTime - startTime) + " ms");
    // Performance note: AA ON approx pvcs: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 12 ms for 10000 coupons.

  }

  @Test(enabled = false)
  /**
   * Reports the error of the arithmetic average approximation by the log of the compounded rate.
   */
  public void averageApproximation() {

    final MulticurveProviderDiscount multicurvesCst = new MulticurveProviderDiscount();
    YieldAndDiscountCurve curveCst = YieldCurve.from(ConstantDoublesCurve.from(0.0, "CST"));
    multicurvesCst.setCurve(FEDFUND, curveCst);

    final double[] rateLevel = {0.01, 0.05, 0.10 };
    final int nbLevel = rateLevel.length;
    final int nbStart = 36;
    final Period step = Period.ofMonths(1);
    final ZonedDateTime[] effectiveDate = new ZonedDateTime[nbStart];
    effectiveDate[0] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, GENERATOR_SWAP_EONIA.getSpotLag(), NYC);

    final double[][] payComp = new double[nbLevel][nbStart];
    final double[][] payAA = new double[nbLevel][nbStart];
    final double[][] payAAApprox = new double[nbLevel][nbStart];
    final double[][] rateComp = new double[nbLevel][nbStart];
    final double[][] rateAA = new double[nbLevel][nbStart];
    final double[][] rateAAApprox = new double[nbLevel][nbStart];

    for (int looplevel = 0; looplevel < nbLevel; looplevel++) {
      curveCst = YieldCurve.from(ConstantDoublesCurve.from(rateLevel[looplevel], "CST"));
      multicurvesCst.replaceCurve(FEDFUND, curveCst);

      for (int loopstart = 0; loopstart < nbStart; loopstart++) {
        effectiveDate[loopstart] = ScheduleCalculator.getAdjustedDate(effectiveDate[0], step.multipliedBy(loopstart), 
            USDLIBOR3M, NYC);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(effectiveDate[loopstart], TENOR_3M, USDLIBOR3M, NYC);
        final CouponONArithmeticAverageDefinition cpnONDefinition = CouponONArithmeticAverageDefinition.from(FEDFUND, 
            effectiveDate[loopstart], endDate, NOTIONAL, 0, NYC);
        final CouponONArithmeticAverage cpnON = cpnONDefinition.toDerivative(REFERENCE_DATE);
        // Compute daily forwards
        final int nbON = cpnON.getFixingPeriodAccrualFactors().length;
        final double fwdON[] = new double[nbON];
        for (int loopon = 0; loopon < nbON; loopon++) {
          fwdON[loopon] = multicurvesCst.getSimplyCompoundForwardRate(FEDFUND, cpnON.getFixingPeriodStartTimes()[loopon], 
              cpnON.getFixingPeriodEndTimes()[loopon], cpnON.getFixingPeriodAccrualFactors()[loopon]);
        }
        // Compounded period forward
        payComp[looplevel][loopstart] = multicurvesCst.getSimplyCompoundForwardRate(FEDFUND, 
            cpnON.getFixingPeriodStartTimes()[0], cpnON.getFixingPeriodStartTimes()[nbON],
            cpnON.getFixingPeriodRemainingAccrualFactor())
            *
            cpnON.getFixingPeriodRemainingAccrualFactor();
        payAA[looplevel][loopstart] = 0;
        for (int loopon = 0; loopon < nbON; loopon++) {
          payAA[looplevel][loopstart] += fwdON[loopon] * cpnON.getFixingPeriodAccrualFactors()[loopon];
        }
        payAAApprox[looplevel][loopstart] = Math.log(1 + payComp[looplevel][loopstart]);
        rateComp[looplevel][loopstart] = payComp[looplevel][loopstart] / cpnON.getFixingPeriodRemainingAccrualFactor();
        rateAA[looplevel][loopstart] = payAA[looplevel][loopstart] / cpnON.getFixingPeriodRemainingAccrualFactor();
        rateAAApprox[looplevel][loopstart] = payAAApprox[looplevel][loopstart] / cpnON.getFixingPeriodRemainingAccrualFactor();
      }

    }
    //    int t = 0;
    //    t++;
  }

}
