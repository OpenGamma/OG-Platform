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
import com.opengamma.analytics.financial.instrument.payment.CouponArithmeticAverageONDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
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
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

public class CouponArithmeticAverageONDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IndexON FEDFUND = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final Currency USD = FEDFUND.getCurrency();
  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();

  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final Period TENOR_1Y = Period.ofYears(1);
  private static final double NOTIONAL = 100000000; // 100m

  private static final CouponArithmeticAverageONDefinition FEDFUND_CPN_3M_DEF = CouponArithmeticAverageONDefinition.from(FEDFUND, REFERENCE_DATE, TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, NYC);
  private static final CouponArithmeticAverageON FEDFUND_CPN_3M = FEDFUND_CPN_3M_DEF.toDerivative(REFERENCE_DATE);

  private static final CouponArithmeticAverageONDefinition FEDFUND_CPN_3MFWD_DEF = CouponArithmeticAverageONDefinition.from(FEDFUND,
      ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_1Y, USDLIBOR3M, NYC), TENOR_3M, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, NYC);
  private static final CouponArithmeticAverageON FEDFUND_CPN_3MFWD = FEDFUND_CPN_3MFWD_DEF.toDerivative(REFERENCE_DATE);

  private static final CouponArithmeticAverageONDefinition FEDFUND_CPN_1Y_DEF = CouponArithmeticAverageONDefinition.from(FEDFUND, REFERENCE_DATE, TENOR_1Y, NOTIONAL, 0,
      USDLIBOR3M.getBusinessDayConvention(), true, NYC);
  private static final CouponArithmeticAverageON FEDFUND_CPN_1Y = FEDFUND_CPN_1Y_DEF.toDerivative(REFERENCE_DATE);

  private static final CouponArithmeticAverageONDiscountingMethod METHOD_FF_EXACT = CouponArithmeticAverageONDiscountingMethod.getInstance();
  private static final CouponArithmeticAverageONDiscountingApproxMethod METHOD_FF_APPRO = CouponArithmeticAverageONDiscountingApproxMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscounting2Calculator PVCSD2C = PresentValueCurveSensitivityDiscounting2Calculator.getInstance();

  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC2 = new ParameterSensitivityParameterCalculator<>(PVCSD2C);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_REL = 1.0E-6; // 0.01 bp
  private static final double TOLERANCE_REL_DELTA = 1.0E-3;

  @Test
  public void presentValueExactVsApprox() {
    final MultipleCurrencyAmount pv3MExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3M, MULTICURVES);
    final MultipleCurrencyAmount pv3MAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3M, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MExact.getAmount(USD), pv3MAppro.getAmount(USD),
        TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3M.getFixingPeriodRemainingAccrualFactor());
    final MultipleCurrencyAmount pv1YExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_1Y, MULTICURVES);
    final MultipleCurrencyAmount pv1YAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_1Y, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv1YExact.getAmount(USD), pv1YAppro.getAmount(USD),
        TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_1Y.getFixingPeriodRemainingAccrualFactor());
    final MultipleCurrencyAmount pv3MFwdExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    final MultipleCurrencyAmount pv3MFwdAppro = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MFwdExact.getAmount(USD), pv3MFwdAppro.getAmount(USD),
        TOLERANCE_REL * NOTIONAL * FEDFUND_CPN_3MFWD.getFixingPeriodRemainingAccrualFactor());
  }

  @Test
  public void presentValueApproxMethodVsCalculator() {
    final MultipleCurrencyAmount pv3MMethod = METHOD_FF_APPRO.presentValue(FEDFUND_CPN_3M, MULTICURVES);
    final MultipleCurrencyAmount pv3MCalc = FEDFUND_CPN_3M.accept(PVDC, MULTICURVES);
    assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pv3MMethod.getAmount(USD), pv3MCalc.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityApprox() {
    final MultipleCurrencyParameterSensitivity pvpsApprox = PSC.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PSC_DSC_FD.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsApprox, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityApproxMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcs3MMethod = METHOD_FF_APPRO.presentValueCurveSensitivity(FEDFUND_CPN_3M, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcs3MCalc = FEDFUND_CPN_3M.accept(PVCSDC, MULTICURVES);
    AssertSensivityObjects.assertEquals("CouponArithmeticAverageONDiscountingMethod: present value", pvcs3MMethod, pvcs3MCalc, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityExactVsApprox() {
    final MultipleCurrencyParameterSensitivity pvpsAppro = PSC.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsExact = PSC2.calculateSensitivity(FEDFUND_CPN_3MFWD, MULTICURVES, MULTICURVES.getAllNames());
    AssertSensivityObjects.assertEquals("CouponArithmeticAverageONDiscountingMethod: present value curve sensitivity", pvpsAppro, pvpsExact,
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
      final CouponArithmeticAverageONDefinition ffDefinition = CouponArithmeticAverageONDefinition.from(FEDFUND, ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_1Y, USDLIBOR3M, NYC), TENOR_3M,
          NOTIONAL, 0, USDLIBOR3M.getBusinessDayConvention(), true, NYC);
      final CouponArithmeticAverageON ff = ffDefinition.toDerivative(REFERENCE_DATE);
      pvExact = METHOD_FF_EXACT.presentValue(ff, MULTICURVES);
      //      pvExact = METHOD_FF_EXACT.presentValue(FEDFUND_CPN_3MFWD, MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println("CouponArithmeticAverageONDiscountingMethod: " + nbTest + " pv Arithmetic Average ON - Exact: " + (endTime - startTime) + " ms");
    // Performance note: AA ON exact pv: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 10000 coupons.
    // Performance note: AA ON exact constr. + pv: 26-Mar-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 460 ms for 10000 coupons.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final CouponArithmeticAverageONDefinition ffDefinition = CouponArithmeticAverageONDefinition.from(FEDFUND, ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_1Y, USDLIBOR3M, NYC), TENOR_3M,
          NOTIONAL, 0, USDLIBOR3M.getBusinessDayConvention(), true, NYC);
      final CouponArithmeticAverageON ff = ffDefinition.toDerivative(REFERENCE_DATE);
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
        effectiveDate[loopstart] = ScheduleCalculator.getAdjustedDate(effectiveDate[0], step.multipliedBy(loopstart), USDLIBOR3M, NYC);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(effectiveDate[loopstart], TENOR_3M, USDLIBOR3M, NYC);
        final CouponArithmeticAverageONDefinition cpnONDefinition = CouponArithmeticAverageONDefinition.from(FEDFUND, effectiveDate[loopstart], endDate, NOTIONAL, 0, NYC);
        final CouponArithmeticAverageON cpnON = cpnONDefinition.toDerivative(REFERENCE_DATE);
        // Compute daily forwards
        final int nbON = cpnON.getFixingPeriodAccrualFactors().length;
        final double fwdON[] = new double[nbON];
        for (int loopon = 0; loopon < nbON; loopon++) {
          fwdON[loopon] = multicurvesCst.getForwardRate(FEDFUND, cpnON.getFixingPeriodTimes()[loopon], cpnON.getFixingPeriodTimes()[loopon + 1], cpnON.getFixingPeriodAccrualFactors()[loopon]);
        }
        // Compounded period forward
        payComp[looplevel][loopstart] = multicurvesCst.getForwardRate(FEDFUND, cpnON.getFixingPeriodTimes()[0], cpnON.getFixingPeriodTimes()[nbON], cpnON.getFixingPeriodRemainingAccrualFactor())
            * cpnON.getFixingPeriodRemainingAccrualFactor();
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
